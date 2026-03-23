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
 */
public class EnemyPanel extends JPanel {

    // ── Animation States ──────────────────────────────────────────────────────
    public enum AnimState { IDLE, ATTACK, HURT, DIE }

    private Enemy enemy;
    private int        animTick  = 0;
    private AnimState  animState = AnimState.IDLE;
    private int        animFrame = 0;   // frame counter ภายใน state ปัจจุบัน
    private float      alpha     = 1f;  // สำหรับ die fade
    private JPanel     statusPanel;
    private JPanel     artPanel;

    private static final String DESC_POISON   = "<html><b>[PSN] Poison</b><br>Deals damage equal to stack<br>at start of enemy turn.</html>";
    private static final String DESC_WEAK     = "<html><b>[WEK] Weak</b><br>Reduces attack damage by 25%.</html>";
    private static final String DESC_STRENGTH = "<html><b>[STR] Strength</b><br>Increases attack damage.</html>";

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

        statusPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 5, 3));
        statusPanel.setBackground(new Color(16, 14, 28));
        statusPanel.setPreferredSize(new Dimension(320, 34));

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

    public void refreshStatus() {
        statusPanel.removeAll();
        if (enemy == null) { statusPanel.revalidate(); statusPanel.repaint(); return; }
        if (enemy.getPoison()   > 0) statusPanel.add(statusBadge("☠ " + enemy.getPoison(),   new Color(50,160,60),  DESC_POISON));
        if (enemy.getWeak()     > 0) statusPanel.add(statusBadge("↓ " + enemy.getWeak(),     new Color(120,50,180), DESC_WEAK));
        if (enemy.getStrength() > 0) statusPanel.add(statusBadge("↑ " + enemy.getStrength(), new Color(180,100,20), DESC_STRENGTH));
        statusPanel.revalidate();
        statusPanel.repaint();
    }

    // ── Animation Offset Calculator ───────────────────────────────────────────
    private float[] calcAnimOffsets() {
        float ox = 0, oy = 0, scale = 1f, tint = 0f;
        switch (animState) {
            case IDLE:
                // bob ขึ้นลง + breathe scale
                oy    = (float)(Math.sin(animTick * 0.12) * 4.0);
                scale = (float)(1.0 + Math.sin(animTick * 0.08) * 0.012);
                break;
            case ATTACK:
                // lunge ไปซ้าย (หาผู้เล่น) แล้วกลับ
                if (animFrame < 5)       ox = -animFrame * 8f;
                else if (animFrame < 10) ox = -(10 - animFrame) * 8f;
                scale = animFrame < 5 ? (1f + animFrame * 0.02f) : 1f;
                break;
            case HURT:
                // shake ซ้ายขวา
                ox   = (float)(Math.sin(animFrame * 1.8) * (10 - animFrame));
                tint = Math.max(0f, 1f - animFrame * 0.1f);
                scale = 1f - animFrame * 0.005f;
                break;
            case DIE:
                // ยุบลงและจาง
                oy    = animFrame * 1.5f;
                scale = Math.max(0.3f, 1f - animFrame * 0.03f);
                tint  = Math.min(1f, animFrame * 0.08f);
                break;
        }
        return new float[]{ox, oy, scale, tint};
    }

    // ── Atmosphere Background ─────────────────────────────────────────────────
    private void drawAtmosphere(Graphics2D g2, int w, int h) {
        // subtle radial glow in center
        Color mid = getEnemyGlowColor();
        RadialGradientPaint rg = new RadialGradientPaint(
                new Point2D.Float(w/2f, h/2f + 30), w * 0.45f,
                new float[]{0f, 1f},
                new Color[]{new Color(mid.getRed(), mid.getGreen(), mid.getBlue(), 35),
                        new Color(mid.getRed(), mid.getGreen(), mid.getBlue(), 0)});
        g2.setPaint(rg);
        g2.fillRect(0, 0, w, h);
        g2.setPaint(null);
    }

    private Color getEnemyGlowColor() {
        if (enemy == null) return new Color(80,80,120);
        String n = enemy.getName().toLowerCase();
        if (n.contains("berserker") || n.contains("rage")) return new Color(200, 60, 40);
        if (n.contains("guardian"))  return new Color(80, 120, 200);
        if (n.contains("ghost"))     return new Color(140, 160, 255);
        if (n.contains("slime"))     return new Color(60, 200, 100);
        if (n.contains("skeleton"))  return new Color(200, 200, 180);
        return new Color(100, 80, 140);
    }

    // ── HP Bar ────────────────────────────────────────────────────────────────
    private void drawHpBar(Graphics2D g, int x, int y, int w, int h) {
        g.setColor(new Color(50, 15, 15));
        g.fillRoundRect(x, y, w, h, 8, 8);
        double pct = (double) enemy.getHp() / enemy.getMaxHp();
        Color bar = pct > 0.5 ? new Color(60, 200, 70)
                : pct > 0.25 ? new Color(220, 175, 30)
                : new Color(210, 45, 45);
        g.setColor(bar);
        g.fillRoundRect(x, y, (int)(w * pct), h, 8, 8);
        // shine
        g.setColor(new Color(255, 255, 255, 40));
        g.fillRoundRect(x, y, (int)(w * pct), h / 2, 8, 8);
        g.setColor(new Color(120, 120, 140));
        g.setStroke(new BasicStroke(1f));
        g.drawRoundRect(x, y, w, h, 8, 8);
        g.setFont(new Font("SansSerif", Font.BOLD, 10));
        g.setColor(Color.WHITE);
        String txt = enemy.getHp() + " / " + enemy.getMaxHp();
        FontMetrics fm = g.getFontMetrics();
        g.drawString(txt, x + (w - fm.stringWidth(txt)) / 2, y + h - 2);
    }

    // ── Intent Box ────────────────────────────────────────────────────────────
    private void drawIntentBox(Graphics2D g, int x, int y, int w) {
        String intent = enemy.getIntent();
        // pick color by intent type
        Color intentColor = intent.contains("[ATK]") || intent.contains("RAGE") || intent.contains("SLAM")
                ? new Color(220, 80, 60)
                : intent.contains("[DEF]") || intent.contains("Shield")
                ? new Color(80, 130, 210)
                : intent.contains("[BUFF]") || intent.contains("[REP]")
                ? new Color(180, 130, 30)
                : intent.contains("[FRZ]")
                ? new Color(80, 180, 220)
                : new Color(140, 100, 200);

        g.setColor(new Color(intentColor.getRed()/5, intentColor.getGreen()/5, intentColor.getBlue()/5, 200));
        g.fillRoundRect(x, y, w, 34, 10, 10);
        g.setColor(new Color(intentColor.getRed(), intentColor.getGreen(), intentColor.getBlue(), 160));
        g.setStroke(new BasicStroke(1.2f));
        g.drawRoundRect(x, y, w, 34, 10, 10);

        g.setFont(new Font("SansSerif", Font.BOLD, 11));
        g.setColor(intentColor.brighter());
        FontMetrics fm = g.getFontMetrics();
        if (fm.stringWidth(intent) > w - 16) {
            int split = intent.lastIndexOf(' ', intent.length() / 2 + 10);
            if (split < 0) split = intent.length() / 2;
            g.drawString(intent.substring(0, split),     x + 8, y + 13);
            g.drawString(intent.substring(split + 1), x + 8, y + 27);
        } else {
            g.drawString(intent, x + (w - fm.stringWidth(intent)) / 2, y + 21);
        }
    }

    // ── Monster Router ────────────────────────────────────────────────────────
    private void drawMonsterByName(Graphics2D g, int cx, int cy, String name) {
        if      (name.contains("berserker") || name.contains("rage")) drawRageBoss(g, cx, cy);
        else if (name.contains("guardian"))  drawGuardianBoss(g, cx, cy);
        else if (name.contains("witch"))     drawCurseWitch(g, cx, cy);
        else if (name.contains("golem"))     drawStoneGolem(g, cx, cy);
        else if (name.contains("slime"))     drawSlime(g, cx, cy);
        else if (name.contains("goblin"))    drawGoblin(g, cx, cy);
        else if (name.contains("skeleton"))  drawSkeleton(g, cx, cy);
        else if (name.contains("bat"))       drawBat(g, cx, cy);
        else if (name.contains("ghost"))     drawGhost(g, cx, cy);
        else                                 drawDefaultEnemy(g, cx, cy);
    }

    // ── Monster Drawings (Vector) ─────────────────────────────────────────────

    private void drawSlime(Graphics2D g, int cx, int cy) {
        float bob = (float)(Math.sin(animTick * 0.15) * 3);
        // Body — ellipse squash/stretch with bob
        float sx = (float)(1.0 + Math.sin(animTick * 0.15) * 0.05);
        float sy = (float)(1.0 - Math.sin(animTick * 0.15) * 0.05);
        g.setColor(new Color(40, 190, 90));
        g.fill(new Ellipse2D.Float(cx - 38*sx, cy - 28*sy + bob, 76*sx, 56*sy));
        // Highlight
        g.setColor(new Color(120, 240, 150, 120));
        g.fill(new Ellipse2D.Float(cx - 22*sx, cy - 22*sy + bob, 28*sx, 16*sy));
        // Outline
        g.setColor(new Color(20, 130, 60));
        g.setStroke(new BasicStroke(2.5f));
        g.draw(new Ellipse2D.Float(cx - 38*sx, cy - 28*sy + bob, 76*sx, 56*sy));
        // Eyes
        g.setColor(Color.WHITE);
        g.fillOval(cx - 16, cy - 12 + (int)bob, 12, 12);
        g.fillOval(cx + 4,  cy - 12 + (int)bob, 12, 12);
        g.setColor(new Color(20, 80, 20));
        g.fillOval(cx - 13, cy - 9 + (int)bob, 7, 7);
        g.fillOval(cx + 7,  cy - 9 + (int)bob, 7, 7);
        // Pupils
        g.setColor(Color.BLACK);
        g.fillOval(cx - 11, cy - 7 + (int)bob, 4, 4);
        g.fillOval(cx + 9,  cy - 7 + (int)bob, 4, 4);
        // Mouth
        g.setColor(new Color(20, 100, 50));
        g.setStroke(new BasicStroke(2f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
        g.drawArc(cx - 10, cy + (int)bob, 20, 10, 200, 140);
        g.setStroke(new BasicStroke(1));
    }

    private void drawGoblin(Graphics2D g, int cx, int cy) {
        // Body
        g.setColor(new Color(90, 170, 70));
        g.fillRoundRect(cx - 20, cy - 20, 40, 42, 10, 10);
        // Head
        g.setColor(new Color(110, 190, 85));
        g.fillOval(cx - 18, cy - 46, 36, 32);
        // Ears
        g.setColor(new Color(110, 190, 85));
        int[][] earL = {{cx-26, cy-40},{cx-16, cy-34},{cx-20, cy-28}};
        int[][] earR = {{cx+26, cy-40},{cx+16, cy-34},{cx+20, cy-28}};
        g.fillPolygon(new int[]{earL[0][0],earL[1][0],earL[2][0]}, new int[]{earL[0][1],earL[1][1],earL[2][1]}, 3);
        g.fillPolygon(new int[]{earR[0][0],earR[1][0],earR[2][0]}, new int[]{earR[0][1],earR[1][1],earR[2][1]}, 3);
        // Eyes
        g.setColor(new Color(220, 50, 50));
        g.fillOval(cx - 10, cy - 38, 9, 9);
        g.fillOval(cx + 1,  cy - 38, 9, 9);
        g.setColor(Color.BLACK);
        g.fillOval(cx - 8, cy - 36, 5, 5);
        g.fillOval(cx + 3, cy - 36, 5, 5);
        // Nose
        g.setColor(new Color(80, 150, 55));
        g.fillOval(cx - 4, cy - 28, 8, 6);
        // Mouth / tusks
        g.setColor(Color.WHITE);
        g.fillRect(cx - 8, cy - 22, 5, 8);
        g.fillRect(cx + 3, cy - 22, 5, 8);
        // Arms
        g.setColor(new Color(90, 170, 70));
        g.setStroke(new BasicStroke(8f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
        g.drawLine(cx - 20, cy - 12, cx - 36, cy + 8);
        g.drawLine(cx + 20, cy - 12, cx + 36, cy + 8);
        // Legs
        g.setStroke(new BasicStroke(9f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
        g.drawLine(cx - 10, cy + 22, cx - 14, cy + 44);
        g.drawLine(cx + 10, cy + 22, cx + 14, cy + 44);
        // Weapon (club)
        g.setColor(new Color(120, 80, 40));
        g.setStroke(new BasicStroke(5f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
        g.drawLine(cx + 32, cy + 4, cx + 44, cy - 16);
        g.setColor(new Color(100, 65, 30));
        g.fillOval(cx + 38, cy - 24, 16, 16);
        g.setStroke(new BasicStroke(1));
        // Outline
        g.setColor(new Color(50, 110, 40));
        g.setStroke(new BasicStroke(1.8f));
        g.drawRoundRect(cx - 20, cy - 20, 40, 42, 10, 10);
        g.drawOval(cx - 18, cy - 46, 36, 32);
        g.setStroke(new BasicStroke(1));
    }

    private void drawSkeleton(Graphics2D g, int cx, int cy) {
        Color bone = new Color(225, 220, 200);
        Color shadow = new Color(160, 155, 140);
        // Ribcage
        g.setColor(bone);
        g.fillRoundRect(cx - 18, cy - 16, 36, 32, 6, 6);
        g.setColor(shadow);
        for (int i = 0; i < 4; i++)
            g.drawLine(cx - 14, cy - 10 + i*8, cx + 14, cy - 10 + i*8);
        // Skull
        g.setColor(bone);
        g.fillOval(cx - 16, cy - 50, 32, 30);
        // Jaw
        g.fillRoundRect(cx - 12, cy - 24, 24, 10, 4, 4);
        // Eyes (dark hollow)
        g.setColor(new Color(20, 18, 30));
        g.fillOval(cx - 10, cy - 44, 9, 10);
        g.fillOval(cx + 1,  cy - 44, 9, 10);
        // Nose
        g.fillOval(cx - 3, cy - 35, 6, 5);
        // Teeth
        g.setColor(bone);
        for (int i = -2; i <= 2; i++)
            g.fillRect(cx + i*4 - 1, cy - 24, 3, 6);
        // Arms (bones)
        g.setColor(bone);
        g.setStroke(new BasicStroke(6f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
        g.drawLine(cx - 18, cy - 10, cx - 38, cy + 10);
        g.drawLine(cx + 18, cy - 10, cx + 38, cy + 10);
        // Hands
        g.fillOval(cx - 44, cy + 8, 12, 12);
        g.fillOval(cx + 32, cy + 8, 12, 12);
        // Legs
        g.setStroke(new BasicStroke(7f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
        g.drawLine(cx - 10, cy + 16, cx - 14, cy + 44);
        g.drawLine(cx + 10, cy + 16, cx + 14, cy + 44);
        g.fillOval(cx - 20, cy + 42, 12, 12);
        g.fillOval(cx + 8,  cy + 42, 12, 12);
        // Outline
        g.setColor(shadow);
        g.setStroke(new BasicStroke(1.5f));
        g.drawOval(cx - 16, cy - 50, 32, 30);
        g.drawRoundRect(cx - 18, cy - 16, 36, 32, 6, 6);
        g.setStroke(new BasicStroke(1));
    }

    private void drawBat(Graphics2D g, int cx, int cy) {
        float wingFlap = (float)(Math.sin(animTick * 0.35) * 18);
        Color body = new Color(75, 55, 110);
        Color wing = new Color(60, 40, 90);
        // Left wing
        int[] lx = {cx - 8, cx - 50, cx - 38, cx - 18};
        int[] ly = {cy - 5, cy - 10 - (int)wingFlap, cy + 12, cy + 10};
        g.setColor(wing);
        g.fillPolygon(lx, ly, 4);
        g.setColor(body);
        g.setStroke(new BasicStroke(1.5f));
        g.drawPolygon(lx, ly, 4);
        // Right wing
        int[] rx = {cx + 8, cx + 50, cx + 38, cx + 18};
        int[] ry = {cy - 5, cy - 10 - (int)wingFlap, cy + 12, cy + 10};
        g.setColor(wing);
        g.fillPolygon(rx, ry, 4);
        g.setColor(body);
        g.drawPolygon(rx, ry, 4);
        // Body
        g.setColor(body);
        g.fillOval(cx - 14, cy - 14, 28, 28);
        // Eyes
        g.setColor(new Color(255, 60, 60));
        g.fillOval(cx - 8, cy - 10, 8, 8);
        g.fillOval(cx + 1, cy - 10, 8, 8);
        // Ears
        int[] exL = {cx - 10, cx - 16, cx - 4};
        int[] eyL = {cy - 14, cy - 28, cy - 26};
        int[] exR = {cx + 10, cx + 4, cx + 16};
        int[] eyR = {cy - 14, cy - 26, cy - 28};
        g.setColor(body);
        g.fillPolygon(exL, eyL, 3);
        g.fillPolygon(exR, eyR, 3);
        // Fangs
        g.setColor(Color.WHITE);
        g.fillRect(cx - 5, cy + 4, 4, 8);
        g.fillRect(cx + 1, cy + 4, 4, 8);
        g.setStroke(new BasicStroke(1));
    }

    private void drawGhost(Graphics2D g, int cx, int cy) {
        float float_y = (float)(Math.sin(animTick * 0.1) * 6);
        // Glow aura
        g.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 0.25f));
        g.setColor(new Color(160, 180, 255));
        g.fillOval(cx - 44, cy - 58 + (int)float_y, 88, 96);
        g.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 0.72f));
        // Body
        g.setColor(new Color(175, 195, 255));
        // Top arc
        g.fillArc(cx - 32, cy - 54 + (int)float_y, 64, 64, 0, 180);
        // Bottom wavy
        int[] bx = {cx - 32, cx - 22, cx - 10, cx, cx + 10, cx + 22, cx + 32};
        int[] by = {cy + 10 + (int)float_y, cy + 22 + (int)float_y, cy + 12 + (int)float_y,
                cy + 24 + (int)float_y, cy + 12 + (int)float_y, cy + 22 + (int)float_y, cy + 10 + (int)float_y};
        int[] fullX = new int[bx.length + 2];
        int[] fullY = new int[by.length + 2];
        fullX[0] = cx - 32; fullY[0] = cy - 22 + (int)float_y;
        System.arraycopy(bx, 0, fullX, 1, bx.length);
        fullX[fullX.length-1] = cx + 32; fullY[fullY.length-1] = cy - 22 + (int)float_y;
        System.arraycopy(by, 0, fullY, 1, by.length);
        g.fillPolygon(fullX, fullY, fullX.length);
        // Eyes (hollow dark)
        g.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 1f));
        g.setColor(new Color(20, 18, 50));
        g.fillOval(cx - 16, cy - 36 + (int)float_y, 13, 16);
        g.fillOval(cx + 3,  cy - 36 + (int)float_y, 13, 16);
        // Mouth
        g.setColor(new Color(20, 18, 50));
        g.setStroke(new BasicStroke(2.5f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
        g.drawArc(cx - 10, cy - 18 + (int)float_y, 20, 12, 200, 140);
        g.setStroke(new BasicStroke(1));
    }

    private void drawGuardianBoss(Graphics2D g, int cx, int cy) {
        // Legs
        g.setColor(new Color(100, 120, 170));
        g.setStroke(new BasicStroke(12f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
        g.drawLine(cx - 14, cy + 24, cx - 16, cy + 50);
        g.drawLine(cx + 14, cy + 24, cx + 16, cy + 50);
        g.setColor(new Color(80, 100, 150));
        g.fillRect(cx - 22, cy + 46, 16, 8);
        g.fillRect(cx + 6,  cy + 46, 16, 8);
        // Body armor
        Color armor = new Color(130, 155, 200);
        g.setColor(armor);
        g.fillRoundRect(cx - 28, cy - 20, 56, 48, 8, 8);
        // Chest detail
        g.setColor(new Color(100, 125, 175));
        g.fillRoundRect(cx - 18, cy - 12, 36, 28, 6, 6);
        g.setColor(new Color(80, 110, 160));
        g.drawLine(cx, cy - 12, cx, cy + 16);
        // Shoulder pauldrons
        g.setColor(new Color(110, 135, 185));
        g.fillOval(cx - 42, cy - 24, 22, 20);
        g.fillOval(cx + 20, cy - 24, 22, 20);
        // Arms
        g.setColor(armor);
        g.setStroke(new BasicStroke(11f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
        g.drawLine(cx - 28, cy - 10, cx - 48, cy + 10);
        g.drawLine(cx + 28, cy - 10, cx + 48, cy + 10);
        // Shield (left)
        g.setColor(new Color(70, 100, 190));
        g.fillRoundRect(cx - 68, cy - 18, 28, 38, 6, 6);
        g.setColor(new Color(140, 170, 240));
        g.drawRoundRect(cx - 68, cy - 18, 28, 38, 6, 6);
        g.setColor(new Color(200, 220, 255));
        g.fillOval(cx - 58, cy - 4, 10, 10);
        // Sword (right)
        g.setColor(new Color(200, 210, 230));
        g.setStroke(new BasicStroke(5f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
        g.drawLine(cx + 50, cy + 16, cx + 70, cy - 24);
        g.setColor(new Color(170, 140, 60));
        g.setStroke(new BasicStroke(7f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
        g.drawLine(cx + 46, cy + 12, cx + 54, cy + 20);
        // Helmet
        g.setColor(armor);
        g.fillOval(cx - 22, cy - 56, 44, 40);
        g.fillRect(cx - 22, cy - 38, 44, 20);
        g.setColor(new Color(60, 80, 130));
        g.fillRect(cx - 8, cy - 52, 16, 28);
        // Eyes glow
        g.setColor(new Color(100, 200, 255));
        g.fillOval(cx - 14, cy - 44, 10, 8);
        g.fillOval(cx + 4,  cy - 44, 10, 8);
        // Outline
        g.setColor(new Color(80, 100, 150));
        g.setStroke(new BasicStroke(2f));
        g.drawRoundRect(cx - 28, cy - 20, 56, 48, 8, 8);
        g.drawOval(cx - 22, cy - 56, 44, 40);
        g.setStroke(new BasicStroke(1));
    }

    private void drawRageBoss(Graphics2D g, int cx, int cy) {
        boolean enraged = animState == AnimState.ATTACK;
        Color skin  = enraged ? new Color(210, 50, 30)  : new Color(185, 55, 40);
        Color dark  = enraged ? new Color(150, 30, 15)  : new Color(130, 35, 25);
        Color eyes  = enraged ? new Color(255, 220, 30) : new Color(255, 140, 0);

        // Fire aura when enraged
        if (enraged || animState == AnimState.IDLE) {
            float flicker = (float)(Math.sin(animTick * 0.4) * 0.3 + 0.5);
            g.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, flicker * 0.3f));
            g.setColor(new Color(255, 100, 0));
            g.fillOval(cx - 48, cy - 68, 96, 110);
            g.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 1f));
        }
        // Legs
        g.setColor(dark);
        g.setStroke(new BasicStroke(14f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
        g.drawLine(cx - 16, cy + 26, cx - 20, cy + 54);
        g.drawLine(cx + 16, cy + 26, cx + 20, cy + 54);
        g.setColor(dark.darker());
        g.fillRect(cx - 26, cy + 50, 16, 10);
        g.fillRect(cx + 10, cy + 50, 16, 10);
        // Body
        g.setColor(skin);
        g.fillRoundRect(cx - 30, cy - 18, 60, 48, 10, 10);
        // Muscle detail
        g.setColor(new Color(skin.getRed()-20, skin.getGreen()-10, skin.getBlue()-5, 100));
        g.fillOval(cx - 22, cy - 8, 18, 22);
        g.fillOval(cx + 4,  cy - 8, 18, 22);
        // Arms (thick)
        g.setColor(skin);
        g.setStroke(new BasicStroke(16f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
        g.drawLine(cx - 30, cy - 8,  cx - 55, cy + 18);
        g.drawLine(cx + 30, cy - 8,  cx + 55, cy + 18);
        // Fists
        g.setColor(dark);
        g.fillOval(cx - 66, cy + 12, 22, 22);
        g.fillOval(cx + 44, cy + 12, 22, 22);
        // Neck
        g.setColor(skin);
        g.fillRoundRect(cx - 14, cy - 30, 28, 16, 6, 6);
        // Head
        g.fillOval(cx - 26, cy - 62, 52, 46);
        // Horns
        g.setColor(dark);
        int[] hxL = {cx - 20, cx - 32, cx - 10};
        int[] hyL = {cy - 60, cy - 88, cy - 76};
        int[] hxR = {cx + 20, cx + 10, cx + 32};
        int[] hyR = {cy - 60, cy - 76, cy - 88};
        g.fillPolygon(hxL, hyL, 3);
        g.fillPolygon(hxR, hyR, 3);
        // Eyes
        g.setColor(eyes);
        g.fillOval(cx - 14, cy - 52, 12, 10);
        g.fillOval(cx + 2,  cy - 52, 12, 10);
        g.setColor(Color.BLACK);
        g.fillOval(cx - 11, cy - 50, 6, 6);
        g.fillOval(cx + 5,  cy - 50, 6, 6);
        // Mouth / snarl
        g.setColor(dark.darker());
        g.setStroke(new BasicStroke(2.5f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
        g.drawArc(cx - 10, cy - 34, 20, 12, 200, 140);
        g.setColor(Color.WHITE);
        g.fillRect(cx - 8, cy - 30, 4, 7);
        g.fillRect(cx + 4, cy - 30, 4, 7);
        // Outline
        g.setColor(dark);
        g.setStroke(new BasicStroke(2f));
        g.drawRoundRect(cx - 30, cy - 18, 60, 48, 10, 10);
        g.drawOval(cx - 26, cy - 62, 52, 46);
        g.setStroke(new BasicStroke(1));
    }

    private void drawCurseWitch(Graphics2D g, int cx, int cy) {
        float float_y = (float)(Math.sin(animTick * 0.12) * 4);
        // Robe
        g.setColor(new Color(40, 20, 70));
        int[] rx = {cx - 24, cx + 24, cx + 32, cx - 32};
        int[] ry = {cy - 10, cy - 10, cy + 50, cy + 50};
        g.fillPolygon(rx, ry, 4);
        // Robe hem (wavy)
        g.setColor(new Color(60, 30, 100));
        g.fillArc(cx - 32, cy + 38, 64, 24, 0, -180);
        // Body / torso
        g.setColor(new Color(55, 28, 88));
        g.fillOval(cx - 18, cy - 18, 36, 36);
        // Arms
        g.setColor(new Color(180, 140, 100));
        g.setStroke(new BasicStroke(6f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
        g.drawLine(cx - 18, cy - 6, cx - 38, cy + 10);
        g.drawLine(cx + 18, cy - 6, cx + 38, cy + 10);
        // Orb in hand
        g.setColor(new Color(160, 60, 220));
        g.fillOval(cx + 32, cy + 4, 16, 16);
        g.setColor(new Color(220, 160, 255, 150));
        g.fillOval(cx + 34, cy + 6, 8, 8);
        // Head (skin)
        g.setColor(new Color(200, 175, 140));
        g.fillOval(cx - 18, cy - 56 + (int)float_y, 36, 36);
        // Hat
        g.setColor(new Color(30, 15, 55));
        int[] hx = {cx - 22, cx + 22, cx + 14, cx - 14};
        int[] hy = {cy - 42 + (int)float_y, cy - 42 + (int)float_y,
                cy - 86 + (int)float_y, cy - 86 + (int)float_y};
        g.fillPolygon(hx, hy, 4);
        g.fillOval(cx - 24, cy - 46 + (int)float_y, 48, 12);
        // Eyes (glowing)
        g.setColor(new Color(200, 80, 255));
        g.fillOval(cx - 10, cy - 46 + (int)float_y, 8, 8);
        g.fillOval(cx + 2,  cy - 46 + (int)float_y, 8, 8);
        g.setColor(new Color(255, 200, 255, 180));
        g.fillOval(cx - 9, cy - 45 + (int)float_y, 4, 4);
        g.fillOval(cx + 3, cy - 45 + (int)float_y, 4, 4);
        g.setStroke(new BasicStroke(1));
    }

    private void drawStoneGolem(Graphics2D g, int cx, int cy) {
        Color stone = new Color(130, 125, 115);
        Color dark  = new Color(85,  82,  75);
        Color crack = new Color(60,  58,  52);
        // Legs (thick pillars)
        g.setColor(dark);
        g.fillRoundRect(cx - 28, cy + 16, 24, 38, 6, 6);
        g.fillRoundRect(cx + 4,  cy + 16, 24, 38, 6, 6);
        g.setColor(crack);
        g.setStroke(new BasicStroke(1.5f));
        g.drawLine(cx - 20, cy + 20, cx - 16, cy + 46);
        g.drawLine(cx + 12, cy + 22, cx + 16, cy + 48);
        // Body (massive block)
        g.setColor(stone);
        g.fillRoundRect(cx - 36, cy - 22, 72, 42, 10, 10);
        // Cracks on body
        g.setColor(crack);
        g.setStroke(new BasicStroke(1.8f));
        g.drawLine(cx - 20, cy - 18, cx - 10, cy + 8);
        g.drawLine(cx + 8,  cy - 14, cx + 20, cy + 12);
        // Arms (chunky)
        g.setColor(dark);
        g.fillRoundRect(cx - 58, cy - 18, 26, 44, 8, 8);
        g.fillRoundRect(cx + 32, cy - 18, 26, 44, 8, 8);
        // Fist details
        g.setColor(stone.darker());
        g.setStroke(new BasicStroke(1.5f));
        g.drawRoundRect(cx - 58, cy - 18, 26, 44, 8, 8);
        g.drawRoundRect(cx + 32, cy - 18, 26, 44, 8, 8);
        // Head
        g.setColor(stone);
        g.fillRoundRect(cx - 26, cy - 62, 52, 44, 10, 10);
        g.setColor(crack);
        g.setStroke(new BasicStroke(1.5f));
        g.drawLine(cx - 10, cy - 58, cx - 4, cy - 38);
        g.drawLine(cx + 6,  cy - 56, cx + 12, cy - 40);
        // Eyes (molten glow)
        g.setColor(new Color(255, 140, 30));
        g.fillOval(cx - 14, cy - 50, 12, 10);
        g.fillOval(cx + 2,  cy - 50, 12, 10);
        g.setColor(new Color(255, 220, 100, 200));
        g.fillOval(cx - 11, cy - 48, 6, 6);
        g.fillOval(cx + 5,  cy - 48, 6, 6);
        // Outline
        g.setColor(dark);
        g.setStroke(new BasicStroke(2f));
        g.drawRoundRect(cx - 36, cy - 22, 72, 42, 10, 10);
        g.drawRoundRect(cx - 26, cy - 62, 52, 44, 10, 10);
        g.setStroke(new BasicStroke(1));
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