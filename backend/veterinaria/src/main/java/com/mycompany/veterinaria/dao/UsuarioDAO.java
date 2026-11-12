
package com.mycompany.veterinaria.dao;

import com.mycompany.veterinaria.model.entity.Usuario;
import jakarta.persistence.EntityManager;

public class UsuarioDAO extends GenericDAO<Usuario, Long> {

    public UsuarioDAO(EntityManager em) {
        super(em, Usuario.class);
    }

    public Usuario buscarPorUsername(String username) {
        return em.createQuery(
                "SELECT u FROM Usuario u WHERE u.username = :user AND u.estado = true",
                Usuario.class
        ).setParameter("user", username)
         .getResultStream()
         .findFirst()
         .orElse(null);
    }

    /** Busca por usuario O por correo (sin importar mayúsculas) y SIN filtrar por estado. */
    public Usuario buscarPorIdentificador(String identificador) {
        return em.createQuery(
                "SELECT u FROM Usuario u WHERE LOWER(u.username) = :id OR LOWER(u.email) = :id",
                Usuario.class
        ).setParameter("id", identificador.trim().toLowerCase())
         .setMaxResults(1)
         .getResultStream()
         .findFirst()
         .orElse(null);
    }

    /** ¿Existe otro usuario (distinto de excluirId) con ese username? */
    public boolean existeUsername(String username, Long excluirId) {
        return contar("LOWER(u.username) = :v", username.toLowerCase(), excluirId) > 0;
    }

    /** ¿Existe otro usuario (distinto de excluirId) con ese correo? */
    public boolean existeEmail(String email, Long excluirId) {
        return contar("LOWER(u.email) = :v", email.toLowerCase(), excluirId) > 0;
    }

    private long contar(String condicion, String valor, Long excluirId) {
        return em.createQuery(
                "SELECT COUNT(u) FROM Usuario u WHERE " + condicion
                + " AND (:excluir IS NULL OR u.idUsuario <> :excluir)", Long.class)
                .setParameter("v", valor)
                .setParameter("excluir", excluirId)
                .getSingleResult();
    }
}
