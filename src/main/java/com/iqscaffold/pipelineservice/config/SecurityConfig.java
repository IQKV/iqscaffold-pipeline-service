package com.iqscaffold.pipelineservice.config;

import com.iqscaffold.pipelineservice.security.JwtAuthenticationFilter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.security.oauth2.jwt.NimbusJwtDecoder;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationConverter;
import org.springframework.security.oauth2.server.resource.authentication.JwtGrantedAuthoritiesConverter;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

@Configuration
@EnableWebSecurity
@EnableMethodSecurity(prePostEnabled = true)
public class SecurityConfig {

  private final JwtAuthenticationFilter jwtAuthenticationFilter;
  private final IqScaffoldProperties iqScaffoldProperties;

  public SecurityConfig(final JwtAuthenticationFilter jwtAuthenticationFilter,
      final IqScaffoldProperties iqScaffoldProperties) {
    this.jwtAuthenticationFilter = jwtAuthenticationFilter;
    this.iqScaffoldProperties = iqScaffoldProperties;
  }

  @Bean
  public SecurityFilterChain securityFilterChain(final HttpSecurity http) throws Exception {
    http
        .csrf(csrf -> csrf.disable())
        .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
        .authorizeHttpRequests(auth -> auth
            .requestMatchers("/actuator/**", "/swagger-ui/**", "/v3/api-docs/**",
                "/api/v1/pipeline/health").permitAll()
            .anyRequest().authenticated())
        .oauth2ResourceServer(oauth2 -> oauth2
            .jwt(jwt -> jwt
                .decoder(jwtDecoder())
                .jwtAuthenticationConverter(jwtAuthenticationConverter())))
        .addFilterAfter(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class);

    return http.build();
  }

  @Bean
  public JwtDecoder jwtDecoder() {
    var jwtProps = iqScaffoldProperties.getSecurity().getJwt();

    if (isSymmetricConfigured()) {
      var algorithm = jwtProps.getAlgorithm() != null ? jwtProps.getAlgorithm() : "HS256";
      javax.crypto.SecretKey key = new javax.crypto.spec.SecretKeySpec(
          jwtProps.getSecretKey().getBytes(), "Hmac" + algorithm.substring(2));
      return NimbusJwtDecoder.withSecretKey(key).build();
    }

    var jwkSetUri = jwtProps.getJwkSetUri();
    if (jwkSetUri == null || jwkSetUri.isBlank()) {
      throw new IllegalStateException("Neither secret-key nor jwk-set-uri is configured for JWT validation");
    }
    return NimbusJwtDecoder.withJwkSetUri(jwkSetUri).build();
  }

  private boolean isSymmetricConfigured() {
    var secretKey = iqScaffoldProperties.getSecurity().getJwt().getSecretKey();
    return secretKey != null && !secretKey.isBlank() && !"change-me-in-production".equals(secretKey);
  }

  @Bean
  public JwtAuthenticationConverter jwtAuthenticationConverter() {
    JwtGrantedAuthoritiesConverter authoritiesConverter = new JwtGrantedAuthoritiesConverter();
    authoritiesConverter.setAuthorityPrefix("");
    authoritiesConverter.setAuthoritiesClaimName("roles");

    JwtAuthenticationConverter authenticationConverter = new JwtAuthenticationConverter();
    authenticationConverter.setJwtGrantedAuthoritiesConverter(authoritiesConverter);
    return authenticationConverter;
  }
}
