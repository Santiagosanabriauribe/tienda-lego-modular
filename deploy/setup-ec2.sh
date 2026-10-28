#!/bin/bash
# =============================================================
# Script de despliegue — Tienda LEGO en AWS EC2 (Amazon Linux 2023)
# Ejecutar como: sudo bash setup-ec2.sh
# =============================================================

set -e

echo "=== 1. Instalando dependencias ==="
dnf update -y
dnf install -y java-21-amazon-corretto nginx

echo "=== 2. Creando directorios ==="
mkdir -p /var/www/tienda-lego/frontend
mkdir -p /opt/tienda-lego
mkdir -p /etc/nginx/ssl

echo "=== 3. Generando certificado SSL autofirmado ==="
openssl req -x509 -nodes -days 365 -newkey rsa:2048 \
  -keyout /etc/nginx/ssl/selfsigned.key \
  -out /etc/nginx/ssl/selfsigned.crt \
  -subj "/CN=tienda-lego/O=UPB/C=CO"

echo "=== 4. Copiando archivos ==="
echo "NOTA: Antes de ejecutar este script, sube estos archivos al EC2:"
echo "  - backend/build/libs/TiendaLego-0.0.1-SNAPSHOT.jar  ->  /opt/tienda-lego/app.jar"
echo "  - frontend/ (todo el contenido)                      ->  /var/www/tienda-lego/frontend/"
echo "  - deploy/nginx.conf                                  ->  /etc/nginx/conf.d/tienda-lego.conf"
echo ""

echo "=== 5. Configurando nginx ==="
# Eliminar config default si existe
rm -f /etc/nginx/conf.d/default.conf
cp /tmp/deploy/nginx.conf /etc/nginx/conf.d/tienda-lego.conf 2>/dev/null || echo "Copia nginx.conf manualmente"
nginx -t
systemctl enable nginx
systemctl restart nginx

echo "=== 6. Creando servicio systemd para el backend ==="
cat > /etc/systemd/system/tienda-lego.service << 'EOF'
[Unit]
Description=Tienda LEGO Backend
After=network.target

[Service]
Type=simple
User=ec2-user
ExecStart=/usr/bin/java -jar /opt/tienda-lego/app.jar --spring.profiles.active=prod
Environment=MONGODB_URI=REEMPLAZAR_CON_TU_URI_DE_MONGO_ATLAS
Restart=always
RestartSec=10

[Install]
WantedBy=multi-user.target
EOF

systemctl daemon-reload
systemctl enable tienda-lego

echo "=== 7. Recordatorios ==="
echo ""
echo "ANTES DE INICIAR:"
echo "  1. Edita /etc/systemd/system/tienda-lego.service"
echo "     y reemplaza REEMPLAZAR_CON_TU_URI_DE_MONGO_ATLAS con tu URI real"
echo ""
echo "  2. Sube el JAR:"
echo "     scp backend/build/libs/TiendaLego-0.0.1-SNAPSHOT.jar ec2-user@<IP>:/opt/tienda-lego/app.jar"
echo ""
echo "  3. Sube el frontend:"
echo "     scp -r frontend/* ec2-user@<IP>:/var/www/tienda-lego/frontend/"
echo ""
echo "  4. Inicia el backend:"
echo "     sudo systemctl start tienda-lego"
echo ""
echo "  5. Abre los puertos en el Security Group de EC2:"
echo "     - Puerto 80  (HTTP)"
echo "     - Puerto 443 (HTTPS)"
echo ""
echo "  6. Accede a: https://<IP-PUBLICA-EC2>"
echo ""
echo "=== Listo ==="
