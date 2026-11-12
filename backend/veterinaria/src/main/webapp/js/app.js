const API = '/veterinaria/api';

// Logout (RF-12): el servidor revoca el token y el navegador lo olvida
function logout() {
    fetch('/veterinaria/api/logout', { method: 'POST' })
        .catch(error => console.error('Error durante el cierre de sesión:', error))
        .finally(() => {
            Sesion.limpiar();
            window.location.href = 'index.html';
        });
}

// USUARIOS
// Cargar la lista de Usuarios
function cargarUsuarios() {
    document.getElementById('titulo').innerText = 'Usuarios';

    fetch(`/veterinaria/api/usuarios`)
        .then(r => r.json())
        .then(data => {

            let html = `
            <button class="btn btn-success mb-3" onclick="mostrarFormularioUsuario()">Nuevo Usuario</button>

            <table class="table table-striped table-bordered">
                <thead>
                    <tr>
                        <th>Username</th>
                        <th>Nombre</th>
                        <th>Correo</th>
                        <th>Estado</th>
                        <th>Roles</th>
                        <th>Acciones</th>
                    </tr>
                </thead>
                <tbody>`;

            data.forEach(u => {
                html += `
                <tr>
                    <td>${esc(u.username)}</td>
                    <td>${esc(u.nombre)}</td>
                    <td>${esc(u.email)}</td>
                    <td>${u.estado ? 'Activo' : 'Inactivo'}</td>
                    <td>${esc((u.roles || []).join(', '))}</td>
                    <td>
                        <button class="btn btn-sm btn-warning" onclick='editarUsuario(${JSON.stringify(u)})'>Editar</button>
                    </td>
                </tr>`;
            });

            html += `</tbody></table>`;
            document.getElementById('contenido').innerHTML = html;
        });
}

//Mostrar el formmulario de usuarios
function mostrarFormularioUsuario() {
    const template = document.getElementById('formUsuarioTemplate');
    const clone = template.content.cloneNode(true);
    document.getElementById('contenido').innerHTML = '';
    document.getElementById('contenido').appendChild(clone);
}

//Guardar o crear un Usuario
function guardarUsuario(e) {
    e.preventDefault();

    const idUsuario = document.getElementById('idUsuario').value;

    const usuario = {
        idUsuario: idUsuario ? Number(idUsuario) : null,
        username: document.getElementById('username').value,
        nombre: document.getElementById('nombre').value,
        email: document.getElementById('email').value,
        estado: document.getElementById('estado').value === 'true',
        password: document.getElementById('password').value || undefined,
        roles: Array.from(document.getElementById('rolesUsuario').selectedOptions).map(o => o.value)
    };

    fetch(`${API}/usuarios`, {
        method: idUsuario ? 'PUT' : 'POST',
        headers: { 'Content-Type': 'application/json' },
        body: JSON.stringify(usuario)
    }).then(async r => {
        if (!r.ok) {
            alert(await mensajeError(r, 'No se pudo guardar el usuario'));
            return;
        }
        cargarUsuarios();
    });
}

//Editar un usuario
function editarUsuario(u) {
    mostrarFormularioUsuario();

    document.getElementById('idUsuario').value = u.idUsuario;
    document.getElementById('username').value = u.username;
    document.getElementById('nombre').value = u.nombre;
    document.getElementById('email').value = u.email;
    document.getElementById('estado').value = u.estado;
    Array.from(document.getElementById('rolesUsuario').options).forEach(o => o.selected = (u.roles || []).includes(o.value));
}

// PERFIL O ROLES
// Cargar la lista de Perfiles
function cargarPerfiles() {
    document.getElementById('titulo').innerText = 'Perfiles';

    fetch(`${API}/roles`)
        .then(r => r.json())
        .then(data => {

            let html = `
            <button class="btn btn-success mb-3" onclick="mostrarFormularioPerfil()">Nuevo Perfil</button>

            <table class="table table-striped table-bordered">
                <thead>
                    <tr>
                        <th>Nombre</th>
                        <th>Descripción</th>
                        <th>Acciones</th>
                    </tr>
                </thead>
                <tbody>`;

            data.forEach(r => {
                html += `
                <tr>
                    <td>${r.nombre}</td>
                    <td>${r.descripcion}</td>
                    <td>
                        <button class="btn btn-sm btn-warning" onclick='editarPerfil(${JSON.stringify(r)})'>Editar</button>
                    </td>
                </tr>`;
            });

            html += `</tbody></table>`;
            document.getElementById('contenido').innerHTML = html;
        });
}

// Mostrar el formulario de perfiles (para crear o editar)
function mostrarFormularioPerfil() {
    const template = document.getElementById('formRolTemplate');
    const clone = template.content.cloneNode(true);
    document.getElementById('contenido').innerHTML = '';
    document.getElementById('contenido').appendChild(clone);
}

// Guardar o crear un rol o perfil
function guardarRol(e) {
    e.preventDefault();

    const idRol = document.getElementById('idRol').value;

    const rol = {
        idRol: idRol ? Number(idRol) : null,
        nombre: document.getElementById('nombreRol').value,
        descripcion: document.getElementById('descripcionRol').value
    };

    fetch(`${API}/roles`, {
        method: idRol ? 'PUT' : 'POST',
        headers: { 'Content-Type': 'application/json' },
        body: JSON.stringify(rol)
    }).then(r => {
        if (!r.ok) {
            alert('No se pudo guardar el perfil');
            return;
        }
        cargarPerfiles();
    });
}

// Editar un rol o perfil
function editarPerfil(r) {
    mostrarFormularioPerfil();

    document.getElementById('idRol').value = r.idRol;
    document.getElementById('nombreRol').value = r.nombre;
    document.getElementById('descripcionRol').value = r.descripcion;
}

// MASCOTAS
// Cargar la lista de Mascotas
function cargarMascotas() {
    document.getElementById('titulo').innerText = 'Mascotas';

    fetch(`${API}/mascotas`)
        .then(r => {
            if (r.status === 401) window.location.href = 'index.html';
            return r.json();
        })
        .then(data => {

            let html = `
            <button class="btn btn-success mb-3" onclick="mostrarFormularioMascota()">Nueva Mascota</button>

            <table class="table table-striped table-bordered">
                <thead>
                    <tr>
                        <th>Nombre</th>
                        <th>Especie</th>
                        <th>Raza</th>
                        <th>Propietario</th>
                        <th>Acciones</th>
                    </tr>
                </thead>
                <tbody>`;

            data.forEach(m => {
                html += `
                <tr>
                    <td>${m.nombre}</td>
                    <td>${m.especie}</td>
                    <td>${m.raza || ''}</td>
                    <td>${m.nombrePropietario || ''}</td>
                    <td>
                        <button class="btn btn-sm btn-warning" onclick='editarMascota(${JSON.stringify(m)})'>Editar</button>
                        <button class="btn btn-sm btn-danger" onclick='eliminarMascota(${m.id})'>Eliminar</button>
                    </td>
                </tr>`;
            });

            html += `</tbody></table>`;
            document.getElementById('contenido').innerHTML = html;
        });
}

// Mostrar el formulario de mascotas (para crear o editar)
function mostrarFormularioMascota() {
    const template = document.getElementById('formMascotaTemplate');

    if (!template) {
        console.error('Template formMascotaTemplate no encontrado');
        return;
    }

    const clone = template.content.cloneNode(true);

    const contenido = document.getElementById('contenido');
    contenido.innerHTML = '';
    contenido.appendChild(clone);

    // Devuelve la promesa para poder esperar a que el select termine de cargar
    return cargarSelectPropietarios();
}

// Cargar select de propietarios
function cargarSelectPropietarios() {

    return fetch('/veterinaria/api/propietarios')
        .then(r => r.json())
        .then(data => {

            const select = document.getElementById('idPropietario');

            if (!select) {
                console.error('Select idPropietario no existe en el DOM');
                return;
            }

            select.innerHTML = '<option value="">Seleccione propietario</option>';

            data.forEach(p => {
                const option = document.createElement('option');
                option.value = p.idPropietario || p.id;
                option.textContent = `${p.nombres} ${p.apellidos}`;
                select.appendChild(option);
            });
        });
}

// Guardar o crear una mascota
function guardarMascota(e) {
    e.preventDefault();

    const idMascota = document.getElementById('idMascota').value;
    const idProp = document.getElementById('idPropietario').value;

    if (!idProp) {
        alert('Debe seleccionar un propietario');
        return;
    }

    const mascota = {
        idMascota: idMascota ? Number(idMascota) : null,
        nombre: document.getElementById('nombre').value,
        especie: document.getElementById('especie').value,
        raza: document.getElementById('raza').value,
        sexo: document.getElementById('sexo').value,
        fechaNacimiento: document.getElementById('fechaNacimiento').value || null,
        color: document.getElementById('color').value,
        pesoActual: document.getElementById('pesoActual').value
            ? Number(document.getElementById('pesoActual').value)
            : null,
        propietario: { idPropietario: Number(idProp) }
    };

    const metodo = idMascota ? 'PUT' : 'POST';

    fetch('/veterinaria/api/mascotas', {
        method: metodo,
        headers: { 'Content-Type': 'application/json' },
        body: JSON.stringify(mascota)
    })
    .then(response => {
        if (!response.ok) {
            throw new Error('Error HTTP: ' + response.status);
        }
        return response;
    })
    .then(() => {

        alert(
            idMascota
                ? 'Mascota actualizada correctamente'
                : 'Mascota registrada correctamente'
        );

        cargarMascotas();
        cargarEstadisticas();
    })
    .catch(error => {
        console.error('Error:', error);
        alert('Ocurrió un error al guardar la mascota');
    });
}

// Editar una mascota
function editarMascota(m) {

    mostrarFormularioMascota().then(() => {

        document.getElementById('idMascota').value = m.id;
        document.getElementById('nombre').value = m.nombre || '';
        document.getElementById('especie').value = m.especie || '';
        document.getElementById('raza').value = m.raza || '';
        // El DTO trae "genero"; la entidad usa "sexo"
        document.getElementById('sexo').value = m.sexo || m.genero || 'M';
        document.getElementById('fechaNacimiento').value = m.fechaNacimiento || '';
        document.getElementById('color').value = m.color || '';
        document.getElementById('pesoActual').value = m.pesoActual || '';

        // El select de propietarios ya está cargado: se puede seleccionar el dueño
        if (m.idPropietario) {
            document.getElementById('idPropietario').value = m.idPropietario;
        }
    });
}

// Eliminar una mascota
function eliminarMascota(id) {
    if (!confirm('¿Está seguro de que desea eliminar esta mascota?')) {
        return;
    }

    fetch(`/veterinaria/api/mascotas?id=${id}`, {
        method: 'DELETE'
    }).then(r => {
        if (!r.ok) alert('No se pudo eliminar la mascota (puede tener historial clínico asociado)');
        cargarMascotas();
        cargarEstadisticas();
    });
}

// PROPIETARIOS
// Cargar la lista de Propietarios
function cargarPropietarios() {
    document.getElementById('titulo').innerText = 'Propietarios';

    fetch(`${API}/propietarios`)
        .then(r => r.json())
        .then(data => {

            let html = `
            <button class="btn btn-success mb-3" onclick="mostrarFormularioPropietario()">Nuevo Propietario</button>

            <table class="table table-striped table-bordered">
                <thead>
                    <tr>
                        <th>Nombre Completo</th>
                        <th>Documento</th>
                        <th>Teléfono</th>
                        <th>Email</th>
                        <th>Acciones</th>
                    </tr>
                </thead>
                <tbody>`;

            data.forEach(p => {
                html += `
                <tr>
                    <td>${p.nombres} ${p.apellidos}</td>
                    <td>${p.numeroDocumento}</td>
                    <td>${p.telefono || ''}</td>
                    <td>${p.email || ''}</td>
                    <td>
                        <button class="btn btn-sm btn-warning" onclick='editarPropietario(${JSON.stringify(p)})'>Editar</button>
                        <button class="btn btn-sm btn-danger" onclick='eliminarPropietario(${p.id})'>Eliminar</button>
                    </td>
                </tr>`;
            });

            html += `</tbody></table>`;
            document.getElementById('contenido').innerHTML = html;
        });
}

// Mostrar el formulario de propietario (para crear o editar)
function mostrarFormularioPropietario() {
    const template = document.getElementById('formPropietarioTemplate');

    if (!template) {
        console.error('Template formPropietarioTemplate no encontrado');
        return;
    }

    const clone = template.content.cloneNode(true);
    document.getElementById('contenido').innerHTML = '';
    document.getElementById('contenido').appendChild(clone);
}

// Guardar o crear un Propietario
function guardarPropietario(e) {
    e.preventDefault();

    const idProp = document.getElementById('idPropietario').value;

    const propietario = {
        idPropietario: idProp ? Number(idProp) : null,
        tipoDocumento: document.getElementById('tipoDocumento').value,
        numeroDocumento: document.getElementById('numeroDocumento').value,
        nombres: document.getElementById('nombres').value,
        apellidos: document.getElementById('apellidos').value,
        telefono: document.getElementById('telefono').value,
        email: document.getElementById('email').value,
        direccion: document.getElementById('direccion').value
    };

    const metodo = idProp ? 'PUT' : 'POST';

    fetch(`${API}/propietarios`, {
        method: metodo,
        headers: { 'Content-Type': 'application/json' },
        body: JSON.stringify(propietario)
    }).then(r => {
        if (!r.ok) {
            alert('Ocurrió un error al guardar el propietario');
            return;
        }
        alert(idProp ? 'Propietario actualizado' : 'Propietario registrado');
        cargarPropietarios();
        cargarEstadisticas();
    });
}

// Editar un propietario
function editarPropietario(p) {
    mostrarFormularioPropietario();

    document.getElementById('idPropietario').value = p.idPropietario || p.id;
    document.getElementById('tipoDocumento').value = p.tipoDocumento;
    document.getElementById('numeroDocumento').value = p.numeroDocumento;
    document.getElementById('nombres').value = p.nombres;
    document.getElementById('apellidos').value = p.apellidos;
    document.getElementById('telefono').value = p.telefono;
    document.getElementById('email').value = p.email;
    document.getElementById('direccion').value = p.direccion;
}

// Eliminar un propietario
function eliminarPropietario(id) {
    if (!confirm('¿Desea eliminar este propietario? También se eliminarán sus mascotas.')) {
        return;
    }

    fetch(`${API}/propietarios?id=${id}`, {
        method: 'DELETE'
    }).then(r => {
        if (!r.ok) alert('No se pudo eliminar el propietario (puede tener datos asociados)');
        cargarPropietarios();
        cargarEstadisticas();
    });
}

// VETERINARIOS
// Cargar la lista de Veterinarios
function cargarVeterinarios() {
    document.getElementById('titulo').innerText = 'Veterinarios';

    fetch(`${API}/veterinarios`)
        .then(r => r.json())
        .then(data => {

            let html = `
            <button class="btn btn-success mb-3" onclick="mostrarFormularioVeterinario()">Nuevo Veterinario</button>

            <table class="table table-striped table-bordered">
                <thead>
                    <tr>
                        <th>Nombre Completo</th>
                        <th>Especialidad</th>
                        <th>Teléfono</th>
                        <th>Estado</th>
                        <th>Acciones</th>
                    </tr>
                </thead>
                <tbody>`;

            data.forEach(v => {
                html += `
                <tr>
                    <td>${v.nombres} ${v.apellidos}</td>
                    <td>${v.especialidad}</td>
                    <td>${v.telefono || ''}</td>
                    <td>${v.estado || ''}</td>
                    <td>
                        <button class="btn btn-sm btn-warning" onclick='editarVeterinario(${JSON.stringify(v)})'>Editar</button>
                        <button class="btn btn-sm btn-danger" onclick='eliminarVeterinario(${v.id})'>Eliminar</button>
                    </td>
                </tr>`;
            });

            html += `</tbody></table>`;
            document.getElementById('contenido').innerHTML = html;
        });
}

// Mostrar el formulario de veterinarios (para crear o editar)
function mostrarFormularioVeterinario() {
    const template = document.getElementById('formVeterinarioTemplate');
    const clone = template.content.cloneNode(true);
    document.getElementById('contenido').innerHTML = '';
    document.getElementById('contenido').appendChild(clone);
}

// Guardar o crear un Veterinario
function guardarVeterinario(e) {
    e.preventDefault();

    const idVet = document.getElementById('idVeterinario').value;

    const veterinario = {
        idVeterinario: idVet ? Number(idVet) : null,
        tipoDocumento: document.getElementById('tipoDocumento').value,
        numeroDocumento: document.getElementById('numeroDocumento').value,
        nombres: document.getElementById('nombres').value,
        apellidos: document.getElementById('apellidos').value,
        especialidad: document.getElementById('especialidad').value,
        telefono: document.getElementById('telefono').value,
        email: document.getElementById('email').value,
        estado: document.getElementById('estado').value
    };

    const metodo = idVet ? 'PUT' : 'POST';

    fetch(`${API}/veterinarios`, {
        method: metodo,
        headers: { 'Content-Type': 'application/json' },
        body: JSON.stringify(veterinario)
    }).then(r => {
        if (!r.ok) {
            alert('Ocurrió un error al guardar el veterinario');
            return;
        }
        alert(idVet ? 'Veterinario actualizado' : 'Veterinario registrado');
        cargarVeterinarios();
        cargarEstadisticas();
    });
}

// Editar un Veterinario
function editarVeterinario(v) {
    mostrarFormularioVeterinario();

    document.getElementById('idVeterinario').value = v.id;
    document.getElementById('tipoDocumento').value = v.tipoDocumento;
    document.getElementById('numeroDocumento').value = v.numeroDocumento;
    document.getElementById('nombres').value = v.nombres;
    document.getElementById('apellidos').value = v.apellidos;
    document.getElementById('especialidad').value = v.especialidad;
    document.getElementById('telefono').value = v.telefono;
    document.getElementById('email').value = v.email;
    document.getElementById('estado').value = v.estado;
}

// Cancelar el formulario de veterinarios
function cancelarFormulario() {
    document.getElementById('contenido').innerHTML = '';
}

// Eliminar un Veterinario
function eliminarVeterinario(id) {
    if (!confirm('¿Está seguro de que desea eliminar este veterinario?')) {
        return;
    }

    fetch(`/veterinaria/api/veterinarios?id=${id}`, {
        method: 'DELETE'
    }).then(r => {
        if (!r.ok) alert('No se pudo eliminar el veterinario (puede tener historiales asociados)');
        cargarVeterinarios();
        cargarEstadisticas();
    });
}

// Función para cargar los datos estadísticos
function cargarEstadisticas() {
    fetch('/veterinaria/api/estadisticas')
        .then(r => r.json())
        .then(data => {
            document.getElementById('mascotas-count').textContent = data.mascotas;
            document.getElementById('propietarios-count').textContent = data.propietarios;
            document.getElementById('veterinarios-count').textContent = data.veterinarios;
        })
        .catch(err => {
            console.error('Error al cargar las estadísticas:', err);
        });
}

// Copia de seguridad
function backupBD() {
    if (!confirm("¿Desea generar la copia de seguridad de la base de datos?")) {
        return;
    }

    descargar("/backup", "backup_veterinaria.sql");
}

function exportarMascotasExcel() {
    descargar("/export/mascotas/excel", "mascotas.xlsx");
}

function exportarMascotasPDF() {
    descargar("/export/mascotas/pdf", "mascotas.pdf");
}

function exportarPropietariosExcel() {
    descargar("/export/propietarios/excel", "propietarios.xlsx");
}

function exportarPropietariosPDF() {
    descargar("/export/propietarios/pdf", "propietarios.pdf");
}

function exportarVeterinariosExcel() {
    descargar("/export/veterinarios/excel", "veterinarios.xlsx");
}

function exportarVeterinariosPDF() {
    descargar("/export/veterinarios/pdf", "veterinarios.pdf");
}

// AUDITORÍA
// Registros de auditoría que se están mostrando (se guardan para poder filtrar sin volver al servidor)
let auditoriaDatos = [];

// Escapa texto antes de meterlo en el HTML (evita que un nombre con <etiquetas> rompa la página)
function escaparHtml(texto) {
    return String(texto ?? '')
        .replace(/&/g, '&amp;')
        .replace(/</g, '&lt;')
        .replace(/>/g, '&gt;')
        .replace(/"/g, '&quot;')
        .replace(/'/g, '&#39;');
}

// Cargar la lista de Auditoría (quién creó, actualizó o eliminó datos)
function cargarAuditoria() {
    document.getElementById('titulo').innerText = 'Auditoría';

    fetch(`${API}/auditoria`)
        .then(r => {
            if (r.status === 401) {
                window.location.href = 'index.html';
                throw new Error('Sesión expirada');
            }
            if (!r.ok) throw new Error('Error HTTP: ' + r.status);
            return r.json();
        })
        .then(data => {
            auditoriaDatos = data;

            document.getElementById('contenido').innerHTML = `
            <div class="auditoria-toolbar">
                <div class="d-flex align-items-center gap-2">
                    <label class="form-label mb-0 fw-semibold" for="filtroAccion">Acción:</label>
                    <select id="filtroAccion" class="form-select form-select-sm w-auto" onchange="pintarAuditoria()">
                        <option value="">Todas</option>
                        <option value="CREAR">Crear</option>
                        <option value="ACTUALIZAR">Actualizar</option>
                        <option value="ELIMINAR">Eliminar</option>
                    </select>
                </div>
                <div class="d-flex align-items-center gap-2">
                    <span class="auditoria-total" id="auditoriaTotal"></span>
                    <button class="btn btn-sm btn-outline-primary" onclick="cargarAuditoria()">
                        <i class="bi bi-arrow-clockwise"></i> Actualizar
                    </button>
                </div>
            </div>

            <div class="table-responsive">
                <table class="table table-striped">
                    <thead>
                        <tr>
                            <th>Fecha y hora</th>
                            <th>Usuario</th>
                            <th>Acción</th>
                            <th>Entidad</th>
                            <th>ID registro</th>
                            <th>Descripción</th>
                        </tr>
                    </thead>
                    <tbody id="auditoriaCuerpo"></tbody>
                </table>
            </div>`;

            pintarAuditoria();
        })
        .catch(error => {
            console.error('Error al cargar la auditoría:', error);
            document.getElementById('contenido').innerHTML =
                '<div class="alert alert-danger">No se pudo cargar la auditoría.</div>';
        });
}

// Dibuja las filas de la tabla de auditoría aplicando el filtro de acción
function pintarAuditoria() {
    const filtro = document.getElementById('filtroAccion').value;
    const filas = filtro
        ? auditoriaDatos.filter(a => a.accion === filtro)
        : auditoriaDatos;

    let html = '';

    filas.forEach(a => {
        const fecha = a.fechaHora
            ? new Date(a.fechaHora).toLocaleString('es-CO', { dateStyle: 'medium', timeStyle: 'short' })
            : '';
        const clase = 'accion-' + String(a.accion || '').toLowerCase();

        html += `
        <tr>
            <td class="text-nowrap">${escaparHtml(fecha)}</td>
            <td><i class="bi bi-person-circle text-primary me-1"></i>${escaparHtml(a.username)}</td>
            <td><span class="badge badge-accion ${escaparHtml(clase)}">${escaparHtml(a.accion)}</span></td>
            <td>${escaparHtml(a.entidad)}</td>
            <td>${escaparHtml(a.idRegistro)}</td>
            <td>${escaparHtml(a.descripcion)}</td>
        </tr>`;
    });

    if (!html) {
        html = '<tr><td colspan="6" class="text-center text-muted py-4">No hay registros de auditoría.</td></tr>';
    }

    document.getElementById('auditoriaCuerpo').innerHTML = html;
    document.getElementById('auditoriaTotal').textContent =
        filas.length + (filas.length === 1 ? ' registro' : ' registros');
}

// Marca en el menú la sección que está abierta
document.addEventListener('DOMContentLoaded', () => {
    document.querySelectorAll('#menu .nav-link[data-seccion]').forEach(enlace => {
        enlace.addEventListener('click', () => {
            document.querySelectorAll('#menu .nav-link[data-seccion]')
                .forEach(l => l.classList.remove('active'));
            enlace.classList.add('active');
        });
    });
});

// Llamar a la función al cargar la página del dashboard
document.addEventListener('DOMContentLoaded', cargarEstadisticas);