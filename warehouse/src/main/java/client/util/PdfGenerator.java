package client.util;

import common.model.Transaction;
import com.itextpdf.io.font.constants.StandardFonts;
import com.itextpdf.kernel.colors.ColorConstants;
import com.itextpdf.kernel.font.PdfFont;
import com.itextpdf.kernel.font.PdfFontFactory;
import com.itextpdf.kernel.pdf.PdfDocument;
import com.itextpdf.kernel.pdf.PdfWriter;
import com.itextpdf.layout.Document;
import com.itextpdf.layout.element.Cell;
import com.itextpdf.layout.element.Paragraph;
import com.itextpdf.layout.element.Table;
import com.itextpdf.layout.properties.TextAlignment;
import com.itextpdf.layout.properties.UnitValue;

import java.io.File;
import java.io.IOException;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;

public class PdfGenerator {
    public static final String FONT_PATH = "/client/fonts/Inter_24pt-Regular.ttf";

    public static void createReport(File file, List<Transaction> transactions, 
                                    LocalDate startDate, LocalDate endDate) throws IOException {

        PdfWriter writer = new PdfWriter(file);
        PdfDocument pdf = new PdfDocument(writer);
        Document document = new Document(pdf);

        PdfFont font = PdfFontFactory.createFont(FONT_PATH, "Identity-H", PdfFontFactory.EmbeddingStrategy.PREFER_EMBEDDED);
        document.setFont(font);

        document.add(new Paragraph("BÁO CÁO GIAO DỊCH KHO HÀNG")
                .setFontSize(18)
                .setBold()
                .setTextAlignment(TextAlignment.CENTER));

        DateTimeFormatter dtf = DateTimeFormatter.ofPattern("dd/MM/yyyy");
        document.add(new Paragraph(String.format("Từ ngày: %s - Đến ngày: %s", 
                                    startDate.format(dtf), endDate.format(dtf)))
                .setFontSize(12)
                .setTextAlignment(TextAlignment.CENTER)
                .setMarginBottom(20));

        Table table = new Table(UnitValue.createPercentArray(new float[]{3, 2, 2, 4, 2, 5}));
        table.setWidth(UnitValue.createPercentValue(100));

        addTableHeader(table);

        for (Transaction tx : transactions) {
            table.addCell(new Cell().add(new Paragraph(tx.getTimestamp())));
            table.addCell(new Cell().add(new Paragraph(tx.getClientName())));
            table.addCell(new Cell().add(new Paragraph(tx.getAction())));
            table.addCell(new Cell().add(new Paragraph(tx.getProduct())));
            table.addCell(new Cell().add(new Paragraph(String.valueOf(tx.getQuantity())))
                                    .setTextAlignment(TextAlignment.RIGHT));
            table.addCell(new Cell().add(new Paragraph(tx.getResult())));
        }

        document.add(table);
        document.close();
    }

    private static void addTableHeader(Table table) {
        table.addHeaderCell(new Cell().add(new Paragraph("Thời gian")).setBold().setBackgroundColor(ColorConstants.LIGHT_GRAY));
        table.addHeaderCell(new Cell().add(new Paragraph("Nhân viên")).setBold().setBackgroundColor(ColorConstants.LIGHT_GRAY));
        table.addHeaderCell(new Cell().add(new Paragraph("Hành động")).setBold().setBackgroundColor(ColorConstants.LIGHT_GRAY));
        table.addHeaderCell(new Cell().add(new Paragraph("Sản phẩm")).setBold().setBackgroundColor(ColorConstants.LIGHT_GRAY));
        table.addHeaderCell(new Cell().add(new Paragraph("Số lượng")).setBold().setBackgroundColor(ColorConstants.LIGHT_GRAY));
        table.addHeaderCell(new Cell().add(new Paragraph("Kết quả")).setBold().setBackgroundColor(ColorConstants.LIGHT_GRAY));
    }
}