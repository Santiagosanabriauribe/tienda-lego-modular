const API = '/api/v1';

const rol = localStorage.getItem('rol');
if (rol !== 'ADMIN') {
    window.location.href = 'index.html';
}

function esc(str) {
    const el = document.createElement('span');
    el.textContent = String(str ?? '');
    return el.innerHTML;
}

function renderNav() {
    const nav = document.getElementById('nav-links');
    nav.innerHTML = `
        <a href="index.html">Catalogo</a>
        <a href="carrito.html">Carrito</a>
        <a href="admin.html">Admin</a>
        <a href="#" id="btn-logout">Salir</a>
    `;
    document.getElementById('btn-logout').addEventListener('click', (e) => {
        e.preventDefault();
        fetch(`${API}/auth/logout`, { method: 'POST' }).finally(() => {
            localStorage.removeItem('rol');
            localStorage.removeItem('carritoId');
            window.location.href = 'login.html';
        });
    });
}

document.querySelectorAll('.admin-tab').forEach(tab => {
    tab.addEventListener('click', () => {
        document.querySelectorAll('.admin-tab').forEach(t => t.classList.remove('active'));
        document.querySelectorAll('.admin-panel').forEach(p => p.classList.add('hidden'));
        tab.classList.add('active');
        document.getElementById('tab-' + tab.dataset.tab).classList.remove('hidden');

        if (tab.dataset.tab === 'pedidos') cargarPedidos();
    });
});

let categoriasCache = [];

async function cargarCategorias() {
    const res = await fetch(`${API}/categorias/todas`);
    categoriasCache = await res.json();

    const select = document.getElementById('prod-categoriaId');
    select.innerHTML = '<option value="">Seleccionar categoria</option>';
    categoriasCache.forEach(c => {
        select.innerHTML += `<option value="${esc(c.id)}">${esc(c.nombre)}</option>`;
    });

    const lista = document.getElementById('lista-categorias');
    if (categoriasCache.length === 0) {
        lista.innerHTML = '<p>No hay categorias.</p>';
        return;
    }
    lista.innerHTML = categoriasCache.map(c => `
        <div class="admin-item">
            <div>
                <strong>${esc(c.nombre)}</strong>
                <span class="admin-item-desc">${esc(c.descripcion)}</span>
                <span class="admin-item-badge ${c.activa ? 'activa' : 'inactiva'}">${c.activa ? 'Activa' : 'Inactiva'}</span>
            </div>
            <div class="admin-item-acciones">
                <button data-action="editar-cat" data-id="${esc(c.id)}" data-nombre="${esc(c.nombre)}" data-desc="${esc(c.descripcion)}">Editar</button>
                <button data-action="eliminar-cat" data-id="${esc(c.id)}">Eliminar</button>
            </div>
        </div>
    `).join('');

    lista.querySelectorAll('[data-action="editar-cat"]').forEach(btn => {
        btn.addEventListener('click', () => editarCategoria(btn.dataset.id, btn.dataset.nombre, btn.dataset.desc));
    });
    lista.querySelectorAll('[data-action="eliminar-cat"]').forEach(btn => {
        btn.addEventListener('click', () => eliminarCategoria(btn.dataset.id));
    });
}

document.getElementById('form-categoria').addEventListener('submit', async (e) => {
    e.preventDefault();
    const id = document.getElementById('cat-id').value;
    const body = {
        nombre: document.getElementById('cat-nombre').value,
        descripcion: document.getElementById('cat-descripcion').value
    };

    if (id) {
        await fetch(`${API}/categorias/${id}`, {
            method: 'PUT',
            headers: { 'Content-Type': 'application/json' },
            body: JSON.stringify(body)
        });
    } else {
        await fetch(`${API}/categorias`, {
            method: 'POST',
            headers: { 'Content-Type': 'application/json' },
            body: JSON.stringify(body)
        });
    }

    cancelarCategoria();
    cargarCategorias();
});

function editarCategoria(id, nombre, descripcion) {
    document.getElementById('cat-id').value = id;
    document.getElementById('cat-nombre').value = nombre;
    document.getElementById('cat-descripcion').value = descripcion;
    document.getElementById('cat-cancelar').style.display = 'inline-block';
}

function cancelarCategoria() {
    document.getElementById('cat-id').value = '';
    document.getElementById('cat-nombre').value = '';
    document.getElementById('cat-descripcion').value = '';
    document.getElementById('cat-cancelar').style.display = 'none';
}

async function eliminarCategoria(id) {
    if (confirm('Eliminar esta categoria?')) {
        const res = await fetch(`${API}/categorias/${id}`, { method: 'DELETE' });
        if (!res.ok) {
            try {
                const data = await res.json();
                alert(data.error || 'Error al eliminar la categoria');
            } catch {
                alert('Error al eliminar la categoria');
            }
        }
        cargarCategorias();
    }
}

let productosCache = [];

async function cargarProductos() {
    const res = await fetch(`${API}/productos/todos`);
    productosCache = await res.json();

    const lista = document.getElementById('lista-productos');
    if (productosCache.length === 0) {
        lista.innerHTML = '<p>No hay productos.</p>';
        return;
    }
    lista.innerHTML = productosCache.map(p => `
        <div class="admin-item">
            <div style="display:flex;align-items:center;gap:0.75rem">
                <img src="${p.imagenUrl || 'https://placehold.co/50x50/eee/999?text=?'}"
                     alt="${esc(p.nombre)}"
                     style="width:50px;height:50px;object-fit:cover;border-radius:4px"
                     onerror="this.src='https://placehold.co/50x50/eee/999?text=?'">
                <div>
                    <strong>${esc(p.nombre)}</strong>
                    ${p.numeroSet ? `<span class="admin-item-desc">Set #${esc(p.numeroSet)}</span>` : ''}
                    <span class="admin-item-desc">$ ${Number(p.precio).toLocaleString('es-CO')} COP | Stock: ${p.stock}</span>
                </div>
            </div>
            <div class="admin-item-acciones">
                <button data-action="editar-prod" data-id="${esc(p.id)}">Editar</button>
                <button data-action="eliminar-prod" data-id="${esc(p.id)}">Eliminar</button>
            </div>
        </div>
    `).join('');

    lista.querySelectorAll('[data-action="editar-prod"]').forEach(btn => {
        btn.addEventListener('click', () => {
            const prod = productosCache.find(pr => pr.id === btn.dataset.id);
            if (prod) editarProducto(prod);
        });
    });
    lista.querySelectorAll('[data-action="eliminar-prod"]').forEach(btn => {
        btn.addEventListener('click', () => eliminarProducto(btn.dataset.id));
    });
}

document.getElementById('prod-archivo').addEventListener('change', async (e) => {
    const archivo = e.target.files[0];
    if (!archivo) return;
    const estado = document.getElementById('prod-imagen-estado');
    estado.textContent = 'Subiendo imagen...';
    estado.style.color = '#666';

    const formData = new FormData();
    formData.append('archivo', archivo);

    try {
        const res = await fetch(`${API}/imagenes`, { method: 'POST', body: formData });
        const data = await res.json();
        document.getElementById('prod-imagenUrl').value = data.url;
        estado.textContent = 'Imagen subida';
        estado.style.color = 'green';
    } catch (err) {
        estado.textContent = 'Error al subir';
        estado.style.color = 'red';
    }
});

document.getElementById('form-producto').addEventListener('submit', async (e) => {
    e.preventDefault();
    const id = document.getElementById('prod-id').value;
    const body = {
        nombre: document.getElementById('prod-nombre').value,
        numeroSet: document.getElementById('prod-numeroSet').value,
        descripcion: document.getElementById('prod-descripcion').value,
        precio: parseFloat(document.getElementById('prod-precio').value),
        stock: parseInt(document.getElementById('prod-stock').value),
        imagenUrl: document.getElementById('prod-imagenUrl').value,
        categoriaId: document.getElementById('prod-categoriaId').value
    };

    if (id) {
        await fetch(`${API}/productos/${id}`, {
            method: 'PUT',
            headers: { 'Content-Type': 'application/json' },
            body: JSON.stringify(body)
        });
    } else {
        await fetch(`${API}/productos`, {
            method: 'POST',
            headers: { 'Content-Type': 'application/json' },
            body: JSON.stringify(body)
        });
    }

    cancelarProducto();
    cargarProductos();
});

function editarProducto(p) {
    document.getElementById('prod-id').value = p.id;
    document.getElementById('prod-nombre').value = p.nombre;
    document.getElementById('prod-numeroSet').value = p.numeroSet || '';
    document.getElementById('prod-descripcion').value = p.descripcion || '';
    document.getElementById('prod-precio').value = p.precio;
    document.getElementById('prod-stock').value = p.stock;
    document.getElementById('prod-imagenUrl').value = p.imagenUrl || '';
    document.getElementById('prod-categoriaId').value = p.categoriaId;
    document.getElementById('prod-cancelar').style.display = 'inline-block';
}

function cancelarProducto() {
    document.getElementById('prod-id').value = '';
    document.getElementById('prod-nombre').value = '';
    document.getElementById('prod-numeroSet').value = '';
    document.getElementById('prod-descripcion').value = '';
    document.getElementById('prod-precio').value = '';
    document.getElementById('prod-stock').value = '';
    document.getElementById('prod-imagenUrl').value = '';
    document.getElementById('prod-categoriaId').value = '';
    document.getElementById('prod-cancelar').style.display = 'none';
}

async function eliminarProducto(id) {
    if (confirm('Eliminar este producto?')) {
        await fetch(`${API}/productos/${id}`, { method: 'DELETE' });
        cargarProductos();
    }
}

async function cargarPedidos() {
    const res = await fetch(`${API}/pedidos`);
    const pedidos = await res.json();
    const lista = document.getElementById('lista-pedidos');

    if (pedidos.length === 0) {
        lista.innerHTML = '<p>No hay pedidos.</p>';
        return;
    }

    lista.innerHTML = pedidos.map(p => `
        <div class="admin-item pedido-item">
            <div>
                <strong>Pedido ${esc(p.id.substring(0, 8))}...</strong>
                <span class="admin-item-desc">${esc(p.usuarioNombre)} | ${esc(p.metodoPago)} | ${new Date(p.fecha).toLocaleDateString('es-CO')}</span>
                <span class="admin-item-desc">Items: ${p.items.map(i => esc(i.productoNombre) + ' x' + i.cantidad).join(', ')}</span>
            </div>
            <div>
                <strong>$ ${Number(p.total).toLocaleString('es-CO')} COP</strong>
                <span class="admin-item-badge activa">${esc(p.estado)}</span>
            </div>
        </div>
    `).join('');
}

renderNav();
cargarCategorias();
cargarProductos();
