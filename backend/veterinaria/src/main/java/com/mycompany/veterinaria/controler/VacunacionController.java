package com.mycompany.veterinaria.controler;

import com.mycompany.veterinaria.DTO.VacunacionDTO;
import com.mycompany.veterinaria.service.VacunacionService;
import com.mycompany.veterinaria.util.Json;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;

/**
 * /api/vacunaciones  — carnet de vacunación
 *   GET ?idMascota=2 &proximasDias=30    lista (ambos filtros opcionales; proximasDias = refuerzos que vencen pronto)
 *   GET ?id=4                            detalle
 *   POST / PUT                           registrar / editar          DELETE ?id=4   solo ADMIN
 */
@WebServlet("/api/vacunaciones")
public class VacunacionController extends ApiServlet {

    private final VacunacionService service = new VacunacionService();

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        Long id = numero(req, "id");
        if (id != null) {
            Json.responder(resp, service.obtener(id));
        } else {
            Long dias = numero(req, "proximasDias");
            Json.responder(resp, service.listar(numero(req, "idMascota"), dias == null ? null : dias.intValue()));
        }
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        Json.responder(resp, HttpServletResponse.SC_CREATED, service.crear(Json.leer(req, VacunacionDTO.class), actor(req)));
    }

    @Override
    protected void doPut(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        Json.responder(resp, service.actualizar(Json.leer(req, VacunacionDTO.class), actor(req)));
    }

    @Override
    protected void doDelete(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        service.eliminar(numeroObligatorio(req, "id"), actor(req));
        resp.setStatus(HttpServletResponse.SC_NO_CONTENT);
    }
}
