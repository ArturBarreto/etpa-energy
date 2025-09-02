variable "region"           { type = string, default = "eu-central-1" }
variable "app_name"         { type = string, default = "etpa-energy" }
variable "db_name"          { type = string, default = "energy" }
variable "db_user"          { type = string, default = "energy" }
variable "db_pass"          { type = string, sensitive = true }
variable "github_org_repo"  { type = string } # e.g. "yourorg/etpa-energy"
variable "image_tag"        { type = string, default = "bootstrap" } # commit SHA later
variable "create_apprunner" { type = bool,   default = false }       # create it after image exists
