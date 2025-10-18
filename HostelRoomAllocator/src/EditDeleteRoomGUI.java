import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.sql.*;
import java.util.*;

public class EditDeleteRoomGUI extends JFrame {
    private JComboBox<String> roomBox, typeBox, blockCombo;
    private JTextField roomNumField, capacityField;
    private Map<String, Integer> roomMap = new HashMap<>();
    private Map<String, Integer> blockMap = new HashMap<>();
    private int selectedRoomId = -1;

    public EditDeleteRoomGUI() {
        setTitle("Edit/Delete Room");
        setSize(420, 340);
        setLocationRelativeTo(null);
        setDefaultCloseOperation(DISPOSE_ON_CLOSE);

        JPanel panel = new JPanel(new GridLayout(6,2,8,8));
        panel.setBorder(BorderFactory.createEmptyBorder(20, 30, 20, 30));

        panel.add(new JLabel("Select Room:")); roomBox = new JComboBox<>();    panel.add(roomBox);
        panel.add(new JLabel("Room Number:")); roomNumField = new JTextField();panel.add(roomNumField);
        panel.add(new JLabel("Block:"));       blockCombo = new JComboBox<>(); panel.add(blockCombo);
        panel.add(new JLabel("Type:"));
        typeBox = new JComboBox<>(new String[]{"single", "twin", "triple", "four-sharing"});
        panel.add(typeBox);
        panel.add(new JLabel("Capacity:"));    capacityField = new JTextField();panel.add(capacityField);

        JButton editBtn = new JButton("Edit Room");    JButton delBtn = new JButton("Delete Room");
        panel.add(editBtn); panel.add(delBtn);

        loadBlocksFromDB();
        loadRoomsFromDB();

        roomBox.addActionListener(e -> loadRoomDetails());

        editBtn.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) { updateRoomInDB(); }
        });

        delBtn.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) { deleteRoomFromDB(); }
        });

        add(panel);
        setVisible(true);
    }

    private void loadRoomsFromDB() {
        String sql = "SELECT r.room_id, r.room_number, b.block_name FROM rooms r JOIN blocks b ON r.block_id = b.block_id";
        try (Connection conn = DatabaseHelper.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {
            while (rs.next()) {
                String label = rs.getString("room_number") + " (" + rs.getString("block_name") + ")";
                int id = rs.getInt("room_id");
                roomMap.put(label, id);
                roomBox.addItem(label);
            }
            if (roomBox.getItemCount() > 0) loadRoomDetails();
        } catch (SQLException e) {
            roomBox.addItem("No Rooms Found");
        }
    }

    private void loadBlocksFromDB() {
        String sql = "SELECT block_id, block_name FROM blocks";
        try (Connection conn = DatabaseHelper.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {
            while (rs.next()) {
                String name = rs.getString("block_name");
                int id = rs.getInt("block_id");
                blockMap.put(name, id);
                blockCombo.addItem(name);
            }
        } catch (SQLException e) {
            blockCombo.addItem("No Blocks Found");
        }
    }

    private void loadRoomDetails() {
        String name = (String) roomBox.getSelectedItem();
        selectedRoomId = roomMap.getOrDefault(name, -1);
        if (selectedRoomId == -1) return;
        String sql = "SELECT * FROM rooms WHERE room_id = ?";
        try (Connection conn = DatabaseHelper.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, selectedRoomId);
            ResultSet rs = stmt.executeQuery();
            if (rs.next()) {
                roomNumField.setText(rs.getString("room_number"));
                blockCombo.setSelectedItem(getBlockNameById(rs.getInt("block_id")));
                typeBox.setSelectedItem(rs.getString("type"));
                capacityField.setText(String.valueOf(rs.getInt("capacity")));
            }
        } catch (SQLException e) { /* ignore */ }
    }

    private String getBlockNameById(int blockId) {
        for (String name : blockMap.keySet()) {
            if (blockMap.get(name) == blockId) return name;
        }
        return null;
    }

    private void updateRoomInDB() {
        if (selectedRoomId == -1) return;
        String sql = "UPDATE rooms SET room_number=?, block_id=?, type=?, capacity=? WHERE room_id=?";
        try (Connection conn = DatabaseHelper.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, roomNumField.getText().trim());
            stmt.setInt(2, blockMap.get((String) blockCombo.getSelectedItem()));
            stmt.setString(3, (String) typeBox.getSelectedItem());
            stmt.setInt(4, Integer.parseInt(capacityField.getText().trim()));
            stmt.setInt(5, selectedRoomId);
            int rows = stmt.executeUpdate();
            if (rows > 0) {
                JOptionPane.showMessageDialog(this, "Room updated!", "Success", JOptionPane.INFORMATION_MESSAGE);
            }
        } catch (SQLException e) {
            JOptionPane.showMessageDialog(this, "Error updating room.", "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void deleteRoomFromDB() {
        if (selectedRoomId == -1) return;
        int confirm = JOptionPane.showConfirmDialog(this, "Delete this room? All student allocations will be removed.", "Confirm Delete", JOptionPane.YES_NO_OPTION);
        if (confirm != JOptionPane.YES_OPTION) return;
        String deleteAlloc = "DELETE FROM allocations WHERE room_id = ?";
        String deleteRoom = "DELETE FROM rooms WHERE room_id = ?";
        try (Connection conn = DatabaseHelper.getConnection();
             PreparedStatement allocStmt = conn.prepareStatement(deleteAlloc);
             PreparedStatement roomStmt = conn.prepareStatement(deleteRoom)) {
            allocStmt.setInt(1, selectedRoomId);
            allocStmt.executeUpdate();
            roomStmt.setInt(1, selectedRoomId);
            int rows = roomStmt.executeUpdate();
            if (rows > 0) {
                JOptionPane.showMessageDialog(this, "Room deleted!", "Success", JOptionPane.INFORMATION_MESSAGE);
                dispose();
            }
        } catch (SQLException e) {
            JOptionPane.showMessageDialog(this, "Error deleting room (check foreign key).", "Error", JOptionPane.ERROR_MESSAGE);
        }
    }
}
