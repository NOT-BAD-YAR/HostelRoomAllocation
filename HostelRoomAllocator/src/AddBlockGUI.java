import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.sql.*;

public class AddBlockGUI extends JFrame {
    private JTextField blockNameField, descriptionField;

    public AddBlockGUI() {
        setTitle("Add New Block");
        setSize(380, 250);
        setLocationRelativeTo(null);
        setDefaultCloseOperation(DISPOSE_ON_CLOSE);

        JPanel panel = new JPanel(new GridLayout(3,2,10,16));
        panel.setBorder(BorderFactory.createEmptyBorder(20, 24, 20, 24));

        panel.add(new JLabel("Block Name:"));      blockNameField = new JTextField();     panel.add(blockNameField);
        panel.add(new JLabel("Description:"));     descriptionField = new JTextField();   panel.add(descriptionField);

        JButton addBtn = new JButton("Add Block"); panel.add(addBtn);   panel.add(new JLabel());

        addBtn.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                if (validateFields()) {
                    if (isDuplicateBlock(blockNameField.getText().trim())) {
                        JOptionPane.showMessageDialog(AddBlockGUI.this, "Block name already exists!", "Error", JOptionPane.ERROR_MESSAGE);
                        return;
                    }
                    addBlockToDB();
                }
            }
        });

        add(panel);
        setVisible(true);
    }

    private boolean validateFields() {
        if (blockNameField.getText().trim().isEmpty() || descriptionField.getText().trim().isEmpty()) {
            JOptionPane.showMessageDialog(this, "All fields required!", "Validation", JOptionPane.WARNING_MESSAGE);
            return false;
        }
        return true;
    }

    private boolean isDuplicateBlock(String blockName) {
        String sql = "SELECT block_id FROM blocks WHERE block_name = ?";
        try (Connection conn = DatabaseHelper.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, blockName);
            ResultSet rs = stmt.executeQuery();
            return rs.next();
        } catch (SQLException e) {
            return false;
        }
    }

    private void addBlockToDB() {
        String sql = "INSERT INTO blocks (block_name, description) VALUES (?, ?)";
        try (Connection conn = DatabaseHelper.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, blockNameField.getText().trim());
            stmt.setString(2, descriptionField.getText().trim());
            int rowsInserted = stmt.executeUpdate();
            if (rowsInserted > 0) {
                JOptionPane.showMessageDialog(this, "Block added successfully!", "Success", JOptionPane.INFORMATION_MESSAGE);
                dispose();
            } else {
                JOptionPane.showMessageDialog(this, "Failed to add block.", "Error", JOptionPane.ERROR_MESSAGE);
            }
        } catch (SQLException e) {
            JOptionPane.showMessageDialog(this, "DB error: " + e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        }
    }
}
