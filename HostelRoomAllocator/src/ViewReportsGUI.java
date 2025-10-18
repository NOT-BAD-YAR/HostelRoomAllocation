import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.awt.event.*;
import java.sql.*;

public class ViewReportsGUI extends JFrame {
    private JComboBox<String> reportTypeBox;
    private JTable reportTable;
    private DefaultTableModel tableModel;

    public ViewReportsGUI() {
        setTitle("View Reports");
        setSize(750, 480);
        setLocationRelativeTo(null);
        setDefaultCloseOperation(DISPOSE_ON_CLOSE);

        JPanel topPanel = new JPanel();
        reportTypeBox = new JComboBox<>(new String[]{
                "Student Allocations",
                "Room Vacancies",
                "Student List"
        });
        topPanel.add(new JLabel("Report Type: "));
        topPanel.add(reportTypeBox);

        JButton loadBtn = new JButton("Load Report");
        topPanel.add(loadBtn);

        tableModel = new DefaultTableModel();
        reportTable = new JTable(tableModel);
        JScrollPane scrollPane = new JScrollPane(reportTable);

        loadBtn.addActionListener(e -> loadReport());

        setLayout(new BorderLayout());
        add(topPanel, BorderLayout.NORTH);
        add(scrollPane, BorderLayout.CENTER);

        loadReport(); // Load default report

        setVisible(true);
    }

    private void loadReport() {
        int idx = reportTypeBox.getSelectedIndex();
        switch (idx) {
            case 0: loadStudentAllocations(); break;
            case 1: loadRoomVacancies(); break;
            case 2: loadStudentList(); break;
        }
    }

    private void loadStudentAllocations() {
        tableModel.setRowCount(0);
        tableModel.setColumnIdentifiers(new String[]{
                "Student ID", "Name", "Room Number", "Room Type", "Block Name"
        });
        String sql = "SELECT s.student_id, s.name, r.room_number, r.type, b.block_name " +
                "FROM students s " +
                "LEFT JOIN rooms r ON s.room_id = r.room_id " +
                "LEFT JOIN blocks b ON r.block_id = b.block_id";
        try (Connection conn = DatabaseHelper.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {
            while (rs.next()) {
                tableModel.addRow(new Object[]{
                        rs.getInt("student_id"),
                        rs.getString("name"),
                        rs.getString("room_number"),
                        rs.getString("type"),
                        rs.getString("block_name")
                });
            }
        } catch (SQLException e) { tableModel.addRow(new Object[]{"Error loading data"}); }
    }

    private void loadRoomVacancies() {
        tableModel.setRowCount(0);
        tableModel.setColumnIdentifiers(new String[]{
                "Room Number", "Room Type", "Block Name", "Capacity", "Occupants", "Vacancy"
        });
        String sql = "SELECT r.room_number, r.type, b.block_name, r.capacity, r.occupants " +
                "FROM rooms r JOIN blocks b ON r.block_id = b.block_id";
        try (Connection conn = DatabaseHelper.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {
            while (rs.next()) {
                int capacity = rs.getInt("capacity");
                int occupants = rs.getInt("occupants");
                tableModel.addRow(new Object[]{
                        rs.getString("room_number"),
                        rs.getString("type"),
                        rs.getString("block_name"),
                        capacity,
                        occupants,
                        capacity - occupants
                });
            }
        } catch (SQLException e) { tableModel.addRow(new Object[]{"Error loading data"}); }
    }

    private void loadStudentList() {
        tableModel.setRowCount(0);
        tableModel.setColumnIdentifiers(new String[]{
                "Student ID", "Name", "Roll Number", "Year", "Course", "Gender", "Mobile", "Parent", "Address"
        });
        String sql = "SELECT student_id, name, roll_number, year, course, gender, mobile_number, parent_details, address FROM students";
        try (Connection conn = DatabaseHelper.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {
            while (rs.next()) {
                tableModel.addRow(new Object[]{
                        rs.getInt("student_id"),
                        rs.getString("name"),
                        rs.getString("roll_number"),
                        rs.getInt("year"),
                        rs.getString("course"),
                        rs.getString("gender"),
                        rs.getString("mobile_number"),
                        rs.getString("parent_details"),
                        rs.getString("address")
                });
            }
        } catch (SQLException e) { tableModel.addRow(new Object[]{"Error loading data"}); }
    }
}
