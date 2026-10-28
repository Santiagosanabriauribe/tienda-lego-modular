const API = '/api/v1';

const emailRegex = /^[A-Za-z0-9._%+-]+@[A-Za-z0-9.-]+\.[A-Za-z]{2,}$/;

const emailInput = document.getElementById('email');
const passwordInput = document.getElementById('password');
const emailError = document.getElementById('email-error');
const passwordError = document.getElementById('password-error');

emailInput.addEventListener('input', () => {
    if (emailInput.value && !emailRegex.test(emailInput.value)) {
        emailError.classList.remove('hidden');
    } else {
        emailError.classList.add('hidden');
    }
});

passwordInput.addEventListener('input', () => {
    if (passwordInput.value && passwordInput.value.length < 8) {
        passwordError.classList.remove('hidden');
    } else {
        passwordError.classList.add('hidden');
    }
});

document.getElementById('registro-form').addEventListener('submit', async (e) => {
    e.preventDefault();
    const errorMsg = document.getElementById('error-msg');
    errorMsg.classList.add('hidden');

    const nombre = document.getElementById('nombre').value;
    const email = emailInput.value;
    const password = passwordInput.value;

    if (!emailRegex.test(email)) {
        emailError.classList.remove('hidden');
        return;
    }

    if (password.length < 8) {
        passwordError.classList.remove('hidden');
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
