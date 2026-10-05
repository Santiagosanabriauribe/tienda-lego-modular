const API = 'http://localhost:8080/api/v1';

let categorias = [];
let productos = [];
let categoriaActiva = 'todas';

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

    if (productos.length === 0) {
        grid.innerHTML = '<p class="sin-productos">No hay productos disponibles.</p>';
        return;
    }

    grid.innerHTML = productos.map(p => `
        <div class="producto-card" data-id="${p.id}">
            <img src="${p.imagenUrl || 'https://placehold.co/400x300/fafafa/ccc?text=LEGO'}"
                 alt="${p.nombre}"
                 onerror="this.src='https://placehold.co/400x300/fafafa/ccc?text=LEGO'">
            <div class="producto-info">
                <span class="categoria-tag">${nombreCategoria(p.categoriaId)}</span>
                <h3>${p.nombre}</h3>
                ${p.numeroSet ? `<span class="set-number">Set #${p.numeroSet}</span>` : ''}
                <div class="precio-stock">
                    <span class="precio">$${formatoPrecio(p.precio)}</span>
                    <span class="stock ${p.stock === 0 ? 'agotado' : ''}">
                        ${p.stock > 0 ? `${p.stock} disponibles` : 'Agotado'}
                    </span>
                </div>
            </div>
        </div>
    `).join('');

    grid.querySelectorAll('.producto-card').forEach(card => {
        card.addEventListener('click', () => abrirDetalle(card.dataset.id));
    });
}

function formatoPrecio(precio) {
    return Number(precio).toLocaleString('es-CO', { minimumFractionDigits: 0 });
}

function abrirDetalle(id) {
    const p = productos.find(prod => prod.id === id);
    if (!p) return;

    const modal = document.getElementById('modal');
    const body = document.getElementById('modal-body');

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
                    <span class="precio">$${formatoPrecio(p.precio)}</span>
                    <span class="stock ${p.stock === 0 ? 'agotado' : ''}">
                        ${p.stock > 0 ? `${p.stock} unidades disponibles` : 'Agotado'}
                    </span>
                </div>
            </div>
        </div>
    `;

    modal.classList.remove('hidden');
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

cargarCategorias();
cargarProductos();
