package card;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Random;

/**
 * CardLibrary — คลังการ์ดทั้งหมดแยกตาม Rarity
 *
 * COMMON    — การ์ดพื้นฐาน, ใช้งานง่าย
 * RARE      — effect เพิ่มขึ้น หรือ dual effect
 * EPIC      — mechanic พิเศษ, high risk/reward
 * LEGENDARY — unique mechanic ที่ไม่มีการ์ดอื่นทำได้
 */
public class CardLibrary {

    private static final Random rand = new Random();

    // ── COMMON CARDS ──────────────────────────────────────────────────────────
    public static ArrayList<Card> getCommons() {
        ArrayList<Card> list = new ArrayList<>();

        // Attack
        list.add(new Card("Strike",        CardType.ATTACK, Card.Rarity.COMMON).damage(8).cost(1));
        list.add(new Card("Heavy Strike",  CardType.ATTACK, Card.Rarity.COMMON).damage(14).cost(2));
        list.add(new Card("Quick Stab",    CardType.ATTACK, Card.Rarity.COMMON).damage(5).cost(0));
        list.add(new Card("Twin Strike",   CardType.ATTACK, Card.Rarity.COMMON).damage(6).multiHit().cost(1));
        list.add(new Card("Iron Wave",     CardType.ATTACK, Card.Rarity.COMMON).damage(8).block(4).cost(1));
        list.add(new Card("Bash",          CardType.ATTACK, Card.Rarity.COMMON).damage(10).weak(1).cost(2));

        // Skill
        list.add(new Card("Defend",        CardType.SKILL,  Card.Rarity.COMMON).block(8).cost(1));
        list.add(new Card("Guard Up",      CardType.SKILL,  Card.Rarity.COMMON).block(16).cost(2));
        list.add(new Card("Poison Dart",   CardType.SKILL,  Card.Rarity.COMMON).poison(5).cost(1));
        list.add(new Card("Battle Stance", CardType.SKILL,  Card.Rarity.COMMON).strength(2).cost(1));
        list.add(new Card("Weaken",        CardType.SKILL,  Card.Rarity.COMMON).weak(2).cost(1));

        // Heal
        list.add(new Card("Quick Heal",    CardType.HEAL,   Card.Rarity.COMMON).heal(6).cost(1));
        list.add(new Card("Bandage",       CardType.HEAL,   Card.Rarity.COMMON).heal(4).block(4).cost(1));

        return list;
    }

    // ── RARE CARDS ────────────────────────────────────────────────────────────
    public static ArrayList<Card> getRares() {
        ArrayList<Card> list = new ArrayList<>();

        // Attack
        list.add(new Card("Uppercut",      CardType.ATTACK, Card.Rarity.RARE).damage(14).weak(1).cost(2));
        list.add(new Card("Cleave",        CardType.ATTACK, Card.Rarity.RARE).damage(18).cost(2));
        list.add(new Card("Fury Slash",    CardType.ATTACK, Card.Rarity.RARE).damage(9).multiHit().cost(2));
        list.add(new Card("Poison Blade",  CardType.ATTACK, Card.Rarity.RARE).damage(8).poison(4).cost(2));
        list.add(new Card("Shield Bash",   CardType.ATTACK, Card.Rarity.RARE).damage(12).block(6).cost(2));
        list.add(new Card("Reckless Blow", CardType.ATTACK, Card.Rarity.RARE).damage(22).cost(3));

        // Skill
        list.add(new Card("Fortify",       CardType.SKILL,  Card.Rarity.RARE).block(18).cost(2));
        list.add(new Card("Deadly Poison", CardType.SKILL,  Card.Rarity.RARE).poison(6).cost(1));
        list.add(new Card("Battle Trance", CardType.SKILL,  Card.Rarity.RARE).strength(2).cost(1));
        list.add(new Card("Hex",           CardType.SKILL,  Card.Rarity.RARE).weak(3).poison(3).cost(2));
        list.add(new Card("Iron Shell",    CardType.SKILL,  Card.Rarity.RARE).block(8).strength(1).cost(2));

        // Heal
        list.add(new Card("Second Wind",   CardType.HEAL,   Card.Rarity.RARE).heal(14).cost(2));
        list.add(new Card("War Cry",       CardType.HEAL,   Card.Rarity.RARE).heal(6).strength(1).cost(2));

        return list;
    }

    // ── EPIC CARDS ────────────────────────────────────────────────────────────
    public static ArrayList<Card> getEpics() {
        ArrayList<Card> list = new ArrayList<>();

        // Attack
        // Whirlwind: 6 dmg x remaining Mana (Epic)
        list.add(new Card("Whirlwind",   CardType.ATTACK, Card.Rarity.EPIC).damage(6).energyBurst().cost(0));
        list.add(new Card("Death Strike",    CardType.ATTACK, Card.Rarity.EPIC).damage(35).cost(3));
        list.add(new Card("Blitz",           CardType.ATTACK, Card.Rarity.EPIC).damage(12).multiHit().weak(1).cost(2));
        list.add(new Card("Venom Barrage",   CardType.ATTACK, Card.Rarity.EPIC).damage(12).poison(8).cost(2));
        list.add(new Card("Berserker's Fist",CardType.ATTACK, Card.Rarity.EPIC).damage(16).strength(2).cost(3).exhaust());

        // Skill
        list.add(new Card("Unbreakable",     CardType.SKILL,  Card.Rarity.EPIC).block(28).cost(3));
        list.add(new Card("Corrupted Power", CardType.SKILL,  Card.Rarity.EPIC).strength(4).cost(2).exhaust());
        list.add(new Card("Plague",          CardType.SKILL,  Card.Rarity.EPIC).poison(12).weak(2).cost(2));
        list.add(new Card("Counter Stance",  CardType.SKILL,  Card.Rarity.EPIC).block(12).strength(2).cost(2));

        // Heal
        list.add(new Card("Blood Pact Heal", CardType.HEAL,   Card.Rarity.EPIC).heal(28).cost(2).exhaust());
        list.add(new Card("Adrenaline",      CardType.HEAL,   Card.Rarity.EPIC).heal(16).strength(2).cost(2));

        return list;
    }

    // ── LEGENDARY CARDS ───────────────────────────────────────────────────────
    public static ArrayList<Card> getLegendaries() {
        ArrayList<Card> list = new ArrayList<>();

        list.add(new Card("Blade of Ruin",   CardType.ATTACK, Card.Rarity.LEGENDARY).damage(40).cost(2).exhaust());
        list.add(new Card("Soul Shatter",    CardType.ATTACK, Card.Rarity.LEGENDARY).damage(16).multiHit().weak(3).cost(2));
        // Chaos Surge: 9 dmg x remaining Mana + Strength +3 (Legendary)
        list.add(new Card("Chaos Surge", CardType.ATTACK, Card.Rarity.LEGENDARY).damage(9).energyBurst().strength(3).cost(0));
        list.add(new Card("Eternal Stance",  CardType.SKILL,  Card.Rarity.LEGENDARY).block(18).strength(3).cost(1));
        list.add(new Card("Death's Blessing",CardType.HEAL,   Card.Rarity.LEGENDARY).heal(30).strength(2).block(10).cost(2).exhaust());
        list.add(new Card("Venom God",       CardType.SKILL,  Card.Rarity.LEGENDARY).poison(20).weak(4).cost(3).exhaust());

        return list;
    }

    // ── KNIGHT CLASS CARDS ────────────────────────────────────────────────────
    public static ArrayList<Card> getKnightCards() {
        ArrayList<Card> list = new ArrayList<>();

        // Common
        list.add(new Card("Iron Guard",     CardType.SKILL,  Card.Rarity.COMMON).block(12).cost(1));
        list.add(new Card("Shield Slam",    CardType.ATTACK, Card.Rarity.COMMON).damage(6).block(6).cost(1));
        list.add(new Card("Brace",          CardType.SKILL,  Card.Rarity.COMMON).block(5).strength(1).cost(1));

        // Rare
        list.add(new Card("Shield Bash",    CardType.ATTACK, Card.Rarity.RARE).damage(10).block(8).cost(2));
        list.add(new Card("Bulwark",        CardType.SKILL,  Card.Rarity.RARE).block(20).cost(2));
        list.add(new Card("Counter Blow",   CardType.ATTACK, Card.Rarity.RARE).damage(14).weak(1).cost(2));
        list.add(new Card("Iron Skin",      CardType.SKILL,  Card.Rarity.RARE).block(10).strength(2).cost(2));

        // Epic
        list.add(new Card("Fortress Stance",CardType.SKILL,  Card.Rarity.EPIC).block(16).strength(3).cost(2));
        list.add(new Card("Retribution",    CardType.ATTACK, Card.Rarity.EPIC).damage(18).block(12).cost(2));
        list.add(new Card("Juggernaut",     CardType.ATTACK, Card.Rarity.EPIC).damage(22).strength(2).cost(3));
        list.add(new Card("Unshakeable",    CardType.SKILL,  Card.Rarity.EPIC).block(24).cost(2).exhaust());

        // Legendary
        list.add(new Card("Aegis",          CardType.SKILL,  Card.Rarity.LEGENDARY).block(30).strength(4).cost(2).exhaust());
        list.add(new Card("Iron Colossus",  CardType.ATTACK, Card.Rarity.LEGENDARY).damage(26).block(20).cost(3));

        return list;
    }

    // ── ROGUE CLASS CARDS ─────────────────────────────────────────────────────
    public static ArrayList<Card> getRogueCards() {
        ArrayList<Card> list = new ArrayList<>();

        // Common
        list.add(new Card("Sneak Attack",   CardType.ATTACK, Card.Rarity.COMMON).damage(7).poison(2).cost(1));
        list.add(new Card("Poison Blade",   CardType.ATTACK, Card.Rarity.COMMON).damage(5).poison(4).cost(1));
        list.add(new Card("Envenom",        CardType.SKILL,  Card.Rarity.COMMON).poison(6).cost(1));

        // Rare
        list.add(new Card("Hemorrhage",     CardType.ATTACK, Card.Rarity.RARE).damage(10).poison(5).cost(2));
        list.add(new Card("Deadly Poison",  CardType.SKILL,  Card.Rarity.RARE).poison(9).cost(1));
        list.add(new Card("Quick Slash",    CardType.ATTACK, Card.Rarity.RARE).damage(8).cost(0));
        list.add(new Card("Ambush",         CardType.ATTACK, Card.Rarity.RARE).damage(16).cost(2).exhaust());

        // Epic
        list.add(new Card("Combo Strike",   CardType.ATTACK, Card.Rarity.EPIC).damage(14).poison(4).cost(1));
        list.add(new Card("Venom Barrage",  CardType.SKILL,  Card.Rarity.EPIC).poison(12).weak(2).cost(2));
        list.add(new Card("Shadow Step",    CardType.SKILL,  Card.Rarity.EPIC).poison(8).block(8).cost(1));
        list.add(new Card("Assassinate",    CardType.ATTACK, Card.Rarity.EPIC).damage(24).poison(6).cost(3).exhaust());

        // Legendary
        list.add(new Card("Venom God",      CardType.SKILL,  Card.Rarity.LEGENDARY).poison(20).weak(4).cost(3).exhaust());
        list.add(new Card("Death Mark",     CardType.ATTACK, Card.Rarity.LEGENDARY).damage(20).poison(14).weak(2).cost(3));

        return list;
    }

    /** ดึง class card pool ตาม PlayerClass */
    public static ArrayList<Card> getClassCards(core.PlayerClass pc) {
        if (pc == null) return new ArrayList<>();
        switch (pc) {
            case KNIGHT: return getKnightCards();
            case ROGUE:  return getRogueCards();
            default:     return new ArrayList<>();
        }
    }

    /** ดึง class cards แยกตาม rarity */
    private static ArrayList<Card> getClassCardsByRarity(core.PlayerClass pc, Card.Rarity rarity) {
        ArrayList<Card> filtered = new ArrayList<>();
        for (Card c : getClassCards(pc)) {
            if (c.getRarity() == rarity) filtered.add(c);
        }
        return filtered;
    }

    /** Reward pool — shared 1 ใบ + class-specific 2 ใบ */
    public static ArrayList<Card> getRewardPool(int level, core.PlayerClass pc) {
        ArrayList<Card> pool = new ArrayList<>();
        int legendaryChance = Math.min(level * 3, 20);
        int epicChance      = Math.min(level * 8, 40);
        int rareChance      = 40;

        // 1 shared card
        pool.add(rollCard(legendaryChance, epicChance, rareChance));

        // 2 class-specific cards
        for (int i = 0; i < 2; i++) {
            pool.add(rollClassCard(legendaryChance, epicChance, rareChance, pc));
        }
        return pool;
    }

    /** Backward-compatible overload ไม่มี class */
    public static ArrayList<Card> getRewardPool(int level) {
        return getRewardPool(level, null);
    }

    /** สุ่ม class card ถ้าไม่มี class → fallback เป็น shared */
    public static Card rollClassCard(int legendary, int epic, int rare, core.PlayerClass pc) {
        if (pc == null) return rollCard(legendary, epic, rare);
        int roll = rand.nextInt(100);
        Card.Rarity rarity;
        if (roll < legendary)               rarity = Card.Rarity.LEGENDARY;
        else if (roll < legendary + epic)   rarity = Card.Rarity.EPIC;
        else if (roll < legendary+epic+rare)rarity = Card.Rarity.RARE;
        else                                rarity = Card.Rarity.COMMON;

        ArrayList<Card> source = getClassCardsByRarity(pc, rarity);
        if (source.isEmpty()) source = getClassCards(pc); // fallback ถ้า rarity นั้นว่าง
        if (source.isEmpty()) return rollCard(legendary, epic, rare); // fallback shared
        return source.get(rand.nextInt(source.size()));
    }

    /** Shop — 1 shared + 1 class + 1 random */
    public static Card rollShopCard(core.PlayerClass pc) {
        int roll = rand.nextInt(3);
        if (roll == 0) return rollClassCard(2, 15, 45, pc);
        return rollCard(2, 15, 45);
    }

    /** Backward-compatible overload */
    public static Card rollShopCard() { return rollShopCard(null); }

    // ── Starter Deck (ตาม class) ──────────────────────────────────────────────
    public static ArrayList<Card> getStarterDeck(core.PlayerClass pc) {
        ArrayList<Card> deck = new ArrayList<>();
        // shared starter
        for (int i = 0; i < 4; i++) deck.add(new Card("Strike", CardType.ATTACK, Card.Rarity.COMMON).damage(8).cost(1));
        for (int i = 0; i < 4; i++) deck.add(new Card("Defend", CardType.SKILL,  Card.Rarity.COMMON).block(8).cost(1));
        // class starter card
        if (pc == core.PlayerClass.KNIGHT) {
            deck.add(new Card("Iron Guard",  CardType.SKILL,  Card.Rarity.COMMON).block(12).cost(1));
            deck.add(new Card("Shield Slam", CardType.ATTACK, Card.Rarity.COMMON).damage(6).block(6).cost(1));
        } else if (pc == core.PlayerClass.ROGUE) {
            deck.add(new Card("Sneak Attack",CardType.ATTACK, Card.Rarity.COMMON).damage(7).poison(2).cost(1));
            deck.add(new Card("Envenom",     CardType.SKILL,  Card.Rarity.COMMON).poison(6).cost(1));
        }
        return deck;
    }

    public static ArrayList<Card> getStarterDeck() { return getStarterDeck(null); }

    /** สุ่มการ์ด 1 ใบตาม weight */
    public static Card rollCard(int legendaryChance, int epicChance, int rareChance) {
        int roll = rand.nextInt(100);
        ArrayList<Card> source;
        if (roll < legendaryChance)                          source = getLegendaries();
        else if (roll < legendaryChance + epicChance)        source = getEpics();
        else if (roll < legendaryChance + epicChance + rareChance) source = getRares();
        else                                                 source = getCommons();
        return source.get(rand.nextInt(source.size()));
    }

    /** สุ่มการ์ด Upgraded สำหรับ Event (Rare ขึ้นไป, class-aware) */
    public static Card rollEventCard(core.PlayerClass pc) {
        Card c = rand.nextBoolean() ? rollClassCard(10, 35, 55, pc) : rollCard(10, 35, 55);
        c.upgrade();
        return c;
    }
    public static Card rollEventCard() { return rollEventCard(null); }

    /** ดึงการ์ดทั้งหมดรวมกัน */
    public static ArrayList<Card> getAllCards() {
        ArrayList<Card> all = new ArrayList<>();
        all.addAll(getCommons()); all.addAll(getRares());
        all.addAll(getEpics());   all.addAll(getLegendaries());
        return all;
    }
}