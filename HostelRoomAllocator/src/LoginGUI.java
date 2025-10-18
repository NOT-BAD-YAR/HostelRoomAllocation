import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.sql.*;

public class LoginGUI extends JFrame {
    private JTextField usernameField;
    private JPasswordField passwordField;
    private int failedAttempts = 0;

    public LoginGUI() {
        setTitle("Hostel Management Login");
        setSize(410, 370);
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setLocationRelativeTo(null);

        JPanel content = new JPanel() {
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                Graphics2D g2 = (Graphics2D) g;
                GradientPaint gp = new GradientPaint(0, 0, new Color(232,245,253), 0, getHeight(), new Color(255,251,233));
                g2.setPaint(gp);
                g2.fillRect(0,0,getWidth(),getHeight());
            }
        };
        content.setLayout(new BorderLayout());

        JLabel heading = new JLabel("Admin Login", JLabel.CENTER);
        heading.setFont(new java.awt.Font("SansSerif", java.awt.Font.BOLD, 24));
        heading.setBorder(BorderFactory.createEmptyBorder(24,0,12,0));
        content.add(heading, BorderLayout.NORTH);

        JPanel form = new JPanel(new GridLayout(2, 2, 14, 18));
        form.setOpaque(false);
        form.setBorder(BorderFactory.createEmptyBorder(8,48,12,48));

        form.add(new JLabel("Username:"));
        usernameField = new JTextField();
        usernameField.setFont(new java.awt.Font("SansSerif", java.awt.Font.PLAIN, 14));
        form.add(usernameField);

        form.add(new JLabel("Password:"));
        passwordField = new JPasswordField();
        passwordField.setFont(new java.awt.Font("SansSerif", java.awt.Font.PLAIN, 14));
        form.add(passwordField);

        content.add(form, BorderLayout.CENTER);

        JButton loginBtn = new JButton("Login");
        loginBtn.setFont(new java.awt.Font("SansSerif", java.awt.Font.BOLD, 15));
        loginBtn.setBackground(new Color(33, 150, 243));
        loginBtn.setForeground(Color.WHITE);
        loginBtn.setFocusPainted(false);
        loginBtn.setCursor(new Cursor(Cursor.HAND_CURSOR));
        loginBtn.addMouseListener(new MouseAdapter() {
            public void mouseEntered(MouseEvent e) { loginBtn.setBackground(new Color(25,105,190)); }
            public void mouseExited(MouseEvent e) { loginBtn.setBackground(new Color(33,150,243)); }
        });

        JButton signupBtn = new JButton("Sign Up");
        signupBtn.setFont(new java.awt.Font("SansSerif", java.awt.Font.BOLD, 15));
        signupBtn.setBackground(new Color(100, 181, 246));
        signupBtn.setForeground(Color.WHITE);
        signupBtn.setFocusPainted(false);
        signupBtn.setCursor(new Cursor(Cursor.HAND_CURSOR));

        JButton forgotBtn = new JButton("Forgot Password");
        forgotBtn.setFont(new java.awt.Font("SansSerif", java.awt.Font.BOLD, 15));
        forgotBtn.setBackground(new Color(240, 98, 146));
        forgotBtn.setForeground(Color.WHITE);
        forgotBtn.setFocusPainted(false);
        forgotBtn.setCursor(new Cursor(Cursor.HAND_CURSOR));

        JPanel buttonPanel = new JPanel(new FlowLayout());
        buttonPanel.setOpaque(false);
        buttonPanel.add(loginBtn);
        buttonPanel.add(signupBtn);
        buttonPanel.add(forgotBtn);
        content.add(buttonPanel, BorderLayout.SOUTH);

        loginBtn.addActionListener(e -> performLogin());
        signupBtn.addActionListener(e -> new RegisterGUI());
        forgotBtn.addActionListener(e -> new ForgotPasswordGUI());

        add(content);
        setVisible(true);
    }

    private void performLogin() {
        String username = usernameField.getText().trim();
        String password = new String(passwordField.getPassword());

        if(username.isEmpty() || password.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Enter both username and password.", "Validation", JOptionPane.WARNING_MESSAGE);
            return;
        }

        try (Connection conn = DatabaseHelper.getConnection()) {
            String sql = "SELECT * FROM users WHERE username=? AND password=?";
            try (PreparedStatement stmt = conn.prepareStatement(sql)) {
                stmt.setString(1, username);
                stmt.setString(2, password);
                ResultSet rs = stmt.executeQuery();
                if(rs.next()) {
                    JOptionPane.showMessageDialog(this, "Login successful!", "Success", JOptionPane.INFORMATION_MESSAGE);
                    dispose();
                    new MainDashboardGUI();
                } else {
                    failedAttempts++;
                    if(failedAttempts >= 3) {
                        AdminLockoutDialog.showLockoutDialog();
                        failedAttempts = 0;
                    } else {
                        JOptionPane.showMessageDialog(this, "Invalid credentials. Attempt " + failedAttempts + "/3", "Login Failed", JOptionPane.ERROR_MESSAGE);
                    }
                }
            }
        } catch (SQLException e) {
            JOptionPane.showMessageDialog(this, "Database error: " + e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    public static void main(String[] args) {
        new LoginGUI();
    }
}
