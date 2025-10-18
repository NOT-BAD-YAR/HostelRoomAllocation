import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.sql.*;

public class AutoAllocationGUI extends JFrame {
    public AutoAllocationGUI() {
        setTitle("Auto Allocation");
        setSize(400, 200);
        setLocationRelativeTo(null);
        setDefaultCloseOperation(DISPOSE_ON_CLOSE);

        JPanel panel = new JPanel(new BorderLayout());
        JLabel label = new JLabel("Allocate all unassigned students automatically to available rooms.", SwingConstants.CENTER);
        label.setFont(new Font("Arial", Font.PLAIN, 14));
        panel.add(label, BorderLayout.CENTER);

        JButton allocBtn = new JButton("Run Auto Allocation");
        panel.add(allocBtn, BorderLayout.SOUTH);

        allocBtn.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                int count = performAutoAllocation();
                JOptionPane.showMessageDialog(AutoAllocationGUI.this,
                        count + " student(s) auto-allocated!", "Auto Allocation", JOptionPane.INFORMATION_MESSAGE);
                dispose();
            }
        });

        add(panel);
        setVisible(true);
    }

    // Auto allocates all unassigned students to first available room with space
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

    // Get first vacant room (rooms with available slots)
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
