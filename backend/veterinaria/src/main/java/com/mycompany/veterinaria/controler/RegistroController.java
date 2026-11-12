package com.mycompany.veterinaria.controler;

import com.mycompany.veterinaria.DTO.UsuarioRequestDTO;
import com.mycompany.veterinaria.service.UsuarioService;
import com.mycompany.veterinaria.util.Json;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * POST /api/registro  (ruta pública, RF-01)
 * Crea una cuenta VETERINARIO en estado inactivo. Un administrador debe activarla antes del primer login.
 * Valida correo y usuario únicos y la política de contraseñas.
 */
@WebServlet("/api/registro")
public class RegistroController extends HttpServlet {

    private final UsuarioService service = new UsuarioService();

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        UsuarioRequestDTO datos = Json.leer(req, UsuarioRequestDTO.class);
        Map<String, Object> usuario = service.registrar(datos);

        Map<String, Object> cuerpo = new LinkedHashMap<>();
        cuerpo.put("mensaje", "Registro recibido. Un administrador debe activar su cuenta antes de iniciar sesión");
        cuerpo.put("usuario", usuario);
        Json.responder(resp, HttpServletResponse.SC_CREATED, cuerpo);
    }
}
