locals {
  image_identifier = "${aws_ecr_repository.app.repository_url}:${var.image_tag}"
}

resource "aws_apprunner_service" "svc" {
  count        = var.create_apprunner ? 1 : 0
  service_name = var.app_name

  source_configuration {
    auto_deployments_enabled = true

    authentication_configuration {
      access_role_arn = aws_iam_role.apprunner_ecr_access.arn
    }

    image_repository {
      image_repository_type = "ECR"
      image_identifier      = local.image_identifier

      image_configuration {
        port = "8080"
        runtime_environment_variables = {
          SPRING_PROFILES_ACTIVE     = "prod"
          SPRING_DATASOURCE_URL      = "jdbc:postgresql://${aws_db_instance.pg.address}:5432/${var.db_name}"
          SPRING_DATASOURCE_USERNAME = var.db_user
          SPRING_DATASOURCE_PASSWORD = var.db_pass
        }
      }
    }
  }

  health_check_configuration {
    protocol            = "HTTP"
    path                = "/api/hello" # change to "/actuator/health" if you didn't set a base-path
    healthy_threshold   = 1
    interval            = 10
    timeout             = 5
    unhealthy_threshold = 5
  }

  instance_configuration {
    cpu    = "1 vCPU"
    memory = "2 GB"
  }
}
