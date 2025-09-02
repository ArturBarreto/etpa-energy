data "aws_caller_identity" "me" {}

resource "aws_iam_role" "apprunner_ecr_access" {
  name = "etpa-energy-apprunner-ecr-access"

  assume_role_policy = jsonencode({
    Version = "2012-10-17",
    Statement = [{
      Effect = "Allow",
      Principal = { Service = "build.apprunner.amazonaws.com" },
      Action   = "sts:AssumeRole"
    }]
  })
}

# Attach AWS managed policy that grants ECR pull permissions
resource "aws_iam_role_policy_attachment" "apprunner_ecr_access_attach" {
  role       = aws_iam_role.apprunner_ecr_access.name
  policy_arn = "arn:aws:iam::aws:policy/service-role/AWSAppRunnerServicePolicyForECRAccess"
}
