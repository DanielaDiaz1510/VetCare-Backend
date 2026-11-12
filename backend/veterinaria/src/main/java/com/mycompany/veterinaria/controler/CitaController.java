package com.mycompany.veterinaria.controler;

import com.mycompany.veterinaria.DTO.AtenderCitaDTO;
import com.mycompany.veterinaria.DTO.CitaDTO;
import com.mycompany.veterinaria.exception.NoEncontradoException;
import com.mycompany.veterinaria.exception.ValidacionException;
import com.mycompany.veterinaria.service.CitaService;
import com.mycompany.veterinaria.util.Json;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.Map;

/**
 * /api/citas
 *   GET                      lista   ?estado=PROGRAMADA &idVeterinario=1 &idMascota=2 &fecha=2026-11-20   (RF-09)
 *   GET  /api/citas/5        detalle (RF-10)
 *   POST /api/citas          agendar. Cuerpo: {"idMascota":1,"idVeterinario":1,"fechaHora":"2026-11-20T09:00:00","motivo":"Control"}
 *   PUT  /api/citas          reprogramar / editar (solo citas PROGRAMADAS)
 *   POST /api/citas/5/cancelar   cuerpo opcional {"motivo":"..."}
 *   POST /api/citas/5/atender    cuerpo opcional {"diagnostico":"...","peso":12.5,"temperatura":38.5}
 */
@WebServlet({"/api/citas", "/api/citas/*"})
public class CitaController extends ApiServlet {

    private final CitaService service = new CitaService();

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        String[] partes = subruta(req);
        if (partes.length == 1) {
            Json.responder(resp, service.obtenerDTO(idDe(partes[0])));
            return;
        }
        if (partes.length > 1) {
            throw new NoEncontradoException("Ruta no encontrada");
        }
        Json.responder(resp, service.buscarDTO(texto(req, "estado"), numero(req, "idVeterinario"),
                numero(req, "idMascota"), fecha(req, "fecha")));
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        String[] partes = subruta(req);
        if (partes.length == 0) {
            Json.responder(resp, HttpServletResponse.SC_CREATED, service.crear(Json.leer(req, CitaDTO.class), usuario(req)));
        } else if (partes.length == 2 && partes[1].equals("cancelar")) {
            Map<?, ?> cuerpo = cuerpoOpcional(req, Map.class);
            String motivo = cuerpo == null || cuerpo.get("motivo") == null ? null : cuerpo.get("motivo").toString();
            Json.responder(resp, service.cancelar(idDe(partes[0]), motivo, usuario(req)));
        } else if (partes.length == 2 && partes[1].equals("atender")) {
            Json.responder(resp, service.atender(idDe(partes[0]), cuerpoOpcional(req, AtenderCitaDTO.class), usuario(req)));
        } else {
            throw new NoEncontradoException("Ruta no encontrada");
        }
    }

    @Override
    protected void doPut(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        Json.responder(resp, service.actualizar(Json.leer(req, CitaDTO.class), usuario(req)));
    }

    // ------------------------------------------------------------------ utilidades

    /** Segmentos después de /api/citas: "/5/cancelar" -> ["5","cancelar"]. */
    private static String[] subruta(HttpServletRequest req) {
        String info = req.getPathInfo();
        if (info == null || info.equals("/")) {
            return new String[0];
        }
        return info.substring(1).split("/");
    }

    private static Long idDe(String texto) {
        try {
            return Long.valueOf(texto);
        } catch (NumberFormatException e) {
            throw new ValidacionException("El identificador de la cita debe ser un número");
        }
    }

    /** Lee el cuerpo si lo hay; si está vacío devuelve null (cancelar/atender no lo exigen). */
    private static <T> T cuerpoOpcional(HttpServletRequest req, Class<T> clase) throws IOException {
        if (req.getContentLengthLong() == 0) {
            return null;
        }
        byte[] bytes = req.getInputStream().readAllBytes();
        return bytes.length == 0 ? null : Json.MAPPER.readValue(bytes, clase);
    }
}
