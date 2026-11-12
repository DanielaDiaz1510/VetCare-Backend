package com.mycompany.veterinaria.filter;

import jakarta.servlet.Filter;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.ServletRequest;
import jakarta.servlet.ServletResponse;
import jakarta.servlet.annotation.WebFilter;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;

/**
 * Evita que el navegador reutilice copias viejas del panel web (HTML y JavaScript).
 * Sin esto, tras actualizar la aplicación el navegador podía seguir usando un auth.js antiguo
 * que no guarda el token, y el panel devolvía al login justo después de entrar.
 */
@WebFilter(urlPatterns = {"*.html", "/js/*", "/css/*"})
public class NoCacheFilter implements Filter {

    @Override
    public void doFilter(ServletRequest req, ServletResponse resp, FilterChain chain) throws IOException, ServletException {
        HttpServletResponse r = (HttpServletResponse) resp;
        r.setHeader("Cache-Control", "no-cache, no-store, must-revalidate");
        r.setHeader("Pragma", "no-cache");
        r.setDateHeader("Expires", 0);
        chain.doFilter(req, resp);
    }
}
