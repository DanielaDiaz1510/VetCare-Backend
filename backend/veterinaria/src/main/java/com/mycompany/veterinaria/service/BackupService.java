package com.mycompany.veterinaria.service;

import com.mycompany.veterinaria.util.Config;
import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.stream.Collectors;

/**
 * Copia de seguridad de la base de datos con la herramienta mysqldump.
 * Solo la puede ejecutar un ADMIN (lo exige el filtro de seguridad).
 *
 * Los valores por defecto sirven para XAMPP en Windows. Se pueden cambiar con variables de entorno
 * (BACKUP_MYSQLDUMP, DB_NAME, DB_USER, DB_PASSWORD, DB_HOST) sin tocar el código.
 */
public class BackupService {

    private static final String MYSQL_PATH = Config.get("BACKUP_MYSQLDUMP", "C:\\xampp\\mysql\\bin\\mysqldump.exe");
    private static final String DB_NAME = Config.get("DB_NAME", "veterinaria");
    private static final String DB_USER = Config.get("DB_USER", "root");
    private static final String DB_PASS = Config.get("DB_PASSWORD", "");
    private static final String DB_HOST = Config.get("DB_HOST", "localhost");

    public File generarBackup() throws IOException {

        String fecha = LocalDateTime.now()
                .format(DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss"));

        // El archivo se crea en la carpeta temporal del servidor y se borra después de enviarlo
        File backup = new File(System.getProperty("java.io.tmpdir"), "backup_" + fecha + ".sql");

        ProcessBuilder pb = new ProcessBuilder(
                MYSQL_PATH,
                "--host=" + DB_HOST,
                "--user=" + DB_USER,
                DB_NAME
        );
        // La contraseña viaja en una variable de entorno del proceso hijo, no en la línea de comandos
        // (así no se ve en la lista de procesos del sistema).
        pb.environment().put("MYSQL_PWD", DB_PASS);

        // Redirige salida SQL al archivo
        pb.redirectOutput(backup);

        // Captura errores
        pb.redirectError(ProcessBuilder.Redirect.PIPE);

        try {
            Process process = pb.start();

            String error;
            try (BufferedReader br = new BufferedReader(
                    new InputStreamReader(process.getErrorStream()))) {
                error = br.lines().collect(Collectors.joining("\n"));
            }

            int exitCode = process.waitFor();

            if (exitCode != 0) {
                backup.delete();
                throw new RuntimeException(
                        "mysqldump falló (" + exitCode + "):\n" + error
                );
            }

        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new RuntimeException("Proceso interrumpido", e);
        }

        return backup;
    }
}
