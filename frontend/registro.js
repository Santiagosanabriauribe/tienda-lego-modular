const API = '/api/v1';

document.getElementById('registro-form').addEventListener('submit', async (e) => {
    e.preventDefault();
    const errorMsg = document.getElementById('error-msg');
    errorMsg.classList.add('hidden');

    const nombre = document.getElementById('nombre').value;
    const email = document.getElementById('email').value;
    const password = document.getElementById('password').value;

    const emailRegex = /^[A-Za-z0-9._%+-]+@[A-Za-z0-9.-]+\.[A-Za-z]{2,}$/;
    if (!emailRegex.test(email)) {
        errorMsg.textContent = 'Ingresa un correo electronico valido';
        errorMsg.classList.remove('hidden');
        return;
    }

    if (password.length < 8) {
        errorMsg.textContent = 'La contraseña debe tener minimo 8 caracteres';
        errorMsg.classList.remove('hidden');
        return;
    }

    try {
        const res = await fetch(`${API}/auth/registro`, {
            method: 'POST',
            headers: { 'Content-Type': 'application/json' },
            body: JSON.stringify({ nombre, email, password })
        });

        const data = await res.json();

        if (!res.ok) {
            errorMsg.textContent = data.error || 'Error al registrarse';
            errorMsg.classList.remove('hidden');
            return;
        }

        localStorage.setItem('usuario', JSON.stringify(data));
        window.location.href = 'index.html';
    } catch (err) {
        errorMsg.textContent = 'Error de conexion con el servidor';
        errorMsg.classList.remove('hidden');
    }
});
