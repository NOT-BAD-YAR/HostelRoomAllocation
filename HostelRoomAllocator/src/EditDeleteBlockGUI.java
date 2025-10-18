import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.sql.*;
import java.util.*;

public class EditDeleteBlockGUI extends JFrame {
    private JComboBox<String> blockBox;
    private JTextField blockNameField, descriptionField;
    private Map<String, Integer> blockMap = new HashMap<>();
    private int selectedBlockId = -1;

    public EditDeleteBlockGUI() {
        setTitle("🏢 Edit/Delete Block");
        setSize(430, 250);
        setLocationRelativeTo(null);
        setDefaultCloseOperation(DISPOSE_ON_CLOSE);

        // Gradient background
        JPanel content = new JPanel() {
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                Graphics2D g2 = (Graphics2D) g;
                GradientPaint gp = new GradientPaint(
                        0, 0, new Color(255,238,238),
                        0, getHeight(), new Color(232,245,253)
                );
                g2.setPaint(gp);
                g2.fillRect(0, 0, getWidth(), getHeight());
            }
        };
        content.setLayout(new BorderLayout());
        add(content);

        JLabel title = new JLabel("Edit or Delete Block", JLabel.CENTER);
        title.setFont(new Font("Segoe UI", Font.BOLD, 22));
        title.setBorder(BorderFactory.createEmptyBorder(12, 0, 12, 0));
        content.add(title, BorderLayout.NORTH);

        JPanel form = new JPanel(new GridLayout(3,2,14,12));
        form.setOpaque(false);
        form.setBorder(BorderFactory.createEmptyBorder(10,44,20,44));

        form.add(new JLabel("Select Block:")); blockBox = new JComboBox<>(); styleCombo(blockBox); form.add(blockBox);
        form.add(new JLabel("Block Name:")); blockNameField = new JTextField(); styleField(blockNameField); form.add(blockNameField);
        form.add(new JLabel("Description:")); descriptionField = new JTextField(); styleField(descriptionField); form.add(descriptionField);

        JButton editBtn = new JButton("Edit Block");
        styleActionBtn(editBtn, new Color(0,150,136), new Color(38,166,154));
        JButton delBtn = new JButton("Delete Block");
        styleActionBtn(delBtn, new Color(244,67,54), new Color(229,115,115));
        form.add(editBtn); form.add(delBtn);

        content.add(form, BorderLayout.CENTER);

        loadBlocksFromDB();
        blockBox.addActionListener(e -> loadBlockDetails());
        editBtn.addActionListener(e -> updateBlockInDB());
        delBtn.addActionListener(e -> deleteBlockFromDB());

        setVisible(true);
    }

    private void styleField(JTextField field) {
        field.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        field.setBackground(new Color(255,255,255,238));
        field.setBorder(BorderFactory.createLineBorder(new Color(178,223,219), 1, true));
    }
    private void styleCombo(JComboBox<?> combo) {
        combo.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        combo.setBackground(new Color(255,255,255,230));
        combo.setBorder(BorderFactory.createLineBorder(new Color(178,223,219), 1, true));
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

    private void loadBlocksFromDB() {
        String sql = "SELECT block_id, block_name FROM blocks";
        try (Connection conn = DatabaseHelper.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {
            while (rs.next()) {
                String label = rs.getString("block_name");
                int id = rs.getInt("block_id");
                blockMap.put(label, id);
                blockBox.addItem(label);
            }
            if (blockBox.getItemCount() > 0) loadBlockDetails();
        } catch (SQLException e) {
            blockBox.addItem("No Blocks Found");
        }
    }

    private void loadBlockDetails() {
        String name = (String) blockBox.getSelectedItem();
        selectedBlockId = blockMap.getOrDefault(name, -1);
        if (selectedBlockId == -1) return;
        String sql = "SELECT * FROM blocks WHERE block_id = ?";
        try (Connection conn = DatabaseHelper.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, selectedBlockId);
            ResultSet rs = stmt.executeQuery();
            if (rs.next()) {
                blockNameField.setText(rs.getString("block_name"));
                descriptionField.setText(rs.getString("description"));
            }
        } catch (SQLException e) { }
    }

    private void updateBlockInDB() {
        if (selectedBlockId == -1) return;
        String sql = "UPDATE blocks SET block_name=?, description=? WHERE block_id=?";
        try (Connection conn = DatabaseHelper.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, blockNameField.getText().trim());
            stmt.setString(2, descriptionField.getText().trim());
            stmt.setInt(3, selectedBlockId);
            int rows = stmt.executeUpdate();
            if (rows > 0) {
                JOptionPane.showMessageDialog(this, "Block updated!", "Success", JOptionPane.INFORMATION_MESSAGE);
            }
        } catch (SQLException e) {
            JOptionPane.showMessageDialog(this, "Error updating block.", "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void deleteBlockFromDB() {
        if (selectedBlockId == -1) return;
        if (hasRoomsInBlock(selectedBlockId)) {
            JOptionPane.showMessageDialog(this, "Cannot delete block with rooms assigned!\nDelete/assign rooms first.", "Error", JOptionPane.ERROR_MESSAGE);
            return;
        }
        int confirm = JOptionPane.showConfirmDialog(this, "Delete this block?", "Confirm Delete", JOptionPane.YES_NO_OPTION);
        if (confirm != JOptionPane.YES_OPTION) return;
        String deleteBlock = "DELETE FROM blocks WHERE block_id = ?";
        try (Connection conn = DatabaseHelper.getConnection();
             PreparedStatement stmt = conn.prepareStatement(deleteBlock)) {
            stmt.setInt(1, selectedBlockId);
            int rows = stmt.executeUpdate();
            if (rows > 0) {
                JOptionPane.showMessageDialog(this, "Block deleted!", "Success", JOptionPane.INFORMATION_MESSAGE);
                dispose();
            }
        } catch (SQLException e) {
            JOptionPane.showMessageDialog(this, "Error deleting block (check foreign key).", "Error", JOptionPane.ERROR_MESSAGE);
        }
    }
    private boolean hasRoomsInBlock(int blockId) {
        String sql = "SELECT room_id FROM rooms WHERE block_id = ?";
        try (Connection conn = DatabaseHelper.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, blockId);
            ResultSet rs = stmt.executeQuery();
            return rs.next();
        } catch (SQLException e) { return false; }
    }
}
