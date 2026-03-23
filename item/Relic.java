package item;

/**
 * Relic — ของสะสมพร้อม Rarity และ RelicType
 */
public class Relic {

    // ── Rarity ────────────────────────────────────────────────────────────────
    public enum Rarity {
        COMMON, RARE, EPIC, LEGENDARY;

        public java.awt.Color color() {
            switch (this) {
                case COMMON:    return new java.awt.Color(160, 160, 170);
                case RARE:      return new java.awt.Color(60,  130, 220);
                case EPIC:      return new java.awt.Color(160, 50,  220);
                case LEGENDARY: return new java.awt.Color(220, 160, 20);
                default:        return java.awt.Color.WHITE;
            }
        }
        public String label() {
            switch (this) {
                case COMMON:    return "Common";
                case RARE:      return "Rare";
                case EPIC:      return "Epic";
                case LEGENDARY: return "Legendary";
                default:        return "";
            }
        }
    }

    // ── RelicType ─────────────────────────────────────────────────────────────
    public enum RelicType {
        VAMPIRIC_FANG,    // Heal 2 HP ทุกครั้งที่เล่น Attack
        IRON_GAUNTLET,    // Block +3
        RAGE_CRYSTAL,     // เริ่ม battle: Strength +1
        POISON_VIAL,      // Poison +2 stack
        TOME_OF_ENERGY,   // เริ่ม battle: Energy +1
        WARRIORS_CREST,   // ชนะศัตรู: heal 10
        GOLD_MEDALLION,   // Shop ราคา -15G
        BONE_CHARM,       // รับดาเมจ -1
        DEMON_MARK,       // เริ่ม battle: Strength +2
        // EPIC
        PHOENIX_FEATHER,  // ครั้งแรกที่ HP = 0, heal 20 แทนตาย
        BLOOD_STONE,      // Attack card ดีล +3 dmg ทุกใบ
        CURSED_HOURGLASS, // เริ่ม battle ด้วย Energy +2 แต่ HP max -10
        // LEGENDARY
        HEART_OF_STORM,   // ทุกเทิร์นที่จบ: deal 3 poison ให้ศัตรูฟรี
        TITANS_CORE,      // Block ทุกใบ +6, Strength +3 ตอนเริ่ม battle
        SOUL_ANCHOR,      // ป้องกัน game over ครั้งเดียว (heal 50% แทน)
        GENERIC
    }

    private String name;
    private String description;
    private RelicType type;
    private Rarity rarity;

    public Relic(String name, String description, RelicType type, Rarity rarity) {
        this.name = name; this.description = description;
        this.type = type; this.rarity = rarity;
    }
    // backward compat
    public Relic(String name, String description, RelicType type) {
        this(name, description, type, Rarity.COMMON);
    }
    public Relic(String name, String description) {
        this(name, description, RelicType.GENERIC, Rarity.COMMON);
    }

    public String getName()        { return name; }
    public String getDescription() { return description; }
    public RelicType getType()     { return type; }
    public Rarity getRarity()      { return rarity; }
}