import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.io.FileWriter;
import java.io.FileOutputStream;
import java.io.File;
import java.sql.*;
import com.itextpdf.text.BaseColor;
import com.itextpdf.text.Document;
import com.itextpdf.text.Paragraph;
import com.itextpdf.text.Phrase;
import com.itextpdf.text.pdf.*;

public class ExportGUI extends JFrame {
    private JComboBox<String> reportTypeBox, formatBox;

    // Watermark event for all PDF pages
    static class WatermarkPageEvent extends PdfPageEventHelper {
        private final Phrase watermark;
        public WatermarkPageEvent(String text) {
            // Use fully qualified com.itextpdf.text.Font
            com.itextpdf.text.Font wmFont =
                    new com.itextpdf.text.Font(com.itextpdf.text.Font.FontFamily.HELVETICA, 60, com.itextpdf.text.Font.BOLD, new BaseColor(200, 200, 255, 80));
            this.watermark = new Phrase(text, wmFont);
        }
        @Override
        public void onEndPage(PdfWriter writer, Document doc) {
            PdfContentByte cb = writer.getDirectContentUnder();
            ColumnText.showTextAligned(cb, com.itextpdf.text.Element.ALIGN_CENTER,
                    watermark, (doc.right() + doc.left()) / 2, (doc.top() + doc.bottom()) / 2, 45);
        }
    }

    public ExportGUI() {
        setTitle("🗂️ Export Reports");
        setSize(440, 200);
        setLocationRelativeTo(null);
        setDefaultCloseOperation(DISPOSE_ON_CLOSE);

        // Gradient background
        JPanel content = new JPanel() {
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                Graphics2D g2 = (Graphics2D) g;
                GradientPaint gp = new GradientPaint(0,0,new Color(255,251,233), 0,getHeight(),new Color(232,245,253));
                g2.setPaint(gp);
                g2.fillRect(0,0,getWidth(),getHeight());
            }
        };
        content.setLayout(new BorderLayout());

        JLabel title = new JLabel("Export Reports", JLabel.CENTER);
        // Use fully qualified java.awt.Font
        title.setFont(new java.awt.Font("SansSerif", java.awt.Font.BOLD, 22));
        title.setBorder(BorderFactory.createEmptyBorder(22,0,10,0));
        content.add(title, BorderLayout.NORTH);

        JPanel form = new JPanel(new GridLayout(3,2,12,16));
        form.setOpaque(false);
        form.setBorder(BorderFactory.createEmptyBorder(10,55,18,55));

        reportTypeBox = new JComboBox<>(new String[]{
                "Student Allocations",
                "Room Vacancies",
                "Student List"
        });
        styleCombo(reportTypeBox);

        formatBox = new JComboBox<>(new String[]{"PDF", "CSV"});
        styleCombo(formatBox);

        form.add(new JLabel("Report Type:"));
        form.add(reportTypeBox);
        form.add(new JLabel("Export as:"));
        form.add(formatBox);

        JButton exportBtn = new JButton("Export");
        exportBtn.setFont(new java.awt.Font("SansSerif", java.awt.Font.BOLD, 15));
        exportBtn.setForeground(Color.WHITE);
        exportBtn.setBackground(new Color(255,152,0));
        exportBtn.setFocusPainted(false);
        exportBtn.setCursor(new Cursor(Cursor.HAND_CURSOR));
        exportBtn.setBorder(BorderFactory.createEmptyBorder(7, 22, 7, 22));
        exportBtn.addMouseListener(new MouseAdapter() {
            public void mouseEntered(MouseEvent e) { exportBtn.setBackground(new Color(251,140,0)); }
            public void mouseExited(MouseEvent e) { exportBtn.setBackground(new Color(255,152,0)); }
        });

        form.add(exportBtn);
        form.add(new JLabel());

        exportBtn.addActionListener(e -> exportReport());

        content.add(form, BorderLayout.CENTER);

        add(content);
        setVisible(true);
    }

    private void styleCombo(JComboBox<?> combo) {
        combo.setFont(new java.awt.Font("SansSerif", java.awt.Font.PLAIN, 14));
        combo.setBackground(new Color(255,255,255,235));
        combo.setBorder(BorderFactory.createLineBorder(new Color(251,192,45), 1, true));
    }

    // Report export logic
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

    // PDF exports: use com.itextpdf.text.Font
    private void exportStudentAllocationsPDF(String fileName) throws Exception {
        Document doc = new Document();
        PdfWriter writer = PdfWriter.getInstance(doc, new FileOutputStream(fileName));
        writer.setPageEvent(new WatermarkPageEvent("MARiO"));
        doc.open();
        com.itextpdf.text.Font titleFont =
                new com.itextpdf.text.Font(com.itextpdf.text.Font.FontFamily.HELVETICA, 18, com.itextpdf.text.Font.BOLD, new BaseColor(0, 70, 200));
        Paragraph title = new Paragraph("Student Allocations Report\n\n", titleFont);
        title.setAlignment(com.itextpdf.text.Element.ALIGN_CENTER);
        doc.add(title);
        PdfPTable table = new PdfPTable(5); table.setWidthPercentage(100);
        com.itextpdf.text.Font headerFont =
                new com.itextpdf.text.Font(com.itextpdf.text.Font.FontFamily.HELVETICA, 12, com.itextpdf.text.Font.BOLD, BaseColor.WHITE);
        BaseColor headerBg = new BaseColor(34, 90, 149);
        String[] headers = {"Student ID", "Name", "Room Number", "Room Type", "Block Name"};
        for (String h : headers) {
            PdfPCell cell = new PdfPCell(new Phrase(h, headerFont));
            cell.setBackgroundColor(headerBg);
            cell.setHorizontalAlignment(com.itextpdf.text.Element.ALIGN_CENTER);
            table.addCell(cell);
        }
        String sql = "SELECT s.student_id, s.name, r.room_number, r.type, b.block_name FROM students s LEFT JOIN rooms r ON s.room_id = r.room_id LEFT JOIN blocks b ON r.block_id = b.block_id";
        try (Connection conn = DatabaseHelper.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql); ResultSet rs = stmt.executeQuery()) {
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
        com.itextpdf.text.Font titleFont =
                new com.itextpdf.text.Font(com.itextpdf.text.Font.FontFamily.HELVETICA, 18, com.itextpdf.text.Font.BOLD, new BaseColor(200, 0, 0));
        Paragraph title = new Paragraph("Room Vacancy Report\n\n", titleFont);
        title.setAlignment(com.itextpdf.text.Element.ALIGN_CENTER);
        doc.add(title);
        PdfPTable table = new PdfPTable(6); table.setWidthPercentage(100);
        com.itextpdf.text.Font headerFont =
                new com.itextpdf.text.Font(com.itextpdf.text.Font.FontFamily.HELVETICA, 12, com.itextpdf.text.Font.BOLD, BaseColor.WHITE);
        BaseColor headerBg = new BaseColor(128, 0, 0);
        String[] headers = {"Room Number", "Room Type", "Block Name", "Capacity", "Occupants", "Vacancy"};
        for (String h : headers) {
            PdfPCell cell = new PdfPCell(new Phrase(h, headerFont));
            cell.setBackgroundColor(headerBg);
            cell.setHorizontalAlignment(com.itextpdf.text.Element.ALIGN_CENTER);
            table.addCell(cell);
        }
        String sql = "SELECT r.room_number, r.type, b.block_name, r.capacity, r.occupants FROM rooms r JOIN blocks b ON r.block_id = b.block_id";
        try (Connection conn = DatabaseHelper.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql); ResultSet rs = stmt.executeQuery()) {
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
        com.itextpdf.text.Font titleFont =
                new com.itextpdf.text.Font(com.itextpdf.text.Font.FontFamily.HELVETICA, 18, com.itextpdf.text.Font.BOLD, new BaseColor(0, 128, 0));
        Paragraph title = new Paragraph("Student List Report\n\n", titleFont);
        title.setAlignment(com.itextpdf.text.Element.ALIGN_CENTER);
        doc.add(title);
        PdfPTable table = new PdfPTable(9); table.setWidthPercentage(100);
        com.itextpdf.text.Font headerFont =
                new com.itextpdf.text.Font(com.itextpdf.text.Font.FontFamily.HELVETICA, 10, com.itextpdf.text.Font.BOLD, BaseColor.WHITE);
        BaseColor headerBg = new BaseColor(0, 128, 0);
        String[] headers = {"Student ID", "Name", "Roll Number", "Year", "Course", "Gender", "Mobile", "Parent", "Address"};
        for (String h : headers) {
            PdfPCell cell = new PdfPCell(new Phrase(h, headerFont));
            cell.setBackgroundColor(headerBg);
            cell.setHorizontalAlignment(com.itextpdf.text.Element.ALIGN_CENTER);
            table.addCell(cell);
        }
        String sql = "SELECT student_id, name, roll_number, year, course, gender, mobile_number, parent_details, address FROM students";
        try (Connection conn = DatabaseHelper.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql); ResultSet rs = stmt.executeQuery()) {
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

    // Only use String for value here - PDF cell helper
    private void addColoredCell(PdfPTable table, String value, BaseColor bg) {
        PdfPCell cell = new PdfPCell(new Phrase(value));
        cell.setBackgroundColor(bg);
        cell.setHorizontalAlignment(com.itextpdf.text.Element.ALIGN_CENTER);
        table.addCell(cell);
    }

    // CSV Exports
    private void exportStudentAllocationsCSV(String fileName) throws Exception {
        String sql = "SELECT s.student_id, s.name, r.room_number, r.type, b.block_name FROM students s LEFT JOIN rooms r ON s.room_id = r.room_id LEFT JOIN blocks b ON r.block_id = b.block_id";
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
        String sql = "SELECT r.room_number, r.type, b.block_name, r.capacity, r.occupants FROM rooms r JOIN blocks b ON r.block_id = b.block_id";
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
                        capacity + "," +
                        occ + "," +
                        (capacity - occ) + "\n");
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
