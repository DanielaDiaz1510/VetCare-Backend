// =====================================================================
//  api.js - capa común del panel web para trabajar con JWT
//  1) Guarda el token en sessionStorage (se borra al cerrar la pestaña)
//  2) Reemplaza fetch() para que TODA llamada a /veterinaria/api lleve
//        Authorization: Bearer <token>
//  3) Si el servidor responde 401 (token vencido/revocado) vuelve al login
//  4) descargar(): baja archivos (Excel/PDF/backup) con el token, porque
//     window.open() no puede enviar cabeceras.
// =====================================================================
const TOKEN_KEY = 'vet_token';
const USUARIO_KEY = 'vet_usuario';

const Sesion = {
    guardar(token, usuario) {
        sessionStorage.setItem(TOKEN_KEY, token);
        sessionStorage.setItem(USUARIO_KEY, JSON.stringify(usuario || {}));
    },
    token() { return sessionStorage.getItem(TOKEN_KEY); },
    usuario() { try { return JSON.parse(sessionStorage.getItem(USUARIO_KEY)) || {}; } catch (e) { return {}; } },
    esAdmin() { return (Sesion.usuario().roles || []).includes('ADMIN'); },
    limpiar() { sessionStorage.removeItem(TOKEN_KEY); sessionStorage.removeItem(USUARIO_KEY); }
};

// Vuelve al login dejando escrito el motivo, para que index.html lo muestre (así no parece "un salto" sin explicación)
function irAlLogin(motivo) {
    Sesion.limpiar();
    try { sessionStorage.setItem('vet_motivo', motivo); } catch (e) { /* sin almacenamiento */ }
    window.location.href = 'index.html';
}

const fetchOriginal = window.fetch.bind(window);
window.fetch = function (url, opciones) {
    const ruta = typeof url === 'string' ? url : url.url;
    if (!ruta.startsWith('/veterinaria/api')) return fetchOriginal(url, opciones);

    opciones = opciones || {};
    const cabeceras = new Headers(opciones.headers || {});
    const token = Sesion.token();
    if (token) cabeceras.set('Authorization', 'Bearer ' + token);
    opciones.headers = cabeceras;

    return fetchOriginal(url, opciones).then(resp => {
        const esLogin = ruta.endsWith('/api/login');
        if (resp.status === 401 && !esLogin && !ruta.endsWith('/api/logout')) {
            resp.clone().json().catch(() => ({}))
                .then(j => irAlLogin('La sesión no es válida (' + ruta + ' respondió 401: ' + (j.error || 'sin detalle') + '). Inicie sesión de nuevo.'));
        }
        return resp;
    });
};

// Convierte texto del servidor en texto seguro para meter dentro de innerHTML
function esc(valor) {
    return String(valor ?? '').replace(/[&<>"']/g,
        c => ({ '&': '&amp;', '<': '&lt;', '>': '&gt;', '"': '&quot;', "'": '&#39;' }[c]));
}

// Mensaje legible a partir del JSON de error {status,codigo,error,ruta}
async function mensajeError(resp, porDefecto) {
    try { const j = await resp.json(); if (j && j.error) return j.error; } catch (e) { /* sin cuerpo */ }
    return porDefecto || ('Error ' + resp.status);
}

// Descarga un archivo protegido: fetch con token -> blob -> enlace temporal
async function descargar(ruta, nombreArchivo) {
    const resp = await fetch('/veterinaria/api' + ruta);
    if (!resp.ok) { alert(await mensajeError(resp, 'No se pudo descargar el archivo')); return; }
    const blob = await resp.blob();
    const enlace = document.createElement('a');
    enlace.href = URL.createObjectURL(blob);
    enlace.download = nombreArchivo;
    document.body.appendChild(enlace);
    enlace.click();
    enlace.remove();
    setTimeout(() => URL.revokeObjectURL(enlace.href), 10000);
}

// En el dashboard: sin token no se entra
if (window.location.pathname.endsWith('dashboard.html') && !Sesion.token()) {
    irAlLogin('No hay token guardado en este navegador. Inicie sesión de nuevo.');
}

// Un VETERINARIO no ve las opciones que el servidor le prohibiría (el servidor igual responde 403)
document.addEventListener('DOMContentLoaded', () => {
    if (!window.location.pathname.endsWith('dashboard.html') || Sesion.esAdmin()) return;
    document.querySelectorAll(
        '[onclick*="cargarUsuarios"],[onclick*="cargarPerfiles"],[onclick*="cargarAuditoria"],[onclick*="backupBD"]'
    ).forEach(el => (el.closest('li') || el).style.display = 'none');
});
