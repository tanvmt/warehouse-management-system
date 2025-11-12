package client.util;

import client.model.ProductSummary;
import client.service.SessionManager;
import com.itextpdf.io.font.constants.StandardFonts;
import com.itextpdf.io.image.ImageData;
import com.itextpdf.io.image.ImageDataFactory;
import com.itextpdf.kernel.colors.Color;
import com.itextpdf.kernel.colors.DeviceRgb;
import com.itextpdf.kernel.font.PdfFont;
import com.itextpdf.kernel.font.PdfFontFactory;
import com.itextpdf.kernel.geom.PageSize;
import com.itextpdf.kernel.pdf.PdfDocument;
import com.itextpdf.kernel.pdf.PdfWriter;
import com.itextpdf.layout.Document;
import com.itextpdf.layout.borders.Border;
import com.itextpdf.layout.element.Cell;
import com.itextpdf.layout.element.Image;
import com.itextpdf.layout.element.Paragraph;
import com.itextpdf.layout.element.Table;
import com.itextpdf.layout.properties.TextAlignment;
import com.itextpdf.layout.properties.UnitValue;
import javafx.application.Platform;
import javafx.embed.swing.SwingFXUtils;
import javafx.scene.Node;
import javafx.scene.SnapshotParameters;
import javafx.scene.chart.BarChart;
import javafx.scene.chart.PieChart;
import javafx.scene.image.WritableImage;

import javax.imageio.ImageIO;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;

public class PdfGenerator {

    public static final String FONT_PATH = "/client/fonts/Inter_24pt-Regular.ttf";
    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("dd/MM/yyyy");
    private static final Color HEADER_BG_COLOR = new DeviceRgb(0, 95, 115); 

    public static void createReport(File file, LocalDate startDate, LocalDate endDate,
                                    List<ProductSummary> summaryData,
                                    PieChart pieChart, BarChart barChart) throws IOException {

        PdfWriter writer = new PdfWriter(file);
        PdfDocument pdfDoc = new PdfDocument(writer);
        Document document = new Document(pdfDoc, PageSize.A4);

        byte[] fontBytes = PdfGenerator.class.getResourceAsStream(FONT_PATH).readAllBytes();
        PdfFont vietnameseFont = PdfFontFactory.createFont(fontBytes, PdfFontFactory.EmbeddingStrategy.PREFER_EMBEDDED);

        document.add(new Paragraph("BÁO CÁO KHO HÀNG")
                .setFont(vietnameseFont).setFontSize(20).setBold()
                .setTextAlignment(TextAlignment.CENTER)
                .setMarginBottom(15));

        addGeneralInfo(document, vietnameseFont, startDate, endDate);

        addKpiSummary(document, vietnameseFont, pieChart);

        document.add(new Paragraph("Bảng tổng hợp Nhập - Xuất - Tồn")
                .setFont(vietnameseFont).setFontSize(14).setBold()
                .setMarginTop(15));
        addSummaryTable(document, vietnameseFont, summaryData);

        document.add(new Paragraph("Biểu đồ trực quan")
                .setFont(vietnameseFont).setFontSize(14).setBold()
                .setMarginTop(15));
        addCharts(document, pieChart, barChart);

        addSignature(document, vietnameseFont);

        document.close();
    }

    private static void addGeneralInfo(Document document, PdfFont font, LocalDate start, LocalDate end) {
        Table infoTable = new Table(UnitValue.createPercentArray(new float[]{1, 2, 1, 2}));
        infoTable.setWidth(UnitValue.createPercentValue(100)).setBorder(Border.NO_BORDER);

        infoTable.addCell(createSimpleCell("Kỳ báo cáo:", font, true));
        infoTable.addCell(createSimpleCell(String.format("%s - %s", start.format(DATE_FORMATTER), end.format(DATE_FORMATTER)), font, false));
        infoTable.addCell(createSimpleCell("Ngày lập:", font, true));
        infoTable.addCell(createSimpleCell(LocalDate.now().format(DATE_FORMATTER), font, false));
        infoTable.addCell(createSimpleCell("Người lập:", font, true));
        infoTable.addCell(createSimpleCell(SessionManager.getFullName(), font, false)); 
        infoTable.addCell(createSimpleCell("", font, false)); 
        infoTable.addCell(createSimpleCell("", font, false)); 
        
        document.add(infoTable);
    }

    private static void addKpiSummary(Document document, PdfFont font, PieChart pieChart) {
        int totalProducts = (int) pieChart.getData().stream().filter(d -> d.getPieValue() > 0).count();
        double totalStock = pieChart.getData().stream().mapToDouble(PieChart.Data::getPieValue).sum();

        document.add(new Paragraph("Số liệu tổng quan (Tồn kho Live)")
                .setFont(font).setFontSize(14).setBold()
                .setMarginTop(15));
                
        document.add(new Paragraph(String.format("- Tổng số loại sản phẩm (đang có hàng): %d", totalProducts))
                .setFont(font).setMarginLeft(10));
        document.add(new Paragraph(String.format("- Tổng số lượng tồn kho: %,.0f", totalStock))
                .setFont(font).setMarginLeft(10));
    }

    private static void addSummaryTable(Document document, PdfFont font, List<ProductSummary> data) {
        Table table = new Table(UnitValue.createPercentArray(new float[]{4, 2, 2, 2}));
        table.setWidth(UnitValue.createPercentValue(100)).setMarginTop(10);

        table.addHeaderCell(createHeaderCell("Sản phẩm", font));
        table.addHeaderCell(createHeaderCell("Tổng Nhập", font));
        table.addHeaderCell(createHeaderCell("Tổng Xuất", font));
        table.addHeaderCell(createHeaderCell("Tồn kho hiện tại", font));

        for (ProductSummary item : data) {
            table.addCell(createBodyCell(item.getProductName(), font, TextAlignment.LEFT));
            table.addCell(createBodyCell(String.valueOf(item.getTotalImport()), font, TextAlignment.RIGHT));
            table.addCell(createBodyCell(String.valueOf(item.getTotalExport()), font, TextAlignment.RIGHT));
            table.addCell(createBodyCell(String.valueOf(item.getTotalInventory()), font, TextAlignment.RIGHT));
        }
        document.add(table);
    }

    private static void addCharts(Document document, PieChart pieChart, BarChart barChart) throws IOException {
        byte[] pieChartImg = snapshotChart(pieChart);
        byte[] barChartImg = snapshotChart(barChart);

        if (pieChartImg == null || barChartImg == null) {
            document.add(new Paragraph("Lỗi: Không thể chụp ảnh biểu đồ.").setFontColor(DeviceRgb.RED));
            return;
        }

        Image pie = new Image(ImageDataFactory.create(pieChartImg)).setAutoScale(true);
        Image bar = new Image(ImageDataFactory.create(barChartImg)).setAutoScale(true);

        Table chartTable = new Table(UnitValue.createPercentArray(new float[]{1, 1}));
        chartTable.setWidth(UnitValue.createPercentValue(100)).setMarginTop(10);

        chartTable.addCell(new Cell().add(pie).setBorder(Border.NO_BORDER));
        chartTable.addCell(new Cell().add(bar).setBorder(Border.NO_BORDER));
        
        document.add(chartTable);
    }

    private static void addSignature(Document document, PdfFont font) {
        document.add(new Paragraph("\n\n\n")
                .setTextAlignment(TextAlignment.RIGHT)
                .setFont(font)
                .setFontSize(12)
                .add("Người lập báo cáo\n")
                .add(String.format("(Ký, họ tên)\n\n\n\n%s", SessionManager.getFullName()))
        );
    }

    private static Cell createHeaderCell(String text, PdfFont font) {
        return new Cell().add(new Paragraph(text)
                .setFont(font)
                .setFontSize(10)
                .setBold()
                .setFontColor(DeviceRgb.WHITE))
                .setBackgroundColor(HEADER_BG_COLOR)
                .setTextAlignment(TextAlignment.CENTER);
    }

    private static Cell createBodyCell(String text, PdfFont font, TextAlignment alignment) {
        return new Cell().add(new Paragraph(text)
                .setFont(font)
                .setFontSize(9))
                .setTextAlignment(alignment);
    }
    
    private static Cell createSimpleCell(String text, PdfFont font, boolean isBold) {
         Paragraph p = new Paragraph(text).setFont(font).setFontSize(10);
         if(isBold) p.setBold();
         return new Cell().add(p).setBorder(Border.NO_BORDER);
    }

    private static byte[] snapshotChart(Node chart) {
        try {
            WritableImage image = chart.snapshot(new SnapshotParameters(), null);
            ByteArrayOutputStream os = new ByteArrayOutputStream();
            ImageIO.write(SwingFXUtils.fromFXImage(image, null), "png", os);
            return os.toByteArray();
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }
}