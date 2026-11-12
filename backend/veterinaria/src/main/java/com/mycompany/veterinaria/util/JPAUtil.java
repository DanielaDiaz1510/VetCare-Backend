package com.mycompany.veterinaria.util;

import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityManagerFactory;
import jakarta.persistence.Persistence;
import java.util.HashMap;
import java.util.Map;

/**
 * Crea una única fábrica de conexiones (EntityManagerFactory) para toda la aplicación.
 *
 * Los valores de persistence.xml sirven para desarrollo (XAMPP: root sin contraseña).
 * Para otro entorno se pueden sobrescribir SIN tocar el código con:
 *   DB_URL, DB_USER, DB_PASSWORD y DB_HBM2DDL (none | validate | update | create).
 */
public class JPAUtil {

    private static final EntityManagerFactory emf = crearFabrica();

    private static EntityManagerFactory crearFabrica() {
        Map<String, Object> props = new HashMap<>();
        poner(props, "jakarta.persistence.jdbc.url", Config.get("DB_URL", null));
        poner(props, "jakarta.persistence.jdbc.user", Config.get("DB_USER", null));
        poner(props, "jakarta.persistence.jdbc.password", Config.get("DB_PASSWORD", null));
        poner(props, "hibernate.hbm2ddl.auto", Config.get("DB_HBM2DDL", null));
        return Persistence.createEntityManagerFactory("VeterinariaPU", props);
    }

    private static void poner(Map<String, Object> props, String clave, String valor) {
        if (valor != null) {
            props.put(clave, valor);
        }
    }

    public static EntityManager getEntityManager() {
        return emf.createEntityManager();
    }

    /** Se llama al detener la aplicación para liberar las conexiones. */
    public static void cerrar() {
        if (emf.isOpen()) {
            emf.close();
        }
    }
}
