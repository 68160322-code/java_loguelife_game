import java.util.*;

public class Deck {
    private ArrayList<Card> drawPile = new ArrayList<>();
    private ArrayList<Card> discardPile = new ArrayList<>();
    private ArrayList<Card> exhaustPile = new ArrayList<>();
    private ArrayList<Card> masterDeck = new ArrayList<>();

    public Deck() {
        // สร้าง Deck เริ่มต้น (เหมือนเดิมของคุณ)
        for (int i = 0; i < 5; i++) {
            masterDeck.add(new Card("Strike", CardType.ATTACK).damage(8).cost(1));
            masterDeck.add(new Card("Defend", CardType.SKILL).block(8).cost(1));
        }
    }

    // --- แก้ไขจุดนี้ ---
    public void startNewBattle() {
        // 1. ล้างกองการ์ดที่ใช้ในไฟต์ก่อนหน้าให้เกลี้ยง
        drawPile.clear();
        discardPile.clear();
        exhaustPile.clear();

        // 2. คัดลอกการ์ดจาก Master Deck มาใส่ Draw Pile
        // ใช้addAll เพื่อดึงข้อมูลการ์ดที่ผู้เล่นสะสมมาทั้งหมด
        drawPile.addAll(masterDeck);

        // 3. สับไพ่
        Collections.shuffle(drawPile);

        System.out.println("Battle Started: " + drawPile.size() + " cards in deck.");
    }

    public ArrayList<Card> draw(int amount) {
        ArrayList<Card> hand = new ArrayList<>();
        for (int i = 0; i < amount; i++) {
            if (drawPile.isEmpty()) {
                if (discardPile.isEmpty()) break; // ไม่มีการ์ดเหลือให้จั่วแล้วจริงๆ

                drawPile.addAll(discardPile);
                discardPile.clear();
                Collections.shuffle(drawPile);
            }

            if (!drawPile.isEmpty()) {
                hand.add(drawPile.remove(0));
            }
        }
        return hand;
    }

    public void discard(Card card) { discardPile.add(card); }
    public void exhaust(Card card) { exhaustPile.add(card); }
    public void addToMasterDeck(Card card) { masterDeck.add(card); }

    // Getters สำหรับตรวจสอบจำนวนการ์ด
    public int getDrawPileSize() { return drawPile.size(); }
    public int getDiscardPileSize() { return discardPile.size(); }
    public String viewDrawPile() { return buildList(drawPile); }
    public String viewDiscardPile() { return buildList(discardPile); }

    private String buildList(ArrayList<Card> pile) {
        if (pile.isEmpty()) return "Empty";
        StringBuilder sb = new StringBuilder();
        for (Card c : pile) sb.append("- ").append(c.getName()).append("\n");
        return sb.toString();
    }
    public ArrayList<Card> getMasterDeck() {
        return masterDeck;
    }
}