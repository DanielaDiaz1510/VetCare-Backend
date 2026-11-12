package com.mycompany.veterinaria.controler;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.mycompany.veterinaria.DTO.AuditoriaDTO;
import com.mycompany.veterinaria.model.entity.Auditoria;
import com.mycompany.veterinaria.service.AuditoriaService;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;
import java.util.List;

@WebServlet("/api/auditoria")
public class AuditoriaController extends HttpServlet {

    private final AuditoriaService service = new AuditoriaService();
    private final ObjectMapper mapper;

    public AuditoriaController() {

        mapper = new ObjectMapper();

        mapper.registerModule(new JavaTimeModule());
        mapper.disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);
    }

    @Override
    protected void doGet(
            HttpServletRequest req,
            HttpServletResponse resp)
            throws ServletException, IOException {

        resp.setContentType("application/json");
        resp.setCharacterEncoding("UTF-8");

        try {

            List<Auditoria> auditorias = service.listarTodos();

            List<AuditoriaDTO> resultado = auditorias.stream()
                    .map(a -> new AuditoriaDTO(
                            a.getIdAuditoria(),
                            a.getUsuario().getIdUsuario(),
                            a.getUsuario().getUsername(),
                            a.getAccion(),
                            a.getEntidad(),
                            a.getIdRegistro(),
                            a.getFechaHora(),
                            a.getDescripcion()
                    ))
                    .toList();

            mapper.writeValue(
                    resp.getWriter(),
                    resultado
            );

        } catch (Exception e) {

            e.printStackTrace();

            resp.setStatus(
                    HttpServletResponse.SC_INTERNAL_SERVER_ERROR
            );

            mapper.writeValue(
                    resp.getWriter(),
                    "Error al consultar la auditoria: "
                    + e.getMessage()
            );
        }
    }
}
