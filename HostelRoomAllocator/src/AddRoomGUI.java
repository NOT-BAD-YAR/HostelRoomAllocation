import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.sql.*;
import java.util.*;

public class AddRoomGUI extends JFrame {
    private JTextField roomNumField, capacityField;
    private JComboBox<String> blockBox, typeBox;
    private JButton addBtn;
    private Map<String, Integer> blockMap = new HashMap<>();

    public AddRoomGUI() {
        setTitle("🛏️ Add New Room");
        setSize(470, 400);
        setLocationRelativeTo(null);
        setDefaultCloseOperation(DISPOSE_ON_CLOSE);

        // Gradient background panel
        JPanel content = new JPanel() {
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                Graphics2D g2 = (Graphics2D) g;
                GradientPaint gp = new GradientPaint(
                        0, 0, new Color(243, 249, 252),
                        0, getHeight(), new Color(211, 235, 246)
                );
                g2.setPaint(gp);
                g2.fillRect(0, 0, getWidth(), getHeight());
            }
        };
        content.setLayout(new BorderLayout());
        add(content);

        JLabel title = new JLabel("Add New Room", JLabel.CENTER);
        title.setFont(new Font("Segoe UI", Font.BOLD, 22));
        title.setBorder(BorderFactory.createEmptyBorder(14, 0, 18, 0));
        content.add(title, BorderLayout.NORTH);

        JPanel form = new JPanel(new GridLayout(6, 2, 12, 12));
        form.setOpaque(false);
        form.setBorder(BorderFactory.createEmptyBorder(10, 40, 10, 40));

        form.add(new JLabel("Room Number:"));
        roomNumField = new JTextField(); styleField(roomNumField); form.add(roomNumField);

        form.add(new JLabel("Block:"));
        blockBox = new JComboBox<>(); styleCombo(blockBox); form.add(blockBox);

        form.add(new JLabel("Type:"));
        typeBox = new JComboBox<>(new String[]{"single", "twin", "triple", "four-sharing"}); styleCombo(typeBox); form.add(typeBox);

        form.add(new JLabel("Capacity:"));
        capacityField = new JTextField(); styleField(capacityField); form.add(capacityField);

        addBtn = new JButton("Add Room");
        addBtn.setFont(new Font("Segoe UI", Font.BOLD, 15));
        addBtn.setForeground(Color.WHITE);
        addBtn.setBackground(new Color(33, 150, 243));
        addBtn.setFocusPainted(false);
        addBtn.setCursor(new Cursor(Cursor.HAND_CURSOR));
        addBtn.setBorder(BorderFactory.createEmptyBorder(6, 15, 6, 15));
        addBtn.addMouseListener(new MouseAdapter() {
            public void mouseEntered(MouseEvent e) { addBtn.setBackground(new Color(25,105,190)); }
            public void mouseExited(MouseEvent e) { addBtn.setBackground(new Color(33,150,243)); }
        });

        form.add(addBtn);
        form.add(new JLabel());

        content.add(form, BorderLayout.CENTER);

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

        setVisible(true);
    }

    private void styleField(JTextField field) {
        field.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        field.setBackground(new Color(255, 255, 255, 240));
        field.setBorder(BorderFactory.createLineBorder(new Color(188, 222, 255), 1, true));
    }
    private void styleCombo(JComboBox<?> combo) {
        combo.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        combo.setBackground(new Color(255,255,255,240));
        combo.setBorder(BorderFactory.createLineBorder(new Color(188, 222, 255), 1, true));
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
            if (stmt.executeUpdate() > 0) {
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
