package com.mycompany.veterinaria.controler;

import com.mycompany.veterinaria.model.entity.Propietario;
import com.mycompany.veterinaria.service.PropietarioService;
import com.mycompany.veterinaria.util.Json;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;

/**
 * /api/propietarios
 *   GET                    lista       ?q=texto &documento=123   filtra por 2 criterios (RF-09)
 *   GET ?id=5              detalle (RF-10)
 *   POST / PUT             crear / editar (devuelven el registro guardado)
 *   DELETE ?id=5           eliminar (solo ADMIN; falla con 409 si tiene mascotas)
 */
@WebServlet("/api/propietarios")
public class PropietarioController extends ApiServlet {

    private final PropietarioService service = new PropietarioService();

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        Long id = numero(req, "id");
        if (id != null) {
            Json.responder(resp, service.obtenerDTO(id));
        } else {
            Json.responder(resp, service.buscarDTO(texto(req, "q"), texto(req, "documento")));
        }
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        Propietario datos = Json.leer(req, Propietario.class);
        Json.responder(resp, HttpServletResponse.SC_CREATED, service.crear(datos, actor(req)));
    }

    @Override
    protected void doPut(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        Propietario datos = Json.leer(req, Propietario.class);
        Json.responder(resp, service.actualizar(datos, actor(req)));
    }

    @Override
    protected void doDelete(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        service.eliminar(numeroObligatorio(req, "id"), actor(req));
        resp.setStatus(HttpServletResponse.SC_NO_CONTENT);
    }
}
