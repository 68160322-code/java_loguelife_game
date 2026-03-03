import javax.swing.*;
import java.awt.*;
import java.util.*;

public class GameFrame extends JFrame {

    private Player player;
    private Enemy enemy;
    private Deck deck;

    private JTextArea logArea;
    private JLabel playerLabel;
    private JLabel enemyLabel;
    private JLabel energyLabel;
    private JLabel intentLabel;
    private JPanel playerStatusPanel;
    private JPanel enemyStatusPanel;

    private JPanel handPanel;
    private ArrayList<Card> currentHand = new ArrayList<>();

    private JButton endTurnBtn;
    private JButton viewDeckBtn;
    private JButton viewDiscardBtn;

    private HealthBar playerHB;
    private HealthBar enemyHB;

    private int level = 1;

    public GameFrame() {
        setTitle("Rogue Card Game");
        setSize(900, 600);
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setLayout(new BorderLayout());
        setLocationRelativeTo(null);

        // 1. สร้าง Data พื้นฐาน
        player = new Player(70);
        deck = new Deck();

        // 2. 🔥 ต้องเรียก startLevel() ก่อน เพื่อให้ enemy ไม่เป็น null
        startLevel();

        // 3. สร้าง UI (ซึ่งจะเรียก updateLabels() ที่ใช้ข้อมูลจาก enemy)
        setupUI();

        // 4. เริ่มต้นระบบเด็คและเริ่มเทิร์น
        deck.startNewBattle();
        startPlayerTurn();

        setVisible(true);
    }


    private void setupUI() {
        // แถวบน 3 ช่องเหมือนเดิม: [ข้อมูลผู้เล่น] [ข้อมูลศัตรู] [พลังงาน]
        JPanel topPanel = new JPanel(new GridLayout(2, 3, 10, 5));

        playerLabel = new JLabel("Player Status");
        enemyLabel = new JLabel("Enemy Status");

        // สร้างหลอดเลือด (ตรวจสอบว่า Player/Enemy มี getMaxHp() และ getHp() แล้ว)
        playerHB = new HealthBar(player.getHp(), Color.GREEN);
        enemyHB = new HealthBar(enemy.getHp(), Color.RED);

        // --- จัดกลุ่มแผงผู้เล่น (ชื่อ + หลอดเลือด) ---
        JPanel playerInfoPanel = new JPanel(new BorderLayout());
        playerInfoPanel.add(playerLabel, BorderLayout.NORTH);
        playerInfoPanel.add(playerHB, BorderLayout.CENTER);

        // --- จัดกลุ่มแผงศัตรู (ชื่อ + หลอดเลือด) ---
        JPanel enemyInfoPanel = new JPanel(new BorderLayout());
        enemyInfoPanel.add(enemyLabel, BorderLayout.NORTH);
        enemyInfoPanel.add(enemyHB, BorderLayout.CENTER);

        energyLabel = new JLabel();
        intentLabel = new JLabel();

        playerStatusPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        enemyStatusPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));

        // เพิ่มเข้า topPanel ตามลำดับ
        topPanel.add(playerInfoPanel);    // ช่อง 1 แถว 1
        topPanel.add(enemyInfoPanel);     // ช่อง 2 แถว 1
        topPanel.add(energyLabel);       // ช่อง 3 แถว 1
        topPanel.add(playerStatusPanel); // ช่อง 1 แถว 2
        topPanel.add(enemyStatusPanel);  // ช่อง 2 แถว 2
        topPanel.add(intentLabel);       // ช่อง 3 แถว 2

        add(topPanel, BorderLayout.NORTH);

        // ----- ส่วนอื่นๆ เหมือนเดิม -----
        handPanel = new JPanel();
        add(handPanel, BorderLayout.CENTER);

        endTurnBtn = new JButton("End Turn");
        endTurnBtn.addActionListener(e -> endTurn());

        viewDeckBtn = new JButton("View Deck");
        viewDeckBtn.addActionListener(e ->
                JOptionPane.showMessageDialog(this, deck.viewDrawPile(), "Draw Pile", JOptionPane.INFORMATION_MESSAGE));

        viewDiscardBtn = new JButton("View Discard");
        viewDiscardBtn.addActionListener(e ->
                JOptionPane.showMessageDialog(this, deck.viewDiscardPile(), "Discard Pile", JOptionPane.INFORMATION_MESSAGE));

        JPanel rightPanel = new JPanel(new GridLayout(3, 1, 5, 5));
        rightPanel.add(endTurnBtn);
        rightPanel.add(viewDeckBtn);
        rightPanel.add(viewDiscardBtn);
        add(rightPanel, BorderLayout.EAST);

        logArea = new JTextArea(6, 20);
        logArea.setEditable(false);
        logArea.setLineWrap(true);
        logArea.setWrapStyleWord(true);

        JScrollPane scrollPane = new JScrollPane(logArea);
        scrollPane.setPreferredSize(new Dimension(900, 120));
        scrollPane.setBorder(BorderFactory.createTitledBorder("Battle Log"));
        add(scrollPane, BorderLayout.SOUTH);

        updateLabels();
    }

    private void startLevel() {
        if (level % 5 == 0) {
            enemy = new RageBoss(level);
        } else if (level % 3 == 0) {
            enemy = new BossEnemy(level);
        } else {
            enemy = new Enemy(level);
        }
        log("--- Level " + level + ": " + enemy.getName() + " appears! ---");
    }

    private void startPlayerTurn() {
        player.resetEnergy();
        player.resetBlock();
        enemy.decideIntent();
        currentHand = deck.draw(5);
        drawCards();
        updateLabels();
    }

    private void endTurn() {
        for (Card card : currentHand) {
            deck.discard(card);
        }
        currentHand.clear();
        handPanel.removeAll();

        enemyTurn();

        if (player.isDead()) {
            showGameOver();
            return;
        }

        player.reduceWeak();
        enemy.reduceWeak();
        startPlayerTurn();
    }

    private void enemyTurn() {
        if (enemy.getPoison() > 0) {
            int pDmg = enemy.getPoison();
            enemy.applyPoison();
            log("Poison deals " + pDmg + " damage to " + enemy.getName());
        }

        if (enemy.isDead()) {
            log(enemy.getName() + " defeated!");
            showRewardScreen();
            return;
        }

        int damage = enemy.attack();
        if (enemy.getIntent().contains("🛡️")) {
            log(enemy.getName() + " braces itself!");
        } else if (damage > 0) {
            player.takeDamage(damage);
            log(enemy.getName() + " attacks for " + damage + " damage!");
        } else {
            log(enemy.getName() + " is preparing something...");
        }
        updateLabels();
    }

    private void drawCards() {
        handPanel.removeAll();
        for (Card card : currentHand) {
            String btnText = "<html><center>" + card.getName() + "<br>"
                    + "<font color='blue'>Cost: " + card.getCost() + "</font><br>"
                    + "<small>" + card.getDescription() + "</small></center></html>";
            JButton btn = new JButton(btnText);
            btn.setPreferredSize(new Dimension(120, 100));
            btn.addActionListener(e -> playCard(card));
            handPanel.add(btn);
        }
        handPanel.revalidate();
        handPanel.repaint();
    }

    private void playCard(Card card) {
        if (!player.useEnergy(card.getCost())) {
            log("Not enough energy!");
            return;
        }
        card.play(player, enemy);
        if (card.isExhaust()) {
            deck.exhaust(card);
            log(card.getName() + " was exhausted!");
        } else {
            deck.discard(card);
        }
        currentHand.remove(card);
        drawCards();
        log("Played " + card.getName());
        if (enemy.isDead()) {
            log("Enemy defeated!");
            showRewardScreen();
        }
        updateLabels();
    }

    // แก้ไข updateLabels ให้ "ใจเย็นขึ้น" ถ้าข้อมูลยังไม่มา ไม่ต้องรีบวาด
    private void updateLabels() {
        // 1. เช็คความปลอดภัยก่อน (Null Guard)
        if (player == null || enemy == null || playerHB == null || enemyHB == null) return;

        // 2. อัปเดตข้อความพื้นฐาน
        playerLabel.setText("HP: " + player.getHp() + " | Block: " + player.getBlock());
        enemyLabel.setText(enemy.getName() + " HP: " + enemy.getHp());
        energyLabel.setText("Energy: " + player.getEnergy());
        intentLabel.setText("Intent: " + enemy.getIntent());

        // 3. 🔥 อัปเดตหลอดเลือดให้ตรงกับค่าปัจจุบัน
        playerHB.updateHealth(player.getHp(), player.getMaxHp());
        enemyHB.updateHealth(enemy.getHp(), enemy.getMaxHp());

        // 4. จัดการ Status Icons ของ Player
        playerStatusPanel.removeAll();
        if (player.getBlock() > 0) {
            playerStatusPanel.add(createStatusBox("🛡 " + player.getBlock(), new Color(180, 220, 255)));
        }
        if (player.getStrength() > 0) {
            playerStatusPanel.add(createStatusBox("💪 " + player.getStrength(), new Color(255, 200, 150)));
        }
        if (player.getWeak() > 0) {
            playerStatusPanel.add(createStatusBox("😵 " + player.getWeak(), new Color(220, 180, 255)));
        }
        playerStatusPanel.revalidate();
        playerStatusPanel.repaint();

        // 5. จัดการ Status Icons ของ Enemy
        enemyStatusPanel.removeAll();
        if (enemy.getPoison() > 0) {
            enemyStatusPanel.add(createStatusBox("☠ " + enemy.getPoison(), new Color(180, 255, 180)));
        }
        if (enemy.getWeak() > 0) {
            enemyStatusPanel.add(createStatusBox("😵 " + enemy.getWeak(), new Color(220, 180, 255)));
        }
        if (enemy.getStrength() > 0) { // เพิ่ม Strength ของศัตรูด้วยถ้ามี
            enemyStatusPanel.add(createStatusBox("💪 " + enemy.getStrength(), new Color(255, 150, 150)));
        }
        enemyStatusPanel.revalidate();
        enemyStatusPanel.repaint();
    }

    private JPanel createStatusBox(String text, Color bgColor) {
        JPanel box = new JPanel();
        box.setBackground(bgColor);
        box.setBorder(BorderFactory.createLineBorder(Color.BLACK));
        box.add(new JLabel(text));
        return box;
    }

    private void log(String text) {
        if (logArea == null) return;
        logArea.append(text + "\n");
        logArea.setCaretPosition(logArea.getDocument().getLength());
    }

    private void resetBattleState() {
        for (Card card : currentHand) deck.discard(card);
        currentHand.clear();
        handPanel.removeAll();
        handPanel.revalidate();
        handPanel.repaint();
    }

    private void showRewardScreen() {
        resetBattleState();
        boolean isBossLevel = (level % 3 == 0 || level % 5 == 0);
        if (isBossLevel) {
            player.increaseMaxEnergy(1);
            JOptionPane.showMessageDialog(this, "BOSS DEFEATED!\nMax Energy increased!");
        }

        String[] restOptions = {"🔥 Rest (Heal 30%)", "🔨 Smith (Max HP +10)"};
        int restChoice = JOptionPane.showOptionDialog(this, "Campfire", "Rest Site", 0, 1, null, restOptions, restOptions[0]);
        if (restChoice == 0) player.healPercent(30);
        else if (restChoice == 1) player.increaseMaxHp(10);

        Card c1 = getRandomRewardCard();
        Card c2 = getRandomRewardCard();
        Card c3 = getRandomRewardCard();
        Card[] options = {c1, c2, c3};
        String[] names = {c1.getName(), c2.getName(), c3.getName()};
        int choice = JOptionPane.showOptionDialog(this, "Reward", "Card", 0, 3, null, names, names[0]);
        if (choice >= 0) deck.addToMasterDeck(options[choice]);

        level++;
        player.increaseMaxHp(2);
        startLevel();
        deck.startNewBattle();
        startPlayerTurn();
    }

    private Card getRandomRewardCard() {
        ArrayList<Card> pool = new ArrayList<>();
        pool.add(new Card("Power Strike", CardType.ATTACK).damage(18).cost(2));
        pool.add(new Card("Shield Wall", CardType.SKILL).block(15).cost(2));
        pool.add(new Card("Toxic Burst", CardType.SKILL).poison(6).cost(1).exhaust());
        Collections.shuffle(pool);
        return pool.get(0);
    }

    private void showGameOver() {
        JOptionPane.showMessageDialog(this, "GAME OVER\nLevel: " + level);
        System.exit(0);
    }
}