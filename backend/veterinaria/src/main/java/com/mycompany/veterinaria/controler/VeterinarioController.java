package com.mycompany.veterinaria.controler;

import com.mycompany.veterinaria.DTO.VeterinarioDTO;
import com.mycompany.veterinaria.service.VeterinarioService;
import com.mycompany.veterinaria.util.Json;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;

/**
 * /api/veterinarios
 *   GET (ADMIN y VETERINARIO)   ?q=texto &estado=ACTIVO|INACTIVO|TODOS    GET ?id=5 detalle
 *   POST / PUT / DELETE         solo ADMIN (DELETE desactiva)
 */
@WebServlet("/api/veterinarios")
public class VeterinarioController extends ApiServlet {

    private final VeterinarioService service = new VeterinarioService();

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        Long id = numero(req, "id");
        if (id != null) {
            Json.responder(resp, service.obtenerDTO(id));
        } else {
            Json.responder(resp, service.buscarDTO(texto(req, "q"), texto(req, "estado")));
        }
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        Json.responder(resp, HttpServletResponse.SC_CREATED,
                service.crear(Json.leer(req, VeterinarioDTO.class), actor(req)));
    }

    @Override
    protected void doPut(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        Json.responder(resp, service.actualizar(Json.leer(req, VeterinarioDTO.class), actor(req)));
    }

    @Override
    protected void doDelete(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        service.desactivar(numeroObligatorio(req, "id"), actor(req));
        resp.setStatus(HttpServletResponse.SC_NO_CONTENT);
    }
}
