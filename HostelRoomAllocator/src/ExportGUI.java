import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import com.itextpdf.text.*;
import com.itextpdf.text.Font;
import com.itextpdf.text.pdf.*;
import java.io.FileOutputStream;
import java.sql.*;

public class ExportGUI extends JFrame {
    private JComboBox<String> reportTypeBox, formatBox;

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

    public ExportGUI() {
        setTitle("Export Reports");
        setSize(420, 180);
        setLocationRelativeTo(null);
        setDefaultCloseOperation(DISPOSE_ON_CLOSE);

        JPanel panel = new JPanel(new GridLayout(3, 2, 12, 14));
        panel.setBorder(BorderFactory.createEmptyBorder(22, 38, 22, 38));

        reportTypeBox = new JComboBox<>(new String[]{
                "Student Allocations",
                "Room Vacancies",
                "Student List"
        });
        formatBox = new JComboBox<>(new String[]{"PDF", "CSV"});

        panel.add(new JLabel("Report Type:")); panel.add(reportTypeBox);
        panel.add(new JLabel("Export as:")); panel.add(formatBox);

        JButton exportBtn = new JButton("Export");
        panel.add(exportBtn); panel.add(new JLabel());

        exportBtn.addActionListener(e -> exportReport());

        add(panel);
        setVisible(true);
    }

    private void exportReport() {
        String type = (String) reportTypeBox.getSelectedItem();
        String format = (String) formatBox.getSelectedItem();
        String fileName = type.replace(" ", "_").toLowerCase() + (format.equals("PDF") ? "_colorful.pdf" : ".csv");

        try {
            if (type.equals("Student Allocations")) {
                if (format.equals("PDF")) exportStudentAllocationsPDF(fileName);
                else exportStudentAllocationsCSV(fileName);
            } else if (type.equals("Room Vacancies")) {
                if (format.equals("PDF")) exportRoomVacanciesPDF(fileName);
                else exportRoomVacanciesCSV(fileName);
            } else if (type.equals("Student List")) {
                if (format.equals("PDF")) exportStudentListPDF(fileName);
                else exportStudentListCSV(fileName);
            }
            JOptionPane.showMessageDialog(this, "Exported: " + new File(fileName).getAbsolutePath(),
                    "Success", JOptionPane.INFORMATION_MESSAGE);
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, "Export failed: " + ex.getMessage(),
                    "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    // Colorful PDF exports with MARiO watermark
    private void exportStudentAllocationsPDF(String fileName) throws Exception {
        Document doc = new Document();
        PdfWriter writer = PdfWriter.getInstance(doc, new FileOutputStream(fileName));
        writer.setPageEvent(new WatermarkPageEvent("MARiO"));
        doc.open();

        Font titleFont = new Font(Font.FontFamily.HELVETICA, 18, Font.BOLD, new BaseColor(0, 70, 200));
        Paragraph title = new Paragraph("Student Allocations Report\n\n", titleFont);
        title.setAlignment(Element.ALIGN_CENTER);
        doc.add(title);

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
                "FROM students s LEFT JOIN rooms r ON s.room_id = r.room_id LEFT JOIN blocks b ON r.block_id = b.block_id";
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
        doc.add(table); doc.close();
    }

    private void exportRoomVacanciesPDF(String fileName) throws Exception {
        Document doc = new Document();
        PdfWriter writer = PdfWriter.getInstance(doc, new FileOutputStream(fileName));
        writer.setPageEvent(new WatermarkPageEvent("MARiO"));
        doc.open();

        Font titleFont = new Font(Font.FontFamily.HELVETICA, 18, Font.BOLD, new BaseColor(200, 0, 0));
        Paragraph title = new Paragraph("Room Vacancy Report\n\n", titleFont);
        title.setAlignment(Element.ALIGN_CENTER);
        doc.add(title);

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
                int capacity = rs.getInt("capacity"), occ = rs.getInt("occupants");
                BaseColor rowColor = odd ? new BaseColor(255, 230, 230) : BaseColor.WHITE;
                addColoredCell(table, rs.getString("room_number"), rowColor);
                addColoredCell(table, rs.getString("type"), rowColor);
                addColoredCell(table, rs.getString("block_name"), rowColor);
                addColoredCell(table, String.valueOf(capacity), rowColor);
                addColoredCell(table, String.valueOf(occ), rowColor);
                addColoredCell(table, String.valueOf(capacity - occ), rowColor);
                odd = !odd;
            }
        }
        doc.add(table); doc.close();
    }

    private void exportStudentListPDF(String fileName) throws Exception {
        Document doc = new Document();
        PdfWriter writer = PdfWriter.getInstance(doc, new FileOutputStream(fileName));
        writer.setPageEvent(new WatermarkPageEvent("MARiO"));
        doc.open();

        Font titleFont = new Font(Font.FontFamily.HELVETICA, 18, Font.BOLD, new BaseColor(0, 128, 0));
        Paragraph title = new Paragraph("Student List Report\n\n", titleFont);
        title.setAlignment(Element.ALIGN_CENTER);
        doc.add(title);

        PdfPTable table = new PdfPTable(9);
        table.setWidthPercentage(100);

        Font headerFont = new Font(Font.FontFamily.HELVETICA, 10, Font.BOLD, BaseColor.WHITE);
        BaseColor headerBg = new BaseColor(0, 128, 0);
        String[] headers = {"Student ID", "Name", "Roll Number", "Year", "Course", "Gender", "Mobile", "Parent", "Address"};
        for (String h : headers) {
            PdfPCell cell = new PdfPCell(new Phrase(h, headerFont));
            cell.setBackgroundColor(headerBg);
            cell.setHorizontalAlignment(Element.ALIGN_CENTER);
            table.addCell(cell);
        }

        String sql = "SELECT student_id, name, roll_number, year, course, gender, mobile_number, parent_details, address FROM students";
        try (Connection conn = DatabaseHelper.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {
            boolean odd = true;
            while (rs.next()) {
                BaseColor rowColor = odd ? new BaseColor(230, 255, 230) : BaseColor.WHITE;
                addColoredCell(table, String.valueOf(rs.getInt("student_id")), rowColor);
                addColoredCell(table, rs.getString("name"), rowColor);
                addColoredCell(table, rs.getString("roll_number"), rowColor);
                addColoredCell(table, String.valueOf(rs.getInt("year")), rowColor);
                addColoredCell(table, rs.getString("course"), rowColor);
                addColoredCell(table, rs.getString("gender"), rowColor);
                addColoredCell(table, rs.getString("mobile_number"), rowColor);
                addColoredCell(table, rs.getString("parent_details"), rowColor);
                addColoredCell(table, rs.getString("address"), rowColor);
                odd = !odd;
            }
        }
        doc.add(table); doc.close();
    }

    private void addColoredCell(PdfPTable table, String value, BaseColor bg) {
        PdfPCell cell = new PdfPCell(new Phrase(value));
        cell.setBackgroundColor(bg);
        cell.setHorizontalAlignment(Element.ALIGN_CENTER);
        table.addCell(cell);
    }

    // CSV exports (same as before)
    private void exportStudentAllocationsCSV(String fileName) throws Exception {
        String sql = "SELECT s.student_id, s.name, r.room_number, r.type, b.block_name " +
                "FROM students s LEFT JOIN rooms r ON s.room_id = r.room_id LEFT JOIN blocks b ON r.block_id = b.block_id";
        try (Connection conn = DatabaseHelper.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery();
             FileWriter writer = new FileWriter(fileName)) {
            writer.write("Student ID,Name,Room Number,Room Type,Block Name\n");
            while (rs.next()) {
                writer.write(rs.getInt("student_id") + "," +
                        rs.getString("name") + "," +
                        rs.getString("room_number") + "," +
                        rs.getString("type") + "," +
                        rs.getString("block_name") + "\n");
            }
        }
    }

    private void exportRoomVacanciesCSV(String fileName) throws Exception {
        String sql = "SELECT r.room_number, r.type, b.block_name, r.capacity, r.occupants " +
                "FROM rooms r JOIN blocks b ON r.block_id = b.block_id";
        try (Connection conn = DatabaseHelper.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery();
             FileWriter writer = new FileWriter(fileName)) {
            writer.write("Room Number,Room Type,Block Name,Capacity,Occupants,Vacancy\n");
            while (rs.next()) {
                int capacity = rs.getInt("capacity"), occ = rs.getInt("occupants");
                writer.write(rs.getString("room_number") + "," +
                        rs.getString("type") + "," +
                        rs.getString("block_name") + "," +
                        capacity + "," + occ + "," + (capacity - occ) + "\n");
            }
        }
    }

    private void exportStudentListCSV(String fileName) throws Exception {
        String sql = "SELECT student_id, name, roll_number, year, course, gender, mobile_number, parent_details, address FROM students";
        try (Connection conn = DatabaseHelper.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery();
             FileWriter writer = new FileWriter(fileName)) {
            writer.write("Student ID,Name,Roll Number,Year,Course,Gender,Mobile,Parent,Address\n");
            while (rs.next()) {
                writer.write(rs.getInt("student_id") + "," +
                        rs.getString("name") + "," +
                        rs.getString("roll_number") + "," +
                        rs.getInt("year") + "," +
                        rs.getString("course") + "," +
                        rs.getString("gender") + "," +
                        rs.getString("mobile_number") + "," +
                        rs.getString("parent_details") + "," +
                        rs.getString("address") + "\n");
            }
        }
    }
}
