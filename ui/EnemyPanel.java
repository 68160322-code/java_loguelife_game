package ui;

import entity.Enemy;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.awt.geom.*;

/**
 * EnemyPanel — vector-drawn monsters with animations:
 *  - idle: gentle bob + breathe
 *  - attack: lunge forward + flash
 *  - hurt: shake + red tint
 *  - die: fade + shrink
 *
 * FIXED: แก้ไขการแสดง status effects ให้ครบถ้วน
 */
public class EnemyPanel extends JPanel {
    private Enemy enemy;
    private JLabel intentLabel;
    private JPanel statusPanel;

    // ── Animation States ──────────────────────────────────────────────────────
    public enum AnimState { IDLE, ATTACK, HURT, DIE }

    private int        animTick  = 0;
    private AnimState  animState = AnimState.IDLE;
    private int        animFrame = 0;   // frame counter ภายใน state ปัจจุบัน
    private float      alpha     = 1f;  // สำหรับ die fade
    private JPanel     artPanel;

    private static final String DESC_POISON   = "<html><b>[PSN] Poison</b><br>Deals damage equal to stack<br>at start of enemy turn.</html>";
    private static final String DESC_WEAK     = "<html><b>[WEK] Weak</b><br>Reduces attack damage by 25%.</html>";
    private static final String DESC_STRENGTH = "<html><b>[STR] Strength</b><br>Increases attack damage.</html>";
    private static final String DESC_VULNERABLE = "<html><b>[VUL] Vulnerable</b><br>Takes 50% more damage.</html>";
    private static final String DESC_BLOCK = "<html><b>[BLK] Block</b><br>Prevents damage.</html>";

    public EnemyPanel() {
        setLayout(new BorderLayout(0, 4));
        setBackground(new Color(16, 14, 28));
        setBorder(BorderFactory.createLineBorder(new Color(70, 60, 110), 2));

        artPanel = new JPanel() {
            @Override protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                if (enemy == null) return;
                Graphics2D g2 = (Graphics2D) g;
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING,       RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setRenderingHint(RenderingHints.KEY_RENDERING,          RenderingHints.VALUE_RENDER_QUALITY);
                g2.setRenderingHint(RenderingHints.KEY_STROKE_CONTROL,     RenderingHints.VALUE_STROKE_PURE);

                int w = getWidth(), h = getHeight();

                // Background atmosphere
                drawAtmosphere(g2, w, h);

                // HP bar + intent
                drawHpBar(g2, 12, 8, w - 24, 16);
                drawIntentBox(g2, 12, 30, w - 24);

                // Name + Level
                g2.setFont(new Font("SansSerif", Font.BOLD, 14));
                g2.setColor(Color.WHITE);
                String nameStr = enemy.getName() + "  [Lv." + enemy.getLevel() + "]";
                FontMetrics fm = g2.getFontMetrics();
                g2.drawString(nameStr, (w - fm.stringWidth(nameStr)) / 2, 76);

                // Monster drawing with animation offset
                int cx = w / 2, cy = h / 2 + 28;
                float[] offsets = calcAnimOffsets();
                float ox = offsets[0], oy = offsets[1], scale = offsets[2];
                float tint = offsets[3]; // 0-1, red tint for hurt

                // Apply alpha for die
                g2.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, alpha));

                // Apply scale + translate
                AffineTransform old = g2.getTransform();
                g2.translate(cx + ox, cy + oy);
                g2.scale(scale, scale);
                g2.translate(-cx, -cy);

                // Red tint overlay composite
                drawMonsterByName(g2, cx, cy, enemy.getName().toLowerCase());

                g2.setTransform(old);

                // Hurt red flash overlay
                if (tint > 0) {
                    g2.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, tint * 0.45f));
                    g2.setColor(new Color(255, 60, 60));
                    g2.fillRect(0, 0, w, h);
                }

                // Attack flash overlay (white)
                if (animState == AnimState.ATTACK && animFrame < 4) {
                    g2.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 0.18f));
                    g2.setColor(Color.WHITE);
                    g2.fillRect(0, 0, w, h);
                }

                g2.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 1f));
            }
        };
        artPanel.setBackground(new Color(16, 14, 28));
        artPanel.setOpaque(true);

        // Status Panel - แสดง status effects
        statusPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 5, 0));
        statusPanel.setBackground(new Color(30, 25, 45));
        statusPanel.setBorder(BorderFactory.createEmptyBorder(8, 0, 8, 0));

        // Add panels to layout
        add(artPanel, BorderLayout.CENTER);
        add(statusPanel, BorderLayout.SOUTH);

        Timer timer = new Timer(50, e -> {
            animTick++;
            animFrame++;
            // Auto-return to IDLE after attack/hurt animations finish
            if (animState == AnimState.ATTACK && animFrame > 14) setAnimState(AnimState.IDLE);
            if (animState == AnimState.HURT    && animFrame > 10) setAnimState(AnimState.IDLE);
            if (animState == AnimState.DIE) {
                alpha = Math.max(0f, alpha - 0.04f);
            }
            artPanel.repaint();
        });
        timer.start();
    }

    // ── Public API ────────────────────────────────────────────────────────────
    public void setEnemy(Enemy enemy) {
        this.enemy = enemy;
        animState = AnimState.IDLE;
        animFrame = 0;
        alpha     = 1f;
        refreshStatus();
        artPanel.repaint();
    }

    /** เรียกตอนศัตรูโจมตี */
    public void playAttackAnim()  { setAnimState(AnimState.ATTACK); }
    /** เรียกตอนศัตรูโดนดาเมจ */
    public void playHurtAnim()    { setAnimState(AnimState.HURT); }
    /** เรียกตอนศัตรูตาย */
    public void playDieAnim()     { setAnimState(AnimState.DIE); }

    private void setAnimState(AnimState s) { animState = s; animFrame = 0; }

    /**
     * อัพเดทการแสดง status effects ทั้งหมด
     * FIXED: ใช้ method เดียวที่ครอบคลุมทุก status
     */
    public void refreshStatus() {
        statusPanel.removeAll();

        if (enemy == null) {
            statusPanel.revalidate();
            statusPanel.repaint();
            return;
        }

        // แสดง Poison (☠)
        if (enemy.getPoison() > 0) {
            statusPanel.add(statusBadge("☠ " + enemy.getPoison(), new Color(50, 160, 60), DESC_POISON));
        }

        // แสดง Weak (↓)
        if (enemy.getWeak() > 0) {
            statusPanel.add(statusBadge("↓ " + enemy.getWeak(), new Color(120, 50, 180), DESC_WEAK));
        }

        // แสดง Strength (↑)
        if (enemy.getStrength() > 0) {
            statusPanel.add(statusBadge("↑ " + enemy.getStrength(), new Color(180, 100, 20), DESC_STRENGTH));
        }

        // แสดง Vulnerable (🎯) - ตรวจสอบว่า Enemy มี method นี้หรือไม่
        try {
            int vulnerable = enemy.getVulnerable();
            if (vulnerable > 0) {
                statusPanel.add(statusBadge("🎯 " + vulnerable, new Color(220, 60, 100), DESC_VULNERABLE));
            }
        } catch (Exception e) {
            // Enemy class อาจไม่มี getVulnerable() - ข้ามไป
        }

        // แสดง Block (🛡) - สำหรับ ImprovedEnemy
        if (enemy instanceof entity.ImprovedEnemy) {
            entity.ImprovedEnemy improvedEnemy = (entity.ImprovedEnemy) enemy;
            if (improvedEnemy.getBlock() > 0) {
                statusPanel.add(statusBadge("🛡 " + improvedEnemy.getBlock(), new Color(80, 140, 210), DESC_BLOCK));
            }
        }

        statusPanel.revalidate();
        statusPanel.repaint();
    }

    // ── Drawing Methods ───────────────────────────────────────────────────────

    private float[] calcAnimOffsets() {
        float ox = 0, oy = 0, scale = 1f, tint = 0f;
        switch (animState) {
            case IDLE:
                float breathe = (float) Math.sin(animTick * 0.08) * 2f;
                float bob     = (float) Math.sin(animTick * 0.06) * 3f;
                oy = breathe + bob;
                break;
            case ATTACK:
                if (animFrame < 5) {
                    ox = (animFrame / 5f) * -30f;
                } else if (animFrame < 10) {
                    ox = ((10 - animFrame) / 5f) * -30f;
                }
                break;
            case HURT:
                int shake = (animFrame % 2 == 0) ? 4 : -4;
                ox = shake;
                tint = 1f - (animFrame / 10f);
                break;
            case DIE:
                scale = Math.max(0.3f, 1f - (animFrame * 0.03f));
                oy = animFrame * 2f;
                break;
        }
        return new float[]{ox, oy, scale, tint};
    }

    private void drawAtmosphere(Graphics2D g, int w, int h) {
        GradientPaint gp = new GradientPaint(0, 0, new Color(24, 20, 40, 180),
                0, h, new Color(16, 14, 28, 255));
        g.setPaint(gp);
        g.fillRect(0, 0, w, h);
    }

    private void drawHpBar(Graphics2D g, int x, int y, int w, int h) {
        if (enemy == null) return;
        float hpRatio = Math.max(0, (float) enemy.getHp() / enemy.getMaxHp());

        // Background
        g.setColor(new Color(50, 40, 60));
        g.fillRoundRect(x, y, w, h, 6, 6);

        // HP fill (gradient: green → yellow → red)
        Color hpColor;
        if (hpRatio > 0.6f) hpColor = new Color(60, 200, 80);
        else if (hpRatio > 0.3f) hpColor = new Color(220, 200, 60);
        else hpColor = new Color(220, 60, 60);

        int fillW = (int) (w * hpRatio);
        g.setColor(hpColor);
        g.fillRoundRect(x, y, fillW, h, 6, 6);

        // Border
        g.setColor(new Color(100, 90, 120));
        g.setStroke(new BasicStroke(1.5f));
        g.drawRoundRect(x, y, w, h, 6, 6);

        // HP text
        g.setColor(Color.WHITE);
        g.setFont(new Font("SansSerif", Font.BOLD, 11));
        String hpText = enemy.getHp() + "/" + enemy.getMaxHp();
        FontMetrics fm = g.getFontMetrics();
        g.drawString(hpText, x + (w - fm.stringWidth(hpText)) / 2, y + h - 3);
    }

    private void drawIntentBox(Graphics2D g, int x, int y, int w) {
        if (enemy == null || enemy.getIntent() == null) return;

        String intent = enemy.getIntent();

        // Background
        Color intentBg;
        if (intent.contains("[ATK]")) intentBg = new Color(180, 60, 60, 200);
        else if (intent.contains("[DEF]")) intentBg = new Color(60, 140, 200, 200);
        else if (intent.contains("[BUF]")) intentBg = new Color(200, 140, 60, 200);
        else if (intent.contains("[DEB]")) intentBg = new Color(140, 60, 200, 200);
        else if (intent.contains("[SPE]")) intentBg = new Color(220, 100, 200, 200);
        else intentBg = new Color(100, 90, 120, 200);

        g.setColor(intentBg);
        g.fillRoundRect(x, y, w, 32, 8, 8);

        // Border
        g.setColor(intentBg.brighter());
        g.setStroke(new BasicStroke(1.5f));
        g.drawRoundRect(x, y, w, 32, 8, 8);

        // Intent text
        g.setColor(Color.WHITE);
        g.setFont(new Font("SansSerif", Font.BOLD, 12));
        FontMetrics fm = g.getFontMetrics();

        // Word wrap if needed
        String[] words = intent.split(" ");
        StringBuilder line1 = new StringBuilder();
        StringBuilder line2 = new StringBuilder();
        boolean secondLine = false;

        for (String word : words) {
            if (!secondLine && fm.stringWidth(line1 + " " + word) < w - 10) {
                if (line1.length() > 0) line1.append(" ");
                line1.append(word);
            } else {
                secondLine = true;
                if (line2.length() > 0) line2.append(" ");
                line2.append(word);
            }
        }

        if (line2.length() == 0) {
            // Single line
            g.drawString(line1.toString(), x + (w - fm.stringWidth(line1.toString())) / 2, y + 20);
        } else {
            // Two lines
            g.drawString(line1.toString(), x + (w - fm.stringWidth(line1.toString())) / 2, y + 14);
            g.drawString(line2.toString(), x + (w - fm.stringWidth(line2.toString())) / 2, y + 26);
        }
    }

    private void drawMonsterByName(Graphics2D g, int cx, int cy, String name) {
        // Match name pattern และเรียก draw function ที่เหมาะสม
        if (name.contains("slime")) {
            drawSlime(g, cx, cy);
        } else if (name.contains("goblin")) {
            drawGoblin(g, cx, cy);
        } else if (name.contains("skeleton")) {
            drawSkeleton(g, cx, cy);
        } else if (name.contains("bat")) {
            drawBat(g, cx, cy);
        } else if (name.contains("ghost") || name.contains("phantom")) {
            drawGhost(g, cx, cy);
        } else if (name.contains("cultist")) {
            drawCultist(g, cx, cy);
        } else if (name.contains("mushroom")) {
            drawMushroom(g, cx, cy);
        } else {
            // Default enemy
            drawDefaultEnemy(g, cx, cy);
        }
    }

    // ── Monster Drawing Functions ────────────────────────────────────────────

    private void drawSlime(Graphics2D g, int cx, int cy) {
        float squish = (float) Math.sin(animTick * 0.1) * 0.08f + 1f;
        Color slimeColor = new Color(100, 200, 120, 220);
        Color slimeDark = new Color(60, 140, 80);

        // Shadow
        g.setColor(new Color(0, 0, 0, 50));
        g.fillOval(cx - 40, cy + 40, 80, 20);

        // Body (squishing ellipse)
        g.setColor(slimeColor);
        int w = (int) (70 * squish);
        int h = (int) (60 / squish);
        g.fillOval(cx - w/2, cy - h/2, w, h);

        // Shine
        g.setColor(new Color(200, 255, 220, 150));
        g.fillOval(cx - 15, cy - 20, 20, 15);

        // Eyes
        g.setColor(Color.BLACK);
        g.fillOval(cx - 15, cy - 8, 10, 12);
        g.fillOval(cx + 5, cy - 8, 10, 12);

        // Eye shine
        g.setColor(Color.WHITE);
        g.fillOval(cx - 12, cy - 6, 4, 4);
        g.fillOval(cx + 8, cy - 6, 4, 4);

        // Outline
        g.setColor(slimeDark);
        g.setStroke(new BasicStroke(2.5f));
        g.drawOval(cx - w/2, cy - h/2, w, h);
    }

    private void drawGoblin(Graphics2D g, int cx, int cy) {
        Color skin = new Color(100, 140, 80);
        Color dark = new Color(60, 80, 50);

        // Body
        g.setColor(new Color(100, 60, 40));
        g.fillRoundRect(cx - 20, cy - 10, 40, 35, 10, 10);

        // Arms
        g.setColor(skin);
        g.setStroke(new BasicStroke(8f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
        g.drawLine(cx - 20, cy, cx - 35, cy + 20);
        g.drawLine(cx + 20, cy, cx + 35, cy + 20);

        // Legs
        g.setStroke(new BasicStroke(9f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
        g.drawLine(cx - 8, cy + 25, cx - 10, cy + 50);
        g.drawLine(cx + 8, cy + 25, cx + 10, cy + 50);

        // Head
        g.fillOval(cx - 22, cy - 50, 44, 42);

        // Ears
        int[] earL = {cx - 22, cx - 32, cx - 18};
        int[] earLy = {cy - 40, cy - 44, cy - 30};
        int[] earR = {cx + 22, cx + 18, cx + 32};
        int[] earRy = {cy - 40, cy - 30, cy - 44};
        g.fillPolygon(earL, earLy, 3);
        g.fillPolygon(earR, earRy, 3);

        // Eyes
        g.setColor(Color.YELLOW);
        g.fillOval(cx - 12, cy - 38, 10, 12);
        g.fillOval(cx + 2, cy - 38, 10, 12);
        g.setColor(Color.BLACK);
        g.fillOval(cx - 9, cy - 34, 4, 6);
        g.fillOval(cx + 5, cy - 34, 4, 6);

        // Nose
        g.setColor(dark);
        g.fillOval(cx - 3, cy - 26, 6, 8);

        // Mouth (grin)
        g.setColor(Color.BLACK);
        g.setStroke(new BasicStroke(2f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
        g.drawArc(cx - 10, cy - 18, 20, 12, 200, 140);
    }

    private void drawSkeleton(Graphics2D g, int cx, int cy) {
        Color bone = new Color(240, 235, 215);
        Color dark = new Color(180, 175, 160);

        // Ribcage
        g.setColor(bone);
        g.fillRoundRect(cx - 24, cy - 15, 48, 38, 10, 10);

        // Ribs
        g.setColor(dark);
        g.setStroke(new BasicStroke(2f));
        for (int i = 0; i < 4; i++) {
            int ry = cy - 8 + (i * 10);
            g.drawArc(cx - 18, ry, 36, 8, 0, 180);
        }

        // Arms
        g.setColor(bone);
        g.setStroke(new BasicStroke(6f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
        g.drawLine(cx - 24, cy - 5, cx - 38, cy + 18);
        g.drawLine(cx + 24, cy - 5, cx + 38, cy + 18);

        // Legs
        g.setStroke(new BasicStroke(7f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
        g.drawLine(cx - 10, cy + 23, cx - 12, cy + 50);
        g.drawLine(cx + 10, cy + 23, cx + 12, cy + 50);

        // Skull
        g.setColor(bone);
        g.fillRoundRect(cx - 22, cy - 58, 44, 46, 10, 10);

        // Eye sockets
        g.setColor(Color.BLACK);
        g.fillOval(cx - 14, cy - 48, 12, 16);
        g.fillOval(cx + 2, cy - 48, 12, 16);

        // Eyes (glowing)
        g.setColor(new Color(100, 200, 255, 200));
        g.fillOval(cx - 11, cy - 44, 6, 8);
        g.fillOval(cx + 5, cy - 44, 6, 8);

        // Nose hole
        g.setColor(Color.BLACK);
        int[] nose = {cx - 2, cx + 2, cx};
        int[] noseY = {cy - 34, cy - 34, cy - 28};
        g.fillPolygon(nose, noseY, 3);

        // Teeth
        g.setColor(bone);
        for (int i = 0; i < 6; i++) {
            g.fillRect(cx - 12 + (i * 4), cy - 22, 3, 6);
        }

        // Outline
        g.setColor(dark);
        g.setStroke(new BasicStroke(2f));
        g.drawRoundRect(cx - 22, cy - 58, 44, 46, 10, 10);
        g.drawRoundRect(cx - 24, cy - 15, 48, 38, 10, 10);
    }

    private void drawBat(Graphics2D g, int cx, int cy) {
        float wingFlap = (float) Math.sin(animTick * 0.2) * 15f;
        Color batColor = new Color(60, 50, 70);
        Color wingColor = new Color(80, 70, 100);

        // Wings
        g.setColor(wingColor);

        // Left wing
        int[] wingLx = {cx - 10, cx - 40, cx - 50, cx - 30};
        int[] wingLy = {(int)(cy - wingFlap), (int)(cy - 20 - wingFlap), (int)(cy + 10 - wingFlap), (int)(cy + 10)};
        g.fillPolygon(wingLx, wingLy, 4);

        // Right wing
        int[] wingRx = {cx + 10, cx + 30, cx + 50, cx + 40};
        int[] wingRy = {(int)(cy + 10), (int)(cy + 10 - wingFlap), (int)(cy - 20 - wingFlap), (int)(cy - wingFlap)};
        g.fillPolygon(wingRx, wingRy, 4);

        // Body
        g.setColor(batColor);
        g.fillOval(cx - 12, cy - 8, 24, 28);

        // Head
        g.fillOval(cx - 14, cy - 30, 28, 24);

        // Ears
        int[] earL = {cx - 10, cx - 16, cx - 6};
        int[] earLy = {cy - 30, cy - 46, cy - 36};
        int[] earR = {cx + 10, cx + 6, cx + 16};
        int[] earRy = {cy - 30, cy - 36, cy - 46};
        g.fillPolygon(earL, earLy, 3);
        g.fillPolygon(earR, earRy, 3);

        // Eyes (red)
        g.setColor(new Color(220, 60, 60));
        g.fillOval(cx - 8, cy - 22, 6, 6);
        g.fillOval(cx + 2, cy - 22, 6, 6);

        // Fangs
        g.setColor(Color.WHITE);
        int[] fangL = {cx - 6, cx - 4, cx - 5};
        int[] fangLy = {cy - 10, cy - 10, cy - 4};
        int[] fangR = {cx + 4, cx + 5, cx + 6};
        int[] fangRy = {cy - 10, cy - 4, cy - 10};
        g.fillPolygon(fangL, fangLy, 3);
        g.fillPolygon(fangR, fangRy, 3);
    }

    private void drawGhost(Graphics2D g, int cx, int cy) {
        float float_y = (float) Math.sin(animTick * 0.08) * 6f;

        // Body (wispy)
        g.setColor(new Color(200, 200, 240, 180));
        g.fillOval(cx - 30, (int)(cy - 40 + float_y), 60, 70);

        // Wispy bottom
        int[] wispX = {cx - 30, cx - 20, cx - 10, cx, cx + 10, cx + 20, cx + 30};
        int[] wispY = {
                (int)(cy + 20 + float_y),
                (int)(cy + 30 + float_y + Math.sin(animTick * 0.1) * 3),
                (int)(cy + 26 + float_y),
                (int)(cy + 32 + float_y + Math.sin(animTick * 0.12) * 4),
                (int)(cy + 28 + float_y),
                (int)(cy + 34 + float_y + Math.sin(animTick * 0.09) * 3),
                (int)(cy + 24 + float_y)
        };

        for (int i = 0; i < wispX.length - 1; i++) {
            QuadCurve2D curve = new QuadCurve2D.Float(
                    wispX[i], wispY[i],
                    (wispX[i] + wispX[i+1]) / 2, wispY[i] + 5,
                    wispX[i+1], wispY[i+1]
            );
            g.fill(curve);
        }

        // Eyes (dark)
        g.setColor(new Color(40, 40, 80));
        g.fillOval(cx - 14, (int)(cy - 24 + float_y), 12, 18);
        g.fillOval(cx + 2, (int)(cy - 24 + float_y), 12, 18);

        // Eye glow
        g.setColor(new Color(140, 140, 255, 150));
        g.fillOval(cx - 10, (int)(cy - 20 + float_y), 4, 8);
        g.fillOval(cx + 6, (int)(cy - 20 + float_y), 4, 8);

        // Mouth (wavy)
        g.setColor(new Color(40, 40, 80, 180));
        g.setStroke(new BasicStroke(2.5f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
        g.drawArc(cx - 8, (int)(cy - 8 + float_y), 16, 10, 200, 140);
    }

    private void drawCultist(Graphics2D g, int cx, int cy) {
        Color robe = new Color(60, 40, 80);
        Color skin = new Color(200, 180, 150);

        // Robe
        int[] robeX = {cx - 28, cx + 28, cx + 24, cx - 24};
        int[] robeY = {cy - 20, cy - 20, cy + 50, cy + 50};
        g.setColor(robe);
        g.fillPolygon(robeX, robeY, 4);

        // Hood
        g.fillArc(cx - 30, cy - 60, 60, 50, 0, 180);

        // Arms
        g.setStroke(new BasicStroke(10f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
        g.drawLine(cx - 24, cy, cx - 40, cy + 25);
        g.drawLine(cx + 24, cy, cx + 40, cy + 25);

        // Hands
        g.setColor(skin);
        g.fillOval(cx - 46, cy + 20, 14, 14);
        g.fillOval(cx + 32, cy + 20, 14, 14);

        // Face (shadowed)
        g.setColor(new Color(40, 30, 50));
        g.fillOval(cx - 18, cy - 45, 36, 40);

        // Eyes (glowing)
        g.setColor(new Color(200, 60, 60));
        g.fillOval(cx - 10, cy - 38, 8, 10);
        g.fillOval(cx + 2, cy - 38, 8, 10);

        // Mystic symbol on chest
        g.setColor(new Color(200, 100, 220));
        g.setStroke(new BasicStroke(2f));
        g.drawLine(cx, cy + 5, cx, cy + 20);
        g.drawOval(cx - 8, cy + 8, 16, 16);
    }

    private void drawMushroom(Graphics2D g, int cx, int cy) {
        Color capColor = new Color(180, 80, 140);
        Color stemColor = new Color(220, 210, 190);
        Color spotColor = new Color(240, 200, 220);

        // Stem
        g.setColor(stemColor);
        g.fillRoundRect(cx - 16, cy - 10, 32, 44, 8, 8);

        // Gills under cap
        g.setColor(new Color(160, 140, 150));
        g.fillOval(cx - 34, cy - 16, 68, 20);

        // Cap
        g.setColor(capColor);
        g.fillArc(cx - 38, cy - 52, 76, 48, 0, 180);

        // Spots
        g.setColor(spotColor);
        g.fillOval(cx - 24, cy - 44, 12, 12);
        g.fillOval(cx - 6, cy - 50, 16, 16);
        g.fillOval(cx + 14, cy - 42, 10, 10);
        g.fillOval(cx - 14, cy - 32, 8, 8);
        g.fillOval(cx + 8, cy - 30, 10, 10);

        // Face
        g.setColor(new Color(140, 60, 100));
        g.fillOval(cx - 10, cy + 4, 8, 10);
        g.fillOval(cx + 2, cy + 4, 8, 10);

        // Smile
        g.setStroke(new BasicStroke(2f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
        g.drawArc(cx - 8, cy + 14, 16, 10, 200, 140);

        // Spores floating
        g.setColor(new Color(140, 220, 160, 150));
        for (int i = 0; i < 5; i++) {
            int sx = cx - 30 + (i * 15) + (int)(Math.sin(animTick * 0.08 + i) * 4);
            int sy = cy - 60 + (int)(Math.sin(animTick * 0.1 + i * 0.5) * 8);
            g.fillOval(sx, sy, 4, 4);
        }
    }

    private void drawDefaultEnemy(Graphics2D g, int cx, int cy) {
        // Generic humanoid
        g.setColor(new Color(140, 90, 55));
        g.fillOval(cx - 18, cy - 52, 36, 32);
        g.fillRoundRect(cx - 20, cy - 22, 40, 40, 8, 8);
        g.setColor(new Color(110, 70, 40));
        g.setStroke(new BasicStroke(8f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
        g.drawLine(cx - 20, cy - 10, cx - 36, cy + 14);
        g.drawLine(cx + 20, cy - 10, cx + 36, cy + 14);
        g.setStroke(new BasicStroke(9f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
        g.drawLine(cx - 10, cy + 18, cx - 12, cy + 44);
        g.drawLine(cx + 10, cy + 18, cx + 12, cy + 44);
        g.setColor(Color.WHITE);
        g.fillOval(cx - 9, cy - 44, 8, 8);
        g.fillOval(cx + 1, cy - 44, 8, 8);
        g.setColor(Color.BLACK);
        g.fillOval(cx - 7, cy - 42, 4, 4);
        g.fillOval(cx + 3, cy - 42, 4, 4);
        g.setColor(new Color(110, 70, 40));
        g.setStroke(new BasicStroke(2f));
        g.drawOval(cx - 18, cy - 52, 36, 32);
        g.drawRoundRect(cx - 20, cy - 22, 40, 40, 8, 8);
        g.setStroke(new BasicStroke(1));
    }

    // ── Status Badge ──────────────────────────────────────────────────────────
    private JButton statusBadge(String label, Color bg, String tooltip) {
        JButton btn = new JButton(label) {
            @Override protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g;
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(new Color(bg.getRed()/3, bg.getGreen()/3, bg.getBlue()/3));
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), 8, 8);
                g2.setColor(bg);
                g2.setStroke(new BasicStroke(1.2f));
                g2.drawRoundRect(1, 1, getWidth()-2, getHeight()-2, 8, 8);
                g2.setFont(getFont());
                g2.setColor(Color.WHITE);
                FontMetrics fm = g2.getFontMetrics();
                g2.drawString(getText(), (getWidth()-fm.stringWidth(getText()))/2,
                        (getHeight()+fm.getAscent()-fm.getDescent())/2);
            }
            @Override protected void paintBorder(Graphics g) {}
        };
        btn.setFont(new Font("SansSerif", Font.BOLD, 11));
        btn.setContentAreaFilled(false);
        btn.setFocusPainted(false);
        btn.setPreferredSize(new Dimension(72, 24));
        btn.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        btn.addActionListener(e -> JOptionPane.showMessageDialog(this, tooltip,
                "Status Effect", JOptionPane.INFORMATION_MESSAGE));
        return btn;
    }
}