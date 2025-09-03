resource "aws_db_instance" "pg" {
  identifier              = "${var.app_name}-pg"
  engine                  = "postgres"
  engine_version          = "16.3"
  instance_class          = "db.t3.micro"
  allocated_storage       = 20
  username                = var.db_user
  password                = var.db_pass
  db_name                 = var.db_name
  publicly_accessible     = true
  skip_final_snapshot     = true
  backup_retention_period = 1
  vpc_security_group_ids  = [aws_security_group.pg_demo.id]
}

data "aws_vpc" "default" {
  default = true
}

resource "aws_security_group" "pg_demo" {
  name        = "${var.app_name}-pg-demo"
  description = "Allow inbound PostgreSQL for demo"
  vpc_id      = data.aws_vpc.default.id

  ingress {
    description = "TEMP: PostgreSQL"
    from_port   = 0
    to_port     = 0
    protocol    = "-1"
    cidr_blocks = ["0.0.0.0/0"]
  }

  egress {
    from_port   = 0
    to_port     = 0
    protocol    = "-1"
    cidr_blocks = ["0.0.0.0/0"]
  }
}

output "rds_endpoint" {
  value = aws_db_instance.pg.address
}
