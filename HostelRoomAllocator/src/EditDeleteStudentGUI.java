import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.sql.*;
import java.util.*;

public class EditDeleteStudentGUI extends JFrame {
    private JComboBox<String> studentBox;
    private JTextField nameField, rollField, yearField, courseField, genderField, mobileField, parentField, addressField;
    private Map<String, Integer> studentMap = new HashMap<>();
    private int selectedStudentId = -1;

    public EditDeleteStudentGUI() {
        setTitle("👤 Edit/Delete Student");
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
                        0, 0, new Color(247,234,253),
                        0, getHeight(), new Color(237,242,255)
                );
                g2.setPaint(gp);
                g2.fillRect(0,0,getWidth(),getHeight());
            }
        };
        content.setLayout(new BorderLayout());
        add(content);

        JLabel title = new JLabel("Edit or Delete Student", JLabel.CENTER);
        title.setFont(new Font("Segoe UI", Font.BOLD, 24));
        title.setBorder(BorderFactory.createEmptyBorder(18, 0, 18, 0));
        content.add(title, BorderLayout.NORTH);

        JPanel form = new JPanel(new GridLayout(10,2,12,12));
        form.setOpaque(false);
        form.setBorder(BorderFactory.createEmptyBorder(8,44,10,44));

        form.add(new JLabel("Select Student:")); studentBox = new JComboBox<>(); styleCombo(studentBox); form.add(studentBox);

        form.add(new JLabel("Name:")); nameField = new JTextField(); styleField(nameField); form.add(nameField);
        form.add(new JLabel("Roll No.:")); rollField = new JTextField(); styleField(rollField); form.add(rollField);
        form.add(new JLabel("Year:")); yearField = new JTextField(); styleField(yearField); form.add(yearField);
        form.add(new JLabel("Course:")); courseField = new JTextField(); styleField(courseField); form.add(courseField);
        form.add(new JLabel("Gender:")); genderField = new JTextField(); styleField(genderField); form.add(genderField);
        form.add(new JLabel("Mobile:")); mobileField = new JTextField(); styleField(mobileField); form.add(mobileField);
        form.add(new JLabel("Parent Info:")); parentField = new JTextField(); styleField(parentField); form.add(parentField);
        form.add(new JLabel("Address:")); addressField = new JTextField(); styleField(addressField); form.add(addressField);

        JButton editBtn = new JButton("Edit Student");
        styleActionBtn(editBtn, new Color(63,81,181), new Color(92,107,192));
        JButton delBtn = new JButton("Delete Student");
        styleActionBtn(delBtn, new Color(244,67,54), new Color(229,115,115));

        form.add(editBtn); form.add(delBtn);

        content.add(form, BorderLayout.CENTER);

        loadStudentsFromDB();
        studentBox.addActionListener(e -> loadStudentDetails());

        editBtn.addActionListener(e -> updateStudentInDB());
        delBtn.addActionListener(e -> deleteStudentFromDB());

        setVisible(true);
    }


    private void styleField(JTextField field) {
        field.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        field.setBackground(new Color(255,255,255,240));
        field.setBorder(BorderFactory.createLineBorder(new Color(184,197,255), 1, true));
    }
    private void styleCombo(JComboBox<?> combo) {
        combo.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        combo.setBackground(new Color(255,255,255,235));
        combo.setBorder(BorderFactory.createLineBorder(new Color(184,197,255), 1, true));
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

    private void loadStudentsFromDB() {
        String sql = "SELECT student_id, name, roll_number FROM students";
        try (Connection conn = DatabaseHelper.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {
            while (rs.next()) {
                String label = rs.getString("name") + " (" + rs.getString("roll_number") + ")";
                int id = rs.getInt("student_id");
                studentMap.put(label, id);
                studentBox.addItem(label);
            }
            if(studentBox.getItemCount() > 0) loadStudentDetails();
        } catch (SQLException e) { studentBox.addItem("No Students Found"); }
    }

    private void loadStudentDetails() {
        String name = (String) studentBox.getSelectedItem();
        selectedStudentId = studentMap.getOrDefault(name, -1);
        if (selectedStudentId == -1) return;
        String sql = "SELECT * FROM students WHERE student_id = ?";
        try (Connection conn = DatabaseHelper.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, selectedStudentId);
            ResultSet rs = stmt.executeQuery();
            if (rs.next()) {
                nameField.setText(rs.getString("name"));
                rollField.setText(rs.getString("roll_number"));
                yearField.setText(String.valueOf(rs.getInt("year")));
                courseField.setText(rs.getString("course"));
                genderField.setText(rs.getString("gender"));
                mobileField.setText(rs.getString("mobile_number"));
                parentField.setText(rs.getString("parent_details"));
                addressField.setText(rs.getString("address"));
            }
        } catch (SQLException e) { }
    }

    private void updateStudentInDB() {
        if (selectedStudentId == -1) return;
        String sql = "UPDATE students SET name=?, roll_number=?, year=?, course=?, gender=?, mobile_number=?, parent_details=?, address=? WHERE student_id=?";
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
            stmt.setInt(9, selectedStudentId);
            int rows = stmt.executeUpdate();
            if (rows > 0) {
                JOptionPane.showMessageDialog(this, "Student updated!", "Success", JOptionPane.INFORMATION_MESSAGE);
            }
        } catch (SQLException e) {
            JOptionPane.showMessageDialog(this, "Error updating student.", "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void deleteStudentFromDB() {
        if (selectedStudentId == -1) return;
        int confirm = JOptionPane.showConfirmDialog(this, "Delete this student? All allocations will be removed.", "Confirm Delete", JOptionPane.YES_NO_OPTION);
        if (confirm != JOptionPane.YES_OPTION) return;
        String deleteAlloc = "DELETE FROM allocations WHERE student_id = ?";
        String deleteStudent = "DELETE FROM students WHERE student_id = ?";
        try (Connection conn = DatabaseHelper.getConnection();
             PreparedStatement allocStmt = conn.prepareStatement(deleteAlloc);
             PreparedStatement studentStmt = conn.prepareStatement(deleteStudent)) {
            allocStmt.setInt(1, selectedStudentId);
            allocStmt.executeUpdate();
            studentStmt.setInt(1, selectedStudentId);
            int rows = studentStmt.executeUpdate();
            if (rows > 0) {
                JOptionPane.showMessageDialog(this, "Student deleted!", "Success", JOptionPane.INFORMATION_MESSAGE);
                dispose();
            }
        } catch (SQLException e) {
            JOptionPane.showMessageDialog(this, "Error deleting student (check foreign key).", "Error", JOptionPane.ERROR_MESSAGE);
        }
    }
}
