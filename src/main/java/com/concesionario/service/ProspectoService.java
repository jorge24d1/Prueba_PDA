package com.concesionario.service;

import com.concesionario.dto.ProspectoDTO;
import com.concesionario.model.Prospecto;
import com.concesionario.model.Trabajador;
import com.concesionario.repository.ProspectoRepository;
import com.concesionario.repository.TrabajadorRepository;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.ss.util.CellRangeAddress;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.YearMonth;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class ProspectoService {

    @Autowired
    private ProspectoRepository prospectoRepository;
    @Autowired
    private TrabajadorRepository trabajadorRepository;
    public List<ProspectoDTO> obtenerProspectosParaAsesor(String asesorId) {
        List<Prospecto> prospectos = prospectoRepository.findByTrabajadorId(asesorId);
        return prospectos.stream()
                .map(this::convertirAProspectoDTO)
                .collect(Collectors.toList());
    }

    public void registrarProspectoManual(String nombre, String apellido, String correo, String telefono, String vehiculoInteres, String asesorId, String observaciones) {
        Prospecto prospecto = new Prospecto();
        prospecto.setNombre(nombre);
        prospecto.setApellido(apellido);
        prospecto.setCorreo(correo);
        prospecto.setTelefono(telefono);
        prospecto.setVehiculoInteres(vehiculoInteres);
        prospecto.setTrabajadorId(asesorId);
        prospecto.setObservaciones(observaciones);
        prospecto.setOrigen("Presencial");
        prospecto.setEstado("Nuevo");
        prospecto.setFechaRegistro(LocalDateTime.now());

        prospectoRepository.save(prospecto);
    }

    private ProspectoDTO convertirAProspectoDTO(Prospecto prospecto) {
        ProspectoDTO dto = new ProspectoDTO();
        dto.setId(prospecto.getId());
        dto.setNombreCompleto(prospecto.getNombre() + " " + prospecto.getApellido());
        dto.setEmail(prospecto.getCorreo());
        dto.setTelefono(prospecto.getTelefono());
        dto.setVehiculoInteres(prospecto.getVehiculoInteres());
        dto.setEstado(prospecto.getEstado());
        dto.setUltimoContacto(prospecto.getUltimoContacto() != null ? prospecto.getUltimoContacto() : prospecto.getFechaRegistro());
        dto.setOrigen(prospecto.getOrigen());
        dto.setObservaciones(prospecto.getObservaciones());
        return dto;
    }

    public void cambiarEstadoContactado(String prospectoId) {
        Prospecto prospecto = prospectoRepository.findById(prospectoId)
                .orElseThrow(() -> new RuntimeException("Prospecto no encontrado"));


        if ("Nuevo".equalsIgnoreCase(prospecto.getEstado())) {
            prospecto.setEstado("Contactado");
        }

        prospecto.setUltimoContacto(LocalDateTime.now());
        prospectoRepository.save(prospecto);
    }

    public void actualizarEstadoProspecto(String prospectoId, String nuevoEstado) {
        Prospecto prospecto = prospectoRepository.findById(prospectoId)
                .orElseThrow(() -> new RuntimeException("Prospecto no encontrado"));
        prospecto.setEstado(nuevoEstado);
        prospecto.setUltimoContacto(LocalDateTime.now());
        prospectoRepository.save(prospecto);
    }

    public ByteArrayInputStream generarReporteRendimientoMensual() throws IOException {
        try (Workbook workbook = new XSSFWorkbook()) {
            // ========== CONFIGURACIÓN DEL MES ==========
            LocalDate ahora = LocalDate.now();
            YearMonth mesActual = YearMonth.from(ahora);
            String nombreMes = obtenerNombreMes(mesActual.getMonthValue());
            int año = mesActual.getYear();

            // Fechas para filtrar
            LocalDateTime inicioMes = mesActual.atDay(1).atStartOfDay();
            LocalDateTime finMes = mesActual.atEndOfMonth().atTime(23, 59, 59);

            // ========== CREAR ESTILOS ==========
            CellStyle headerStyle = crearEstiloHeader(workbook);
            CellStyle dataStyle = crearEstiloDatos(workbook);
            CellStyle ventaStyle = crearEstiloVentas(workbook);
            CellStyle porcentajeStyle = crearEstiloPorcentaje(workbook);
            CellStyle totalStyle = crearEstiloTotales(workbook);
            CellStyle tituloStyle = crearEstiloTitulo(workbook);

            // ========== CREAR HOJA ==========
            Sheet sheet = workbook.createSheet("Rendimiento " + nombreMes);

            // ========== TÍTULO DEL REPORTE ==========
            Row tituloRow = sheet.createRow(0);
            Cell tituloCell = tituloRow.createCell(0);
            tituloCell.setCellValue("REPORTE DE RENDIMIENTO MENSUAL - " + nombreMes.toUpperCase() + " " + año);
            tituloCell.setCellStyle(tituloStyle);
            sheet.addMergedRegion(new CellRangeAddress(0, 0, 0, 9));

            // Subtítulo con período
            Row subtituloRow = sheet.createRow(1);
            Cell subtituloCell = subtituloRow.createCell(0);
            subtituloCell.setCellValue("Período: " + inicioMes.toLocalDate().format(DateTimeFormatter.ofPattern("dd/MM/yyyy")) +
                    " al " + finMes.toLocalDate().format(DateTimeFormatter.ofPattern("dd/MM/yyyy")));

            // Fecha de generación - CORREGIDO
            Row fechaGenRow = sheet.createRow(2);
            Cell fechaGenCell = fechaGenRow.createCell(0);
            fechaGenCell.setCellValue("Generado: " + LocalDateTime.now().format(DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm")));

            // Espacio
            sheet.createRow(3);


            Row headerRow = sheet.createRow(4);
            String[] headers = {
                    "IDENTIFICACIÓN", "ASESOR", "CORREO", "TOTAL",
                    "NUEVOS", "CONTACTADOS", "EN PROCESO",
                    "VENTAS", "PERDIDOS", "TASA CONV."
            };

            for (int i = 0; i < headers.length; i++) {
                Cell cell = headerRow.createCell(i);
                cell.setCellValue(headers[i]);
                cell.setCellStyle(headerStyle);
            }


            List<Trabajador> asesores = trabajadorRepository.findAll().stream()
                    .filter(t -> t.isActivo())
                    .filter(t -> {
                        if (t.getCargo() != null && t.getCargo().toLowerCase().contains("asesor")) {
                            return true;
                        }
                        if (t.getRoles() != null && !t.getRoles().isEmpty()) {
                            for (Object rolObj : t.getRoles()) {
                                String rol = rolObj.toString().toUpperCase();
                                if (rol.contains("ASESOR")) return true;
                            }
                        }
                        return false;
                    })
                    .collect(Collectors.toList());


            int rowNum = 5;
            long totalProspectosGlobal = 0;
            long totalVentasGlobal = 0;

            for (Trabajador asesor : asesores) {
                Map<String, Object> stats = obtenerEstadisticasAsesorMes(asesor.getId(), inicioMes, finMes);

                Row row = sheet.createRow(rowNum++);

                // Columna 0: IDENTIFICACIÓN
                Cell cell0 = row.createCell(0);
                cell0.setCellValue(asesor.getIdentificacion() != null ? asesor.getIdentificacion() : "N/A");
                cell0.setCellStyle(dataStyle);

                // Columna 1: NOMBRE ASESOR
                Cell cell1 = row.createCell(1);
                cell1.setCellValue(asesor.getNombreCompleto());
                cell1.setCellStyle(dataStyle);

                // Columna 2: CORREO
                Cell cell2 = row.createCell(2);
                cell2.setCellValue(asesor.getCorreo() != null ? asesor.getCorreo() : "N/A");
                cell2.setCellStyle(dataStyle);

                // Columna 3: TOTAL PROSPECTOS DEL MES
                long totalProspectos = (Long) stats.get("totalProspectos");
                Cell cell3 = row.createCell(3);
                cell3.setCellValue(totalProspectos);
                cell3.setCellStyle(dataStyle);
                totalProspectosGlobal += totalProspectos;

                // Columna 4: NUEVOS
                Cell cell4 = row.createCell(4);
                cell4.setCellValue((Long) stats.get("nuevos"));
                cell4.setCellStyle(dataStyle);

                // Columna 5: CONTACTADOS
                Cell cell5 = row.createCell(5);
                cell5.setCellValue((Long) stats.get("contactados"));
                cell5.setCellStyle(dataStyle);

                // Columna 6: EN PROCESO
                Cell cell6 = row.createCell(6);
                cell6.setCellValue((Long) stats.get("enProceso"));
                cell6.setCellStyle(dataStyle);

                // Columna 7: VENTAS (DESTACADO)
                long ventas = (Long) stats.get("ventas");
                Cell cell7 = row.createCell(7);
                cell7.setCellValue(ventas);
                cell7.setCellStyle(ventaStyle);
                totalVentasGlobal += ventas;

                // Columna 8: PERDIDOS
                Cell cell8 = row.createCell(8);
                cell8.setCellValue((Long) stats.get("perdidos"));
                cell8.setCellStyle(dataStyle);

                // Columna 9: TASA CONVERSIÓN
                double tasa = (Double) stats.get("tasaConversion");
                Cell cell9 = row.createCell(9);
                cell9.setCellValue(tasa / 100.0);
                cell9.setCellStyle(porcentajeStyle);
            }


            if (!asesores.isEmpty()) {
                Row totalRow = sheet.createRow(rowNum);


                for (int i = 0; i < 3; i++) {
                    Cell cell = totalRow.createCell(i);
                    if (i == 0) {
                        cell.setCellValue("TOTALES " + nombreMes.toUpperCase());
                    }
                    cell.setCellStyle(totalStyle);
                }
                sheet.addMergedRegion(new CellRangeAddress(rowNum, rowNum, 0, 2));

                // Total prospectos
                Cell totalProspectosCell = totalRow.createCell(3);
                totalProspectosCell.setCellValue(totalProspectosGlobal);
                totalProspectosCell.setCellStyle(totalStyle);

                // Total ventas
                Cell totalVentasCell = totalRow.createCell(7);
                totalVentasCell.setCellValue(totalVentasGlobal);
                totalVentasCell.setCellStyle(totalStyle);

                // Tasa global
                double tasaGlobal = totalProspectosGlobal > 0 ?
                        ((double) totalVentasGlobal / totalProspectosGlobal) * 100.0 : 0.0;
                Cell totalTasaCell = totalRow.createCell(9);
                totalTasaCell.setCellValue(tasaGlobal / 100.0);
                totalTasaCell.setCellStyle(porcentajeStyle);
            }


            rowNum += 2;
            Row resumenRow = sheet.createRow(rowNum);
            Cell resumenCell = resumenRow.createCell(0);
            resumenCell.setCellValue("RESUMEN DEL MES");
            resumenCell.setCellStyle(headerStyle);
            sheet.addMergedRegion(new CellRangeAddress(rowNum, rowNum, 0, 9));

            rowNum++;
            Row detalle1Row = sheet.createRow(rowNum++);
            detalle1Row.createCell(0).setCellValue("Total Asesores:");
            detalle1Row.createCell(1).setCellValue(asesores.size());

            Row detalle2Row = sheet.createRow(rowNum++);
            detalle2Row.createCell(0).setCellValue("Total Prospectos del Mes:");
            detalle2Row.createCell(1).setCellValue(totalProspectosGlobal);

            Row detalle3Row = sheet.createRow(rowNum++);
            detalle3Row.createCell(0).setCellValue("Total Ventas del Mes:");
            detalle3Row.createCell(1).setCellValue(totalVentasGlobal);

            // ========== AJUSTAR ANCHO ==========
            for (int i = 0; i < headers.length; i++) {
                sheet.autoSizeColumn(i);
                int currentWidth = sheet.getColumnWidth(i);
                if (currentWidth < 2500) {
                    sheet.setColumnWidth(i, 2500);
                }
            }

            // ========== ESCRIBIR ==========
            ByteArrayOutputStream out = new ByteArrayOutputStream();
            workbook.write(out);
            return new ByteArrayInputStream(out.toByteArray());

        } catch (Exception e) {
            e.printStackTrace(); // Para ver el error real
            throw new IOException("Error al generar reporte: " + e.getMessage(), e);
        }
    }


    private Map<String, Object> obtenerEstadisticasAsesorMes(String asesorId, LocalDateTime inicioMes, LocalDateTime finMes) {
        Map<String, Object> stats = new HashMap<>();

        try {

            List<Prospecto> prospectosMes;

            try {

                prospectosMes = prospectoRepository.findByTrabajadorIdAndFechaRegistroBetween(
                        asesorId, inicioMes, finMes);
            } catch (Exception e) {

                List<Prospecto> todosProspectos = prospectoRepository.findByTrabajadorId(asesorId);
                prospectosMes = todosProspectos.stream()
                        .filter(p -> p.getFechaRegistro() != null)
                        .filter(p -> !p.getFechaRegistro().isBefore(inicioMes) &&
                                !p.getFechaRegistro().isAfter(finMes))
                        .collect(Collectors.toList());
            }


            long nuevos = prospectosMes.stream()
                    .filter(p -> p.getEstado() != null)
                    .filter(p -> p.getEstado().equalsIgnoreCase("Nuevo"))
                    .count();

            long contactados = prospectosMes.stream()
                    .filter(p -> p.getEstado() != null)
                    .filter(p -> p.getEstado().equalsIgnoreCase("Contactado"))
                    .count();

            long enProceso = prospectosMes.stream()
                    .filter(p -> p.getEstado() != null)
                    .filter(p -> p.getEstado().equalsIgnoreCase("En Proceso"))
                    .count();

            long ventas = prospectosMes.stream()
                    .filter(p -> p.getEstado() != null)
                    .filter(p -> {
                        String estado = p.getEstado().toLowerCase();
                        return estado.contains("venta") ||
                                estado.contains("compra") ||
                                estado.equals("vendido");
                    })
                    .count();

            long perdidos = prospectosMes.stream()
                    .filter(p -> p.getEstado() != null)
                    .filter(p -> p.getEstado().equalsIgnoreCase("Perdido"))
                    .count();


            double tasaConversion = 0.0;
            if (prospectosMes.size() > 0) {
                tasaConversion = ((double) ventas / prospectosMes.size()) * 100.0;
            }

            stats.put("totalProspectos", (long) prospectosMes.size());
            stats.put("nuevos", nuevos);
            stats.put("contactados", contactados);
            stats.put("enProceso", enProceso);
            stats.put("ventas", ventas);
            stats.put("perdidos", perdidos);
            stats.put("tasaConversion", Math.round(tasaConversion * 10.0) / 10.0);

        } catch (Exception e) {
            e.printStackTrace();
            // Valores por defecto
            stats.put("totalProspectos", 0L);
            stats.put("nuevos", 0L);
            stats.put("contactados", 0L);
            stats.put("enProceso", 0L);
            stats.put("ventas", 0L);
            stats.put("perdidos", 0L);
            stats.put("tasaConversion", 0.0);
        }

        return stats;
    }


    private CellStyle crearEstiloHeader(Workbook workbook) {
        CellStyle style = workbook.createCellStyle();
        Font font = workbook.createFont();
        font.setBold(true);
        font.setColor(IndexedColors.WHITE.getIndex());
        style.setFont(font);
        style.setFillForegroundColor(IndexedColors.DARK_BLUE.getIndex());
        style.setFillPattern(FillPatternType.SOLID_FOREGROUND);
        style.setAlignment(HorizontalAlignment.CENTER);
        style.setVerticalAlignment(VerticalAlignment.CENTER);
        style.setBorderBottom(BorderStyle.THIN);
        style.setBorderTop(BorderStyle.THIN);
        style.setBorderRight(BorderStyle.THIN);
        style.setBorderLeft(BorderStyle.THIN);
        return style;
    }

    private CellStyle crearEstiloTitulo(Workbook workbook) {
        CellStyle style = workbook.createCellStyle();
        Font font = workbook.createFont();
        font.setBold(true);
        font.setFontHeightInPoints((short) 14);
        font.setColor(IndexedColors.DARK_BLUE.getIndex());
        style.setFont(font);
        style.setAlignment(HorizontalAlignment.CENTER);
        return style;
    }

    private CellStyle crearEstiloDatos(Workbook workbook) {
        CellStyle style = workbook.createCellStyle();
        style.setAlignment(HorizontalAlignment.CENTER);
        style.setVerticalAlignment(VerticalAlignment.CENTER);
        style.setBorderBottom(BorderStyle.THIN);
        style.setBorderTop(BorderStyle.THIN);
        style.setBorderRight(BorderStyle.THIN);
        style.setBorderLeft(BorderStyle.THIN);
        return style;
    }

    private CellStyle crearEstiloVentas(Workbook workbook) {
        CellStyle style = workbook.createCellStyle();
        Font font = workbook.createFont();
        font.setBold(true);
        font.setColor(IndexedColors.WHITE.getIndex());
        style.setFont(font);
        style.setFillForegroundColor(IndexedColors.GREEN.getIndex());
        style.setFillPattern(FillPatternType.SOLID_FOREGROUND);
        style.setAlignment(HorizontalAlignment.CENTER);
        style.setVerticalAlignment(VerticalAlignment.CENTER);
        style.setBorderBottom(BorderStyle.THIN);
        style.setBorderTop(BorderStyle.THIN);
        style.setBorderRight(BorderStyle.THIN);
        style.setBorderLeft(BorderStyle.THIN);
        return style;
    }

    private CellStyle crearEstiloPorcentaje(Workbook workbook) {
        CellStyle style = workbook.createCellStyle();
        style.setDataFormat(workbook.createDataFormat().getFormat("0.0%"));
        style.setAlignment(HorizontalAlignment.CENTER);
        style.setVerticalAlignment(VerticalAlignment.CENTER);
        style.setBorderBottom(BorderStyle.THIN);
        style.setBorderTop(BorderStyle.THIN);
        style.setBorderRight(BorderStyle.THIN);
        style.setBorderLeft(BorderStyle.THIN);
        return style;
    }

    private CellStyle crearEstiloTotales(Workbook workbook) {
        CellStyle style = workbook.createCellStyle();
        Font font = workbook.createFont();
        font.setBold(true);
        font.setColor(IndexedColors.WHITE.getIndex());
        style.setFont(font);
        style.setFillForegroundColor(IndexedColors.ROYAL_BLUE.getIndex());
        style.setFillPattern(FillPatternType.SOLID_FOREGROUND);
        style.setAlignment(HorizontalAlignment.CENTER);
        style.setVerticalAlignment(VerticalAlignment.CENTER);
        style.setBorderBottom(BorderStyle.THIN);
        style.setBorderTop(BorderStyle.THIN);
        style.setBorderRight(BorderStyle.THIN);
        style.setBorderLeft(BorderStyle.THIN);
        return style;
    }

    private String obtenerNombreMes(int numeroMes) {
        String[] meses = {
                "Enero", "Febrero", "Marzo", "Abril", "Mayo", "Junio",
                "Julio", "Agosto", "Septiembre", "Octubre", "Noviembre", "Diciembre"
        };
        if (numeroMes >= 1 && numeroMes <= 12) {
            return meses[numeroMes - 1];
        }
        return "Mes " + numeroMes;
    }
}
