import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.sql.*;

public class AddStudentGUI extends JFrame {
    private JTextField nameField, rollField, yearField, courseField, genderField, mobileField, parentField, addressField;
    private JButton addBtn;

    public AddStudentGUI() {
        setTitle("➕ Add New Student");
        setSize(500, 580);
        setLocationRelativeTo(null);
        setDefaultCloseOperation(DISPOSE_ON_CLOSE);

        // Gradient background panel
        JPanel content = new JPanel() {
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                Graphics2D g2 = (Graphics2D) g;
                GradientPaint gp = new GradientPaint(
                        0, 0, new Color(255, 250, 240),
                        0, getHeight(), new Color(245, 238, 228)
                );
                g2.setPaint(gp);
                g2.fillRect(0, 0, getWidth(), getHeight());
            }
        };
        content.setLayout(new BorderLayout());
        add(content);

        JLabel title = new JLabel("Add New Student", JLabel.CENTER);
        title.setFont(new Font("Segoe UI", Font.BOLD, 24));
        title.setBorder(BorderFactory.createEmptyBorder(20, 0, 20, 0));
        content.add(title, BorderLayout.NORTH);

        JPanel form = new JPanel(new GridLayout(9, 2, 12, 12));
        form.setOpaque(false);
        form.setBorder(BorderFactory.createEmptyBorder(10, 40, 10, 40));

        form.add(new JLabel("Name:"));
        nameField = new JTextField(); styleField(nameField); form.add(nameField);

        form.add(new JLabel("Roll No.:"));
        rollField = new JTextField(); styleField(rollField); form.add(rollField);

        form.add(new JLabel("Year:"));
        yearField = new JTextField(); styleField(yearField); form.add(yearField);

        form.add(new JLabel("Course:"));
        courseField = new JTextField(); styleField(courseField); form.add(courseField);

        form.add(new JLabel("Gender:"));
        genderField = new JTextField(); styleField(genderField); form.add(genderField);

        form.add(new JLabel("Mobile:"));
        mobileField = new JTextField(); styleField(mobileField); form.add(mobileField);

        form.add(new JLabel("Parent Info:"));
        parentField = new JTextField(); styleField(parentField); form.add(parentField);

        form.add(new JLabel("Address:"));
        addressField = new JTextField(); styleField(addressField); form.add(addressField);

        addBtn = new JButton("Add Student");
        addBtn.setFont(new Font("Segoe UI", Font.BOLD, 16));
        addBtn.setForeground(Color.WHITE);
        addBtn.setBackground(new Color(67, 160, 71));
        addBtn.setFocusPainted(false);
        addBtn.setCursor(new Cursor(Cursor.HAND_CURSOR));
        addBtn.setBorder(BorderFactory.createEmptyBorder(8, 20, 8, 20));
        addBtn.addMouseListener(new MouseAdapter() {
            public void mouseEntered(MouseEvent e) { addBtn.setBackground(new Color(56, 142, 60)); }
            public void mouseExited(MouseEvent e) { addBtn.setBackground(new Color(67, 160, 71)); }
        });

        form.add(addBtn);
        form.add(new JLabel());

        content.add(form, BorderLayout.CENTER);

        // Action Listener
        addBtn.addActionListener(e -> {
            if (validateFields()) {
                if (isDuplicateRollNumber(rollField.getText().trim())) {
                    JOptionPane.showMessageDialog(this, "Duplicate Roll Number!", "Error", JOptionPane.ERROR_MESSAGE);
                    return;
                }
                addStudentToDB();
            }
        });

        setVisible(true);
    }

    private void styleField(JTextField field) {
        field.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        field.setBackground(new Color(255, 255, 255, 230));
        field.setBorder(BorderFactory.createLineBorder(new Color(200, 200, 200), 1, true));
    }

    private boolean validateFields() {
        if (nameField.getText().trim().isEmpty() ||
                rollField.getText().trim().isEmpty() ||
                yearField.getText().trim().isEmpty() ||
                courseField.getText().trim().isEmpty() ||
                parentField.getText().trim().isEmpty() ||
                addressField.getText().trim().isEmpty()) {
            JOptionPane.showMessageDialog(this, "Fill all fields!", "Validation", JOptionPane.WARNING_MESSAGE);
            return false;
        }
        try {
            if (Integer.parseInt(yearField.getText().trim()) <= 0) throw new Exception();
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, "Year must be a positive integer.", "Validation", JOptionPane.WARNING_MESSAGE);
            return false;
        }
        String g = genderField.getText().trim().toLowerCase();
        if (!g.equals("male") && !g.equals("female")) {
            JOptionPane.showMessageDialog(this, "Gender must be 'male' or 'female'", "Validation", JOptionPane.WARNING_MESSAGE);
            return false;
        }
        if (mobileField.getText().trim().length() < 10) {
            JOptionPane.showMessageDialog(this, "Mobile number must be 10+ digits.", "Validation", JOptionPane.WARNING_MESSAGE);
            return false;
        }
        return true;
    }

    private boolean isDuplicateRollNumber(String rollNumber) {
        String sql = "SELECT student_id FROM students WHERE roll_number = ?";
        try (Connection conn = DatabaseHelper.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, rollNumber);
            return stmt.executeQuery().next();
        } catch (SQLException e) {
            return false;
        }
    }

    private void addStudentToDB() {
        String sql = "INSERT INTO students (name, roll_number, year, course, gender, mobile_number, parent_details, address) VALUES (?, ?, ?, ?, ?, ?, ?, ?)";
        try (Connection conn = DatabaseHelper.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, nameField.getText().trim());
            stmt.setString(2, rollField.getText().trim());
            stmt.setInt(3, Integer.parseInt(yearField.getText().trim()));
            stmt.setString(4, courseField.getText().trim());
            stmt.setString(5, genderField.getText().trim());
            stmt.setString(6, mobileField.getText().trim());
            stmt.setString(7, parentField.getText().trim());
            stmt.setString(8, addressField.getText().trim());
            if (stmt.executeUpdate() > 0) {
                JOptionPane.showMessageDialog(this, "Student added successfully!", "Success", JOptionPane.INFORMATION_MESSAGE);
                dispose();
            } else {
                JOptionPane.showMessageDialog(this, "Failed to add student.", "Error", JOptionPane.ERROR_MESSAGE);
            }
        } catch (SQLException e) {
            JOptionPane.showMessageDialog(this, "DB error: " + e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        }
    }
}
