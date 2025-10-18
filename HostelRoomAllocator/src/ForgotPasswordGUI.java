import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.sql.*;

public class ForgotPasswordGUI extends JFrame {
    private JTextField usernameField;
    private JPasswordField newPassField, confirmField;

    public ForgotPasswordGUI() {
        setTitle("Forgot Password");
        setSize(420, 320);
        setDefaultCloseOperation(DISPOSE_ON_CLOSE);
        setLocationRelativeTo(null);

        JPanel content = new JPanel() {
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                Graphics2D g2 = (Graphics2D) g;
                GradientPaint gp = new GradientPaint(0,0,new Color(255,241,245),0,getHeight(),new Color(236,239,241));
                g2.setPaint(gp);
                g2.fillRect(0,0,getWidth(),getHeight());
            }
        };
        content.setLayout(new BorderLayout());

        JLabel title = new JLabel("Reset Password", JLabel.CENTER);
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

        form.add(new JLabel("New Password:"));
        newPassField = new JPasswordField();
        newPassField.setFont(new java.awt.Font("SansSerif", java.awt.Font.PLAIN, 14));
        form.add(newPassField);

        form.add(new JLabel("Confirm:"));
        confirmField = new JPasswordField();
        confirmField.setFont(new java.awt.Font("SansSerif", java.awt.Font.PLAIN, 14));
        form.add(confirmField);

        JButton resetBtn = new JButton("Reset");
        resetBtn.setFont(new java.awt.Font("SansSerif", java.awt.Font.BOLD, 15));
        resetBtn.setForeground(Color.WHITE);
        resetBtn.setBackground(new Color(239,83,80));
        resetBtn.setFocusPainted(false);
        resetBtn.setBorder(BorderFactory.createEmptyBorder(7,22,7,22));
        resetBtn.setCursor(new Cursor(Cursor.HAND_CURSOR));
        resetBtn.addMouseListener(new MouseAdapter() {
            public void mouseEntered(MouseEvent e) { resetBtn.setBackground(new Color(198,40,40)); }
            public void mouseExited(MouseEvent e) { resetBtn.setBackground(new Color(239,83,80)); }
        });

        form.add(resetBtn);
        form.add(new JLabel());

        content.add(form, BorderLayout.CENTER);

        resetBtn.addActionListener(e -> doReset());

        add(content);
        setVisible(true);
    }

    private void doReset() {
        String username = usernameField.getText().trim();
        String pass = new String(newPassField.getPassword());
        String confirm = new String(confirmField.getPassword());

        if(username.isEmpty() || pass.isEmpty() || confirm.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Fill all fields!", "Validation", JOptionPane.WARNING_MESSAGE);
            return;
        }
        if(!pass.equals(confirm)) {
            JOptionPane.showMessageDialog(this, "Passwords do not match!", "Validation", JOptionPane.WARNING_MESSAGE);
            return;
        }

        try (Connection conn = DatabaseHelper.getConnection()) {
            String sql = "UPDATE users SET password=? WHERE username=?";
            try (PreparedStatement ps = conn.prepareStatement(sql)) {
                ps.setString(1, pass);
                ps.setString(2, username);
                int rows = ps.executeUpdate();
                if(rows > 0) {
                    JOptionPane.showMessageDialog(this, "Password reset! Please login.", "Success", JOptionPane.INFORMATION_MESSAGE);
                    dispose();
                } else {
                    JOptionPane.showMessageDialog(this, "Username not found.", "Validation", JOptionPane.ERROR_MESSAGE);
                }
            }
        } catch (SQLException e) {
            JOptionPane.showMessageDialog(this, "Error: " + e.getMessage(), "DB Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    public static void main(String[] args) {
        new ForgotPasswordGUI();
    }
}
