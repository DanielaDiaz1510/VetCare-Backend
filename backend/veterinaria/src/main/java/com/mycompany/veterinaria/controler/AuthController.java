package com.mycompany.veterinaria.controler;

import com.mycompany.veterinaria.DTO.LoginRequestDTO;
import com.mycompany.veterinaria.DTO.UsuarioDTO;
import com.mycompany.veterinaria.security.JwtUtil;
import com.mycompany.veterinaria.service.AuthService;
import com.mycompany.veterinaria.util.Json;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * POST /api/login   (ruta pública)
 * Recibe {"username": "...", "password": "..."}  — "username" acepta el usuario o el correo.
 * Devuelve el token JWT que la app debe guardar y enviar en cada petición:
 *     Authorization: Bearer <token>
 */
@WebServlet("/api/login")
public class AuthController extends HttpServlet {

    private final AuthService authService = new AuthService();

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        LoginRequestDTO login = Json.leer(req, LoginRequestDTO.class);
        UsuarioDTO usuario = authService.login(login.getUsername(), login.getPassword());

        JwtUtil jwt = JwtUtil.instancia();
        String token = jwt.generar(usuario.getId(), usuario.getUsername(), usuario.getNombre(), usuario.getRoles());

        Map<String, Object> cuerpo = new LinkedHashMap<>();
        cuerpo.put("token", token);
        cuerpo.put("tipo", "Bearer");
        cuerpo.put("expiraEnSegundos", jwt.getDuracionSegundos());
        cuerpo.put("usuario", usuario);
        Json.responder(resp, cuerpo);
    }
}
