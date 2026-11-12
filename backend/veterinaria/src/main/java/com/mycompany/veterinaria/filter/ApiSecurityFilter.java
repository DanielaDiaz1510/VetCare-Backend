package com.mycompany.veterinaria.filter;

import com.mycompany.veterinaria.security.JwtException;
import com.mycompany.veterinaria.service.AuthService;
import com.mycompany.veterinaria.security.JwtUtil;
import com.mycompany.veterinaria.security.Permisos;
import com.mycompany.veterinaria.security.TokenRevocados;
import com.mycompany.veterinaria.security.UsuarioAutenticado;
import com.mycompany.veterinaria.util.ErrorApi;
import jakarta.servlet.Filter;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletRequest;
import jakarta.servlet.ServletResponse;
import jakarta.servlet.annotation.WebFilter;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

/**
 * Guardia de la API. TODA petición a /api/* pasa por aquí antes de llegar a un controlador:
 *
 *   1. ¿La ruta es pública (login/registro)?       -> pasa sin token.
 *   2. ¿Trae "Authorization: Bearer <token>"?      -> si no, 401.
 *   3. ¿El token tiene firma válida y no expiró?   -> si no, 401 (TOKEN_INVALIDO / TOKEN_EXPIRADO).
 *   4. ¿El usuario cerró sesión con ese token?     -> si sí, 401 (TOKEN_REVOCADO).
 *   5. ¿La cuenta sigue activa en la BD?           -> si no, 401 (CUENTA_INACTIVA).
 *   6. ¿Su rol permite este método y esta ruta?    -> si no, 403.
 *   7. Todo bien: deja el usuario en la petición y continúa hacia el controlador.
 *
 * Además captura cualquier excepción que lance un controlador y la convierte en JSON (ErrorApi).
 * La validación ocurre SIEMPRE en el servidor: no depende de lo que haga la app Android.
 */
@WebFilter(urlPatterns = "/api/*")
public class ApiSecurityFilter implements Filter {

    private final AuthService authService = new AuthService();

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain) {
        HttpServletRequest req = (HttpServletRequest) request;
        HttpServletResponse resp = (HttpServletResponse) response;

        try {
            resp.setHeader("Cache-Control", "no-store");
            resp.setHeader("X-Content-Type-Options", "nosniff");

            String metodo = req.getMethod();
            // servletPath + pathInfo = ruta ya decodificada y normalizada por el contenedor
            // (ignora ";jsessionid=..." y "../", que podrían usarse para saltarse la comprobación).
            String ruta = req.getServletPath() + (req.getPathInfo() == null ? "" : req.getPathInfo());

            if (!Permisos.esPublica(metodo, ruta)) {
                String token = JwtUtil.extraerBearer(req.getHeader("Authorization"));
                if (token == null) {
                    resp.setHeader("WWW-Authenticate", "Bearer");
                    ErrorApi.responder(req, resp, 401, "NO_AUTENTICADO",
                            "Debe iniciar sesión: falta el encabezado Authorization: Bearer <token>");
                    return;
                }

                UsuarioAutenticado usuario;
                try {
                    usuario = JwtUtil.instancia().validar(token);
                } catch (JwtException e) {
                    resp.setHeader("WWW-Authenticate", "Bearer error=\"invalid_token\"");
                    ErrorApi.responder(req, resp, 401, e.getCodigo(), e.getMessage());
                    return;
                }

                if (TokenRevocados.estaRevocado(usuario.getJti())) {
                    resp.setHeader("WWW-Authenticate", "Bearer error=\"invalid_token\"");
                    ErrorApi.responder(req, resp, 401, "TOKEN_REVOCADO", "La sesión fue cerrada. Inicie sesión de nuevo");
                    return;
                }

                if (!authService.cuentaActiva(usuario.getId())) {
                    ErrorApi.responder(req, resp, 401, "CUENTA_INACTIVA", "La cuenta fue desactivada o eliminada");
                    return;
                }

                if (!Permisos.permite(metodo, ruta, usuario.getRoles())) {
                    ErrorApi.responder(req, resp, 403, "ACCESO_DENEGADO",
                            "No tiene permisos para realizar esta operación");
                    return;
                }
                req.setAttribute(UsuarioAutenticado.ATRIBUTO, usuario);
            }

            chain.doFilter(request, response);

        } catch (Exception e) {
            try {
                ErrorApi.manejar(e, req, resp);
            } catch (Exception ignorada) {
                // la respuesta ya no se puede escribir (conexión cerrada)
            }
        }
    }
}
