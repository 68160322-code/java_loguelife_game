import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.awt.image.BufferedImage;
import java.io.File;
import javax.imageio.ImageIO;

public class MainMenu extends JFrame {

    // ===== กำหนด path รูปภาพตรงนี้ =====
    private static final String BG_IMAGE_PATH = "D:\\countless-lost-souls-drift-through-pale-field-of-flowers-their-faces-blurred-in-ethereal-landscape-twisted-tree-stands-in-center-reaching-dark-sky-whispering-secrets-photo.jpeg"; // เปลี่ยน path ได้เลย
    // ====================================

    public static void main(String[] args) {
        try {
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
        } catch (Exception e) {}
        SwingUtilities.invokeLater(() -> new MainMenu().setVisible(true));
    }

    public MainMenu() {
        setTitle("Rogue Card Game");
        setSize(900, 600);
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setLocationRelativeTo(null);
        setUndecorated(false);

        // โหลดรูป background
        BufferedImage bgImage = loadImage(BG_IMAGE_PATH);

        // สร้าง main panel วาด background
        BackgroundPanel mainPanel = new BackgroundPanel(bgImage);
        mainPanel.setLayout(new BorderLayout());
        setContentPane(mainPanel);

        // overlay มืดเพื่อให้ข้อความอ่านง่าย
        mainPanel.add(buildTitlePanel(), BorderLayout.NORTH);
        mainPanel.add(buildCenterPanel(), BorderLayout.CENTER);
        mainPanel.add(buildFooterPanel(), BorderLayout.SOUTH);
    }

    // ===== โหลดรูป =====
    private BufferedImage loadImage(String path) {
        try {
            File f = new File(path);
            if (f.exists()) return ImageIO.read(f);
        } catch (Exception e) {
            System.out.println("Cannot load image: " + path);
        }
        return null; // ถ้าไม่มีรูป จะใช้ gradient แทนอัตโนมัติ
    }

    // ===== Title Panel =====
    private JPanel buildTitlePanel() {
        JPanel panel = new JPanel() {
            @Override protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                // gradient overlay ด้านบน
                Graphics2D g2 = (Graphics2D) g;
                GradientPaint gp = new GradientPaint(0, 0,
                        new Color(0, 0, 0, 180), 0, getHeight(), new Color(0, 0, 0, 0));
                g2.setPaint(gp);
                g2.fillRect(0, 0, getWidth(), getHeight());
            }
        };
        panel.setOpaque(false);
        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
        panel.setBorder(BorderFactory.createEmptyBorder(40, 0, 20, 0));

        // ชื่อเกม
        JLabel title = new JLabel("ROGUE CARD", SwingConstants.CENTER) {
            @Override protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g;
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                // เงาข้อความ
                g2.setFont(getFont());
                g2.setColor(new Color(0, 0, 0, 150));
                FontMetrics fm = g2.getFontMetrics();
                int x = (getWidth() - fm.stringWidth(getText())) / 2;
                g2.drawString(getText(), x + 3, fm.getAscent() + 3);
                // ข้อความหลัก gradient สีทอง
                GradientPaint gp = new GradientPaint(0, 0, new Color(255, 220, 80),
                        0, getHeight(), new Color(180, 100, 20));
                g2.setPaint(gp);
                g2.drawString(getText(), x, fm.getAscent());
            }
        };
        title.setFont(new Font("Serif", Font.BOLD, 72));
        title.setAlignmentX(Component.CENTER_ALIGNMENT);
        title.setPreferredSize(new Dimension(900, 90));

        JLabel subtitle = new JLabel("- A FANTASY DUNGEON ADVENTURE -", SwingConstants.CENTER);
        subtitle.setFont(new Font("Serif", Font.ITALIC, 18));
        subtitle.setForeground(new Color(200, 180, 120));
        subtitle.setAlignmentX(Component.CENTER_ALIGNMENT);

        panel.add(title);
        panel.add(Box.createRigidArea(new Dimension(0, 6)));
        panel.add(subtitle);
        return panel;
    }

    // ===== Center Panel (ปุ่ม) =====
    private JPanel buildCenterPanel() {
        JPanel wrapper = new JPanel(new GridBagLayout());
        wrapper.setOpaque(false);

        JPanel btnPanel = new JPanel();
        btnPanel.setLayout(new BoxLayout(btnPanel, BoxLayout.Y_AXIS));
        btnPanel.setOpaque(false);

        JButton startBtn = createMenuButton("  BEGIN JOURNEY  ", new Color(120, 60, 10), new Color(200, 140, 40));
        JButton settingsBtn = createMenuButton("  CHANGE IMAGE  ", new Color(30, 50, 80), new Color(80, 130, 200));
        JButton exitBtn = createMenuButton("  QUIT GAME  ", new Color(80, 10, 10), new Color(180, 50, 50));

        startBtn.addActionListener(e -> { new GameFrame(); dispose(); });
        settingsBtn.addActionListener(e -> changeBackgroundImage());
        exitBtn.addActionListener(e -> System.exit(0));

        btnPanel.add(startBtn);
        btnPanel.add(Box.createRigidArea(new Dimension(0, 16)));
        btnPanel.add(settingsBtn);
        btnPanel.add(Box.createRigidArea(new Dimension(0, 16)));
        btnPanel.add(exitBtn);

        wrapper.add(btnPanel);
        return wrapper;
    }

    // ===== Footer =====
    private JPanel buildFooterPanel() {
        JPanel panel = new JPanel() {
            @Override protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g;
                GradientPaint gp = new GradientPaint(0, 0, new Color(0,0,0,0),
                        0, getHeight(), new Color(0,0,0,180));
                g2.setPaint(gp);
                g2.fillRect(0, 0, getWidth(), getHeight());
            }
        };
        panel.setOpaque(false);
        panel.setPreferredSize(new Dimension(900, 50));
        JLabel ver = new JLabel("v0.1  |  Use arrow keys or mouse to play", SwingConstants.CENTER);
        ver.setForeground(new Color(150, 130, 90));
        ver.setFont(new Font("SansSerif", Font.PLAIN, 12));
        panel.setLayout(new BorderLayout());
        panel.add(ver, BorderLayout.CENTER);
        return panel;
    }

    // ===== สร้างปุ่ม Fantasy style =====
    private JButton createMenuButton(String text, Color bgDark, Color bgLight) {
        JButton btn = new JButton(text) {
            @Override protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g;
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                // gradient background
                GradientPaint gp = new GradientPaint(0, 0, bgLight, 0, getHeight(), bgDark);
                g2.setPaint(gp);
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), 12, 12);
                // ขอบทอง
                g2.setColor(new Color(200, 160, 60));
                g2.setStroke(new BasicStroke(2));
                g2.drawRoundRect(1, 1, getWidth()-2, getHeight()-2, 12, 12);
                // ข้อความ
                g2.setFont(getFont());
                g2.setColor(Color.WHITE);
                FontMetrics fm = g2.getFontMetrics();
                int x = (getWidth() - fm.stringWidth(getText())) / 2;
                int y = (getHeight() + fm.getAscent() - fm.getDescent()) / 2;
                // เงา
                g2.setColor(new Color(0,0,0,120));
                g2.drawString(getText(), x+2, y+2);
                g2.setColor(Color.WHITE);
                g2.drawString(getText(), x, y);
            }
            @Override protected void paintBorder(Graphics g) {}
        };
        btn.setFont(new Font("Serif", Font.BOLD, 20));
        btn.setAlignmentX(Component.CENTER_ALIGNMENT);
        btn.setPreferredSize(new Dimension(280, 55));
        btn.setMaximumSize(new Dimension(280, 55));
        btn.setContentAreaFilled(false);
        btn.setFocusPainted(false);
        btn.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));

        // hover effect
        btn.addMouseListener(new MouseAdapter() {
            @Override public void mouseEntered(MouseEvent e) {
                btn.setFont(new Font("Serif", Font.BOLD, 22));
                btn.repaint();
            }
            @Override public void mouseExited(MouseEvent e) {
                btn.setFont(new Font("Serif", Font.BOLD, 20));
                btn.repaint();
            }
        });
        return btn;
    }

    // ===== เปลี่ยนรูป Background ผ่าน File Chooser =====
    private void changeBackgroundImage() {
        JFileChooser chooser = new JFileChooser();
        chooser.setDialogTitle("Select Background Image");
        chooser.setFileFilter(new javax.swing.filechooser.FileNameExtensionFilter(
                "Images (png, jpg, jpeg, gif)", "png", "jpg", "jpeg", "gif"));

        int result = chooser.showOpenDialog(this);
        if (result == JFileChooser.APPROVE_OPTION) {
            File selected = chooser.getSelectedFile();
            try {
                BufferedImage newBg = ImageIO.read(selected);
                ((BackgroundPanel) getContentPane()).setBackground(newBg);
                repaint();
                JOptionPane.showMessageDialog(this,
                        "Image loaded!\nPath: " + selected.getAbsolutePath()
                                + "\n\nCopy path above to BG_IMAGE_PATH in MainMenu.java\nto load automatically next time.",
                        "Image Changed", JOptionPane.INFORMATION_MESSAGE);
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(this, "Cannot load image!", "Error", JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    // ===== Panel วาด background image หรือ gradient ถ้าไม่มีรูป =====
    static class BackgroundPanel extends JPanel {
        private BufferedImage bgImage;

        public BackgroundPanel(BufferedImage img) {
            this.bgImage = img;
        }

        public void setBackground(BufferedImage img) {
            this.bgImage = img;
        }

        @Override protected void paintComponent(Graphics g) {
            super.paintComponent(g);
            Graphics2D g2 = (Graphics2D) g;
            g2.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BILINEAR);

            if (bgImage != null) {
                // วาดรูป cover เต็มหน้าจอ
                g2.drawImage(bgImage, 0, 0, getWidth(), getHeight(), null);
                // overlay มืดเล็กน้อยเพื่อให้ข้อความอ่านได้
                g2.setColor(new Color(0, 0, 0, 100));
                g2.fillRect(0, 0, getWidth(), getHeight());
            } else {
                // ไม่มีรูป → วาด gradient แฟนตาซี
                GradientPaint gp = new GradientPaint(
                        0, 0, new Color(10, 5, 30),
                        0, getHeight(), new Color(40, 15, 60));
                g2.setPaint(gp);
                g2.fillRect(0, 0, getWidth(), getHeight());
                // วาดดาวสุ่ม
                g2.setColor(new Color(255, 255, 255, 80));
                java.util.Random rand = new java.util.Random(42);
                for (int i = 0; i < 120; i++) {
                    int sx = rand.nextInt(900);
                    int sy = rand.nextInt(600);
                    int ss = rand.nextInt(3) + 1;
                    g2.fillOval(sx, sy, ss, ss);
                }
            }
        }
    }
}