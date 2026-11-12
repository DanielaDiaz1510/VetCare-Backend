package com.mycompany.veterinaria.controler;

import com.mycompany.veterinaria.service.BackupService;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.OutputStream;

@WebServlet("/api/backup")
public class BackupController extends HttpServlet {

    private final BackupService backupService = new BackupService();

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        
        File backup = backupService.generarBackup();
        
        resp.setContentType("application/sql");
        resp.setHeader(
                "Content-Disposition",
                "attachment; filename=" + backup.getName()
        );
        
        try (FileInputStream fis = new FileInputStream(backup);
             OutputStream os = resp.getOutputStream()) {
            
            fis.transferTo(os);
        } finally {
            backup.delete(); // no dejar copias en el servidor
        }
    }
}
