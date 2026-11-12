package com.mycompany.veterinaria.service;

import com.mycompany.veterinaria.DTO.PropietarioDTO;
import com.mycompany.veterinaria.dao.PropietarioDAO;
import com.mycompany.veterinaria.exception.NoEncontradoException;
import com.mycompany.veterinaria.exception.ReglaNegocioException;
import com.mycompany.veterinaria.model.entity.Propietario;
import com.mycompany.veterinaria.util.Tx;
import com.mycompany.veterinaria.util.Validador;
import java.util.List;

/** Reglas de negocio de los propietarios (dueños de las mascotas). */
public class PropietarioService {

    private final AuditoriaService auditoria = new AuditoriaService();

    public List<PropietarioDTO> listarTodosDTO() {
        return buscarDTO(null, null);
    }

    /** RF-09: filtra por texto (nombre/apellido) y/o por documento. */
    public List<PropietarioDTO> buscarDTO(String q, String documento) {
        String texto = (q == null || q.isBlank()) ? null : q.trim();
        String doc = (documento == null || documento.isBlank()) ? null : documento.trim();
        return Tx.leer(em -> new PropietarioDAO(em).buscar(texto, doc).stream().map(this::aDTO).toList());
    }

    /** RF-10: detalle de un propietario. */
    public PropietarioDTO obtenerDTO(Long id) {
        return Tx.leer(em -> aDTO(buscar(em.find(Propietario.class, id))));
    }

    public PropietarioDTO crear(Propietario datos, Long idActor) {
        PropietarioDTO creado = Tx.ejecutar(em -> {
            Propietario p = new Propietario();
            copiar(datos, p);
            if (new PropietarioDAO(em).existeDocumento(p.getTipoDocumento(), p.getNumeroDocumento(), null)) {
                throw new ReglaNegocioException("Ya existe un propietario con el documento "
                        + p.getTipoDocumento() + " " + p.getNumeroDocumento());
            }
            em.persist(p);
            return aDTO(p);
        });
        auditoria.registrar(idActor, "CREAR", "Propietario", creado.getId(),
                "Creación de propietario: " + creado.getNombres() + " " + creado.getApellidos());
        return creado;
    }

    public PropietarioDTO actualizar(Propietario datos, Long idActor) {
        PropietarioDTO actualizado = Tx.ejecutar(em -> {
            Propietario actual = buscar(datos.getIdPropietario() == null ? null : em.find(Propietario.class, datos.getIdPropietario()));
            copiar(datos, actual);
            if (new PropietarioDAO(em).existeDocumento(actual.getTipoDocumento(), actual.getNumeroDocumento(), actual.getIdPropietario())) {
                throw new ReglaNegocioException("Ya existe otro propietario con el documento "
                        + actual.getTipoDocumento() + " " + actual.getNumeroDocumento());
            }
            return aDTO(actual);
        });
        auditoria.registrar(idActor, "ACTUALIZAR", "Propietario", actualizado.getId(),
                "Actualización de propietario: " + actualizado.getNombres() + " " + actualizado.getApellidos());
        return actualizado;
    }

    /** Solo se elimina si no tiene mascotas: las mascotas y su historia clínica nunca se borran en cascada. */
    public void eliminar(Long id, Long idActor) {
        String nombre = Tx.ejecutar(em -> {
            Propietario p = buscar(em.find(Propietario.class, id));
            long mascotas = new PropietarioDAO(em).contarMascotas(id);
            if (mascotas > 0) {
                throw new ReglaNegocioException("No se puede eliminar: el propietario tiene " + mascotas
                        + " mascota(s) registrada(s)");
            }
            String n = p.getNombres() + " " + p.getApellidos();
            em.remove(p);
            return n;
        });
        auditoria.registrar(idActor, "ELIMINAR", "Propietario", id, "Eliminación de propietario: " + nombre);
    }

    // ------------------------------------------------------------------ internos

    private Propietario buscar(Propietario p) {
        if (p == null) {
            throw new NoEncontradoException("Propietario no encontrado");
        }
        return p;
    }

    /** Valida los datos recibidos y los copia a la entidad (solo campos permitidos). */
    private void copiar(Propietario origen, Propietario destino) {
        destino.setTipoDocumento(Validador.uno(origen.getTipoDocumento(), "tipoDocumento", "CC", "TI", "CE", "PAS", "NIT"));
        destino.setNumeroDocumento(Validador.documento(origen.getNumeroDocumento()));
        destino.setNombres(Validador.requerido(origen.getNombres(), "nombres", 100));
        destino.setApellidos(Validador.requerido(origen.getApellidos(), "apellidos", 100));
        destino.setTelefono(Validador.telefonoOpcional(origen.getTelefono()));
        destino.setEmail(Validador.emailOpcional(origen.getEmail()));
        destino.setDireccion(Validador.opcional(origen.getDireccion(), "direccion", 200));
    }

    private PropietarioDTO aDTO(Propietario p) {
        return new PropietarioDTO(p.getIdPropietario(), p.getTipoDocumento(), p.getNumeroDocumento(),
                p.getNombres(), p.getApellidos(), p.getTelefono(), p.getEmail(), p.getDireccion());
    }
}
