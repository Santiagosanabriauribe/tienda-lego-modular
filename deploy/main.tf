terraform {
  required_version = ">= 1.5"
  required_providers {
    aws = {
      source  = "hashicorp/aws"
      version = "~> 5.0"
    }
  }
}

provider "aws" {
  region = var.aws_region
}

# --- Data: AMI de Amazon Linux 2023 mas reciente ---
data "aws_ami" "amazon_linux" {
  most_recent = true
  owners      = ["amazon"]

  filter {
    name   = "name"
    values = ["al2023-ami-*-x86_64"]
  }

  filter {
    name   = "virtualization-type"
    values = ["hvm"]
  }
}

# --- Security Group ---
resource "aws_security_group" "tienda_lego" {
  name        = "tienda-lego-sg"
  description = "Puertos para Tienda LEGO"

  ingress {
    description = "SSH"
    from_port   = 22
    to_port     = 22
    protocol    = "tcp"
    cidr_blocks = ["0.0.0.0/0"]
  }

  ingress {
    description = "HTTP"
    from_port   = 80
    to_port     = 80
    protocol    = "tcp"
    cidr_blocks = ["0.0.0.0/0"]
  }

  ingress {
    description = "HTTPS"
    from_port   = 443
    to_port     = 443
    protocol    = "tcp"
    cidr_blocks = ["0.0.0.0/0"]
  }

  egress {
    from_port   = 0
    to_port     = 0
    protocol    = "-1"
    cidr_blocks = ["0.0.0.0/0"]
  }

  tags = {
    Name = "tienda-lego-sg"
  }
}

# --- EC2 Instance ---
resource "aws_instance" "tienda_lego" {
  ami                    = data.aws_ami.amazon_linux.id
  instance_type          = var.instance_type
  key_name               = var.key_name
  vpc_security_group_ids = [aws_security_group.tienda_lego.id]

  user_data = base64encode(templatefile("${path.module}/user-data.sh", {
    mongodb_uri = var.mongodb_uri
  }))

  root_block_device {
    volume_size = 10
    volume_type = "gp3"
  }

  tags = {
    Name = "tienda-lego"
  }
}

# --- Elastic IP (IP fija gratuita mientras la instancia este corriendo) ---
resource "aws_eip" "tienda_lego" {
  instance = aws_instance.tienda_lego.id
  domain   = "vpc"

  tags = {
    Name = "tienda-lego-eip"
  }
}
