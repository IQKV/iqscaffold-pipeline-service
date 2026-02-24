# 📊 IQ Scaffold Pipeline Service

> CRM pipeline management microservice providing lead tracking, pipeline stage management, follow-up scheduling, and comprehensive sales analytics with multi-tenant support and event-driven integration.

## Table of Contents

- [Business Purpose](#business-purpose)
- [Overview](#overview)
- [What It Demonstrates](#what-it-demonstrates)
- [Architecture Patterns](#architecture-patterns)
- [Technical Highlights](#technical-highlights)
- [Use Cases Implemented](#use-cases-implemented)
- [API Endpoints](#api-endpoints)
- [API Examples](#api-examples)
- [Learning Points](#learning-points)
- [Adapting for Your Domain](#adapting-for-your-domain)
- [Integration with Other Services](#integration-with-other-services)
- [Deployment Guide](docs/deployment/README.md)

## Business Purpose

CRM Pipeline management service that handles:

- **Pipeline Stage Management** - Configurable sales pipeline stages with customizable workflows and stage transitions
- **Lead Tracking** - Complete lead lifecycle tracking through pipeline stages with conversion analytics
- **Follow-up Scheduling** - Automated follow-up scheduling with due date tracking and overdue notifications
- **Activity Logging** - Comprehensive audit trail of all lead interactions and stage transitions
- **Sales Analytics** - Real-time dashboard statistics, conversion metrics, and pipeline velocity analysis
- **Multi-Tenancy** - Complete tenant isolation ensuring data segregation across organizations with schema-per-tenant strategy

## Overview

This is the pipeline management hub for the IQ Scaffold CRM platform. It centralizes sales pipeline tracking, enabling sales teams to manage leads through customizable stages, schedule follow-ups, and analyze conversion performance while maintaining comprehensive activity logs and analytics.

## What It Demonstrates

### 📊 Pipeline Management & Analytics

- Configurable pipeline stages with customizable workflows (New → Contacted → Qualified → Proposal → Won/Lost)
- Lead progression tracking through pipeline stages with conversion analytics
- Real-time dashboard statistics and pipeline velocity metrics
- Follow-up scheduling with due date management and overdue tracking
- Comprehensive activity logging for all pipeline interactions

### 📈 Sales Performance Analytics

- Conversion rate analysis across pipeline stages
- Pipeline velocity tracking and bottleneck identification
- Lead source performance and ROI analysis
- Sales team performance metrics and reporting
- Time-to-close analysis and forecasting

### 🔄 Workflow Automation

- Automated stage transitions based on lead actions
- Follow-up reminder notifications and scheduling
- Pipeline health monitoring and alerts
- Integration with lead and contact services for seamless workflow

## Architecture Patterns

### Key Design Patterns

- Repository pattern for data access with custom queries
- Service layer for business logic and pipeline management
- DTO pattern with Java records for API contracts
- Event-driven architecture with RabbitMQ integration
- Pipeline stage workflow patterns
- Multi-tenant context management with schema isolation

### API Design

- RESTful endpoints with proper HTTP methods and status codes
- Versioning support (URL-based: `/api/v1/`)
- OpenAPI/Swagger documentation with comprehensive examples
- Dashboard and analytics endpoints for real-time insights
- Consistent error response format with Problem Details (RFC 7807)
- Pagination and sorting support for list operations

## Technical Highlights

### Pipeline Management Features

- Configurable pipeline stages with custom ordering and workflows
- Lead progression tracking with stage transition history
- Follow-up scheduling with due date management and notifications
- Activity logging for comprehensive audit trails
- Dashboard analytics with conversion metrics and velocity tracking
- Pipeline health monitoring and bottleneck identification

### Performance Optimization

- Redis caching for pipeline statistics and frequently accessed data
- Efficient database queries with proper indexing
- Connection pooling for database connections
- Async processing for analytics calculations
- Optimized dashboard queries for real-time performance

### Data Management

- Liquibase for database migrations with tenant-specific schemas
- PostgreSQL with proper indexing and constraints
- Transaction management with rollback support
- Audit fields for tracking creation and modification
- Pipeline stage ordering and workflow management

### Multi-Tenancy Implementation

- Schema-per-tenant isolation with automatic context resolution
- Tenant context extraction from JWT tokens and headers
- Tenant-scoped repositories and queries
- Cross-tenant data isolation and security
- Tenant-aware pipeline configurations

### Testing Approach

- Unit tests with JUnit 5 and Mockito
- Integration tests with Testcontainers
- Architecture tests with ArchUnit for layer validation
- Code coverage with JaCoCo (70% minimum)
- Pipeline workflow testing

### Operational Features

- Docker containerization with multi-stage builds
- Environment-specific profiles (local, staging, production)
- Structured JSON logging with correlation IDs
- Health checks and actuator endpoints
- Prometheus metrics integration
- OpenTelemetry distributed tracing

## Use Cases Implemented

### Pipeline Stage Management

- Create and configure custom pipeline stages
- Define stage ordering and transition rules
- Update stage properties and workflows
- Delete unused stages with proper cleanup
- Reorder stages for optimal workflow

### Lead Pipeline Tracking

- Add leads to pipeline with initial stage assignment
- Track lead progression through pipeline stages
- Move leads between stages with transition logging
- Remove leads from pipeline with proper cleanup
- Monitor lead velocity and conversion rates

### Follow-up Management

- Schedule follow-ups with due dates and reminders
- Track follow-up completion and outcomes
- Identify overdue follow-ups for immediate attention
- Manage daily follow-up schedules
- Log follow-up activities and results

### Activity Logging

- Log all pipeline interactions and stage transitions
- Track lead-specific activity timelines
- Monitor user actions and system events
- Provide comprehensive audit trails
- Support compliance and reporting requirements

### Dashboard Analytics

- Real-time pipeline statistics and metrics
- Conversion rate analysis across stages
- Pipeline velocity and time-to-close tracking
- Lead source performance analysis
- Sales team performance metrics

## API Endpoints

<details>
<summary>Click to expand API endpoints</summary>

### Pipeline Stages

- `GET /api/v1/pipeline/stages` - List all pipeline stages
- `POST /api/v1/pipeline/stages` - Create pipeline stage
- `GET /api/v1/pipeline/stages/{id}` - Get stage by ID
- `PUT /api/v1/pipeline/stages/{id}` - Update stage
- `DELETE /api/v1/pipeline/stages/{id}` - Delete stage
- `PUT /api/v1/pipeline/stages/{id}/order` - Reorder stages

### Pipeline Items (Leads in Pipeline)

- `GET /api/v1/pipeline/items` - List pipeline items
- `POST /api/v1/pipeline/items` - Add lead to pipeline
- `GET /api/v1/pipeline/items/{id}` - Get pipeline item
- `PUT /api/v1/pipeline/items/{id}` - Update pipeline item
- `PUT /api/v1/pipeline/items/{id}/stage` - Move to different stage
- `DELETE /api/v1/pipeline/items/{id}` - Remove from pipeline

### Follow-ups

- `GET /api/v1/pipeline/follow-ups` - List follow-ups
- `POST /api/v1/pipeline/follow-ups` - Schedule follow-up
- `GET /api/v1/pipeline/follow-ups/{id}` - Get follow-up
- `PUT /api/v1/pipeline/follow-ups/{id}` - Update follow-up
- `PUT /api/v1/pipeline/follow-ups/{id}/complete` - Mark as completed
- `GET /api/v1/pipeline/follow-ups/today` - Get today's follow-ups
- `GET /api/v1/pipeline/follow-ups/overdue` - Get overdue follow-ups

### Activity Log

- `GET /api/v1/pipeline/activities` - List activities
- `POST /api/v1/pipeline/activities` - Log activity
- `GET /api/v1/pipeline/activities/{id}` - Get activity
- `GET /api/v1/pipeline/activities/lead/{leadId}` - Get lead activities

### Dashboard & Stats

- `GET /api/v1/pipeline/dashboard/stats` - Get pipeline statistics
- `GET /api/v1/pipeline/dashboard/conversion` - Get conversion rates
- `GET /api/v1/pipeline/dashboard/velocity` - Get pipeline velocity

</details>

## API Examples

### Pipeline Stage Management

#### Create Pipeline Stage

- `POST /api/v1/pipeline/stages` - Create new pipeline stage

**Request:**

```json
{
  "name": "Proposal Sent",
  "description": "Proposal has been sent to the prospect",
  "order": 4,
  "color": "#FFA500",
  "isActive": true
}
```

**Response (201 Created):**

```json
{
  "id": 1,
  "name": "Proposal Sent",
  "description": "Proposal has been sent to the prospect",
  "order": 4,
  "color": "#FFA500",
  "isActive": true,
  "createdAt": "2026-02-03T10:30:00Z",
  "updatedAt": "2026-02-03T10:30:00Z"
}
```

#### List Pipeline Stages

- `GET /api/v1/pipeline/stages` - Get all pipeline stages

**Response (200 OK):**

```json
[
  {
    "id": 1,
    "name": "New",
    "description": "Newly created leads",
    "order": 1,
    "color": "#E3F2FD",
    "isActive": true
  },
  {
    "id": 2,
    "name": "Contacted",
    "description": "Initial contact made",
    "order": 2,
    "color": "#FFF3E0",
    "isActive": true
  },
  {
    "id": 3,
    "name": "Qualified",
    "description": "Lead qualified as potential customer",
    "order": 3,
    "color": "#E8F5E8",
    "isActive": true
  }
]
```

### Pipeline Item Management

#### Add Lead to Pipeline

- `POST /api/v1/pipeline/items` - Add lead to pipeline

**Request:**

```json
{
  "leadId": "lead-123",
  "stageId": 1,
  "value": 5000.0,
  "probability": 25,
  "expectedCloseDate": "2026-03-15T00:00:00Z",
  "notes": "Initial contact made, showing interest"
}
```

**Response (201 Created):**

```json
{
  "id": 1,
  "leadId": "lead-123",
  "stageId": 1,
  "stageName": "New",
  "value": 5000.0,
  "probability": 25,
  "expectedCloseDate": "2026-03-15T00:00:00Z",
  "notes": "Initial contact made, showing interest",
  "createdAt": "2026-02-03T10:30:00Z",
  "updatedAt": "2026-02-03T10:30:00Z"
}
```

#### Move Lead to Different Stage

- `PUT /api/v1/pipeline/items/{id}/stage` - Move lead to different stage

**Request:**

```json
{
  "stageId": 3,
  "probability": 60,
  "notes": "Lead qualified after discovery call"
}
```

### Follow-up Management

#### Schedule Follow-up

- `POST /api/v1/pipeline/follow-ups` - Schedule follow-up

**Request:**

```json
{
  "leadId": "lead-123",
  "type": "CALL",
  "subject": "Follow-up call to discuss proposal",
  "description": "Call to review proposal details and answer questions",
  "dueDate": "2026-02-05T14:00:00Z",
  "priority": "HIGH"
}
```

**Response (201 Created):**

```json
{
  "id": 1,
  "leadId": "lead-123",
  "type": "CALL",
  "subject": "Follow-up call to discuss proposal",
  "description": "Call to review proposal details and answer questions",
  "dueDate": "2026-02-05T14:00:00Z",
  "priority": "HIGH",
  "status": "PENDING",
  "createdAt": "2026-02-03T10:30:00Z"
}
```

### Dashboard Analytics

#### Get Pipeline Statistics

- `GET /api/v1/pipeline/dashboard/stats` - Get pipeline statistics

**Response (200 OK):**

```json
{
  "totalLeads": 150,
  "totalValue": 750000.0,
  "averageValue": 5000.0,
  "conversionRate": 15.5,
  "averageTimeToClose": 45,
  "stageDistribution": [
    {
      "stageId": 1,
      "stageName": "New",
      "count": 45,
      "value": 225000.0,
      "percentage": 30.0
    },
    {
      "stageId": 2,
      "stageName": "Contacted",
      "count": 35,
      "value": 175000.0,
      "percentage": 23.3
    }
  ]
}
```

#### Get Conversion Rates

- `GET /api/v1/pipeline/dashboard/conversion` - Get conversion rates

**Response (200 OK):**

```json
{
  "overallConversionRate": 15.5,
  "stageConversions": [
    {
      "fromStage": "New",
      "toStage": "Contacted",
      "rate": 75.0,
      "count": 120
    },
    {
      "fromStage": "Contacted",
      "toStage": "Qualified",
      "rate": 60.0,
      "count": 90
    }
  ],
  "timeToConvert": {
    "averageDays": 45,
    "medianDays": 38,
    "fastest": 15,
    "slowest": 120
  }
}
```

### Configuration

#### Environment Variables

Key environment variables for configuration:

<details>
<summary>Click to expand environment variables</summary>

```bash
# Database
IQSCAFFOLD_DATABASE_URL=jdbc:postgresql://localhost:5432/iqscaffold_pipeline_local
IQSCAFFOLD_DATABASE_USERNAME=iqscaffold_pipeline
IQSCAFFOLD_DATABASE_PASSWORD=iqscaffold_password

# Redis
IQSCAFFOLD_CACHE_REDIS_HOST=localhost
IQSCAFFOLD_CACHE_REDIS_PORT=6379

# RabbitMQ
IQSCAFFOLD_MESSAGING_RABBITMQ_HOST=localhost
IQSCAFFOLD_MESSAGING_RABBITMQ_PORT=5672

# Security
USER_SERVICE_URL=http://iqscaffold-user-service:8080
JWT_ISSUER=iqscaffold-user-service

# Lead Service Integration
LEAD_SERVICE_URL=http://lead-service:8080
```

</details>

#### Profiles

- `local` - Local development with debug logging
- `staging` - Staging environment configuration
- `production` - Production environment with JSON logging

### Multi-tenancy

The service uses schema-per-tenant isolation:

1. **Tenant Identification**: Via `X-Tenant-ID` header
2. **Schema Management**: Automatic schema creation and migration
3. **Data Isolation**: Complete separation between tenants

### Database Schema

#### System Schema (public)

- `tenant_info` - Tenant metadata and schema mapping

#### Tenant Schemas

- `pipeline_stages` - Pipeline stage definitions and ordering
- `pipeline_items` - Leads in pipeline with current stage
- `follow_ups` - Scheduled follow-ups with due dates
- `pipeline_activities` - Activity log for all pipeline actions

### Pipeline Stages

Default pipeline stages:

1. **New** - Newly created leads
2. **Contacted** - Initial contact made
3. **Qualified** - Lead qualified as potential customer
4. **Proposal** - Proposal sent
5. **Won** - Deal closed successfully
6. **Lost** - Deal lost

### Development

#### Running Tests

<details>
<summary>Click to expand bash commands</summary>

```bash
# Unit tests
mvn test

# Integration tests
mvn verify

# With coverage
mvn clean verify jacoco:report
```

</details>

#### Code Quality

The project includes:

- Checkstyle for code style
- JaCoCo for test coverage (70% minimum)
- ArchUnit for architecture testing

### Monitoring

#### Health Checks

- Liveness: `/actuator/health/liveness`
- Readiness: `/actuator/health/readiness`

#### Metrics

- Prometheus: `/actuator/prometheus`
- Application metrics: `/actuator/metrics`

#### Tracing

- OpenTelemetry integration
- Distributed tracing support

#### Grafana Dashboard

A Grafana dashboard is available at `docs/monitoring/grafana-dashboard.json` providing real-time visibility into:

- Service Health: uptime, request rate, error rate, p95 latency, active sessions
- HTTP Metrics: request rate by status code (2xx/4xx/5xx), response time percentiles (p50/p95/p99)
- JVM Memory: heap/non-heap usage, GC pause time, thread count
- Database: HikariCP connection pool usage, connection acquisition time
- Business Metrics: user registration rate, login attempts (success/failed), email verification rate

The dashboard uses Prometheus as the data source and auto-refreshes every 30 seconds. Import it into your Grafana instance to monitor service performance and health.

## Learning Points

This implementation serves as a reference for:

- Building pipeline management systems with configurable workflows
- Implementing sales analytics and dashboard functionality
- Designing follow-up scheduling and reminder systems
- Multi-tenant data isolation with schema-per-tenant patterns
- Activity logging and audit trail implementation
- Performance optimization for analytics queries
- Real-time dashboard development

## Adapting for Your Domain

This pipeline management service demonstrates patterns applicable to various scenarios:

### Sales Pipeline Management

- Lead qualification workflows
- Opportunity tracking systems
- Sales forecasting platforms
- CRM pipeline automation

### Project Management

- Task progression tracking
- Project stage management
- Milestone tracking systems
- Resource allocation workflows

### Workflow Management

- Business process automation
- Approval workflows
- Document lifecycle management
- Quality assurance processes

### Analytics and Reporting

- Performance metrics tracking
- Conversion analysis systems
- Business intelligence dashboards
- KPI monitoring platforms

The patterns demonstrated here apply to any domain requiring stage-based workflow management, analytics, and performance tracking.

## Integration with Other Services

### Lead Service Integration

The Pipeline Service integrates with the Lead Service for comprehensive lead management:

**Lead Pipeline Flow:**

1. Lead Service creates new lead
2. Pipeline Service automatically adds lead to "New" stage
3. Sales team moves lead through pipeline stages
4. Pipeline Service tracks conversion and analytics

**Event Consumption:**
Pipeline Service listens for lead events to maintain pipeline synchronization:

```java
@RabbitListener(queues = "pipeline.service.lead.events")
public void handleLeadCreated(LeadCreatedEvent event) {
  pipelineService.addLeadToPipeline(event.getLeadId(), getDefaultStage(), event.getLeadValue());
}
```

### Contact Service Integration

Pipeline Service consumes contact events for conversion tracking:

```java
@RabbitListener(queues = "pipeline.service.contact.events")
public void handleContactCreated(ContactCreatedEvent event) {
  if (event.getMetadata().getConvertedFromLeadId() != null) {
    pipelineService.markAsWon(event.getMetadata().getConvertedFromLeadId(), event.getContactId(), event.getTimestamp());
  }
}
```

### User Service Integration

Pipeline Service validates JWT tokens and extracts tenant context:

```yaml
spring:
  security:
    oauth2:
      resourceserver:
        jwt:
          jwk-set-uri: http://iqscaffold-user-service:8080/api/v1/auth/.well-known/jwks.json
```

Extract user and tenant context from JWT:

```java
@GetMapping("/pipeline/items")
public ResponseEntity<Page<PipelineItemDto>> getPipelineItems(Authentication auth, @RequestHeader("X-Tenant-ID") String tenantId, Pageable pageable) {
  // Tenant context automatically resolved
  // User context available from JWT claims
  return ResponseEntity.ok(pipelineService.findAll(pageable));
}
```

---

**Use this as a blueprint** for building pipeline management services and implementing sales analytics systems in your microservices architecture. The code demonstrates production-ready patterns for workflow management, analytics, and cross-service integration.
