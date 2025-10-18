import javax.swing.*;
import java.awt.*;
import java.awt.event.*;

public class MainDashboardGUI extends JFrame {
    public MainDashboardGUI() {
        setTitle("🏨 Hostel Admin Dashboard");
        setSize(800, 500);
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setLocationRelativeTo(null);

        // Create gradient background panel
        JPanel mainPanel = new JPanel() {
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                Graphics2D g2d = (Graphics2D) g;
                g2d.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);
                int w = getWidth(), h = getHeight();
                Color color1 = new Color(240, 249, 255);
                Color color2 = new Color(230, 240, 255);
                GradientPaint gp = new GradientPaint(0, 0, color1, 0, h, color2);
                g2d.setPaint(gp);
                g2d.fillRect(0, 0, w, h);
            }
        };
        mainPanel.setLayout(new BorderLayout());

        // Header with gradient style
        JLabel heading = new JLabel("Hostel Room Management System", JLabel.CENTER);
        heading.setFont(new Font("Segoe UI", Font.BOLD, 32));
        heading.setForeground(new Color(30, 90, 180));
        heading.setBorder(BorderFactory.createEmptyBorder(30, 0, 30, 0));
        mainPanel.add(heading, BorderLayout.NORTH);

        // Button panel
        JPanel buttonPanel = new JPanel(new GridLayout(2, 4, 25, 25));
        buttonPanel.setOpaque(false);
        buttonPanel.setBorder(BorderFactory.createEmptyBorder(20, 50, 40, 50));

        JButton addBlockBtn = createGradientButton("Add Block", new Color(100, 181, 246), new Color(33, 150, 243));
        JButton addRoomBtn = createGradientButton("Add Room", new Color(129, 199, 132), new Color(67, 160, 71));
        JButton addStudentBtn = createGradientButton("Add Student", new Color(255, 183, 77), new Color(255, 152, 0));
        JButton manualAllocBtn = createGradientButton("Manual Allocation", new Color(186, 104, 200), new Color(142, 36, 170));
        JButton autoAllocBtn = createGradientButton("Auto Allocation", new Color(244, 143, 177), new Color(233, 30, 99));
        JButton viewReportsBtn = createGradientButton("View Reports", new Color(77, 182, 172), new Color(0, 150, 136));
        JButton exportBtn = createGradientButton("Export PDF/CSV", new Color(255, 138, 101), new Color(255, 87, 34));
        JButton editDeleteBtn = createGradientButton("Edit/Delete", new Color(149, 117, 205), new Color(103, 58, 183));

        buttonPanel.add(addBlockBtn);
        buttonPanel.add(addRoomBtn);
        buttonPanel.add(addStudentBtn);
        buttonPanel.add(manualAllocBtn);
        buttonPanel.add(autoAllocBtn);
        buttonPanel.add(viewReportsBtn);
        buttonPanel.add(exportBtn);
        buttonPanel.add(editDeleteBtn);

        // Connect buttons to actions
        addBlockBtn.addActionListener(e -> new AddBlockGUI());
        addRoomBtn.addActionListener(e -> new AddRoomGUI());
        addStudentBtn.addActionListener(e -> new AddStudentGUI());
        manualAllocBtn.addActionListener(e -> new ManualAllocationGUI());
        autoAllocBtn.addActionListener(e -> new AutoAllocationGUI());
        viewReportsBtn.addActionListener(e -> new ViewReportsGUI());
        exportBtn.addActionListener(e -> new ExportGUI());
        editDeleteBtn.addActionListener(e -> {
            String[] options = {"Student", "Room", "Block"};
            int choice = JOptionPane.showOptionDialog(
                    this, "Select entity to Edit/Delete:", "Edit/Delete",
                    JOptionPane.DEFAULT_OPTION, JOptionPane.QUESTION_MESSAGE,
                    null, options, options[0]
            );
            if (choice == 0) new EditDeleteStudentGUI();
            else if (choice == 1) new EditDeleteRoomGUI();
            else if (choice == 2) new EditDeleteBlockGUI();
        });

        mainPanel.add(buttonPanel, BorderLayout.CENTER);
        add(mainPanel);
        setVisible(true);
    }

    // Create beautiful gradient buttons
    private JButton createGradientButton(String text, Color color1, Color color2) {
        JButton btn = new JButton(text) {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2d = (Graphics2D) g.create();
                g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                int w = getWidth(), h = getHeight();
                GradientPaint gp = new GradientPaint(0, 0, color1, 0, h, color2);
                g2d.setPaint(gp);
                g2d.fillRoundRect(0, 0, w, h, 15, 15);
                g2d.dispose();
                super.paintComponent(g);
            }
        };
        btn.setFont(new Font("Segoe UI", Font.BOLD, 15));
        btn.setForeground(Color.WHITE);
        btn.setFocusPainted(false);
        btn.setBorderPainted(false);
        btn.setContentAreaFilled(false);
        btn.setCursor(new Cursor(Cursor.HAND_CURSOR));
        btn.setPreferredSize(new Dimension(160, 50));

        // Hover effect
        btn.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseEntered(java.awt.event.MouseEvent evt) {
                btn.setFont(new Font("Segoe UI", Font.BOLD, 16));
            }
            public void mouseExited(java.awt.event.MouseEvent evt) {
                btn.setFont(new Font("Segoe UI", Font.BOLD, 15));
            }
        });
        return btn;
    }
}
