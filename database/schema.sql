-- =============================================================================
--  VETCARE SOLUTIONS S.A.S. - Gestor de veterinaria
--  schema.sql : estructura de la base de datos (MySQL 8 / MariaDB 10.5+)
--
--  Uso:   mysql -u root -p < schema.sql        (o ejecutarlo desde phpMyAdmin / Workbench)
--  Luego: mysql -u root -p veterinaria < seed.sql     (datos de ejemplo, opcional)
--
--  Notas de diseño
--   * Los nombres de columna siguen los de las entidades JPA (camelCase) para que Hibernate valide
--     el esquema sin cambios. Las claves foráneas usan snake_case (id_mascota, id_veterinario...).
--   * Integridad referencial (RNF-09): toda relación tiene su FOREIGN KEY. Por defecto ON DELETE RESTRICT:
--     no se puede borrar un registro que otro todavía usa. Por eso la API "desactiva" mascotas y veterinarios.
--   * Los CHECK refuerzan en la base de datos las mismas reglas que valida la API
--     (se aplican en MySQL 8.0.16+ y MariaDB 10.2+; las versiones anteriores los ignoran).
-- =============================================================================

CREATE DATABASE IF NOT EXISTS veterinaria CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
USE veterinaria;

SET FOREIGN_KEY_CHECKS = 0;
DROP TABLE IF EXISTS cita, auditoria, mascota_alergia, alergia, vacunacion, vacuna, tratamiento, medicamento,
                     historial_clinico, mascota, propietario, veterinario, usuario_rol, usuario, rol;
SET FOREIGN_KEY_CHECKS = 1;

-- ---------------------------------------------------------------------------
--  SEGURIDAD: roles, usuarios y su relación (muchos a muchos)
-- ---------------------------------------------------------------------------
CREATE TABLE rol (
    idRol        BIGINT       NOT NULL AUTO_INCREMENT,
    nombre       VARCHAR(30)  NOT NULL,                  -- ADMIN | VETERINARIO
    descripcion  VARCHAR(150) NULL,
    PRIMARY KEY (idRol),
    CONSTRAINT uk_rol_nombre UNIQUE (nombre)
) ENGINE=InnoDB;

CREATE TABLE usuario (
    idUsuario     BIGINT       NOT NULL AUTO_INCREMENT,
    username      VARCHAR(50)  NOT NULL,
    passwordHash  VARCHAR(100) NOT NULL,                 -- hash BCrypt, nunca la contraseña en claro
    nombre        VARCHAR(100) NOT NULL,
    email         VARCHAR(120) NULL,
    estado        BIT(1)       NOT NULL,                 -- 1 = activo, 0 = inactivo / pendiente de aprobación
    PRIMARY KEY (idUsuario),
    CONSTRAINT uk_usuario_username UNIQUE (username),
    CONSTRAINT uk_usuario_email    UNIQUE (email)        -- RF-01: correo único (varios NULL permitidos)
) ENGINE=InnoDB;

CREATE TABLE usuario_rol (
    id          BIGINT NOT NULL AUTO_INCREMENT,
    id_usuario  BIGINT NOT NULL,
    id_rol      BIGINT NOT NULL,
    PRIMARY KEY (id),
    CONSTRAINT uk_usuario_rol UNIQUE (id_usuario, id_rol),
    CONSTRAINT fk_usuariorol_usuario FOREIGN KEY (id_usuario) REFERENCES usuario (idUsuario) ON DELETE CASCADE,
    CONSTRAINT fk_usuariorol_rol     FOREIGN KEY (id_rol)     REFERENCES rol (idRol)
) ENGINE=InnoDB;

-- ---------------------------------------------------------------------------
--  PERSONAS: propietarios (dueños) y veterinarios (profesionales)
-- ---------------------------------------------------------------------------
CREATE TABLE propietario (
    idPropietario    BIGINT       NOT NULL AUTO_INCREMENT,
    tipoDocumento    VARCHAR(5)   NOT NULL,              -- CC | TI | CE | PAS | NIT
    numeroDocumento  VARCHAR(30)  NOT NULL,
    nombres          VARCHAR(100) NOT NULL,
    apellidos        VARCHAR(100) NOT NULL,
    telefono         VARCHAR(30)  NULL,
    email            VARCHAR(120) NULL,
    direccion        VARCHAR(200) NULL,
    PRIMARY KEY (idPropietario),
    CONSTRAINT uk_propietario_documento UNIQUE (tipoDocumento, numeroDocumento),
    CONSTRAINT ck_propietario_tipodoc CHECK (tipoDocumento IN ('CC','TI','CE','PAS','NIT'))
) ENGINE=InnoDB;

CREATE TABLE veterinario (
    idVeterinario    BIGINT       NOT NULL AUTO_INCREMENT,
    tipoDocumento    VARCHAR(5)   NOT NULL,
    numeroDocumento  VARCHAR(30)  NOT NULL,
    nombres          VARCHAR(100) NOT NULL,
    apellidos        VARCHAR(100) NOT NULL,
    especialidad     VARCHAR(100) NULL,
    telefono         VARCHAR(30)  NULL,
    email            VARCHAR(120) NULL,
    estado           VARCHAR(10)  NOT NULL,              -- ACTIVO | INACTIVO
    id_usuario       BIGINT       NULL,                  -- cuenta de login del profesional (opcional, 1 a 1)
    PRIMARY KEY (idVeterinario),
    CONSTRAINT uk_veterinario_documento UNIQUE (tipoDocumento, numeroDocumento),
    CONSTRAINT uk_veterinario_usuario   UNIQUE (id_usuario),
    CONSTRAINT fk_veterinario_usuario FOREIGN KEY (id_usuario) REFERENCES usuario (idUsuario),
    CONSTRAINT ck_veterinario_estado CHECK (estado IN ('ACTIVO','INACTIVO'))
) ENGINE=InnoDB;

-- ---------------------------------------------------------------------------
--  PACIENTES: mascotas (1 propietario tiene N mascotas)
-- ---------------------------------------------------------------------------
CREATE TABLE mascota (
    idMascota        BIGINT       NOT NULL AUTO_INCREMENT,
    id_propietario   BIGINT       NOT NULL,
    nombre           VARCHAR(100) NOT NULL,
    especie          VARCHAR(50)  NOT NULL,
    raza             VARCHAR(80)  NULL,
    sexo             VARCHAR(1)   NULL,                  -- M | H
    fechaNacimiento  DATE         NULL,
    color            VARCHAR(50)  NULL,
    pesoActual       DOUBLE       NULL,                  -- kg
    esterilizado     BIT(1)       NULL,
    estado           VARCHAR(10)  NOT NULL,              -- ACTIVO | INACTIVO (no se borran: se desactivan)
    PRIMARY KEY (idMascota),
    CONSTRAINT fk_mascota_propietario FOREIGN KEY (id_propietario) REFERENCES propietario (idPropietario),
    CONSTRAINT ck_mascota_estado CHECK (estado IN ('ACTIVO','INACTIVO')),
    CONSTRAINT ck_mascota_sexo   CHECK (sexo IS NULL OR sexo IN ('M','H')),
    CONSTRAINT ck_mascota_peso   CHECK (pesoActual IS NULL OR pesoActual > 0)
) ENGINE=InnoDB;

-- ---------------------------------------------------------------------------
--  ATENCIÓN CLÍNICA: historial, medicamentos y tratamientos
-- ---------------------------------------------------------------------------
CREATE TABLE historial_clinico (
    idHistorial     BIGINT        NOT NULL AUTO_INCREMENT,
    id_mascota      BIGINT        NOT NULL,
    id_veterinario  BIGINT        NOT NULL,
    fechaAtencion   DATETIME(6)   NULL,
    motivoConsulta  VARCHAR(255)  NOT NULL,
    diagnostico     VARCHAR(2000) NULL,
    observaciones   VARCHAR(2000) NULL,
    peso            DOUBLE        NULL,
    temperatura     DOUBLE        NULL,                  -- grados Celsius
    PRIMARY KEY (idHistorial),
    CONSTRAINT fk_historial_mascota     FOREIGN KEY (id_mascota)     REFERENCES mascota (idMascota),
    CONSTRAINT fk_historial_veterinario FOREIGN KEY (id_veterinario) REFERENCES veterinario (idVeterinario),
    CONSTRAINT ck_historial_peso CHECK (peso IS NULL OR (peso > 0 AND peso <= 500)),
    CONSTRAINT ck_historial_temp CHECK (temperatura IS NULL OR (temperatura >= 30 AND temperatura <= 45))
) ENGINE=InnoDB;

CREATE TABLE medicamento (
    idMedicamento  BIGINT       NOT NULL AUTO_INCREMENT,
    nombre         VARCHAR(100) NOT NULL,
    tipo           VARCHAR(255) NULL,
    presentacion   VARCHAR(255) NULL,
    descripcion    VARCHAR(255) NULL,
    PRIMARY KEY (idMedicamento),
    CONSTRAINT uk_medicamento_nombre UNIQUE (nombre)
) ENGINE=InnoDB;

CREATE TABLE tratamiento (                               -- medicamento recetado dentro de una atención
    idTratamiento  BIGINT       NOT NULL AUTO_INCREMENT,
    id_historial   BIGINT       NOT NULL,
    id_medicamento BIGINT       NOT NULL,
    dosis          VARCHAR(100) NOT NULL,
    frecuencia     VARCHAR(100) NOT NULL,
    duracion       INT          NULL,                    -- días
    indicaciones   VARCHAR(500) NULL,
    PRIMARY KEY (idTratamiento),
    CONSTRAINT fk_tratamiento_historial    FOREIGN KEY (id_historial)   REFERENCES historial_clinico (idHistorial) ON DELETE CASCADE,
    CONSTRAINT fk_tratamiento_medicamento  FOREIGN KEY (id_medicamento) REFERENCES medicamento (idMedicamento),
    CONSTRAINT ck_tratamiento_duracion CHECK (duracion IS NULL OR (duracion >= 1 AND duracion <= 365))
) ENGINE=InnoDB;

-- ---------------------------------------------------------------------------
--  VACUNAS y ALERGIAS
-- ---------------------------------------------------------------------------
CREATE TABLE vacuna (
    idVacuna          BIGINT       NOT NULL AUTO_INCREMENT,
    nombre            VARCHAR(100) NOT NULL,
    descripcion       VARCHAR(255) NULL,
    especieAplicable  VARCHAR(50)  NULL,
    PRIMARY KEY (idVacuna),
    CONSTRAINT uk_vacuna_nombre UNIQUE (nombre)
) ENGINE=InnoDB;

CREATE TABLE vacunacion (                                -- carnet: mascota + vacuna + fechas
    idVacunacion     BIGINT       NOT NULL AUTO_INCREMENT,
    id_mascota       BIGINT       NOT NULL,
    id_vacuna        BIGINT       NOT NULL,
    fechaAplicacion  DATE         NOT NULL,
    fechaProxima     DATE         NULL,                  -- próximo refuerzo
    observaciones    VARCHAR(255) NULL,
    PRIMARY KEY (idVacunacion),
    CONSTRAINT fk_vacunacion_mascota FOREIGN KEY (id_mascota) REFERENCES mascota (idMascota),
    CONSTRAINT fk_vacunacion_vacuna  FOREIGN KEY (id_vacuna)  REFERENCES vacuna (idVacuna),
    CONSTRAINT ck_vacunacion_fechas CHECK (fechaProxima IS NULL OR fechaProxima >= fechaAplicacion)
) ENGINE=InnoDB;

CREATE TABLE alergia (
    idAlergia    BIGINT       NOT NULL AUTO_INCREMENT,
    nombre       VARCHAR(100) NOT NULL,
    descripcion  VARCHAR(255) NULL,
    PRIMARY KEY (idAlergia),
    CONSTRAINT uk_alergia_nombre UNIQUE (nombre)
) ENGINE=InnoDB;

CREATE TABLE mascota_alergia (                           -- relación N:M entre mascota y alergia
    id             BIGINT       NOT NULL AUTO_INCREMENT,
    id_mascota     BIGINT       NOT NULL,
    id_alergia     BIGINT       NOT NULL,
    observaciones  VARCHAR(255) NULL,
    PRIMARY KEY (id),
    CONSTRAINT uk_mascota_alergia UNIQUE (id_mascota, id_alergia),
    CONSTRAINT fk_mascotaalergia_mascota FOREIGN KEY (id_mascota) REFERENCES mascota (idMascota),
    CONSTRAINT fk_mascotaalergia_alergia FOREIGN KEY (id_alergia) REFERENCES alergia (idAlergia)
) ENGINE=InnoDB;

-- ---------------------------------------------------------------------------
--  CITAS (módulo propio del Grupo 7)
-- ---------------------------------------------------------------------------
CREATE TABLE cita (
    idCita           BIGINT       NOT NULL AUTO_INCREMENT,
    id_mascota       BIGINT       NOT NULL,
    id_veterinario   BIGINT       NOT NULL,
    fechaHora        DATETIME(6)  NOT NULL,
    duracionMinutos  INT          NOT NULL,
    motivo           VARCHAR(255) NOT NULL,
    estado           VARCHAR(15)  NOT NULL,              -- PROGRAMADA | ATENDIDA | CANCELADA
    observaciones    VARCHAR(500) NULL,
    id_historial     BIGINT       NULL,                  -- atención generada al atender la cita
    PRIMARY KEY (idCita),
    KEY idx_cita_fecha  (fechaHora),
    KEY idx_cita_estado (estado),
    CONSTRAINT fk_cita_mascota     FOREIGN KEY (id_mascota)     REFERENCES mascota (idMascota),
    CONSTRAINT fk_cita_veterinario FOREIGN KEY (id_veterinario) REFERENCES veterinario (idVeterinario),
    CONSTRAINT fk_cita_historial   FOREIGN KEY (id_historial)   REFERENCES historial_clinico (idHistorial) ON DELETE SET NULL,
    CONSTRAINT ck_cita_estado   CHECK (estado IN ('PROGRAMADA','ATENDIDA','CANCELADA')),
    CONSTRAINT ck_cita_duracion CHECK (duracionMinutos BETWEEN 10 AND 240)
) ENGINE=InnoDB;

-- ---------------------------------------------------------------------------
--  TRAZABILIDAD: quién hizo qué y cuándo
-- ---------------------------------------------------------------------------
CREATE TABLE auditoria (
    idAuditoria  BIGINT       NOT NULL AUTO_INCREMENT,
    idUsuario    BIGINT       NOT NULL,
    accion       VARCHAR(30)  NOT NULL,                  -- CREAR, ACTUALIZAR, CANCELAR, ATENDER, DESACTIVAR...
    entidad      VARCHAR(100) NOT NULL,
    idRegistro   BIGINT       NULL,
    fechaHora    DATETIME(6)  NOT NULL,
    descripcion  VARCHAR(500) NULL,
    PRIMARY KEY (idAuditoria),
    CONSTRAINT fk_auditoria_usuario FOREIGN KEY (idUsuario) REFERENCES usuario (idUsuario)
) ENGINE=InnoDB;
