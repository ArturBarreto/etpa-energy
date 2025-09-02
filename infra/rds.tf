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
}

output "rds_endpoint" {
  value = aws_db_instance.pg.address
}
