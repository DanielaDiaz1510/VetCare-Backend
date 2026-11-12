# 🐾 VetCare – API REST para clínicas veterinarias

API en **Java 17** para gestionar propietarios, mascotas, citas, historias clínicas, tratamientos y vacunación. La usan un panel web y una app Android, y cada rol tiene sus propios permisos.

*Proyecto final de la materia de Backend – Fundación Universitaria Compensar.*

![Documentación Swagger de la API](docs/capturas/swagger.png)

## 🛠️ Tecnologías

Java 17 · Jakarta EE (Servlets) · Tomcat 10 · JPA/Hibernate · MySQL · JWT · BCrypt · Apache POI · iText · JUnit 5 · Swagger · Maven

## ✨ Funcionalidades

- **Seguridad:** inicio de sesión con JWT, contraseñas cifradas con BCrypt y bloqueo por intentos fallidos.
- **Roles:** ADMIN puede hacer todo; VETERINARIO solo gestiona sus propias citas e historias clínicas.
- **Gestión clínica:** citas (agendar, cancelar y atender), historia clínica, tratamientos, vacunación y alergias.
- **Administración:** auditoría de acciones, respaldo de la base de datos y reportes en Excel y PDF.
- **Arquitectura por capas:** filtro de seguridad → controladores → servicios → DAO → entidades, con 29 rutas documentadas en Swagger.

## 🚀 Cómo ejecutarlo

1. Crea la base de datos con `database/schema.sql` y carga los datos de ejemplo con `database/seed.sql`.
2. Define `JWT_SECRET` (mínimo 32 caracteres) y los datos de MySQL: `DB_URL`, `DB_USER` y `DB_PASSWORD`.
3. Compila con `mvn clean package` dentro de `backend/veterinaria` y despliega `veterinaria.war` en Tomcat 10.
4. Abre http://localhost:8080/veterinaria/ para el panel web o http://localhost:8080/veterinaria/swagger/ para la documentación.

Cuentas de prueba: `admin / admin123` y `veterinario / vet12345`.

Pruebas: `mvn test`. El resumen técnico está en [`documentacion/Resumen_Backend.md`](documentacion/Resumen_Backend.md).

---

👩‍💻 **Daniela Díaz Falla** · [LinkedIn](https://www.linkedin.com/in/daniela-diaz-517538385/) · [Portafolio](https://danieladiaz1510.github.io/Ejemplo_Portafolio/)
