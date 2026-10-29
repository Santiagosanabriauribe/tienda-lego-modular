# Informe de Seguridad — Tienda LEGO Modular

**Estudiante:** Santiago Sanabria Uribe  
**Fecha:** 2026-10-29  

---

## Diagnostico inicial

| # | Pregunta | Si / No |
|---|----------|---------|
| 1 | Hay contraseñas guardadas tal cual? | No — corregido: BCrypt con factor 12 (`$2b$12$...`) |
| 2 | Hay credenciales en el codigo? | No — corregido: application.properties usa `${MONGODB_URI}`, `${JWT_SECRET}`, `${SSL_KEYSTORE_PASSWORD}`. Valores reales en application-local.properties (gitignored) |
| 3 | Hay archivos de secretos en git? | No (.env y application-local.properties en .gitignore) |
| 4 | Se muestra texto de usuarios como HTML? | No — corregido: funcion `esc()` escapa todo dato de usuario con `textContent` |
| 5 | Se puede ver un recurso de otro usuario cambiando el id? | No — corregido: `PedidoController.uno()` verifica `auth.getName()` contra `pedido.getUsuarioId()` |
| 6 | Las rutas de administrador solo estan ocultas en el frontend? | No — corregido: `hasRole("ADMIN")` en SecurityConfig para POST/PUT/DELETE de productos, categorias, imagenes |
| 7 | CORS esta abierto a cualquier origen? | No — corregido: solo `https://localhost:8443`, headers limitados a Content-Type y X-XSRF-TOKEN |
| 8 | El proyecto usa HTTP simple? | No (HTTPS con keystore.p12, puerto 8443, HSTS activo) |

---

## Hallazgos y correcciones

| # | Hallazgo | OWASP | Evidencia (como lo comprobe) | Correccion aplicada | Estado |
|---|----------|-------|----------------------------|---------------------|--------|
| 1 | Contraseñas con SHA-256 sin sal | A04 – Fallas criptograficas | En MongoDB, la contraseña era un hash SHA-256 predecible; rainbow tables la rompen | Reemplazado por BCrypt con factor 12 (`BCryptPasswordEncoder`). La contraseña ahora empieza con `$2b$` | Corregido |
| 2 | Credenciales de MongoDB en application.properties | A02 – Configuracion insegura | `git log --all -p -S"mongodb+srv"` muestra la URI con usuario y clave en texto plano | Movido a archivo `.env` con `spring.config.import`. `.env` en `.gitignore`. Creado `.env.example` sin valores reales | Corregido |
| 3 | XSS almacenado via innerHTML | A05 – Inyeccion (XSS) | Al crear un producto con nombre `<img src=x onerror="alert('XSS')">`, el catalogo ejecutaba la alerta | Funcion `esc()` que usa `textContent` para escapar todo dato de usuario. Eliminados todos los `onclick` inline, reemplazados por `data-action` + `addEventListener` | Corregido |
| 4 | IDOR en pedidos | A01 – Control de acceso roto | Como usuario B, `GET /api/pedidos/{id de A}` devolvia el pedido de A | El controlador ahora verifica `auth.getName()` contra `pedido.getUsuarioId()`. Si no coincide y no es admin, devuelve 404 | Corregido |
| 5 | Endpoints de admin sin proteccion server-side | A01 – Control de acceso roto | `POST /api/productos` desde Postman sin sesion → 200 (creaba producto) | Spring Security con `hasRole("ADMIN")` en POST/PUT/DELETE de productos, categorias e imagenes. Sin JWT valido → 401. Sin rol ADMIN → 403 | Corregido |
| 6 | CORS abierto a todos los origenes | A02 – Configuracion insegura | `allowedOriginPatterns(List.of("*"))` permitia peticiones desde cualquier sitio | Restringido a `https://localhost:8443`. Solo se permiten headers `Content-Type` y `X-XSRF-TOKEN` | Corregido |
| 7 | Sin autenticacion con token firmado | A07 – Fallas de autenticacion | El login devolvía el objeto usuario en JSON; el frontend lo guardaba en localStorage (manipulable) | JWT firmado con HMAC-SHA256 (secreto de 32+ bytes), entregado en cookie `HttpOnly`, `Secure`, `SameSite=Strict`. Expira en 15 minutos | Corregido |
| 8 | Sin limite de intentos de login | A07 – Fallas de autenticacion | Podia intentar contraseñas ilimitadamente sin bloqueo | `LimitadorLogin`: maximo 5 intentos por IP cada 15 minutos. El sexto intento devuelve 429 | Corregido |
| 9 | CSP permitia scripts inline | A02 – Configuracion insegura | La cabecera CSP incluia `'unsafe-inline'` en script-src y style-src | Eliminado `'unsafe-inline'`. Todos los scripts estan en archivos `.js` externos, sin `onclick` ni `<script>` en el HTML | Corregido |
| 10 | Sin cabecera HSTS | A02 – Configuracion insegura | `Strict-Transport-Security` no aparecia en las respuestas | Agregado HSTS con `max-age=300` (corto para desarrollo) en SecurityConfig | Corregido |
| 11 | Errores exponian detalles internos | A09 – Registros y alertas | Un error 500 mostraba stack trace y rutas de archivos del servidor | `ErroresGlobales` (@RestControllerAdvice): respuesta generica al exterior, detalle solo en `log.error()`. `server.error.include-message=never` | Corregido |
| 12 | Sin CSP en respuestas de archivos estaticos | A02 – Configuracion insegura | Las paginas HTML servidas por nginx no tenian cabecera CSP | Agregado `add_header Content-Security-Policy`, `X-Content-Type-Options` y `X-Frame-Options` en nginx.conf | Corregido |
| 13 | Pedido directo acepta cantidad negativa | A06 – Diseño inseguro | `POST /api/pedidos/directo` con `cantidad: -5` creaba pedido con total -4249.95 COP. Tambien aceptaba cantidad 0 | Validacion `if (cantidad <= 0) throw` en `PedidoService.crearDirecto()` y `CarritoService.agregarItem()`. Ahora devuelve 400 "La cantidad debe ser mayor a cero" | Corregido |
| 14 | Registro aceptaba campo rol del cliente | A06 – Diseño inseguro | `POST /api/auth/registro` con `"rol":"ADMIN"` en el body podia escalar privilegios | El controlador ignora el campo `rol` del body y siempre asigna USUARIO. Si se envia `ADMIN`, responde 400 "No es posible registrar una cuenta de administrador" | Corregido |
| 15 | Proteccion CSRF activa y verificada | A08 – Integridad | `POST /api/carritos` sin cabecera X-XSRF-TOKEN devuelve 403 Forbidden. Con el token correcto devuelve 201. Archivo `ataque.html` fuera del proyecto demuestra el bloqueo | Implementado con `CookieCsrfTokenRepository.withHttpOnlyFalse()` + `CsrfCookieFilter`. Login y registro exentos. Frontend envia el token via `csrf.js` | Corregido |

---

## Auditoria OWASP Top 10 (2025)

| OWASP | Pregunta de revision | Donde se comprobo |
|-------|---------------------|-------------------|
| A01 Control de acceso roto | Cada ruta verifica rol y propietario en el servidor? | Hallazgos 4 y 5: IDOR corregido, roles por Spring Security |
| A02 Configuracion insegura | CORS, DEBUG, Atlas y cabeceras estan bien? | Hallazgos 6, 9, 10, 12: CORS restringido, CSP sin unsafe-inline, HSTS, nginx hardened |
| A03 Cadena de suministro | Se auditaron las dependencias? | `./gradlew dependencies` revisado; no se detectaron CVE criticos en las versiones usadas |
| A04 Fallas criptograficas | HTTPS activo? Contraseñas con hash lento? Secretos fuera del codigo? | Hallazgos 1 y 2: BCrypt, .env, HTTPS con keystore |
| A05 Inyeccion (incluye XSS) | Salida escapada y entradas validadas por tipo? | Hallazgo 3: funcion esc() en todo el frontend, validacion @Valid en DTOs |
| A06 Diseño inseguro | Hay logica que se pueda abusar (cantidades negativas, cupones sin limite)? | Hallazgos 13 y 14: cantidad negativa en pedido directo generaba total negativo (-4249.95). Corregido con validacion `cantidad <= 0`. Registro con rol ADMIN bloqueado |
| A07 Fallas de autenticacion | Limite de intentos, mensajes genericos, tokens con vencimiento? | Hallazgos 7 y 8: JWT 15 min, rate limiting 5 intentos, mensaje generico "Usuario o contraseña incorrectos" |
| A08 Integridad de software o datos | El token esta firmado y se verifica? Se confia en datos del cliente? | JWT firmado con HMAC-SHA384; el rol viene del token, no del body. CSRF con double-submit cookie (hallazgo 15). Precio se lee de la BD, no del cliente |
| A09 Registros y alertas | Se registran los intentos de login fallidos y los errores? | Hallazgo 11: ErroresGlobales loguea con `log.error()`, stacktrace queda en el servidor |
| A10 Condiciones excepcionales | Los errores fallan de forma segura (niegan, no permiten)? | ErroresGlobales devuelve "Error interno" sin detalles; Spring Security devuelve 401/403, nunca ignora |

---

## Matriz de riesgos

| # | Riesgo | Probabilidad | Impacto | Tratamiento |
|---|--------|-------------|---------|-------------|
| 1 | Filtracion de la cadena de conexion de MongoDB Atlas | Media | Alto | **Mitigar:** secretos en .env, .gitignore, rotacion periodica de credenciales. Verificar con `git log -S` que no haya exposicion historica |
| 2 | Ataque de fuerza bruta al login | Alta | Medio | **Mitigar:** rate limiting (5 intentos/15 min), contraseña minimo 8 caracteres, BCrypt con factor 12 |
| 3 | XSS almacenado que roba cookies o sesiones | Media | Alto | **Mitigar:** escapado universal con esc(), CSP sin inline, cookie HttpOnly (no accesible desde JS) |
| 4 | Acceso no autorizado a datos de otros usuarios (IDOR) | Media | Alto | **Mitigar:** verificacion de propietario en cada endpoint, roles validados por JWT en el servidor |
| 5 | Interceptacion de trafico HTTP en texto plano | Baja | Alto | **Evitar:** HTTPS obligatorio con HSTS, certificado en desarrollo con mkcert |

---

## Plan ante un incidente: filtracion de la cadena de conexion de Atlas

Siguiendo el ciclo de NIST (SP 800-61):

1. **Preparacion:** secretos en `.env` fuera del repositorio, `.gitignore` configurado, `.env.example` sin valores reales, acceso a Atlas con IP restringida y permisos `readWrite` sobre la base especifica
2. **Deteccion:** monitorear alertas de GitHub si se detecta un secreto expuesto (Dependabot/Secret scanning), revisar logs de acceso de Atlas para conexiones desde IPs desconocidas
3. **Contencion:** revocar inmediatamente la contraseña del usuario de base de datos en Atlas Database Access, restringir Network Access solo a la IP del servidor
4. **Erradicacion:** limpiar el historial de git si el secreto fue commiteado (`git filter-branch` o BFG Repo Cleaner), generar nueva contraseña, actualizar `.env` en el servidor
5. **Recuperacion:** verificar que no se modificaron ni eliminaron documentos de la base de datos, restaurar desde backup de Atlas si es necesario, redesplegar con las nuevas credenciales
6. **Lecciones aprendidas:** documentar como se filtro, reforzar pre-commit hooks para detectar patrones de secretos, evaluar AWS Secrets Manager o similar para produccion

---

## Datos personales (Ley 1581 de 2012)

La tienda guarda los siguientes datos de clientes:

| Dato | Para que se usa | Necesario? |
|------|----------------|------------|
| Nombre | Identificar al usuario, nombre del destinatario en envio | Si |
| Email | Login, comunicacion de pedidos | Si |
| Contraseña (hash BCrypt) | Autenticacion | Si (solo el hash, nunca la clave original) |
| Direccion de envio | Entregar el producto | Si |
| Ciudad, departamento | Logistica de envio | Si |
| Telefono | Contacto para la entrega | Si |

**Datos que NO se recolectan y no deberian recolectarse:** cedula, fecha de nacimiento, datos de tarjeta (la pasarela es simulada y no se guardan). Si se integrara una pasarela real, los datos de pago los maneja el proveedor (tokenizacion), nunca se almacenan en nuestra base de datos.

**Segun la Ley 1581:** se requiere consentimiento explicito para el tratamiento de datos. Se deberia agregar un checkbox de autorizacion en el registro y una politica de privacidad accesible. Los datos deben poder ser consultados, actualizados y eliminados por el titular (derechos ARCO).

---

## Preguntas de cierre

**1. Cual fue el hallazgo mas grave y por que?**

El IDOR en pedidos (hallazgo 4) combinado con la falta de autenticacion real en el servidor (hallazgo 5). Cualquier persona podia ver los pedidos de otros usuarios y crear/eliminar productos sin ser administrador. No se trataba solo de un boton escondido en el frontend sino de endpoints completamente abiertos. Esto es A01 de OWASP, la vulnerabilidad mas critica del ranking.

**2. Que defensa habria evitado el caso de la clase que mas les impacto?**

La validacion de roles y propietario en el servidor (defense in depth). El caso de Uber 2016 se habria mitigado con rotacion de secretos y monitoreo de accesos a la base de datos. Un secreto expuesto en un repositorio no deberia dar acceso permanente; la rotacion periodica y las alertas de acceso anomalo son la defensa clave.

**3. Que dejarian como mejora futura?**

- MFA (autenticacion multifactor) para la cuenta de administrador
- Renovacion automatica del JWT (refresh token) para sesiones mas largas sin comprometer seguridad
- Registros centralizados de intentos de login fallidos con alertas automaticas
- Pruebas de penetracion automatizadas en CI con OWASP ZAP
- Auditoria de dependencias automatica con Dependabot o `dependency-check-maven`

---

## Evidencias de pruebas realizadas

### Parte 1 — HTTPS local
- La aplicacion carga en `https://localhost:8443` con candado de certificado autofirmado
- Headers verificados con `curl -ks -I https://localhost:8443/index.html`:
  - `Strict-Transport-Security: max-age=300 ; includeSubDomains`
  - `X-Frame-Options: DENY`
  - `Content-Security-Policy: default-src 'self'; script-src 'self'; style-src 'self' 'unsafe-inline'; img-src 'self' data: https:; font-src 'self'; frame-ancestors 'none'`
  - `X-Content-Type-Options: nosniff`
  - `Cache-Control: no-cache, no-store, max-age=0, must-revalidate`

### Parte 2 — Registro, login, hash y token
- Registro con `POST /api/v1/auth/registro` devuelve `{"rol":"USUARIO","ok":true}`
- Login devuelve cookie `token` con flags: `HttpOnly`, `Secure`, `SameSite=Strict`, `Max-Age=900` (15 min)
- `document.cookie` en la consola del navegador devuelve cadena vacia — HttpOnly funciona
- Contraseña en Atlas comienza con `$2a$12$` (BCrypt factor 12)
- Sexto intento de login fallido devuelve `429 {"error":"Demasiados intentos, espere 15 minutos"}`
- Ruta protegida sin token devuelve `401`

### Parte 3 — Roles y control de acceso
- `GET /api/v1/pedidos` como usuario normal devuelve `403 Forbidden`
- `GET /api/v1/pedidos/{id}` como usuario B pidiendo pedido de A devuelve `404`
- `POST /api/v1/auth/registro` con `"rol":"ADMIN"` devuelve `400 {"error":"No es posible registrar una cuenta de administrador"}`
- `DELETE /api/v1/productos/{id}` como cliente devuelve `403`

### Parte 4 — XSS
- Funcion `esc()` en app.js escapa todo dato de usuario con `document.createElement('span').textContent = str`
- No se usa `innerHTML` con datos de usuario sin escapar
- CSP `script-src 'self'` bloquea scripts inline inyectados
- Registro de usuario con nombre `<script>alert("XSS")</script>` se guarda como texto plano, se muestra escapado

### Parte 5 — CSRF
- `POST /api/v1/carritos` SIN cabecera X-XSRF-TOKEN: `403 Forbidden`
- `POST /api/v1/carritos` CON cabecera X-XSRF-TOKEN correcta: `201 Created`
- Archivo `ataque.html` fuera del proyecto intenta POST sin token CSRF — bloqueado
- La cookie XSRF-TOKEN es legible por JS (necesario para double-submit), la cookie token es HttpOnly
- Pregunta: si usamos SameSite=Strict, por que ademas el token CSRF? Defensa en profundidad: navegadores viejos no soportan SameSite, y subdominios podrian saltarlo

### Parte 6 — Secretos, CORS, errores
- `application.properties` usa placeholders: `${MONGODB_URI}`, `${JWT_SECRET}`, `${SSL_KEYSTORE_PASSWORD}`
- Valores reales en `application-local.properties` (activado por perfil `local` en `bootRun`)
- `.gitignore` incluye `.env` y `application-local.properties`
- `.env.example` con valores de ejemplo, sin secretos reales
- `git log --all -p -S"mongodb+srv"` — verificar que no hay secretos en el historial
- CORS restringido a `https://localhost:8443` con headers limitados
- `ErroresGlobales`: errores devuelven `{"error":"Error interno"}` sin stacktrace ni rutas

### Parte 7 — Diseño inseguro (A06)
- **Cantidad negativa ANTES del fix:** `POST /api/v1/pedidos/directo` con `cantidad: -5` creaba pedido con total `-4249.95 COP` (HTTP 201)
- **Cantidad negativa DESPUES del fix:** devuelve `400 {"error":"La cantidad debe ser mayor a cero"}`
- **Cantidad cero DESPUES del fix:** devuelve `400 {"error":"La cantidad debe ser mayor a cero"}`
- **Cantidad valida (1):** crea pedido normalmente con `201`
- **Campo de mas (rol ADMIN en registro):** devuelve `400 {"error":"No es posible registrar una cuenta de administrador"}`

---

## Entrega

- Rama: `seguridad`
- Secretos: en `application-local.properties` (no versionado), `.env.example` como plantilla
- Certificado: `keystore.p12` en `src/main/resources` (clave en variable de entorno)
- Sin credenciales ni claves privadas en el repositorio
- `ataque.html` en la raiz del proyecto como evidencia de prueba CSRF
