import java.sql.*;
import com.itextpdf.text.*;
import com.itextpdf.text.pdf.*;
import java.io.FileOutputStream;

public class ReportToPDF {
    public static void main(String[] args) {
        exportStudentAllocationsPDF("student_allocations.pdf");
        exportRoomVacancyPDF("room_vacancy_report.pdf");
        System.out.println("PDF reports created !");
    }

    // Watermark event for all PDF pages
    static class WatermarkPageEvent extends PdfPageEventHelper {
        private final Phrase watermark;
        public WatermarkPageEvent(String text) {
            Font wmFont = new Font(Font.FontFamily.HELVETICA, 60, Font.BOLD, new BaseColor(200, 200, 255, 80));
            this.watermark = new Phrase(text, wmFont);
        }
        @Override
        public void onEndPage(PdfWriter writer, Document doc) {
            PdfContentByte cb = writer.getDirectContentUnder();
            ColumnText.showTextAligned(cb, Element.ALIGN_CENTER,
                    watermark, (doc.right() + doc.left()) / 2, (doc.top() + doc.bottom()) / 2, 45);
        }
    }

    public static void exportStudentAllocationsPDF(String fileName) {
        try {
            Document document = new Document();
            PdfWriter writer = PdfWriter.getInstance(document, new FileOutputStream(fileName));
            writer.setPageEvent(new WatermarkPageEvent("MARiO"));
            document.open();

            // Colorful title
            Font titleFont = new Font(Font.FontFamily.HELVETICA, 18, Font.BOLD, BaseColor.BLUE);
            Paragraph title = new Paragraph("Student Allocations Report\n\n", titleFont);
            title.setAlignment(Element.ALIGN_CENTER);
            document.add(title);

            // Colorful table headers
            PdfPTable table = new PdfPTable(5);
            table.setWidthPercentage(100);

            Font headerFont = new Font(Font.FontFamily.HELVETICA, 12, Font.BOLD, BaseColor.WHITE);
            BaseColor headerBg = new BaseColor(34, 90, 149);
            String[] headers = {"Student ID", "Name", "Room Number", "Room Type", "Block Name"};
            for (String h : headers) {
                PdfPCell cell = new PdfPCell(new Phrase(h, headerFont));
                cell.setBackgroundColor(headerBg);
                cell.setHorizontalAlignment(Element.ALIGN_CENTER);
                table.addCell(cell);
            }

            String sql = "SELECT s.student_id, s.name, r.room_number, r.type, b.block_name " +
                    "FROM students s " +
                    "LEFT JOIN rooms r ON s.room_id = r.room_id " +
                    "LEFT JOIN blocks b ON r.block_id = b.block_id";
            try (Connection conn = DatabaseHelper.getConnection();
                 PreparedStatement stmt = conn.prepareStatement(sql);
                 ResultSet rs = stmt.executeQuery()) {
                boolean odd = true;
                while (rs.next()) {
                    BaseColor rowColor = odd ? new BaseColor(210, 230, 255) : BaseColor.WHITE;
                    addColoredCell(table, String.valueOf(rs.getInt("student_id")), rowColor);
                    addColoredCell(table, rs.getString("name"), rowColor);
                    addColoredCell(table, rs.getString("room_number"), rowColor);
                    addColoredCell(table, rs.getString("type"), rowColor);
                    addColoredCell(table, rs.getString("block_name"), rowColor);
                    odd = !odd;
                }
            }
            document.add(table);
            document.close();
        } catch (Exception e) {
            System.out.println("Error generating student allocations PDF.");
            e.printStackTrace();
        }
    }

    public static void exportRoomVacancyPDF(String fileName) {
        try {
            Document document = new Document();
            PdfWriter writer = PdfWriter.getInstance(document, new FileOutputStream(fileName));
            writer.setPageEvent(new WatermarkPageEvent("MARiO"));
            document.open();

            Font titleFont = new Font(Font.FontFamily.HELVETICA, 18, Font.BOLD, BaseColor.RED);
            Paragraph title = new Paragraph("Room Vacancy Report\n\n", titleFont);
            title.setAlignment(Element.ALIGN_CENTER);
            document.add(title);

            PdfPTable table = new PdfPTable(6);
            table.setWidthPercentage(100);

            Font headerFont = new Font(Font.FontFamily.HELVETICA, 12, Font.BOLD, BaseColor.WHITE);
            BaseColor headerBg = new BaseColor(128, 0, 0);
            String[] headers = {"Room Number", "Room Type", "Block Name", "Capacity", "Occupants", "Vacancy"};
            for (String h : headers) {
                PdfPCell cell = new PdfPCell(new Phrase(h, headerFont));
                cell.setBackgroundColor(headerBg);
                cell.setHorizontalAlignment(Element.ALIGN_CENTER);
                table.addCell(cell);
            }

            String sql = "SELECT r.room_number, r.type, b.block_name, r.capacity, r.occupants " +
                    "FROM rooms r JOIN blocks b ON r.block_id = b.block_id";
            try (Connection conn = DatabaseHelper.getConnection();
                 PreparedStatement stmt = conn.prepareStatement(sql);
                 ResultSet rs = stmt.executeQuery()) {
                boolean odd = true;
                while (rs.next()) {
                    int capacity = rs.getInt("capacity");
                    int occupants = rs.getInt("occupants");
                    int vacancy = capacity - occupants;
                    BaseColor rowColor = odd ? new BaseColor(255, 230, 230) : BaseColor.WHITE;
                    addColoredCell(table, rs.getString("room_number"), rowColor);
                    addColoredCell(table, rs.getString("type"), rowColor);
                    addColoredCell(table, rs.getString("block_name"), rowColor);
                    addColoredCell(table, String.valueOf(capacity), rowColor);
                    addColoredCell(table, String.valueOf(occupants), rowColor);
                    addColoredCell(table, String.valueOf(vacancy), rowColor);
                    odd = !odd;
                }
            }
            document.add(table);
            document.close();
        } catch (Exception e) {
            System.out.println("Error generating room vacancy PDF.");
            e.printStackTrace();
        }
    }

    private static void addColoredCell(PdfPTable table, String value, BaseColor bg) {
        PdfPCell cell = new PdfPCell(new Phrase(value));
        cell.setBackgroundColor(bg);
        cell.setHorizontalAlignment(Element.ALIGN_CENTER);
        table.addCell(cell);
    }
}
