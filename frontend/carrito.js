const API = '/api/v1';

const usuario = JSON.parse(localStorage.getItem('usuario'));
if (!usuario) {
    window.location.href = 'login.html';
}

function renderNav() {
    const nav = document.getElementById('nav-links');
    if (!usuario) return;
    nav.innerHTML = `
        <a href="index.html">Catalogo</a>
        <a href="carrito.html">Carrito</a>
        ${usuario.rol === 'ADMIN' ? '<a href="admin.html">Admin</a>' : `<span class="nav-user">${usuario.nombre}</span>`}
        <a href="#" id="btn-logout">Salir</a>
    `;
    document.getElementById('btn-logout').addEventListener('click', (e) => {
        e.preventDefault();
        localStorage.removeItem('usuario');
        localStorage.removeItem('carritoId');
        window.location.href = 'login.html';
    });
}

async function cargarCarrito() {
    const contenido = document.getElementById('carrito-contenido');
    const carritoId = localStorage.getItem('carritoId');

    if (!carritoId) {
        contenido.innerHTML = '<p class="sin-productos">Tu carrito esta vacio. <a href="index.html">Ver catalogo</a></p>';
        return;
    }

    try {
        const res = await fetch(`${API}/carritos/${carritoId}`);
        if (!res.ok) {
            contenido.innerHTML = '<p class="sin-productos">Tu carrito esta vacio. <a href="index.html">Ver catalogo</a></p>';
            return;
        }

        const carrito = await res.json();

        if (!carrito.items || carrito.items.length === 0) {
            contenido.innerHTML = '<p class="sin-productos">Tu carrito esta vacio. <a href="index.html">Ver catalogo</a></p>';
            return;
        }

        contenido.innerHTML = `
            <div class="carrito-lista">
                ${carrito.items.map(item => `
                    <div class="carrito-item">
                        <div class="carrito-item-info">
                            <h3>${item.productoNombre}</h3>
                            <p>Precio unitario: ${formatoPrecio(item.precioUnitario)}</p>
                        </div>
                        <div class="carrito-item-controles">
                            <button class="btn-cantidad" onclick="cambiarCantidad('${item.productoId}', ${item.cantidad - 1})">-</button>
                            <span class="cantidad">${item.cantidad}</span>
                            <button class="btn-cantidad" onclick="cambiarCantidad('${item.productoId}', ${item.cantidad + 1})">+</button>
                            <span class="item-subtotal">${formatoPrecio(item.subtotal)}</span>
                            <button class="btn-eliminar" onclick="eliminarItem('${item.productoId}')">Eliminar</button>
                        </div>
                    </div>
                `).join('')}
            </div>
            <div class="carrito-total">
                <h3>Total: ${formatoPrecio(carrito.total)}</h3>
                <button class="btn-primary btn-comprar" onclick="comprarCarrito()">Comprar Todo</button>
            </div>
        `;
    } catch (err) {
        contenido.innerHTML = '<p class="sin-productos">Error al cargar el carrito.</p>';
    }
}

async function cambiarCantidad(productoId, nuevaCantidad) {
    const carritoId = localStorage.getItem('carritoId');
    if (nuevaCantidad <= 0) {
        await eliminarItem(productoId);
        return;
    }
    try {
        await fetch(`${API}/carritos/${carritoId}/items/${productoId}?cantidad=${nuevaCantidad}`, {
            method: 'PUT'
        });
        cargarCarrito();
    } catch (err) {
        alert('Error al actualizar cantidad');
    }
}

async function eliminarItem(productoId) {
    const carritoId = localStorage.getItem('carritoId');
    try {
        await fetch(`${API}/carritos/${carritoId}/items/${productoId}`, {
            method: 'DELETE'
        });
        cargarCarrito();
    } catch (err) {
        alert('Error al eliminar producto');
    }
}

function comprarCarrito() {
    const carritoId = localStorage.getItem('carritoId');
    window.location.href = `pago.html?tipo=carrito&carritoId=${carritoId}`;
}

function formatoPrecio(precio) {
    return '$ ' + Number(precio).toLocaleString('es-CO') + ' COP';
}

renderNav();
cargarCarrito();
