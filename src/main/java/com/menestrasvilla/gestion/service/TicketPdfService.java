package com.menestrasvilla.gestion.service;

import com.lowagie.text.*;
import com.lowagie.text.pdf.PdfPCell;
import com.lowagie.text.pdf.PdfPTable;
import com.lowagie.text.pdf.PdfWriter;
import com.menestrasvilla.gestion.entity.Configuracion;
import com.menestrasvilla.gestion.entity.DetalleVenta;
import com.menestrasvilla.gestion.entity.Venta;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.format.DateTimeFormatter;

@Service
public class TicketPdfService {

    public void generarTicketPdf(Venta venta, Configuracion config, HttpServletResponse response) throws IOException {
        Rectangle pageSize = new Rectangle(250f, 700f); 
        Document document = new Document(pageSize, 10, 10, 15, 15);
        PdfWriter.getInstance(document, response.getOutputStream());

        document.open();

        Font fontNombre = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 14);
        Font fontNormal = FontFactory.getFont(FontFactory.HELVETICA, 8);
        Font fontBold = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 9);
        Font fontMini = FontFactory.getFont(FontFactory.HELVETICA, 7);

        Paragraph pNombre = new Paragraph(config != null ? config.getNombreComercial().toUpperCase() : "MENESTRAS VILLA", fontNombre);
        pNombre.setAlignment(Element.ALIGN_CENTER);
        document.add(pNombre);

        String direccion = config != null ? config.getDireccion() : "Sede Lima";
        Paragraph pDireccion = new Paragraph(direccion, fontMini);
        pDireccion.setAlignment(Element.ALIGN_CENTER);
        document.add(pDireccion);

        Paragraph pRuc = new Paragraph("RUC: " + (config != null ? config.getRuc() : "10190220961"), fontBold);
        pRuc.setAlignment(Element.ALIGN_CENTER);
        document.add(pRuc);


        String tipoLabel = "NOTA DE VENTA";
        if ("BOLETA".equals(venta.getTipoComprobante())) tipoLabel = "BOLETA DE VENTA ELECTRÓNICA";
        if ("FACTURA".equals(venta.getTipoComprobante())) tipoLabel = "FACTURA ELECTRÓNICA";

        Paragraph pTipo = new Paragraph(tipoLabel, fontBold);
        pTipo.setAlignment(Element.ALIGN_CENTER);
        document.add(pTipo);

        Paragraph pTicket = new Paragraph("TICKET: " + venta.getId(), fontBold);
        pTicket.setAlignment(Element.ALIGN_CENTER);
        document.add(pTicket);

        document.add(new Paragraph("-----------------------------------------------------------", fontMini));

        DateTimeFormatter dtf = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");
        document.add(new Paragraph("FECHA: " + venta.getFecha().format(dtf), fontNormal));
        
        if (venta.getClienteNombre() != null && !venta.getClienteNombre().isEmpty()) {
            document.add(new Paragraph("CLIENTE: " + venta.getClienteNombre().toUpperCase(), fontNormal));
        }
        
        if (venta.getClienteDoc() != null && !venta.getClienteDoc().isEmpty()) {
            String docLabel = venta.getTipoComprobante().equals("FACTURA") ? "RUC: " : "DNI: ";
            document.add(new Paragraph(docLabel + venta.getClienteDoc(), fontNormal));
        }

        document.add(new Paragraph("PAGO: " + venta.getMetodoPago(), fontNormal));
        document.add(new Paragraph("CAJERO: " + venta.getUsuario().getNombreCompleto().toUpperCase(), fontNormal));

        document.add(new Paragraph("-----------------------------------------------------------", fontMini));

        PdfPTable table = new PdfPTable(3);
        table.setWidthPercentage(100);
        try {
            table.setWidths(new float[]{1.2f, 3.5f, 1.5f});
        } catch (DocumentException e) { e.printStackTrace(); }

        table.addCell(crearCelda("CANT", fontBold, Element.ALIGN_LEFT, true));
        table.addCell(crearCelda("DESCRIPCIÓN", fontBold, Element.ALIGN_LEFT, true));
        table.addCell(crearCelda("TOTAL", fontBold, Element.ALIGN_RIGHT, true));

        for (DetalleVenta d : venta.getDetalles()) {
            String unidad = d.getUnidadMedida().equals("KILO") ? "kg" : "sc";
            table.addCell(crearCelda(String.format("%.1f", d.getCantidadUnidades()) + unidad, fontNormal, Element.ALIGN_LEFT, false));
            table.addCell(crearCelda(d.getProducto().getNombre().toUpperCase(), fontNormal, Element.ALIGN_LEFT, false));
            table.addCell(crearCelda(String.format("%.2f", d.getSubtotal()), fontNormal, Element.ALIGN_RIGHT, false));
        }
        document.add(table);

        document.add(new Paragraph("-----------------------------------------------------------", fontMini));

        String moneda = config != null ? config.getSimboloMoneda() : "S/ ";
        if (!"NOTA".equals(venta.getTipoComprobante())) {
            BigDecimal igvP = config != null ? config.getPorcentajeIgv() : new BigDecimal("18.0");
            BigDecimal div = BigDecimal.ONE.add(igvP.divide(new BigDecimal("100")));
            BigDecimal gravada = venta.getTotalNeto().divide(div, 2, RoundingMode.HALF_UP);
            BigDecimal igvM = venta.getTotalNeto().subtract(gravada);

            document.add(crearParrafo("OP. GRAVADA: " + moneda + String.format("%.2f", gravada), fontNormal, Element.ALIGN_RIGHT));
            document.add(crearParrafo("IGV (" + igvP + "%): " + moneda + String.format("%.2f", igvM), fontNormal, Element.ALIGN_RIGHT));
        }

        Paragraph pTotal = new Paragraph("TOTAL: " + moneda + String.format("%.2f", venta.getTotalNeto()), fontNombre);
        pTotal.setAlignment(Element.ALIGN_RIGHT);
        document.add(pTotal);

        document.add(new Paragraph("-----------------------------------------------------------", fontMini));

        String msg = (config != null && config.getMensajeTicket() != null) ? config.getMensajeTicket() : "¡GRACIAS POR SU COMPRA!";
        Paragraph pFooter = new Paragraph(msg, fontBold);
        pFooter.setAlignment(Element.ALIGN_CENTER);
        document.add(pFooter);

        Paragraph pLegal = new Paragraph("Representación impresa de la\n" + venta.getTipoComprobante(), fontMini);
        pLegal.setAlignment(Element.ALIGN_CENTER);
        document.add(pLegal);

        document.close();
    }

    private PdfPCell crearCelda(String t, Font f, int align, boolean border) {
        PdfPCell c = new PdfPCell(new Phrase(t, f));
        c.setBorder(border ? Rectangle.BOTTOM : Rectangle.NO_BORDER);
        c.setHorizontalAlignment(align);
        c.setPaddingBottom(3);
        return c;
    }

    private Paragraph crearParrafo(String t, Font f, int align) {
        Paragraph p = new Paragraph(t, f);
        p.setAlignment(align);
        return p;
    }
}