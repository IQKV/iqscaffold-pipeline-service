## 📊 Deployment Guide

### Overview

The IQ Scaffold Pipeline Service is deployed using Helm charts and automated CI/CD pipelines. The service provides CRM pipeline management, lead tracking, stage management, follow-up scheduling, and multi-tenancy capabilities.

### Prerequisites

- Kubernetes 1.19+
- Helm 3.2.0+
- External infrastructure services (PostgreSQL, RabbitMQ)
- User Service and Lead Service (for integration)

### Environments

| Environment | Namespace                   | Purpose                      |
| ----------- | --------------------------- | ---------------------------- |
| Dev         | `iqscaffold-dev-env`        | Development and WIP branches |
| Test        | `iqscaffold-test-env`       | Feature branch testing       |
| Staging     | `iqscaffold-staging-env`    | Pre-production validation    |
| Production  | `iqscaffold-production-env` | Live production environment  |

### Automated Deployment (CI/CD)

#### Drone Pipeline Overview

The service uses a comprehensive Drone CI/CD pipeline with 10 stages:

1. **VerifyCode** - Code quality, tests, static analysis
2. **PublishArtifacts** - Maven artifacts to Nexus
3. **PublishDockerImage** - Container images to registry
4. **DeployWorkInProgressOnDev** - WIP branch auto-deployment
5. **RollbackWorkInProgressOnDev** - WIP rollback
6. **PromoteFeatureDeployment** - Feature branch promotion
7. **RollbackFeatureDeployment** - Feature rollback
8. **PromoteDeployment** - Release promotion
9. **RollbackDeployment** - Release rollback
10. **ReleasePackage** - Automated version management

#### Branch Deployment Strategy

| Branch Type | Auto Deploy | Manual Promote | Target Environment |
| ----------- | ----------- | -------------- | ------------------ |
| `wip`       | ✅ Dev      | -              | Dev                |
| `feature/*` | -           | ✅ Test        | Test               |
| `dev`       | -           | ✅ Staging     | Staging            |
| Tags        | -           | ✅ Production  | Production         |

<details>
<summary>Deployment Commands</summary>

The pipeline uses these Helm commands for deployment:

```bash
# Development (WIP branches)
helm upgrade --install --atomic --wait --timeout 5m iqscaffold-pipeline-service ./ \
  --values ./values.yaml \
  --values ./values-local.yaml \
  --set image.tag=wip \
  --set secrets.database.password=${INFRA_POSTGRESQL_PASSWORD} \
  --set secrets.rabbitmq.password=${INFRA_RABBITMQ_PASSWORD} \
  --namespace iqscaffold-dev-env

# Production (Tagged releases)
helm upgrade --install --atomic --wait --timeout 5m iqscaffold-pipeline-service ./ \
  --values ./values.yaml \
  --values ./values-production.yaml \
  --set image.tag=${DRONE_TAG} \
  --set secrets.database.password=${INFRA_POSTGRESQL_PASSWORD} \
  --set secrets.rabbitmq.password=${INFRA_RABBITMQ_PASSWORD} \
  --namespace iqscaffold-production-env
```

</details>

### Manual Deployment

#### Quick Start

<details>
<summary>Quick Start Commands</summary>

```bash
# Clone Helm charts
git clone <HELM_CHARTS_REPOSITORY> charts
cd charts/IQKV/iqscaffold-pipeline-service

# Deploy to development
helm upgrade --install pipeline-service ./ \
  --values values-local.yaml \
  --set secrets.database.password="your-db-password" \
  --set secrets.rabbitmq.password="your-rabbitmq-password" \
  --namespace iqscaffold-dev-env \
  --create-namespace
```

</details>

#### Environment-Specific Deployments

<details>
<summary>Development Deployment</summary>

```bash
helm upgrade --install pipeline-service ./ \
  --values values-local.yaml \
  --namespace iqscaffold-dev-env \
  --create-namespace
```

</details>

<details>
<summary>Production Deployment</summary>

```bash
helm upgrade --install pipeline-service ./ \
  --values values-production.yaml \
  --set secrets.database.password="${DB_PASSWORD}" \
  --set secrets.rabbitmq.password="${RABBITMQ_PASSWORD}" \
  --namespace iqscaffold-production-env \
  --create-namespace
```

</details>

### Configuration

#### Required Secrets

| Secret            | Environment Variable       | Required | Description             |
| ----------------- | -------------------------- | -------- | ----------------------- |
| Database Password | `INFRA_POSTGRESQL_PASSWORD`  | ✅       | PostgreSQL password     |
| RabbitMQ Password | `INFRA_RABBITMQ_PASSWORD` | ✅       | Message broker password |

#### External Services

The service connects to these external infrastructure components:

- **PostgreSQL**: Pipeline data storage
- **RabbitMQ**: Event messaging
- **User Service**: Authentication and user management
- **Lead Service**: Lead data integration
- **Contact Service**: Contact data integration

#### Service Configuration

| Setting        | Dev      | Production       |
| -------------- | -------- | ---------------- |
| Replicas       | 1        | 3                |
| CPU Request    | 250m     | 500m             |
| Memory Request | 384Mi    | 512Mi            |
| Autoscaling    | Disabled | 3-10 replicas    |
| Ingress        | Disabled | Enabled with TLS |
| Monitoring     | Enabled  | Enabled          |

<details>
<summary>Pipeline-Specific Configuration</summary>

| Setting                   | Dev  | Production | Description                    |
| ------------------------- | ---- | ---------- | ------------------------------ |
| Auto Stage Progression    | true | true       | Automatic pipeline advancement |
| Follow-up Reminders       | true | true       | Automated reminder system      |
| Activity Tracking         | true | true       | Track pipeline activities      |
| Overdue Threshold (hours) | 2    | 24         | Follow-up overdue threshold    |
| Reminder Interval (hours) | 1    | 4          | Reminder frequency             |
| Max Follow-ups per Lead   | 20   | 50         | Maximum follow-ups allowed     |

</details>

### Monitoring & Health Checks

#### Health Endpoints

- **Liveness**: `/actuator/health/liveness` (port 8081)
- **Readiness**: `/actuator/health/readiness` (port 8081)
- **Metrics**: `/actuator/prometheus` (port 8081)

#### Monitoring Stack

Production deployments include:

- Prometheus ServiceMonitor
- Alerting rules for service health
- Grafana dashboards

<details>
<summary>Pipeline-Specific Alerts</summary>

| Alert                              | Condition                           | Severity | Description                       |
| ---------------------------------- | ----------------------------------- | -------- | --------------------------------- |
| PipelineServiceDown                | Service unavailable > 1 minute      | Critical | Service is down                   |
| PipelineServiceHighMemory          | Memory usage > 80%                  | Warning  | High memory consumption           |
| PipelineServiceHighLatency         | 95th percentile latency > 2 seconds | Warning  | High response times               |
| PipelineServiceDatabaseConnection  | No active database connections      | Critical | Database connectivity issues      |
| PipelineServiceHighFollowUpOverdue | Overdue follow-ups > 100            | Warning  | High number of overdue follow-ups |

</details>

### Troubleshooting

#### Common Issues

<details>
<summary>Database Connection Failures</summary>

```bash
# Check service logs
kubectl logs deployment/iqscaffold-pipeline-service -n iqscaffold-dev-env

# Check database connectivity
kubectl exec -it deployment/iqscaffold-pipeline-service -n iqscaffold-dev-env -- \
  nc -zv iqscaffold-infra-postgresql.iqscaffold-dev-env.svc.cluster.local 5432
```

</details>

<details>
<summary>Check Configuration</summary>

```bash
# View ConfigMap
kubectl describe configmap iqscaffold-pipeline-service-config -n iqscaffold-dev-env

# View Secrets
kubectl describe secret iqscaffold-pipeline-service-secrets -n iqscaffold-dev-env
```

</details>

<details>
<summary>Test Health Endpoints</summary>

```bash
# Port forward to access health endpoints
kubectl port-forward deployment/iqscaffold-pipeline-service 8081:8081 -n iqscaffold-dev-env

# Test health endpoints
curl http://localhost:8081/actuator/health
curl http://localhost:8081/actuator/health/liveness
curl http://localhost:8081/actuator/health/readiness
curl http://localhost:8081/actuator/prometheus
```

</details>

<details>
<summary>Service Integration Issues</summary>

```bash
# Test User Service connectivity
kubectl exec -it deployment/iqscaffold-pipeline-service -n iqscaffold-dev-env -- \
  curl -v http://iqscaffold-user-service/.well-known/jwks.json

# Test Lead Service connectivity
kubectl exec -it deployment/iqscaffold-pipeline-service -n iqscaffold-dev-env -- \
  curl -v http://iqscaffold-lead-service/actuator/health

# Check RabbitMQ connectivity
kubectl exec -it deployment/iqscaffold-pipeline-service -n iqscaffold-dev-env -- \
  nc -zv iqscaffold-infra-rabbitmq.iqscaffold-dev-env.svc.cluster.local 5672
```

</details>

#### Rollback

<details>
<summary>Rollback Commands</summary>

```bash
# Rollback to previous version
helm rollback iqscaffold-pipeline-service -n iqscaffold-production-env

# Or uninstall completely
helm uninstall iqscaffold-pipeline-service -n iqscaffold-production-env
```

</details>

### Security

- All sensitive values passed via `--set` flags
- TLS enabled in production
- Network policies restrict pod communication
- Non-root container execution
- Read-only root filesystem in production
- JWT-based authentication integration
- Multi-tenant data isolation
