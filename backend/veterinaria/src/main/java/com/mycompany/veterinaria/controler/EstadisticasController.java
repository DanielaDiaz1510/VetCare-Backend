package com.mycompany.veterinaria.controler;

import com.mycompany.veterinaria.model.entity.Cita;
import com.mycompany.veterinaria.util.Json;
import com.mycompany.veterinaria.util.Tx;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.time.LocalDate;
import java.util.LinkedHashMap;
import java.util.Map;

/** GET /api/estadisticas : contadores para la pantalla Home (solo cuentan registros activos). */
@WebServlet("/api/estadisticas")
public class EstadisticasController extends ApiServlet {

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        LocalDate hoy = LocalDate.now();
        Map<String, Long> r = Tx.leer(em -> {
            Map<String, Long> m = new LinkedHashMap<>();
            m.put("mascotas", em.createQuery("SELECT COUNT(x) FROM Mascota x WHERE x.estado = 'ACTIVO'", Long.class).getSingleResult());
            m.put("propietarios", em.createQuery("SELECT COUNT(x) FROM Propietario x", Long.class).getSingleResult());
            m.put("veterinarios", em.createQuery("SELECT COUNT(x) FROM Veterinario x WHERE x.estado = 'ACTIVO'", Long.class).getSingleResult());
            m.put("citasProgramadas", em.createQuery("SELECT COUNT(c) FROM Cita c WHERE c.estado = :e", Long.class)
                    .setParameter("e", Cita.PROGRAMADA).getSingleResult());
            m.put("citasHoy", em.createQuery("SELECT COUNT(c) FROM Cita c WHERE c.estado = :e AND c.fechaHora >= :a AND c.fechaHora < :b", Long.class)
                    .setParameter("e", Cita.PROGRAMADA).setParameter("a", hoy.atStartOfDay())
                    .setParameter("b", hoy.plusDays(1).atStartOfDay()).getSingleResult());
            m.put("vacunasPorVencer30Dias", em.createQuery(
                    "SELECT COUNT(v) FROM Vacunacion v WHERE v.fechaProxima >= :a AND v.fechaProxima <= :b", Long.class)
                    .setParameter("a", hoy).setParameter("b", hoy.plusDays(30)).getSingleResult());
            return m;
        });
        Json.responder(resp, r);
    }
}
