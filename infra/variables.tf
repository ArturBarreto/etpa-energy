variable "region" {
  type = string
  default = "eu-central-1"
}

variable "app_name" {
  type = string
  default = "etpa-energy"
}

variable "db_name" {
  type = string
  default = "energydb"
}

variable "db_user" {
  type = string
  default = "energy"
}

variable "db_pass" {
  type = string
  sensitive = true
}

# e.g. "yourorg/etpa-energy"
variable "github_org_repo" {
  type = string
}

# commit SHA later
variable "image_tag" {
  type = string
  default = "bootstrap"
}

# create it after image exists
variable "create_apprunner" {
  type = bool
  default = false
}
