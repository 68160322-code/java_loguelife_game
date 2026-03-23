package core;

import card.Card;
import card.CardType;
import card.Deck;
import entity.Enemy;
import entity.Player;
import event.EventManager;
import event.ShopManager;
import item.Relic;
import map.MapNode;
import map.MapScreen;
import ui.EnemyPanel;
import ui.HealthBar;
import ui.PlayerModel;

import javax.swing.*;
import javax.swing.Timer;
import javax.swing.border.*;
import java.awt.*;
import java.awt.event.*;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Random;

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
    private int lastMapCol = -1;
    private int lastMapRow = -1;
    private boolean inBattle = false;
    private boolean isLoadedGame = false;

    // ── Battle UI ────────────────────────────────────────────────────────────
    private JTextArea    logArea;
    private JLabel           playerLabel, energyLabel;
    private JPanel           playerStatusPanel, handPanel;
    private HealthBar        playerHB;
    private EnemyPanel       enemyPanel;
    private PlayerModel      playerModel;
    private JButton      endTurnBtn, viewDeckBtn, viewDiscardBtn;

    // ── Map UI ───────────────────────────────────────────────────────────────
    private JPanel mapWrapper;

    // ─────────────────────────────────────────────────────────────────────────
    public GameFrame() {
        state = new GameState();
        setupWindow();
        // เลือก class ก่อนเริ่ม
        PlayerClass chosen = showClassSelectDialog();
        state.getPlayer().setPlayerClass(chosen);
        // apply class rules ให้ deck (Knight: poison skill → POISON type)
        if (chosen != null) chosen.applyClassRules(state.getDeck().getMasterDeck());
        buildCardLayout();
        showMap();
        setVisible(true);
        addWindowListener(new java.awt.event.WindowAdapter() {
            @Override public void windowClosing(java.awt.event.WindowEvent e) {
                SaveManager.save(state, mapScreen, lastMapCol, lastMapRow);
            }
        });
    }

    /** Constructor สำหรับโหลด save */
    public GameFrame(GameState loadedState) {
        state = loadedState;
        // โหลดตำแหน่ง map ที่บันทึกไว้
        lastMapCol = SaveManager.loadMapCol();
        lastMapRow = SaveManager.loadMapRow();
        isLoadedGame = true;
        setupWindow();
        buildCardLayout();
        showMap();
        setVisible(true);
        addWindowListener(new java.awt.event.WindowAdapter() {
            @Override public void windowClosing(java.awt.event.WindowEvent e) {
                SaveManager.save(state, mapScreen, lastMapCol, lastMapRow);
            }
        });
    }

    // ── Window Setup ─────────────────────────────────────────────────────────
    private void setupWindow() {
        setTitle("Rogue Card Game");
        setMinimumSize(new Dimension(800, 600));
        setSize(1000, 720);
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setLocationRelativeTo(null);
        setResizable(true);
        getContentPane().setBackground(new Color(10, 8, 20));

        // ESC เปิด pause menu
        KeyStroke esc = KeyStroke.getKeyStroke(KeyEvent.VK_ESCAPE, 0);
        getRootPane().getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW).put(esc, "pause");
        getRootPane().getActionMap().put("pause", new AbstractAction() {
            @Override public void actionPerformed(ActionEvent e) { openPauseMenu(); }
        });
    }

    private void openPauseMenu() {
        AudioManager.pauseMusic();
        PauseMenu menu = new PauseMenu(this);
        menu.setVisible(true);

        switch (menu.getResult()) {
            case RESUME:
                AudioManager.resumeMusic();
                break;
            case RESTART:
                AudioManager.stopMusic();
                dispose();
                new GameFrame();
                break;
            case QUIT:
                AudioManager.stopMusic();
                dispose();
                new MainMenu().setVisible(true); // กลับหน้าหลัก
                break;
        }
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
        title.setFont(new Font("SansSerif", Font.BOLD, 20));
        title.setForeground(new Color(210, 180, 255));
        header.add(title, BorderLayout.WEST);

        // Center: stats
        JLabel statsLbl = new JLabel();
        statsLbl.setFont(new Font("SansSerif", Font.PLAIN, 12));
        statsLbl.setForeground(new Color(160, 145, 190));
        statsLbl.setName("MAP_STATS");
        statsLbl.setHorizontalAlignment(SwingConstants.CENTER);
        header.add(statsLbl, BorderLayout.CENTER);

        // Right: Back to Battle + Pause
        JPanel rightPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 6, 0));
        rightPanel.setOpaque(false);

        JButton backBtn = new JButton("◀  Return") {
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
        backBtn.setContentAreaFilled(false); backBtn.setFocusPainted(false);
        backBtn.setPreferredSize(new Dimension(160, 34));
        backBtn.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        backBtn.addActionListener(e -> { if (inBattle) cardLayout.show(mainPanel, "BATTLE"); });
        backBtn.setName("BACK_BTN");

        JButton pauseMapBtn = sideButton("||", new Color(50, 50, 75), new Color(35, 35, 55));
        pauseMapBtn.setPreferredSize(new Dimension(40, 34));
        pauseMapBtn.setFont(new Font("SansSerif", Font.BOLD, 11));
        pauseMapBtn.addActionListener(e -> openPauseMenu());

        rightPanel.add(backBtn);
        rightPanel.add(pauseMapBtn);
        header.add(rightPanel, BorderLayout.EAST);

        wrapper.add(header, BorderLayout.NORTH);
        wrapper.add(new JPanel(), BorderLayout.CENTER);
        return wrapper;
    }

    private void showMap() {
        // Refresh header stats
        JPanel northPanel = (JPanel)((BorderLayout)mapWrapper.getLayout()).getLayoutComponent(BorderLayout.NORTH);
        for (Component c : northPanel.getComponents()) {
            if (c instanceof JLabel && "MAP_STATS".equals(((JLabel)c).getName())) {
                ((JLabel)c).setText("2620 Lv." + state.getLevel() + "  |  HP " + state.getPlayer().getHp()
                        + "/" + state.getPlayer().getMaxHp() + "  |  " + state.getGold() + "G");
            }
        }

        // ถ้า mapScreen ยังไม่มี ให้สร้างใหม่
        if (mapScreen == null) {
            mapScreen = new MapScreen(node -> onNodeSelected(node));
            JPanel center = new JPanel(new BorderLayout());
            center.setBackground(new Color(10, 8, 20));
            center.add(mapScreen, BorderLayout.CENTER);
            mapWrapper.remove(((BorderLayout)mapWrapper.getLayout()).getLayoutComponent(BorderLayout.CENTER));
            mapWrapper.add(center, BorderLayout.CENTER);

            // ถ้าเป็นการโหลด save — restore node states และตำแหน่งผู้เล่น
            if (isLoadedGame) {
                SaveManager.restoreMapState(mapScreen);
                isLoadedGame = false;
            }
        }

        // อัปเดตตำแหน่งและปลดล็อค
        mapScreen.setCurrentPosition(lastMapCol, lastMapRow);
        mapScreen.setReadOnly(false);
        mapWrapper.revalidate(); mapWrapper.repaint();

        inBattle = false;
        repaintMapHeader();
        AudioManager.playMusic(AudioManager.Track.MAP);
        SaveManager.save(state, mapScreen, lastMapCol, lastMapRow);
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
        String[] opts = {"Rest — Recover 30% HP", "The Forge — Upgrade a Card", "Pray — Max HP +8"};
        int c = JOptionPane.showOptionDialog(this, "A dying campfire flickers in the dark.", "Respite",
                JOptionPane.DEFAULT_OPTION, JOptionPane.PLAIN_MESSAGE, null, opts, opts[0]);
        if (c == 0) {
            state.getPlayer().healPercent(30);
            log("You rest your weary bones. HP restored.");
        } else if (c == 1) {
            upgradeACard();
        } else if (c == 2) {
            state.getPlayer().increaseMaxHp(8);
            log("The prayers harden your will. Max HP +8");
        }
        showMap();
    }

    private void upgradeACard() {
        ArrayList<Card> deck = state.getDeck().getMasterDeck();
        if (!deck.isEmpty()) {
            Card c = deck.get(new Random().nextInt(deck.size()));
            c.upgrade();
            JOptionPane.showMessageDialog(this, "Forged: " + c.getName(), "The Forge", JOptionPane.INFORMATION_MESSAGE);
        }
    }

    private MapNode.NodeType lastBattleType = MapNode.NodeType.BATTLE;

    private void startBattle(MapNode.NodeType type) {
        inBattle = true;
        lastBattleType = type;
        state.nextLevelByType(type);
        if (enemyPanel != null) enemyPanel.setEnemy(state.getEnemy());
        // reset ก่อน แล้วค่อย apply relic — ลำดับสำคัญมาก
        state.getPlayer().resetBattleState();
        state.getPlayer().applyBattleStartRelics();
        if (type == MapNode.NodeType.BOSS || type == MapNode.NodeType.ELITE)
            AudioManager.playMusic(AudioManager.Track.BOSS);
        else
            AudioManager.playMusic(AudioManager.Track.BATTLE);
        cardLayout.show(mainPanel, "BATTLE");
        startPlayerTurn();
    }

    // ── Battle Screen — Layout B: Enemy บน, Player ล่าง ─────────────────────
    private JPanel buildBattleScreen() {
        JPanel screen = new JPanel(new BorderLayout(0, 0));
        screen.setBackground(new Color(12, 10, 22));

        // NORTH: Enemy panel (full width)
        screen.add(buildEnemyBar(), BorderLayout.NORTH);

        // CENTER: Player panel + log — log ความสูงตายตัว
        JPanel middle = new JPanel(new BorderLayout(0, 0));
        middle.setBackground(new Color(12, 10, 22));

        logArea = new JTextArea(2, 20);
        logArea.setEditable(false); logArea.setLineWrap(true); logArea.setWrapStyleWord(true);
        logArea.setBackground(new Color(16, 13, 28)); logArea.setForeground(new Color(180, 175, 205));
        logArea.setFont(new Font("SansSerif", Font.PLAIN, 11));
        logArea.setBorder(new EmptyBorder(4, 8, 4, 8));
        JScrollPane scroll = new JScrollPane(logArea);
        scroll.setBorder(BorderFactory.createMatteBorder(1, 0, 0, 0, new Color(50, 45, 70)));
        scroll.setPreferredSize(new Dimension(0, 52));

        middle.add(buildPlayerBar(), BorderLayout.NORTH);
        middle.add(scroll,           BorderLayout.SOUTH);

        screen.add(middle, BorderLayout.CENTER);
        screen.add(buildSideButtons(), BorderLayout.EAST);
        screen.add(buildHandArea(), BorderLayout.SOUTH);
        return screen;
    }

    /** Enemy bar — full width, สูงพอให้ monster แสดงได้ */
    private JPanel buildEnemyBar() {
        enemyPanel = new EnemyPanel();
        enemyPanel.setEnemy(state.getEnemy());
        enemyPanel.setPreferredSize(new Dimension(0, 280));
        return enemyPanel;
    }

    /** Player bar — full width, stats ซ้าย + model ขวา */
    private JPanel buildPlayerBar() {
        JPanel bar = new JPanel(new BorderLayout(8, 0));
        bar.setBackground(new Color(14, 12, 24));
        bar.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createMatteBorder(1, 0, 1, 0, new Color(55, 50, 80)),
                new EmptyBorder(6, 8, 6, 8)));
        bar.setPreferredSize(new Dimension(0, 130));

        // ── Stats (LEFT) ──────────────────────────────────────────────────────
        playerHB = new HealthBar(state.getPlayer().getMaxHp(), new Color(60, 200, 80));
        playerHB.setPreferredSize(new Dimension(220, 12));
        playerLabel = new JLabel();
        playerLabel.setForeground(new Color(170, 165, 195));
        playerLabel.setFont(new Font("SansSerif", Font.PLAIN, 11));
        energyLabel = new JLabel();
        energyLabel.setFont(new Font("SansSerif", Font.BOLD, 12));
        energyLabel.setForeground(new Color(255, 225, 80));
        playerStatusPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 4, 0));
        playerStatusPanel.setOpaque(false);

        JPanel statsPanel = new JPanel();
        statsPanel.setLayout(new BoxLayout(statsPanel, BoxLayout.Y_AXIS));
        statsPanel.setOpaque(false);
        statsPanel.add(playerLabel);
        statsPanel.add(Box.createRigidArea(new Dimension(0, 4)));
        statsPanel.add(playerHB);
        statsPanel.add(Box.createRigidArea(new Dimension(0, 4)));
        statsPanel.add(energyLabel);
        statsPanel.add(playerStatusPanel);
        bar.add(statsPanel, BorderLayout.CENTER);

        // ── Player Model (RIGHT) ──────────────────────────────────────────────
        playerModel = new PlayerModel();
        playerModel.setPreferredSize(new Dimension(130, 118));
        bar.add(playerModel, BorderLayout.EAST);

        return bar;
    }

    // ไม่ใช้แล้ว — แทนด้วย buildEnemyBar + buildPlayerBar
    private JPanel buildTopBar() { return new JPanel(); }

    private JPanel buildSideButtons() {
        endTurnBtn     = sideButton("End Turn",  new Color(160, 60, 60),  new Color(120, 40, 40));
        viewDeckBtn    = sideButton("Spellbook", new Color(50, 80, 130),  new Color(35, 60, 100));
        viewDiscardBtn = sideButton("Discard",   new Color(60, 60, 100),  new Color(40, 40, 80));
        JButton mapBtn   = sideButton("The Map",  new Color(80, 50, 120),  new Color(60, 35, 95));
        JButton pauseBtn = sideButton("❚❚ Pause",  new Color(50, 50, 75),   new Color(35, 35, 55));

        endTurnBtn.addActionListener(e -> endTurn());
        viewDeckBtn.addActionListener(e ->
                JOptionPane.showMessageDialog(this, state.getDeck().viewDrawPile(), "Grimoire", JOptionPane.INFORMATION_MESSAGE));
        viewDiscardBtn.addActionListener(e ->
                JOptionPane.showMessageDialog(this, state.getDeck().viewDiscardPile(), "Discard", JOptionPane.INFORMATION_MESSAGE));
        mapBtn.addActionListener(e -> showMapReadOnly());
        pauseBtn.addActionListener(e -> openPauseMenu());

        JPanel panel = new JPanel(new GridLayout(5, 1, 4, 6));
        panel.setBackground(new Color(12, 10, 22));
        panel.setBorder(new EmptyBorder(8, 4, 8, 6));
        panel.add(endTurnBtn); panel.add(viewDeckBtn);
        panel.add(viewDiscardBtn); panel.add(mapBtn); panel.add(pauseBtn);
        panel.setPreferredSize(new Dimension(115, 0));
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
        wrapper.setPreferredSize(new Dimension(0, 215));
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
        Color[] palette  = cardPalette(card.getType());
        Color top = palette[0], bot = palette[1];
        // border สีตาม rarity (ไม่ใช่แค่ type)
        Color rarityColor = card.getRarity().color();

        JButton btn = new JButton() {
            @Override protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g;
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                GradientPaint gp = new GradientPaint(0, 0, top, 0, getHeight(), bot);
                g2.setPaint(gp); g2.fillRoundRect(0, 0, getWidth(), getHeight(), 12, 12);
                g2.setColor(rarityColor); g2.setStroke(new BasicStroke(2));
                g2.drawRoundRect(1, 1, getWidth()-2, getHeight()-2, 12, 12);
            }
            @Override protected void paintBorder(Graphics g) {}
        };
        btn.setLayout(new BoxLayout(btn, BoxLayout.Y_AXIS));
        btn.setPreferredSize(new Dimension(148, 175));
        btn.setContentAreaFilled(false); btn.setFocusPainted(false);
        btn.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));

        // Name
        JLabel nameL = new JLabel("<html><center>" + card.getName() + "</center></html>", SwingConstants.CENTER);
        nameL.setFont(new Font("SansSerif", Font.BOLD, 13));
        nameL.setForeground(Color.WHITE);
        nameL.setAlignmentX(Component.CENTER_ALIGNMENT);
        nameL.setBorder(new EmptyBorder(8, 4, 0, 4));

        // Rarity badge
        JLabel rarityL = new JLabel("◆ " + card.getRarity().label(), SwingConstants.CENTER);
        rarityL.setFont(new Font("SansSerif", Font.BOLD, 9));
        rarityL.setForeground(rarityColor);
        rarityL.setAlignmentX(Component.CENTER_ALIGNMENT);

        // Type tag
        JLabel typeL = new JLabel(card.getType().toString(), SwingConstants.CENTER);
        typeL.setFont(new Font("SansSerif", Font.PLAIN, 10));
        typeL.setForeground(new Color(180, 175, 200));
        typeL.setAlignmentX(Component.CENTER_ALIGNMENT);

        // Divider สีตาม rarity
        JSeparator sep = new JSeparator();
        sep.setForeground(rarityColor);
        sep.setMaximumSize(new Dimension(120, 1));

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

        btn.add(nameL); btn.add(rarityL); btn.add(typeL);
        btn.add(Box.createRigidArea(new Dimension(0, 2)));
        btn.add(sep); btn.add(descL);
        btn.add(Box.createVerticalGlue()); btn.add(costL);

        btn.addMouseListener(new MouseAdapter() {
            @Override public void mouseEntered(MouseEvent e) { btn.setBorder(BorderFactory.createLineBorder(Color.WHITE, 2, true)); btn.repaint(); }
            @Override public void mouseExited(MouseEvent e)  { btn.setBorder(null); btn.repaint(); }
        });
        btn.addActionListener(e -> playCard(card));
        return btn;
    }

    private Color[] cardPalette(CardType type) {
        switch (type) {
            case ATTACK: return new Color[]{new Color(90,20,20),  new Color(50,10,10),  new Color(200,80,60)};
            case SKILL:  return new Color[]{new Color(20,40,90),  new Color(10,20,55),  new Color(80,130,210)};
            case HEAL:   return new Color[]{new Color(20,70,35),  new Color(10,40,20),  new Color(60,180,90)};
            case POISON: return new Color[]{new Color(30,40,20),  new Color(18,25,12),  new Color(90,100,50)}; // มืดและจาง — ใช้ไม่ได้
            default:     return new Color[]{new Color(40,40,60),  new Color(20,20,40),  new Color(120,120,160)};
        }
    }

    /** Dialog เลือก class ก่อนเริ่มเกม */
    private PlayerClass showClassSelectDialog() {
        JPanel panel = new JPanel(new BorderLayout(0, 14));
        panel.setBackground(new Color(10, 8, 20));
        panel.setBorder(BorderFactory.createEmptyBorder(20, 28, 20, 28));

        JLabel title = new JLabel("Choose Your Class", SwingConstants.CENTER);
        title.setFont(new Font("Serif", Font.BOLD, 22));
        title.setForeground(new Color(210, 185, 255));
        panel.add(title, BorderLayout.NORTH);

        JPanel cards = new JPanel(new GridLayout(1, PlayerClass.values().length, 16, 0));
        cards.setBackground(new Color(10, 8, 20));

        int[] selected = {0};
        java.util.List<JPanel> classPanels = new java.util.ArrayList<>();

        for (int i = 0; i < PlayerClass.values().length; i++) {
            PlayerClass pc = PlayerClass.values()[i];
            final int idx = i;
            Color c = pc.color;

            JPanel cp = new JPanel(new BorderLayout(0, 6)) {
                @Override protected void paintComponent(Graphics g) {
                    Graphics2D g2 = (Graphics2D) g;
                    g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                    g2.setColor(new Color(18, 16, 32));
                    g2.fillRoundRect(0, 0, getWidth(), getHeight(), 12, 12);
                    Color border = selected[0] == idx ? Color.WHITE : c.darker();
                    g2.setColor(border);
                    g2.setStroke(new BasicStroke(selected[0] == idx ? 2.5f : 1.5f));
                    g2.drawRoundRect(1, 1, getWidth()-2, getHeight()-2, 12, 12);
                }
            };
            cp.setOpaque(false);
            cp.setPreferredSize(new Dimension(200, 220));
            cp.setBorder(BorderFactory.createEmptyBorder(14, 14, 14, 14));
            cp.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));

            JLabel nameL = new JLabel(pc.name, SwingConstants.CENTER);
            nameL.setFont(new Font("Serif", Font.BOLD, 18));
            nameL.setForeground(c.brighter());

            JLabel titleL = new JLabel(pc.title, SwingConstants.CENTER);
            titleL.setFont(new Font("SansSerif", Font.ITALIC, 12));
            titleL.setForeground(c);

            JLabel descL = new JLabel("<html><center>" + pc.description.replace("\n", "<br>") + "</center></html>", SwingConstants.CENTER);
            descL.setFont(new Font("SansSerif", Font.PLAIN, 11));
            descL.setForeground(new Color(190, 180, 215));

            JPanel top = new JPanel(new BorderLayout(0, 4));
            top.setOpaque(false);
            top.add(nameL, BorderLayout.NORTH);
            top.add(titleL, BorderLayout.SOUTH);

            cp.add(top,   BorderLayout.NORTH);
            cp.add(descL, BorderLayout.CENTER);

            cp.addMouseListener(new MouseAdapter() {
                @Override public void mouseClicked(MouseEvent e) {
                    if (pc == PlayerClass.ROGUE) return; // ยังไม่เปิด
                    selected[0] = idx;
                    for (JPanel p : classPanels) p.repaint();
                }
            });

            // Rogue ยังไม่เปิด — dim และ cursor ปกติ
            if (pc == PlayerClass.ROGUE) {
                cp.setCursor(Cursor.getDefaultCursor());
                nameL.setForeground(new Color(80, 70, 100));
                titleL.setForeground(new Color(70, 60, 90));
                descL.setForeground(new Color(80, 70, 100));
                JLabel comingSoon = new JLabel("[ Coming Soon ]", SwingConstants.CENTER);
                comingSoon.setFont(new Font("SansSerif", Font.ITALIC, 11));
                comingSoon.setForeground(new Color(100, 90, 120));
                cp.add(comingSoon, BorderLayout.SOUTH);
            }

            classPanels.add(cp);
            cards.add(cp);
        }
        panel.add(cards, BorderLayout.CENTER);

        JLabel hint = new JLabel("Select a class and click OK", SwingConstants.CENTER);
        hint.setForeground(new Color(120, 110, 150));
        hint.setFont(new Font("SansSerif", Font.PLAIN, 11));
        panel.add(hint, BorderLayout.SOUTH);

        int result = JOptionPane.showConfirmDialog(null, panel, "Class Selection",
                JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE);
        if (result == JOptionPane.OK_OPTION)
            return PlayerClass.values()[selected[0]];
        return PlayerClass.KNIGHT; // default
    }

    private void playCard(Card card) {
        // Knight: ไม่สามารถเล่นการ์ด POISON ได้
        if (card.getType() == CardType.POISON) {
            log("The Iron Vow prevents use of poison arts!");
            return;
        }
        if (!state.getPlayer().useEnergy(card.getCost())) { log("Not enough mana!"); return; }
        card.play(state.getPlayer(), state.getEnemy());

        // Relic: Blood Stone — Attack card ดีลโบนัส +3
        if (card.getType() == CardType.ATTACK) {
            int bonus = state.getPlayer().getAttackBonus();
            if (bonus > 0 && !state.getEnemy().isDead()) {
                state.getEnemy().takeDamage(bonus);
            }
            state.getPlayer().onAttackPlayed(); // Vampiric Fang
            if (enemyPanel != null) enemyPanel.playHurtAnim();
            if (playerModel != null) playerModel.playAttack(); // ผู้เล่นโจมตี
        }

        // Heal card — flash สีเขียว
        if (card.getType() == CardType.HEAL && playerModel != null) {
            playerModel.playHeal(card.getHeal());
        }

        // Skill card ที่มี block — flash สีน้ำเงิน
        if (card.getType() == CardType.SKILL && card.getBlock() > 0 && playerModel != null) {
            playerModel.playBlock(card.getBlock());
        }

        // Relic: Poison Vial
        if (state.getPlayer().hasRelic(item.Relic.RelicType.POISON_VIAL)
                && state.getEnemy().getPoison() > 0) {
            state.getEnemy().addPoison(2);
        }

        if (card.isExhaust()) state.getDeck().exhaust(card);
        else                  state.getDeck().discard(card);
        currentHand.remove(card);
        renderHand();
        log("Cast: " + card.getName());
        if (state.getEnemy().isDead()) {
            if (enemyPanel != null) enemyPanel.playDieAnim();
            state.getPlayer().onEnemyDefeated();
            log("Foe slain!");
            Timer t = new Timer(600, ev -> showRewardScreen());
            t.setRepeats(false); t.start();
            return;
        }
        updateLabels();
    }

    private void endTurn() {
        // Relic: Heart of Storm — deal 3 poison to enemy at end of each turn
        if (state.getPlayer().hasRelic(item.Relic.RelicType.HEART_OF_STORM)) {
            state.getEnemy().addPoison(3);
            log("Heart of Storm: Venomous tendrils coil around the foe!");
        }
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
        if (e.getPoison() > 0) { e.applyPoison(); log("Poison festers! Foe writhes in agony."); }
        if (e.isDead()) { showRewardScreen(); return; }
        int dmg = e.attack();
        if (dmg > 0 && enemyPanel != null) enemyPanel.playAttackAnim();
        if (dmg > 0 && playerModel != null) playerModel.playHurt(); // ผู้เล่นโดนตี
        state.getPlayer().takeDamage(dmg);
        log(e.getName() + " attacks for " + dmg + " damage.");
        updateLabels();
    }

    // ── Reward / Game Over ────────────────────────────────────────────────────
    private void showRewardScreen() {
        showCardRewardDialog();
        state.getPlayer().resetStrength();
        state.getPlayer().forceResetBlock(); // reset block หลังจบ battle เสมอ
        log("--- Level " + state.getLevel() + " cleared ---");

        if (lastBattleType == MapNode.NodeType.BOSS) {
            showYouWin();
        } else {
            showMap();
        }
    }

    private void showYouWin() {
        SaveManager.deleteSave();
        JPanel panel = new JPanel(new BorderLayout(0, 16));
        panel.setBackground(new Color(10, 8, 20));
        panel.setBorder(BorderFactory.createEmptyBorder(24, 36, 24, 36));

        JLabel titleL = new JLabel("✦  VICTORY  ✦", SwingConstants.CENTER);
        titleL.setFont(new Font("SansSerif", Font.BOLD, 36));
        titleL.setForeground(new Color(255, 210, 60));
        panel.add(titleL, BorderLayout.NORTH);

        JPanel stats = new JPanel(new GridLayout(3, 1, 0, 8));
        stats.setBackground(new Color(10, 8, 20));
        stats.setBorder(BorderFactory.createEmptyBorder(16, 0, 16, 0));
        stats.add(statLine("Depths Cleared", String.valueOf(state.getLevel())));
        stats.add(statLine("Remaining Vitality",   state.getPlayer().getHp() + " / " + state.getPlayer().getMaxHp()));
        stats.add(statLine("Gold Plundered", state.getGold() + "G"));
        panel.add(stats, BorderLayout.CENTER);

        JLabel subL = new JLabel("The dungeon falls silent. You have prevailed.", SwingConstants.CENTER);
        subL.setFont(new Font("SansSerif", Font.ITALIC, 14));
        subL.setForeground(new Color(160, 150, 200));
        panel.add(subL, BorderLayout.SOUTH);

        JOptionPane.showMessageDialog(this, panel, "Thy Legend Endures!", JOptionPane.PLAIN_MESSAGE);
        System.exit(0);
    }

    private JPanel statLine(String label, String value) {
        JPanel row = new JPanel(new BorderLayout());
        row.setBackground(new Color(18, 15, 30));
        row.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(50, 45, 70), 1),
                BorderFactory.createEmptyBorder(6, 12, 6, 12)));
        JLabel lbl = new JLabel(label);
        lbl.setForeground(new Color(160, 150, 190));
        lbl.setFont(new Font("SansSerif", Font.PLAIN, 13));
        JLabel val = new JLabel(value, SwingConstants.RIGHT);
        val.setForeground(new Color(255, 220, 80));
        val.setFont(new Font("SansSerif", Font.BOLD, 13));
        row.add(lbl, BorderLayout.WEST);
        row.add(val, BorderLayout.EAST);
        return row;
    }

    private ArrayList<Card> getRewardPool() {
        return card.CardLibrary.getRewardPool(state.getLevel());
    }

    private void showCardRewardDialog() {
        ArrayList<Card> rewards = getRewardPool();

        // ── Outer panel ──────────────────────────────────────────────────────
        JPanel panel = new JPanel(new BorderLayout(10, 12));
        panel.setBackground(new Color(14, 12, 24));
        panel.setBorder(BorderFactory.createEmptyBorder(10, 14, 10, 14));

        JLabel title = new JLabel("✦  Choose Your Card  ✦", SwingConstants.CENTER);
        title.setFont(new Font("SansSerif", Font.BOLD, 18));
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
            nameL.setFont(new Font("SansSerif", Font.BOLD, 15));
            nameL.setForeground(Color.WHITE);
            nameL.setAlignmentX(Component.CENTER_ALIGNMENT);
            nameL.setBorder(BorderFactory.createEmptyBorder(12, 6, 2, 6));

            // ── Type tag ──
            JLabel typeL = new JLabel("[" + card.getType() + "]", SwingConstants.CENTER);
            typeL.setFont(new Font("SansSerif", Font.PLAIN, 11));
            typeL.setForeground(pal[2].brighter());
            typeL.setAlignmentX(Component.CENTER_ALIGNMENT);

            // ── Rarity badge ──
            Color rarityColor = card.getRarity().color();
            JLabel rarityL = new JLabel("◆ " + card.getRarity().label(), SwingConstants.CENTER);
            rarityL.setFont(new Font("SansSerif", Font.BOLD, 10));
            rarityL.setForeground(rarityColor);
            rarityL.setAlignmentX(Component.CENTER_ALIGNMENT);

            // ── เส้นคั่น ── (สีตาม rarity)
            JPanel divider = new JPanel();
            divider.setOpaque(false);
            divider.setMaximumSize(new Dimension(130, 1));
            divider.setPreferredSize(new Dimension(130, 1));
            divider.setBorder(BorderFactory.createMatteBorder(0, 0, 1, 0, rarityColor));
            divider.setAlignmentX(Component.CENTER_ALIGNMENT);

            // ── Description ──
            JLabel descL = new JLabel("<html><div style='text-align:center;width:130px'>"
                    + card.getDescription() + "</div></html>", SwingConstants.CENTER);
            descL.setFont(new Font("SansSerif", Font.PLAIN, 11));
            descL.setForeground(new Color(220, 215, 240));
            descL.setAlignmentX(Component.CENTER_ALIGNMENT);
            descL.setBorder(BorderFactory.createEmptyBorder(6, 8, 4, 8));

            // ── Cost ──
            JLabel costL = new JLabel("⚡ " + card.getCost() + " Mana", SwingConstants.CENTER);
            costL.setFont(new Font("SansSerif", Font.BOLD, 12));
            costL.setForeground(new Color(255, 225, 70));
            costL.setAlignmentX(Component.CENTER_ALIGNMENT);
            costL.setBorder(BorderFactory.createEmptyBorder(0, 0, 12, 0));

            cardPanel.add(nameL);
            cardPanel.add(rarityL);
            cardPanel.add(typeL);
            cardPanel.add(Box.createRigidArea(new Dimension(0, 4)));
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

        JLabel hint = new JLabel("Select a card — your destiny awaits", SwingConstants.CENTER);
        hint.setForeground(new Color(130, 120, 160));
        hint.setFont(new Font("SansSerif", Font.PLAIN, 11));
        hint.setBorder(BorderFactory.createEmptyBorder(6, 0, 0, 0));
        panel.add(hint, BorderLayout.SOUTH);

        int result = JOptionPane.showConfirmDialog(this, panel, "Spoils of Battle",
                JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE);
        if (result == JOptionPane.OK_OPTION) {
            Card chosen = rewards.get(selected[0]);
            // apply class rules ก่อน add (Knight: poison skill → POISON type)
            PlayerClass pc = state.getPlayer().getPlayerClass();
            if (pc != null) {
                java.util.ArrayList<Card> single = new java.util.ArrayList<>();
                single.add(chosen);
                pc.applyClassRules(single);
            }
            state.getDeck().addToMasterDeck(chosen);
            showRewardNotification(chosen);
        }
    }

    /** Popup สวยๆ แสดงสิ่งที่ได้รับ */
    private void showRewardNotification(Card card) {
        JPanel panel = new JPanel(new BorderLayout(0, 10));
        panel.setBackground(new Color(14, 12, 24));
        panel.setBorder(BorderFactory.createEmptyBorder(16, 24, 16, 24));

        JLabel title = new JLabel("✦  Claimed  ✦", SwingConstants.CENTER);
        title.setFont(new Font("SansSerif", Font.BOLD, 16));
        title.setForeground(new Color(210, 185, 255));
        panel.add(title, BorderLayout.NORTH);

        Color rarityColor = card.getRarity().color();
        JPanel info = new JPanel(new GridLayout(3, 1, 0, 4));
        info.setBackground(new Color(20, 18, 32));
        info.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(rarityColor, 1),
                BorderFactory.createEmptyBorder(8, 12, 8, 12)));

        JLabel nameL = new JLabel(card.getName(), SwingConstants.CENTER);
        nameL.setFont(new Font("SansSerif", Font.BOLD, 18));
        nameL.setForeground(Color.WHITE);

        JLabel rarityL = new JLabel("◆ " + card.getRarity().label() + "  |  " + card.getType(), SwingConstants.CENTER);
        rarityL.setFont(new Font("SansSerif", Font.BOLD, 12));
        rarityL.setForeground(rarityColor);

        JLabel descL = new JLabel("<html><center>" + card.getDescription() + "</center></html>", SwingConstants.CENTER);
        descL.setFont(new Font("SansSerif", Font.PLAIN, 11));
        descL.setForeground(new Color(200, 195, 220));

        info.add(nameL); info.add(rarityL); info.add(descL);
        panel.add(info, BorderLayout.CENTER);

        JOptionPane.showMessageDialog(this, panel, "Claimed!", JOptionPane.PLAIN_MESSAGE);
        log("2694 Bound to deck: [" + card.getRarity().label() + "] \"" + card.getName() + "\"");
    }

    private void showGameOver() {
        SaveManager.deleteSave();
        AudioManager.stopMusic();
        JOptionPane.showMessageDialog(this,
                "The darkness claims you.\nFallen at depth " + state.getLevel(), "Fallen", JOptionPane.ERROR_MESSAGE);
        dispose();
        new MainMenu().setVisible(true);
    }

    // ── UI Helpers ────────────────────────────────────────────────────────────
    private void updateLabels() {
        if (state == null) return;
        Player p = state.getPlayer();
        playerLabel.setText("HP: " + p.getHp() + "/" + p.getMaxHp() + "  |  Gold: " + state.getGold() + "G  |  Lv." + state.getLevel());
        energyLabel.setText("⚡ " + p.getEnergy() + " Mana");
        if (enemyPanel != null) { enemyPanel.setEnemy(state.getEnemy()); enemyPanel.refreshStatus(); }
        playerHB.updateHealth(p.getHp(), p.getMaxHp());

        playerStatusPanel.removeAll();
        if (p.getBlock() > 0)    playerStatusPanel.add(statusBox("[DEF] "+p.getBlock(),    new Color(80,140,210)));
        if (p.getStrength() > 0) playerStatusPanel.add(statusBox("[STR] "+p.getStrength(), new Color(200,120,30)));
        if (p.getWeak() > 0)     playerStatusPanel.add(statusBox("[WEK] "+p.getWeak(),     new Color(160,60,200)));
        for (Relic r : state.getPlayer().getRelics())
            playerStatusPanel.add(statusBox("✦ "+r.getName(), new Color(180,160,40)));
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