package ui;

import javax.swing.*;
import java.awt.*;

public class HealthBar extends JComponent {
    private int current;
    private int max;
    private Color barColor;

    public HealthBar(int max, Color color) {
        this.max = max;
        this.current = max;
        this.barColor = color;
        setPreferredSize(new Dimension(200, 20));
    }

    public void updateHealth(int current, int max) {
        this.current = current;
        this.max = max;
        repaint();
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        // พื้นหลัง (สีเทา)
        g.setColor(Color.DARK_GRAY);
        g.fillRect(0, 0, getWidth(), getHeight());

        // หลอดเลือด (สีตามที่กำหนด)
        double percent = (double) current / max;
        int fillWidth = (int) (getWidth() * percent);

        g.setColor(barColor);
        g.fillRect(0, 0, fillWidth, getHeight());

        // ขอบ
        g.setColor(Color.BLACK);
        g.drawRect(0, 0, getWidth() - 1, getHeight() - 1);

        // ตัวเลขเลือด (เช่น 50/100)
        g.setColor(Color.WHITE);
        String text = current + " / " + max;
        FontMetrics fm = g.getFontMetrics();
        int textX = (getWidth() - fm.stringWidth(text)) / 2;
        int textY = (getHeight() + fm.getAscent()) / 2 - 2;
        g.drawString(text, textX, textY);
    }
}