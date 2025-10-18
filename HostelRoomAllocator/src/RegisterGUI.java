import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.sql.*;

public class RegisterGUI extends JFrame {
    private JTextField usernameField;
    private JPasswordField passwordField, confirmField;

    public RegisterGUI() {
        setTitle("User Registration");
        setSize(420, 320);
        setDefaultCloseOperation(DISPOSE_ON_CLOSE);
        setLocationRelativeTo(null);

        JPanel content = new JPanel() {
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                Graphics2D g2 = (Graphics2D) g;
                GradientPaint gp = new GradientPaint(0,0,new Color(255,251,233),0,getHeight(),new Color(232,245,253));
                g2.setPaint(gp);
                g2.fillRect(0,0,getWidth(),getHeight());
            }
        };
        content.setLayout(new BorderLayout());

        JLabel title = new JLabel("Register New User", JLabel.CENTER);
        title.setFont(new java.awt.Font("SansSerif", java.awt.Font.BOLD, 22));
        title.setBorder(BorderFactory.createEmptyBorder(20,0,10,0));
        content.add(title, BorderLayout.NORTH);

        JPanel form = new JPanel(new GridLayout(4,2,14,14));
        form.setOpaque(false);
        form.setBorder(BorderFactory.createEmptyBorder(10,44,24,44));

        form.add(new JLabel("Username:"));
        usernameField = new JTextField();
        usernameField.setFont(new java.awt.Font("SansSerif", java.awt.Font.PLAIN, 14));
        form.add(usernameField);

        form.add(new JLabel("Password:"));
        passwordField = new JPasswordField();
        passwordField.setFont(new java.awt.Font("SansSerif", java.awt.Font.PLAIN, 14));
        form.add(passwordField);

        form.add(new JLabel("Confirm Password:"));
        confirmField = new JPasswordField();
        confirmField.setFont(new java.awt.Font("SansSerif", java.awt.Font.PLAIN, 14));
        form.add(confirmField);

        JButton registerBtn = new JButton("Register");
        registerBtn.setFont(new java.awt.Font("SansSerif", java.awt.Font.BOLD, 15));
        registerBtn.setForeground(Color.WHITE);
        registerBtn.setBackground(new Color(67,160,71));
        registerBtn.setFocusPainted(false);
        registerBtn.setBorder(BorderFactory.createEmptyBorder(7,22,7,22));
        registerBtn.setCursor(new Cursor(Cursor.HAND_CURSOR));
        registerBtn.addMouseListener(new MouseAdapter() {
            public void mouseEntered(MouseEvent e) { registerBtn.setBackground(new Color(56,142,60)); }
            public void mouseExited(MouseEvent e) { registerBtn.setBackground(new Color(67,160,71)); }
        });

        form.add(registerBtn);
        form.add(new JLabel());

        content.add(form, BorderLayout.CENTER);

        registerBtn.addActionListener(e -> doRegister());

        add(content);
        setVisible(true);
    }

    private void doRegister() {
        String username = usernameField.getText().trim();
        String password = new String(passwordField.getPassword());
        String confirm = new String(confirmField.getPassword());

        if (username.isEmpty() || password.isEmpty() || confirm.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Fill all fields!", "Validation", JOptionPane.WARNING_MESSAGE);
            return;
        }
        if (!password.equals(confirm)) {
            JOptionPane.showMessageDialog(this, "Passwords do not match!", "Validation", JOptionPane.WARNING_MESSAGE);
            return;
        }

        try (Connection conn = DatabaseHelper.getConnection()) {
            String checkSQL = "SELECT * FROM users WHERE username=?";
            try (PreparedStatement cs = conn.prepareStatement(checkSQL)) {
                cs.setString(1, username);
                ResultSet rs = cs.executeQuery();
                if (rs.next()) {
                    JOptionPane.showMessageDialog(this, "Username already exists.", "Validation", JOptionPane.WARNING_MESSAGE);
                    return;
                }
            }
            String sql = "INSERT INTO users(username, password) VALUES (?, ?)";
            try (PreparedStatement ps = conn.prepareStatement(sql)) {
                ps.setString(1, username);
                ps.setString(2, password);
                int rows = ps.executeUpdate();
                if(rows > 0) {
                    JOptionPane.showMessageDialog(this, "Registered successfully! Now you can log in.", "Success", JOptionPane.INFORMATION_MESSAGE);
                    dispose();
                }
            }
        } catch (SQLException e) {
            JOptionPane.showMessageDialog(this, "Error: " + e.getMessage(), "DB Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    public static void main(String[] args) {
        new RegisterGUI();
    }
}
