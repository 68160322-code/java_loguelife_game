package event;

import core.GameState;
import card.Card;
import card.CardType;
import item.Relic;
import event.EventManager;

import javax.swing.*;
import javax.swing.border.*;
import java.awt.*;
import java.awt.event.*;
import java.util.*;

public class ShopManager {

    private static final Random rand = new Random();

    // ── สินค้าในร้าน ──────────────────────────────────────────────────────────
    private static Card[]  shopCards;
    private static Relic[] shopRelics;
    private static boolean[] cardSold  = new boolean[3];
    private static boolean[] relicSold = new boolean[2];
    private static boolean upgraded    = false; // Upgrade service ใช้ได้ครั้งเดียวต่อ visit

    public static void openShop(JFrame parent, GameState state) {
        shopCards  = rollCards();
        shopRelics = rollRelics();
        cardSold   = new boolean[3];
        relicSold  = new boolean[2];
        // fix relic prices so they don't change on refresh
        relicPrices = new int[]{80 + rand.nextInt(5)*5, 90 + rand.nextInt(5)*5};
        upgraded   = false;

        // สร้าง dialog ครั้งเดียว แล้วส่งต่อให้ buildContent refresh
        JDialog dialog = new JDialog(parent, "The Wandering Merchant", true);
        dialog.setDefaultCloseOperation(JDialog.DO_NOTHING_ON_CLOSE);
        dialog.setMinimumSize(new Dimension(680, 400));
        refreshShopContent(dialog, parent, state);
        dialog.pack();
        dialog.setLocationRelativeTo(parent);
        dialog.setVisible(true);
    }

    private static int[] relicPrices = {100, 120};

    /** สร้าง/อัปเดต content ใน dialog เดิม — ไม่สร้าง dialog ใหม่ */
    private static void refreshShopContent(JDialog dialog, JFrame parent, GameState state) {
        JPanel root = new JPanel(new BorderLayout(0, 0));
        root.setBackground(new Color(12, 10, 22));
        root.setPreferredSize(new Dimension(680, 400));

        root.add(buildHeader(state), BorderLayout.NORTH);

        JPanel content = new JPanel(new BorderLayout(10, 10));
        content.setBackground(new Color(12, 10, 22));
        content.setBorder(new EmptyBorder(10, 14, 10, 14));

        // ── Cards ──
        JPanel cardsSection = buildSection("⚔  Tomes & Scrolls");
        JPanel cardsRow = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 0));
        cardsRow.setBackground(new Color(18, 15, 30));
        for (int i = 0; i < shopCards.length; i++) {
            final int idx = i;
            final int price = getCardPrice(shopCards[i]);
            cardsRow.add(buildCardItem(shopCards[i], price, cardSold[i], parent, state, () -> {
                if (cardSold[idx]) {
                    msg(parent, "This relic has found another master.", "ร้านค้า");
                } else if (state.spendGold(price)) {
                    card.Card obj = shopCards[idx];
                    // apply class rules ก่อน add (Knight: poison skill → POISON)
                    core.PlayerClass pc = state.getPlayer().getPlayerClass();
                    if (pc != null) {
                        java.util.ArrayList<card.Card> single = new java.util.ArrayList<>();
                        single.add(obj);
                        pc.applyClassRules(single);
                    }
                    state.getDeck().addToMasterDeck(obj);
                    cardSold[idx] = true;
                    refreshShopContent(dialog, parent, state); // refresh dialog เดิม
                } else {
                    msg(parent, "ทองไม่พอ!", "ร้านค้า");
                }
            }));
        }
        cardsSection.add(cardsRow, BorderLayout.CENTER);
        content.add(cardsSection, BorderLayout.NORTH);

        // ── Relics + Services ──
        JPanel bottomRow = new JPanel(new GridLayout(1, 2, 10, 0));
        bottomRow.setBackground(new Color(12, 10, 22));

        JPanel relicsSection = buildSection("✦  Dark Relics");
        JPanel relicsRow = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 0));
        relicsRow.setBackground(new Color(18, 15, 30));
        for (int i = 0; i < shopRelics.length; i++) {
            final int idx = i;
            final int price = relicPrices[i];
            relicsRow.add(buildRelicItem(shopRelics[i], price, relicSold[i], parent, state, () -> {
                if (relicSold[idx]) {
                    msg(parent, "This relic has found another master.", "ร้านค้า");
                } else if (state.spendGold(price)) {
                    state.getPlayer().addRelic(shopRelics[idx]);
                    relicSold[idx] = true;
                    refreshShopContent(dialog, parent, state);
                } else {
                    msg(parent, "ทองไม่พอ!", "ร้านค้า");
                }
            }));
        }
        relicsSection.add(relicsRow, BorderLayout.CENTER);
        bottomRow.add(relicsSection);

        // ── Services ──
        JPanel servicesSection = buildSection("🔧  Services");
        JPanel servicesCol = new JPanel();
        servicesCol.setLayout(new BoxLayout(servicesCol, BoxLayout.Y_AXIS));
        servicesCol.setBackground(new Color(18, 15, 30));
        servicesCol.setBorder(new EmptyBorder(8, 8, 8, 8));

        int healAmt = (int)(state.getPlayer().getMaxHp() * 0.3);
        servicesCol.add(buildServiceItem(
                "⚕  Heal  +" + healAmt + " HP", "50G",
                "Restore " + healAmt + " HP  (Current: " + state.getPlayer().getHp() + "/" + state.getPlayer().getMaxHp() + ")",
                new Color(30, 120, 60), state.getGold() >= 50, parent, state, () -> {
                    if (state.spendGold(50)) {
                        state.getPlayer().heal(healAmt);
                        msg(parent, "Healed +" + healAmt + " HP!", "Healed");
                        refreshShopContent(dialog, parent, state);
                    } else msg(parent, "Not enough gold! Need 50G", "ร้านค้า");
                }));
        servicesCol.add(Box.createRigidArea(new Dimension(0, 8)));

        servicesCol.add(buildServiceItem(
                upgraded ? "🔨  Upgrade  (Used)" : "🔨  Upgrade a Card",
                upgraded ? "—" : "75G",
                upgraded ? "Already used this visit" : "Pick a card from your deck to upgrade",
                upgraded ? new Color(45, 45, 65) : new Color(120, 80, 20),
                !upgraded && state.getGold() >= 75, parent, state, () -> {
                    if (upgraded) { msg(parent, "Already used this visit!", "ร้านค้า"); return; }
                    if (state.spendGold(75)) {
                        pickAndUpgradeCard(parent, state);
                        upgraded = true;
                        refreshShopContent(dialog, parent, state);
                    } else msg(parent, "Not enough gold! Need 75G", "ร้านค้า");
                }));
        servicesCol.add(Box.createRigidArea(new Dimension(0, 8)));

        servicesCol.add(buildServiceItem(
                "🗑  Remove a Card", "50G",
                "Pick a card from your deck to remove permanently",
                new Color(120, 30, 30), state.getGold() >= 50, parent, state, () -> {
                    if (state.spendGold(50)) {
                        pickAndRemoveCard(parent, state);
                        refreshShopContent(dialog, parent, state);
                    } else msg(parent, "Not enough gold! Need 50G", "ร้านค้า");
                }));

        servicesSection.add(servicesCol, BorderLayout.CENTER);
        bottomRow.add(servicesSection);
        content.add(bottomRow, BorderLayout.CENTER);

        JScrollPane scrollPane = new JScrollPane(content);
        scrollPane.setBorder(null);
        scrollPane.setBackground(new Color(12, 10, 22));
        scrollPane.getViewport().setBackground(new Color(12, 10, 22));
        scrollPane.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED);
        scrollPane.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
        scrollPane.getVerticalScrollBar().setUnitIncrement(16);
        root.add(scrollPane, BorderLayout.CENTER);

        // Leave button
        JButton leaveBtn = new JButton("Leave") {
            @Override protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g;
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(new Color(60, 50, 80));
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), 8, 8);
                g2.setColor(new Color(120, 100, 160));
                g2.drawRoundRect(1, 1, getWidth()-2, getHeight()-2, 8, 8);
                g2.setFont(getFont()); g2.setColor(Color.WHITE);
                FontMetrics fm = g2.getFontMetrics();
                g2.drawString(getText(), (getWidth()-fm.stringWidth(getText()))/2,
                        (getHeight()+fm.getAscent()-fm.getDescent())/2);
            }
            @Override protected void paintBorder(Graphics g) {}
        };
        leaveBtn.setFont(new Font("SansSerif", Font.BOLD, 13));
        leaveBtn.setContentAreaFilled(false); leaveBtn.setFocusPainted(false);
        leaveBtn.setPreferredSize(new Dimension(680, 40));
        leaveBtn.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        leaveBtn.addActionListener(e -> dialog.dispose()); // ปิด dialog เดียว

        JPanel footer = new JPanel(new FlowLayout(FlowLayout.CENTER));
        footer.setBackground(new Color(12, 10, 22));
        footer.add(leaveBtn);
        root.add(footer, BorderLayout.SOUTH);

        // อัปเดต content ใน dialog เดิม (ไม่สร้างใหม่)
        dialog.setContentPane(root);
        dialog.revalidate();
        dialog.repaint();
    }

    // ── Header bar ────────────────────────────────────────────────────────────
    private static JPanel buildHeader(GameState state) {
        JPanel header = new JPanel(new BorderLayout()) {
            @Override protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g;
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                GradientPaint gp = new GradientPaint(0, 0, new Color(40, 20, 10),
                        getWidth(), 0, new Color(20, 10, 40));
                g2.setPaint(gp);
                g2.fillRect(0, 0, getWidth(), getHeight());
            }
        };
        header.setOpaque(false);
        header.setBorder(new EmptyBorder(12, 16, 12, 16));

        JLabel title = new JLabel("✦  The Wandering Merchant  ✦");
        title.setFont(new Font("SansSerif", Font.BOLD, 22));
        title.setForeground(new Color(255, 210, 80));
        header.add(title, BorderLayout.WEST);

        JLabel gold = new JLabel("Gold: " + state.getGold() + "G  |  HP: "
                + state.getPlayer().getHp() + "/" + state.getPlayer().getMaxHp());
        gold.setFont(new Font("SansSerif", Font.BOLD, 13));
        gold.setForeground(new Color(255, 220, 100));
        header.add(gold, BorderLayout.EAST);

        return header;
    }

    // ── Section wrapper ───────────────────────────────────────────────────────
    private static JPanel buildSection(String sectionTitle) {
        JPanel section = new JPanel(new BorderLayout(0, 6));
        section.setBackground(new Color(18, 15, 30));
        section.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(55, 45, 80), 1),
                new EmptyBorder(8, 8, 8, 8)));

        JLabel lbl = new JLabel(sectionTitle);
        lbl.setFont(new Font("SansSerif", Font.BOLD, 13));
        lbl.setForeground(new Color(200, 175, 255));
        lbl.setBorder(new EmptyBorder(0, 0, 4, 0));
        section.add(lbl, BorderLayout.NORTH);
        return section;
    }

    // ── Card item ─────────────────────────────────────────────────────────────
    private static JPanel buildCardItem(Card card, int price, boolean sold,
                                        JFrame parent, GameState state, Runnable onBuy) {
        Color[] pal = cardPalette(card.getType());

        JPanel panel = new JPanel() {
            @Override protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g;
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                if (sold) {
                    g2.setColor(new Color(25, 22, 38));
                } else {
                    GradientPaint gp = new GradientPaint(0, 0, pal[0], 0, getHeight(), pal[1]);
                    g2.setPaint(gp);
                }
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), 12, 12);
                g2.setColor(sold ? new Color(50, 47, 65) : pal[2]);
                g2.setStroke(new BasicStroke(1.5f));
                g2.drawRoundRect(1, 1, getWidth()-2, getHeight()-2, 12, 12);
                g2.setStroke(new BasicStroke(1));
                // SOLD overlay
                if (sold) {
                    g2.setColor(new Color(255, 80, 80, 120));
                    g2.setFont(new Font("SansSerif", Font.BOLD, 18));
                    FontMetrics fm = g2.getFontMetrics();
                    g2.drawString("Sold", (getWidth()-fm.stringWidth("Sold"))/2, getHeight()/2+6);
                }
            }
        };
        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
        panel.setOpaque(false);
        panel.setPreferredSize(new Dimension(150, 190));

        if (!sold) {
            panel.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
            panel.addMouseListener(new MouseAdapter() {
                @Override public void mouseClicked(MouseEvent e) { onBuy.run(); }
                @Override public void mouseEntered(MouseEvent e) { panel.repaint(); }
                @Override public void mouseExited(MouseEvent e)  { panel.repaint(); }
            });
        }

        JLabel nameL = new JLabel(card.getName(), SwingConstants.CENTER);
        nameL.setFont(new Font("SansSerif", Font.BOLD, 13));
        nameL.setForeground(sold ? new Color(80, 75, 100) : Color.WHITE);
        nameL.setAlignmentX(Component.CENTER_ALIGNMENT);
        nameL.setBorder(new EmptyBorder(10, 4, 0, 4));

        // Rarity badge
        Color rarityColor = sold ? new Color(60, 58, 75) : card.getRarity().color();
        JLabel rarityL = new JLabel("◆ " + card.getRarity().label(), SwingConstants.CENTER);
        rarityL.setFont(new Font("SansSerif", Font.BOLD, 9));
        rarityL.setForeground(rarityColor);
        rarityL.setAlignmentX(Component.CENTER_ALIGNMENT);

        JLabel typeL = new JLabel("[" + card.getType() + "]", SwingConstants.CENTER);
        typeL.setFont(new Font("SansSerif", Font.PLAIN, 10));
        typeL.setForeground(sold ? new Color(70, 67, 88) : new Color(170, 165, 190));
        typeL.setAlignmentX(Component.CENTER_ALIGNMENT);

        JPanel div = new JPanel(); div.setOpaque(false);
        div.setMaximumSize(new Dimension(120, 1));
        div.setBorder(new MatteBorder(0, 0, 1, 0, sold ? new Color(55, 52, 70) : pal[2]));
        div.setAlignmentX(Component.CENTER_ALIGNMENT);

        JLabel descL = new JLabel("<html><div style='text-align:center;width:120px'>"
                + card.getDescription() + "</div></html>", SwingConstants.CENTER);
        descL.setFont(new Font("SansSerif", Font.PLAIN, 10));
        descL.setForeground(sold ? new Color(70, 67, 88) : new Color(215, 210, 235));
        descL.setAlignmentX(Component.CENTER_ALIGNMENT);
        descL.setBorder(new EmptyBorder(4, 6, 2, 6));

        JLabel priceL = new JLabel(sold ? "Sold" : price + "G  |  ⚡" + card.getCost(), SwingConstants.CENTER);
        priceL.setFont(new Font("SansSerif", Font.BOLD, 11));
        priceL.setForeground(sold ? new Color(180, 60, 60)
                : state.getGold() >= price ? new Color(255, 210, 60) : new Color(200, 80, 80));
        priceL.setAlignmentX(Component.CENTER_ALIGNMENT);
        priceL.setBorder(new EmptyBorder(0, 0, 8, 0));

        panel.add(nameL); panel.add(rarityL); panel.add(typeL);
        panel.add(Box.createRigidArea(new Dimension(0, 4)));
        panel.add(div); panel.add(descL);
        panel.add(Box.createVerticalGlue()); panel.add(priceL);
        return panel;
    }

    // ── Relic item ────────────────────────────────────────────────────────────
    private static JPanel buildRelicItem(Relic relic, int price, boolean sold,
                                         JFrame parent, GameState state, Runnable onBuy) {
        JPanel panel = new JPanel() {
            @Override protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g;
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(sold ? new Color(25, 22, 38) : new Color(40, 25, 10));
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), 12, 12);
                g2.setColor(sold ? new Color(55, 50, 35) : new Color(180, 140, 40));
                g2.setStroke(new BasicStroke(1.5f));
                g2.drawRoundRect(1, 1, getWidth()-2, getHeight()-2, 12, 12);
                g2.setStroke(new BasicStroke(1));
                if (sold) {
                    g2.setColor(new Color(255, 80, 80, 120));
                    g2.setFont(new Font("SansSerif", Font.BOLD, 16));
                    FontMetrics fm = g2.getFontMetrics();
                    g2.drawString("Sold", (getWidth()-fm.stringWidth("Sold"))/2, getHeight()/2+5);
                }
            }
        };
        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
        panel.setOpaque(false);
        panel.setPreferredSize(new Dimension(150, 110));

        if (!sold) {
            panel.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
            panel.addMouseListener(new MouseAdapter() {
                @Override public void mouseClicked(MouseEvent e) { onBuy.run(); }
            });
        }

        JLabel nameL = new JLabel("> " + relic.getName(), SwingConstants.CENTER);
        nameL.setFont(new Font("SansSerif", Font.BOLD, 12));
        nameL.setForeground(sold ? new Color(80, 75, 60) : new Color(255, 215, 80));
        nameL.setAlignmentX(Component.CENTER_ALIGNMENT);
        nameL.setBorder(new EmptyBorder(8, 6, 0, 6));

        // Rarity badge
        Color relicRarityColor = sold ? new Color(70, 65, 50) : relic.getRarity().color();
        JLabel rarityL = new JLabel("◆ " + relic.getRarity().label(), SwingConstants.CENTER);
        rarityL.setFont(new Font("SansSerif", Font.BOLD, 9));
        rarityL.setForeground(relicRarityColor);
        rarityL.setAlignmentX(Component.CENTER_ALIGNMENT);

        JLabel descL = new JLabel("<html><div style='text-align:center;width:130px'>"
                + relic.getDescription() + "</div></html>", SwingConstants.CENTER);
        descL.setFont(new Font("SansSerif", Font.PLAIN, 10));
        descL.setForeground(sold ? new Color(70, 67, 55) : new Color(215, 200, 170));
        descL.setAlignmentX(Component.CENTER_ALIGNMENT);
        descL.setBorder(new EmptyBorder(2, 6, 2, 6));

        JLabel priceL = new JLabel(sold ? "Sold" : price + "G", SwingConstants.CENTER);
        priceL.setFont(new Font("SansSerif", Font.BOLD, 12));
        priceL.setForeground(sold ? new Color(180, 60, 60)
                : state.getGold() >= price ? new Color(255, 210, 60) : new Color(200, 80, 80));
        priceL.setAlignmentX(Component.CENTER_ALIGNMENT);
        priceL.setBorder(new EmptyBorder(2, 0, 8, 0));

        panel.add(nameL); panel.add(rarityL); panel.add(descL);
        panel.add(Box.createVerticalGlue()); panel.add(priceL);
        return panel;
    }

    // ── Service item ──────────────────────────────────────────────────────────
    private static JPanel buildServiceItem(String title, String price, String desc,
                                           Color accent, boolean canAfford,
                                           JFrame parent, GameState state, Runnable onUse) {
        JPanel panel = new JPanel(new BorderLayout(0, 2)) {
            @Override protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g;
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(new Color(accent.getRed()/3, accent.getGreen()/3, accent.getBlue()/3));
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), 8, 8);
                g2.setColor(accent);
                g2.setStroke(new BasicStroke(1.2f));
                g2.drawRoundRect(1, 1, getWidth()-2, getHeight()-2, 8, 8);
            }
        };
        panel.setOpaque(false);
        panel.setMaximumSize(new Dimension(9999, 56));
        panel.setBorder(new EmptyBorder(7, 10, 7, 10));
        panel.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        panel.addMouseListener(new MouseAdapter() {
            @Override public void mouseClicked(MouseEvent e) { onUse.run(); }
        });

        // Title + price ในแถวเดียวกัน
        Color priceFg = price.equals("—") ? new Color(100, 95, 115)
                : canAfford          ? new Color(255, 215, 50)
                :                      new Color(225, 70, 70);
        JLabel topL = new JLabel(title + "    " + price);
        topL.setFont(new Font("SansSerif", Font.BOLD, 12));
        // ใช้ HTML เพื่อแสดงราคาสีต่างกัน
        String priceHex = String.format("#%02x%02x%02x", priceFg.getRed(), priceFg.getGreen(), priceFg.getBlue());
        topL.setText("<html>" + title + "&nbsp;&nbsp;<font color='" + priceHex + "'><b>" + price + "</b></font></html>");
        topL.setForeground(Color.WHITE);

        JLabel descL = new JLabel(desc);
        descL.setFont(new Font("SansSerif", Font.PLAIN, 10));
        descL.setForeground(new Color(175, 170, 195));

        panel.add(topL,  BorderLayout.NORTH);
        panel.add(descL, BorderLayout.SOUTH);

        return panel;
    }

    // ── Helpers ───────────────────────────────────────────────────────────────

    /** แสดง dialog ให้เลือกการ์ดที่จะ Upgrade */
    private static void pickAndUpgradeCard(JFrame parent, GameState state) {
        ArrayList<Card> deck = state.getDeck().getMasterDeck();
        if (deck.isEmpty()) { msg(parent, "Your deck is empty!", "ร้านค้า"); return; }

        Card chosen = showCardPickerDialog(parent, deck, "🔨  Choose a Card to Upgrade");
        if (chosen != null) {
            chosen.upgrade();
            msg(parent, "✦ Forged: " + chosen.getName(), "Forged!");
        }
    }

    /** แสดง dialog ให้เลือกการ์ดที่จะ Remove */
    private static void pickAndRemoveCard(JFrame parent, GameState state) {
        ArrayList<Card> deck = state.getDeck().getMasterDeck();
        if (deck.size() <= 1) { msg(parent, "Your deck is too small to remove from!", "ร้านค้า"); return; }

        Card chosen = showCardPickerDialog(parent, deck, "🗑  Choose a Card to Remove");
        if (chosen != null) {
            deck.remove(chosen);
            msg(parent, "Removed: " + chosen.getName() + " from your deck.", "Card Removed");
        }
    }

    /** Dialog picker — แสดงการ์ดใน deck ให้คลิกเลือก 1 ใบ */
    private static Card showCardPickerDialog(JFrame parent, ArrayList<Card> deck, String title) {
        Card[] result = {null};

        JPanel root = new JPanel(new BorderLayout(0, 10));
        root.setBackground(new Color(14, 12, 24));
        root.setBorder(new EmptyBorder(12, 14, 12, 14));

        JLabel titleL = new JLabel(title, SwingConstants.CENTER);
        titleL.setFont(new Font("SansSerif", Font.BOLD, 16));
        titleL.setForeground(new Color(210, 185, 255));
        root.add(titleL, BorderLayout.NORTH);

        // Grid ของการ์ด
        int cols = Math.min(4, deck.size());
        JPanel grid = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 10));
        grid.setBackground(new Color(14, 12, 24));

        JPanel[] highlight = {null};  // track ที่เลือกอยู่

        for (Card card : deck) {
            Color[] pal = cardPalette(card.getType());

            JPanel cardPanel = new JPanel() {
                @Override protected void paintComponent(Graphics g) {
                    Graphics2D g2 = (Graphics2D) g;
                    g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                    GradientPaint gp = new GradientPaint(0, 0, pal[0], 0, getHeight(), pal[1]);
                    g2.setPaint(gp);
                    g2.fillRoundRect(0, 0, getWidth(), getHeight(), 12, 12);
                    g2.setColor(this == highlight[0] ? Color.WHITE : pal[2]);
                    g2.setStroke(new BasicStroke(this == highlight[0] ? 3f : 1.5f));
                    g2.drawRoundRect(1, 1, getWidth()-2, getHeight()-2, 12, 12);
                }
            };
            cardPanel.setLayout(new BoxLayout(cardPanel, BoxLayout.Y_AXIS));
            cardPanel.setOpaque(false);
            cardPanel.setPreferredSize(new Dimension(140, 175));
            cardPanel.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));

            JLabel nameL = new JLabel(card.getName(), SwingConstants.CENTER);
            nameL.setFont(new Font("SansSerif", Font.BOLD, 12));
            nameL.setForeground(Color.WHITE);
            nameL.setAlignmentX(Component.CENTER_ALIGNMENT);
            nameL.setBorder(new EmptyBorder(8, 4, 2, 4));

            JLabel typeL = new JLabel("[" + card.getType() + "]", SwingConstants.CENTER);
            typeL.setFont(new Font("SansSerif", Font.PLAIN, 10));
            typeL.setForeground(pal[2].brighter());
            typeL.setAlignmentX(Component.CENTER_ALIGNMENT);

            JLabel descL = new JLabel("<html><div style='text-align:center;width:115px'>"
                    + card.getDescription() + "</div></html>", SwingConstants.CENTER);
            descL.setFont(new Font("SansSerif", Font.PLAIN, 10));
            descL.setForeground(new Color(215, 210, 235));
            descL.setAlignmentX(Component.CENTER_ALIGNMENT);
            descL.setBorder(new EmptyBorder(4, 6, 2, 6));

            JLabel costL = new JLabel("⚡ " + card.getCost(), SwingConstants.CENTER);
            costL.setFont(new Font("SansSerif", Font.BOLD, 11));
            costL.setForeground(new Color(255, 225, 70));
            costL.setAlignmentX(Component.CENTER_ALIGNMENT);
            costL.setBorder(new EmptyBorder(0, 0, 8, 0));

            cardPanel.add(nameL); cardPanel.add(typeL);
            cardPanel.add(descL); cardPanel.add(Box.createVerticalGlue()); cardPanel.add(costL);

            cardPanel.addMouseListener(new MouseAdapter() {
                @Override public void mouseClicked(MouseEvent e) {
                    result[0] = card;
                    highlight[0] = cardPanel;
                    grid.repaint();
                }
            });
            grid.add(cardPanel);
        }

        JScrollPane scroll = new JScrollPane(grid);
        scroll.setBorder(null);
        scroll.setBackground(new Color(14, 12, 24));
        scroll.getViewport().setBackground(new Color(14, 12, 24));
        scroll.setPreferredSize(new Dimension(620, 260));
        root.add(scroll, BorderLayout.CENTER);

        JLabel hint = new JLabel("Click a card to select it", SwingConstants.CENTER);
        hint.setForeground(new Color(130, 120, 160));
        hint.setFont(new Font("SansSerif", Font.PLAIN, 11));
        root.add(hint, BorderLayout.SOUTH);

        int res = JOptionPane.showConfirmDialog(parent, root, title,
                JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE);
        return (res == JOptionPane.OK_OPTION) ? result[0] : null;
    }

    private static int getCardPrice(Card card) {
        return 50 + card.getCost() * 10 + rand.nextInt(3) * 5;
    }

    private static void msg(JFrame parent, String text, String title) {
        JOptionPane.showMessageDialog(parent, text, title, JOptionPane.INFORMATION_MESSAGE);
    }

    private static Card[] rollCards() {
        // ใช้ CardLibrary สุ่ม 3 ใบ (เน้น Common/Rare, มี Epic บ้าง)
        return new Card[]{
                card.CardLibrary.rollShopCard(),
                card.CardLibrary.rollShopCard(),
                card.CardLibrary.rollShopCard()
        };
    }

    private static Relic[] rollRelics() {
        return new Relic[]{
                item.RelicLibrary.rollShopRelic(),
                item.RelicLibrary.rollShopRelic()
        };
    }

    private static Color[] cardPalette(CardType type) {
        switch (type) {
            case ATTACK: return new Color[]{new Color(90,20,20), new Color(50,10,10), new Color(200,80,60)};
            case SKILL:  return new Color[]{new Color(20,40,90), new Color(10,20,55), new Color(80,130,210)};
            case HEAL:   return new Color[]{new Color(20,70,35), new Color(10,40,20), new Color(60,180,90)};
            default:     return new Color[]{new Color(40,40,60), new Color(20,20,40), new Color(120,120,160)};
        }
    }
}