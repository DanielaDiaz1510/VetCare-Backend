package com.mycompany.veterinaria.controler;

import com.mycompany.veterinaria.security.TokenRevocados;
import com.mycompany.veterinaria.security.UsuarioAutenticado;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.Map;
import com.mycompany.veterinaria.util.Json;

/**
 * POST /api/logout  (RF-12)
 * Anota el token en la lista de revocados: aunque alguien lo conserve, deja de funcionar.
 * La app Android además debe borrar el token de su almacenamiento local.
 */
@WebServlet("/api/logout")
public class LogoutController extends HttpServlet {

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        UsuarioAutenticado u = UsuarioAutenticado.desde(req);
        if (u != null) {
            TokenRevocados.revocar(u.getJti(), u.getExpiraEpoch());
        }
        Json.responder(resp, Map.of("mensaje", "Sesión cerrada"));
    }
}
