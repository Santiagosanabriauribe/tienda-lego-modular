variable "aws_region" {
  description = "Region de AWS"
  type        = string
  default     = "us-east-1"
}

variable "instance_type" {
  description = "Tipo de instancia EC2"
  type        = string
  default     = "t2.micro"
}

variable "key_name" {
  description = "Nombre del Key Pair de EC2 para SSH"
  type        = string
}

variable "mongodb_uri" {
  description = "URI de conexion a MongoDB Atlas"
  type        = string
  sensitive   = true
}
