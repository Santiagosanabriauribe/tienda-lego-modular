#!/bin/bash
# User data — se ejecuta automaticamente al crear la instancia EC2
set -ex

# 1. Instalar Java 21 y nginx
dnf update -y
dnf install -y java-21-amazon-corretto nginx git

# 2. Crear directorios
mkdir -p /var/www/tienda-lego/frontend
mkdir -p /opt/tienda-lego
mkdir -p /etc/nginx/ssl

# 3. Generar certificado SSL autofirmado
openssl req -x509 -nodes -days 365 -newkey rsa:2048 \
  -keyout /etc/nginx/ssl/selfsigned.key \
  -out /etc/nginx/ssl/selfsigned.crt \
  -subj "/CN=tienda-lego/O=UPB/C=CO"

# 4. Configurar nginx
cat > /etc/nginx/conf.d/tienda-lego.conf << 'NGINX'
server {
    listen 80;
    server_name _;
    return 301 https://$host$request_uri;
}

server {
    listen 443 ssl;
    server_name _;

    ssl_certificate     /etc/nginx/ssl/selfsigned.crt;
    ssl_certificate_key /etc/nginx/ssl/selfsigned.key;
    ssl_protocols       TLSv1.2 TLSv1.3;

    root /var/www/tienda-lego/frontend;
    index index.html;

    location / {
        try_files $uri $uri/ /index.html;
    }

    location /api/ {
        proxy_pass http://127.0.0.1:8080;
        proxy_set_header Host $host;
        proxy_set_header X-Real-IP $remote_addr;
        proxy_set_header X-Forwarded-For $proxy_add_x_forwarded_for;
        proxy_set_header X-Forwarded-Proto $scheme;
    }

    location /swagger-ui.html {
        proxy_pass http://127.0.0.1:8080;
        proxy_set_header Host $host;
    }

    location /swagger-ui/ {
        proxy_pass http://127.0.0.1:8080;
        proxy_set_header Host $host;
    }

    location /api-docs {
        proxy_pass http://127.0.0.1:8080;
        proxy_set_header Host $host;
    }

    location /uploads/ {
        proxy_pass http://127.0.0.1:8080;
        proxy_set_header Host $host;
    }
}
NGINX

rm -f /etc/nginx/conf.d/default.conf
systemctl enable nginx
systemctl start nginx

# 5. Crear servicio systemd para el backend
cat > /etc/systemd/system/tienda-lego.service << EOF
[Unit]
Description=Tienda LEGO Backend
After=network.target

[Service]
Type=simple
User=ec2-user
WorkingDirectory=/opt/tienda-lego
ExecStart=/usr/bin/java -jar /opt/tienda-lego/app.jar --spring.profiles.active=prod
Environment=MONGODB_URI=${mongodb_uri}
Restart=always
RestartSec=10

[Install]
WantedBy=multi-user.target
EOF

systemctl daemon-reload
systemctl enable tienda-lego

# 6. Clonar el repo, compilar y desplegar
cd /tmp
git clone https://github.com/Santiagosanabriauribe/tienda-lego-modular.git repo
cd repo

# Compilar backend
cd backend
chmod +x gradlew
./gradlew bootJar --no-daemon
cp build/libs/*.jar /opt/tienda-lego/app.jar
chown ec2-user:ec2-user /opt/tienda-lego/app.jar
cd ..

# Copiar frontend
cp -r frontend/* /var/www/tienda-lego/frontend/

# Limpiar
rm -rf /tmp/repo

# 7. Iniciar el backend
systemctl start tienda-lego
