package com.iqscaffold.pipelineservice.config;

import org.springframework.amqp.core.Binding;
import org.springframework.amqp.core.BindingBuilder;
import org.springframework.amqp.core.Queue;
import org.springframework.amqp.core.QueueBuilder;
import org.springframework.amqp.core.TopicExchange;
import org.springframework.amqp.rabbit.config.SimpleRabbitListenerContainerFactory;
import org.springframework.amqp.rabbit.connection.ConnectionFactory;
import org.springframework.amqp.rabbit.core.RabbitAdmin;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;
import org.springframework.amqp.support.converter.MessageConverter;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * RabbitMQ configuration for Pipeline Service.
 * Configures exchanges, queues, and bindings for lead lifecycle events.
 */
@Configuration
@ConditionalOnClass(ConnectionFactory.class)
public class RabbitMQConfig {

  public static final String EXCHANGE_NAME = "iqscaffold.events";
  public static final String DLX_EXCHANGE = "iqscaffold.dlx";
  public static final String LEAD_CREATED_QUEUE = "iqscaffold.pipeline.lead.created";
  public static final String LEAD_DELETED_QUEUE = "iqscaffold.pipeline.lead.deleted";
  public static final String CONTACT_CREATED_QUEUE = "iqscaffold.pipeline.contact.created";
  public static final String DLQ = "iqscaffold.dlq";
  public static final String LEAD_CREATED_ROUTING_KEY = "lead.created";
  public static final String LEAD_DELETED_ROUTING_KEY = "lead.deleted";
  public static final String CONTACT_CREATED_ROUTING_KEY = "contact.created";
  public static final String STAGE_CHANGED_ROUTING_KEY = "stage.changed";
  public static final String FOLLOWUP_SCHEDULED_ROUTING_KEY = "followup.scheduled";
  public static final String FOLLOWUP_COMPLETED_ROUTING_KEY = "followup.completed";

  @Bean
  public TopicExchange crmEventsExchange() {
    return new TopicExchange(EXCHANGE_NAME, true, false);
  }

  /**
   * Dead Letter Exchange for failed messages
   */
  @Bean
  public TopicExchange deadLetterExchange() {
    return new TopicExchange(DLX_EXCHANGE, true, false);
  }

  @Bean
  public Queue leadCreatedQueue() {
    return QueueBuilder
        .durable(LEAD_CREATED_QUEUE)
        .withArgument("x-dead-letter-exchange", DLX_EXCHANGE)
        .withArgument("x-message-ttl", 86400000) // 24 hours
        .build();
  }

  @Bean
  public Queue leadDeletedQueue() {
    return QueueBuilder
        .durable(LEAD_DELETED_QUEUE)
        .withArgument("x-dead-letter-exchange", DLX_EXCHANGE)
        .withArgument("x-message-ttl", 86400000) // 24 hours
        .build();
  }

  @Bean
  public Queue contactCreatedQueue() {
    return QueueBuilder
        .durable(CONTACT_CREATED_QUEUE)
        .withArgument("x-dead-letter-exchange", DLX_EXCHANGE)
        .withArgument("x-message-ttl", 86400000) // 24 hours
        .build();
  }

  /**
   * Dead Letter Queue for failed messages
   */
  @Bean
  public Queue deadLetterQueue() {
    return QueueBuilder
        .durable(DLQ)
        .build();
  }

  @Bean
  public Binding leadCreatedBinding(final Queue leadCreatedQueue, final TopicExchange crmEventsExchange) {
    return BindingBuilder.bind(leadCreatedQueue)
        .to(crmEventsExchange)
        .with(LEAD_CREATED_ROUTING_KEY);
  }

  @Bean
  public Binding leadDeletedBinding(final Queue leadDeletedQueue, final TopicExchange crmEventsExchange) {
    return BindingBuilder.bind(leadDeletedQueue)
        .to(crmEventsExchange)
        .with(LEAD_DELETED_ROUTING_KEY);
  }

  @Bean
  public Binding contactCreatedBinding(final Queue contactCreatedQueue, final TopicExchange crmEventsExchange) {
    return BindingBuilder.bind(contactCreatedQueue)
        .to(crmEventsExchange)
        .with(CONTACT_CREATED_ROUTING_KEY);
  }

  /**
   * Bind dead letter queue to DLX with all routing keys
   */
  @Bean
  public Binding deadLetterBinding() {
    return BindingBuilder
        .bind(deadLetterQueue())
        .to(deadLetterExchange())
        .with("#");
  }

  @Bean
  public MessageConverter jsonMessageConverter() {
    return new Jackson2JsonMessageConverter();
  }

  @Bean
  public RabbitAdmin rabbitAdmin(final ConnectionFactory connectionFactory) {
    return new RabbitAdmin(connectionFactory);
  }

  @Bean
  public RabbitTemplate rabbitTemplate(final ConnectionFactory connectionFactory) {
    final RabbitTemplate rabbitTemplate = new RabbitTemplate(connectionFactory);
    rabbitTemplate.setMessageConverter(jsonMessageConverter());
    return rabbitTemplate;
  }

  @Bean
  public SimpleRabbitListenerContainerFactory rabbitListenerContainerFactory(
      final ConnectionFactory connectionFactory) {
    final SimpleRabbitListenerContainerFactory factory = new SimpleRabbitListenerContainerFactory();
    factory.setConnectionFactory(connectionFactory);
    factory.setMessageConverter(jsonMessageConverter());
    factory.setMissingQueuesFatal(false);
    return factory;
  }


  /**
   * Tenant events queue with dead letter routing
   * Receives tenant lifecycle events from User Service
   */
  @Bean
  public Queue tenantEventsQueue() {
    return QueueBuilder
        .durable("iqscaffold.pipeline.tenant.events")
        .withArgument("x-dead-letter-exchange", DLX_EXCHANGE)
        .withArgument("x-message-ttl", 86400000) // 24 hours
        .build();
  }

  /**
   * Bind tenant events queue to exchange with tenant.# routing key
   * Receives all tenant lifecycle events (created, updated, deleted)
   */
  @Bean
  public Binding tenantEventsBinding(
      final Queue tenantEventsQueue,
      final TopicExchange crmEventsExchange) {
    return BindingBuilder.bind(tenantEventsQueue)
        .to(crmEventsExchange)
        .with("tenant.#");
  }

}
