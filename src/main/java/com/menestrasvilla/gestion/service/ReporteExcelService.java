package com.menestrasvilla.gestion.service;

import com.menestrasvilla.gestion.entity.Caja;
import com.menestrasvilla.gestion.entity.Compra;
import com.menestrasvilla.gestion.entity.Gasto;
import com.menestrasvilla.gestion.entity.Venta;
import com.menestrasvilla.gestion.entity.MovimientoInventario;
import jakarta.servlet.ServletOutputStream;
import jakarta.servlet.http.HttpServletResponse;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.math.BigDecimal;
import java.time.format.DateTimeFormatter;
import java.util.List;

@Service
public class ReporteExcelService {

    public void exportarVentas(List<Venta> ventas, HttpServletResponse response) throws IOException {
        Workbook workbook = new XSSFWorkbook();
        Sheet sheet = workbook.createSheet("Reporte de Ventas");
        
        Row headerRow = sheet.createRow(0);
        String[] columnas = {"ID Ticket", "Fecha", "Cliente / Doc", "Comprobante", "Método Pago", "Cajero", "Total (S/)"};
        
        CellStyle headerStyle = workbook.createCellStyle();
        headerStyle.setFillForegroundColor(IndexedColors.LIGHT_BLUE.getIndex());
        headerStyle.setFillPattern(FillPatternType.SOLID_FOREGROUND);
        Font font = workbook.createFont();
        font.setColor(IndexedColors.WHITE.getIndex());
        font.setBold(true);
        headerStyle.setFont(font);

        for (int i = 0; i < columnas.length; i++) {
            Cell cell = headerRow.createCell(i);
            cell.setCellValue(columnas[i]);
            cell.setCellStyle(headerStyle);
        }

        int rowNum = 1;
        BigDecimal totalGeneral = BigDecimal.ZERO;
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");

        for (Venta v : ventas) {
            Row row = sheet.createRow(rowNum++);
            row.createCell(0).setCellValue("#" + v.getId());
            row.createCell(1).setCellValue(v.getFecha().format(formatter));
            
            String cliente = v.getClienteNombre() != null && !v.getClienteNombre().isEmpty() ? v.getClienteNombre() : "Público General";
            if (v.getClienteDoc() != null && !v.getClienteDoc().isEmpty()) cliente += " (" + v.getClienteDoc() + ")";
            row.createCell(2).setCellValue(cliente);
            
            row.createCell(3).setCellValue(v.getTipoComprobante());
            row.createCell(4).setCellValue(v.getMetodoPago());
            row.createCell(5).setCellValue(v.getUsuario().getUsername());
            row.createCell(6).setCellValue(v.getTotalNeto().doubleValue());
            
            totalGeneral = totalGeneral.add(v.getTotalNeto());
        }

        Row footerRow = sheet.createRow(rowNum);
        footerRow.createCell(5).setCellValue("TOTAL RECAUDADO:");
        footerRow.createCell(6).setCellValue(totalGeneral.doubleValue());

        for (int i = 0; i < columnas.length; i++) {
            sheet.autoSizeColumn(i);
        }

        escribirRespuesta(workbook, response, "Reporte_Ventas.xlsx");
    }

    public void exportarCompras(List<Compra> compras, HttpServletResponse response) throws IOException {
        Workbook workbook = new XSSFWorkbook();
        Sheet sheet = workbook.createSheet("Reporte de Compras");
        
        Row headerRow = sheet.createRow(0);
        String[] columnas = {"Código", "Fecha Ingreso", "Proveedor", "RUC", "Registrado Por", "Total Invertido (S/)"};
        
        CellStyle headerStyle = workbook.createCellStyle();
        headerStyle.setFillForegroundColor(IndexedColors.ROSE.getIndex());
        headerStyle.setFillPattern(FillPatternType.SOLID_FOREGROUND);
        Font font = workbook.createFont();
        font.setColor(IndexedColors.WHITE.getIndex());
        font.setBold(true);
        headerStyle.setFont(font);

        for (int i = 0; i < columnas.length; i++) {
            Cell cell = headerRow.createCell(i);
            cell.setCellValue(columnas[i]);
            cell.setCellStyle(headerStyle);
        }

        int rowNum = 1;
        BigDecimal totalGeneral = BigDecimal.ZERO;
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");

        for (Compra c : compras) {
            Row row = sheet.createRow(rowNum++);
            row.createCell(0).setCellValue("CMP-" + c.getId());
            row.createCell(1).setCellValue(c.getCreadoEn().format(formatter));
            row.createCell(2).setCellValue(c.getProveedor().getRazonSocial());
            row.createCell(3).setCellValue(c.getProveedor().getRuc());
            row.createCell(4).setCellValue(c.getUsuario().getUsername());
            row.createCell(5).setCellValue(c.getTotalCompra().doubleValue());
            
            totalGeneral = totalGeneral.add(c.getTotalCompra());
        }

        Row footerRow = sheet.createRow(rowNum);
        footerRow.createCell(4).setCellValue("TOTAL INVERTIDO:");
        footerRow.createCell(5).setCellValue(totalGeneral.doubleValue());

        for (int i = 0; i < columnas.length; i++) {
            sheet.autoSizeColumn(i);
        }

        escribirRespuesta(workbook, response, "Reporte_Compras.xlsx");
    }
    
    public void exportarCajas(List<Caja> cajas, HttpServletResponse response) throws IOException {
        Workbook workbook = new XSSFWorkbook();
        Sheet sheet = workbook.createSheet("Reporte de Cajas");
        
        Row headerRow = sheet.createRow(0);
        String[] columnas = {"Turno", "Apertura", "Cierre", "Cajero", "Fondo Base (S/)", "Ventas Sistema (S/)", "Dinero Entregado (S/)", "Diferencia (S/)"};
        
        CellStyle headerStyle = workbook.createCellStyle();
        headerStyle.setFillForegroundColor(IndexedColors.DARK_YELLOW.getIndex());
        headerStyle.setFillPattern(FillPatternType.SOLID_FOREGROUND);
        Font font = workbook.createFont();
        font.setColor(IndexedColors.WHITE.getIndex());
        font.setBold(true);
        headerStyle.setFont(font);

        for (int i = 0; i < columnas.length; i++) {
            Cell cell = headerRow.createCell(i);
            cell.setCellValue(columnas[i]);
            cell.setCellStyle(headerStyle);
        }

        int rowNum = 1;
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");

        for (Caja c : cajas) {
            Row row = sheet.createRow(rowNum++);
            row.createCell(0).setCellValue("#" + c.getId());
            row.createCell(1).setCellValue(c.getFechaApertura().format(formatter));
            row.createCell(2).setCellValue(c.getFechaCierre() != null ? c.getFechaCierre().format(formatter) : "Pendiente");
            row.createCell(3).setCellValue(c.getUsuario().getUsername());
            row.createCell(4).setCellValue(c.getMontoInicial() != null ? c.getMontoInicial().doubleValue() : 0);
            row.createCell(5).setCellValue(c.getTotalVentas() != null ? c.getTotalVentas().doubleValue() : 0);
            row.createCell(6).setCellValue(c.getMontoFinalReal() != null ? c.getMontoFinalReal().doubleValue() : 0);
            row.createCell(7).setCellValue(c.getDiferencia() != null ? c.getDiferencia().doubleValue() : 0);
        }

        for (int i = 0; i < columnas.length; i++) {
            sheet.autoSizeColumn(i);
        }

        escribirRespuesta(workbook, response, "Reporte_Cajas.xlsx");
    }

    public void exportarKardex(List<MovimientoInventario> movimientos, HttpServletResponse response) throws IOException {
        Workbook workbook = new XSSFWorkbook();
        Sheet sheet = workbook.createSheet("Kardex de Movimientos");
        
        Row headerRow = sheet.createRow(0);
        String[] columnas = {"Fecha y Hora", "Producto", "Tipo", "Cantidad", "Unidad", "Motivo / Referencia", "Responsable"};
        
        CellStyle headerStyle = workbook.createCellStyle();
        headerStyle.setFillForegroundColor(IndexedColors.GREY_50_PERCENT.getIndex());
        headerStyle.setFillPattern(FillPatternType.SOLID_FOREGROUND);
        Font font = workbook.createFont();
        font.setColor(IndexedColors.WHITE.getIndex());
        font.setBold(true);
        headerStyle.setFont(font);

        for (int i = 0; i < columnas.length; i++) {
            Cell cell = headerRow.createCell(i);
            cell.setCellValue(columnas[i]);
            cell.setCellStyle(headerStyle);
        }

        int rowNum = 1;
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");

        for (MovimientoInventario mov : movimientos) {
            Row row = sheet.createRow(rowNum++);
            
            row.createCell(0).setCellValue(mov.getFechaHora() != null ? mov.getFechaHora().format(formatter) : "");
            row.createCell(1).setCellValue(mov.getProducto().getNombre());
            row.createCell(2).setCellValue(mov.getTipoMovimiento());
            
            String prefijo = mov.getTipoMovimiento().equals("ENTRADA") ? "+" : (mov.getTipoMovimiento().equals("SALIDA") ? "-" : "");
            row.createCell(3).setCellValue(prefijo + mov.getCantidad());
            
            row.createCell(4).setCellValue(mov.getUnidadMedida());
            row.createCell(5).setCellValue(mov.getMotivo());
            row.createCell(6).setCellValue(mov.getUsuario().getUsername());
        }

        for (int i = 0; i < columnas.length; i++) {
            sheet.autoSizeColumn(i);
        }

        escribirRespuesta(workbook, response, "Kardex_Movimientos.xlsx");
    }

    public void exportarGastos(List<Gasto> gastos, HttpServletResponse response) throws IOException {
        Workbook workbook = new XSSFWorkbook();
        Sheet sheet = workbook.createSheet("Reporte de Gastos");
        
        Row headerRow = sheet.createRow(0);
        String[] columnas = {"ID", "Fecha", "Categoría", "Frecuencia", "Descripción", "Responsable", "Monto (S/)"};
        
        CellStyle headerStyle = workbook.createCellStyle();
        headerStyle.setFillForegroundColor(IndexedColors.DARK_RED.getIndex());
        headerStyle.setFillPattern(FillPatternType.SOLID_FOREGROUND);
        Font font = workbook.createFont();
        font.setColor(IndexedColors.WHITE.getIndex());
        font.setBold(true);
        headerStyle.setFont(font);

        for (int i = 0; i < columnas.length; i++) {
            Cell cell = headerRow.createCell(i);
            cell.setCellValue(columnas[i]);
            cell.setCellStyle(headerStyle);
        }

        int rowNum = 1;
        BigDecimal totalGeneral = BigDecimal.ZERO;
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy"); 

        for (Gasto g : gastos) {
            Row row = sheet.createRow(rowNum++);
            row.createCell(0).setCellValue(g.getId());
            row.createCell(1).setCellValue(g.getFechaPago().format(formatter));
            row.createCell(2).setCellValue(g.getCategoria());
            row.createCell(3).setCellValue(g.getFrecuencia());
            row.createCell(4).setCellValue(g.getDescripcion());
            row.createCell(5).setCellValue(g.getResponsable());
            row.createCell(6).setCellValue(g.getMonto().doubleValue());
            
            totalGeneral = totalGeneral.add(g.getMonto());
        }

        Row footerRow = sheet.createRow(rowNum);
        footerRow.createCell(5).setCellValue("TOTAL EGRESOS:");
        footerRow.createCell(6).setCellValue(totalGeneral.doubleValue());

        for (int i = 0; i < columnas.length; i++) {
            sheet.autoSizeColumn(i);
        }

        escribirRespuesta(workbook, response, "Reporte_Egresos_Administrativos.xlsx");
    }

    private void escribirRespuesta(Workbook workbook, HttpServletResponse response, String fileName) throws IOException {
        response.setContentType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet");
        response.setHeader("Content-Disposition", "attachment; filename=" + fileName);
        ServletOutputStream outputStream = response.getOutputStream();
        workbook.write(outputStream);
        workbook.close();
        outputStream.close();
    }
}