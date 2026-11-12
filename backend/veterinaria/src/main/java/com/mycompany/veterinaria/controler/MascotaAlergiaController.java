package com.mycompany.veterinaria.controler;

import com.mycompany.veterinaria.DTO.MascotaAlergiaDTO;
import com.mycompany.veterinaria.service.MascotaAlergiaService;
import com.mycompany.veterinaria.util.Json;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;

/**
 * /api/mascota-alergias  — antecedentes alérgicos de una mascota
 *   GET ?idMascota=2   lista      POST   registrar {"idMascota":2,"idAlergia":1,"observaciones":"..."}
 *   DELETE ?id=5       solo ADMIN
 */
@WebServlet("/api/mascota-alergias")
public class MascotaAlergiaController extends ApiServlet {

    private final MascotaAlergiaService service = new MascotaAlergiaService();

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        Json.responder(resp, service.listarPorMascota(numeroObligatorio(req, "idMascota")));
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        Json.responder(resp, HttpServletResponse.SC_CREATED, service.crear(Json.leer(req, MascotaAlergiaDTO.class), actor(req)));
    }

    @Override
    protected void doDelete(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        service.eliminar(numeroObligatorio(req, "id"), actor(req));
        resp.setStatus(HttpServletResponse.SC_NO_CONTENT);
    }
}
