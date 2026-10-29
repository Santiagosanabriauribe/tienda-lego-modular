const API = '/api/v1';

const rol = localStorage.getItem('rol');
if (!rol) {
    window.location.href = 'login.html';
}

const params = new URLSearchParams(window.location.search);
const tipo = params.get('tipo');
const carritoId = params.get('carritoId');
const productoId = params.get('productoId');
const cantidad = parseInt(params.get('cantidad') || '1');

document.querySelectorAll('input[name="metodo"]').forEach(radio => {
    radio.addEventListener('change', () => {
        document.getElementById('form-tarjeta').classList.add('hidden');
        document.getElementById('form-pse').classList.add('hidden');
        document.getElementById('form-efectivo').classList.add('hidden');

        const valor = document.querySelector('input[name="metodo"]:checked').value;
        if (valor === 'TARJETA') document.getElementById('form-tarjeta').classList.remove('hidden');
        if (valor === 'PSE') document.getElementById('form-pse').classList.remove('hidden');
        if (valor === 'EFECTIVO') document.getElementById('form-efectivo').classList.remove('hidden');
    });
});

async function cargarResumen() {
    const resumen = document.getElementById('resumen-compra');

    if (tipo === 'carrito') {
        try {
            const res = await fetch(`${API}/carritos/${carritoId}`);
            const carrito = await res.json();
            resumen.innerHTML = `
                <h3>Resumen del pedido</h3>
                ${carrito.items.map(i => `
                    <div class="resumen-item">
                        <span>${i.productoNombre} x${i.cantidad}</span>
                        <span>${formatoPrecio(i.subtotal)}</span>
                    </div>
                `).join('')}
                <div class="resumen-total">
                    <strong>Total:</strong>
                    <strong>${formatoPrecio(carrito.total)}</strong>
                </div>
            `;
        } catch (err) {
            resumen.innerHTML = '<p>Error al cargar el resumen</p>';
        }
    } else if (tipo === 'directo') {
        try {
            const res = await fetch(`${API}/productos/${productoId}`);
            const producto = await res.json();
            const subtotal = producto.precio * cantidad;
            resumen.innerHTML = `
                <h3>Resumen del pedido</h3>
                <div class="resumen-item">
                    <span>${producto.nombre} x${cantidad}</span>
                    <span>${formatoPrecio(subtotal)}</span>
                </div>
                <div class="resumen-total">
                    <strong>Total:</strong>
                    <strong>${formatoPrecio(subtotal)}</strong>
                </div>
            `;
        } catch (err) {
            resumen.innerHTML = '<p>Error al cargar el resumen</p>';
        }
    }
}

async function procesarPago() {
    const errorMsg = document.getElementById('error-msg');
    errorMsg.classList.add('hidden');

    const metodoPago = document.querySelector('input[name="metodo"]:checked').value;

    const nombreDest = document.getElementById('nombre-dest').value.trim();
    const direccion = document.getElementById('direccion').value.trim();
    const ciudad = document.getElementById('ciudad').value.trim();
    const departamento = document.getElementById('departamento').value.trim();
    const telefono = document.getElementById('telefono').value.trim();

    if (!nombreDest || !direccion || !ciudad || !departamento || !telefono) {
        errorMsg.textContent = 'Completa todos los datos de envio';
        errorMsg.classList.remove('hidden');
        return;
    }

    if (metodoPago === 'TARJETA') {
        const numero = document.getElementById('numero-tarjeta').value;
        const vencimiento = document.getElementById('vencimiento').value;
        const cvv = document.getElementById('cvv').value;
        const titular = document.getElementById('titular').value;

        if (!numero || !vencimiento || !cvv || !titular) {
            errorMsg.textContent = 'Completa todos los datos de la tarjeta';
            errorMsg.classList.remove('hidden');
            return;
        }
    }

    if (metodoPago === 'PSE') {
        const banco = document.getElementById('banco').value;
        if (!banco) {
            errorMsg.textContent = 'Selecciona un banco';
            errorMsg.classList.remove('hidden');
            return;
        }
    }

    const btn = document.getElementById('btn-pagar');
    btn.disabled = true;
    btn.textContent = 'Procesando...';

    try {
        let url, body;

        const datosEnvio = {
            direccionEnvio: direccion,
            ciudadEnvio: ciudad,
            departamentoEnvio: departamento,
            telefonoEnvio: telefono,
            nombreDestinatario: nombreDest
        };

        if (tipo === 'carrito') {
            url = `${API}/pedidos/carrito`;
            body = { carritoId, metodoPago, ...datosEnvio };
        } else {
            url = `${API}/pedidos/directo`;
            body = { productoId, cantidad: String(cantidad), metodoPago, ...datosEnvio };
        }

        const res = await fetch(url, {
            method: 'POST',
            headers: { 'Content-Type': 'application/json' },
            body: JSON.stringify(body)
        });

        const data = await res.json();

        if (!res.ok) {
            errorMsg.textContent = data.error || 'Error al procesar el pago';
            errorMsg.classList.remove('hidden');
            btn.disabled = false;
            btn.textContent = 'Confirmar Pago';
            return;
        }

        if (tipo === 'carrito') {
            localStorage.removeItem('carritoId');
        }

        document.querySelector('.pago-container').classList.add('hidden');
        const confirmacion = document.getElementById('confirmacion');
        confirmacion.classList.remove('hidden');
        document.getElementById('detalle-pedido').innerHTML = `
            <p><strong>Pedido:</strong> ${data.id}</p>
            <p><strong>Metodo:</strong> ${data.metodoPago}</p>
            <p><strong>Total:</strong> ${formatoPrecio(data.total)}</p>
            <p><strong>Estado:</strong> ${data.estado}</p>
        `;
    } catch (err) {
        errorMsg.textContent = 'Error de conexion con el servidor';
        errorMsg.classList.remove('hidden');
        btn.disabled = false;
        btn.textContent = 'Confirmar Pago';
    }
}

function formatoPrecio(precio) {
    return '$ ' + Number(precio).toLocaleString('es-CO') + ' COP';
}

cargarResumen();
