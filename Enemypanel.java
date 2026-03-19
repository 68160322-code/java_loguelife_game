import javax.swing.*;
import java.awt.*;
import java.awt.event.*;

public class EnemyPanel extends JPanel {
    private Enemy enemy;
    private int animTick = 0;
    private JPanel statusPanel;
    private JPanel artPanel;

    // tooltip descriptions ของแต่ละสถานะ
    private static final String DESC_POISON   = "<html><b>[PSN] Poison</b><br>Deals damage equal to stack<br>at the start of enemy turn,<br>then reduces by 1.</html>";
    private static final String DESC_WEAK     = "<html><b>[WEK] Weak</b><br>Reduces attack damage by 25%.<br>Decreases by 1 each turn.</html>";
    private static final String DESC_STRENGTH = "<html><b>[STR] Strength</b><br>Increases attack damage<br>by the stack amount.</html>";

    public EnemyPanel() {
        setLayout(new BorderLayout(0, 4));
        setBackground(new Color(20, 20, 35));
        setBorder(BorderFactory.createLineBorder(new Color(80, 80, 120), 2));

        // Panel วาด pixel art (custom paintComponent)
        artPanel = new JPanel() {
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                if (enemy == null) return;
                Graphics2D g2 = (Graphics2D) g;
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                int w = getWidth(), h = getHeight();
                int bobY = (int)(Math.sin(animTick * 0.2) * 3);

                // HP Bar
                drawHpBar(g2, 10, 8, w - 20, 16, enemy.getHp(), enemy.getMaxHp());

                // Intent box
                drawIntentBox(g2, 10, 30, w - 20, enemy.getIntent());

                // ชื่อ + Level
                g2.setFont(new Font("SansSerif", Font.BOLD, 13));
                g2.setColor(Color.WHITE);
                String nameStr = enemy.getName() + "  [Lv." + enemy.getLevel() + "]";
                FontMetrics fm2 = g2.getFontMetrics();
                g2.drawString(nameStr, (w - fm2.stringWidth(nameStr)) / 2, 75);

                // pixel art
                drawMonster(g2, w / 2, h / 2 + 30 + bobY, enemy);
            }
        };
        artPanel.setBackground(new Color(20, 20, 35));
        artPanel.setOpaque(true);

        // Panel สถานะด้านล่าง
        statusPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 6, 4));
        statusPanel.setBackground(new Color(20, 20, 35));
        statusPanel.setPreferredSize(new Dimension(320, 36));

        add(artPanel, BorderLayout.CENTER);
        add(statusPanel, BorderLayout.SOUTH);

        // Timer animate
        Timer timer = new Timer(80, e -> {
            animTick++;
            artPanel.repaint();
        });
        timer.start();
    }

    public void setEnemy(Enemy enemy) {
        this.enemy = enemy;
        refreshStatus();
        artPanel.repaint();
    }

    // อัปเดต status badges ทุกครั้งที่ข้อมูลเปลี่ยน
    public void refreshStatus() {
        statusPanel.removeAll();
        if (enemy == null) { statusPanel.revalidate(); statusPanel.repaint(); return; }

        if (enemy.getPoison() > 0)
            statusPanel.add(makeStatusBtn("[PSN] " + enemy.getPoison(), new Color(60, 160, 60), DESC_POISON));
        if (enemy.getWeak() > 0)
            statusPanel.add(makeStatusBtn("[WEK] " + enemy.getWeak(), new Color(130, 60, 180), DESC_WEAK));
        if (enemy.getStrength() > 0)
            statusPanel.add(makeStatusBtn("[STR] " + enemy.getStrength(), new Color(180, 100, 20), DESC_STRENGTH));

        statusPanel.revalidate();
        statusPanel.repaint();
    }

    // สร้างปุ่ม badge คลิกได้ + tooltip
    private JButton makeStatusBtn(String label, Color bg, String tooltip) {
        JButton btn = new JButton(label);
        btn.setFont(new Font("SansSerif", Font.BOLD, 11));
        btn.setForeground(Color.WHITE);
        btn.setBackground(bg);
        btn.setFocusPainted(false);
        btn.setBorderPainted(true);
        btn.setBorder(BorderFactory.createLineBorder(bg.brighter(), 1));
        btn.setPreferredSize(new Dimension(80, 24));
        btn.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));

        // คลิกแล้วแสดง popup อธิบาย
        btn.addActionListener(e -> {
            JOptionPane.showMessageDialog(this, tooltip,
                    "Status Effect Info", JOptionPane.INFORMATION_MESSAGE);
        });
        return btn;
    }

    // ========== วาด HP Bar ==========
    private void drawHpBar(Graphics2D g, int x, int y, int w, int h, int hp, int maxHp) {
        g.setColor(new Color(60, 20, 20));
        g.fillRoundRect(x, y, w, h, 8, 8);
        double pct = (double) hp / maxHp;
        Color barColor = pct > 0.5 ? new Color(80, 200, 80)
                : pct > 0.25 ? new Color(220, 180, 40)
                : new Color(200, 50, 50);
        g.setColor(barColor);
        g.fillRoundRect(x, y, (int)(w * pct), h, 8, 8);
        g.setColor(new Color(150, 150, 150));
        g.drawRoundRect(x, y, w, h, 8, 8);
        g.setFont(new Font("SansSerif", Font.BOLD, 11));
        g.setColor(Color.WHITE);
        String txt = hp + " / " + maxHp;
        FontMetrics fm = g.getFontMetrics();
        g.drawString(txt, x + (w - fm.stringWidth(txt)) / 2, y + h - 2);
    }

    // ========== วาด Intent Box ==========
    private void drawIntentBox(Graphics2D g, int x, int y, int w, String intent) {
        g.setColor(new Color(40, 40, 70));
        g.fillRoundRect(x, y, w, 36, 10, 10);
        g.setColor(new Color(120, 120, 200));
        g.drawRoundRect(x, y, w, 36, 10, 10);
        g.setFont(new Font("SansSerif", Font.PLAIN, 11));
        g.setColor(new Color(255, 220, 100));
        FontMetrics fm = g.getFontMetrics();
        if (fm.stringWidth(intent) > w - 16) {
            int split = intent.lastIndexOf(' ', intent.length() / 2 + 10);
            if (split < 0) split = intent.length() / 2;
            g.drawString(intent.substring(0, split), x + 8, y + 14);
            g.drawString(intent.substring(split + 1), x + 8, y + 28);
        } else {
            g.drawString(intent, x + 8, y + 22);
        }
    }

    // ========== Pixel Art ==========
    private void drawMonster(Graphics2D g, int cx, int cy, Enemy e) {
        String name = e.getName().toLowerCase();
        if      (name.contains("berserker") || name.contains("rage")) drawRageBoss(g, cx, cy);
        else if (name.contains("guardian"))  drawGuardianBoss(g, cx, cy);
        else if (name.contains("slime"))     drawSlime(g, cx, cy);
        else if (name.contains("goblin"))    drawGoblin(g, cx, cy);
        else if (name.contains("skeleton"))  drawSkeleton(g, cx, cy);
        else if (name.contains("bat"))       drawBat(g, cx, cy);
        else if (name.contains("ghost"))     drawGhost(g, cx, cy);
        else                                 drawDefaultEnemy(g, cx, cy);
    }

    private void drawSlime(Graphics2D g, int cx, int cy) {
        int s = 6;
        int[][] body = {{0,0,1,1,1,1,0,0},{0,1,1,1,1,1,1,0},{1,1,1,1,1,1,1,1},{1,1,1,1,1,1,1,1},{1,1,1,1,1,1,1,1},{0,1,1,1,1,1,1,0},{0,0,1,1,1,1,0,0}};
        int[][] eyes = {{0,0,0,0,0,0,0,0},{0,0,1,0,0,1,0,0},{0,0,0,0,0,0,0,0}};
        drawPixelGrid(g, cx - body[0].length*s/2, cy - body.length*s/2, s, body, new Color(60, 200, 100));
        drawPixelGrid(g, cx - eyes[0].length*s/2, cy - body.length*s/2 + s, s, eyes, Color.BLACK);
    }

    private void drawGoblin(Graphics2D g, int cx, int cy) {
        int s = 5;
        int[][] body = {{0,0,1,1,1,0,0},{0,1,1,1,1,1,0},{0,0,1,0,1,0,0},{0,1,1,1,1,1,0},{1,1,1,1,1,1,1},{0,1,0,0,0,1,0},{0,1,0,0,0,1,0}};
        int[][] eyes = {{0,1,0,1,0}};
        drawPixelGrid(g, cx - body[0].length*s/2, cy - body.length*s/2, s, body, new Color(100, 180, 80));
        drawPixelGrid(g, cx - eyes[0].length*s/2, cy - body.length*s/2 + s, s, eyes, Color.RED);
    }

    private void drawSkeleton(Graphics2D g, int cx, int cy) {
        int s = 5;
        int[][] skull = {{0,1,1,1,0},{1,1,1,1,1},{1,0,1,0,1},{1,1,1,1,1},{0,1,0,1,0}};
        int[][] body  = {{0,1,1,1,0},{1,0,1,0,1},{0,1,1,1,0},{0,1,0,1,0},{1,0,0,0,1}};
        drawPixelGrid(g, cx - skull[0].length*s/2, cy - 50, s, skull, new Color(230, 230, 210));
        drawPixelGrid(g, cx - body[0].length*s/2,  cy - 20, s, body,  new Color(200, 200, 180));
    }

    private void drawBat(Graphics2D g, int cx, int cy) {
        int s = 5;
        int[][] bat  = {{1,0,0,1,1,0,0,1},{1,1,0,1,1,0,1,1},{0,1,1,1,1,1,1,0},{0,0,1,0,0,1,0,0},{0,0,0,1,1,0,0,0}};
        int[][] eyes = {{0,1,0,1,0}};
        drawPixelGrid(g, cx - bat[0].length*s/2, cy - bat.length*s/2, s, bat, new Color(80, 60, 120));
        drawPixelGrid(g, cx - eyes[0].length*s/2, cy - bat.length*s/2 + s, s, eyes, new Color(255, 80, 80));
    }

    private void drawGhost(Graphics2D g, int cx, int cy) {
        int s = 6;
        int[][] ghost = {{0,0,1,1,1,1,0,0},{0,1,1,1,1,1,1,0},{1,1,1,1,1,1,1,1},{1,1,1,1,1,1,1,1},{1,1,1,1,1,1,1,1},{1,0,1,1,1,1,0,1},{0,0,0,1,1,0,0,0}};
        int[][] eyes  = {{0,1,0,1,0,1,0}};
        g.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 0.75f));
        drawPixelGrid(g, cx - ghost[0].length*s/2, cy - ghost.length*s/2, s, ghost, new Color(180, 200, 255));
        g.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 1f));
        drawPixelGrid(g, cx - eyes[0].length*s/2, cy - ghost.length*s/2 + s, s, eyes, new Color(20, 20, 80));
    }

    private void drawGuardianBoss(Graphics2D g, int cx, int cy) {
        int s = 7;
        int[][] armor  = {{0,0,1,1,1,1,0,0},{0,1,1,1,1,1,1,0},{1,1,0,1,1,0,1,1},{1,1,1,1,1,1,1,1},{1,1,1,1,1,1,1,1},{0,1,1,1,1,1,1,0},{1,1,0,0,0,0,1,1},{1,1,0,0,0,0,1,1}};
        int[][] shield = {{0,1,1,1,0},{1,1,1,1,1},{1,1,1,1,1},{0,1,1,1,0},{0,0,1,0,0}};
        drawPixelGrid(g, cx - armor[0].length*s/2, cy - armor.length*s/2, s, armor, new Color(140, 160, 200));
        drawPixelGrid(g, cx + armor[0].length*s/2 - 10, cy - 10, s-2, shield, new Color(80, 120, 200));
        g.setColor(new Color(100, 200, 255));
        g.fillOval(cx - 14, cy - armor.length*s/2 + 16, 8, 8);
        g.fillOval(cx + 6,  cy - armor.length*s/2 + 16, 8, 8);
    }

    private void drawRageBoss(Graphics2D g, int cx, int cy) {
        int s = 7;
        int[][] body  = {{0,0,1,0,0,0,1,0,0},{0,0,1,1,1,1,1,0,0},{0,1,1,1,1,1,1,1,0},{1,1,0,1,1,1,0,1,1},{1,1,1,1,1,1,1,1,1},{1,1,1,1,1,1,1,1,1},{0,1,1,1,1,1,1,1,0},{0,1,1,0,0,0,1,1,0},{0,1,0,0,0,0,0,1,0}};
        int[][] horns = {{1,0,0,0,0,0,0,0,1},{1,1,0,0,0,0,0,1,1}};
        drawPixelGrid(g, cx - horns[0].length*s/2, cy - body.length*s/2 - horns.length*s, s, horns, new Color(180, 40, 40));
        drawPixelGrid(g, cx - body[0].length*s/2, cy - body.length*s/2, s, body, new Color(180, 60, 60));
        g.setColor(new Color(255, 100, 0));
        g.fillOval(cx - 15, cy - body.length*s/2 + 24, 9, 9);
        g.fillOval(cx + 6,  cy - body.length*s/2 + 24, 9, 9);
    }

    private void drawDefaultEnemy(Graphics2D g, int cx, int cy) {
        int s = 6;
        int[][] body = {{0,1,1,1,1,1,0},{1,1,1,1,1,1,1},{1,1,0,1,1,0,1},{1,1,1,1,1,1,1},{0,1,1,1,1,1,0},{0,1,0,0,0,1,0},{0,1,0,0,0,1,0}};
        drawPixelGrid(g, cx - body[0].length*s/2, cy - body.length*s/2, s, body, new Color(160, 100, 60));
    }

    private void drawPixelGrid(Graphics2D g, int startX, int startY, int size, int[][] grid, Color color) {
        for (int row = 0; row < grid.length; row++) {
            for (int col = 0; col < grid[row].length; col++) {
                if (grid[row][col] == 1) {
                    int px = startX + col * size;
                    int py = startY + row * size;
                    g.setColor(color);
                    g.fillRect(px, py, size, size);
                    g.setColor(color.darker());
                    g.drawRect(px, py, size, size);
                }
            }
        }
    }
}