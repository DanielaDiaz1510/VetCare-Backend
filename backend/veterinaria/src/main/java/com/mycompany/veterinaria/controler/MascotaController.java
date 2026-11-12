package com.mycompany.veterinaria.controler;

import com.mycompany.veterinaria.model.entity.Mascota;
import com.mycompany.veterinaria.service.MascotaService;
import com.mycompany.veterinaria.util.Json;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;

/**
 * /api/mascotas
 *   GET        lista activas   ?q=nombre &especie=Perro &estado=ACTIVO|INACTIVO|TODOS &idPropietario=3   (RF-09)
 *   GET ?id=5  detalle (RF-10)
 *   POST / PUT crear / editar. Cuerpo: {"nombre":"Luna","especie":"Perro","propietario":{"idPropietario":1}, ...}
 *   DELETE ?id=5   DESACTIVA la mascota (solo ADMIN); la historia clínica se conserva
 */
@WebServlet("/api/mascotas")
public class MascotaController extends ApiServlet {

    private final MascotaService service = new MascotaService();

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        Long id = numero(req, "id");
        if (id != null) {
            Json.responder(resp, service.obtenerDTO(id));
        } else {
            Json.responder(resp, service.buscarDTO(texto(req, "q"), texto(req, "especie"),
                    texto(req, "estado"), numero(req, "idPropietario")));
        }
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        Mascota mascota = Json.leer(req, Mascota.class);
        Json.responder(resp, HttpServletResponse.SC_CREATED, service.crear(mascota, actor(req)));
    }

    @Override
    protected void doPut(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        Mascota mascota = Json.leer(req, Mascota.class);
        Json.responder(resp, service.actualizar(mascota, actor(req)));
    }

    @Override
    protected void doDelete(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        service.desactivar(numeroObligatorio(req, "id"), actor(req));
        resp.setStatus(HttpServletResponse.SC_NO_CONTENT);
    }
}
