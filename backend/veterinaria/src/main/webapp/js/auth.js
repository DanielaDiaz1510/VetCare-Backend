const API = '/veterinaria/api';

// Si el panel nos devolvió aquí, mostrar por qué
(function () {
    const motivo = sessionStorage.getItem('vet_motivo');
    if (motivo) {
        sessionStorage.removeItem('vet_motivo');
        const e = document.getElementById('error');
        if (e) e.innerText = motivo;
    }
})();

// Manejador del formulario de login (RF-02)
document.getElementById('loginForm')?.addEventListener('submit', async e => {
    e.preventDefault();
    const error = document.getElementById('error');
    error.innerText = '';

    const username = document.getElementById('username').value;
    const password = document.getElementById('password').value;

    try {
        const resp = await fetch(`${API}/login`, {
            method: 'POST',
            headers: { 'Content-Type': 'application/json' },
            body: JSON.stringify({ username, password })
        });
        if (!resp.ok) { error.innerText = await mensajeError(resp, 'Credenciales inválidas'); return; }
        const datos = await resp.json();
        Sesion.guardar(datos.token, datos.usuario);   // el token viaja en cada petición posterior
        if (!Sesion.token()) { error.innerText = 'El navegador no permitió guardar la sesión (¿modo privado o almacenamiento bloqueado?)'; return; }
        window.location.href = 'dashboard.html';
    } catch (err) {
        error.innerText = 'No se pudo conectar con el servidor';
    }
});
