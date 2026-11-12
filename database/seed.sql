-- =============================================================================
--  VETCARE SOLUTIONS S.A.S. - Gestor de veterinaria
--  seed.sql : datos de ejemplo para probar y sustentar (ejecutar DESPUÉS de schema.sql)
--
--  Uso:   mysql -u root -p veterinaria < seed.sql
--
--  CUENTAS DE DEMOSTRACIÓN (cámbielas si el sistema se usa con datos reales):
--     admin        / admin123     rol ADMIN
--     veterinario  / vet12345     rol VETERINARIO  (perfil: Veterinario Demo)
--     dra.rojas    / vet12345     rol VETERINARIO  (perfil: Camila Rojas)
--  Las contraseñas están guardadas como hash BCrypt, no en texto plano.
--
--  Las fechas de citas y vacunas son RELATIVAS a hoy (CURDATE()), así los ejemplos
--  siempre quedan en el futuro o en el pasado reciente, sin importar cuándo se cargue.
-- =============================================================================
USE veterinaria;

-- ---------- Roles y usuarios ----------
INSERT INTO rol (idRol, nombre, descripcion) VALUES
 (1, 'ADMIN',       'Administrador del sistema'),
 (2, 'VETERINARIO', 'Veterinario: gestiona pacientes, citas e historia clínica');

INSERT INTO usuario (idUsuario, username, passwordHash, nombre, email, estado) VALUES
 (1, 'admin',       '$2a$10$X/7XjkiSHX16AVo9mG59Ae.V87cl4Uk6ARYgDuPFzzHNkvVHJeJmW', 'Administrador',    'admin@veterinaria.com',       1),
 (2, 'veterinario', '$2a$10$8sqeHC1pJFMTVoosyTAhVuS0yI.9CEaBZfpWjc1CkAJ3JRN1XQ9O6', 'Veterinario Demo', 'veterinario@veterinaria.com', 1),
 (3, 'dra.rojas',   '$2a$10$8sqeHC1pJFMTVoosyTAhVuS0yI.9CEaBZfpWjc1CkAJ3JRN1XQ9O6', 'Camila Rojas',     'camila.rojas@veterinaria.com', 1);

INSERT INTO usuario_rol (id_usuario, id_rol) VALUES (1, 1), (2, 2), (3, 2);

-- ---------- Veterinarios ----------
INSERT INTO veterinario (idVeterinario, tipoDocumento, numeroDocumento, nombres, apellidos, especialidad, telefono, email, estado, id_usuario) VALUES
 (1, 'CC', '1000000001', 'Veterinario', 'Demo',   'Medicina general',     '3000000001', 'veterinario@veterinaria.com',  'ACTIVO',   2),
 (2, 'CC', '52111222',   'Camila',      'Rojas',  'Cirugía y ortopedia',  '3105551122', 'camila.rojas@veterinaria.com', 'ACTIVO',   3),
 (3, 'CC', '79333444',   'Andrés',      'Pardo',  'Dermatología',         '3207773344', 'andres.pardo@veterinaria.com', 'INACTIVO', NULL);

-- ---------- Propietarios ----------
INSERT INTO propietario (idPropietario, tipoDocumento, numeroDocumento, nombres, apellidos, telefono, email, direccion) VALUES
 (1, 'CC', '1019876543', 'Laura',   'Gómez Rincón',   '3001112233', 'laura.gomez@correo.com',  'Cra 15 # 80-20, Bogotá'),
 (2, 'CC', '80123456',   'Carlos',  'Martínez Silva', '3012223344', 'carlos.martinez@correo.com', 'Calle 45 # 12-30, Bogotá'),
 (3, 'CE', '445566',     'Sofía',   'Herrera Díaz',   '3023334455', 'sofia.herrera@correo.com', 'Av. Boyacá # 63-10, Bogotá'),
 (4, 'CC', '52987654',   'Andrea',  'Ruiz Pérez',     '3034445566', NULL,                       'Cll 100 # 19-54, Bogotá'),
 (5, 'TI', '1002003004', 'Miguel',  'Torres Castro',  '3045556677', 'miguel.torres@correo.com', 'Transv. 20 # 60-15, Bogotá');

-- ---------- Mascotas ----------
INSERT INTO mascota (idMascota, id_propietario, nombre, especie, raza, sexo, fechaNacimiento, color, pesoActual, esterilizado, estado) VALUES
 (1, 1, 'Luna',   'Perro', 'Labrador',    'H', '2020-03-15', 'Dorado',          28.50, 1, 'ACTIVO'),
 (2, 1, 'Michi',  'Gato',  'Criollo',     'M', '2022-08-01', 'Gris atigrado',    4.20, 1, 'ACTIVO'),
 (3, 2, 'Rocky',  'Perro', 'Bulldog',     'M', '2019-11-20', 'Blanco y café',   22.00, 0, 'ACTIVO'),
 (4, 3, 'Nala',   'Gato',  'Siamés',      'H', '2021-05-05', 'Crema',            3.80, 1, 'ACTIVO'),
 (5, 3, 'Toby',   'Perro', 'Beagle',      'M', '2018-01-30', 'Tricolor',        12.30, 1, 'ACTIVO'),
 (6, 4, 'Kiwi',   'Ave',   'Periquito',   'M', '2023-02-14', 'Verde',            0.04, 0, 'ACTIVO'),
 (7, 5, 'Bruno',  'Perro', 'Pastor alemán','M', '2017-07-07', 'Negro y fuego',  35.00, 1, 'ACTIVO'),
 (8, 2, 'Pelusa', 'Conejo','Enano',       'H', '2021-12-12', 'Blanco',           1.60, 0, 'INACTIVO');

-- ---------- Catálogos: vacunas, medicamentos, alergias ----------
INSERT INTO vacuna (idVacuna, nombre, descripcion, especieAplicable) VALUES
 (1, 'Rabia',            'Vacuna antirrábica anual',                    'Perro y gato'),
 (2, 'Parvovirus',       'Protección contra parvovirosis canina',       'Perro'),
 (3, 'Triple felina',    'Rinotraqueítis, calicivirus y panleucopenia', 'Gato'),
 (4, 'Moquillo',         'Distemper canino',                            'Perro'),
 (5, 'Bordetella',       'Tos de las perreras',                         'Perro');

INSERT INTO medicamento (idMedicamento, nombre, tipo, presentacion, descripcion) VALUES
 (1, 'Amoxicilina',   'Antibiótico',         'Tabletas 500 mg',   'Antibiótico de amplio espectro'),
 (2, 'Meloxicam',     'Antiinflamatorio',    'Suspensión 1.5 mg/ml', 'Control de dolor e inflamación'),
 (3, 'Ivermectina',   'Antiparasitario',     'Inyectable 1%',     'Parásitos internos y externos'),
 (4, 'Omeprazol',     'Protector gástrico',  'Cápsulas 20 mg',    'Gastritis y reflujo'),
 (5, 'Prednisolona',  'Corticoide',          'Tabletas 5 mg',     'Alergias y procesos inflamatorios');

INSERT INTO alergia (idAlergia, nombre, descripcion) VALUES
 (1, 'Pulgas (DAPP)',      'Dermatitis alérgica a la picadura de pulga'),
 (2, 'Pollo',              'Alergia alimentaria a proteína de pollo'),
 (3, 'Penicilina',         'Reacción a antibióticos betalactámicos'),
 (4, 'Polen',              'Alergia estacional');

INSERT INTO mascota_alergia (id_mascota, id_alergia, observaciones) VALUES
 (3, 2, 'Diarrea y picazón al consumir alimento con pollo'),
 (5, 1, 'Rascado intenso en la base de la cola'),
 (4, 3, 'Reacción cutánea tras inyección en 2023');

-- ---------- Atenciones clínicas y tratamientos ----------
INSERT INTO historial_clinico (idHistorial, id_mascota, id_veterinario, fechaAtencion, motivoConsulta, diagnostico, observaciones, peso, temperatura) VALUES
 (1, 1, 1, DATE_SUB(NOW(), INTERVAL 90 DAY), 'Control anual',           'Paciente sano',            'Se recomienda mantener dieta y ejercicio.', 28.00, 38.5),
 (2, 3, 1, DATE_SUB(NOW(), INTERVAL 40 DAY), 'Diarrea de 3 días',        'Gastroenteritis alimentaria', 'Dieta blanda 5 días. Evitar pollo (alergia conocida).', 22.10, 39.1),
 (3, 5, 2, DATE_SUB(NOW(), INTERVAL 25 DAY), 'Picazón y pérdida de pelo','Dermatitis alérgica por pulgas', 'Antiparasitario y control en 15 días.', 12.30, 38.8),
 (4, 7, 2, DATE_SUB(NOW(), INTERVAL 10 DAY), 'Cojera pata trasera',      'Esguince leve de rodilla', 'Reposo relativo 2 semanas.', 35.20, 38.4),
 (5, 4, 1, DATE_SUB(NOW(), INTERVAL 5 DAY),  'Vómito ocasional',         'Gastritis leve',           'Alimentación fraccionada.', 3.80, 38.6);

INSERT INTO tratamiento (id_historial, id_medicamento, dosis, frecuencia, duracion, indicaciones) VALUES
 (2, 4, '1 cápsula',   'Cada 24 horas', 5,  'Administrar en ayunas'),
 (3, 3, '0.2 ml',      'Dosis única',   1,  'Repetir en 15 días'),
 (3, 5, '1/2 tableta', 'Cada 12 horas', 7,  'Con comida'),
 (4, 2, '1.5 ml',      'Cada 24 horas', 10, 'Después de comer'),
 (5, 4, '1 cápsula',   'Cada 24 horas', 7,  'En ayunas');

-- ---------- Vacunaciones (los refuerzos próximos alimentan la alerta del Home) ----------
INSERT INTO vacunacion (id_mascota, id_vacuna, fechaAplicacion, fechaProxima, observaciones) VALUES
 (1, 1, DATE_SUB(CURDATE(), INTERVAL 350 DAY), DATE_ADD(CURDATE(), INTERVAL 15 DAY),  'Refuerzo anual próximo'),
 (1, 2, DATE_SUB(CURDATE(), INTERVAL 200 DAY), DATE_ADD(CURDATE(), INTERVAL 165 DAY), NULL),
 (3, 1, DATE_SUB(CURDATE(), INTERVAL 120 DAY), DATE_ADD(CURDATE(), INTERVAL 245 DAY), NULL),
 (3, 4, DATE_SUB(CURDATE(), INTERVAL 340 DAY), DATE_ADD(CURDATE(), INTERVAL 25 DAY),  'Refuerzo próximo'),
 (2, 3, DATE_SUB(CURDATE(), INTERVAL 60 DAY),  DATE_ADD(CURDATE(), INTERVAL 305 DAY), NULL),
 (4, 3, DATE_SUB(CURDATE(), INTERVAL 30 DAY),  DATE_ADD(CURDATE(), INTERVAL 335 DAY), NULL),
 (7, 1, DATE_SUB(CURDATE(), INTERVAL 10 DAY),  DATE_ADD(CURDATE(), INTERVAL 355 DAY), NULL);

-- ---------- Citas ----------
INSERT INTO cita (idCita, id_mascota, id_veterinario, fechaHora, duracionMinutos, motivo, estado, observaciones, id_historial) VALUES
 (1, 1, 1, TIMESTAMP(DATE_ADD(CURDATE(), INTERVAL 2 DAY), '09:00:00'), 30, 'Aplicar refuerzo de rabia',   'PROGRAMADA', NULL, NULL),
 (2, 3, 1, TIMESTAMP(DATE_ADD(CURDATE(), INTERVAL 2 DAY), '10:00:00'), 45, 'Control de gastroenteritis',  'PROGRAMADA', NULL, NULL),
 (3, 5, 2, TIMESTAMP(DATE_ADD(CURDATE(), INTERVAL 3 DAY), '15:30:00'), 30, 'Control de dermatitis',       'PROGRAMADA', NULL, NULL),
 (4, 7, 2, TIMESTAMP(DATE_ADD(CURDATE(), INTERVAL 4 DAY), '11:00:00'), 60, 'Revisión de rodilla',         'PROGRAMADA', NULL, NULL),
 (5, 2, 1, TIMESTAMP(DATE_SUB(CURDATE(), INTERVAL 5 DAY), '16:00:00'), 30, 'Vómito ocasional',            'ATENDIDA',   NULL, 5),
 (6, 6, 2, TIMESTAMP(DATE_SUB(CURDATE(), INTERVAL 2 DAY), '09:30:00'), 30, 'Revisión de plumaje',         'CANCELADA',  'Cancelada: el propietario no pudo asistir', NULL);

-- ---------- Auditoría de ejemplo ----------
INSERT INTO auditoria (idUsuario, accion, entidad, idRegistro, fechaHora, descripcion) VALUES
 (1, 'CREAR',     'Propietario', 1, DATE_SUB(NOW(), INTERVAL 100 DAY), 'Creación de propietario: Laura Gómez Rincón'),
 (1, 'CREAR',     'Mascota',     1, DATE_SUB(NOW(), INTERVAL 100 DAY), 'Creación de mascota: Luna'),
 (2, 'CREAR',     'Cita',        1, DATE_SUB(NOW(), INTERVAL 1 DAY),   'Cita para Luna'),
 (2, 'ATENDER',   'Cita',        5, DATE_SUB(NOW(), INTERVAL 5 DAY),   'Cita atendida de Michi'),
 (3, 'CANCELAR',  'Cita',        6, DATE_SUB(NOW(), INTERVAL 2 DAY),   'Cancelación de cita de Kiwi');
