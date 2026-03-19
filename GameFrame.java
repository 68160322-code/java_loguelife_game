import javax.swing.*;
import javax.swing.border.*;
import java.awt.*;
import java.awt.event.*;
import java.util.*;

/**
 * GameFrame — Main game window.
 * Changes vs original:
 *  - Integrated MapScreen: after each battle, player navigates the map
 *  - EventManager: EVENT nodes trigger random story events
 *  - Dark polished UI: custom card buttons, status bar, cleaner layout
 *  - Balance: easier enemy scaling, more gold, player starts with 80 HP
 */
public class GameFrame extends JFrame {

    // ── State ────────────────────────────────────────────────────────────────
    private GameState state;
    private ArrayList<Card> currentHand = new ArrayList<>();
    private MapScreen mapScreen;
    private JPanel mainPanel;
    private CardLayout cardLayout;
    private int lastMapCol = -1;   // ตำแหน่ง node ล่าสุดที่เลือก
    private int lastMapRow = -1;
    private boolean inBattle = false;

    // ── Battle UI ────────────────────────────────────────────────────────────
    private JTextArea    logArea;
    private JLabel       playerLabel, energyLabel;
    private JPanel       playerStatusPanel, handPanel;
    private HealthBar    playerHB, enemyHB;
    private EnemyPanel   enemyPanel;
    private JButton      endTurnBtn, viewDeckBtn, viewDiscardBtn;

    // ── Map UI ───────────────────────────────────────────────────────────────
    private JPanel mapWrapper;

    // ─────────────────────────────────────────────────────────────────────────
    public GameFrame() {
        state = new GameState();
        setupWindow();
        buildCardLayout();
        showMap();          // start on map
        setVisible(true);
    }

    // ── Window Setup ─────────────────────────────────────────────────────────
    private void setupWindow() {
        setTitle("Rogue Card Game");
        setSize(900, 640);
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setLocationRelativeTo(null);
        getContentPane().setBackground(new Color(10, 8, 20));
    }

    // ── CardLayout: map ↔ battle ──────────────────────────────────────────────
    private void buildCardLayout() {
        cardLayout = new CardLayout();
        mainPanel  = new JPanel(cardLayout);
        mainPanel.setBackground(new Color(10, 8, 20));

        // Map wrapper
        mapWrapper = buildMapWrapper();
        mainPanel.add(mapWrapper, "MAP");

        // Battle screen
        JPanel battleScreen = buildBattleScreen();
        mainPanel.add(battleScreen, "BATTLE");

        setContentPane(mainPanel);
    }

    // ── Map Wrapper ───────────────────────────────────────────────────────────
    private JPanel buildMapWrapper() {
        JPanel wrapper = new JPanel(new BorderLayout());
        wrapper.setBackground(new Color(10, 8, 20));

        // Header bar
        JPanel header = new JPanel(new BorderLayout());
        header.setBackground(new Color(15, 12, 28));
        header.setBorder(new EmptyBorder(8, 16, 8, 16));

        JLabel title = new JLabel("✦  ROGUE CARD  ✦");
        title.setFont(new Font("Serif", Font.BOLD, 20));
        title.setForeground(new Color(210, 180, 255));
        header.add(title, BorderLayout.WEST);

        // Center: stats
        JLabel statsLbl = new JLabel();
        statsLbl.setFont(new Font("SansSerif", Font.PLAIN, 12));
        statsLbl.setForeground(new Color(160, 145, 190));
        statsLbl.setName("MAP_STATS");
        statsLbl.setHorizontalAlignment(SwingConstants.CENTER);
        header.add(statsLbl, BorderLayout.CENTER);

        // Right: ปุ่ม Back (แสดงเฉพาะตอน readOnly)
        JButton backBtn = new JButton("◀  Back to Battle") {
            @Override protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g;
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(inBattle ? new Color(180, 80, 30) : new Color(40, 38, 55));
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), 8, 8);
                g2.setColor(inBattle ? new Color(230, 120, 50) : new Color(60, 58, 75));
                g2.drawRoundRect(1, 1, getWidth()-2, getHeight()-2, 8, 8);
                g2.setFont(getFont());
                g2.setColor(inBattle ? Color.WHITE : new Color(80, 78, 95));
                FontMetrics fm = g2.getFontMetrics();
                g2.drawString(getText(), (getWidth()-fm.stringWidth(getText()))/2,
                        (getHeight()+fm.getAscent()-fm.getDescent())/2);
            }
            @Override protected void paintBorder(Graphics g) {}
        };
        backBtn.setFont(new Font("SansSerif", Font.BOLD, 12));
        backBtn.setContentAreaFilled(false);
        backBtn.setFocusPainted(false);
        backBtn.setPreferredSize(new Dimension(160, 34));
        backBtn.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        backBtn.addActionListener(e -> {
            if (inBattle) cardLayout.show(mainPanel, "BATTLE");
        });
        backBtn.setName("BACK_BTN");
        header.add(backBtn, BorderLayout.EAST);

        wrapper.add(header, BorderLayout.NORTH);
        wrapper.add(new JPanel(), BorderLayout.CENTER);
        return wrapper;
    }

    private void showMap() {
        // Refresh header stats
        JPanel northPanel = (JPanel)((BorderLayout)mapWrapper.getLayout()).getLayoutComponent(BorderLayout.NORTH);
        for (Component c : northPanel.getComponents()) {
            if (c instanceof JLabel && "MAP_STATS".equals(((JLabel)c).getName())) {
                ((JLabel)c).setText("Level " + state.getLevel() + "  |  HP " + state.getPlayer().getHp()
                        + "/" + state.getPlayer().getMaxHp() + "  |  " + state.getGold() + "G");
            }
        }

        // ถ้า mapScreen ยังไม่มี หรือออกมาจาก battle ให้ reuse อันเดิม (ไม่ rebuild)
        if (mapScreen == null) {
            mapScreen = new MapScreen(node -> onNodeSelected(node));
            JPanel center = new JPanel(new BorderLayout());
            center.setBackground(new Color(10, 8, 20));
            center.add(mapScreen, BorderLayout.CENTER);
            mapWrapper.remove(((BorderLayout)mapWrapper.getLayout()).getLayoutComponent(BorderLayout.CENTER));
            mapWrapper.add(center, BorderLayout.CENTER);
        }

        // อัปเดตตำแหน่งและปลดล็อค
        mapScreen.setCurrentPosition(lastMapCol, lastMapRow);
        mapScreen.setReadOnly(false);
        mapWrapper.revalidate(); mapWrapper.repaint();

        inBattle = false;
        repaintMapHeader();
        cardLayout.show(mainPanel, "MAP");
    }

    /** เปิด map ระหว่างต่อสู้ — ดูได้อย่างเดียว */
    private void showMapReadOnly() {
        if (mapScreen == null) return;
        mapScreen.setCurrentPosition(lastMapCol, lastMapRow);
        mapScreen.setReadOnly(true);
        mapWrapper.revalidate(); mapWrapper.repaint();
        repaintMapHeader();
        cardLayout.show(mainPanel, "MAP");
    }

    private void repaintMapHeader() {
        JPanel northPanel = (JPanel)((BorderLayout)mapWrapper.getLayout()).getLayoutComponent(BorderLayout.NORTH);
        northPanel.repaint();
    }

    private void onNodeSelected(MapNode node) {
        // บันทึกตำแหน่งที่เลือก
        lastMapCol = node.getCol();
        lastMapRow = node.getRow();

        switch (node.getType()) {
            case REST:
                handleRestNode();
                break;
            case SHOP:
                ShopManager.openShop(this, state);
                showMap();
                break;
            case EVENT:
                EventManager.triggerEvent(this, state);
                showMap();
                break;
            case BATTLE:
            case ELITE:
            case BOSS:
                startBattle(node.getType());
                break;
        }
    }

    private void handleRestNode() {
        String[] opts = {"Rest (Heal 30%)", "Smith (Upgrade a card)", "Pray (Max HP +8)"};
        int c = JOptionPane.showOptionDialog(this, "You find a quiet campfire.", "Rest Site",
                JOptionPane.DEFAULT_OPTION, JOptionPane.PLAIN_MESSAGE, null, opts, opts[0]);
        if (c == 0) {
            state.getPlayer().healPercent(30);
            log("Rested — HP restored.");
        } else if (c == 1) {
            upgradeACard();
        } else if (c == 2) {
            state.getPlayer().increaseMaxHp(8);
            log("Max HP increased by 8.");
        }
        showMap();
    }

    private void upgradeACard() {
        ArrayList<Card> deck = state.getDeck().getMasterDeck();
        if (!deck.isEmpty()) {
            Card c = deck.get(new Random().nextInt(deck.size()));
            c.upgrade();
            JOptionPane.showMessageDialog(this, "Card upgraded: " + c.getName(), "Smith", JOptionPane.INFORMATION_MESSAGE);
        }
    }

    private void startBattle(MapNode.NodeType type) {
        inBattle = true;
        state.nextLevelByType(type);
        if (enemyPanel != null) enemyPanel.setEnemy(state.getEnemy());
        cardLayout.show(mainPanel, "BATTLE");
        startPlayerTurn();
    }

    // ── Battle Screen ─────────────────────────────────────────────────────────
    private JPanel buildBattleScreen() {
        JPanel screen = new JPanel(new BorderLayout(0, 0));
        screen.setBackground(new Color(12, 10, 22));

        // TOP: player info + enemy panel
        screen.add(buildTopBar(), BorderLayout.NORTH);

        // CENTER: log
        logArea = new JTextArea(4, 20);
        logArea.setEditable(false); logArea.setLineWrap(true); logArea.setWrapStyleWord(true);
        logArea.setBackground(new Color(16, 13, 28)); logArea.setForeground(new Color(200, 195, 220));
        logArea.setFont(new Font("SansSerif", Font.PLAIN, 12));
        logArea.setBorder(new EmptyBorder(6, 8, 6, 8));
        JScrollPane scroll = new JScrollPane(logArea);
        scroll.setBorder(BorderFactory.createMatteBorder(1, 0, 1, 0, new Color(50, 45, 70)));
        screen.add(scroll, BorderLayout.CENTER);

        // EAST: buttons
        screen.add(buildSideButtons(), BorderLayout.EAST);

        // SOUTH: hand
        screen.add(buildHandArea(), BorderLayout.SOUTH);

        return screen;
    }

    private JPanel buildTopBar() {
        JPanel top = new JPanel(new BorderLayout(8, 0));
        top.setBackground(new Color(12, 10, 22));
        top.setBorder(new EmptyBorder(6, 6, 6, 6));

        // Player panel (left)
        playerHB = new HealthBar(state.getPlayer().getMaxHp(), new Color(60, 200, 80));
        playerLabel = new JLabel();
        playerLabel.setForeground(new Color(210, 210, 230));
        playerLabel.setFont(new Font("SansSerif", Font.BOLD, 12));
        energyLabel = new JLabel();
        energyLabel.setFont(new Font("SansSerif", Font.BOLD, 14));
        energyLabel.setForeground(new Color(255, 225, 80));
        playerStatusPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 4, 2));
        playerStatusPanel.setOpaque(false);

        JPanel playerPanel = new JPanel();
        playerPanel.setLayout(new BoxLayout(playerPanel, BoxLayout.Y_AXIS));
        playerPanel.setBackground(new Color(16, 24, 16));
        playerPanel.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(60, 100, 60), 2),
                new EmptyBorder(6, 8, 6, 8)));
        playerPanel.add(playerLabel);
        playerPanel.add(Box.createRigidArea(new Dimension(0,3)));
        playerPanel.add(playerHB);
        playerPanel.add(Box.createRigidArea(new Dimension(0,3)));
        playerPanel.add(energyLabel);
        playerPanel.add(playerStatusPanel);
        playerPanel.setPreferredSize(new Dimension(220, 150));
        top.add(playerPanel, BorderLayout.WEST);

        // Enemy panel (center)
        enemyPanel = new EnemyPanel();
        enemyPanel.setEnemy(state.getEnemy());
        top.add(enemyPanel, BorderLayout.CENTER);

        return top;
    }

    private JPanel buildSideButtons() {
        endTurnBtn   = sideButton("End Turn",     new Color(160, 60, 60),  new Color(120, 40, 40));
        viewDeckBtn  = sideButton("View Deck",    new Color(50, 80, 130),  new Color(35, 60, 100));
        viewDiscardBtn = sideButton("Discard",    new Color(60, 60, 100),  new Color(40, 40, 80));
        JButton mapBtn = sideButton("View Map",    new Color(80, 50, 120),  new Color(60, 35, 95));

        endTurnBtn.addActionListener(e -> endTurn());
        viewDeckBtn.addActionListener(e ->
                JOptionPane.showMessageDialog(this, state.getDeck().viewDrawPile(), "Draw Pile", JOptionPane.INFORMATION_MESSAGE));
        viewDiscardBtn.addActionListener(e ->
                JOptionPane.showMessageDialog(this, state.getDeck().viewDiscardPile(), "Discard Pile", JOptionPane.INFORMATION_MESSAGE));
        mapBtn.addActionListener(e -> showMapReadOnly());  // ดูแผนที่อย่างเดียว

        JPanel panel = new JPanel(new GridLayout(4, 1, 4, 6));
        panel.setBackground(new Color(12, 10, 22));
        panel.setBorder(new EmptyBorder(6, 4, 6, 6));
        panel.add(endTurnBtn); panel.add(viewDeckBtn); panel.add(viewDiscardBtn); panel.add(mapBtn);
        panel.setPreferredSize(new Dimension(110, 160));
        return panel;
    }

    private JButton sideButton(String text, Color light, Color dark) {
        JButton btn = new JButton(text) {
            @Override protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g;
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                GradientPaint gp = new GradientPaint(0, 0, light, 0, getHeight(), dark);
                g2.setPaint(gp);
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), 8, 8);
                g2.setColor(light.brighter());
                g2.setStroke(new BasicStroke(1.2f));
                g2.drawRoundRect(1, 1, getWidth()-2, getHeight()-2, 8, 8);
                g2.setFont(getFont()); g2.setColor(Color.WHITE);
                FontMetrics fm = g2.getFontMetrics();
                g2.drawString(getText(), (getWidth()-fm.stringWidth(getText()))/2,
                        (getHeight()+fm.getAscent()-fm.getDescent())/2);
            }
            @Override protected void paintBorder(Graphics g) {}
        };
        btn.setFont(new Font("SansSerif", Font.BOLD, 12));
        btn.setContentAreaFilled(false); btn.setFocusPainted(false);
        btn.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        return btn;
    }

    private JPanel buildHandArea() {
        handPanel = new JPanel();
        handPanel.setBackground(new Color(14, 22, 14));
        handPanel.setLayout(new FlowLayout(FlowLayout.CENTER, 8, 6));

        JLabel handTitle = new JLabel("  Hand");
        handTitle.setFont(new Font("SansSerif", Font.BOLD, 11));
        handTitle.setForeground(new Color(140, 160, 140));

        JPanel wrapper = new JPanel(new BorderLayout());
        wrapper.setBackground(new Color(14, 22, 14));
        wrapper.setBorder(BorderFactory.createMatteBorder(2, 0, 0, 0, new Color(40, 65, 40)));
        wrapper.add(handTitle, BorderLayout.NORTH);
        wrapper.add(handPanel, BorderLayout.CENTER);
        wrapper.setPreferredSize(new Dimension(900, 185));
        return wrapper;
    }

    // ── Turn Logic ────────────────────────────────────────────────────────────
    private void startPlayerTurn() {
        state.getPlayer().resetEnergy();
        state.getPlayer().resetBlock();
        state.getEnemy().decideIntent();
        currentHand = state.getDeck().draw(5);
        renderHand();
        updateLabels();
    }

    private void renderHand() {
        handPanel.removeAll();
        for (Card card : currentHand) handPanel.add(buildCardButton(card));
        handPanel.revalidate(); handPanel.repaint();
    }

    /** Styled card button with gradient by type */
    private JButton buildCardButton(Card card) {
        Color[] palette = cardPalette(card.getType());
        Color top = palette[0], bot = palette[1], border = palette[2];

        JButton btn = new JButton() {
            @Override protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g;
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                GradientPaint gp = new GradientPaint(0, 0, top, 0, getHeight(), bot);
                g2.setPaint(gp); g2.fillRoundRect(0, 0, getWidth(), getHeight(), 12, 12);
                g2.setColor(border); g2.setStroke(new BasicStroke(2));
                g2.drawRoundRect(1, 1, getWidth()-2, getHeight()-2, 12, 12);
            }
            @Override protected void paintBorder(Graphics g) {}
        };
        btn.setLayout(new BoxLayout(btn, BoxLayout.Y_AXIS));
        btn.setPreferredSize(new Dimension(145, 155));
        btn.setContentAreaFilled(false); btn.setFocusPainted(false);
        btn.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));

        // Name
        JLabel nameL = new JLabel("<html><center>" + card.getName() + "</center></html>", SwingConstants.CENTER);
        nameL.setFont(new Font("Serif", Font.BOLD, 13));
        nameL.setForeground(Color.WHITE);
        nameL.setAlignmentX(Component.CENTER_ALIGNMENT);
        nameL.setBorder(new EmptyBorder(8, 4, 2, 4));

        // Type tag
        JLabel typeL = new JLabel(card.getType().toString(), SwingConstants.CENTER);
        typeL.setFont(new Font("SansSerif", Font.PLAIN, 10));
        typeL.setForeground(border.brighter());
        typeL.setAlignmentX(Component.CENTER_ALIGNMENT);

        // Divider
        JSeparator sep = new JSeparator();
        sep.setForeground(border);
        sep.setMaximumSize(new Dimension(120, 2));

        // Description
        JLabel descL = new JLabel("<html><center>" + card.getDescription() + "</center></html>", SwingConstants.CENTER);
        descL.setFont(new Font("SansSerif", Font.PLAIN, 10));
        descL.setForeground(new Color(220, 215, 235));
        descL.setAlignmentX(Component.CENTER_ALIGNMENT);
        descL.setBorder(new EmptyBorder(2, 6, 2, 6));

        // Cost
        JLabel costL = new JLabel("⚡ " + card.getCost(), SwingConstants.CENTER);
        costL.setFont(new Font("SansSerif", Font.BOLD, 12));
        costL.setForeground(new Color(255, 225, 80));
        costL.setAlignmentX(Component.CENTER_ALIGNMENT);
        costL.setBorder(new EmptyBorder(0, 0, 6, 0));

        btn.add(nameL); btn.add(typeL); btn.add(Box.createRigidArea(new Dimension(0,2)));
        btn.add(sep); btn.add(descL); btn.add(Box.createVerticalGlue()); btn.add(costL);

        // Hover
        btn.addMouseListener(new MouseAdapter() {
            @Override public void mouseEntered(MouseEvent e) { btn.setBorder(BorderFactory.createLineBorder(Color.WHITE, 2, true)); btn.repaint(); }
            @Override public void mouseExited(MouseEvent e)  { btn.setBorder(null); btn.repaint(); }
        });

        btn.addActionListener(e -> playCard(card));
        return btn;
    }

    private Color[] cardPalette(CardType type) {
        switch (type) {
            case ATTACK: return new Color[]{new Color(90,20,20), new Color(50,10,10), new Color(200,80,60)};
            case SKILL:  return new Color[]{new Color(20,40,90), new Color(10,20,55), new Color(80,130,210)};
            case HEAL:   return new Color[]{new Color(20,70,35), new Color(10,40,20), new Color(60,180,90)};
            default:     return new Color[]{new Color(40,40,60), new Color(20,20,40), new Color(120,120,160)};
        }
    }

    private void playCard(Card card) {
        if (!state.getPlayer().useEnergy(card.getCost())) { log("Not enough energy!"); return; }
        card.play(state.getPlayer(), state.getEnemy());
        if (card.isExhaust()) state.getDeck().exhaust(card);
        else                  state.getDeck().discard(card);
        currentHand.remove(card);
        renderHand();
        log("Played: " + card.getName());
        if (state.getEnemy().isDead()) { log("Enemy defeated!"); showRewardScreen(); return; }
        updateLabels();
    }

    private void endTurn() {
        for (Card c : currentHand) state.getDeck().discard(c);
        currentHand.clear(); handPanel.removeAll();
        enemyTurn();
        if (!state.getPlayer().isDead()) {
            state.getPlayer().reduceWeak();
            state.getEnemy().reduceWeak();
            startPlayerTurn();
        } else showGameOver();
    }

    private void enemyTurn() {
        Enemy e = state.getEnemy();
        if (e.getPoison() > 0) { e.applyPoison(); log("Poison ticks! Enemy takes damage."); }
        if (e.isDead()) { showRewardScreen(); return; }
        int dmg = e.attack();
        state.getPlayer().takeDamage(dmg);
        log(e.getName() + " attacks for " + dmg + " damage.");
        updateLabels();
    }

    // ── Reward / Game Over ────────────────────────────────────────────────────
    private void showRewardScreen() {
        // Pick card reward
        showCardRewardDialog();
        state.getPlayer().resetStrength();
        log("--- Level " + state.getLevel() + " cleared ---");
        // Return to map
        showMap();
    }

    private ArrayList<Card> getRewardPool() {
        ArrayList<Card> pool = new ArrayList<>();
        pool.add(new Card("Heavy Blade",    CardType.ATTACK).damage(20).cost(2));
        pool.add(new Card("Iron Wave",      CardType.ATTACK).damage(8).block(8).cost(1));
        pool.add(new Card("Twin Strike",    CardType.ATTACK).damage(6).multiHit().cost(1));
        pool.add(new Card("Uppercut",       CardType.ATTACK).damage(14).weak(1).cost(2));
        pool.add(new Card("Whirlwind",      CardType.ATTACK).energyBurst().cost(0));
        pool.add(new Card("Shrug It Off",   CardType.SKILL).block(12).cost(1));
        pool.add(new Card("Deadly Poison",  CardType.SKILL).poison(7).cost(1));
        pool.add(new Card("Battle Trance",  CardType.SKILL).strength(2).cost(1));
        pool.add(new Card("Fortify",        CardType.SKILL).block(20).cost(2));
        pool.add(new Card("Weaken",         CardType.SKILL).weak(3).cost(1));
        pool.add(new Card("Quick Heal",     CardType.HEAL).heal(8).cost(1));
        Collections.shuffle(pool);
        return new ArrayList<>(pool.subList(0, 3));
    }

    private void showCardRewardDialog() {
        ArrayList<Card> rewards = getRewardPool();

        // ── Outer panel ──────────────────────────────────────────────────────
        JPanel panel = new JPanel(new BorderLayout(10, 12));
        panel.setBackground(new Color(14, 12, 24));
        panel.setBorder(BorderFactory.createEmptyBorder(10, 14, 10, 14));

        JLabel title = new JLabel("✦  Choose a Card  ✦", SwingConstants.CENTER);
        title.setFont(new Font("Serif", Font.BOLD, 18));
        title.setForeground(new Color(210, 185, 255));
        title.setBorder(BorderFactory.createEmptyBorder(0, 0, 6, 0));
        panel.add(title, BorderLayout.NORTH);

        // ── Cards row ─────────────────────────────────────────────────────────
        JPanel cardsPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 16, 0));
        cardsPanel.setBackground(new Color(14, 12, 24));

        int[] selected = {0};   // index ที่เลือกอยู่
        java.util.List<JPanel> cardPanels = new java.util.ArrayList<>();

        for (int i = 0; i < rewards.size(); i++) {
            Card card = rewards.get(i);
            Color[] pal = cardPalette(card.getType());
            final int idx = i;

            JPanel cardPanel = new JPanel() {
                @Override protected void paintComponent(Graphics g) {
                    Graphics2D g2 = (Graphics2D) g;
                    g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                    // gradient background
                    GradientPaint gp = new GradientPaint(0, 0, pal[0], 0, getHeight(), pal[1]);
                    g2.setPaint(gp);
                    g2.fillRoundRect(0, 0, getWidth(), getHeight(), 14, 14);
                    // border — ขาวถ้าเลือกอยู่ ปกติถ้าไม่ได้เลือก
                    if (selected[0] == idx) {
                        g2.setColor(Color.WHITE);
                        g2.setStroke(new BasicStroke(3));
                    } else {
                        g2.setColor(pal[2]);
                        g2.setStroke(new BasicStroke(1.5f));
                    }
                    g2.drawRoundRect(1, 1, getWidth()-2, getHeight()-2, 14, 14);
                    g2.setStroke(new BasicStroke(1));
                }
            };
            cardPanel.setLayout(new BoxLayout(cardPanel, BoxLayout.Y_AXIS));
            cardPanel.setOpaque(false);
            cardPanel.setPreferredSize(new Dimension(160, 230));
            cardPanel.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));

            // ── ชื่อการ์ด ──
            JLabel nameL = new JLabel(card.getName(), SwingConstants.CENTER);
            nameL.setFont(new Font("Serif", Font.BOLD, 15));
            nameL.setForeground(Color.WHITE);
            nameL.setAlignmentX(Component.CENTER_ALIGNMENT);
            nameL.setBorder(BorderFactory.createEmptyBorder(12, 6, 2, 6));

            // ── Type tag ──
            JLabel typeL = new JLabel("[" + card.getType() + "]", SwingConstants.CENTER);
            typeL.setFont(new Font("SansSerif", Font.PLAIN, 11));
            typeL.setForeground(pal[2].brighter());
            typeL.setAlignmentX(Component.CENTER_ALIGNMENT);

            // ── เส้นคั่น ──
            JPanel divider = new JPanel();
            divider.setOpaque(false);
            divider.setMaximumSize(new Dimension(130, 1));
            divider.setPreferredSize(new Dimension(130, 1));
            divider.setBorder(BorderFactory.createMatteBorder(0, 0, 1, 0, pal[2]));
            divider.setAlignmentX(Component.CENTER_ALIGNMENT);

            // ── Description (แยกทีละบรรทัด) ──
            JLabel descL = new JLabel("<html><div style='text-align:center;width:130px'>"
                    + card.getDescription().replace("<b>", "<b>").replace("</b>", "</b>")
                    + "</div></html>", SwingConstants.CENTER);
            descL.setFont(new Font("SansSerif", Font.PLAIN, 11));
            descL.setForeground(new Color(220, 215, 240));
            descL.setAlignmentX(Component.CENTER_ALIGNMENT);
            descL.setBorder(BorderFactory.createEmptyBorder(6, 8, 4, 8));

            // ── Cost ──
            JLabel costL = new JLabel("⚡ " + card.getCost() + " Energy", SwingConstants.CENTER);
            costL.setFont(new Font("SansSerif", Font.BOLD, 12));
            costL.setForeground(new Color(255, 225, 70));
            costL.setAlignmentX(Component.CENTER_ALIGNMENT);
            costL.setBorder(BorderFactory.createEmptyBorder(0, 0, 12, 0));

            cardPanel.add(nameL);
            cardPanel.add(typeL);
            cardPanel.add(Box.createRigidArea(new Dimension(0, 6)));
            cardPanel.add(divider);
            cardPanel.add(descL);
            cardPanel.add(Box.createVerticalGlue());
            cardPanel.add(costL);

            // click เพื่อเลือก
            cardPanel.addMouseListener(new MouseAdapter() {
                @Override public void mouseClicked(MouseEvent e) {
                    selected[0] = idx;
                    for (JPanel cp : cardPanels) cp.repaint();
                }
                @Override public void mouseEntered(MouseEvent e) { cardPanel.repaint(); }
                @Override public void mouseExited(MouseEvent e)  { cardPanel.repaint(); }
            });

            cardPanels.add(cardPanel);
            cardsPanel.add(cardPanel);
        }
        panel.add(cardsPanel, BorderLayout.CENTER);

        JLabel hint = new JLabel("Click a card to select it, then click OK", SwingConstants.CENTER);
        hint.setForeground(new Color(130, 120, 160));
        hint.setFont(new Font("SansSerif", Font.PLAIN, 11));
        hint.setBorder(BorderFactory.createEmptyBorder(6, 0, 0, 0));
        panel.add(hint, BorderLayout.SOUTH);

        int result = JOptionPane.showConfirmDialog(this, panel, "Card Reward",
                JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE);
        if (result == JOptionPane.OK_OPTION) {
            Card chosen = rewards.get(selected[0]);
            state.getDeck().addToMasterDeck(chosen);
            log("Added \"" + chosen.getName() + "\" to deck.");
        }
    }

    private void showGameOver() {
        JOptionPane.showMessageDialog(this,
                "GAME OVER\nYou reached Level " + state.getLevel(), "Defeated", JOptionPane.ERROR_MESSAGE);
        System.exit(0);
    }

    // ── UI Helpers ────────────────────────────────────────────────────────────
    private void updateLabels() {
        if (state == null) return;
        Player p = state.getPlayer();
        playerLabel.setText("HP: " + p.getHp() + "/" + p.getMaxHp() + "  |  Gold: " + state.getGold() + "G  |  Lv." + state.getLevel());
        energyLabel.setText("⚡ " + p.getEnergy() + " Energy");
        if (enemyPanel != null) { enemyPanel.setEnemy(state.getEnemy()); enemyPanel.refreshStatus(); }
        playerHB.updateHealth(p.getHp(), p.getMaxHp());

        playerStatusPanel.removeAll();
        if (p.getBlock() > 0)    playerStatusPanel.add(statusBox("[DEF] "+p.getBlock(),    new Color(80,140,210)));
        if (p.getStrength() > 0) playerStatusPanel.add(statusBox("[STR] "+p.getStrength(), new Color(200,120,30)));
        if (p.getWeak() > 0)     playerStatusPanel.add(statusBox("[WEK] "+p.getWeak(),     new Color(160,60,200)));
        for (Relic r : state.getPlayer().getRelics())
            playerStatusPanel.add(statusBox("✦"+r.getName(), new Color(180,160,40)));
        playerStatusPanel.revalidate(); playerStatusPanel.repaint();
    }

    private JLabel statusBox(String text, Color bg) {
        JLabel lbl = new JLabel(" " + text + " ");
        lbl.setFont(new Font("SansSerif", Font.BOLD, 10));
        lbl.setForeground(Color.WHITE);
        lbl.setBackground(bg);
        lbl.setOpaque(true);
        lbl.setBorder(BorderFactory.createLineBorder(bg.brighter(), 1));
        return lbl;
    }

    private void log(String text) {
        if (logArea == null) return;
        logArea.append(text + "\n");
        logArea.setCaretPosition(logArea.getDocument().getLength());
    }
}