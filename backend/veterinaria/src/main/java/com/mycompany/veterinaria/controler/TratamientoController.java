package com.mycompany.veterinaria.controler;

import com.mycompany.veterinaria.DTO.TratamientoDTO;
import com.mycompany.veterinaria.service.TratamientoService;
import com.mycompany.veterinaria.util.Json;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;

/**
 * /api/tratamientos  — medicamentos recetados en una atención
 *   GET ?idHistorial=7   lista de la atención      GET ?id=3   detalle
 *   POST / PUT           crear / editar            DELETE ?id=3   solo ADMIN
 */
@WebServlet("/api/tratamientos")
public class TratamientoController extends ApiServlet {

    private final TratamientoService service = new TratamientoService();

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        Long id = numero(req, "id");
        if (id != null) {
            Json.responder(resp, service.obtener(id));
        } else {
            Json.responder(resp, service.listarPorHistorial(numeroObligatorio(req, "idHistorial")));
        }
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        Json.responder(resp, HttpServletResponse.SC_CREATED,
                service.crear(Json.leer(req, TratamientoDTO.class), usuario(req)));
    }

    @Override
    protected void doPut(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        Json.responder(resp, service.actualizar(Json.leer(req, TratamientoDTO.class), usuario(req)));
    }

    @Override
    protected void doDelete(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        service.eliminar(numeroObligatorio(req, "id"), actor(req));
        resp.setStatus(HttpServletResponse.SC_NO_CONTENT);
    }
}
