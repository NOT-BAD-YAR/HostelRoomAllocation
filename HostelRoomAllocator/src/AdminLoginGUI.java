import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

public class AdminLoginGUI extends JFrame {
    private JTextField usernameField;
    private JPasswordField passwordField;

    public AdminLoginGUI() {
        setTitle("Hostel Admin Login");
        setSize(400, 220);
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setLocationRelativeTo(null); // Center

        JPanel panel = new JPanel();
        panel.setLayout(null);

        JLabel titleLabel = new JLabel("Hostel Room Admin Login", SwingConstants.CENTER);
        titleLabel.setFont(new Font("Arial", Font.BOLD, 20));
        titleLabel.setBounds(40, 15, 320, 30);
        panel.add(titleLabel);

        JLabel userLabel = new JLabel("Username:");
        userLabel.setBounds(60, 60, 80, 25);
        panel.add(userLabel);

        usernameField = new JTextField();
        usernameField.setBounds(160, 60, 180, 25);
        panel.add(usernameField);

        JLabel passLabel = new JLabel("Password:");
        passLabel.setBounds(60, 95, 80, 25);
        panel.add(passLabel);

        passwordField = new JPasswordField();
        passwordField.setBounds(160, 95, 180, 25);
        panel.add(passwordField);

        JButton loginBtn = new JButton("Login");
        loginBtn.setBounds(145, 140, 90, 30);
        panel.add(loginBtn);

        loginBtn.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                String username = usernameField.getText().trim();
                String password = new String(passwordField.getPassword());
                boolean success = AdminLogin.authenticate(username, password); // <-- uses your existing DB login logic

                if (success) {
                    JOptionPane.showMessageDialog(AdminLoginGUI.this,
                            "Login successful!", "Welcome",
                            JOptionPane.INFORMATION_MESSAGE);
                    // Next: open MainDashboardGUI window here!
                    dispose();
                    new MainDashboardGUI();
                } else {
                    JOptionPane.showMessageDialog(AdminLoginGUI.this,
                            "Invalid username or password.", "Login Failed",
                            JOptionPane.ERROR_MESSAGE);
                }
            }
        });

        add(panel);
        setVisible(true);
    }

    public static void main(String[] args) {
        new AdminLoginGUI();
    }
}
