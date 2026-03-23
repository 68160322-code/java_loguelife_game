package item;

import java.util.ArrayList;
import java.util.Random;

/**
 * RelicLibrary — คลัง Relic ทั้งหมดแยกตาม Rarity
 */
public class RelicLibrary {

    private static final Random rand = new Random();

    public static ArrayList<Relic> getCommons() {
        ArrayList<Relic> list = new ArrayList<>();
        list.add(new Relic("Vampiric Fang",   "Heal 2 HP each time you play an Attack card",    Relic.RelicType.VAMPIRIC_FANG,   Relic.Rarity.COMMON));
        list.add(new Relic("Iron Gauntlet",   "Your Block cards give +3 extra Block",            Relic.RelicType.IRON_GAUNTLET,   Relic.Rarity.COMMON));
        list.add(new Relic("Bone Charm",      "Take 1 less damage from all attacks",             Relic.RelicType.BONE_CHARM,      Relic.Rarity.COMMON));
        list.add(new Relic("Gold Medallion",  "Shop items cost 15G less",                        Relic.RelicType.GOLD_MEDALLION,  Relic.Rarity.COMMON));
        return list;
    }

    public static ArrayList<Relic> getRares() {
        ArrayList<Relic> list = new ArrayList<>();
        list.add(new Relic("Rage Crystal",    "Start each battle with +1 Strength",              Relic.RelicType.RAGE_CRYSTAL,    Relic.Rarity.RARE));
        list.add(new Relic("Poison Vial",     "All Poison effects deal +2 extra damage",         Relic.RelicType.POISON_VIAL,     Relic.Rarity.RARE));
        list.add(new Relic("Tome of Energy",  "Start each battle with +1 Energy",                Relic.RelicType.TOME_OF_ENERGY,  Relic.Rarity.RARE));
        list.add(new Relic("Warrior's Crest", "Gain 10 HP when you defeat an enemy",             Relic.RelicType.WARRIORS_CREST,  Relic.Rarity.RARE));
        list.add(new Relic("Demon Mark",      "+2 Strength at battle start (permanent pact)",    Relic.RelicType.DEMON_MARK,      Relic.Rarity.RARE));
        return list;
    }

    public static ArrayList<Relic> getEpics() {
        ArrayList<Relic> list = new ArrayList<>();
        list.add(new Relic("Phoenix Feather", "Once per run: when you would die, heal 20 HP instead",
                Relic.RelicType.PHOENIX_FEATHER,  Relic.Rarity.EPIC));
        list.add(new Relic("Blood Stone",     "All Attack cards deal +3 bonus damage",
                Relic.RelicType.BLOOD_STONE,      Relic.Rarity.EPIC));
        list.add(new Relic("Cursed Hourglass","Start battles with +2 Energy, but Max HP -10",
                Relic.RelicType.CURSED_HOURGLASS, Relic.Rarity.EPIC));
        return list;
    }

    public static ArrayList<Relic> getLegendaries() {
        ArrayList<Relic> list = new ArrayList<>();
        list.add(new Relic("Heart of Storm",  "At end of each turn, deal 3 Poison to enemy for free",
                Relic.RelicType.HEART_OF_STORM, Relic.Rarity.LEGENDARY));
        list.add(new Relic("Titan's Core",    "Block cards give +6 Block. Start battles with +3 Strength",
                Relic.RelicType.TITANS_CORE,    Relic.Rarity.LEGENDARY));
        list.add(new Relic("Soul Anchor",     "Once per run: survive a killing blow at 1 HP instead",
                Relic.RelicType.SOUL_ANCHOR,    Relic.Rarity.LEGENDARY));
        return list;
    }

    // ── Random by rarity weight ───────────────────────────────────────────────

    /** สุ่ม Relic สำหรับ Event (ส่วนใหญ่ Common/Rare) */
    public static Relic rollEventRelic() {
        return roll(5, 20, 50);
    }

    /** สุ่ม Relic สำหรับ Shop (Common/Rare เป็นหลัก) */
    public static Relic rollShopRelic() {
        return roll(3, 15, 45);
    }

    /** สุ่ม Relic สำหรับ Elite reward (Rare/Epic เป็นหลัก) */
    public static Relic rollEliteRelic() {
        return roll(10, 40, 50);
    }

    /** สุ่ม Relic สำหรับ Boss reward (Epic/Legendary) */
    public static Relic rollBossRelic() {
        return roll(35, 65, 0);
    }

    private static Relic roll(int legendaryChance, int epicChance, int rareChance) {
        int r = rand.nextInt(100);
        ArrayList<Relic> pool;
        if (r < legendaryChance)                         pool = getLegendaries();
        else if (r < legendaryChance + epicChance)       pool = getEpics();
        else if (r < legendaryChance + epicChance + rareChance) pool = getRares();
        else                                             pool = getCommons();
        if (pool.isEmpty()) pool = getCommons();
        return pool.get(rand.nextInt(pool.size()));
    }
}