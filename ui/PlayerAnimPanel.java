package ui;

import javax.swing.*;
import java.awt.*;
import java.awt.geom.RoundRectangle2D;

/**
 * PlayerAnimPanel — panel ผู้เล่นที่มี animation
 *
 * Animations:
 *  - HURT    : panel สั่น + flash สีแดง (โดนตี)
 *  - HEAL    : flash สีเขียว + เลขลอยขึ้น
 *  - BLOCK   : flash สีน้ำเงิน + shield icon
 *  - ATTACK  : panel เคลื่อนไปขวา (โจมตี)
 */
public class PlayerAnimPanel extends JPanel {

    public enum Anim { IDLE, HURT, HEAL, BLOCK, ATTACK }

    private Anim    anim      = Anim.IDLE;
    private int     frame     = 0;
    private String  floatText = "";
    private Color   flashColor = null;
    private Timer   animTimer;

    // offset สำหรับสั่น/เคลื่อน
    private int ox = 0, oy = 0;
    private float flashAlpha = 0f;

    public PlayerAnimPanel() {
        setOpaque(false);
        animTimer = new Timer(16, e -> tick());
        animTimer.start();
    }

    // ── Trigger animations ────────────────────────────────────────────────────

    public void playHurt() {
        anim = Anim.HURT;
        frame = 0;
        floatText = "";
        flashColor = new Color(220, 50, 50);
    }

    public void playHeal(int amount) {
        anim = Anim.HEAL;
        frame = 0;
        floatText = "+" + amount + " HP";
        flashColor = new Color(50, 210, 80);
    }

    public void playBlock(int amount) {
        anim = Anim.BLOCK;
        frame = 0;
        floatText = "+" + amount;
        flashColor = new Color(60, 130, 220);
    }

    public void playAttack() {
        anim = Anim.ATTACK;
        frame = 0;
        floatText = "";
        flashColor = new Color(220, 160, 30);
    }

    // ── Animation tick ────────────────────────────────────────────────────────

    private void tick() {
        if (anim == Anim.IDLE) return;
        frame++;

        switch (anim) {
            case HURT:
                // สั่น 8 เฟรม
                ox = (frame % 4 < 2) ? -4 : 4;
                oy = (frame % 3 == 0) ? -2 : 0;
                flashAlpha = Math.max(0f, 0.5f - frame * 0.05f);
                if (frame > 10) { anim = Anim.IDLE; ox = 0; oy = 0; flashAlpha = 0; }
                break;

            case HEAL:
                ox = 0; oy = 0;
                flashAlpha = frame < 6 ? frame * 0.08f : Math.max(0f, 0.48f - (frame-6) * 0.04f);
                if (frame > 20) { anim = Anim.IDLE; flashAlpha = 0; floatText = ""; }
                break;

            case BLOCK:
                ox = 0; oy = 0;
                flashAlpha = frame < 4 ? frame * 0.1f : Math.max(0f, 0.4f - (frame-4) * 0.04f);
                if (frame > 16) { anim = Anim.IDLE; flashAlpha = 0; floatText = ""; }
                break;

            case ATTACK:
                // เคลื่อนไปขวาแล้วกลับ
                if (frame < 5)       ox = frame * 6;
                else if (frame < 10) ox = (10 - frame) * 6;
                else { ox = 0; anim = Anim.IDLE; }
                flashAlpha = 0.3f - frame * 0.03f;
                break;
        }
        repaint();
    }

    // ── Paint ─────────────────────────────────────────────────────────────────

    @Override
    protected void paintComponent(Graphics g) {
        Graphics2D g2 = (Graphics2D) g.create();
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

        int w = getWidth(), h = getHeight();

        // เลื่อนทั้ง panel ตาม offset
        g2.translate(ox, oy);

        // วาด children (playerLabel, healthBar, etc.) ปกติ
        super.paintComponent(g2);

        // Flash overlay
        if (flashAlpha > 0 && flashColor != null) {
            g2.setColor(new Color(
                    flashColor.getRed(), flashColor.getGreen(), flashColor.getBlue(),
                    (int)(flashAlpha * 255)));
            g2.fill(new RoundRectangle2D.Float(2, 2, w-4, h-4, 10, 10));
        }

        // Floating text (heal/block)
        if (!floatText.isEmpty() && frame > 0) {
            float textY = h * 0.3f - (frame * 1.5f); // ลอยขึ้น
            float textAlpha = Math.max(0f, 1f - frame * 0.05f);
            g2.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, textAlpha));
            g2.setFont(new Font("Serif", Font.BOLD, 18));
            g2.setColor(flashColor != null ? flashColor.brighter() : Color.WHITE);
            // เงา
            g2.setColor(new Color(0, 0, 0, (int)(textAlpha * 180)));
            FontMetrics fm = g2.getFontMetrics();
            int tx = (w - fm.stringWidth(floatText)) / 2;
            g2.drawString(floatText, tx + 1, (int)textY + 1);
            // ข้อความหลัก
            g2.setColor(flashColor != null ? flashColor.brighter() : Color.WHITE);
            g2.drawString(floatText, tx, (int)textY);
        }

        // Shield icon ตอน BLOCK
        if (anim == Anim.BLOCK && frame < 16) {
            float a = Math.max(0f, 1f - frame * 0.065f);
            g2.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, a));
            g2.setFont(new Font("SansSerif", Font.BOLD, 28));
            g2.setColor(new Color(100, 180, 255));
            FontMetrics fm2 = g2.getFontMetrics();
            String shield = "[+]";
            g2.drawString(shield, (w - fm2.stringWidth(shield)) / 2, h / 2 + 10);
        }

        g2.dispose();
    }
}