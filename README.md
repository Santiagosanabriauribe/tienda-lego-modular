# Tienda en Linea Modular — LEGO

Aplicacion web de comercio electronico especializada en sets y coleccionables de LEGO.
Backend REST API (Spring Boot 4) + Frontend independiente (HTML/SCSS/JS), comunicados
exclusivamente por API REST.

---

## Situacion problema

Un pequeno negocio de venta de sets LEGO no cuenta con plataforma propia para ofrecer sus
productos en linea. Solo publica fotos en redes sociales sin catalogo organizado, sin control
de inventario y sin forma de que un cliente arme un pedido con varios productos.

Esta aplicacion resuelve eso: catalogo por categorias, carrito de compras, panel de
administracion, pagos simulados y seguridad aplicada.

## Objetivos

- Catalogo publico navegable por categorias con imagenes reales
- Carrito de compras con recalculo automatico de totales
- Panel de administrador para gestionar categorias, productos y ver pedidos
- API REST documentada con Swagger/OpenAPI
- Seguridad: HTTPS, mitigacion XSS (Content-Security-Policy), proteccion CSRF
- Estilos con preprocesador SCSS, gestionado por npm

## Alcance

### Funcional
- CRUD completo de categorias y productos
- Carrito de compras: agregar, quitar, modificar cantidades, calcular total
- Registro e inicio de sesion de usuarios
- Panel de administracion: gestionar productos, categorias y ver pedidos
- Subida de imagenes para productos
- Proceso de pago simulado (tarjeta, PSE, contra entrega)
- Catalogo filtrable por categoria

### No funcional
- Tiempo de respuesta < 2 segundos por operacion
- Disponibilidad durante la sustentacion
- Proteccion contra XSS y CSRF
- HTTPS disponible via perfil SSL
- Cobertura de pruebas unitarias

### Exclusiones
- Pasarela de pagos real
- Aplicaciones moviles nativas
- Internacionalizacion (solo espanol)
- Sistema de envios o logistica

## Arquitectura general

```
+----------------+       API REST (JSON)       +------------------+
|   Frontend     | <=========================> |    Backend       |
|  HTML/SCSS/JS  |    http://localhost:8080     |  Spring Boot 4   |
|  Puerto 5500   |    /api/v1/*                |  Java 21 LTS     |
+----------------+                             +------------------+
                                                        |
                                                        v
                                               +------------------+
                                               |   MongoDB Atlas  |
                                               |   (tienda_lego)  |
                                               +------------------+
```

**Principio clave:** backend y frontend son proyectos independientes. El backend no renderiza
vistas — solo expone datos via API REST. El frontend consume la API con `fetch()`.

## Modelo de datos

```
Categoria (1) ------> (0..*) Producto
Usuario   (1) ------> (0..*) Pedido
Carrito   (1) ------> (0..*) ItemCarrito -----> Producto
```

| Entidad     | Campos principales                                                         |
|-------------|---------------------------------------------------------------------------|
| Categoria   | id, nombre, descripcion, activa                                           |
| Producto    | id, nombre, numeroSet, descripcion, precio, stock, imagenUrl, activo, categoriaId |
| Usuario     | id, nombre, email, passwordHash, rol (USUARIO/ADMIN)                      |
| Carrito     | id, fechaCreacion, estado, total, items[]                                  |
| ItemCarrito | productoId, productoNombre, cantidad, precioUnitario, subtotal            |
| Pedido      | id, usuarioId, usuarioNombre, items[], total, metodoPago, estado, fecha   |

## Stack tecnologico

| Capa       | Tecnologia                                          |
|------------|-----------------------------------------------------|
| Backend    | Java 21 LTS + Spring Boot 4.0.2 + Gradle            |
| BD         | MongoDB 8 (Atlas)                                    |
| API Docs   | OpenAPI 3.1 / Swagger UI (springdoc-openapi 2.8)     |
| Seguridad  | Spring Security 7 (CSRF, headers CSP, HTTPS)         |
| Frontend   | HTML5 + SCSS + JavaScript ES2022 (vanilla)            |
| Estilos    | SCSS compilado con Dart Sass via npm                  |
| Gestor     | npm (frontend) + Gradle (backend)                     |

## Endpoints de la API

### Categorias (`/api/v1/categorias`)

| Metodo | Ruta                        | Descripcion                    |
|--------|-----------------------------|--------------------------------|
| GET    | `/api/v1/categorias`        | Listar categorias activas      |
| GET    | `/api/v1/categorias/todas`  | Listar todas (admin)           |
| GET    | `/api/v1/categorias/{id}`   | Buscar por ID                  |
| POST   | `/api/v1/categorias`        | Crear categoria                |
| PUT    | `/api/v1/categorias/{id}`   | Actualizar categoria           |
| DELETE | `/api/v1/categorias/{id}`   | Eliminar categoria             |

### Productos (`/api/v1/productos`)

| Metodo | Ruta                           | Descripcion                            |
|--------|--------------------------------|----------------------------------------|
| GET    | `/api/v1/productos`            | Listar activos (`?categoriaId=` filtro)|
| GET    | `/api/v1/productos/todos`      | Listar todos (admin)                   |
| GET    | `/api/v1/productos/{id}`       | Buscar por ID                          |
| POST   | `/api/v1/productos`            | Crear producto                         |
| PUT    | `/api/v1/productos/{id}`       | Actualizar producto                    |
| DELETE | `/api/v1/productos/{id}`       | Eliminar producto                      |

### Carrito (`/api/v1/carritos`)

| Metodo | Ruta                                             | Descripcion              |
|--------|--------------------------------------------------|--------------------------|
| POST   | `/api/v1/carritos`                               | Crear carrito             |
| GET    | `/api/v1/carritos/{id}`                          | Consultar carrito         |
| POST   | `/api/v1/carritos/{id}/items?productoId=&cantidad=` | Agregar producto      |
| PUT    | `/api/v1/carritos/{id}/items/{productoId}?cantidad=`| Cambiar cantidad      |
| DELETE | `/api/v1/carritos/{id}/items/{productoId}`       | Quitar producto           |

### Autenticacion (`/api/v1/auth`)

| Metodo | Ruta                    | Descripcion               |
|--------|-------------------------|---------------------------|
| POST   | `/api/v1/auth/registro` | Registrar usuario nuevo   |
| POST   | `/api/v1/auth/login`    | Iniciar sesion            |

### Pedidos (`/api/v1/pedidos`)

| Metodo | Ruta                               | Descripcion                      |
|--------|-------------------------------------|----------------------------------|
| POST   | `/api/v1/pedidos/carrito`           | Crear pedido desde carrito       |
| POST   | `/api/v1/pedidos/directo`           | Compra directa sin carrito       |
| GET    | `/api/v1/pedidos`                   | Listar todos (admin)             |
| GET    | `/api/v1/pedidos/usuario/{userId}`  | Pedidos de un usuario            |

### Imagenes (`/api/v1/imagenes`)

| Metodo | Ruta                  | Descripcion                |
|--------|-----------------------|----------------------------|
| POST   | `/api/v1/imagenes`    | Subir imagen (multipart)   |

## Seguridad implementada

### HTTPS
Certificado autofirmado generado con `keytool` (RSA 2048, PKCS12). Para activar:

```bash
cd backend
./gradlew bootRun --args='--spring.profiles.active=ssl'
```

El backend escucha en `https://localhost:8443`. El archivo `keystore.p12` esta en
`src/main/resources/`. **Para demostracion:** abrir `https://localhost:8443/swagger-ui.html`
y aceptar el certificado autofirmado.

### Mitigacion XSS
- **Content-Security-Policy** enviado en cada respuesta: `default-src 'self'; script-src 'self' 'unsafe-inline'; ...`
- **X-Content-Type-Options: nosniff** — previene MIME sniffing
- **X-Frame-Options: DENY** — previene clickjacking
- Validacion de entrada con Bean Validation (`@NotBlank`, `@Positive`, `@Min`)

**Caso de demostracion:** intentar crear un producto con nombre `<script>alert('xss')</script>`
— el backend lo almacena como texto plano, y el navegador NO ejecuta el script gracias a la
cabecera CSP que bloquea scripts inline no autorizados.

### Proteccion CSRF
- Spring Security genera un token CSRF y lo envia como cookie `XSRF-TOKEN`
- El frontend lee la cookie y la reenvia como header `X-XSRF-TOKEN` en cada peticion POST/PUT/DELETE
- Las rutas de autenticacion (`/api/v1/auth/**`) estan exentas para permitir registro y login

**Caso de demostracion:** abrir DevTools > Application > Cookies, ver la cookie `XSRF-TOKEN`.
En Network, verificar que cada POST lleva el header `X-XSRF-TOKEN`. Si se hace una peticion
POST sin el header (por ejemplo desde Postman sin token), el servidor responde 403 Forbidden.

## Estructura del proyecto

```
lego-store/
├── backend/                          # Proyecto Spring Boot (Gradle)
│   ├── build.gradle
│   ├── src/main/java/com/tiendalego/
│   │   ├── TiendaLegoApplication.java
│   │   ├── config/
│   │   │   ├── MongoConfig.java      # Conexion programatica a MongoDB Atlas
│   │   │   ├── OpenApiConfig.java    # Metadata de Swagger/OpenAPI
│   │   │   ├── SecurityConfig.java   # CSRF, CORS, cabeceras de seguridad
│   │   │   └── WebConfig.java        # Recursos estaticos (uploads)
│   │   ├── model/                    # Entidades MongoDB
│   │   ├── repository/               # Repositorios Spring Data
│   │   ├── service/                  # Logica de negocio
│   │   └── controller/               # Endpoints REST con anotaciones Swagger
│   └── src/test/java/com/tiendalego/
│       └── service/                  # Pruebas unitarias con Mockito
│
├── frontend/                         # Proyecto frontend independiente (npm)
│   ├── package.json                  # Gestor de dependencias + scripts
│   ├── scss/
│   │   └── styles.scss               # Fuente SCSS (variables, nesting, mixins)
│   ├── styles.css                    # CSS compilado desde SCSS
│   ├── csrf.js                       # Interceptor CSRF automatico
│   ├── app.js                        # Catalogo y navegacion
│   ├── admin.js                      # Panel de administracion
│   ├── carrito.js                    # Logica del carrito
│   ├── pago.js                       # Proceso de pago
│   ├── login.js / registro.js        # Autenticacion
│   └── *.html                        # Vistas (6 paginas)
│
└── docs/                             # Evidencias y capturas
```

## Como ejecutar

### Requisitos previos
- Java 21 LTS
- Node.js 18+ y npm
- Conexion a internet (MongoDB Atlas)

### Backend

```bash
cd backend
export MONGODB_URI="mongodb+srv://usuario:password@cluster.mongodb.net/tienda_lego?retryWrites=true&w=majority"
./gradlew bootRun
```

El backend inicia en `http://localhost:8080`.
Swagger UI: `http://localhost:8080/swagger-ui.html`

### Frontend

```bash
cd frontend
npm install
npm run build:css
python -m http.server 5500
```

Abrir `http://localhost:5500` en el navegador.

### Ejecutar pruebas

```bash
cd backend
./gradlew test
```

### Activar HTTPS (para demostracion)

```bash
cd backend
./gradlew bootRun --args='--spring.profiles.active=ssl'
```

Abrir `https://localhost:8443/swagger-ui.html` y aceptar el certificado.

## Evidencias Swagger

![Swagger Overview](docs/swagger-overview.jpg)
![Swagger Endpoints](docs/swagger-endpoints.jpg)
![Swagger Productos y Schemas](docs/swagger-productos-schemas.jpg)

## Metodologia

Se sigue una metodologia iterativa incremental, con entregas semanales (semanas 10 a 16):

1. **Semana 10:** Definicion, planeacion, modelo de datos, arquitectura
2. **Semana 11:** Backend — modelos, conexion a BD, primeros endpoints con Swagger
3. **Semana 12:** CRUD completo + frontend catalogo consumiendo la API
4. **Semana 13:** Carrito de compras + panel de administrador
5. **Semana 14:** Seguridad (HTTPS, XSS, CSRF) + SCSS + npm
6. **Semana 15:** Pruebas unitarias, integracion final, documentacion tecnica
7. **Semana 16:** Entrega final, aplicacion accesible, sustentacion

## Requerimientos funcionales

| ID     | Descripcion                                                |
|--------|------------------------------------------------------------|
| RF-01  | CRUD de categorias de productos                            |
| RF-02  | CRUD de productos asociados a categorias                   |
| RF-03  | Catalogo publico filtrable por categoria                   |
| RF-04  | Carrito de compras con recalculo de totales                 |
| RF-05  | Panel de administracion para productos y categorias         |
| RF-06  | Registro e inicio de sesion de usuarios                     |
| RF-07  | Proceso de pago simulado (tarjeta, PSE, contra entrega)     |
| RF-08  | Subida de imagenes para productos                           |
| RF-09  | API REST documentada con Swagger/OpenAPI                    |
| RF-10  | Consulta de pedidos por usuario y administrador             |

## Requerimientos no funcionales

| ID      | Descripcion                                                 |
|---------|-------------------------------------------------------------|
| RNF-01  | Backend y frontend como proyectos independientes             |
| RNF-02  | Comunicacion exclusiva por API REST                          |
| RNF-03  | HTTPS disponible para demostracion                           |
| RNF-04  | Mitigacion de XSS con Content-Security-Policy                |
| RNF-05  | Proteccion CSRF con token en cookie + header                 |
| RNF-06  | Estilos escritos en SCSS, compilados con Dart Sass           |
| RNF-07  | Gestor de dependencias npm en el frontend                    |
| RNF-08  | Pruebas unitarias con JUnit 5 y Mockito                      |
| RNF-09  | Aplicacion accesible durante la sustentacion                 |
| RNF-10  | Tiempo de respuesta < 2 segundos por operacion               |
