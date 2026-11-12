package com.mycompany.veterinaria.controler;

import com.itextpdf.kernel.pdf.PdfDocument;
import com.itextpdf.kernel.pdf.PdfWriter;
import com.itextpdf.layout.Document;
import com.itextpdf.layout.element.Paragraph;
import com.mycompany.veterinaria.DTO.VeterinarioDTO;
import com.mycompany.veterinaria.service.VeterinarioService;
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

@WebServlet("/api/export/veterinarios/*")
public class VeterinarioExportController extends HttpServlet {

    private final VeterinarioService veterinarioService = new VeterinarioService();

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws IOException {
        String path = request.getPathInfo(); // /excel o /pdf

        if (path == null) {
            response.sendError(HttpServletResponse.SC_BAD_REQUEST);
            return;
        }

        List<VeterinarioDTO> veterinarios = veterinarioService.listarTodosDTO();

        switch (path) {
            case "/excel" -> exportarExcel(veterinarios, response);
            case "/pdf" -> exportarPDF(veterinarios, response);
            default -> response.sendError(HttpServletResponse.SC_NOT_FOUND);
        }
    }

    private void exportarExcel(List<VeterinarioDTO> veterinarios, HttpServletResponse response) throws IOException {
        Workbook workbook = new XSSFWorkbook();
        Sheet sheet = workbook.createSheet("Veterinarios");

        Row header = sheet.createRow(0);
        header.createCell(0).setCellValue("ID");
        header.createCell(1).setCellValue("Tipo Doc");
        header.createCell(2).setCellValue("Documento");
        header.createCell(3).setCellValue("Nombres");
        header.createCell(4).setCellValue("Apellidos");
        header.createCell(5).setCellValue("Especialidad");
        header.createCell(6).setCellValue("Teléfono");
        header.createCell(7).setCellValue("Email");
        header.createCell(8).setCellValue("Estado");

        int rowNum = 1;
        for (VeterinarioDTO v : veterinarios) {
            Row row = sheet.createRow(rowNum++);
            row.createCell(0).setCellValue(v.getId());
            row.createCell(1).setCellValue(v.getTipoDocumento());
            row.createCell(2).setCellValue(v.getNumeroDocumento());
            row.createCell(3).setCellValue(v.getNombres());
            row.createCell(4).setCellValue(v.getApellidos());
            row.createCell(5).setCellValue(v.getEspecialidad());
            row.createCell(6).setCellValue(v.getTelefono());
            row.createCell(7).setCellValue(v.getEmail());
            row.createCell(8).setCellValue(v.getEstado());
        }

        response.setContentType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet");
        response.setHeader("Content-Disposition", "attachment; filename=veterinarios.xlsx");

        workbook.write(response.getOutputStream());
        workbook.close();
    }

    private void exportarPDF(List<VeterinarioDTO> veterinarios, HttpServletResponse response) throws IOException {
        response.setContentType("application/pdf");
        response.setHeader("Content-Disposition", "attachment; filename=veterinarios.pdf");

        PdfWriter writer = new PdfWriter(response.getOutputStream());
        PdfDocument pdf = new PdfDocument(writer);
        Document document = new Document(pdf);

        document.add(new Paragraph("LISTADO DE VETERINARIOS\n\n"));

        for (VeterinarioDTO v : veterinarios) {
            document.add(new Paragraph("ID: " + v.getId()));
            document.add(new Paragraph("Especialidad: " + v.getEspecialidad()));
            document.add(new Paragraph("Nombre Completo: " + v.getNombres() + " " + v.getApellidos()));
            document.add(new Paragraph("Contacto: " + v.getTelefono() + " | " + v.getEmail()));
            document.add(new Paragraph("Estado: " + v.getEstado()));
            document.add(new Paragraph("---------------------------------------"));
        }

        document.close();
    }
}
