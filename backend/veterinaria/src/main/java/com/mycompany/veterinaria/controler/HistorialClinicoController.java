package com.mycompany.veterinaria.controler;

import com.mycompany.veterinaria.DTO.HistorialClinicoDTO;
import com.mycompany.veterinaria.service.HistorialClinicoService;
import com.mycompany.veterinaria.util.Json;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;

/**
 * /api/historiales  — historia clínica
 *   GET ?idMascota=2 &idVeterinario=1   lista de atenciones (filtros opcionales)
 *   GET ?id=7                           detalle con sus tratamientos
 *   POST / PUT                          registrar / editar una atención (un VETERINARIO solo edita las suyas)
 *   DELETE ?id=7                        solo ADMIN
 */
@WebServlet("/api/historiales")
public class HistorialClinicoController extends ApiServlet {

    private final HistorialClinicoService service = new HistorialClinicoService();

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        Long id = numero(req, "id");
        if (id != null) {
            Json.responder(resp, service.detalle(id));
        } else {
            Json.responder(resp, service.listar(numero(req, "idMascota"), numero(req, "idVeterinario")));
        }
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        Json.responder(resp, HttpServletResponse.SC_CREATED,
                service.crear(Json.leer(req, HistorialClinicoDTO.class), usuario(req)));
    }

    @Override
    protected void doPut(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        Json.responder(resp, service.actualizar(Json.leer(req, HistorialClinicoDTO.class), usuario(req)));
    }

    @Override
    protected void doDelete(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        service.eliminar(numeroObligatorio(req, "id"), actor(req));
        resp.setStatus(HttpServletResponse.SC_NO_CONTENT);
    }
}
