package card;

/**
 * Extended Card Library
 *
 * การ์ดใหม่ที่ใช้ Vulnerable และ Poison
 * รวมถึงการ์ดที่มี synergy กับ status effects
 *
 * Note: การ์ดบางใบใช้ basic mechanics เพราะ Card class
 * ยังไม่รองรับ advanced features ทั้งหมด
 */
public class ExtendedCardLibrary {

    // ═══════════════════════════════════════════════════════════════
    // ATTACK CARDS WITH VULNERABLE
    // ═══════════════════════════════════════════════════════════════

    public static Card exposeWeakness() {
        return new Card("Expose Weakness", CardType.SKILL, Card.Rarity.COMMON)
                .cost(1)
                .vulnerable(2); // ใส่ vulnerable 2 turns ให้ศัตรู
    }

    public static Card crushingBlow() {
        return new Card("Crushing Blow", CardType.ATTACK, Card.Rarity.RARE)
                .cost(2)
                .damage(18)
                .vulnerable(1); // โจมตีแรง + ใส่ vulnerable
    }

    public static Card uppercut() {
        return new Card("Uppercut", CardType.ATTACK, Card.Rarity.COMMON)
                .cost(2)
                .damage(13)
                .vulnerable(2)
                .weak(2); // ใส่ทั้ง vulnerable และ weak
    }

    // ═══════════════════════════════════════════════════════════════
    // POISON CARDS
    // ═══════════════════════════════════════════════════════════════

    public static Card poisonedBlade() {
        return new Card("Poisoned Blade", CardType.ATTACK, Card.Rarity.COMMON)
                .cost(1)
                .damage(6)
                .poison(3); // โจมตี + poison
    }

    public static Card toxicCloud() {
        return new Card("Toxic Cloud", CardType.SKILL, Card.Rarity.RARE)
                .cost(1)
                .poison(5)
                .exhaust(); // poison เยอะ แต่ exhaust
    }

    public static Card venomStrike() {
        return new Card("Venom Strike", CardType.ATTACK, Card.Rarity.EPIC)
                .cost(2)
                .damage(10)
                .poison(6)
                .vulnerable(1); // combo มหาประลัย
    }

    public static Card serpentStrike() {
        return new Card("Serpent Strike", CardType.ATTACK, Card.Rarity.COMMON)
                .cost(0)
                .damage(3)
                .poison(2); // cost 0 แต่ดาเมจน้อย
    }

    // ═══════════════════════════════════════════════════════════════
    // DEFENSIVE CARDS
    // ═══════════════════════════════════════════════════════════════

    public static Card protectiveStance() {
        return new Card("Protective Stance", CardType.SKILL, Card.Rarity.COMMON)
                .cost(1)
                .block(8);
    }

    public static Card fortify() {
        return new Card("Fortify", CardType.SKILL, Card.Rarity.RARE)
                .cost(1)
                .block(12);
    }

    public static Card ironWall() {
        return new Card("Iron Wall", CardType.SKILL, Card.Rarity.EPIC)
                .cost(2)
                .block(20);
    }

    // ═══════════════════════════════════════════════════════════════
    // COMBO CARDS
    // ═══════════════════════════════════════════════════════════════

    public static Card poisonousDefense() {
        return new Card("Poisonous Defense", CardType.SKILL, Card.Rarity.RARE)
                .cost(1)
                .block(6)
                .poison(3); // ป้องกัน + ใส่ poison
    }

    public static Card hemorrhage() {
        return new Card("Hemorrhage", CardType.ATTACK, Card.Rarity.RARE)
                .cost(2)
                .damage(12)
                .poison(4); // ดาเมจ + poison
    }

    public static Card devastate() {
        return new Card("Devastate", CardType.ATTACK, Card.Rarity.EPIC)
                .cost(3)
                .damage(20)
                .vulnerable(2); // ดาเมจสูง + vulnerable
    }

    // ═══════════════════════════════════════════════════════════════
    // POWER CARDS
    // ═══════════════════════════════════════════════════════════════

    public static Card battleRage() {
        return new Card("Battle Rage", CardType.SKILL, Card.Rarity.COMMON)
                .cost(1)
                .strength(2); // เพิ่ม strength
    }

    public static Card weakenFoe() {
        return new Card("Weaken Foe", CardType.SKILL, Card.Rarity.COMMON)
                .cost(1)
                .weak(2); // ใส่ weak
    }

    public static Card bloodPact() {
        return new Card("Blood Pact", CardType.SKILL, Card.Rarity.RARE)
                .cost(0)
                .strength(3)
                .exhaust(); // +3 strength แต่ exhaust
    }

    // ═══════════════════════════════════════════════════════════════
    // LEGENDARY CARDS
    // ═══════════════════════════════════════════════════════════════

    public static Card apocalypse() {
        return new Card("Apocalypse", CardType.ATTACK, Card.Rarity.LEGENDARY)
                .cost(4)
                .damage(40)
                .vulnerable(3)
                .weak(3)
                .exhaust(); // โจมตีสุดโหด exhausts
    }

    public static Card plaguebringer() {
        return new Card("Plaguebringer", CardType.SKILL, Card.Rarity.LEGENDARY)
                .cost(3)
                .poison(15)
                .vulnerable(2)
                .exhaust();
    }

    public static Card divineShield() {
        return new Card("Divine Shield", CardType.SKILL, Card.Rarity.LEGENDARY)
                .cost(2)
                .block(30)
                .exhaust();
    }

    public static Card executioner() {
        return new Card("Executioner", CardType.ATTACK, Card.Rarity.LEGENDARY)
                .cost(3)
                .damage(35)
                .exhaust(); // ดาเมจสูงมาก
    }

    // ═══════════════════════════════════════════════════════════════
    // STARTER DECK CARDS
    // ═══════════════════════════════════════════════════════════════

    public static Card strike() {
        return new Card("Strike", CardType.ATTACK, Card.Rarity.COMMON)
                .cost(1)
                .damage(6);
    }

    public static Card defend() {
        return new Card("Defend", CardType.SKILL, Card.Rarity.COMMON)
                .cost(1)
                .block(5);
    }

    public static Card bash() {
        return new Card("Bash", CardType.ATTACK, Card.Rarity.COMMON)
                .cost(2)
                .damage(8)
                .vulnerable(2);
    }

    // ═══════════════════════════════════════════════════════════════
    // MULTI-HIT CARDS
    // ═══════════════════════════════════════════════════════════════

    public static Card twinStrike() {
        return new Card("Twin Strike", CardType.ATTACK, Card.Rarity.COMMON)
                .cost(1)
                .damage(5)
                .multiHit(); // ตี 2 ครั้ง
    }

    public static Card flurry() {
        return new Card("Flurry", CardType.ATTACK, Card.Rarity.RARE)
                .cost(2)
                .damage(7)
                .multiHit(); // ตี 2 ครั้ง, ดาเมจสูงกว่า
    }

    // ═══════════════════════════════════════════════════════════════
    // ENERGY BURST CARDS
    // ═══════════════════════════════════════════════════════════════

    public static Card whirlwind() {
        return new Card("Whirlwind", CardType.ATTACK, Card.Rarity.RARE)
                .cost(0)
                .damage(5)
                .energyBurst(); // 5 dmg x remaining energy
    }

    // ═══════════════════════════════════════════════════════════════
    // UTILITY & HEALING
    // ═══════════════════════════════════════════════════════════════

    public static Card healingPotion() {
        return new Card("Healing Potion", CardType.SKILL, Card.Rarity.COMMON)
                .cost(1)
                .heal(10)
                .exhaust();
    }

    public static Card meditation() {
        return new Card("Meditation", CardType.SKILL, Card.Rarity.RARE)
                .cost(2)
                .heal(15)
                .block(10);
    }

    public static Card vampiricStrike() {
        return new Card("Vampiric Strike", CardType.ATTACK, Card.Rarity.EPIC)
                .cost(2)
                .damage(12)
                .heal(5); // โจมตี + ฟื้น HP
    }

    /**
     * Helper method: สร้าง deck เริ่มต้นที่มีการ์ดใหม่
     */
    public static java.util.ArrayList<Card> createEnhancedStarterDeck() {
        java.util.ArrayList<Card> deck = new java.util.ArrayList<>();

        // Basic strikes and defends
        for (int i = 0; i < 5; i++) deck.add(strike());
        for (int i = 0; i < 4; i++) deck.add(defend());

        // One special card
        deck.add(bash());

        return deck;
    }

    /**
     * Helper: สุ่มการ์ด reward โดยมีโอกาสได้การ์ดใหม่
     */
    public static Card getRandomRewardCard(Card.Rarity minRarity) {
        java.util.ArrayList<Card> pool = new java.util.ArrayList<>();

        // Common cards
        if (minRarity == Card.Rarity.COMMON) {
            pool.add(exposeWeakness());
            pool.add(poisonedBlade());
            pool.add(serpentStrike());
            pool.add(protectiveStance());
            pool.add(strike());
            pool.add(defend());
            pool.add(bash());
            pool.add(twinStrike());
            pool.add(battleRage());
            pool.add(weakenFoe());
            pool.add(healingPotion());
        }

        // Rare cards
        if (minRarity == Card.Rarity.COMMON || minRarity == Card.Rarity.RARE) {
            pool.add(crushingBlow());
            pool.add(toxicCloud());
            pool.add(fortify());
            pool.add(poisonousDefense());
            pool.add(hemorrhage());
            pool.add(bloodPact());
            pool.add(flurry());
            pool.add(whirlwind());
            pool.add(meditation());
        }

        // Epic cards
        if (minRarity != Card.Rarity.LEGENDARY) {
            pool.add(venomStrike());
            pool.add(ironWall());
            pool.add(devastate());
            pool.add(vampiricStrike());
        }

        // Legendary cards
        pool.add(apocalypse());
        pool.add(plaguebringer());
        pool.add(divineShield());
        pool.add(executioner());

        return pool.get(new java.util.Random().nextInt(pool.size()));
    }

    /**
     * คืนการ์ดทุกใบใน ExtendedCardLibrary — ใช้โดย SaveManager ตอนโหลด save
     * เพื่อ restore การ์ดที่ได้มาจาก shop/reward กลับมาพร้อม stats ครบถ้วน
     */
    public static java.util.ArrayList<Card> getAllExtendedCards() {
        java.util.ArrayList<Card> all = new java.util.ArrayList<>();
        // Common
        all.add(exposeWeakness());
        all.add(poisonedBlade());
        all.add(serpentStrike());
        all.add(protectiveStance());
        all.add(uppercut());
        all.add(strike());
        all.add(defend());
        all.add(bash());
        all.add(healingPotion());
        // Rare
        all.add(crushingBlow());
        all.add(toxicCloud());
        all.add(fortify());
        all.add(poisonousDefense());
        all.add(hemorrhage());
        all.add(bloodPact());
        all.add(flurry());
        all.add(whirlwind());
        all.add(meditation());
        all.add(battleRage());
        all.add(weakenFoe());
        all.add(twinStrike());
        all.add(vampiricStrike());
        // Epic
        all.add(venomStrike());
        all.add(ironWall());
        all.add(devastate());
        // Legendary
        all.add(apocalypse());
        all.add(plaguebringer());
        all.add(divineShield());
        all.add(executioner());
        return all;
    }
}