import javax.swing.*;
import javax.swing.table.*;
import java.awt.*;
import java.awt.event.*;
import java.sql.*;

public class ViewReportsGUI extends JFrame {
    private JComboBox<String> reportTypeBox;
    private JTable reportTable;
    private DefaultTableModel tableModel;

    public ViewReportsGUI() {
        setTitle("📊 View Reports");
        setSize(800, 480);
        setLocationRelativeTo(null);
        setDefaultCloseOperation(DISPOSE_ON_CLOSE);

        // Gradient background panel
        JPanel mainPanel = new JPanel() {
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                Graphics2D g2 = (Graphics2D) g;
                GradientPaint gp = new GradientPaint(
                        0, 0, new Color(240,255,245),
                        0, getHeight(), new Color(232,245,253)
                );
                g2.setPaint(gp);
                g2.fillRect(0,0,getWidth(),getHeight());
            }
        };
        mainPanel.setLayout(new BorderLayout());

        JLabel heading = new JLabel("View Hostel Reports", JLabel.CENTER);
        heading.setFont(new Font("Segoe UI", Font.BOLD, 24));
        heading.setBorder(BorderFactory.createEmptyBorder(18, 0, 8, 0));
        heading.setForeground(new Color(44, 62, 80));
        mainPanel.add(heading, BorderLayout.NORTH);

        JPanel topPanel = new JPanel();
        topPanel.setOpaque(false);
        reportTypeBox = new JComboBox<>(new String[]{
                "Student Allocations",
                "Room Vacancies",
                "Student List"
        });
        reportTypeBox.setFont(new Font("Segoe UI", Font.PLAIN, 15));
        reportTypeBox.setBackground(new Color(255,255,255,230));
        reportTypeBox.setBorder(BorderFactory.createLineBorder(new Color(179,229,252), 1, true));
        topPanel.add(reportTypeBox);

        JButton loadBtn = new JButton("Load Report");
        loadBtn.setFont(new Font("Segoe UI", Font.BOLD, 15));
        loadBtn.setForeground(Color.WHITE);
        loadBtn.setBackground(new Color(33,150,243));
        loadBtn.setFocusPainted(false);
        loadBtn.setCursor(new Cursor(Cursor.HAND_CURSOR));
        loadBtn.setBorder(BorderFactory.createEmptyBorder(6,15,6,15));
        loadBtn.addMouseListener(new MouseAdapter() {
            public void mouseEntered(MouseEvent e) { loadBtn.setBackground(new Color(25,105,190)); }
            public void mouseExited(MouseEvent e) { loadBtn.setBackground(new Color(33,150,243)); }
        });
        topPanel.add(loadBtn);

        tableModel = new DefaultTableModel();
        reportTable = new JTable(tableModel) {
            // Alternating row color
            public Component prepareRenderer(TableCellRenderer renderer, int row, int column) {
                Component c = super.prepareRenderer(renderer, row, column);
                if (!isRowSelected(row)) {
                    c.setBackground(row % 2 == 0 ? new Color(255,255,255) : new Color(232,245,253));
                } else {
                    c.setBackground(new Color(79,195,247,80));
                }
                return c;
            }
        };
        reportTable.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        reportTable.setRowHeight(24);
        reportTable.getTableHeader().setFont(new Font("Segoe UI", Font.BOLD, 16));
        reportTable.getTableHeader().setBackground(new Color(178,235,242));
        JScrollPane scrollPane = new JScrollPane(reportTable);

        loadBtn.addActionListener(e -> loadReport());

        mainPanel.add(topPanel, BorderLayout.SOUTH);
        mainPanel.add(scrollPane, BorderLayout.CENTER);

        add(mainPanel);
        loadReport(); // load default report
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
