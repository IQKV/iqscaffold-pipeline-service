## 📊 Deployment Guide

### Overview

The IQ Key Value Pipeline Service is deployed using Helm charts and automated CI/CD pipelines. The service provides CRM pipeline management, lead tracking, stage management, follow-up scheduling, and multi-tenancy capabilities.

### Prerequisites

- Kubernetes 1.19+
- Helm 3.2.0+
- External infrastructure services (PostgreSQL, RabbitMQ)

### Environments

| Environment | Namespace      | Purpose                     |
| ----------- | -------------- | --------------------------- |
| Test        | `iqkv-sit-env` | Feature branch testing      |
| Staging     | `iqkv-uat-env` | Pre-production validation   |
| Production  | `iqkv-prd-env` | Live production environment |

### Automated Deployment (CI/CD)

#### Drone Pipeline Overview

<details>
<summary>📋 Pipeline Stages</summary>

The service uses Drone CI/CD pipeline with 10 stages:

1. **VerifyCode** - Code quality, tests, static analysis
2. **PublishArtifacts** - Maven artifacts to Nexus
3. **PublishDockerImage** - Container images to registry
4. **DeployWorkInProgress** - WIP branch auto-deployment
5. **RollbackWorkInProgress** - WIP rollback
6. **PromoteFeatureDeployment** - Feature branch promotion
7. **RollbackFeatureDeployment** - Feature rollback
8. **PromoteDeployment** - Release promotion
9. **RollbackDeployment** - Release rollback
10. **ReleasePackage** - Automated version management

</details>

<details>
<summary>🔐 Required Drone Secrets</summary>

| Secret Name                       | Purpose                              | Used In                                    |
| --------------------------------- | ------------------------------------ | ------------------------------------------ |
| `NEXUS_DEPLOYER_USERNAME`         | Nexus repository authentication      | Artifact publishing, dependency resolution |
| `NEXUS_DEPLOYER_PASSWORD`         | Nexus repository authentication      | Artifact publishing, dependency resolution |
| `SONAR_HOST`                      | SonarQube server URL                 | Static code analysis                       |
| `SONAR_TOKEN`                     | SonarQube authentication token       | Static code analysis                       |
| `SLACK_WEBHOOK`                   | Slack notifications webhook URL      | Build status notifications                 |
| `GITHUB_API_ACCESS_TOKEN`         | GitHub API access for releases       | Release creation, changelog generation     |
| `SVC_CONTAINER_REGISTRY_USERNAME` | Container registry authentication    | Docker image publishing                    |
| `SVC_CONTAINER_REGISTRY_PASSWORD` | Container registry authentication    | Docker image publishing                    |
| `HELM_CHARTS_REPOSITORY`          | Helm charts repository URL           | Kubernetes deployments                     |
| `INFRA_POSTGRESQL_PASSWORD`       | PostgreSQL database password         | Pipeline data storage                      |
| `INFRA_RABBITMQ_PASSWORD`         | RabbitMQ message broker password     | Pipeline lifecycle event messaging         |
| `JWT_SECRET_KEY`                  | JWT symmetric validation key (HS256) | Request authentication validation          |

</details>

#### Branch Deployment Strategy

| Branch Type | Auto Deploy | Manual Promote | Target Environment |
| ----------- | ----------- | -------------- | ------------------ |
| `wip`       | ✅ Dev      | -              | Dev                |
| `feature/*` | -           | ✅ Test        | Test               |
| `dev`       | -           | ✅ Staging     | Staging            |
| Tags        | -           | ✅ Production  | Production         |

#### Deployment Commands

The pipeline uses these Helm commands for deployment:

<details>
<summary>Helm Commands</summary>

```bash
# Development (WIP branches)
helm upgrade --install --atomic --wait --timeout 5m iqscaffold-pipeline-service ./ \
  --values ./values.yaml \
  --values ./values-test.yaml \
  --set image.tag=wip \
  --set infraServices.postgresql.password=${INFRA_POSTGRESQL_PASSWORD} \
  --set infraServices.rabbitmq.password=${INFRA_RABBITMQ_PASSWORD} \
  --set config.security.jwt.secretKey=${JWT_SECRET_KEY} \
  --namespace iqkv-sit-env

# Production (Tagged releases)
helm upgrade --install --atomic --wait --timeout 5m iqscaffold-pipeline-service ./ \
  --values ./values.yaml \
  --values ./values-prd.yaml \
  --set image.tag=${DRONE_TAG} \
  --set infraServices.postgresql.password=${INFRA_POSTGRESQL_PASSWORD} \
  --set infraServices.rabbitmq.password=${INFRA_RABBITMQ_PASSWORD} \
  --set config.security.jwt.secretKey=${JWT_SECRET_KEY} \
  --namespace iqkv-prd-env
```

</details>

#### Drone CI Secrets Configuration

The following secrets must be configured in Drone CI for automated deployments:

```bash
# Configure Drone secrets (run once per repository)
drone secret add --repository IQKV/iqscaffold-pipeline-service --name INFRA_POSTGRESQL_PASSWORD --data "your-postgresql-password"
drone secret add --repository IQKV/iqscaffold-pipeline-service --name INFRA_RABBITMQ_PASSWORD --data "your-rabbitmq-password"
drone secret add --repository IQKV/iqscaffold-pipeline-service --name JWT_SECRET_KEY --data "your-secure-symmetric-key"
```

#### Environment Variable Mapping

| Drone Secret                | Helm Parameter                      | Application Environment Variable | Description                      |
| --------------------------- | ----------------------------------- | -------------------------------- | -------------------------------- |
| `INFRA_POSTGRESQL_PASSWORD` | `infraServices.postgresql.password` | `SPRING_DATASOURCE_PASSWORD`     | PostgreSQL database password     |
| `INFRA_RABBITMQ_PASSWORD`   | `infraServices.rabbitmq.password`   | `SPRING_RABBITMQ_PASSWORD`       | RabbitMQ message broker password |
| `JWT_SECRET_KEY`            | `config.security.jwt.secretKey`     | `JWT_SECRET_KEY`                 | JWT symmetric validation secret  |

### Manual Deployment

#### Quick Start

```bash
# Clone Helm charts
git clone <HELM_CHARTS_REPOSITORY> charts
cd charts/IQKV/iqscaffold-pipeline-service

# Deploy to development
helm upgrade --install pipeline-service ./ \
  --values values-sit.yaml \
  --set infraServices.postgresql.password="your-postgresql-password" \
  --set infraServices.rabbitmq.password="your-rabbitmq-password" \
  --set config.security.jwt.secretKey="your-secure-symmetric-key" \
  --namespace iqkv-sit-env \
  --create-namespace
```

#### Environment-Specific Deployments

#### Development

```bash
helm upgrade --install pipeline-service ./ \
  --values values-sit.yaml \
  --set infraServices.postgresql.password="your-postgresql-password" \
  --set infraServices.rabbitmq.password="your-rabbitmq-password" \
  --set config.security.jwt.secretKey="your-secure-symmetric-key" \
  --namespace iqkv-sit-env \
  --create-namespace
```

#### Production

```bash
helm upgrade --install pipeline-service ./ \
  --values values-prd.yaml \
  --set infraServices.postgresql.password="${POSTGRESQL_PASSWORD}" \
  --set infraServices.rabbitmq.password="${RABBITMQ_PASSWORD}" \
  --set config.security.jwt.secretKey="${JWT_SECRET_KEY}" \
  --namespace iqkv-prd-env \
  --create-namespace
```

### Configuration

#### External Services

The service connects to these external infrastructure components:

- **PostgreSQL**: Pipeline data storage (database: `iqscaffold_pipeline`)
- **RabbitMQ**: Event messaging for pipeline lifecycle events
- **User Service**: JWT validation and user context
- **Lead Service**: Lead data integration and conversion
- **Contact Service**: Contact data integration

#### Service Configuration

| Setting        | Dev      | Production       |
| -------------- | -------- | ---------------- |
| Replicas       | 1        | 3                |
| CPU Request    | 250m     | 500m             |
| Memory Request | 384Mi    | 512Mi            |
| CPU Limit      | 500m     | 1000m            |
| Memory Limit   | 768Mi    | 1Gi              |
| Autoscaling    | Disabled | 3-10 replicas    |
| Ingress        | Disabled | Enabled with TLS |
| Monitoring     | Enabled  | Enabled          |

#### Pipeline-Specific Configuration

| Setting                 | Dev   | Production | Description                    |
| ----------------------- | ----- | ---------- | ------------------------------ |
| Auto Stage Progression  | false | false      | Automatic pipeline advancement |
| Follow-up Reminders     | true  | true       | Automated reminder system      |
| Activity Tracking       | true  | true       | Track pipeline activities      |
| Max Follow-ups per Lead | 20    | 50         | Maximum follow-ups allowed     |

### Monitoring & Health Checks

#### Health Endpoints

- **Liveness**: `/actuator/health/liveness` (port 8081)
- **Readiness**: `/actuator/health/readiness` (port 8081)
- **Metrics**: `/actuator/prometheus` (port 8081)

#### Monitoring Stack

Production deployments include:

- Prometheus ServiceMonitor
- Alerting rules for service health:
    - **PipelineServiceDown**: Service unavailable for >1 minute
    - **PipelineServiceHighMemory**: Memory usage >80% for >5 minutes
    - **PipelineServiceHighLatency**: 95th percentile latency >2 seconds
    - **PipelineServiceDatabaseConnectionFailure**: No active database connections
    - **PipelineServiceHighFollowUpOverdue**: Overdue follow-ups >100
- Grafana dashboards for pipeline metrics

### Troubleshooting

#### Common Issues

1. **Database Connection Failures**

    ```bash
    kubectl logs deployment/iqscaffold-pipeline-service -n iqkv-sit-env
    ```

2. **RabbitMQ Connection Issues**

    ```bash
    # Check RabbitMQ connectivity
    kubectl exec -it deployment/iqscaffold-pipeline-service -n iqkv-sit-env -- \
      nc -zv foundation-infra-rabbitmq.iqkv-sit-env.svc.cluster.local 5672

    # Verify RabbitMQ password configuration
    kubectl get secret iqscaffold-pipeline-service-secrets -o yaml | grep rabbitmq
    ```

3. **Check Configuration**

    ```bash
    kubectl describe configmap iqscaffold-pipeline-service-config -n iqkv-sit-env
    ```

4. **Test Health Endpoints**

    ```bash
    kubectl port-forward deployment/iqscaffold-pipeline-service 8081:8081 -n iqkv-sit-env
    curl http://localhost:8081/actuator/health
    ```

5. **Pipeline Configuration Issues**

    ```bash
    # Check pipeline configuration
    kubectl get configmap iqscaffold-pipeline-service-config -o yaml | grep PIPELINE_
    ```

6. **Service Integration Issues**
    ```bash
    # Test service connectivity
    kubectl exec -it deployment/iqscaffold-pipeline-service -n iqkv-sit-env -- \
      curl http://iqscaffold-user-service/actuator/health
    ```

#### Rollback

```bash
# Rollback to previous version
helm rollback iqscaffold-pipeline-service -n iqkv-prd-env

# Or uninstall completely
helm uninstall iqscaffold-pipeline-service -n iqkv-prd-env
```

### Security

- All sensitive values passed via `--set` flags
- TLS enabled in production
- Network policies restrict pod communication
- Non-root container execution (UID: 1001)
- Read-only root filesystem in production
- Minimal container capabilities (drop ALL)
- JWT-based authentication integration
- Multi-tenant data isolation
