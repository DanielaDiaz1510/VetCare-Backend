package com.mycompany.veterinaria.service;

import com.mycompany.veterinaria.DTO.MascotaDTO;
import com.mycompany.veterinaria.dao.MascotaDAO;
import com.mycompany.veterinaria.exception.NoEncontradoException;
import com.mycompany.veterinaria.exception.ReglaNegocioException;
import com.mycompany.veterinaria.exception.ValidacionException;
import com.mycompany.veterinaria.model.entity.Cita;
import com.mycompany.veterinaria.model.entity.Mascota;
import com.mycompany.veterinaria.model.entity.Propietario;
import com.mycompany.veterinaria.util.Tx;
import com.mycompany.veterinaria.util.Validador;
import jakarta.persistence.EntityManager;
import java.time.LocalDate;
import java.util.List;

/** Reglas de negocio de las mascotas. Una mascota no se borra: se DESACTIVA para conservar su historia clínica. */
public class MascotaService {

    private final AuditoriaService auditoria = new AuditoriaService();

    /** Mascotas activas (la usan las exportaciones). */
    public List<MascotaDTO> listarTodosDTO() {
        return buscarDTO(null, null, null, null);
    }

    /**
     * RF-09: filtros por nombre (q), especie, estado y propietario.
     * Sin 'estado' se listan solo las ACTIVAS; con estado=TODOS, todas.
     */
    public List<MascotaDTO> buscarDTO(String q, String especie, String estado, Long idPropietario) {
        String texto = vacioANull(q);
        String esp = vacioANull(especie);
        String est = vacioANull(estado);
        if (est == null) {
            est = "ACTIVO";
        } else if ("TODOS".equalsIgnoreCase(est)) {
            est = null;
        } else {
            est = Validador.uno(est, "estado", "ACTIVO", "INACTIVO");
        }
        final String filtroEstado = est;
        return Tx.leer(em -> new MascotaDAO(em).buscar(texto, esp, filtroEstado, idPropietario)
                .stream().map(this::aDTO).toList());
    }

    /** RF-10: detalle. */
    public MascotaDTO obtenerDTO(Long id) {
        return Tx.leer(em -> aDTO(existente(em, id)));
    }

    public Mascota buscarPorId(Long id) {
        return Tx.leer(em -> existente(em, id));
    }

    public MascotaDTO crear(Mascota datos, Long idActor) {
        MascotaDTO creada = Tx.ejecutar(em -> {
            Mascota m = new Mascota();
            copiar(em, datos, m);
            if (m.getEstado() == null) {
                m.setEstado("ACTIVO");
            }
            em.persist(m);
            return aDTO(m);
        });
        auditoria.registrar(idActor, "CREAR", "Mascota", creada.getId(), "Creación de mascota: " + creada.getNombre());
        return creada;
    }

    public MascotaDTO actualizar(Mascota datos, Long idActor) {
        MascotaDTO actualizada = Tx.ejecutar(em -> {
            Mascota actual = existente(em, datos.getIdMascota());
            copiar(em, datos, actual);
            return aDTO(actual);
        });
        auditoria.registrar(idActor, "ACTUALIZAR", "Mascota", actualizada.getId(),
                "Actualización de mascota: " + actualizada.getNombre());
        return actualizada;
    }

    /** "Eliminar" = desactivar (RF-08). Se bloquea si la mascota tiene citas programadas. */
    public void desactivar(Long id, Long idActor) {
        String nombre = Tx.ejecutar(em -> {
            Mascota m = existente(em, id);
            long pendientes = em.createQuery(
                    "SELECT COUNT(c) FROM Cita c WHERE c.mascota.idMascota = :id AND c.estado = :e", Long.class)
                    .setParameter("id", id).setParameter("e", Cita.PROGRAMADA).getSingleResult();
            if (pendientes > 0) {
                throw new ReglaNegocioException("No se puede desactivar: la mascota tiene " + pendientes
                        + " cita(s) programada(s). Cancélelas primero");
            }
            m.setEstado("INACTIVO");
            return m.getNombre();
        });
        auditoria.registrar(idActor, "DESACTIVAR", "Mascota", id, "Desactivación de mascota: " + nombre);
    }

    // ------------------------------------------------------------------ internos

    private Mascota existente(EntityManager em, Long id) {
        Mascota m = id == null ? null : em.find(Mascota.class, id);
        if (m == null) {
            throw new NoEncontradoException("Mascota no encontrada");
        }
        return m;
    }

    private void copiar(EntityManager em, Mascota o, Mascota d) {
        d.setNombre(Validador.requerido(o.getNombre(), "nombre", 100));
        d.setEspecie(Validador.requerido(o.getEspecie(), "especie", 50));
        d.setRaza(Validador.opcional(o.getRaza(), "raza", 80));
        String sexo = Validador.opcional(o.getSexo(), "sexo", 1);
        d.setSexo(sexo == null ? null : Validador.uno(sexo, "sexo", "M", "H"));
        d.setColor(Validador.opcional(o.getColor(), "color", 50));
        if (o.getFechaNacimiento() != null && o.getFechaNacimiento().isAfter(LocalDate.now())) {
            throw new ValidacionException("La fecha de nacimiento no puede estar en el futuro");
        }
        d.setFechaNacimiento(o.getFechaNacimiento());
        Validador.positivo(o.getPesoActual(), "pesoActual");
        d.setPesoActual(o.getPesoActual());
        if (o.getEsterilizado() != null) {
            d.setEsterilizado(o.getEsterilizado());
        }
        if (o.getEstado() != null && !o.getEstado().isBlank()) {
            d.setEstado(Validador.uno(o.getEstado(), "estado", "ACTIVO", "INACTIVO"));
        }
        // El propietario debe existir
        if (o.getPropietario() == null || o.getPropietario().getIdPropietario() == null) {
            throw new ValidacionException("La mascota debe tener un propietario");
        }
        Propietario p = em.find(Propietario.class, o.getPropietario().getIdPropietario());
        if (p == null) {
            throw new NoEncontradoException("Propietario no encontrado");
        }
        d.setPropietario(p);
    }

    private static String vacioANull(String s) {
        return (s == null || s.isBlank()) ? null : s.trim();
    }

    private MascotaDTO aDTO(Mascota m) {
        return new MascotaDTO(m.getIdMascota(), m.getNombre(), m.getEspecie(), m.getRaza(),
                m.getFechaNacimiento(), m.getSexo(), m.getColor(), m.getPesoActual(), m.getEstado(),
                m.getPropietario().getIdPropietario(),
                m.getPropietario().getNombres() + " " + m.getPropietario().getApellidos());
    }
}
