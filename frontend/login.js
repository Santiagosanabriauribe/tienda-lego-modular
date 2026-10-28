const API = '/api/v1';

document.getElementById('login-form').addEventListener('submit', async (e) => {
    e.preventDefault();
    const errorMsg = document.getElementById('error-msg');
    errorMsg.classList.add('hidden');

    const email = document.getElementById('email').value;
    const password = document.getElementById('password').value;

    try {
        const res = await fetch(`${API}/auth/login`, {
            method: 'POST',
            headers: { 'Content-Type': 'application/json' },
            body: JSON.stringify({ email, password })
        });

        const data = await res.json();

        if (!res.ok) {
            errorMsg.textContent = data.error || 'Error al iniciar sesion';
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
