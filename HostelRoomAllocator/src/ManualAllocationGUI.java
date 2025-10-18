import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.sql.*;
import java.util.*;

public class ManualAllocationGUI extends JFrame {
    private JComboBox<String> studentBox, roomBox;
    private Map<String, Integer> studentMap = new HashMap<>();
    private Map<String, Integer> roomMap = new HashMap<>();

    public ManualAllocationGUI() {
        setTitle("Manual Room Allocation");
        setSize(400, 220);
        setLocationRelativeTo(null);
        setDefaultCloseOperation(DISPOSE_ON_CLOSE);

        JPanel panel = new JPanel(new GridLayout(3,2,14,20));
        panel.setBorder(BorderFactory.createEmptyBorder(18, 30, 18, 30));

        panel.add(new JLabel("Select Student:")); studentBox = new JComboBox<>(); panel.add(studentBox);
        panel.add(new JLabel("Select Room:"));   roomBox = new JComboBox<>(); panel.add(roomBox);

        JButton allocBtn = new JButton("Allocate Room"); panel.add(allocBtn); panel.add(new JLabel());

        // Load students and rooms from DB
        loadStudentsFromDB();
        loadRoomsFromDB();

        allocBtn.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                int studentId = getSelectedStudentId();
                int roomId = getSelectedRoomId();
                if (studentId < 0 || roomId < 0) {
                    JOptionPane.showMessageDialog(ManualAllocationGUI.this, "Select valid student and room.", "Error", JOptionPane.ERROR_MESSAGE);
                    return;
                }
                if (isRoomFull(roomId)) {
                    JOptionPane.showMessageDialog(ManualAllocationGUI.this, "Room is already full.", "Error", JOptionPane.ERROR_MESSAGE);
                    return;
                }
                allocateRoom(studentId, roomId);
            }
        });

        add(panel);
        setVisible(true);
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
