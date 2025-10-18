import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.sql.*;

public class AddBlockGUI extends JFrame {
    private JTextField blockNameField, descriptionField;

    public AddBlockGUI() {
        setTitle("🏢 Add New Block");
        setSize(430, 270);
        setLocationRelativeTo(null);
        setDefaultCloseOperation(DISPOSE_ON_CLOSE);

        // Gradient background panel
        JPanel content = new JPanel() {
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                Graphics2D g2 = (Graphics2D) g;
                GradientPaint gp = new GradientPaint(
                        0, 0, new Color(240, 246, 255),
                        0, getHeight(), new Color(220, 235, 250)
                );
                g2.setPaint(gp);
                g2.fillRect(0, 0, getWidth(), getHeight());
            }
        };
        content.setLayout(new BorderLayout());
        add(content);

        JLabel title = new JLabel("Add New Block", JLabel.CENTER);
        title.setFont(new java.awt.Font("SansSerif", java.awt.Font.BOLD, 22));
        title.setBorder(BorderFactory.createEmptyBorder(12, 0, 18, 0));
        content.add(title, BorderLayout.NORTH);

        JPanel form = new JPanel(new GridLayout(3, 2, 14, 18));
        form.setOpaque(false);
        form.setBorder(BorderFactory.createEmptyBorder(10, 40, 20, 40));

        form.add(new JLabel("Block Name:"));
        blockNameField = new JTextField(); styleField(blockNameField); form.add(blockNameField);

        form.add(new JLabel("Description:"));
        descriptionField = new JTextField(); styleField(descriptionField); form.add(descriptionField);

        JButton addBtn = new JButton("Add Block");
        addBtn.setFont(new java.awt.Font("SansSerif", java.awt.Font.BOLD, 15));
        addBtn.setForeground(Color.WHITE);
        addBtn.setBackground(new Color(0, 191, 165));
        addBtn.setFocusPainted(false);
        addBtn.setBorder(BorderFactory.createEmptyBorder(6, 12, 6, 12));
        addBtn.setCursor(new Cursor(Cursor.HAND_CURSOR));
        addBtn.addMouseListener(new MouseAdapter() {
            public void mouseEntered(MouseEvent e) { addBtn.setBackground(new Color(38,166,154)); }
            public void mouseExited(MouseEvent e) { addBtn.setBackground(new Color(0,191,165)); }
        });

        form.add(addBtn); form.add(new JLabel());

        content.add(form, BorderLayout.CENTER);

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

        setVisible(true);
    }

    private void styleField(JTextField field) {
        field.setFont(new java.awt.Font("SansSerif", java.awt.Font.PLAIN, 14));
        field.setBackground(new Color(255,255,255,235));
        field.setBorder(BorderFactory.createLineBorder(new Color(178,223,219), 1, true));
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
        } catch (SQLException e) { return false; }
    }

    private void addBlockToDB() {
        String sql = "INSERT INTO blocks (block_name, description) VALUES (?, ?)";
        try (Connection conn = DatabaseHelper.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, blockNameField.getText().trim());
            stmt.setString(2, descriptionField.getText().trim());
            if(stmt.executeUpdate()>0){
                JOptionPane.showMessageDialog(this, "Block added successfully!", "Success", JOptionPane.INFORMATION_MESSAGE);
                dispose();
            }
            else {
                JOptionPane.showMessageDialog(this, "Failed to add block.", "Error", JOptionPane.ERROR_MESSAGE);
            }
        } catch (SQLException e) {
            JOptionPane.showMessageDialog(this, "DB error: " + e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        }
    }
}
