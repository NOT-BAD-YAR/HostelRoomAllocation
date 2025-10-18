import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.sql.*;
import java.util.*;

public class AddRoomGUI extends JFrame {
    private JTextField roomNumField, capacityField;
    private JComboBox<String> blockBox, typeBox;
    private Map<String, Integer> blockMap = new HashMap<>();

    public AddRoomGUI() {
        setTitle("Add New Room");
        setSize(400, 340);
        setLocationRelativeTo(null);
        setDefaultCloseOperation(DISPOSE_ON_CLOSE);

        JPanel panel = new JPanel();
        panel.setLayout(new GridLayout(6, 2, 10, 10));
        panel.setBorder(BorderFactory.createEmptyBorder(20, 30, 20, 30));

        panel.add(new JLabel("Room Number:"));  roomNumField = new JTextField();     panel.add(roomNumField);

        panel.add(new JLabel("Block:"));        blockBox = new JComboBox<>();        panel.add(blockBox);

        panel.add(new JLabel("Type:"));
        typeBox = new JComboBox<>(new String[]{"single", "twin", "triple", "four-sharing"});
        panel.add(typeBox);

        panel.add(new JLabel("Capacity:"));     capacityField = new JTextField();    panel.add(capacityField);

        JButton addBtn = new JButton("Add Room"); panel.add(addBtn);  panel.add(new JLabel());

        // Load blocks from DB
        loadBlocksFromDB();

        addBtn.addActionListener(e -> {
            if (validateFields()) {
                if (isDuplicateRoom(roomNumField.getText().trim(), getSelectedBlockId())) {
                    JOptionPane.showMessageDialog(this, "Room number already exists in this block.", "Error", JOptionPane.ERROR_MESSAGE);
                    return;
                }
                addRoomToDB();
            }
        });

        add(panel);
        setVisible(true);
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
                blockBox.addItem(name);
            }
        } catch (SQLException e) {
            blockBox.addItem("No Blocks Found");
        }
    }

    private int getSelectedBlockId() {
        String name = (String) blockBox.getSelectedItem();
        return blockMap.getOrDefault(name, -1);
    }

    private boolean validateFields() {
        if (roomNumField.getText().trim().isEmpty() ||
                capacityField.getText().trim().isEmpty() ||
                blockBox.getSelectedIndex() == -1) {
            JOptionPane.showMessageDialog(this, "All fields required!", "Validation", JOptionPane.WARNING_MESSAGE);
            return false;
        }
        try {
            if (Integer.parseInt(capacityField.getText().trim()) <= 0) throw new Exception();
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, "Capacity must be a positive number.", "Validation", JOptionPane.WARNING_MESSAGE);
            return false;
        }
        return true;
    }

    private boolean isDuplicateRoom(String roomNumber, int blockId) {
        String sql = "SELECT room_id FROM rooms WHERE room_number = ? AND block_id = ?";
        try (Connection conn = DatabaseHelper.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, roomNumber);
            stmt.setInt(2, blockId);
            ResultSet rs = stmt.executeQuery();
            return rs.next();
        } catch (SQLException e) {
            return false;
        }
    }

    private void addRoomToDB() {
        String sql = "INSERT INTO rooms (room_number, block_id, type, capacity, occupants) VALUES (?, ?, ?, ?, 0)";
        try (Connection conn = DatabaseHelper.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, roomNumField.getText().trim());
            stmt.setInt(2, getSelectedBlockId());
            stmt.setString(3, (String) typeBox.getSelectedItem());
            stmt.setInt(4, Integer.parseInt(capacityField.getText().trim()));
            int rowsInserted = stmt.executeUpdate();
            if (rowsInserted > 0) {
                JOptionPane.showMessageDialog(this, "Room added successfully!", "Success", JOptionPane.INFORMATION_MESSAGE);
                dispose();
            } else {
                JOptionPane.showMessageDialog(this, "Failed to add room.", "Error", JOptionPane.ERROR_MESSAGE);
            }
        } catch (SQLException e) {
            JOptionPane.showMessageDialog(this, "DB error: " + e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        }
    }
}
