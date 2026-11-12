package com.mycompany.veterinaria.controler;

import com.mycompany.veterinaria.DTO.MedicamentoDTO;
import com.mycompany.veterinaria.service.MedicamentoService;
import com.mycompany.veterinaria.util.Json;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;

/**
 * /api/medicamentos  — catálogo de medicamentos
 *   GET            lista (?q=texto filtra por nombre)     GET ?id=3   detalle
 *   POST / PUT     crear / editar                          DELETE ?id=3   eliminar (solo ADMIN)
 */
@WebServlet("/api/medicamentos")
public class MedicamentoController extends ApiServlet {

    private final MedicamentoService service = new MedicamentoService();

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        Long id = numero(req, "id");
        Json.responder(resp, id != null ? (Object) service.obtener(id) : service.listar(texto(req, "q")));
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        Json.responder(resp, HttpServletResponse.SC_CREATED, service.crear(Json.leer(req, MedicamentoDTO.class), actor(req)));
    }

    @Override
    protected void doPut(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        Json.responder(resp, service.actualizar(Json.leer(req, MedicamentoDTO.class), actor(req)));
    }

    @Override
    protected void doDelete(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        service.eliminar(numeroObligatorio(req, "id"), actor(req));
        resp.setStatus(HttpServletResponse.SC_NO_CONTENT);
    }
}
