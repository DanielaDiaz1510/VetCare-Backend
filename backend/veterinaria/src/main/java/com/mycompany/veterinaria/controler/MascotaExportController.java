package com.mycompany.veterinaria.controler;

import com.itextpdf.kernel.pdf.PdfDocument;
import com.itextpdf.kernel.pdf.PdfWriter;
import com.itextpdf.layout.Document;
import com.itextpdf.layout.element.Paragraph;
import com.mycompany.veterinaria.DTO.MascotaDTO;
import com.mycompany.veterinaria.service.MascotaService;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.List;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

@WebServlet("/api/export/mascotas/*")
public class MascotaExportController extends HttpServlet {

    private final MascotaService mascotaService = new MascotaService();

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws IOException {
        String path = request.getPathInfo(); // /excel o /pdf

        if (path == null) {
            response.sendError(HttpServletResponse.SC_BAD_REQUEST);
            return;
        }

        List<MascotaDTO> mascotas = mascotaService.listarTodosDTO();

        switch (path) {
            case "/excel" -> exportarExcel(mascotas, response);
            case "/pdf" -> exportarPDF(mascotas, response);
            default -> response.sendError(HttpServletResponse.SC_NOT_FOUND);
        }
    }

    private void exportarExcel(List<MascotaDTO> mascotas, HttpServletResponse response) throws IOException {
        Workbook workbook = new XSSFWorkbook();
        Sheet sheet = workbook.createSheet("Mascotas");

        // Header
        Row header = sheet.createRow(0);
        header.createCell(0).setCellValue("ID");
        header.createCell(1).setCellValue("Nombre");
        header.createCell(2).setCellValue("Especie");
        header.createCell(3).setCellValue("Raza");
        header.createCell(4).setCellValue("Sexo");
        header.createCell(5).setCellValue("Fecha Nacimiento");
        header.createCell(6).setCellValue("Color");
        header.createCell(7).setCellValue("Peso");
        header.createCell(8).setCellValue("Propietario");

        int rowNum = 1;

        for (MascotaDTO m : mascotas) {
            Row row = sheet.createRow(rowNum++);

            row.createCell(0).setCellValue(m.getId());
            row.createCell(1).setCellValue(m.getNombre());
            row.createCell(2).setCellValue(m.getEspecie());
            row.createCell(3).setCellValue(m.getRaza());
            row.createCell(4).setCellValue(m.getSexo());
            row.createCell(5).setCellValue(
                    m.getFechaNacimiento() != null ? m.getFechaNacimiento().toString() : ""
            );
            row.createCell(6).setCellValue(m.getColor());
            row.createCell(7).setCellValue(
                    m.getPesoActual() != null ? m.getPesoActual() : 0
            );
            row.createCell(8).setCellValue(m.getNombrePropietario());
        }

        response.setContentType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet");
        response.setHeader("Content-Disposition", "attachment; filename=mascotas.xlsx");

        workbook.write(response.getOutputStream());
        workbook.close();
    }

    private void exportarPDF(List<MascotaDTO> mascotas, HttpServletResponse response) throws IOException {
        response.setContentType("application/pdf");
        response.setHeader("Content-Disposition", "attachment; filename=mascotas.pdf");

        PdfWriter writer = new PdfWriter(response.getOutputStream());
        PdfDocument pdf = new PdfDocument(writer);
        Document document = new Document(pdf);

        document.add(new Paragraph("LISTADO DE MASCOTAS\n\n"));

        for (MascotaDTO m : mascotas) {
            document.add(new Paragraph("ID: " + m.getId()));
            document.add(new Paragraph("Nombre: " + m.getNombre()));
            document.add(new Paragraph("Especie: " + m.getEspecie()));
            document.add(new Paragraph("Raza: " + m.getRaza()));
            document.add(new Paragraph("Propietario: " + m.getNombrePropietario()));
            document.add(new Paragraph("---------------------------------------"));
        }

        document.close();
    }
}
