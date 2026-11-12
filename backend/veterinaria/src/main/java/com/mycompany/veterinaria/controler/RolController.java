package com.mycompany.veterinaria.controler;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.mycompany.veterinaria.model.entity.Rol;
import com.mycompany.veterinaria.service.RolService;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;

@WebServlet("/api/roles")
public class RolController extends HttpServlet {

    private final RolService service = new RolService();
    private final ObjectMapper mapper = new ObjectMapper();

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        resp.setContentType("application/json");
        resp.setCharacterEncoding("UTF-8");
        mapper.writeValue(resp.getWriter(), service.listarTodos());
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        Rol rol = mapper.readValue(req.getInputStream(), Rol.class);
        service.crear(rol);
        resp.setStatus(HttpServletResponse.SC_CREATED);
    }

    @Override
    protected void doPut(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        Rol rol = mapper.readValue(req.getInputStream(), Rol.class);
        service.actualizar(rol);
        resp.setStatus(HttpServletResponse.SC_OK);
    }
}
