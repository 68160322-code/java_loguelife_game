package core;

import javax.swing.*;
import javax.swing.border.*;
import java.awt.*;
import java.awt.event.*;

/**
 * PauseMenu — หน้าเมนูหยุดเกม
 * เปิดด้วยปุ่ม ESC หรือปุ่ม Pause ใน GameFrame
 *
 * ประกอบด้วย:
 *  - Resume       — กลับไปเล่นต่อ
 *  - Restart      — เริ่มเกมใหม่ทั้งหมด
 *  - Audio Settings — เปิด/ปิดเพลง + SFX พร้อม slider
 *  - Quit         — ออกจากเกม
 */
public class PauseMenu extends JDialog {

    public enum Result { RESUME, RESTART, QUIT }

    private Result result = Result.RESUME;

    public PauseMenu(JFrame parent) {
        super(parent, "Paused", true); // modal
        setDefaultCloseOperation(DISPOSE_ON_CLOSE);
        setUndecorated(true); // ไม่มี title bar

        buildUI(parent);
        pack();
        setLocationRelativeTo(parent);
    }

    public Result getResult() { return result; }

    // ── UI ────────────────────────────────────────────────────────────────────
    private void buildUI(JFrame parent) {
        JPanel root = new JPanel(new BorderLayout(0, 0)) {
            @Override protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g;
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                // พื้นหลังมืดโปร่งแสง + border
                g2.setColor(new Color(10, 8, 22));
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), 16, 16);
                g2.setColor(new Color(80, 60, 130));
                g2.setStroke(new BasicStroke(2f));
                g2.drawRoundRect(1, 1, getWidth()-2, getHeight()-2, 16, 16);
            }
        };
        root.setOpaque(false);
        root.setPreferredSize(new Dimension(360, 480));
        root.setBorder(new EmptyBorder(24, 28, 24, 28));

        // ── Header ──
        JLabel title = new JLabel("❚❚  THE JOURNEY PAUSES", SwingConstants.CENTER);
        title.setFont(new Font("SansSerif", Font.BOLD, 26));
        title.setForeground(new Color(210, 185, 255));
        title.setBorder(new EmptyBorder(0, 0, 16, 0));
        root.add(title, BorderLayout.NORTH);

        // ── Buttons + Audio ──
        JPanel center = new JPanel();
        center.setOpaque(false);
        center.setLayout(new BoxLayout(center, BoxLayout.Y_AXIS));

        // Resume
        center.add(menuButton("▶  Continue", new Color(50, 120, 60), new Color(35, 90, 45), e -> {
            result = Result.RESUME;
            dispose();
        }));
        center.add(Box.createRigidArea(new Dimension(0, 10)));

        // Restart
        center.add(menuButton("↺  New Run", new Color(130, 80, 20), new Color(100, 60, 15), e -> {
            int confirm = JOptionPane.showConfirmDialog(this,
                    "Begin a new run?\nAll progress shall be forsaken.",
                    "New Journey", JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE);
            if (confirm == JOptionPane.YES_OPTION) {
                result = Result.RESTART;
                dispose();
            }
        }));
        center.add(Box.createRigidArea(new Dimension(0, 20)));

        // ── Audio Settings ──
        JLabel audioTitle = new JLabel("♪  Sound", SwingConstants.LEFT);
        audioTitle.setFont(new Font("SansSerif", Font.BOLD, 13));
        audioTitle.setForeground(new Color(180, 170, 210));
        audioTitle.setBorder(new EmptyBorder(0, 2, 6, 0));
        audioTitle.setAlignmentX(Component.LEFT_ALIGNMENT);
        center.add(audioTitle);

        center.add(buildAudioPanel());
        center.add(Box.createRigidArea(new Dimension(0, 20)));

        // Quit → กลับหน้าหลัก
        center.add(menuButton("✕  Abandon Run", new Color(130, 30, 30), new Color(100, 20, 20), e -> {
            int confirm = JOptionPane.showConfirmDialog(this,
                    "Return to the surface?\nAll progress shall be forsaken.",
                    "The Crossroads", JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE);
            if (confirm == JOptionPane.YES_OPTION) {
                result = Result.QUIT;
                dispose();
            }
        }));

        root.add(center, BorderLayout.CENTER);

        // ESC ปิด = resume
        getRootPane().getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW)
                .put(KeyStroke.getKeyStroke(KeyEvent.VK_ESCAPE, 0), "close");
        getRootPane().getActionMap().put("close", new AbstractAction() {
            @Override public void actionPerformed(ActionEvent e) {
                result = Result.RESUME;
                dispose();
            }
        });

        setContentPane(root);
    }

    // ── Audio Panel ───────────────────────────────────────────────────────────
    private JPanel buildAudioPanel() {
        JPanel panel = new JPanel();
        panel.setOpaque(false);
        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
        panel.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(60, 50, 90), 1),
                new EmptyBorder(10, 12, 10, 12)));
        panel.setAlignmentX(Component.LEFT_ALIGNMENT);
        panel.setMaximumSize(new Dimension(9999, 130));

        // Music row
        panel.add(buildToggleRow("♩  Ambience",
                AudioManager.isMusicEnabled(),
                AudioManager.getMusicVolume(),
                (enabled) -> AudioManager.setMusicEnabled(enabled),
                (vol)     -> AudioManager.setMusicVolume(vol)));

        panel.add(Box.createRigidArea(new Dimension(0, 10)));

        // SFX row
        panel.add(buildToggleRow("✦  Combat Sounds",
                AudioManager.isSfxEnabled(),
                AudioManager.getSfxVolume(),
                (enabled) -> AudioManager.setSfxEnabled(enabled),
                (vol)     -> AudioManager.setSfxVolume(vol)));

        return panel;
    }

    /** สร้างแถว toggle + slider สำหรับ music หรือ sfx */
    private JPanel buildToggleRow(String label, boolean initEnabled, float initVol,
                                  java.util.function.Consumer<Boolean> onToggle,
                                  java.util.function.Consumer<Float> onVolume) {
        JPanel row = new JPanel(new BorderLayout(8, 0));
        row.setOpaque(false);
        row.setMaximumSize(new Dimension(9999, 40));

        // Label + Toggle
        JPanel left = new JPanel(new FlowLayout(FlowLayout.LEFT, 0, 0));
        left.setOpaque(false);

        JLabel lbl = new JLabel(label);
        lbl.setFont(new Font("SansSerif", Font.BOLD, 12));
        lbl.setForeground(Color.WHITE);
        lbl.setPreferredSize(new Dimension(100, 28));
        left.add(lbl);

        // Toggle button (ON/OFF)
        JButton toggle = new JButton(initEnabled ? "ON" : "OFF") {
            @Override protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g;
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                boolean on = getText().equals("ON");
                g2.setColor(on ? new Color(40, 140, 60) : new Color(100, 30, 30));
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), 8, 8);
                g2.setColor(on ? new Color(70, 200, 90) : new Color(180, 60, 60));
                g2.setStroke(new BasicStroke(1.5f));
                g2.drawRoundRect(1, 1, getWidth()-2, getHeight()-2, 8, 8);
                g2.setFont(new Font("SansSerif", Font.BOLD, 11));
                g2.setColor(Color.WHITE);
                FontMetrics fm = g2.getFontMetrics();
                g2.drawString(getText(), (getWidth()-fm.stringWidth(getText()))/2,
                        (getHeight()+fm.getAscent()-fm.getDescent())/2);
            }
            @Override protected void paintBorder(Graphics g) {}
        };
        toggle.setFont(new Font("SansSerif", Font.BOLD, 11));
        toggle.setPreferredSize(new Dimension(44, 26));
        toggle.setContentAreaFilled(false); toggle.setFocusPainted(false);
        toggle.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        toggle.addActionListener(e -> {
            boolean nowOn = toggle.getText().equals("OFF");
            toggle.setText(nowOn ? "ON" : "OFF");
            onToggle.accept(nowOn);
            toggle.repaint();
        });
        left.add(toggle);
        row.add(left, BorderLayout.WEST);

        // Volume slider
        JSlider slider = new JSlider(0, 100, (int)(initVol * 100));
        slider.setOpaque(false);
        slider.setForeground(new Color(140, 120, 200));
        slider.setPreferredSize(new Dimension(120, 28));
        slider.addChangeListener(e -> {
            onVolume.accept(slider.getValue() / 100f);
        });
        row.add(slider, BorderLayout.CENTER);

        // Volume % label
        JLabel pctL = new JLabel(slider.getValue() + "%", SwingConstants.RIGHT);
        pctL.setFont(new Font("SansSerif", Font.PLAIN, 11));
        pctL.setForeground(new Color(160, 155, 185));
        pctL.setPreferredSize(new Dimension(38, 28));
        slider.addChangeListener(e -> pctL.setText(slider.getValue() + "%"));
        row.add(pctL, BorderLayout.EAST);

        return row;
    }

    // ── Menu Button ───────────────────────────────────────────────────────────
    private JButton menuButton(String text, Color light, Color dark, ActionListener action) {
        JButton btn = new JButton(text) {
            @Override protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g;
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                GradientPaint gp = new GradientPaint(0, 0, light, 0, getHeight(), dark);
                g2.setPaint(gp);
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), 10, 10);
                g2.setColor(light.brighter());
                g2.setStroke(new BasicStroke(1.5f));
                g2.drawRoundRect(1, 1, getWidth()-2, getHeight()-2, 10, 10);
                g2.setFont(getFont()); g2.setColor(Color.WHITE);
                FontMetrics fm = g2.getFontMetrics();
                g2.drawString(getText(), (getWidth()-fm.stringWidth(getText()))/2,
                        (getHeight()+fm.getAscent()-fm.getDescent())/2);
            }
            @Override protected void paintBorder(Graphics g) {}
        };
        btn.setFont(new Font("SansSerif", Font.BOLD, 14));
        btn.setAlignmentX(Component.CENTER_ALIGNMENT);
        btn.setMaximumSize(new Dimension(9999, 44));
        btn.setPreferredSize(new Dimension(300, 44));
        btn.setContentAreaFilled(false); btn.setFocusPainted(false);
        btn.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        btn.addActionListener(action);

        // hover effect
        btn.addMouseListener(new MouseAdapter() {
            @Override public void mouseEntered(MouseEvent e) {
                btn.setBorder(BorderFactory.createLineBorder(new Color(200, 180, 255), 1, true));
                btn.repaint();
            }
            @Override public void mouseExited(MouseEvent e) {
                btn.setBorder(null);
                btn.repaint();
            }
        });
        return btn;
    }
}