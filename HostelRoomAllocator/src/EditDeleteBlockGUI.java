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
        setTitle("Edit/Delete Block");
        setSize(380, 220);
        setLocationRelativeTo(null);
        setDefaultCloseOperation(DISPOSE_ON_CLOSE);

        JPanel panel = new JPanel(new GridLayout(4,2,12,12));
        panel.setBorder(BorderFactory.createEmptyBorder(20, 24, 20, 24));

        panel.add(new JLabel("Select Block:")); blockBox = new JComboBox<>();   panel.add(blockBox);
        panel.add(new JLabel("Block Name:"));   blockNameField = new JTextField(); panel.add(blockNameField);
        panel.add(new JLabel("Description:"));  descriptionField = new JTextField(); panel.add(descriptionField);

        JButton editBtn = new JButton("Edit Block"); JButton delBtn = new JButton("Delete Block");
        panel.add(editBtn); panel.add(delBtn);

        loadBlocksFromDB();
        blockBox.addActionListener(e -> loadBlockDetails());

        editBtn.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) { updateBlockInDB(); }
        });
        delBtn.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) { deleteBlockFromDB(); }
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
        } catch (SQLException e) { /* ignore */ }
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
        // Check for rooms referencing this block first
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
        } catch (SQLException e) {
            return false;
        }
    }
}
