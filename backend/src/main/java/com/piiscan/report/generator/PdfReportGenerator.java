package com.piiscan.report.generator;

import com.lowagie.text.*;
import com.lowagie.text.pdf.*;
import com.piiscan.report.dto.ReportDataContainer;
import org.springframework.stereotype.Component;

import java.awt.Color;
import java.io.ByteArrayOutputStream;
import java.time.format.DateTimeFormatter;
import java.util.Map;

@Component
public class PdfReportGenerator {

    private static final Font TITLE_FONT = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 18, Color.WHITE);
    private static final Font SUBTITLE_FONT = FontFactory.getFont(FontFactory.HELVETICA, 10, new Color(148, 163, 184));
    private static final Font SECTION_FONT = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 13, new Color(15, 23, 42));
    private static final Font METRIC_VAL_FONT = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 14, new Color(2, 132, 199));
    private static final Font METRIC_LBL_FONT = FontFactory.getFont(FontFactory.HELVETICA, 9, new Color(100, 116, 139));
    private static final Font HEADER_FONT = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 9, Color.WHITE);
    private static final Font CELL_FONT = FontFactory.getFont(FontFactory.HELVETICA, 9, new Color(30, 41, 59));
    private static final Font FOOTER_FONT = FontFactory.getFont(FontFactory.HELVETICA_OBLIQUE, 8, new Color(100, 116, 139));

    public byte[] generatePdf(ReportDataContainer data) {
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        Document document = new Document(PageSize.A4, 36, 36, 36, 36);

        try {
            PdfWriter writer = PdfWriter.getInstance(document, out);
            document.open();

            // 1. Header Banner Box
            PdfPTable headerBanner = new PdfPTable(1);
            headerBanner.setWidthPercentage(100);
            PdfPCell bannerCell = new PdfPCell();
            bannerCell.setBackgroundColor(new Color(15, 23, 42)); // Dark slate background
            bannerCell.setPadding(16);
            bannerCell.setBorder(Rectangle.NO_BORDER);

            Paragraph brand = new Paragraph("PRIVACY AUDIT & DATA GOVERNANCE PLATFORM", SUBTITLE_FONT);
            brand.setSpacingAfter(4);
            Paragraph title = new Paragraph(data.getReportTitle(), TITLE_FONT);
            title.setSpacingAfter(6);
            
            DateTimeFormatter fmt = DateTimeFormatter.ofPattern("dd-MMM-yyyy HH:mm:ss");
            String metaInfo = String.format("Generated: %s | By: %s | Date Range: %s to %s | Target: %s",
                    data.getGeneratedAt().format(fmt),
                    data.getGeneratedBy(),
                    data.getDateFrom().toString(),
                    data.getDateTo().toString(),
                    data.getDatabaseName());
            Paragraph meta = new Paragraph(metaInfo, SUBTITLE_FONT);

            bannerCell.addElement(brand);
            bannerCell.addElement(title);
            bannerCell.addElement(meta);
            headerBanner.addCell(bannerCell);
            document.add(headerBanner);

            document.add(new Paragraph("\n"));

            // 2. Executive Summary Metrics Cards
            Paragraph section1 = new Paragraph("Executive Summary", SECTION_FONT);
            section1.setSpacingAfter(10);
            document.add(section1);

            PdfPTable kpiTable = new PdfPTable(4);
            kpiTable.setWidthPercentage(100);
            kpiTable.setSpacingAfter(15);

            addKpiCell(kpiTable, "Total Findings", String.valueOf(data.getTotalPiiFindings()));
            addKpiCell(kpiTable, "Critical Risk", String.valueOf(data.getCriticalRiskCount()));
            addKpiCell(kpiTable, "High Risk", String.valueOf(data.getHighRiskCount()));
            addKpiCell(kpiTable, "Overall Risk Score", String.format("%.1f", data.getOverallRiskScore()));

            document.add(kpiTable);

            // 3. Security Assurance Callout Box
            PdfPTable securityBox = new PdfPTable(1);
            securityBox.setWidthPercentage(100);
            securityBox.setSpacingAfter(15);
            PdfPCell secCell = new PdfPCell();
            secCell.setBackgroundColor(new Color(240, 253, 244)); // Light emerald
            secCell.setBorderColor(new Color(187, 247, 208));
            secCell.setPadding(10);
            Paragraph secText = new Paragraph("Security & Zero-PII Assurance: This report contains aggregated metadata, risk counts, and policy compliance statuses. Original sensitive values (Aadhaar, PAN, Card Numbers, Passwords, Tokens) are strictly unrecorded.", FOOTER_FONT);
            secCell.addElement(secText);
            securityBox.addCell(secCell);
            document.add(securityBox);

            // 4. Detailed Data Table
            Paragraph section2 = new Paragraph("Detailed Analysis & Records Breakdown", SECTION_FONT);
            section2.setSpacingAfter(10);
            document.add(section2);

            if (data.getRows() != null && !data.getRows().isEmpty()) {
                Map<String, Object> firstRow = data.getRows().get(0);
                int colCount = firstRow.keySet().size();
                PdfPTable dataTable = new PdfPTable(colCount);
                dataTable.setWidthPercentage(100);
                dataTable.setSpacingAfter(15);

                // Table Headers
                for (String colName : firstRow.keySet()) {
                    PdfPCell th = new PdfPCell(new Phrase(formatColHeader(colName), HEADER_FONT));
                    th.setBackgroundColor(new Color(30, 41, 59));
                    th.setPadding(6);
                    th.setHorizontalAlignment(Element.ALIGN_CENTER);
                    dataTable.addCell(th);
                }

                // Table Rows
                boolean alternate = false;
                for (Map<String, Object> row : data.getRows()) {
                    Color rowBg = alternate ? new Color(248, 250, 252) : Color.WHITE;
                    for (Object val : row.values()) {
                        PdfPCell td = new PdfPCell(new Phrase(val != null ? val.toString() : "-", CELL_FONT));
                        td.setBackgroundColor(rowBg);
                        td.setPadding(5);
                        dataTable.addCell(td);
                    }
                    alternate = !alternate;
                }
                document.add(dataTable);
            } else {
                Paragraph noData = new Paragraph("No findings available for the selected parameters.", FOOTER_FONT);
                document.add(noData);
            }

            // 5. Footer Metadata
            Paragraph footer = new Paragraph("Confidential — Privacy Audit & PII Scanning Engine | End of Document", FOOTER_FONT);
            footer.setAlignment(Element.ALIGN_CENTER);
            footer.setSpacingBefore(20);
            document.add(footer);

            document.close();
        } catch (Exception e) {
            throw new RuntimeException("PDF Generation Error: " + e.getMessage(), e);
        }

        return out.toByteArray();
    }

    private void addKpiCell(PdfPTable table, String label, String val) {
        PdfPCell cell = new PdfPCell();
        cell.setBackgroundColor(new Color(241, 245, 249));
        cell.setBorderColor(new Color(226, 232, 240));
        cell.setPadding(8);
        cell.setHorizontalAlignment(Element.ALIGN_CENTER);

        Paragraph v = new Paragraph(val, METRIC_VAL_FONT);
        v.setAlignment(Element.ALIGN_CENTER);
        Paragraph l = new Paragraph(label, METRIC_LBL_FONT);
        l.setAlignment(Element.ALIGN_CENTER);

        cell.addElement(v);
        cell.addElement(l);
        table.addCell(cell);
    }

    private String formatColHeader(String raw) {
        if (raw == null) return "";
        StringBuilder sb = new StringBuilder();
        for (char c : raw.toCharArray()) {
            if (Character.isUpperCase(c)) {
                sb.append(" ").append(c);
            } else {
                sb.append(c);
            }
        }
        return sb.toString().toUpperCase().trim();
    }
}
