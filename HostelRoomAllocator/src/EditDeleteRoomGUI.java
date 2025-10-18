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
        setTitle("🚪 Edit/Delete Room");
        setSize(470, 340);
        setLocationRelativeTo(null);
        setDefaultCloseOperation(DISPOSE_ON_CLOSE);

        // Gradient background panel
        JPanel content = new JPanel() {
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                Graphics2D g2 = (Graphics2D) g;
                GradientPaint gp = new GradientPaint(
                        0, 0, new Color(233,242,255),
                        0, getHeight(), new Color(249,253,255)
                );
                g2.setPaint(gp);
                g2.fillRect(0, 0, getWidth(), getHeight());
            }
        };
        content.setLayout(new BorderLayout());
        add(content);

        JLabel title = new JLabel("Edit or Delete Room", JLabel.CENTER);
        title.setFont(new Font("Segoe UI", Font.BOLD, 22));
        title.setBorder(BorderFactory.createEmptyBorder(14, 0, 18, 0));
        content.add(title, BorderLayout.NORTH);

        JPanel form = new JPanel(new GridLayout(6,2,12,12));
        form.setOpaque(false);
        form.setBorder(BorderFactory.createEmptyBorder(10, 44, 10, 44));

        form.add(new JLabel("Select Room:")); roomBox = new JComboBox<>(); styleCombo(roomBox); form.add(roomBox);
        form.add(new JLabel("Room Number:")); roomNumField = new JTextField(); styleField(roomNumField); form.add(roomNumField);
        form.add(new JLabel("Block:")); blockCombo = new JComboBox<>(); styleCombo(blockCombo); form.add(blockCombo);
        form.add(new JLabel("Type:"));
        typeBox = new JComboBox<>(new String[]{"single", "twin", "triple", "four-sharing"}); styleCombo(typeBox); form.add(typeBox);
        form.add(new JLabel("Capacity:")); capacityField = new JTextField(); styleField(capacityField); form.add(capacityField);

        JButton editBtn = new JButton("Edit Room");
        styleActionBtn(editBtn, new Color(30,136,229), new Color(25,118,210));
        JButton delBtn = new JButton("Delete Room");
        styleActionBtn(delBtn, new Color(229,57,53), new Color(211,47,47));

        form.add(editBtn); form.add(delBtn);

        content.add(form, BorderLayout.CENTER);

        loadBlocksFromDB();
        loadRoomsFromDB();

        roomBox.addActionListener(e -> loadRoomDetails());
        editBtn.addActionListener(e -> updateRoomInDB());
        delBtn.addActionListener(e -> deleteRoomFromDB());

        setVisible(true);
    }

    private void styleCombo(JComboBox<?> combo) {
        combo.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        combo.setBackground(new Color(255,255,255,235));
        combo.setBorder(BorderFactory.createLineBorder(new Color(100,181,246), 1, true));
    }
    private void styleField(JTextField field) {
        field.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        field.setBackground(new Color(255,255,255,240));
        field.setBorder(BorderFactory.createLineBorder(new Color(100,181,246), 1, true));
    }

    private void styleActionBtn(JButton btn, Color c1, Color c2) {
        btn.setFont(new Font("Segoe UI", Font.BOLD, 15));
        btn.setForeground(Color.WHITE);
        btn.setBackground(c1);
        btn.setFocusPainted(false);
        btn.setBorder(BorderFactory.createEmptyBorder(7, 12, 7, 12));
        btn.setCursor(new Cursor(Cursor.HAND_CURSOR));
        btn.addMouseListener(new MouseAdapter() {
            public void mouseEntered(MouseEvent e) { btn.setBackground(c2); }
            public void mouseExited(MouseEvent e) { btn.setBackground(c1); }
        });
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
        } catch (SQLException e) { }
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
