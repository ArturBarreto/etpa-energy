import {
  to = aws_ecr_repository.app
  id = "etpa-energy"
}

import {
  to = aws_iam_openid_connect_provider.github
  id = "arn:aws:iam::982534393012:oidc-provider/token.actions.githubusercontent.com"
}

# import {
#   to = aws_db_instance.pg
#   id = "etpa-energy-pg"
# }

import {
  to = aws_iam_role.github_actions
  id = "etpa-energy-github-actions"
}

import {
  to = aws_iam_role.apprunner_ecr_access
  id = "etpa-energy-apprunner-ecr-access"
}

import {
  to = aws_apprunner_service.svc
  id = "arn:aws:apprunner:eu-central-1:982534393012:service/etpa-energy/7e2e602b8ac34318ba4b9ae069abbb6e"
}

import {
  to = aws_security_group.pg_demo
  id = "sg-04ae28928aa11793c"
}
