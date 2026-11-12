#!/bin/bash
# =============================================================================
#  PRUEBAS DE INTEGRACIÓN DE LA API  (JWT, roles, reglas de negocio)
#
#  Uso:   ./pruebas_api.sh [URL_BASE]
#  Ej.:   ./pruebas_api.sh http://localhost:8080/veterinaria
#
#  Requisitos: bash y curl (en Windows sirven "Git Bash" o WSL).
#  La API debe estar corriendo con los usuarios de demostración  admin/admin123  y  veterinario/vet12345.
#  Cada prueba imprime PASA o FALLA con el código HTTP esperado y el recibido.
#  Las pruebas crean datos nuevos con un sufijo único, así que se pueden repetir.
# =============================================================================
BASE="${1:-http://localhost:8080/veterinaria}"
API="$BASE/api"
SUF="$(date +%s)$RANDOM"
OK=0; FAIL=0; STATUS=""; BODY=""
TMP="$(mktemp)"; trap 'rm -f "$TMP"' EXIT

# req MÉTODO RUTA TOKEN [CUERPO]  -> deja STATUS y BODY
req() {
  local m="$1" p="$2" t="$3" d="$4"
  local args=(-s -o "$TMP" -w '%{http_code}' --path-as-is -X "$m")
  [ -n "$t" ] && args+=(-H "Authorization: Bearer $t")
  [ -n "$d" ] && args+=(-H 'Content-Type: application/json' -d "$d")
  STATUS=$(curl "${args[@]}" "$API$p"); BODY=$(cat "$TMP")
}
# check "descripción" CÓDIGO_ESPERADO [TEXTO_QUE_DEBE_APARECER]
check() {
  local desc="$1" esp="$2" txt="$3" ok=1
  [ "$STATUS" = "$esp" ] || ok=0
  if [ -n "$txt" ] && ! echo "$BODY" | grep -q -- "$txt"; then ok=0; fi
  if [ $ok = 1 ]; then OK=$((OK+1)); printf '  \033[32mPASA \033[0m %-78s [%s]\n' "$desc" "$STATUS"
  else FAIL=$((FAIL+1)); printf '  \033[31mFALLA\033[0m %-78s [esperado %s, recibido %s] %s\n' "$desc" "$esp" "$STATUS" "$(echo "$BODY" | cut -c1-140)"; fi
}
campo() { echo "$BODY" | grep -o "\"$1\":[^,}]*" | head -1 | sed 's/^[^:]*://; s/"//g'; }
login() { req POST /login "" "{\"username\":\"$1\",\"password\":\"$2\"}"; }
titulo() { printf '\n\033[1m== %s ==\033[0m\n' "$1"; }
fut() { date -d "+$1 days $2" '+%Y-%m-%dT%H:%M:%S' 2>/dev/null || date -v+"$1"d '+%Y-%m-%dT'"$2"; }

# ------------------------------------------------------------------------------
titulo "1. AUTENTICACIÓN Y JWT"
req GET /mascotas "";                                   check "Sin token -> 401 NO_AUTENTICADO" 401 NO_AUTENTICADO
req GET /mascotas "token.basura.xyz";                   check "Token basura -> 401 TOKEN_INVALIDO" 401 TOKEN_INVALIDO
login admin mala;                                       check "Contraseña incorrecta -> 401" 401 CREDENCIALES_INVALIDAS
MSG1="$BODY"
login noexiste_$SUF cualquiera1;                        check "Usuario inexistente -> 401" 401 CREDENCIALES_INVALIDAS
[ "$(echo "$MSG1" | grep -o '"error":"[^"]*"')" = "$(echo "$BODY" | grep -o '"error":"[^"]*"')" ] && { OK=$((OK+1)); echo "  PASA  Mismo mensaje para usuario inexistente y contraseña mala (no revela cuentas)"; } || { FAIL=$((FAIL+1)); echo "  FALLA Los mensajes difieren: se podrían enumerar usuarios"; }
req POST /login "" '{"username":"","password":""}';     check "Login con campos vacíos -> 400" 400
req POST /login "" '{esto no es json';                  check "Login con JSON roto -> 400" 400 JSON_INVALIDO
login admin admin123;                                   check "Login ADMIN por usuario -> 200 con token" 200 '"token"'
TA=$(campo token)
login admin@veterinaria.com admin123;                   check "Login ADMIN por correo -> 200" 200 '"token"'
login veterinario vet12345;                             check "Login VETERINARIO -> 200 con token" 200 '"token"'
TV=$(campo token)
TAMP="${TA%?}$([ "${TA: -1}" = "A" ] && echo B || echo A)"
req GET /mascotas "$TAMP";                              check "Token con la firma alterada -> 401" 401 TOKEN_INVALIDO
req GET /me "$TA";                                      check "GET /me (ADMIN) -> 200 con rol ADMIN" 200 '"ADMIN"'
req GET /me "$TV";                                      check "GET /me (VET) -> 200 con idVeterinario" 200 idVeterinario
IDVET=$(campo idVeterinario)

# ------------------------------------------------------------------------------
titulo "2. AUTORIZACIÓN POR ROLES (403)"
for r in usuarios roles auditoria backup; do req GET /$r "$TV"; check "VETERINARIO GET /$r -> 403" 403 ACCESO_DENEGADO; done
req POST /usuarios "$TV" '{"username":"x"}';            check "VETERINARIO POST /usuarios -> 403" 403
req POST /veterinarios "$TV" '{"nombres":"x"}';         check "VETERINARIO POST /veterinarios -> 403" 403
req DELETE "/mascotas?id=1" "$TV";                      check "VETERINARIO DELETE /mascotas -> 403" 403
req DELETE "/propietarios?id=1" "$TV";                  check "VETERINARIO DELETE /propietarios -> 403" 403
req GET /usuarios "$TA";                                check "ADMIN GET /usuarios -> 200" 200
req GET /roles "$TA";                                   check "ADMIN GET /roles -> 200" 200 VETERINARIO
req GET /auditoria "$TA";                               check "ADMIN GET /auditoria -> 200" 200
req GET /veterinarios "$TV";                            check "VETERINARIO GET /veterinarios -> 200 (lectura permitida)" 200
req GET "/usuarios;jsessionid=1" "$TV";                 check "Truco ';parametro' en la ruta no evade el 403" 403
req GET /ruta-que-no-existe "$TA";                      check "Ruta desconocida: denegar por defecto -> 403" 403

# ------------------------------------------------------------------------------
titulo "3. REGISTRO, USUARIOS Y CUENTAS"
U="alumno_$SUF"
req POST /registro "" "{\"username\":\"$U\",\"password\":\"Clave2026\",\"nombre\":\"Alumno Prueba\",\"email\":\"$U@correo.com\"}"
check "Registro público válido -> 201" 201 '"estado":false'
IDU=$(campo idUsuario)
req POST /registro "" "{\"username\":\"otro_$SUF\",\"password\":\"Clave2026\",\"nombre\":\"X\",\"email\":\"$U@correo.com\"}"
check "Correo repetido -> 409 (RF-01)" 409 "ya está registrado"
req POST /registro "" "{\"username\":\"$U\",\"password\":\"Clave2026\",\"nombre\":\"X\",\"email\":\"nuevo_$SUF@correo.com\"}"
check "Usuario repetido -> 409" 409
req POST /registro "" "{\"username\":\"d_$SUF\",\"password\":\"corta\",\"nombre\":\"X\",\"email\":\"d_$SUF@c.com\"}"
check "Contraseña débil -> 400" 400
req POST /registro "" "{\"username\":\"e_$SUF\",\"password\":\"Clave2026\",\"nombre\":\"X\",\"email\":\"no-es-correo\"}"
check "Correo con formato inválido -> 400" 400
req POST /registro "" "{\"username\":\"f_$SUF\",\"password\":\"Clave2026\",\"nombre\":\"X\",\"email\":\"f_$SUF@c.com\",\"roles\":[\"ADMIN\"]}"
check "Registro intentando pedir rol ADMIN -> queda VETERINARIO" 201 '"VETERINARIO"'
login "$U" Clave2026;                                   check "Cuenta pendiente: login -> 403" 403 pendiente
req PUT /usuarios "$TA" "{\"idUsuario\":$IDU,\"username\":\"$U\",\"nombre\":\"Alumno Prueba\",\"email\":\"$U@correo.com\",\"estado\":true}"
check "ADMIN activa la cuenta -> 200" 200
login "$U" Clave2026;                                   check "Tras activar: login -> 200" 200 '"token"'
TU=$(campo token)
req GET /usuarios "$TU";                                check "El nuevo usuario (VETERINARIO) no entra a /usuarios -> 403" 403
req PUT /usuarios "$TA" "{\"idUsuario\":$IDU,\"username\":\"$U\",\"nombre\":\"Alumno Prueba\",\"email\":\"$U@correo.com\",\"estado\":false}"
check "ADMIN desactiva la cuenta -> 200" 200
req GET /me "$TU";                                      check "Token de cuenta desactivada deja de servir -> 401" 401 CUENTA_INACTIVA
req POST /usuarios "$TA" "{\"username\":\"adm_$SUF\",\"password\":\"Clave2026\",\"nombre\":\"Admin Dos\",\"email\":\"adm_$SUF@c.com\",\"roles\":[\"ADMIN\"]}"
check "ADMIN crea otro ADMIN -> 201" 201 '"ADMIN"'
req POST /usuarios "$TA" "{\"username\":\"r_$SUF\",\"password\":\"Clave2026\",\"nombre\":\"R\",\"email\":\"r_$SUF@c.com\",\"roles\":[\"SUPERMAN\"]}"
check "Rol que no existe -> 400" 400
req PUT /usuarios "$TA" '{"idUsuario":1,"username":"admin","nombre":"Administrador","email":"admin@veterinaria.com","estado":false}'
check "ADMIN no puede desactivarse a sí mismo -> 409" 409
req PUT /usuarios "$TA" '{"idUsuario":1,"username":"admin","nombre":"Administrador","email":"admin@veterinaria.com","roles":["VETERINARIO"]}'
check "ADMIN no puede quitarse su rol -> 409" 409
req GET "/usuarios?id=1" "$TA";                         check "Detalle de usuario no expone el hash" 200
echo "$BODY" | grep -qi "hash\|password" && { FAIL=$((FAIL+1)); echo "  FALLA La respuesta contiene datos de contraseña"; } || { OK=$((OK+1)); echo "  PASA  La respuesta no contiene la contraseña ni su hash"; }

# ------------------------------------------------------------------------------
titulo "4. PROPIETARIOS Y MASCOTAS (CRUD, validaciones, filtros)"
DOC="9$SUF"
req POST /propietarios "$TV" "{\"tipoDocumento\":\"CC\",\"numeroDocumento\":\"$DOC\",\"nombres\":\"Laura\",\"apellidos\":\"Gómez $SUF\",\"telefono\":\"3001234567\",\"email\":\"laura$SUF@correo.com\"}"
check "Crear propietario -> 201" 201 '"id"'
IDP=$(campo id)
req POST /propietarios "$TV" "{\"tipoDocumento\":\"CC\",\"numeroDocumento\":\"$DOC\",\"nombres\":\"Otra\",\"apellidos\":\"Persona\"}"
check "Documento duplicado -> 409" 409
req POST /propietarios "$TV" '{"tipoDocumento":"CC","numeroDocumento":"123456","apellidos":"Sin nombre"}'
check "Falta 'nombres' -> 400" 400 nombres
req POST /propietarios "$TV" '{"tipoDocumento":"XX","numeroDocumento":"123456","nombres":"A","apellidos":"B"}'
check "Tipo de documento inválido -> 400" 400
req POST /propietarios "$TV" '{"tipoDocumento":"CC","numeroDocumento":"12; DROP TABLE x","nombres":"A","apellidos":"B"}'
check "Documento con caracteres peligrosos -> 400" 400
req GET "/propietarios?id=$IDP" "$TV";                  check "Detalle de propietario (RF-10) -> 200" 200 "Laura"
req GET "/propietarios?id=99999999" "$TV";              check "Propietario inexistente -> 404" 404 NO_ENCONTRADO
req GET "/propietarios?id=abc" "$TV";                   check "Parámetro id no numérico -> 400" 400
req GET "/propietarios?q=laura&documento=$DOC" "$TV";   check "Filtro por 2 criterios (nombre + documento) -> encuentra" 200 "$DOC"
req GET "/propietarios?q=zzzznadie&documento=$DOC" "$TV"; check "Filtro con nombre que no coincide -> lista vacía" 200 '^\[\]$'
req PUT /propietarios "$TV" "{\"idPropietario\":$IDP,\"tipoDocumento\":\"CC\",\"numeroDocumento\":\"$DOC\",\"nombres\":\"Laura Editada\",\"apellidos\":\"Gómez $SUF\"}"
check "Editar propietario -> 200" 200 "Laura Editada"

req POST /mascotas "$TV" "{\"nombre\":\"Luna $SUF\",\"especie\":\"Perro\",\"raza\":\"Criollo\",\"sexo\":\"H\",\"fechaNacimiento\":\"2021-05-10\",\"pesoActual\":12.5,\"propietario\":{\"idPropietario\":$IDP}}"
check "Crear mascota -> 201" 201 '"estado":"ACTIVO"'
IDM=$(campo id)
req POST /mascotas "$TV" '{"nombre":"Sin dueño","especie":"Gato"}';  check "Mascota sin propietario -> 400" 400
req POST /mascotas "$TV" '{"nombre":"Fantasma","especie":"Gato","propietario":{"idPropietario":99999999}}'
check "Propietario inexistente -> 404" 404
req POST /mascotas "$TV" "{\"nombre\":\"Futura\",\"especie\":\"Gato\",\"fechaNacimiento\":\"2099-01-01\",\"propietario\":{\"idPropietario\":$IDP}}"
check "Fecha de nacimiento futura -> 400" 400
req POST /mascotas "$TV" "{\"nombre\":\"Pesada\",\"especie\":\"Gato\",\"pesoActual\":-4,\"propietario\":{\"idPropietario\":$IDP}}"
check "Peso negativo -> 400" 400
req POST /mascotas "$TV" "{\"nombre\":\"Sexo\",\"especie\":\"Gato\",\"sexo\":\"Z\",\"propietario\":{\"idPropietario\":$IDP}}"
check "Sexo inválido -> 400" 400
req GET "/mascotas?q=luna&especie=perro&idPropietario=$IDP" "$TV"; check "Filtro mascotas (nombre + especie + dueño) -> 200" 200 "Luna $SUF"
req GET "/mascotas?especie=ave&idPropietario=$IDP" "$TV";          check "Filtro con especie distinta -> vacío" 200 '^\[\]$'
req GET "/mascotas?estado=XYZ" "$TV";                              check "Estado de filtro inválido -> 400" 400
req PUT /mascotas "$TV" "{\"idMascota\":$IDM,\"nombre\":\"Luna $SUF\",\"especie\":\"Perro\",\"pesoActual\":13.2,\"propietario\":{\"idPropietario\":$IDP}}"
check "Editar mascota -> 200 con el nuevo peso" 200 13.2
req DELETE "/propietarios?id=$IDP" "$TA";               check "No se elimina un propietario con mascotas -> 409" 409 "mascota"

# ------------------------------------------------------------------------------
titulo "5. CITAS (reglas de negocio)"
DIA1=$(fut 7 10:00:00); DIA1B=$(fut 7 10:15:00); DIA1C=$(fut 7 10:30:00); DIA1D=$(fut 7 11:00:00)
req POST /veterinarios "$TA" "{\"tipoDocumento\":\"CC\",\"numeroDocumento\":\"8$SUF\",\"nombres\":\"Segundo\",\"apellidos\":\"Veterinario $SUF\",\"especialidad\":\"Cirugía\",\"estado\":\"ACTIVO\"}"
check "ADMIN crea un segundo veterinario -> 201" 201
IDV2=$(campo id)
req POST /citas "$TV" "{\"idMascota\":$IDM,\"fechaHora\":\"$DIA1\",\"motivo\":\"Control general\"}"
check "VETERINARIO agenda su cita (sin indicar veterinario) -> 201 PROGRAMADA" 201 PROGRAMADA
IDC=$(campo idCita)
req POST /citas "$TA" "{\"idMascota\":$IDM,\"idVeterinario\":$IDVET,\"fechaHora\":\"$DIA1\",\"motivo\":\"Otra\"}"
check "Misma hora y mismo veterinario -> 409" 409 "se cruza"
req POST /citas "$TA" "{\"idMascota\":$IDM,\"idVeterinario\":$IDVET,\"fechaHora\":\"$DIA1B\",\"motivo\":\"Otra\"}"
check "Cruce parcial (10:15 dentro de 10:00-10:30) -> 409" 409
req POST /citas "$TA" "{\"idMascota\":$IDM,\"idVeterinario\":$IDV2,\"fechaHora\":\"$DIA1B\",\"motivo\":\"Otra\"}"
check "La misma mascota no puede estar en 2 citas a la vez -> 409" 409 mascota
req POST /citas "$TA" "{\"idMascota\":$IDM,\"idVeterinario\":$IDVET,\"fechaHora\":\"$DIA1C\",\"motivo\":\"Justo después\"}"
check "Cita que empieza cuando termina la anterior -> 201 (no hay cruce)" 201
IDC2=$(campo idCita)
req POST /citas "$TA" "{\"idMascota\":$IDM,\"idVeterinario\":$IDVET,\"fechaHora\":\"2020-01-01T10:00:00\",\"motivo\":\"Pasado\"}"
check "Cita en el pasado -> 400" 400 futuras
req POST /citas "$TA" "{\"idMascota\":$IDM,\"idVeterinario\":$IDVET,\"fechaHora\":\"$DIA1D\",\"motivo\":\"x\",\"duracionMinutos\":5}"
check "Duración fuera de rango -> 400" 400
req POST /citas "$TA" "{\"idMascota\":$IDM,\"idVeterinario\":$IDVET,\"fechaHora\":\"$DIA1D\"}"
check "Sin motivo -> 400" 400 motivo
req POST /citas "$TA" "{\"idMascota\":$IDM,\"idVeterinario\":$IDVET,\"fechaHora\":\"no-es-fecha\",\"motivo\":\"x\"}"
check "Fecha con formato inválido -> 400" 400
req POST /citas "$TV" "{\"idMascota\":$IDM,\"idVeterinario\":$IDV2,\"fechaHora\":\"$DIA1D\",\"motivo\":\"Para otro\"}"
check "VETERINARIO no puede agendar a nombre de otro -> 403" 403
req POST /citas "$TA" "{\"idMascota\":$IDM,\"idVeterinario\":$IDV2,\"fechaHora\":\"$DIA1D\",\"motivo\":\"Cirugía\"}"
check "ADMIN agenda con el segundo veterinario -> 201" 201
IDC3=$(campo idCita)
req POST "/citas/$IDC3/cancelar" "$TV" '{"motivo":"x"}'
check "VETERINARIO no gestiona citas de OTRO veterinario -> 403" 403 "asignadas a usted"
req PUT /citas "$TV" "{\"idCita\":$IDC3,\"fechaHora\":\"$DIA1D\",\"motivo\":\"x\"}"
check "VETERINARIO no edita citas de otro -> 403" 403
req GET "/citas?estado=PROGRAMADA&idMascota=$IDM" "$TV"; check "Filtro de citas (estado + mascota) -> 200" 200 "Control general"
req GET "/citas?fecha=${DIA1%%T*}&idVeterinario=$IDVET" "$TV"; check "Filtro de citas (fecha + veterinario) -> 200" 200 "$DIA1"
req GET "/citas?estado=INVENTADO" "$TV";                check "Estado de filtro inválido -> 400" 400
req GET "/citas/$IDC" "$TV";                            check "Detalle de cita -> 200" 200 "Control general"
req GET "/citas/99999999" "$TV";                        check "Cita inexistente -> 404" 404
req PUT /citas "$TV" "{\"idCita\":$IDC,\"fechaHora\":\"$(fut 8 09:00:00)\",\"motivo\":\"Control reprogramado\"}"
check "Reprogramar cita propia -> 200" 200 reprogramado
req POST "/citas/$IDC/atender" "$TV" '{"diagnostico":"x"}'
check "No se atiende por adelantado (cita de otro día) -> 409" 409 adelantado
req POST "/citas/$IDC2/cancelar" "$TV" '{"motivo":"El dueño no puede asistir"}'
check "VETERINARIO cancela su cita -> 200 CANCELADA" 200 CANCELADA
req POST "/citas/$IDC2/cancelar" "$TV";                 check "Cancelar una cita ya cancelada -> 409" 409
req PUT /citas "$TV" "{\"idCita\":$IDC2,\"fechaHora\":\"$(fut 9 09:00:00)\",\"motivo\":\"x\"}"
check "Editar una cita CANCELADA -> 409" 409
# Atender una cita de HOY (se crea 2 minutos hacia adelante)
AHORA=$(date -d '+2 minutes' '+%Y-%m-%dT%H:%M:%S' 2>/dev/null)
if [ "${AHORA%%T*}" = "$(date '+%Y-%m-%d')" ]; then
  req POST /citas "$TA" "{\"idMascota\":$IDM,\"idVeterinario\":$IDVET,\"fechaHora\":\"$AHORA\",\"motivo\":\"Consulta de hoy\"}"
  check "Cita para hoy -> 201" 201
  IDC4=$(campo idCita)
  req POST "/citas/$IDC4/atender" "$TV" '{"diagnostico":"Otitis leve","peso":13.8,"temperatura":38.6}'
  check "Atender cita con diagnóstico -> 200 ATENDIDA con historia creada" 200 ATENDIDA
  IDH=$(campo idHistorial)
  req POST "/citas/$IDC4/cancelar" "$TV";               check "No se cancela una cita ya ATENDIDA -> 409" 409
  req GET "/mascotas?id=$IDM" "$TV";                    check "El peso de la mascota se actualizó con el de la atención" 200 13.8
fi

# ------------------------------------------------------------------------------
titulo "6. HISTORIA CLÍNICA, TRATAMIENTOS, VACUNAS Y ALERGIAS"
req POST /medicamentos "$TV" "{\"nombre\":\"Amoxicilina $SUF\",\"tipo\":\"Antibiótico\",\"presentacion\":\"Tabletas 500mg\"}"
check "Crear medicamento -> 201" 201
IDMED=$(campo idMedicamento)
req POST /medicamentos "$TV" "{\"nombre\":\"amoxicilina $SUF\"}";  check "Medicamento repetido (sin importar mayúsculas) -> 409" 409
req POST /historiales "$TV" "{\"idMascota\":$IDM,\"motivoConsulta\":\"Vómito\",\"diagnostico\":\"Gastritis\",\"peso\":13.5,\"temperatura\":38.8}"
check "VETERINARIO registra una atención (queda a su nombre) -> 201" 201 Gastritis
IDH2=$(campo idHistorial)
req POST /historiales "$TV" "{\"idMascota\":$IDM,\"motivoConsulta\":\"x\",\"temperatura\":60}"
check "Temperatura fuera de rango -> 400" 400
req POST /historiales "$TV" "{\"idMascota\":$IDM,\"idVeterinario\":$IDV2,\"motivoConsulta\":\"x\"}"
check "VETERINARIO no puede firmar una atención por otro -> 403" 403
req POST /historiales "$TA" "{\"idMascota\":$IDM,\"idVeterinario\":$IDV2,\"motivoConsulta\":\"Cirugía menor\",\"diagnostico\":\"Herida\"}"
check "ADMIN registra atención del segundo veterinario -> 201" 201
IDH3=$(campo idHistorial)
req PUT /historiales "$TV" "{\"idHistorial\":$IDH3,\"motivoConsulta\":\"Cambiado\"}"
check "VETERINARIO no edita atenciones de OTRO -> 403" 403
req PUT /historiales "$TV" "{\"idHistorial\":$IDH2,\"motivoConsulta\":\"Vómito persistente\",\"diagnostico\":\"Gastritis aguda\"}"
check "VETERINARIO edita su propia atención -> 200" 200 persistente
req POST /tratamientos "$TV" "{\"idHistorial\":$IDH2,\"idMedicamento\":$IDMED,\"dosis\":\"1 tableta\",\"frecuencia\":\"Cada 12 horas\",\"duracion\":7}"
check "Agregar tratamiento a su atención -> 201" 201 Amoxicilina
req POST /tratamientos "$TV" "{\"idHistorial\":$IDH3,\"idMedicamento\":$IDMED,\"dosis\":\"1\",\"frecuencia\":\"1\"}"
check "Tratamiento en atención ajena -> 403" 403
req POST /tratamientos "$TV" "{\"idHistorial\":$IDH2,\"idMedicamento\":$IDMED,\"dosis\":\"1\",\"frecuencia\":\"1\",\"duracion\":9999}"
check "Duración de tratamiento fuera de rango -> 400" 400
req GET "/historiales?id=$IDH2" "$TV";                  check "Detalle de historia incluye sus tratamientos" 200 tratamientos
req GET "/historiales?idMascota=$IDM" "$TV";            check "Historial de la mascota -> 200" 200 Vómito
req GET "/tratamientos" "$TV";                          check "Listar tratamientos sin 'idHistorial' -> 400" 400
req POST /vacunas "$TA" "{\"nombre\":\"Rabia $SUF\",\"especieAplicable\":\"Perro\"}";  check "Crear vacuna -> 201" 201
IDVAC=$(campo idVacuna)
req POST /vacunaciones "$TV" "{\"idMascota\":$IDM,\"idVacuna\":$IDVAC,\"fechaAplicacion\":\"$(date '+%Y-%m-%d')\",\"fechaProxima\":\"$(date -d '+10 days' '+%Y-%m-%d')\"}"
check "Registrar vacunación -> 201" 201
req POST /vacunaciones "$TV" "{\"idMascota\":$IDM,\"idVacuna\":$IDVAC,\"fechaAplicacion\":\"2099-01-01\"}"
check "Vacunación con fecha futura -> 400" 400
req POST /vacunaciones "$TV" "{\"idMascota\":$IDM,\"idVacuna\":$IDVAC,\"fechaAplicacion\":\"2026-06-01\",\"fechaProxima\":\"2026-01-01\"}"
check "Refuerzo anterior a la aplicación -> 400" 400
req GET "/vacunaciones?proximasDias=30" "$TV";          check "Refuerzos próximos a vencer (alerta Home) -> 200" 200 "Rabia $SUF"
req POST /alergias "$TV" "{\"nombre\":\"Polen $SUF\"}";  check "Crear alergia -> 201" 201
IDAL=$(campo idAlergia)
req POST /mascota-alergias "$TV" "{\"idMascota\":$IDM,\"idAlergia\":$IDAL,\"observaciones\":\"Estornudos\"}"
check "Registrar alergia de la mascota -> 201" 201
req POST /mascota-alergias "$TV" "{\"idMascota\":$IDM,\"idAlergia\":$IDAL}"
check "Alergia duplicada en la misma mascota -> 409" 409
req GET "/mascota-alergias?idMascota=$IDM" "$TV";       check "Alergias de la mascota -> 200" 200 Polen
req DELETE "/vacunas?id=$IDVAC" "$TA";                  check "Eliminar una vacuna ya usada -> 409 (integridad referencial)" 409

# ------------------------------------------------------------------------------
titulo "7. DESACTIVAR EN LUGAR DE BORRAR"
req DELETE "/mascotas?id=$IDM" "$TA";                   check "No se desactiva una mascota con citas programadas -> 409" 409 programada
req DELETE "/veterinarios?id=$IDV2" "$TA";             check "No se desactiva un veterinario con citas programadas -> 409" 409
req POST "/citas/$IDC/cancelar" "$TA"
req POST "/citas/$IDC3/cancelar" "$TA"
req DELETE "/mascotas?id=$IDM" "$TA";                   check "ADMIN desactiva la mascota (sin citas pendientes) -> 204" 204
req DELETE "/veterinarios?id=$IDV2" "$TA";             check "Sin citas pendientes, el veterinario se desactiva -> 204" 204
req GET "/veterinarios?estado=INACTIVO&q=Segundo" "$TA"; check "El veterinario desactivado figura como INACTIVO" 200 INACTIVO
req GET "/mascotas?q=Luna%20$SUF" "$TV";               check "La mascota desactivada ya no aparece en la lista" 200 '^\[\]$'
req GET "/mascotas?estado=TODOS&q=Luna%20$SUF" "$TV";  check "Con estado=TODOS sí aparece, como INACTIVO" 200 INACTIVO
req GET "/historiales?idMascota=$IDM" "$TV";           check "Su historia clínica se conserva" 200 Vómito
req POST /citas "$TA" "{\"idMascota\":$IDM,\"idVeterinario\":$IDVET,\"fechaHora\":\"$(fut 20 09:00:00)\",\"motivo\":\"x\"}"
check "Mascota inactiva no recibe citas -> 409" 409 inactiva
req DELETE "/historiales?id=$IDH2" "$TA";              check "ADMIN elimina una atención (con sus tratamientos) -> 204" 204

# ------------------------------------------------------------------------------
titulo "8. ESTADÍSTICAS, EXPORTACIÓN Y AUDITORÍA"
req GET /estadisticas "$TV";                            check "Estadísticas para el Home -> 200" 200 citasProgramadas
req GET /export/mascotas/excel "$TV";                   check "Exportar a Excel con token -> 200" 200
req GET /auditoria "$TA";                               check "La auditoría registró acciones" 200 CREAR
echo "$BODY" | grep -q "Cita" && { OK=$((OK+1)); echo "  PASA  La auditoría incluye acciones sobre citas"; } || { FAIL=$((FAIL+1)); echo "  FALLA La auditoría no incluye citas"; }

# ------------------------------------------------------------------------------
titulo "9. CIERRE DE SESIÓN Y FUERZA BRUTA"
login veterinario vet12345;  TV2=$(campo token)
req GET /me "$TV2";                                     check "Token activo funciona" 200
req POST /logout "$TV2";                                check "Logout -> 200" 200
req GET /me "$TV2";                                     check "El token cerrado ya no sirve -> 401 TOKEN_REVOCADO" 401 TOKEN_REVOCADO
BF="brute_$SUF"
for i in 1 2 3 4 5; do login "$BF" "mala$i"; done
login "$BF" "otra";                                     check "Tras 5 intentos fallidos la cuenta se bloquea -> 429" 429 DEMASIADOS_INTENTOS
req POST /logout "";                                    check "Logout sin token -> 401" 401

# ------------------------------------------------------------------------------
printf '\n\033[1mRESULTADO: %d pasan, %d fallan\033[0m\n' "$OK" "$FAIL"
[ "$FAIL" = 0 ]
