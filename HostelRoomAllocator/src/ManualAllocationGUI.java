import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.sql.*;
import java.util.*;

public class ManualAllocationGUI extends JFrame {
    private JComboBox<String> studentBox, roomBox;
    private Map<String, Integer> studentMap = new HashMap<>();
    private Map<String, Integer> roomMap = new HashMap<>();
    private JButton allocBtn;

    public ManualAllocationGUI() {
        setTitle("🔗 Manual Room Allocation");
        setSize(420, 260);
        setLocationRelativeTo(null);
        setDefaultCloseOperation(DISPOSE_ON_CLOSE);

        // Gradient background
        JPanel content = new JPanel() {
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                Graphics2D g2 = (Graphics2D) g;
                GradientPaint gp = new GradientPaint(
                        0, 0, new Color(245,255,250),
                        0, getHeight(), new Color(198,232,222)
                );
                g2.setPaint(gp);
                g2.fillRect(0, 0, getWidth(), getHeight());
            }
        };
        content.setLayout(new BorderLayout());
        add(content);

        JLabel title = new JLabel("Manual Room Allocation", JLabel.CENTER);
        title.setFont(new Font("Segoe UI", Font.BOLD, 20));
        title.setBorder(BorderFactory.createEmptyBorder(12,0,14,0));
        content.add(title, BorderLayout.NORTH);

        JPanel form = new JPanel(new GridLayout(3,2,14,16));
        form.setOpaque(false);
        form.setBorder(BorderFactory.createEmptyBorder(14,32,8,32));

        form.add(new JLabel("Select Student:")); studentBox = new JComboBox<>(); styleCombo(studentBox); form.add(studentBox);
        form.add(new JLabel("Select Room:")); roomBox = new JComboBox<>(); styleCombo(roomBox); form.add(roomBox);

        allocBtn = new JButton("Allocate Room");
        allocBtn.setFont(new Font("Segoe UI", Font.BOLD, 15));
        allocBtn.setForeground(Color.WHITE);
        allocBtn.setBackground(new Color(102, 181, 210));
        allocBtn.setFocusPainted(false);
        allocBtn.setCursor(new Cursor(Cursor.HAND_CURSOR));
        allocBtn.setBorder(BorderFactory.createEmptyBorder(7, 18, 7, 18));
        allocBtn.addMouseListener(new MouseAdapter() {
            public void mouseEntered(MouseEvent e) { allocBtn.setBackground(new Color(46,128,195)); }
            public void mouseExited(MouseEvent e) { allocBtn.setBackground(new Color(102,181,210)); }
        });

        form.add(allocBtn);
        form.add(new JLabel());
        content.add(form, BorderLayout.CENTER);

        loadStudentsFromDB();
        loadRoomsFromDB();

        allocBtn.addActionListener(e -> {
            int studentId = getSelectedStudentId();
            int roomId = getSelectedRoomId();
            if (studentId < 0 || roomId < 0) {
                JOptionPane.showMessageDialog(this, "Select valid student and room.", "Error", JOptionPane.ERROR_MESSAGE);
                return;
            }
            if (isRoomFull(roomId)) {
                JOptionPane.showMessageDialog(this, "Room is already full.", "Error", JOptionPane.ERROR_MESSAGE);
                return;
            }
            allocateRoom(studentId, roomId);
        });

        setVisible(true);
    }

    private void styleCombo(JComboBox<?> combo) {
        combo.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        combo.setBackground(new Color(250,250,250,240));
        combo.setBorder(BorderFactory.createLineBorder(new Color(140, 200, 180), 1, true));
    }

    private void loadStudentsFromDB() {
        String sql = "SELECT student_id, name, roll_number FROM students WHERE room_id IS NULL";
        try (Connection conn = DatabaseHelper.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {
            while (rs.next()) {
                String label = rs.getString("name") + " (" + rs.getString("roll_number") + ")";
                int id = rs.getInt("student_id");
                studentMap.put(label, id);
                studentBox.addItem(label);
            }
        } catch (SQLException e) {
            studentBox.addItem("No Unassigned Students");
        }
    }

    private void loadRoomsFromDB() {
        String sql = "SELECT r.room_id, r.room_number, r.capacity, r.occupants, b.block_name " +
                "FROM rooms r JOIN blocks b ON r.block_id = b.block_id";
        try (Connection conn = DatabaseHelper.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {
            while (rs.next()) {
                int id = rs.getInt("room_id");
                int capacity = rs.getInt("capacity");
                int occupants = rs.getInt("occupants");
                String label = rs.getString("room_number") + " (" + rs.getString("block_name") + ") " +
                        "[Vacancy: " + (capacity - occupants) + "]";
                roomMap.put(label, id);
                roomBox.addItem(label);
            }
        } catch (SQLException e) {
            roomBox.addItem("No Rooms Found");
        }
    }

    private int getSelectedStudentId() {
        String name = (String) studentBox.getSelectedItem();
        return studentMap.getOrDefault(name, -1);
    }

    private int getSelectedRoomId() {
        String name = (String) roomBox.getSelectedItem();
        return roomMap.getOrDefault(name, -1);
    }

    private boolean isRoomFull(int roomId) {
        String sql = "SELECT capacity, occupants FROM rooms WHERE room_id = ?";
        try (Connection conn = DatabaseHelper.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, roomId);
            ResultSet rs = stmt.executeQuery();
            if (rs.next()) {
                return rs.getInt("occupants") >= rs.getInt("capacity");
            }
        } catch (SQLException e) {
            return true;
        }
        return true;
    }

    private void allocateRoom(int studentId, int roomId) {
        String updateStu = "UPDATE students SET room_id = ? WHERE student_id = ?";
        String updateRoom = "UPDATE rooms SET occupants = occupants + 1 WHERE room_id = ?";
        String addAlloc = "INSERT INTO allocations (student_id, room_id, alloc_time) VALUES (?, ?, NOW())";
        try (Connection conn = DatabaseHelper.getConnection();
             PreparedStatement stuStmt = conn.prepareStatement(updateStu);
             PreparedStatement roomStmt = conn.prepareStatement(updateRoom);
             PreparedStatement allocStmt = conn.prepareStatement(addAlloc)) {
            stuStmt.setInt(1, roomId);
            stuStmt.setInt(2, studentId);
            stuStmt.executeUpdate();

            roomStmt.setInt(1, roomId);
            roomStmt.executeUpdate();

            allocStmt.setInt(1, studentId);
            allocStmt.setInt(2, roomId);
            allocStmt.executeUpdate();

            JOptionPane.showMessageDialog(this, "Student allocated to room!", "Success", JOptionPane.INFORMATION_MESSAGE);
            dispose();
        } catch (SQLException e) {
            JOptionPane.showMessageDialog(this, "DB error: " + e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        }
    }
}
