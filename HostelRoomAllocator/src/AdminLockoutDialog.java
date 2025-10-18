import javax.swing.*;
import java.awt.*;

public class AdminLockoutDialog {
    public static void showLockoutDialog() {
        JLabel label = new JLabel("<html><b>Account locked!</b><br/>Too many failed login attempts.<br/>Please contact Admin to unlock.</html>");
        label.setFont(new java.awt.Font("SansSerif", java.awt.Font.BOLD, 16));
        JOptionPane.showMessageDialog(null, label, "Admin Lockout", JOptionPane.ERROR_MESSAGE);
    }

    public static void main(String[] args) {
        showLockoutDialog();
    }
}
