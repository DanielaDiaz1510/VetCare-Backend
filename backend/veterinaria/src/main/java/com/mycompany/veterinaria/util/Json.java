package com.mycompany.veterinaria.util;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;

/**
 * Un único ObjectMapper (conversor objeto <-> JSON) configurado para toda la API.
 *  - Las fechas viajan como texto ISO-8601: "2026-11-10" o "2026-11-10T09:30:00".
 *  - Los campos desconocidos se ignoran en lugar de romper la petición.
 */
public final class Json {

    public static final ObjectMapper MAPPER = new ObjectMapper()
            .registerModule(new JavaTimeModule())
            .disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS)
            .disable(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES);

    private Json() {
    }

    /** Lee el cuerpo JSON de la petición y lo convierte a la clase indicada. */
    public static <T> T leer(HttpServletRequest req, Class<T> clase) throws IOException {
        return MAPPER.readValue(req.getInputStream(), clase);
    }

    /** Escribe una respuesta JSON con el código HTTP indicado. */
    public static void responder(HttpServletResponse resp, int status, Object cuerpo) throws IOException {
        resp.setStatus(status);
        resp.setContentType("application/json");
        resp.setCharacterEncoding("UTF-8");
        MAPPER.writeValue(resp.getWriter(), cuerpo);
    }

    public static void responder(HttpServletResponse resp, Object cuerpo) throws IOException {
        responder(resp, HttpServletResponse.SC_OK, cuerpo);
    }
}
