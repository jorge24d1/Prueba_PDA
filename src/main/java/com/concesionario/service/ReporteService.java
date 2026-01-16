package com.concesionario.service;

import com.concesionario.model.Usuario;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.ss.util.CellRangeAddress;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.stereotype.Service;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.List;

@Service
public class ReporteService {

    public ByteArrayInputStream generarReportePotenciales(List<Usuario> usuarios) throws IOException {
        try (Workbook workbook = new XSSFWorkbook()) {
            
            // Crear hoja principal
            Sheet sheet = workbook.createSheet("Usuarios Potenciales");

            // ========== ESTILOS PROFESIONALES ==========

            // Estilo para título principal
            CellStyle titleStyle = workbook.createCellStyle();
            Font titleFont = workbook.createFont();
            titleFont.setBold(true);
            titleFont.setFontHeightInPoints((short) 16);
            titleFont.setColor(IndexedColors.DARK_GREEN.getIndex());
            titleStyle.setFont(titleFont);
            titleStyle.setAlignment(HorizontalAlignment.CENTER);
            titleStyle.setVerticalAlignment(VerticalAlignment.CENTER);

            // Estilo para subtítulo
            CellStyle subtitleStyle = workbook.createCellStyle();
            Font subtitleFont = workbook.createFont();
            subtitleFont.setBold(true);
            subtitleFont.setFontHeightInPoints((short) 12);
            subtitleFont.setColor(IndexedColors.GREY_50_PERCENT.getIndex());
            subtitleStyle.setFont(subtitleFont);
            subtitleStyle.setAlignment(HorizontalAlignment.CENTER);

            // Estilo para encabezados de tabla
            CellStyle headerStyle = workbook.createCellStyle();
            Font headerFont = workbook.createFont();
            headerFont.setBold(true);
            headerFont.setFontHeightInPoints((short) 11);
            headerFont.setColor(IndexedColors.WHITE.getIndex());
            headerStyle.setFont(headerFont);
            headerStyle.setFillForegroundColor(IndexedColors.DARK_GREEN.getIndex());
            headerStyle.setFillPattern(FillPatternType.SOLID_FOREGROUND);
            headerStyle.setBorderTop(BorderStyle.THIN);
            headerStyle.setBorderBottom(BorderStyle.THIN);
            headerStyle.setBorderLeft(BorderStyle.THIN);
            headerStyle.setBorderRight(BorderStyle.THIN);
            headerStyle.setAlignment(HorizontalAlignment.CENTER);
            headerStyle.setVerticalAlignment(VerticalAlignment.CENTER);

            // Estilo para celdas de datos
            CellStyle dataStyle = workbook.createCellStyle();
            dataStyle.setBorderTop(BorderStyle.THIN);
            dataStyle.setBorderBottom(BorderStyle.THIN);
            dataStyle.setBorderLeft(BorderStyle.THIN);
            dataStyle.setBorderRight(BorderStyle.THIN);
            dataStyle.setVerticalAlignment(VerticalAlignment.CENTER);

            // Estilo para probabilidad alta
            CellStyle highProbStyle = workbook.createCellStyle();
            highProbStyle.cloneStyleFrom(dataStyle);
            highProbStyle.setFillForegroundColor(IndexedColors.LIGHT_GREEN.getIndex());
            highProbStyle.setFillPattern(FillPatternType.SOLID_FOREGROUND);
            highProbStyle.setDataFormat(workbook.createDataFormat().getFormat("0.0\"%\""));

            // Estilo para probabilidad media
            CellStyle mediumProbStyle = workbook.createCellStyle();
            mediumProbStyle.cloneStyleFrom(dataStyle);
            mediumProbStyle.setFillForegroundColor(IndexedColors.LIGHT_YELLOW.getIndex());
            mediumProbStyle.setFillPattern(FillPatternType.SOLID_FOREGROUND);
            mediumProbStyle.setDataFormat(workbook.createDataFormat().getFormat("0.0\"%\""));

            // Estilo para probabilidad baja
            CellStyle lowProbStyle = workbook.createCellStyle();
            lowProbStyle.cloneStyleFrom(dataStyle);
            lowProbStyle.setFillForegroundColor(IndexedColors.LIGHT_ORANGE.getIndex());
            lowProbStyle.setFillPattern(FillPatternType.SOLID_FOREGROUND);
            lowProbStyle.setDataFormat(workbook.createDataFormat().getFormat("0.0\"%\""));

            // Estilo para resumen
            CellStyle summaryStyle = workbook.createCellStyle();
            Font summaryFont = workbook.createFont();
            summaryFont.setBold(true);
            summaryFont.setColor(IndexedColors.DARK_BLUE.getIndex());
            summaryStyle.setFont(summaryFont);
            summaryStyle.setFillForegroundColor(IndexedColors.GREY_25_PERCENT.getIndex());
            summaryStyle.setFillPattern(FillPatternType.SOLID_FOREGROUND);
            summaryStyle.setBorderTop(BorderStyle.MEDIUM);
            summaryStyle.setBorderBottom(BorderStyle.MEDIUM);

            // ========== CABECERA DEL REPORTE ==========

            // Título principal
            Row titleRow = sheet.createRow(0);
            Cell titleCell = titleRow.createCell(0);
            titleCell.setCellValue("REPORTE DE USUARIOS POTENCIALES - NEXTGEN MOTORS");
            titleCell.setCellStyle(titleStyle);
            sheet.addMergedRegion(new CellRangeAddress(0, 0, 0, 13));

            // Subtítulo
            Row subtitleRow = sheet.createRow(1);
            Cell subtitleCell = subtitleRow.createCell(0);
            subtitleCell.setCellValue("Análisis Predictivo con Inteligencia Artificial");
            subtitleCell.setCellStyle(subtitleStyle);
            sheet.addMergedRegion(new CellRangeAddress(1, 1, 0, 13));

            // Fecha de generación
            Row dateRow = sheet.createRow(2);
            Cell dateCell = dateRow.createCell(0);
            dateCell.setCellValue("Generado: " + java.time.LocalDate.now().toString());
            dateCell.setCellStyle(subtitleStyle);
            sheet.addMergedRegion(new CellRangeAddress(2, 2, 0, 13));

            // Espacio
            sheet.createRow(3);

            // ========== TABLA DE DATOS ==========

            // Encabezados de tabla
            Row headerRow = sheet.createRow(4);
            String[] headers = {
                    "No.", "Nombre", "Apellido", "Identificación", "Correo Electrónico",
                    "Citas", "Antigüedad", "Estado", "Interés", "Tiempo",
                    "Potencial", "Probabilidad", "Confianza", "Observaciones"
            };

            for (int i = 0; i < headers.length; i++) {
                Cell cell = headerRow.createCell(i);
                cell.setCellValue(headers[i]);
                cell.setCellStyle(headerStyle);
            }

            // Llenar datos
            int rowNum = 5;
            int contador = 1;
            for (Usuario usuario : usuarios) {
                Row dataRow = sheet.createRow(rowNum++);

                // Número consecutivo
                dataRow.createCell(0).setCellValue(contador++);

                // Información básica
                dataRow.createCell(1).setCellValue(usuario.getNombreUser() != null ? usuario.getNombreUser() : "N/A");
                dataRow.createCell(2).setCellValue(usuario.getApellidoUser() != null ? usuario.getApellidoUser() : "N/A");
                dataRow.createCell(3).setCellValue(usuario.getIdentificacionUser() != null ? usuario.getIdentificacionUser() : "N/A");
                dataRow.createCell(4).setCellValue(usuario.getCorreoUser() != null ? usuario.getCorreoUser() : "N/A");

                // Métricas
                dataRow.createCell(5).setCellValue(usuario.getCantidadCitas() != null ? usuario.getCantidadCitas() : 0);
                dataRow.createCell(6).setCellValue(usuario.getAntiguedadCuenta() != null ? usuario.getAntiguedadCuenta() : 0);
                dataRow.createCell(7).setCellValue(usuario.getEstadoUltimaCita() != null ? usuario.getEstadoUltimaCita() : "N/A");
                dataRow.createCell(8).setCellValue(usuario.getInteresVehiculo() != null ? usuario.getInteresVehiculo() : "N/A");
                dataRow.createCell(9).setCellValue(usuario.getTiempoEntreCitas() != null ? usuario.getTiempoEntreCitas() : 0);

                // Predicciones
                dataRow.createCell(10).setCellValue(usuario.getClientePotencial() != null ? usuario.getClientePotencial() : "No");

                // Celda de probabilidad con estilo condicional
                Cell probCell = dataRow.createCell(11);
                double probabilidad = usuario.getProbabilidad() != null ? usuario.getProbabilidad() : 0.0;
                probCell.setCellValue(probabilidad);

                if (probabilidad >= 80) {
                    probCell.setCellStyle(highProbStyle);
                } else if (probabilidad >= 60) {
                    probCell.setCellStyle(mediumProbStyle);
                } else {
                    probCell.setCellStyle(lowProbStyle);
                }

                // Nivel de confianza
                dataRow.createCell(12).setCellValue(determinarNivelConfianza(probabilidad));

                // Observaciones
                dataRow.createCell(13).setCellValue(usuario.getObservaciones() != null ? usuario.getObservaciones() : "Sin observaciones");

                // Aplicar estilo de datos a todas las celdas
                for (int i = 0; i < headers.length; i++) {
                    if (i != 11) { // Excluir celda de probabilidad (ya tiene estilo)
                        Cell cell = dataRow.getCell(i);
                        if (cell == null) cell = dataRow.createCell(i);
                        cell.setCellStyle(dataStyle);
                    }
                }
            }

            // ========== RESUMEN ESTADÍSTICO ==========

            int summaryStartRow = rowNum + 2;

            // Título del resumen
            Row summaryTitleRow = sheet.createRow(summaryStartRow);
            Cell summaryTitleCell = summaryTitleRow.createCell(0);
            summaryTitleCell.setCellValue("RESUMEN ESTADÍSTICO");
            summaryTitleCell.setCellStyle(titleStyle);
            sheet.addMergedRegion(new CellRangeAddress(summaryStartRow, summaryStartRow, 0, 13));

            // Estadísticas
            String[] summaryLabels = {
                    "Total de Usuarios Potenciales:",
                    "Probabilidad Promedio:",
                    "Usuarios con Probabilidad Alta (>80%):",
                    "Usuarios con Probabilidad Media (60-80%):",
                    "Usuarios con Probabilidad Baja (<60%):",
                    "Tasa de Conversión Estimada:"
            };

            String[] summaryValues = calcularEstadisticas(usuarios);

            for (int i = 0; i < summaryLabels.length; i++) {
                Row summaryRow = sheet.createRow(summaryStartRow + i + 1);
                summaryRow.createCell(0).setCellValue(summaryLabels[i]);
                summaryRow.createCell(1).setCellValue(summaryValues[i]);

                // Aplicar estilo al label
                summaryRow.getCell(0).setCellStyle(summaryStyle);
                
                // Aplicar estilo al valor
                Cell valueCell = summaryRow.getCell(1);
                valueCell.setCellStyle(summaryStyle);
            }

            // ========== AJUSTES FINALES ==========

            // Autoajustar columnas
            for (int i = 0; i < headers.length; i++) {
                sheet.autoSizeColumn(i);
            }

            // Congelar paneles
            sheet.createFreezePane(0, 5, 0, 5);

            // ========== GENERAR ARCHIVO ==========
            ByteArrayOutputStream out = new ByteArrayOutputStream();
            workbook.write(out);
            
            return new ByteArrayInputStream(out.toByteArray());
        }
    }

    // Método auxiliar para nivel de confianza
    private String determinarNivelConfianza(double probabilidad) {
        if (probabilidad >= 80) return "⭐ MUY ALTO";
        if (probabilidad >= 60) return "▲ ALTO";
        if (probabilidad >= 40) return "● MEDIO";
        return "○ BAJO";
    }

    // Método para calcular estadísticas
    private String[] calcularEstadisticas(List<Usuario> usuarios) {
        if (usuarios.isEmpty()) {
            return new String[]{"0", "0%", "0", "0", "0", "0%"};
        }

        double promedioProb = usuarios.stream()
                .mapToDouble(u -> u.getProbabilidad() != null ? u.getProbabilidad() : 0)
                .average()
                .orElse(0);

        long altaProb = usuarios.stream()
                .filter(u -> u.getProbabilidad() != null && u.getProbabilidad() >= 80)
                .count();

        long mediaProb = usuarios.stream()
                .filter(u -> u.getProbabilidad() != null && u.getProbabilidad() >= 60 && u.getProbabilidad() < 80)
                .count();

        long bajaProb = usuarios.stream()
                .filter(u -> u.getProbabilidad() != null && u.getProbabilidad() < 60)
                .count();

        double tasaConversion = (altaProb * 0.8) + (mediaProb * 0.5) + (bajaProb * 0.2);

        return new String[]{
                String.valueOf(usuarios.size()),
                String.format("%.1f%%", promedioProb),
                String.valueOf(altaProb),
                String.valueOf(mediaProb),
                String.valueOf(bajaProb),
                String.format("%.1f%%", tasaConversion)
        };
    }
}
