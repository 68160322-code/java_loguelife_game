import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.util.*;

/**
 * MapScreen — Branching path map (Slay the Spire style)
 * ผู้เล่นเดินได้เฉพาะตาม edge ที่สร้างไว้ตอน generateMap เท่านั้น
 * ข้ามเส้นทางไม่ได้
 */
public class MapScreen extends JPanel {

    public interface MapListener {
        void onNodeSelected(MapNode node);
    }

    private static final int COLS = 7;
    private static final int ROWS = 4;

    private MapNode[][] grid = new MapNode[COLS][ROWS];

    // edges[c][r] = list ของ row ใน col c+1 ที่ node (c,r) เชื่อมถึง
    @SuppressWarnings("unchecked")
    private java.util.List<Integer>[][] edges = new java.util.ArrayList[COLS][ROWS];

    private MapListener listener;
    private int currentCol = -1;
    private int currentRow = -1;
    private int[] colX, rowY;
    private MapNode hovered = null;
    private boolean readOnly = false;

    public MapScreen(MapListener listener) {
        this.listener = listener;
        setBackground(new Color(10, 8, 20));
        setPreferredSize(new Dimension(860, 480));

        for (int c = 0; c < COLS; c++)
            for (int r = 0; r < ROWS; r++)
                edges[c][r] = new java.util.ArrayList<>();

        generateMap();
        calcPositions();

        addMouseListener(new MouseAdapter() {
            @Override public void mouseClicked(MouseEvent e) {
                if (!readOnly) handleClick(e.getX(), e.getY());
            }
        });
        addMouseMotionListener(new MouseMotionAdapter() {
            @Override public void mouseMoved(MouseEvent e) {
                if (!readOnly) handleHover(e.getX(), e.getY());
                else if (hovered != null) { hovered = null; repaint(); }
            }
        });
    }

    public void setReadOnly(boolean ro) {
        this.readOnly = ro;
        hovered = null;
        repaint();
    }

    public void setCurrentPosition(int col, int row) {
        this.currentCol = col;
        this.currentRow = row;
        repaint();
    }

    // ── Map Generation ────────────────────────────────────────────────────────
    private void generateMap() {
        Random rand = new Random();
        MapNode.NodeType[] pool = {
                MapNode.NodeType.BATTLE, MapNode.NodeType.BATTLE, MapNode.NodeType.BATTLE,
                MapNode.NodeType.EVENT,  MapNode.NodeType.SHOP,   MapNode.NodeType.REST,
                MapNode.NodeType.ELITE
        };

        // 1. สร้าง nodes
        for (int c = 0; c < COLS; c++) {
            for (int r = 0; r < ROWS; r++) {
                MapNode.NodeType type;
                if (c == COLS - 1) type = MapNode.NodeType.BOSS;
                else if (c == 0)   type = MapNode.NodeType.BATTLE;
                else               type = pool[rand.nextInt(pool.length)];
                grid[c][r] = new MapNode(type, c, r);
            }
        }

        // 2. สร้าง edges:
        //    - straight (same row) เสมอ
        //    - 50% โอกาส diagonal ±1 row
        //    - ตรวจสอบว่าทุก node ใน col ถัดไปมีคนเชื่อมมาอย่างน้อย 1 เส้น
        for (int c = 0; c < COLS - 1; c++) {
            boolean[] nextCovered = new boolean[ROWS];

            for (int r = 0; r < ROWS; r++) {
                edges[c][r].add(r);
                nextCovered[r] = true;

                if (rand.nextBoolean()) {
                    int diag = (rand.nextBoolean() && r > 0) ? r - 1
                            : (r < ROWS - 1)               ? r + 1
                            : r - 1;
                    if (diag >= 0 && diag < ROWS && !edges[c][r].contains(Integer.valueOf(diag))) {
                        edges[c][r].add(diag);
                        nextCovered[diag] = true;
                    }
                }
            }

            // node ที่ไม่มีใครเชื่อมถึง → ให้ neighbor ใกล้สุดเชื่อมหา
            for (int nr = 0; nr < ROWS; nr++) {
                if (nextCovered[nr]) continue;
                for (int d = 1; d < ROWS; d++) {
                    if (nr - d >= 0 && !edges[c][nr - d].contains(Integer.valueOf(nr))) {
                        edges[c][nr - d].add(nr);
                        break;
                    } else if (nr + d < ROWS && !edges[c][nr + d].contains(Integer.valueOf(nr))) {
                        edges[c][nr + d].add(nr);
                        break;
                    }
                }
            }
        }

        // 3. เปิด col 0 ทั้งหมด
        for (int r = 0; r < ROWS; r++) grid[0][r].setAvailable(true);
    }

    private void calcPositions() {
        int marginX = 80, marginY = 70;
        colX = new int[COLS];
        rowY = new int[ROWS];
        for (int c = 0; c < COLS; c++) colX[c] = marginX + c * ((860 - marginX * 2) / (COLS - 1));
        for (int r = 0; r < ROWS; r++) rowY[r] = marginY + r * ((480 - marginY * 2) / (ROWS - 1));
    }

    // ── Interaction ───────────────────────────────────────────────────────────
    private void handleClick(int mx, int my) {
        for (int c = 0; c < COLS; c++) for (int r = 0; r < ROWS; r++) {
            MapNode n = grid[c][r];
            if (n == null || !n.isAvailable() || n.isVisited()) continue;
            if (dist(mx, my, colX[c], rowY[r]) < 22) {
                n.setVisited(true);
                currentCol = c;
                currentRow = r;

                // ปิด node อื่นในคอลัมน์เดียวกันที่ไม่ได้เลือก
                for (int or = 0; or < ROWS; or++) {
                    if (or != r && grid[c][or] != null) {
                        grid[c][or].setAvailable(false);
                        grid[c][or].setLocked(true);
                    }
                }

                // ปลดล็อคเฉพาะ node ที่มี edge เชื่อมจาก (c,r) เท่านั้น
                if (c + 1 < COLS) {
                    for (int nr : edges[c][r]) {
                        grid[c + 1][nr].setAvailable(true);
                    }
                }
                repaint();
                if (listener != null) listener.onNodeSelected(n);
                return;
            }
        }
    }

    private void handleHover(int mx, int my) {
        MapNode prev = hovered;
        hovered = null;
        for (int c = 0; c < COLS; c++) for (int r = 0; r < ROWS; r++) {
            MapNode n = grid[c][r];
            if (n != null && n.isAvailable() && !n.isVisited()
                    && dist(mx, my, colX[c], rowY[r]) < 22) {
                hovered = n;
                break;
            }
        }
        if (hovered != prev) repaint();
    }

    private double dist(int x1, int y1, int x2, int y2) {
        return Math.sqrt((x1 - x2) * (double)(x1 - x2) + (y1 - y2) * (double)(y1 - y2));
    }

    // ── Painting ──────────────────────────────────────────────────────────────
    @Override protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        Graphics2D g2 = (Graphics2D) g;
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        drawStars(g2);
        drawConnections(g2);
        drawNodes(g2);
        drawPlayerMarker(g2);
        drawHeader(g2);
        if (readOnly) drawReadOnlyOverlay(g2);
    }

    private void drawStars(Graphics2D g2) {
        Random rand = new Random(42);
        for (int i = 0; i < 150; i++) {
            int sx = rand.nextInt(860), sy = rand.nextInt(480), ss = rand.nextInt(2) + 1;
            g2.setColor(new Color(255, 255, 255, 20 + rand.nextInt(50)));
            g2.fillOval(sx, sy, ss, ss);
        }
    }

    private void drawConnections(Graphics2D g2) {
        for (int c = 0; c < COLS - 1; c++) {
            for (int r = 0; r < ROWS; r++) {
                for (int nr : edges[c][r]) {
                    boolean srcVisited = grid[c][r].isVisited();
                    boolean srcLocked  = grid[c][r].isLocked();
                    boolean dstAvail   = grid[c + 1][nr].isAvailable();

                    Color lineColor;
                    float lineWidth;
                    if (srcLocked) {
                        // เส้นจาก node ที่ไม่ได้เลือก — จางมาก
                        lineColor = new Color(35, 32, 50, 50);
                        lineWidth = 0.8f;
                    } else if (srcVisited) {
                        // เส้นที่เดินผ่านแล้ว — สว่าง
                        lineColor = new Color(160, 120, 240, 220);
                        lineWidth = 2.5f;
                    } else if (grid[c][r].isAvailable() && dstAvail) {
                        // เส้นที่เดินได้ตอนนี้
                        lineColor = new Color(110, 85, 170, 170);
                        lineWidth = 1.8f;
                    } else {
                        // ยังไม่ถึง
                        lineColor = new Color(50, 45, 75, 80);
                        lineWidth = 1.0f;
                    }

                    g2.setColor(lineColor);
                    g2.setStroke(new BasicStroke(lineWidth, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
                    g2.drawLine(colX[c], rowY[r], colX[c + 1], rowY[nr]);
                }
            }
        }
        g2.setStroke(new BasicStroke(1));
    }

    private void drawNodes(Graphics2D g2) {
        for (int c = 0; c < COLS; c++) for (int r = 0; r < ROWS; r++) {
            MapNode n = grid[c][r];
            if (n == null) continue;
            int nx = colX[c], ny = rowY[r], rad = 21;
            boolean isHovered = (n == hovered);

            if (n.isVisited()) {
                // เคยผ่านแล้ว — สีม่วงเข้ม
                g2.setColor(new Color(40, 35, 60));
                g2.fillOval(nx - rad, ny - rad, rad * 2, rad * 2);
                g2.setColor(new Color(70, 60, 100));
                g2.setStroke(new BasicStroke(1.5f));
                g2.drawOval(nx - rad, ny - rad, rad * 2, rad * 2);
            } else if (n.isLocked()) {
                // ถูกปิดเพราะเลือก node อื่นในคอลัมน์นี้ — สีเทาจาง
                g2.setColor(new Color(22, 20, 35));
                g2.fillOval(nx - rad, ny - rad, rad * 2, rad * 2);
                g2.setColor(new Color(45, 42, 58));
                g2.setStroke(new BasicStroke(1f));
                g2.drawOval(nx - rad, ny - rad, rad * 2, rad * 2);
                // วาด X ทับ
                g2.setColor(new Color(70, 65, 85));
                g2.setStroke(new BasicStroke(1.8f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
                int m = 8;
                g2.drawLine(nx - m, ny - m, nx + m, ny + m);
                g2.drawLine(nx + m, ny - m, nx - m, ny + m);
            } else if (n.isAvailable()) {
                // เลือกได้ — สีสดตาม type + glow
                Color base = n.getColor();
                int glowR = isHovered ? rad + 10 : rad + 6;
                g2.setColor(new Color(base.getRed(), base.getGreen(), base.getBlue(), isHovered ? 90 : 50));
                g2.fillOval(nx - glowR, ny - glowR, glowR * 2, glowR * 2);
                g2.setColor(isHovered ? base.brighter() : base);
                g2.fillOval(nx - rad, ny - rad, rad * 2, rad * 2);
                g2.setColor(Color.WHITE);
                g2.setStroke(new BasicStroke(isHovered ? 2.5f : 1.8f));
                g2.drawOval(nx - rad, ny - rad, rad * 2, rad * 2);
            } else {
                // ยังไม่ถึง — มืดมาก
                g2.setColor(new Color(18, 15, 30));
                g2.fillOval(nx - rad, ny - rad, rad * 2, rad * 2);
                g2.setColor(new Color(40, 38, 55));
                g2.drawOval(nx - rad, ny - rad, rad * 2, rad * 2);
            }
            g2.setStroke(new BasicStroke(1));

            // Icon (ไม่วาดถ้า locked)
            if (!n.isLocked()) {
                Color iconColor = n.isVisited()   ? new Color(80, 70, 110)
                        : n.isAvailable() ? Color.WHITE
                        : new Color(50, 45, 65);
                g2.setFont(new Font("SansSerif", Font.BOLD, n.getType() == MapNode.NodeType.BOSS ? 15 : 13));
                g2.setColor(iconColor);
                FontMetrics fm = g2.getFontMetrics();
                g2.drawString(n.getIcon(), nx - fm.stringWidth(n.getIcon()) / 2, ny + 5);
            }

            // Label ใต้ node (เฉพาะ available)
            if (n.isAvailable() && !n.isVisited()) {
                g2.setFont(new Font("SansSerif", Font.PLAIN, 9));
                g2.setColor(new Color(200, 185, 255));
                FontMetrics fm2 = g2.getFontMetrics();
                g2.drawString(n.getLabel(), nx - fm2.stringWidth(n.getLabel()) / 2, ny + rad + 12);
            }
        }
    }

    private void drawPlayerMarker(Graphics2D g2) {
        if (currentCol < 0 || currentRow < 0) return;
        int nx = colX[currentCol], ny = rowY[currentRow];

        g2.setColor(new Color(100, 255, 150, 55));
        g2.fillOval(nx - 33, ny - 33, 66, 66);
        g2.setColor(new Color(80, 220, 120, 130));
        g2.setStroke(new BasicStroke(2.5f));
        g2.drawOval(nx - 33, ny - 33, 66, 66);
        g2.setStroke(new BasicStroke(1));

        int tip  = ny - 28;
        int base = tip - 13;
        int[] xp = {nx - 7, nx + 7, nx};
        int[] yp = {base, base, tip};
        g2.setColor(new Color(100, 255, 150));
        g2.fillPolygon(xp, yp, 3);

        g2.setFont(new Font("SansSerif", Font.BOLD, 10));
        g2.setColor(new Color(100, 255, 150));
        FontMetrics fm = g2.getFontMetrics();
        String lbl = "YOU";
        g2.drawString(lbl, nx - fm.stringWidth(lbl) / 2, base - 2);
    }

    private void drawReadOnlyOverlay(Graphics2D g2) {
        g2.setColor(new Color(0, 0, 0, 160));
        g2.fillRect(0, 0, getWidth(), 56);
        g2.setFont(new Font("Serif", Font.BOLD, 15));
        g2.setColor(new Color(255, 180, 60));
        String msg = "⚔  IN BATTLE — Map is view only  ⚔";
        FontMetrics fm = g2.getFontMetrics();
        g2.drawString(msg, (getWidth() - fm.stringWidth(msg)) / 2, 32);
        g2.setColor(new Color(255, 140, 30, 120));
        g2.setStroke(new BasicStroke(1.5f));
        g2.drawLine(0, 46, getWidth(), 46);
        g2.setStroke(new BasicStroke(1));
        setCursor(Cursor.getDefaultCursor());
    }

    private void drawHeader(Graphics2D g2) {
        g2.setFont(new Font("Serif", Font.BOLD, 18));
        g2.setColor(new Color(200, 170, 255));
        g2.drawString("✦  Choose Your Path  ✦", 290, 26);
        g2.setFont(new Font("SansSerif", Font.PLAIN, 11));
        g2.setColor(new Color(130, 115, 170));
        g2.drawString("Click a glowing node to proceed", 320, 42);
    }
}