output "public_ip" {
  description = "IP publica de la instancia"
  value       = aws_eip.tienda_lego.public_ip
}

output "url_https" {
  description = "URL de la tienda (HTTPS)"
  value       = "https://${aws_eip.tienda_lego.public_ip}"
}

output "url_swagger" {
  description = "URL de Swagger UI"
  value       = "https://${aws_eip.tienda_lego.public_ip}/swagger-ui.html"
}

output "ssh_command" {
  description = "Comando para conectarse por SSH"
  value       = "ssh -i <tu-key.pem> ec2-user@${aws_eip.tienda_lego.public_ip}"
}
