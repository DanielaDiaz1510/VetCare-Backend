package com.mycompany.veterinaria.service;

import com.mycompany.veterinaria.dao.AuditoriaDAO;
import com.mycompany.veterinaria.model.entity.Auditoria;
import com.mycompany.veterinaria.model.entity.Usuario;
import com.mycompany.veterinaria.util.JPAUtil;
import jakarta.persistence.EntityManager;
import java.time.LocalDateTime;
import java.util.List;

public class AuditoriaService {

    public void registrar(
            Long idUsuario,
            String accion,
            String entidad,
            Long idRegistro,
            String descripcion) {

        EntityManager em = JPAUtil.getEntityManager();

        try {
            Usuario usuario = em.find(Usuario.class, idUsuario);

            if (usuario == null) {
                throw new RuntimeException(
                        "No existe el usuario con ID: " + idUsuario
                );
            }

            Auditoria auditoria = new Auditoria();

            auditoria.setUsuario(usuario);
            auditoria.setAccion(accion);
            auditoria.setEntidad(entidad);
            auditoria.setIdRegistro(idRegistro);
            auditoria.setFechaHora(LocalDateTime.now());
            auditoria.setDescripcion(descripcion);

            new AuditoriaDAO(em).crear(auditoria);

        } finally {
            em.close();
        }
    }

    public List<Auditoria> listarTodos() {
        EntityManager em = JPAUtil.getEntityManager();
        try {
            return new AuditoriaDAO(em).listarTodosConUsuario();
        } finally {
            em.close();
        }
    }

    public List<Auditoria> listarPorUsuario(Long idUsuario) {

        EntityManager em = JPAUtil.getEntityManager();

        try {
            return new AuditoriaDAO(em).listarPorUsuario(idUsuario);

        } finally {
            em.close();
        }
    }

    public List<Auditoria> listarPorEntidad(String entidad) {

        EntityManager em = JPAUtil.getEntityManager();

        try {
            return new AuditoriaDAO(em).listarPorEntidad(entidad);

        } finally {
            em.close();
        }
    }
}
