package com.mycompany.veterinaria.controler;

import com.mycompany.veterinaria.security.UsuarioAutenticado;
import com.mycompany.veterinaria.service.UsuarioService;
import com.mycompany.veterinaria.util.Json;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;

/** GET /api/me : perfil del usuario autenticado (RF-04). Alimenta la pantalla Home de la app. */
@WebServlet("/api/me")
public class MeController extends HttpServlet {

    private final UsuarioService service = new UsuarioService();

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        Json.responder(resp, service.perfil(UsuarioAutenticado.desde(req).getId()));
    }
}
