package com.mycompany.veterinaria.util;

import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityTransaction;
import java.util.function.Function;

/**
 * Ejecuta trabajo de base de datos con la apertura/cierre de conexión y la transacción resueltas.
 *
 *   Tx.leer(em -> ...)       consulta sin transacción
 *   Tx.ejecutar(em -> ...)   transacción: si algo falla se deshace TODO (rollback), si no se confirma (commit)
 *
 * Una transacción agrupa varias operaciones para que ocurran todas o ninguna (por ejemplo:
 * crear un usuario Y sus roles). Así la base de datos nunca queda a medias.
 */
public final class Tx {

    private Tx() {
    }

    public static <T> T leer(Function<EntityManager, T> consulta) {
        EntityManager em = JPAUtil.getEntityManager();
        try {
            return consulta.apply(em);
        } finally {
            em.close();
        }
    }

    public static <T> T ejecutar(Function<EntityManager, T> trabajo) {
        EntityManager em = JPAUtil.getEntityManager();
        EntityTransaction tx = em.getTransaction();
        try {
            tx.begin();
            T resultado = trabajo.apply(em);
            tx.commit();
            return resultado;
        } catch (RuntimeException e) {
            if (tx.isActive()) {
                tx.rollback();
            }
            throw e;
        } finally {
            em.close();
        }
    }
}
