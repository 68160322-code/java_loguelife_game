package core;

import core.GameFrame;
import core.AudioManager;
import core.SaveManager;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.util.function.Consumer;
import javax.imageio.ImageIO;

public class MainMenu extends JFrame {

    private static final String BG_IMAGE_PATH = "D:\\countless-lost-souls-drift-through-pale-field-of-flowers-their-faces-blurred-in-ethereal-landscape-twisted-tree-stands-in-center-reaching-dark-sky-whispering-secrets-photo.jpeg";

    public static void main(String[] args) {
        try { UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName()); } catch (Exception e) {}
        SwingUtilities.invokeLater(() -> new MainMenu().setVisible(true));
    }

    public MainMenu() {
        setTitle("Rogue Card Game");
        setSize(900, 600);
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setLocationRelativeTo(null);
        setUndecorated(false);

        AudioManager.playMusic(AudioManager.Track.LOBBY);

        BufferedImage bgImage = loadImage(BG_IMAGE_PATH);
        BackgroundPanel mainPanel = new BackgroundPanel(bgImage);
        mainPanel.setLayout(new BorderLayout());
        setContentPane(mainPanel);

        mainPanel.add(buildTitlePanel(), BorderLayout.NORTH);
        mainPanel.add(buildCenterPanel(), BorderLayout.CENTER);
        mainPanel.add(buildFooterPanel(), BorderLayout.SOUTH);
    }

    /** ถ้ามี save — ถามว่าจะเล่นต่อหรือเริ่มใหม่ */
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

        JLabel subtitle = new JLabel("— A FANTASY DUNGEON ADVENTURE —", SwingConstants.CENTER);
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

        JButton newGameBtn = createMenuButton("  New Round  ", new Color(120, 60, 10), new Color(200, 140, 40));
        JButton exitBtn    = createMenuButton("  Exit The Game  ", new Color(80, 10, 10), new Color(180, 50, 50));

        newGameBtn.addActionListener(e -> {
            SaveManager.deleteSave();
            AudioManager.stopMusic();
            new GameFrame();
            dispose();
        });
        exitBtn.addActionListener(e -> System.exit(0));

        // ปุ่ม "เล่นต่อ" — แสดงเฉพาะตอนมี save
        if (SaveManager.hasSave()) {
            JButton continueBtn = createMenuButton("  Continue Game ", new Color(20, 80, 40), new Color(40, 160, 70));
            continueBtn.addActionListener(e -> {
                AudioManager.stopMusic();
                new GameFrame(SaveManager.load());
                dispose();
            });
            btnPanel.add(continueBtn);
            btnPanel.add(Box.createRigidArea(new Dimension(0, 16)));
        }

        btnPanel.add(newGameBtn);
        btnPanel.add(Box.createRigidArea(new Dimension(0, 16)));
        btnPanel.add(exitBtn);
        btnPanel.add(Box.createRigidArea(new Dimension(0, 16)));

        // Settings button
        JButton settingsBtn = createMenuButton("  ⚙  Settings  ", new Color(40, 40, 70), new Color(80, 80, 130));
        settingsBtn.addActionListener(e -> openSettingsDialog());
        btnPanel.add(settingsBtn);

        wrapper.add(btnPanel);
        return wrapper;
    }

    private void openSettingsDialog() {
        JDialog dialog = new JDialog(this, "Settings", true);
        dialog.setUndecorated(true);

        JPanel root = new JPanel(new BorderLayout(0, 0)) {
            @Override protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g;
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(new Color(10, 8, 20));
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), 14, 14);
                g2.setColor(new Color(100, 70, 30));
                g2.setStroke(new java.awt.BasicStroke(2f));
                g2.drawRoundRect(1, 1, getWidth()-2, getHeight()-2, 14, 14);
            }
        };
        root.setOpaque(false);
        root.setBorder(BorderFactory.createEmptyBorder(20, 28, 20, 28));

        // Title
        JLabel title = new JLabel("⚙  Settings", SwingConstants.CENTER);
        title.setFont(new Font("Serif", Font.BOLD, 20));
        title.setForeground(new Color(210, 175, 90));
        title.setBorder(BorderFactory.createEmptyBorder(0, 0, 16, 0));
        root.add(title, BorderLayout.NORTH);

        // Audio panel
        JPanel audio = new JPanel();
        audio.setOpaque(false);
        audio.setLayout(new BoxLayout(audio, BoxLayout.Y_AXIS));

        JLabel audioTitle = new JLabel("♪  Audio");
        audioTitle.setFont(new Font("Serif", Font.BOLD, 14));
        audioTitle.setForeground(new Color(190, 160, 90));
        audioTitle.setAlignmentX(Component.LEFT_ALIGNMENT);
        audio.add(audioTitle);
        audio.add(Box.createRigidArea(new Dimension(0, 10)));

        audio.add(buildVolumeRow("Music",
                AudioManager.isMusicEnabled(), AudioManager.getMusicVolume(),
                AudioManager::setMusicEnabled, AudioManager::setMusicVolume));
        audio.add(Box.createRigidArea(new Dimension(0, 8)));
        audio.add(buildVolumeRow("Effects",
                AudioManager.isSfxEnabled(), AudioManager.getSfxVolume(),
                AudioManager::setSfxEnabled, AudioManager::setSfxVolume));

        root.add(audio, BorderLayout.CENTER);

        // Close button
        JButton closeBtn = createSmallBtn("  Close  ", new Color(80, 30, 30));
        closeBtn.setPreferredSize(new Dimension(100, 32));
        closeBtn.addActionListener(e -> dialog.dispose());
        JPanel south = new JPanel(new FlowLayout(FlowLayout.CENTER));
        south.setOpaque(false);
        south.setBorder(BorderFactory.createEmptyBorder(14, 0, 0, 0));
        south.add(closeBtn);
        root.add(south, BorderLayout.SOUTH);

        // ESC closes
        root.getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW)
                .put(KeyStroke.getKeyStroke(java.awt.event.KeyEvent.VK_ESCAPE, 0), "close");
        root.getActionMap().put("close", new javax.swing.AbstractAction() {
            public void actionPerformed(java.awt.event.ActionEvent e) { dialog.dispose(); }
        });

        dialog.setContentPane(root);
        dialog.pack();
        dialog.setMinimumSize(new Dimension(320, 220));
        dialog.setLocationRelativeTo(this);
        dialog.setVisible(true);
    }

    private JPanel buildVolumeRow(String label, boolean initEnabled, float initVol,
                                  Consumer<Boolean> onToggle,
                                  Consumer<Float> onVol) {
        JPanel row = new JPanel(new FlowLayout(FlowLayout.LEFT, 6, 0));
        row.setOpaque(false);
        row.setMaximumSize(new Dimension(400, 30));
        row.setAlignmentX(Component.LEFT_ALIGNMENT);

        // Label
        JLabel lbl = new JLabel(label);
        lbl.setFont(new Font("SansSerif", Font.PLAIN, 12));
        lbl.setForeground(new Color(190, 175, 140));
        lbl.setPreferredSize(new Dimension(60, 24));
        row.add(lbl);

        // Toggle ON/OFF
        JButton toggle = createSmallBtn(initEnabled ? "ON" : "OFF",
                initEnabled ? new Color(30, 100, 40) : new Color(100, 30, 30));
        toggle.setPreferredSize(new Dimension(42, 24));
        toggle.addActionListener(e -> {
            boolean nowOn = toggle.getText().equals("OFF");
            toggle.setText(nowOn ? "ON" : "OFF");
            // เปลี่ยนสีปุ่ม
            toggle.setBackground(nowOn ? new Color(30, 100, 40) : new Color(100, 30, 30));
            onToggle.accept(nowOn);
            toggle.repaint();
        });
        row.add(toggle);

        // ปุ่ม −
        JButton minus = createSmallBtn("−", new Color(60, 50, 80));
        minus.setPreferredSize(new Dimension(28, 24));

        // Volume label
        int[] volPct = {(int)(initVol * 100)};
        JLabel volLbl = new JLabel(volPct[0] + "%");
        volLbl.setFont(new Font("SansSerif", Font.BOLD, 12));
        volLbl.setForeground(new Color(220, 200, 150));
        volLbl.setPreferredSize(new Dimension(40, 24));
        volLbl.setHorizontalAlignment(SwingConstants.CENTER);

        // ปุ่ม +
        JButton plus = createSmallBtn("+", new Color(60, 50, 80));
        plus.setPreferredSize(new Dimension(28, 24));

        minus.addActionListener(e -> {
            volPct[0] = Math.max(0, volPct[0] - 10);
            volLbl.setText(volPct[0] + "%");
            onVol.accept(volPct[0] / 100f);
        });
        plus.addActionListener(e -> {
            volPct[0] = Math.min(100, volPct[0] + 10);
            volLbl.setText(volPct[0] + "%");
            onVol.accept(volPct[0] / 100f);
        });

        row.add(minus);
        row.add(volLbl);
        row.add(plus);

        return row;
    }

    private JButton createSmallBtn(String text, Color bg) {
        JButton btn = new JButton(text) {
            @Override protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g;
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(getBackground());
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), 6, 6);
                g2.setColor(getBackground().brighter());
                g2.setStroke(new java.awt.BasicStroke(1f));
                g2.drawRoundRect(1, 1, getWidth()-2, getHeight()-2, 6, 6);
                g2.setFont(new Font("SansSerif", Font.BOLD, 12));
                g2.setColor(Color.WHITE);
                FontMetrics fm = g2.getFontMetrics();
                g2.drawString(getText(), (getWidth()-fm.stringWidth(getText()))/2,
                        (getHeight()+fm.getAscent()-fm.getDescent())/2);
            }
            @Override protected void paintBorder(Graphics g) {}
        };
        btn.setBackground(bg);
        btn.setContentAreaFilled(false);
        btn.setFocusPainted(false);
        btn.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        return btn;
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
        JLabel ver = new JLabel("v0.1  |  Dare you descend?", SwingConstants.CENTER);
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