package com.mycompany.veterinaria.controler;

import com.mycompany.veterinaria.DTO.UsuarioRequestDTO;
import com.mycompany.veterinaria.security.UsuarioAutenticado;
import com.mycompany.veterinaria.service.UsuarioService;
import com.mycompany.veterinaria.util.Json;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;

/**
 * /api/usuarios  — solo ADMIN (lo exige el filtro de seguridad).
 *   GET            lista de usuarios         GET ?id=5   detalle de un usuario
 *   POST           crear usuario con roles   PUT         editar / activar / desactivar / cambiar roles
 */
@WebServlet("/api/usuarios")
public class UsuarioController extends HttpServlet {

    private final UsuarioService service = new UsuarioService();

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        String id = req.getParameter("id");
        if (id != null) {
            Json.responder(resp, service.obtenerPorId(Long.valueOf(id)));
        } else {
            Json.responder(resp, service.listarTodosParaVista());
        }
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        UsuarioRequestDTO datos = Json.leer(req, UsuarioRequestDTO.class);
        Json.responder(resp, HttpServletResponse.SC_CREATED,
                service.crear(datos, UsuarioAutenticado.desde(req).getId()));
    }

    @Override
    protected void doPut(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        UsuarioRequestDTO datos = Json.leer(req, UsuarioRequestDTO.class);
        service.actualizar(datos, UsuarioAutenticado.desde(req).getId());
        resp.setStatus(HttpServletResponse.SC_OK);
    }
}
