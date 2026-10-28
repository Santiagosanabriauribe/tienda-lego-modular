const API = '/api/v1';

let categorias = [];
let productos = [];
let categoriaActiva = 'todas';
let cantidadesCarrito = {};

function getUsuario() {
    try { return JSON.parse(localStorage.getItem('usuario')); } catch { return null; }
}

function renderNav() {
    const nav = document.getElementById('nav-links');
    const usuario = getUsuario();

    if (!usuario) {
        nav.innerHTML = `
            <a href="login.html">Iniciar Sesion</a>
            <a href="registro.html">Registrarse</a>
        `;
    } else {
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
            window.location.href = 'index.html';
        });
    }
}

async function cargarCategorias() {
    try {
        const res = await fetch(`${API}/categorias`);
        categorias = await res.json();
        renderCategorias();
    } catch (err) {
        console.error('Error cargando categorias:', err);
    }
}

async function cargarProductos() {
    try {
        const url = categoriaActiva === 'todas'
            ? `${API}/productos`
            : `${API}/productos?categoriaId=${categoriaActiva}`;
        const res = await fetch(url);
        productos = await res.json();
        renderProductos();
    } catch (err) {
        console.error('Error cargando productos:', err);
        document.getElementById('productos-grid').innerHTML =
            '<p class="sin-productos">Error al cargar los productos. Verifica que el backend este corriendo.</p>';
    }
}

async function cargarCarrito() {
    const carritoId = localStorage.getItem('carritoId');
    if (!carritoId) return;
    try {
        const res = await fetch(`${API}/carritos/${carritoId}`);
        if (!res.ok) return;
        const carrito = await res.json();
        cantidadesCarrito = {};
        if (carrito.items) {
            carrito.items.forEach(item => {
                cantidadesCarrito[item.productoId] = item.cantidad;
            });
        }
    } catch { /* carrito no existe o error de red */ }
}

function renderCategorias() {
    const container = document.getElementById('categorias-btns');
    let html = '<button class="cat-btn active" data-id="todas">Todas</button>';
    categorias.forEach(cat => {
        html += `<button class="cat-btn" data-id="${cat.id}">${cat.nombre}</button>`;
    });
    container.innerHTML = html;

    container.querySelectorAll('.cat-btn').forEach(btn => {
        btn.addEventListener('click', () => {
            container.querySelectorAll('.cat-btn').forEach(b => b.classList.remove('active'));
            btn.classList.add('active');
            categoriaActiva = btn.dataset.id;
            cargarProductos();
        });
    });
}

function nombreCategoria(categoriaId) {
    const cat = categorias.find(c => c.id === categoriaId);
    return cat ? cat.nombre : '';
}

function renderProductos() {
    const grid = document.getElementById('productos-grid');
    const usuario = getUsuario();

    if (productos.length === 0) {
        grid.innerHTML = '<p class="sin-productos">No hay productos disponibles.</p>';
        return;
    }

    grid.innerHTML = productos.map(p => {
        const enCarrito = cantidadesCarrito[p.id] || 0;
        return `
        <div class="producto-card">
            <div class="producto-card-img" onclick="abrirDetalle('${p.id}')">
                <img src="${p.imagenUrl || 'https://placehold.co/400x300/fafafa/ccc?text=LEGO'}"
                     alt="${p.nombre}"
                     onerror="this.src='https://placehold.co/400x300/fafafa/ccc?text=LEGO'">
            </div>
            <div class="producto-info">
                <span class="categoria-tag">${nombreCategoria(p.categoriaId)}</span>
                <h3>${p.nombre}</h3>
                ${p.numeroSet ? `<span class="set-number">Set #${p.numeroSet}</span>` : ''}
                <div class="precio-stock">
                    <span class="precio">${formatoPrecio(p.precio)}</span>
                    <span class="stock ${p.stock === 0 ? 'agotado' : ''}">
                        ${p.stock > 0 ? `${p.stock} disponibles` : 'Agotado'}
                    </span>
                </div>
                ${usuario && p.stock > 0 ? `
                    <div class="producto-acciones" id="acciones-${p.id}">
                        ${enCarrito > 0
                            ? renderContador(p.id, enCarrito, p.stock)
                            : `<button class="btn-agregar" onclick="agregarAlCarrito('${p.id}')">Agregar al carrito</button>`
                        }
                        <button class="btn-comprar-ya" onclick="comprarDirecto('${p.id}')">Comprar</button>
                    </div>
                ` : ''}
            </div>
        </div>`;
    }).join('');
}

function renderContador(productoId, cantidad, stock) {
    return `<div class="contador-carrito">
        <button class="btn-cnt" onclick="cambiarCantidad('${productoId}', -1, ${stock})">-</button>
        <span class="cnt-valor" id="cnt-${productoId}">${cantidad}</span>
        <button class="btn-cnt" onclick="cambiarCantidad('${productoId}', 1, ${stock})" ${cantidad >= stock ? 'disabled style="opacity:0.4;cursor:not-allowed"' : ''}>+</button>
    </div>`;
}

function actualizarBotonesProducto(productoId) {
    const contenedor = document.getElementById(`acciones-${productoId}`);
    if (!contenedor) return;
    const p = productos.find(pr => pr.id === productoId);
    if (!p) return;
    const enCarrito = cantidadesCarrito[productoId] || 0;

    const botonComprar = `<button class="btn-comprar-ya" onclick="comprarDirecto('${productoId}')">Comprar</button>`;

    if (enCarrito > 0) {
        contenedor.innerHTML = renderContador(productoId, enCarrito, p.stock) + botonComprar;
    } else {
        contenedor.innerHTML = `<button class="btn-agregar" onclick="agregarAlCarrito('${productoId}')">Agregar al carrito</button>` + botonComprar;
    }
}

function formatoPrecio(precio) {
    return '$ ' + Number(precio).toLocaleString('es-CO') + ' COP';
}

function abrirDetalle(id) {
    const p = productos.find(prod => prod.id === id);
    if (!p) return;

    const modal = document.getElementById('modal');
    const body = document.getElementById('modal-body');
    const usuario = getUsuario();
    const enCarrito = cantidadesCarrito[p.id] || 0;

    body.innerHTML = `
        <div class="modal-body-inner">
            <img src="${p.imagenUrl || 'https://placehold.co/600x400/fafafa/ccc?text=LEGO'}"
                 alt="${p.nombre}"
                 onerror="this.src='https://placehold.co/600x400/fafafa/ccc?text=LEGO'">
            <div class="detalle">
                <span class="categoria-tag">${nombreCategoria(p.categoriaId)}</span>
                <h2>${p.nombre}</h2>
                ${p.numeroSet ? `<p class="set-number">Set #${p.numeroSet}</p>` : ''}
                <p class="descripcion">${p.descripcion || 'Sin descripcion disponible.'}</p>
                <div class="precio-stock">
                    <span class="precio">${formatoPrecio(p.precio)}</span>
                    <span class="stock ${p.stock === 0 ? 'agotado' : ''}">
                        ${p.stock > 0 ? `${p.stock} unidades disponibles` : 'Agotado'}
                    </span>
                </div>
                ${usuario && p.stock > 0 ? `
                    <div class="producto-acciones" id="acciones-modal-${p.id}" style="margin-top:1rem">
                        ${enCarrito > 0
                            ? renderContadorModal(p.id, enCarrito, p.stock)
                            : `<button class="btn-agregar" onclick="agregarAlCarrito('${p.id}')">Agregar al carrito</button>`
                        }
                        <button class="btn-comprar-ya" onclick="comprarDirecto('${p.id}')">Comprar ahora</button>
                    </div>
                ` : ''}
            </div>
        </div>
    `;

    modal.classList.remove('hidden');
}

function renderContadorModal(productoId, cantidad, stock) {
    return `<div class="contador-carrito">
        <button class="btn-cnt" onclick="cambiarCantidad('${productoId}', -1, ${stock})">-</button>
        <span class="cnt-valor" id="cnt-modal-${productoId}">${cantidad}</span>
        <button class="btn-cnt" onclick="cambiarCantidad('${productoId}', 1, ${stock})" ${cantidad >= stock ? 'disabled style="opacity:0.4;cursor:not-allowed"' : ''}>+</button>
    </div>`;
}

async function agregarAlCarrito(productoId) {
    const usuario = getUsuario();
    if (!usuario) { window.location.href = 'login.html'; return; }

    let carritoId = localStorage.getItem('carritoId');

    try {
        if (!carritoId) {
            const res = await fetch(`${API}/carritos`, { method: 'POST' });
            const carrito = await res.json();
            carritoId = carrito.id;
            localStorage.setItem('carritoId', carritoId);
        }

        const res = await fetch(`${API}/carritos/${carritoId}/items?productoId=${productoId}&cantidad=1`, {
            method: 'POST'
        });

        if (res.ok) {
            cantidadesCarrito[productoId] = (cantidadesCarrito[productoId] || 0) + 1;
            actualizarBotonesProducto(productoId);
            actualizarBotonesModal(productoId);
        } else {
            const data = await res.json();
            console.error(data.error);
        }
    } catch (err) {
        console.error('Error de conexion');
    }
}

async function cambiarCantidad(productoId, delta, stock) {
    const nuevaCantidad = (cantidadesCarrito[productoId] || 0) + delta;

    if (nuevaCantidad > stock) return;

    const carritoId = localStorage.getItem('carritoId');
    if (!carritoId) return;

    try {
        if (nuevaCantidad <= 0) {
            const res = await fetch(`${API}/carritos/${carritoId}/items/${productoId}`, {
                method: 'DELETE'
            });
            if (res.ok) {
                delete cantidadesCarrito[productoId];
                actualizarBotonesProducto(productoId);
                actualizarBotonesModal(productoId);
            }
        } else {
            const res = await fetch(`${API}/carritos/${carritoId}/items?productoId=${productoId}&cantidad=${delta}`, {
                method: 'POST'
            });
            if (res.ok) {
                cantidadesCarrito[productoId] = nuevaCantidad;
                actualizarBotonesProducto(productoId);
                actualizarBotonesModal(productoId);
            }
        }
    } catch (err) {
        console.error('Error actualizando cantidad');
    }
}

function actualizarBotonesModal(productoId) {
    const contenedor = document.getElementById(`acciones-modal-${productoId}`);
    if (!contenedor) return;
    const p = productos.find(pr => pr.id === productoId);
    if (!p) return;
    const enCarrito = cantidadesCarrito[productoId] || 0;

    const botonComprar = `<button class="btn-comprar-ya" onclick="comprarDirecto('${productoId}')">Comprar ahora</button>`;

    if (enCarrito > 0) {
        contenedor.innerHTML = renderContadorModal(productoId, enCarrito, p.stock) + botonComprar;
    } else {
        contenedor.innerHTML = `<button class="btn-agregar" onclick="agregarAlCarrito('${productoId}')">Agregar al carrito</button>` + botonComprar;
    }
}

function comprarDirecto(productoId) {
    const usuario = getUsuario();
    if (!usuario) { window.location.href = 'login.html'; return; }
    window.location.href = `pago.html?tipo=directo&productoId=${productoId}&cantidad=1`;
}

document.getElementById('modal-close').addEventListener('click', () => {
    document.getElementById('modal').classList.add('hidden');
});

document.getElementById('modal').addEventListener('click', (e) => {
    if (e.target === e.currentTarget) {
        e.currentTarget.classList.add('hidden');
    }
});

document.addEventListener('keydown', (e) => {
    if (e.key === 'Escape') {
        document.getElementById('modal').classList.add('hidden');
    }
});

renderNav();
cargarCategorias();
cargarCarrito().then(() => cargarProductos());
