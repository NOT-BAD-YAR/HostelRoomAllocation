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
        setTitle("Edit/Delete Student");
        setSize(450, 490);
        setLocationRelativeTo(null);
        setDefaultCloseOperation(DISPOSE_ON_CLOSE);

        JPanel panel = new JPanel(new GridLayout(10,2,7,7));
        panel.setBorder(BorderFactory.createEmptyBorder(16, 28, 16, 28));

        panel.add(new JLabel("Select Student:")); studentBox = new JComboBox<>(); panel.add(studentBox);
        panel.add(new JLabel("Name:"));           nameField = new JTextField();   panel.add(nameField);
        panel.add(new JLabel("Roll No.:"));       rollField = new JTextField();   panel.add(rollField);
        panel.add(new JLabel("Year:"));           yearField = new JTextField();   panel.add(yearField);
        panel.add(new JLabel("Course:"));         courseField = new JTextField(); panel.add(courseField);
        panel.add(new JLabel("Gender:"));         genderField = new JTextField(); panel.add(genderField);
        panel.add(new JLabel("Mobile:"));         mobileField = new JTextField(); panel.add(mobileField);
        panel.add(new JLabel("Parent Info:"));    parentField = new JTextField(); panel.add(parentField);
        panel.add(new JLabel("Address:"));        addressField = new JTextField();panel.add(addressField);

        JButton editBtn = new JButton("Edit Student");
        JButton delBtn = new JButton("Delete Student");
        panel.add(editBtn); panel.add(delBtn);

        loadStudentsFromDB();

        studentBox.addActionListener(e -> loadStudentDetails());

        editBtn.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) { updateStudentInDB(); }
        });

        delBtn.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) { deleteStudentFromDB(); }
        });

        add(panel);
        setVisible(true);
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
        } catch (SQLException e) {
            studentBox.addItem("No Students Found");
        }
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
        } catch (SQLException e) { /* Handle error if desired */ }
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
