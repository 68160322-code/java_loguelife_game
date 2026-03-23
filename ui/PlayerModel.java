package ui;

import javax.swing.*;
import java.awt.*;
import java.awt.geom.*;

/**
 * PlayerModel — วาด Knight แบบ vector art พร้อม animations
 *
 * Animations:
 *  IDLE   — bob เบาๆ + breathe
 *  HURT   — สั่น + red flash
 *  HEAL   — green glow + เลขลอยขึ้น
 *  BLOCK  — blue shield flash + ยกโล่
 *  ATTACK — lunge forward
 */
public class PlayerModel extends JPanel {

    public enum Anim { IDLE, HURT, HEAL, BLOCK, ATTACK }

    private Anim    anim      = Anim.IDLE;
    private int     tick      = 0;   // global tick สำหรับ idle bob
    private int     animFrame = 0;   // frame ภายใน anim ปัจจุบัน
    private float   flashAlpha = 0f;
    private Color   flashColor = null;
    private String  floatText  = "";
    private float   floatY     = 0f;
    private float   floatAlpha = 0f;

    public PlayerModel() {
        setOpaque(false);
        setPreferredSize(new Dimension(200, 220));

        Timer t = new Timer(16, e -> {
            tick++;
            if (anim != Anim.IDLE) {
                animFrame++;
                updateAnim();
            }
            repaint();
        });
        t.start();
    }

    // ── Triggers ─────────────────────────────────────────────────────────────

    public void playHurt() {
        anim = Anim.HURT; animFrame = 0;
        flashColor = new Color(220, 40, 40); flashAlpha = 0.6f;
        floatText = ""; floatAlpha = 0;
    }

    public void playHeal(int amount) {
        anim = Anim.HEAL; animFrame = 0;
        flashColor = new Color(40, 210, 80); flashAlpha = 0f;
        floatText = "+" + amount + " HP"; floatY = 0; floatAlpha = 1f;
    }

    public void playBlock(int amount) {
        anim = Anim.BLOCK; animFrame = 0;
        flashColor = new Color(60, 140, 230); flashAlpha = 0f;
        floatText = "+" + amount; floatY = 0; floatAlpha = 1f;
    }

    public void playAttack() {
        anim = Anim.ATTACK; animFrame = 0;
        flashColor = new Color(220, 160, 30); flashAlpha = 0.3f;
        floatText = ""; floatAlpha = 0;
    }

    // ── Animation logic ───────────────────────────────────────────────────────

    private void updateAnim() {
        switch (anim) {
            case HURT:
                flashAlpha = Math.max(0f, 0.6f - animFrame * 0.06f);
                if (animFrame > 12) { anim = Anim.IDLE; flashAlpha = 0; }
                break;
            case HEAL:
                flashAlpha = animFrame < 8 ? animFrame * 0.05f : Math.max(0f, 0.4f - (animFrame-8)*0.04f);
                floatY = animFrame * 1.8f;
                floatAlpha = Math.max(0f, 1f - animFrame * 0.04f);
                if (animFrame > 24) { anim = Anim.IDLE; flashAlpha = 0; floatAlpha = 0; }
                break;
            case BLOCK:
                flashAlpha = animFrame < 6 ? animFrame * 0.06f : Math.max(0f, 0.36f - (animFrame-6)*0.04f);
                floatY = animFrame * 1.5f;
                floatAlpha = Math.max(0f, 1f - animFrame * 0.045f);
                if (animFrame > 22) { anim = Anim.IDLE; flashAlpha = 0; floatAlpha = 0; }
                break;
            case ATTACK:
                flashAlpha = Math.max(0f, 0.3f - animFrame * 0.03f);
                if (animFrame > 12) { anim = Anim.IDLE; flashAlpha = 0; }
                break;
        }
    }

    // ── Offsets for current anim ──────────────────────────────────────────────

    private float[] getOffset() {
        // [ox, oy, scaleX, scaleY, shieldRaise]
        // idle bob
        double bob   = Math.sin(tick * 0.06) * 2.5;
        double breath = Math.sin(tick * 0.04) * 0.012;
        float ox = 0, oy = (float)bob;
        float sx = (float)(1.0 + breath), sy = (float)(1.0 - breath);
        float shield = 0;

        switch (anim) {
            case HURT:
                int sh = animFrame % 4;
                ox = sh < 2 ? -5 : 5;
                oy += sh == 1 ? -3 : 2;
                break;
            case ATTACK:
                ox = animFrame < 6 ? animFrame * 7f : (12 - animFrame) * 7f;
                break;
            case BLOCK:
                shield = Math.min(1f, animFrame * 0.15f);
                break;
        }
        return new float[]{ox, oy, sx, sy, shield};
    }

    // ── Paint ─────────────────────────────────────────────────────────────────

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        Graphics2D g2 = (Graphics2D) g.create();
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING,   RenderingHints.VALUE_ANTIALIAS_ON);
        g2.setRenderingHint(RenderingHints.KEY_RENDERING,      RenderingHints.VALUE_RENDER_QUALITY);
        g2.setRenderingHint(RenderingHints.KEY_STROKE_CONTROL, RenderingHints.VALUE_STROKE_PURE);

        int w = getWidth(), h = getHeight();
        int cx = w / 2, cy = (int)(h * 0.55);

        float[] off = getOffset();
        float ox = off[0], oy = off[1], sx = off[2], sy = off[3], shieldRaise = off[4];

        // Glow / atmosphere
        drawAura(g2, cx, cy, w, h);

        // Apply transform
        AffineTransform base = g2.getTransform();
        g2.translate(cx + ox, cy + oy);
        g2.scale(sx, sy);
        g2.translate(-cx, -cy);

        // Draw knight
        drawKnight(g2, cx, cy, shieldRaise);

        g2.setTransform(base);

        // Flash overlay
        if (flashAlpha > 0 && flashColor != null) {
            g2.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, flashAlpha));
            g2.setColor(flashColor);
            g2.fillOval(cx - 55, cy - 90, 110, 140);
        }

        // Floating text
        if (floatAlpha > 0 && !floatText.isEmpty()) {
            g2.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, floatAlpha));
            g2.setFont(new Font("Serif", Font.BOLD, 20));
            FontMetrics fm = g2.getFontMetrics();
            int tx = cx - fm.stringWidth(floatText) / 2;
            int ty = (int)(cy - 80 - floatY);
            // shadow
            g2.setColor(new Color(0, 0, 0, (int)(floatAlpha * 160)));
            g2.drawString(floatText, tx + 2, ty + 2);
            // text
            g2.setColor(flashColor != null ? flashColor.brighter() : Color.WHITE);
            g2.drawString(floatText, tx, ty);
        }

        g2.dispose();
    }

    // ── Atmosphere glow ───────────────────────────────────────────────────────

    private void drawAura(Graphics2D g2, int cx, int cy, int w, int h) {
        // subtle radial glow ใต้ knight
        Color base = anim == Anim.HURT  ? new Color(180, 30, 30, 40)
                : anim == Anim.HEAL  ? new Color(40, 200, 80, 45)
                : anim == Anim.BLOCK ? new Color(40, 120, 220, 40)
                : new Color(80, 120, 200, 25);
        RadialGradientPaint rg = new RadialGradientPaint(
                cx, cy + 20, 70,
                new float[]{0f, 1f},
                new Color[]{base, new Color(0,0,0,0)});
        g2.setPaint(rg);
        g2.fillOval(cx - 70, cy - 50, 140, 120);
    }

    // ── Knight drawing ────────────────────────────────────────────────────────

    private void drawKnight(Graphics2D g2, int cx, int cy, float shieldRaise) {
        // สีชุดเกราะ
        Color armorDark   = new Color(60,  65,  80);
        Color armorMid    = new Color(90,  100, 120);
        Color armorLight  = new Color(140, 155, 175);
        Color armorShine  = new Color(200, 215, 230);
        Color goldTrim    = new Color(200, 160, 40);
        Color goldDark    = new Color(140, 100, 20);
        Color shieldBlue  = new Color(30,  70,  160);
        Color shieldEdge  = new Color(60,  120, 220);
        Color skinColor   = new Color(220, 180, 140);
        Color swordMetal  = new Color(190, 200, 215);
        Color swordEdge   = new Color(230, 240, 255);
        Color darkLine    = new Color(20,  22,  30);

        g2.setStroke(new BasicStroke(1.5f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));

        // ── Legs ──────────────────────────────────────────────────────────────
        // Left leg
        drawArmorLeg(g2, cx - 14, cy + 30, armorMid, armorDark, goldTrim, darkLine, false);
        // Right leg
        drawArmorLeg(g2, cx + 14, cy + 30, armorMid, armorDark, goldTrim, darkLine, true);

        // ── Body / Chest armor ────────────────────────────────────────────────
        // torso
        int[] torsoX = {cx-22, cx+22, cx+18, cx-18};
        int[] torsoY = {cy-10, cy-10, cy+32, cy+32};
        g2.setColor(armorMid);
        g2.fillPolygon(torsoX, torsoY, 4);

        // chest plate gradient
        GradientPaint chestGrad = new GradientPaint(cx-20, cy-10, armorLight, cx+20, cy+20, armorDark);
        g2.setPaint(chestGrad);
        int[] plateX = {cx-18, cx+18, cx+14, cx-14};
        int[] plateY = {cy-8,  cy-8,  cy+20, cy+20};
        g2.fillPolygon(plateX, plateY, 4);
        g2.setColor(darkLine);
        g2.drawPolygon(plateX, plateY, 4);

        // chest center ridge
        g2.setColor(armorShine);
        g2.setStroke(new BasicStroke(1.5f));
        g2.drawLine(cx, cy-6, cx, cy+18);

        // gold trim on chest
        g2.setColor(goldTrim);
        g2.setStroke(new BasicStroke(2f));
        g2.drawLine(cx-16, cy+2, cx+16, cy+2);
        g2.drawLine(cx-14, cy+12, cx+14, cy+12);

        // ── Pauldrons (shoulder armor) ────────────────────────────────────────
        drawPauldron(g2, cx - 24, cy - 12, armorMid, armorLight, goldTrim, darkLine, false);
        drawPauldron(g2, cx + 24, cy - 12, armorMid, armorLight, goldTrim, darkLine, true);

        // ── Arms ─────────────────────────────────────────────────────────────
        // Right arm (sword arm) — ยื่นออกมาพร้อมดาบ
        drawArmorArm(g2, cx + 22, cy, armorMid, armorDark, goldTrim, darkLine, true);
        // Left arm (shield arm) — ยกโล่
        drawArmorArm(g2, cx - 22, cy, armorMid, armorDark, goldTrim, darkLine, false);

        // ── Shield (left side) ────────────────────────────────────────────────
        float sy2 = shieldRaise * 10; // ยกโล่ขึ้นเมื่อ block
        drawShield(g2, cx - 38, (int)(cy + 5 - sy2), shieldBlue, shieldEdge, goldTrim, darkLine);

        // ── Sword (right side) ────────────────────────────────────────────────
        drawSword(g2, cx + 28, cy - 20, swordMetal, swordEdge, goldTrim, goldDark, darkLine);

        // ── Helmet ────────────────────────────────────────────────────────────
        drawHelmet(g2, cx, cy - 55, armorMid, armorLight, armorShine, goldTrim, goldDark, darkLine);

        // ── Belt ──────────────────────────────────────────────────────────────
        g2.setColor(new Color(40, 35, 50));
        g2.fillRect(cx - 18, cy + 22, 36, 10);
        g2.setColor(goldTrim);
        g2.setStroke(new BasicStroke(1.5f));
        g2.drawRect(cx - 18, cy + 22, 36, 10);
        // belt buckle
        g2.setColor(goldTrim);
        g2.fillRect(cx - 5, cy + 24, 10, 6);
        g2.setColor(darkLine);
        g2.drawRect(cx - 5, cy + 24, 10, 6);
    }

    // ── Helmet ────────────────────────────────────────────────────────────────

    private void drawHelmet(Graphics2D g2, int cx, int cy,
                            Color mid, Color light, Color shine, Color gold, Color goldDk, Color line) {

        // dome
        g2.setColor(mid);
        g2.fillOval(cx - 18, cy - 10, 36, 38);

        // gradient highlight
        GradientPaint hg = new GradientPaint(cx-14, cy-8, light, cx+8, cy+20, mid);
        g2.setPaint(hg);
        g2.fillOval(cx - 16, cy - 8, 24, 26);

        // visor slit
        g2.setColor(new Color(15, 18, 28));
        g2.fillRect(cx - 10, cy + 8, 20, 5);
        g2.setColor(new Color(0, 180, 255, 60)); // glow di visor
        g2.fillRect(cx - 9, cy + 9, 18, 3);

        // chin guard
        g2.setColor(mid);
        int[] chinX = {cx-13, cx+13, cx+10, cx-10};
        int[] chinY = {cy+16, cy+16, cy+26, cy+26};
        g2.fillPolygon(chinX, chinY, 4);
        g2.setColor(line);
        g2.setStroke(new BasicStroke(1.2f));
        g2.drawPolygon(chinX, chinY, 4);

        // gold trim
        g2.setColor(gold);
        g2.setStroke(new BasicStroke(2f));
        g2.drawOval(cx - 18, cy - 10, 36, 38);

        // crest/plume at top
        g2.setColor(new Color(180, 30, 30));
        for (int i = -2; i <= 2; i++) {
            int px = cx + i * 3;
            g2.fillOval(px - 2, cy - 18 - Math.abs(i) * 2, 5, 10);
        }

        // shine dot
        g2.setColor(new Color(255, 255, 255, 160));
        g2.fillOval(cx - 8, cy - 4, 5, 5);

        // outline
        g2.setColor(line);
        g2.setStroke(new BasicStroke(1.5f));
        g2.drawOval(cx - 18, cy - 10, 36, 38);
    }

    // ── Pauldron (shoulder) ───────────────────────────────────────────────────

    private void drawPauldron(Graphics2D g2, int cx, int cy,
                              Color mid, Color light, Color gold, Color line, boolean right) {
        int dir = right ? 1 : -1;

        // main shoulder plate
        int[] px = {cx, cx + dir*14, cx + dir*12, cx - dir*2};
        int[] py = {cy - 8, cy - 6, cy + 14, cy + 14};
        g2.setColor(mid);
        g2.fillPolygon(px, py, 4);

        // highlight
        g2.setColor(light);
        g2.drawLine(cx, cy - 8, cx + dir*10, cy - 4);

        // gold trim
        g2.setColor(gold);
        g2.setStroke(new BasicStroke(1.5f));
        g2.drawPolygon(px, py, 4);

        g2.setColor(line);
        g2.setStroke(new BasicStroke(1f));
        g2.drawPolygon(px, py, 4);
    }

    // ── Arm ───────────────────────────────────────────────────────────────────

    private void drawArmorArm(Graphics2D g2, int cx, int cy,
                              Color mid, Color dark, Color gold, Color line, boolean right) {
        int dir = right ? 1 : -1;

        // upper arm
        g2.setColor(mid);
        g2.fillOval(cx - 6, cy - 4, 12, 22);
        // lower arm
        g2.setColor(dark);
        g2.fillOval(cx - 5, cy + 14, 10, 18);
        // gauntlet
        g2.setColor(new Color(70, 75, 90));
        g2.fillOval(cx - 6, cy + 28, 12, 10);

        // gold band
        g2.setColor(gold);
        g2.setStroke(new BasicStroke(1.5f));
        g2.drawOval(cx - 5, cy + 14, 10, 5);

        g2.setColor(line);
        g2.setStroke(new BasicStroke(1f));
        g2.drawOval(cx - 6, cy - 4, 12, 22);
    }

    // ── Leg ───────────────────────────────────────────────────────────────────

    private void drawArmorLeg(Graphics2D g2, int cx, int cy,
                              Color mid, Color dark, Color gold, Color line, boolean right) {
        // thigh
        g2.setColor(mid);
        g2.fillRoundRect(cx - 8, cy, 16, 22, 6, 6);
        // knee plate
        g2.setColor(new Color(110, 120, 140));
        g2.fillOval(cx - 7, cy + 18, 14, 10);
        // shin
        g2.setColor(dark);
        g2.fillRoundRect(cx - 7, cy + 26, 14, 20, 4, 4);
        // boot
        g2.setColor(new Color(40, 38, 50));
        g2.fillRoundRect(cx - 8, cy + 44, 16, 10, 4, 4);

        // gold trim knee
        g2.setColor(gold);
        g2.setStroke(new BasicStroke(1.5f));
        g2.drawOval(cx - 7, cy + 18, 14, 10);

        g2.setColor(line);
        g2.setStroke(new BasicStroke(1f));
        g2.drawRoundRect(cx - 8, cy, 16, 22, 6, 6);
        g2.drawRoundRect(cx - 7, cy + 26, 14, 20, 4, 4);
    }

    // ── Shield ────────────────────────────────────────────────────────────────

    private void drawShield(Graphics2D g2, int cx, int cy,
                            Color base, Color edge, Color gold, Color line) {
        // shield shape
        int[] sx = {cx, cx+18, cx+18, cx+9, cx};
        int[] sy = {cy, cy,    cy+24, cy+32, cy+24};
        int[] sxL = {cx, cx-18, cx-18, cx-9, cx};

        // fill
        GradientPaint sg = new GradientPaint(cx-18, cy, edge, cx+18, cy+24, base);
        g2.setPaint(sg);
        g2.fillPolygon(sx, sy, 5);
        g2.fillPolygon(sxL, sy, 5);

        // cross emblem
        g2.setColor(gold);
        g2.setStroke(new BasicStroke(2.5f));
        g2.drawLine(cx - 10, cy + 12, cx + 10, cy + 12);
        g2.drawLine(cx, cy + 4, cx, cy + 22);

        // border
        g2.setColor(gold);
        g2.setStroke(new BasicStroke(2f));
        g2.drawPolygon(sx, sy, 5);
        g2.drawPolygon(sxL, sy, 5);

        g2.setColor(line);
        g2.setStroke(new BasicStroke(1f));
        g2.drawPolygon(sx, sy, 5);
        g2.drawPolygon(sxL, sy, 5);
    }

    // ── Sword ─────────────────────────────────────────────────────────────────

    private void drawSword(Graphics2D g2, int cx, int cy,
                           Color blade, Color edge, Color gold, Color goldDk, Color line) {
        // blade
        g2.setColor(blade);
        g2.fillRect(cx - 2, cy, 4, 55);
        // edge highlight
        g2.setColor(edge);
        g2.fillRect(cx - 1, cy, 2, 55);

        // crossguard
        g2.setColor(gold);
        g2.fillRoundRect(cx - 12, cy + 50, 24, 7, 3, 3);
        g2.setColor(goldDk);
        g2.drawRoundRect(cx - 12, cy + 50, 24, 7, 3, 3);

        // pommel
        g2.setColor(gold);
        g2.fillOval(cx - 5, cy + 57, 10, 10);
        g2.setColor(goldDk);
        g2.drawOval(cx - 5, cy + 57, 10, 10);

        // blade outline
        g2.setColor(line);
        g2.setStroke(new BasicStroke(1f));
        g2.drawRect(cx - 2, cy, 4, 55);

        // blade shine
        g2.setColor(new Color(255, 255, 255, 100));
        g2.drawLine(cx - 1, cy + 2, cx - 1, cy + 48);
    }
}