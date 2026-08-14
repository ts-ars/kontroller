package com.exempal.shiftcounter.features.report.adapter;

import com.exempal.shiftcounter.features.report.application.ReportRow;
import com.exempal.shiftcounter.features.report.application.ReportView;
import com.exempal.shiftcounter.features.sensor.domain.SensorCatalog;
import org.springframework.stereotype.Component;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

@Component
public class ReportExcelExporter {
    public byte[] export(ReportView report) {
        try (ByteArrayOutputStream output = new ByteArrayOutputStream();
             ZipOutputStream zip = new ZipOutputStream(output, StandardCharsets.UTF_8)) {
            entry(zip, "[Content_Types].xml", """
                    <?xml version="1.0" encoding="UTF-8" standalone="yes"?>
                    <Types xmlns="http://schemas.openxmlformats.org/package/2006/content-types">
                      <Default Extension="rels" ContentType="application/vnd.openxmlformats-package.relationships+xml"/>
                      <Default Extension="xml" ContentType="application/xml"/>
                      <Override PartName="/xl/workbook.xml" ContentType="application/vnd.openxmlformats-officedocument.spreadsheetml.sheet.main+xml"/>
                      <Override PartName="/xl/worksheets/sheet1.xml" ContentType="application/vnd.openxmlformats-officedocument.spreadsheetml.worksheet+xml"/>
                      <Override PartName="/xl/styles.xml" ContentType="application/vnd.openxmlformats-officedocument.spreadsheetml.styles+xml"/>
                    </Types>""");
            entry(zip, "_rels/.rels", """
                    <?xml version="1.0" encoding="UTF-8" standalone="yes"?>
                    <Relationships xmlns="http://schemas.openxmlformats.org/package/2006/relationships">
                      <Relationship Id="rId1" Type="http://schemas.openxmlformats.org/officeDocument/2006/relationships/officeDocument" Target="xl/workbook.xml"/>
                    </Relationships>""");
            entry(zip, "xl/workbook.xml", """
                    <?xml version="1.0" encoding="UTF-8" standalone="yes"?>
                    <workbook xmlns="http://schemas.openxmlformats.org/spreadsheetml/2006/main" xmlns:r="http://schemas.openxmlformats.org/officeDocument/2006/relationships">
                      <sheets><sheet name="Report" sheetId="1" r:id="rId1"/></sheets>
                    </workbook>""");
            entry(zip, "xl/_rels/workbook.xml.rels", """
                    <?xml version="1.0" encoding="UTF-8" standalone="yes"?>
                    <Relationships xmlns="http://schemas.openxmlformats.org/package/2006/relationships">
                      <Relationship Id="rId1" Type="http://schemas.openxmlformats.org/officeDocument/2006/relationships/worksheet" Target="worksheets/sheet1.xml"/>
                      <Relationship Id="rId2" Type="http://schemas.openxmlformats.org/officeDocument/2006/relationships/styles" Target="styles.xml"/>
                    </Relationships>""");
            entry(zip, "xl/styles.xml", """
                    <?xml version="1.0" encoding="UTF-8" standalone="yes"?>
                    <styleSheet xmlns="http://schemas.openxmlformats.org/spreadsheetml/2006/main">
                      <fonts count="2"><font><sz val="11"/><name val="Calibri"/></font><font><b/><sz val="11"/><name val="Calibri"/></font></fonts>
                      <fills count="2"><fill><patternFill patternType="none"/></fill><fill><patternFill patternType="gray125"/></fill></fills>
                      <borders count="1"><border/></borders><cellStyleXfs count="1"><xf/></cellStyleXfs>
                      <cellXfs count="2"><xf fontId="0" fillId="0" borderId="0" xfId="0"/><xf fontId="1" fillId="0" borderId="0" xfId="0" applyFont="1"/></cellXfs>
                    </styleSheet>""");
            entry(zip, "xl/worksheets/sheet1.xml", sheet(report));
            zip.finish();
            return output.toByteArray();
        } catch (IOException exception) {
            throw new IllegalStateException("Could not create report workbook", exception);
        }
    }

    private String sheet(ReportView report) {
        boolean sensorFive = SensorCatalog.SENSOR_5.equals(report.sensorId());
        List<String> headers = sensorFive
                ? List.of("Source", "Type", "Minutes", "Cans", "Reason", "Author")
                : List.of("Type", "Minutes", "Cans", "Reason", "Author");
        StringBuilder xml = new StringBuilder("""
                <?xml version="1.0" encoding="UTF-8" standalone="yes"?>
                <worksheet xmlns="http://schemas.openxmlformats.org/spreadsheetml/2006/main"><sheetData>
                """);
        xml.append("<row r=\"1\">");
        headers.forEach(value -> textCell(xml, value, true));
        xml.append("</row>");
        int rowNumber = 2;
        for (ReportRow row : report.rows()) {
            xml.append("<row r=\"").append(rowNumber++).append("\">");
            if (sensorFive) textCell(xml, row.source(), false);
            textCell(xml, row.type(), false);
            numberCell(xml, row.minutes());
            numberCell(xml, row.cans());
            textCell(xml, row.reason(), false);
            textCell(xml, row.author(), false);
            xml.append("</row>");
        }
        xml.append("<row r=\"").append(rowNumber).append("\">");
        if (sensorFive) textCell(xml, "", true);
        textCell(xml, "Total", true);
        numberCell(xml, report.totalMinutes());
        numberCell(xml, report.totalCans());
        textCell(xml, "", false);
        textCell(xml, "", false);
        return xml.append("</row></sheetData></worksheet>").toString();
    }

    private void textCell(StringBuilder xml, String value, boolean bold) {
        xml.append("<c t=\"inlineStr\"");
        if (bold) xml.append(" s=\"1\"");
        xml.append("><is><t xml:space=\"preserve\">").append(escape(value)).append("</t></is></c>");
    }

    private void numberCell(StringBuilder xml, int value) {
        xml.append("<c><v>").append(value).append("</v></c>");
    }

    private String escape(String value) {
        if (value == null) return "";
        return value.replace("&", "&amp;").replace("<", "&lt;").replace(">", "&gt;")
                .replace("\"", "&quot;").replace("'", "&apos;");
    }

    private void entry(ZipOutputStream zip, String name, String contents) throws IOException {
        zip.putNextEntry(new ZipEntry(name));
        zip.write(contents.getBytes(StandardCharsets.UTF_8));
        zip.closeEntry();
    }
}
