import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.sql.*;

public class AutoAllocationGUI extends JFrame {
    public AutoAllocationGUI() {
        setTitle("⚡ Auto Allocation");
        setSize(420, 200);
        setLocationRelativeTo(null);
        setDefaultCloseOperation(DISPOSE_ON_CLOSE);

        // Gradient background panel
        JPanel content = new JPanel() {
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                Graphics2D g2 = (Graphics2D) g;
                GradientPaint gp = new GradientPaint(
                        0, 0, new Color(255,255,240),
                        0, getHeight(), new Color(232,245,233)
                );
                g2.setPaint(gp);
                g2.fillRect(0, 0, getWidth(), getHeight());
            }
        };
        content.setLayout(new BorderLayout());
        add(content);

        JLabel title = new JLabel("Auto Allocation", JLabel.CENTER);
        title.setFont(new Font("Segoe UI", Font.BOLD, 20));
        title.setBorder(BorderFactory.createEmptyBorder(20,0,8,0));
        content.add(title, BorderLayout.NORTH);

        JLabel infoLbl = new JLabel("<html>Allocate all <b>unassigned students</b> automatically<br>to available rooms in one click.</html>", JLabel.CENTER);
        infoLbl.setFont(new Font("Segoe UI", Font.PLAIN, 15));
        infoLbl.setBorder(BorderFactory.createEmptyBorder(8,0,8,0));
        content.add(infoLbl, BorderLayout.CENTER);

        JButton allocBtn = new JButton("Run Auto Allocation");
        allocBtn.setFont(new Font("Segoe UI", Font.BOLD, 15));
        allocBtn.setForeground(Color.WHITE);
        allocBtn.setBackground(new Color(255,152,0));
        allocBtn.setFocusPainted(false);
        allocBtn.setCursor(new Cursor(Cursor.HAND_CURSOR));
        allocBtn.setBorder(BorderFactory.createEmptyBorder(9,25,9,25));
        allocBtn.addMouseListener(new MouseAdapter() {
            public void mouseEntered(MouseEvent e) { allocBtn.setBackground(new Color(251,140,0)); }
            public void mouseExited(MouseEvent e) { allocBtn.setBackground(new Color(255,152,0)); }
        });
        content.add(allocBtn, BorderLayout.SOUTH);

        allocBtn.addActionListener(e -> {
            int count = performAutoAllocation();
            JOptionPane.showMessageDialog(this,
                    count + " student(s) auto-allocated!", "Auto Allocation", JOptionPane.INFORMATION_MESSAGE);
            dispose();
        });

        setVisible(true);
    }

    private int performAutoAllocation() {
        String fetchStudents = "SELECT student_id FROM students WHERE room_id IS NULL";
        int allocatedCount = 0;
        try (Connection conn = DatabaseHelper.getConnection();
             PreparedStatement psStudents = conn.prepareStatement(fetchStudents);
             ResultSet rsStudents = psStudents.executeQuery()) {

            while (rsStudents.next()) {
                int studentId = rsStudents.getInt("student_id");
                int roomId = findVacantRoom(conn);
                if (roomId > 0) {
                    allocateStudentToRoom(conn, studentId, roomId);
                    allocatedCount++;
                }
            }
        } catch (SQLException e) { /* Handle error if desired */ }
        return allocatedCount;
    }

    private int findVacantRoom(Connection conn) throws SQLException {
        String sql = "SELECT r.room_id FROM rooms r WHERE r.occupants < r.capacity ORDER BY r.room_id ASC LIMIT 1";
        try (PreparedStatement stmt = conn.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {
            if (rs.next()) return rs.getInt("room_id");
        }
        return -1;
    }

    private void allocateStudentToRoom(Connection conn, int studentId, int roomId) throws SQLException {
        String updateStudent = "UPDATE students SET room_id = ? WHERE student_id = ?";
        String addAllocation = "INSERT INTO allocations (student_id, room_id, alloc_time) VALUES (?, ?, NOW())";
        String updateRoom = "UPDATE rooms SET occupants = occupants + 1 WHERE room_id = ?";
        try (PreparedStatement ps1 = conn.prepareStatement(updateStudent);
             PreparedStatement ps2 = conn.prepareStatement(addAllocation);
             PreparedStatement ps3 = conn.prepareStatement(updateRoom)) {

            ps1.setInt(1, roomId);
            ps1.setInt(2, studentId);
            ps1.executeUpdate();

            ps2.setInt(1, studentId);
            ps2.setInt(2, roomId);
            ps2.executeUpdate();

            ps3.setInt(1, roomId);
            ps3.executeUpdate();
        }
    }
}
