# Energy Platform API

A Spring Boot 3 / Java 21 application for managing load profiles, meters, cumulative readings, and deriving monthly consumption. This API was built as part of the ETPA Energy use-case assignment. 

This project is **containerized (Docker)**, **deployed in a real AWS environment (App Runner + RDS + ECR)** via **Terraform**, and automated with **GitHub Actions**.

---

## **Live Demo (AWS)**

- **Link:** https://5vgwnky8hg.eu-central-1.awsapprunner.com/api/  
- **Availability Window:** **08:00–22:00 (CET)** daily (to optimize costs).

---

## **Project Overview**

This service exposes REST endpoints for:
- Managing energy entities like load profiles, meters and cumulative readings
- Retrieving energy monthly consumption
- Application health and readiness via Spring Actuator

Architecture:
- **Spring Boot** (Java 21), **Maven**
- Layered: **controller → service → repository**
- **Swagger / OpenAPI** auto-generated docs
- Local dev uses **H2** (in-memory); production uses **AWS RDS (PostgreSQL)**

---

## **How to Run Locally**

### Requirements
- Java 21+
- Maven 3.8+
- Docker

### Build
```bash
mvn clean package
```

### Run
```bash
mvn spring-boot:run
# or
java -jar target/*.jar
```

Local endpoints:
- Base URL: `http://localhost:8080/api`
- Swagger UI: `http://localhost:8080/api/swagger-ui/index.html`
- Health: `GET http://localhost:8080/api/actuator/health`
- H2 Console (dev): `http://localhost:8080/api/h2-console`  
  - JDBC URL: `jdbc:h2:mem:testdb`  •  Username: `sa`  •  Password: *(blank)*

### Tests
```bash
mvn test
```

---

## **Docker**

```bash
# Build
docker build -t etpa-energy .

# Run
docker run --rm -p 8080:8080 etpa-energy
```

---

## **Infrastructure (`infra/` Terraform)**

This repository contains **Terraform** to manage the live AWS environment (already provisioned).  
Terraform state aligns to existing resources using `import` blocks (see `infra/import.tf`).

### Providers & Region
- Provider: `hashicorp/aws >= 5.0`
- Default region: **eu-central-1**

### Resources (actual)
- **ECR**: `aws_ecr_repository.app`  
  - Output: `ecr_url`
- **App Runner**: `aws_apprunner_service.svc`  
  - **Health check path:** `/api/actuator/health`  
  - **Auto deployments:** enabled  
  - **Instance config:** `1 vCPU`, `2 GB`  
  - Pulls image from ECR using the access role below.
- **IAM (App Runner ECR Access)**:  
  - `aws_iam_role.apprunner_ecr_access`  
  - Attachment: **`AWSAppRunnerServicePolicyForECRAccess`** (managed policy)
- **GitHub OIDC + Role for CI/CD**:  
  - `aws_iam_openid_connect_provider.github`  
  - `aws_iam_role.github_actions` with permissions to interact with ECR/App Runner/IAM/etc.  
  - Output: `github_actions_role_arn`
- **RDS (PostgreSQL)**: `aws_db_instance.pg` (Postgres **16.3**, `db.t3.micro`, 20 GB)  
  - Publicly accessible (demo) with Security Group `pg_demo` (TCP 5432)  
  - Output: `rds_endpoint`

> **Note:** Security group rules are open for demonstration. For production, restrict ingress (e.g., VPC-only, SG-to-SG rules) and remove public access.

### Imports (`infra/import.tf`)
Terraform is configured to **import** the already-existing AWS resources (ECR repo, OIDC provider, RDS, IAM roles, App Runner service, and SG) so the IaC matches the live environment.

---

## **CI/CD & GitFlow**

This repo uses **GitHub Actions** + **OIDC** (no long‑lived AWS keys) and a simple branch flow:

### Branch Strategy
- **Feature branches** → PRs → `main`
- **`main`** is the deployment branch

### Workflows (`.github/workflows/`)
- **`test.yml`** – runs on every **push & PR** (all branches)  
  - Builds and runs tests (`mvn -Dspring.profiles.active=test clean test`)  
  - Uploads Surefire/Failsafe reports on failure
- **`deploy.yml`** – runs on **push to `main`**  
  - Assumes AWS role via **OIDC**  
  - Builds Docker image, tags (with commit SHA), pushes to **ECR**  
  - Executes `terraform apply` in `infra/` with:  
    `region`, `app_name`, `db_pass (secret)`, `github_org_repo`, `create_apprunner=true`, `image_tag=<commit tag>`  
  - App Runner is updated to the new image
- **`bootstrap.yml`** – **manual** (workflow_dispatch)  
  - One‑time/bootstrap: creates the **GitHub OIDC provider** and **CI role** with Terraform (using bootstrap AWS credentials)

**Result:** Every merge to `main` produces a new container image, updates App Runner via Terraform, and rolls out the service to the live AWS endpoint.

---

## **Configuration**

- **Context Path:** `server.servlet.context-path: /api`
- **Profiles:**  
  - `dev` (default): H2 in‑memory DB  
  - `prod`: expects environment variables:
    - `SPRING_DATASOURCE_URL`
    - `SPRING_DATASOURCE_USERNAME`
    - `SPRING_DATASOURCE_PASSWORD`
- **Ports:** 8080 (container/image exposes 8080)

---

## **Folder Structure**

```
etpa-energy/
├── src/
│   ├── main/
│   │   ├── java/...                 # Controllers, services, repositories, config
│   │   └── resources/
│   │       ├── application.yml      # Profiles, H2 console, context path (/api)
│   │       └── static/              # Static resources (hello index.html)
│   └── test/
│       └── java/...                 # Tests
├── infra/                           # Terraform for AWS (ECR, App Runner, RDS, IAM, OIDC)
│   ├── apprunner.tf                 # App Runner service (health: /api/actuator/health)
│   ├── ecr.tf                       # ECR repo + output
│   ├── iam_apprunner.tf             # ECR access role + managed policy attachment
│   ├── iam_oidc.tf                  # GitHub OIDC provider + role & output
│   ├── rds.tf                       # RDS Postgres + demo security group + output
│   ├── main.tf                      # Provider + imports (map to live resources)
│   ├── variables.tf                 # Inputs (region, app_name, db creds, image_tag, toggles)
│   └── import.tf                    # Terraform import declarations (align state with AWS)
├── .github/workflows/               # CI/CD pipelines (test, deploy, bootstrap)
├── Dockerfile                       # Multi-stage build for runtime image (Temurin 21 JRE)
├── pom.xml                          # Maven build file
└── README.md                        # This file
```

---

## **Design Decisions**

- **Spring Boot** for productivity and ecosystem.
- **Docker** for reproducible builds and parity across environments.
- **Terraform** to codify live AWS infra and keep state in sync via imports.
- **GitHub OIDC** for secure, keyless CI deployments.
- **PostgreSQL (RDS)** for ACID correctness and strong consistency; excellent concurrency via MVCC; rich SQL + JSONB for complex reports and semi-structured data; mature RDS operations (backups, replicas, RDS Proxy); pragmatic scaling and excellent Java ecosystem support.
- **App Runner** to simplify container hosting (scales, managed TLS, no servers to manage).

---

## **Trade-Offs & Next Steps**

- **RDS & SG:** Public access and open ingress are for demo; lock down for production.
- **Observability:** Add metrics (Prometheus), tracing, and structured logging.
- **Security:** Add authN/Z (JWT/OAuth2), rotate secrets (AWS Secrets Manager), and WAF.
- **Resilience:** Add DB migrations (Flyway), blue/green or canary deployments.
- **Testing:** Increase E2E coverage and add load/perf tests.
- **CI/CD modernization:** Replace `import.tf` with **remote state per environment**, add **plan‑then‑apply approvals**, **semantic versioning**, **image and dependency scanning**, and **immutable promotions** across dev → stage → prod.
- **Scalability / Control:** While **AWS App Runner** is excellent for quick managed deployments, moving to an **API Gateway + ECS (Fargate)** architecture would provide:
  - Fine-grained autoscaling policies and task sizing.
  - Integration with private VPCs and more controlled networking (e.g., SG-only access to RDS).
  - Ability to use **ALB + API Gateway** for advanced routing, throttling, WAF, caching, and versioned APIs.
  - Easier multi-service orchestration if the platform expands beyond a single container.
  - Lower long-term cost flexibility (spot pricing, custom scaling) at the expense of more operational overhead.

---

## **Contact**

- Artur Gomes Barreto  
  - LinkedIn: https://www.linkedin.com/in/arturgomesbarreto/  
  - GitHub: https://github.com/ArturBarreto  
  - E-mail: artur.gomes.barreto@gmail.com  
  - WhatsApp: https://api.whatsapp.com/send?phone=35677562008
