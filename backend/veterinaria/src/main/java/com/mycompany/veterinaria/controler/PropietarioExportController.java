package com.mycompany.veterinaria.controler;

import com.itextpdf.kernel.pdf.PdfDocument;
import com.itextpdf.kernel.pdf.PdfWriter;
import com.itextpdf.layout.Document;
import com.itextpdf.layout.element.Paragraph;
import com.mycompany.veterinaria.DTO.PropietarioDTO;
import com.mycompany.veterinaria.service.PropietarioService;
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

@WebServlet("/api/export/propietarios/*")
public class PropietarioExportController extends HttpServlet {

    private final PropietarioService propietarioService = new PropietarioService();

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws IOException {
        String path = request.getPathInfo(); // /excel o /pdf

        if (path == null) {
            response.sendError(HttpServletResponse.SC_BAD_REQUEST);
            return;
        }

        List<PropietarioDTO> propietarios = propietarioService.listarTodosDTO();

        switch (path) {
            case "/excel" -> exportarExcel(propietarios, response);
            case "/pdf" -> exportarPDF(propietarios, response);
            default -> response.sendError(HttpServletResponse.SC_NOT_FOUND);
        }
    }

    private void exportarExcel(List<PropietarioDTO> propietarios, HttpServletResponse response) throws IOException {
        Workbook workbook = new XSSFWorkbook();
        Sheet sheet = workbook.createSheet("Propietarios");

        Row header = sheet.createRow(0);
        header.createCell(0).setCellValue("ID");
        header.createCell(1).setCellValue("Tipo Doc");
        header.createCell(2).setCellValue("Documento");
        header.createCell(3).setCellValue("Nombres");
        header.createCell(4).setCellValue("Apellidos");
        header.createCell(5).setCellValue("Teléfono");
        header.createCell(6).setCellValue("Email");
        header.createCell(7).setCellValue("Dirección");

        int rowNum = 1;
        for (PropietarioDTO p : propietarios) {
            Row row = sheet.createRow(rowNum++);
            row.createCell(0).setCellValue(p.getId());
            row.createCell(1).setCellValue(p.getTipoDocumento());
            row.createCell(2).setCellValue(p.getNumeroDocumento());
            row.createCell(3).setCellValue(p.getNombres());
            row.createCell(4).setCellValue(p.getApellidos());
            row.createCell(5).setCellValue(p.getTelefono());
            row.createCell(6).setCellValue(p.getEmail());
            row.createCell(7).setCellValue(p.getDireccion());
        }

        response.setContentType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet");
        response.setHeader("Content-Disposition", "attachment; filename=propietarios.xlsx");

        workbook.write(response.getOutputStream());
        workbook.close();
    }

    private void exportarPDF(List<PropietarioDTO> propietarios, HttpServletResponse response) throws IOException {
        response.setContentType("application/pdf");
        response.setHeader("Content-Disposition", "attachment; filename=propietarios.pdf");

        PdfWriter writer = new PdfWriter(response.getOutputStream());
        PdfDocument pdf = new PdfDocument(writer);
        Document document = new Document(pdf);

        document.add(new Paragraph("LISTADO DE PROPIETARIOS\n\n"));

        for (PropietarioDTO p : propietarios) {
            document.add(new Paragraph("ID: " + p.getId()));
            document.add(new Paragraph("Documento: " + p.getTipoDocumento() + " " + p.getNumeroDocumento()));
            document.add(new Paragraph("Nombre Completo: " + p.getNombres() + " " + p.getApellidos()));
            document.add(new Paragraph("Teléfono: " + p.getTelefono() + " | Email: " + p.getEmail()));
            document.add(new Paragraph("---------------------------------------"));
        }

        document.close();
    }
}
