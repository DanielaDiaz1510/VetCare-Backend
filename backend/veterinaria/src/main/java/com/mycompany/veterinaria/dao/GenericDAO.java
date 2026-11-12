package com.mycompany.veterinaria.dao;

import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityTransaction;
import java.util.List;

/**
 * DAO genérico con las 5 operaciones básicas (CRUD) sobre cualquier entidad.
 * Cada operación de escritura corre en su propia transacción y, si algo falla,
 * hace rollback para no dejar la base de datos a medias.
 */
public abstract class GenericDAO<T, ID> {

    protected EntityManager em;
    private Class<T> entityClass;

    protected GenericDAO(EntityManager em, Class<T> entityClass) {
        this.em = em;
        this.entityClass = entityClass;
    }

    public void crear(T entidad) {
        EntityTransaction tx = em.getTransaction();
        try {
            tx.begin();
            em.persist(entidad);
            tx.commit();
        } catch (RuntimeException e) {
            if (tx.isActive()) {
                tx.rollback();
            }
            throw e;
        }
    }

    public T buscarPorId(ID id) {
        return em.find(entityClass, id);
    }

    public List<T> listarTodos() {
        return em.createQuery(
                "SELECT e FROM " + entityClass.getSimpleName() + " e",
                entityClass
        ).getResultList();
    }

    public void actualizar(T entidad) {
        EntityTransaction tx = em.getTransaction();
        try {
            tx.begin();
            em.merge(entidad);
            tx.commit();
        } catch (RuntimeException e) {
            if (tx.isActive()) {
                tx.rollback();
            }
            throw e;
        }
    }

    public void eliminar(ID id) {
        EntityTransaction tx = em.getTransaction();
        try {
            tx.begin();
            T entidad = buscarPorId(id);
            if (entidad != null) {
                em.remove(entidad);
            }
            tx.commit();
        } catch (RuntimeException e) {
            if (tx.isActive()) {
                tx.rollback();
            }
            throw e;
        }
    }
}
