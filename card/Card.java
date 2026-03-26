package card;

import entity.Player;
import entity.Enemy;

public class Card {

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

    private String name;
    private CardType type;
    private Rarity rarity;
    private int damage, heal, block, cost, poison, strength, weak, vulnerable;
    private boolean exhaust;
    private boolean isUpgraded = false;
    private boolean multiHit = false;   // Twin Strike: ตี 2 ครั้ง
    private boolean energyBurst = false; // Whirlwind: ใช้ energy ทั้งหมด

    public Card(String name, CardType type) {
        this(name, type, Rarity.COMMON);
    }
    public Card(String name, CardType type, Rarity rarity) {
        this.name   = name;
        this.type   = type;
        this.rarity = rarity;
    }

    public Rarity getRarity() { return rarity; }

    // Builder Methods
    public Card damage(int v) { this.damage = v; return this; }
    public Card block(int v) { this.block = v; return this; }
    public Card cost(int v) { this.cost = v; return this; }
    public Card poison(int v) { this.poison = v; return this; }
    public Card heal(int v) { this.heal = v; return this; }
    public Card strength(int v) { this.strength = v; return this; }
    public Card weak(int v) { this.weak = v; return this; }
    public Card vulnerable(int v) { this.vulnerable = v; return this; }
    public Card exhaust() { this.exhaust = true; return this; }
    public Card multiHit() { this.multiHit = true; return this; }
    public Card energyBurst() { this.energyBurst = true; return this; }

    public void play(Player p, Enemy e) {
        if (energyBurst) {
            // ใช้ damage field เป็น dmg per energy (default 5 ถ้าไม่ได้ set)
            int dmgPerEnergy = damage > 0 ? damage : 5;
            int energyLeft   = p.getEnergy();
            int totalDmg     = p.calculateDamage(dmgPerEnergy) * energyLeft;
            e.takeDamage(totalDmg);
            p.useEnergy(energyLeft);
        } else if (multiHit && damage > 0) {
            // Twin Strike: ตี 2 ครั้งแยกกัน
            e.takeDamage(p.calculateDamage(damage));
            e.takeDamage(p.calculateDamage(damage));
        } else {
            if (damage > 0) e.takeDamage(p.calculateDamage(damage));
        }
        if (heal > 0) p.heal(heal);
        if (block > 0) p.addBlock(block);
        if (poison > 0) e.addPoison(poison);
        if (strength > 0) p.addStrength(strength);
        if (weak > 0) e.addWeak(weak);
        if (vulnerable > 0) e.addVulnerable(vulnerable);
    }

    public void upgrade() {
        if (isUpgraded) return;
        this.name += "+";
        this.isUpgraded = true;

        // ── Upgrade logic ตาม card mechanic ──────────────────────────────────
        // energyBurst (Whirlwind, Chaos Surge): ลด cost แทน
        if (energyBurst) {
            // cost 0 → 0 อยู่แล้ว แต่เพิ่ม strength bonus
            this.strength += 1;
            return;
        }

        // multiHit (Twin Strike): เพิ่ม dmg ต่อหนึ่งฮิต
        if (multiHit && damage > 0) {
            this.damage += upgradeAmount(damage, rarity, 0.25f); // +25%
            return;
        }

        // exhaust card: buff ใหญ่กว่าปกติ เพราะใช้ได้ครั้งเดียว
        if (exhaust) {
            if (damage > 0)   this.damage   += upgradeAmount(damage,   rarity, 0.4f);
            if (block > 0)    this.block    += upgradeAmount(block,    rarity, 0.4f);
            if (heal > 0)     this.heal     += upgradeAmount(heal,     rarity, 0.4f);
            if (poison > 0)   this.poison   += upgradeAmount(poison,   rarity, 0.4f);
            if (strength > 0) this.strength += 1;
            if (weak > 0)     this.weak     += 1;
            if (vulnerable > 0) this.vulnerable += 1;
            return;
        }

        // การ์ดที่มีแค่ effect เดียว: buff ใหญ่
        int effectCount = (damage>0?1:0) + (block>0?1:0) + (heal>0?1:0)
                + (poison>0?1:0) + (strength>0?1:0) + (weak>0?1:0) + (vulnerable>0?1:0);

        if (effectCount == 1) {
            if (damage > 0)   this.damage   += upgradeAmount(damage,   rarity, 0.35f);
            if (block > 0)    this.block    += upgradeAmount(block,    rarity, 0.35f);
            if (heal > 0)     this.heal     += upgradeAmount(heal,     rarity, 0.35f);
            if (poison > 0)   this.poison   += upgradeAmount(poison,   rarity, 0.35f);
            if (strength > 0) this.strength += 1;
            if (weak > 0)     this.weak     += 1;
            if (vulnerable > 0) this.vulnerable += 1;
        } else {
            // การ์ดที่มีหลาย effect: buff เล็กลงแต่ครบทุก stat หรือลด cost
            boolean costReduced = false;
            if (cost >= 2 && damage > 0 && block > 0) {
                // dual attack+block → ลด cost แทน buff stat
                this.cost -= 1;
                costReduced = true;
            }
            if (!costReduced) {
                if (damage > 0)   this.damage   += upgradeAmount(damage,   rarity, 0.25f);
                if (block > 0)    this.block    += upgradeAmount(block,    rarity, 0.25f);
                if (heal > 0)     this.heal     += upgradeAmount(heal,     rarity, 0.25f);
                if (poison > 0)   this.poison   += upgradeAmount(poison,   rarity, 0.25f);
                if (strength > 0) this.strength += 1;
                if (weak > 0)     this.weak     += 1;
                if (vulnerable > 0) this.vulnerable += 1;
            }
        }
    }

    /**
     * คำนวณ upgrade amount ตาม base value และ rarity
     * rarity สูง → % เพิ่มน้อยกว่า (เพราะ base value สูงอยู่แล้ว)
     * รับประกัน minimum +1
     */
    private int upgradeAmount(int base, Rarity r, float pct) {
        float multiplier;
        switch (r) {
            case RARE:      multiplier = 0.85f; break;
            case EPIC:      multiplier = 0.70f; break;
            case LEGENDARY: multiplier = 0.55f; break;
            default:        multiplier = 1.00f; break;
        }
        return Math.max(1, Math.round(base * pct * multiplier));
    }

    /**
     * ⭐ FIXED: Complete description with all possible effects
     */
    public String getDescription() {
        StringBuilder sb = new StringBuilder();

        // Check if card has any effects
        boolean hasAnyEffect = damage > 0 || block > 0 || poison > 0 || heal > 0 ||
                strength > 0 || weak > 0 || vulnerable > 0 ||
                multiHit || energyBurst;

        // ถ้าไม่มี effect เลย แสดง message
        if (!hasAnyEffect) {
            sb.append("<i>No effect</i>");
            return sb.toString();
        }

        // Multi-hit attack (แสดงก่อน damage ธรรมดา)
        if (multiHit && damage > 0) {
            sb.append("[ATK] Hits <b>2x</b> for <b>").append(damage).append("</b> damage each<br>");
        }
        // Energy burst attack
        else if (energyBurst) {
            int d = damage > 0 ? damage : 5;
            sb.append("[ATK] Deal <b>").append(d).append("</b> damage per remaining Energy<br>");
        }
        // Normal damage
        else if (damage > 0) {
            sb.append("[ATK] Deal <b>").append(damage).append("</b> damage<br>");
        }

        // Block
        if (block > 0) {
            sb.append("[DEF] Gain <b>").append(block).append("</b> block<br>");
        }

        // Poison
        if (poison > 0) {
            sb.append("[PSN] Apply <b>").append(poison).append("</b> poison<br>");
        }

        // Heal
        if (heal > 0) {
            sb.append("[HEL] Heal <b>").append(heal).append("</b> HP<br>");
        }

        // Strength
        if (strength > 0) {
            sb.append("[STR] Gain <b>").append(strength).append("</b> strength<br>");
        }

        // Weak
        if (weak > 0) {
            sb.append("[WEK] Apply <b>").append(weak).append("</b> weak<br>");
        }

        // Vulnerable
        if (vulnerable > 0) {
            sb.append("[VUL] Apply <b>").append(vulnerable).append("</b> vulnerable<br>");
        }

        // Exhaust
        if (exhaust) {
            sb.append("<i>(Exhaust)</i>");
        }

        return sb.toString();
    }

    public String getName()    { return name; }
    public CardType getType()  { return type; }
    public void setType(CardType t) { this.type = t; }  // ใช้โดย PlayerClass
    public boolean hasPoison() { return poison > 0; }   // ใช้โดย PlayerClass
    public int getCost()       { return cost; }
    public int getHeal()       { return heal; }
    public int getBlock()      { return block; }
    public int getDamage()     { return damage; }
    public int getPoison()     { return poison; }
    public int getStrength()   { return strength; }
    public int getWeak()       { return weak; }
    public int getVulnerable() { return vulnerable; }
    public boolean isExhaust()     { return exhaust; }
    public boolean isMultiHit()    { return multiHit; }
    public boolean isEnergyBurst() { return energyBurst; }
    public boolean isUpgraded()    { return isUpgraded; }
}