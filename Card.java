public class Card {
    private String name;
    private CardType type;
    private int damage, heal, block, cost, poison, strength, weak;
    private boolean exhaust;
    private boolean isUpgraded = false;
    private boolean multiHit = false;   // Twin Strike: ตี 2 ครั้ง
    private boolean energyBurst = false; // Whirlwind: ใช้ energy ทั้งหมด

    public Card(String name, CardType type) {
        this.name = name;
        this.type = type;
    }

    // Builder Methods
    public Card damage(int v) { this.damage = v; return this; }
    public Card block(int v) { this.block = v; return this; }
    public Card cost(int v) { this.cost = v; return this; }
    public Card poison(int v) { this.poison = v; return this; }
    public Card heal(int v) { this.heal = v; return this; }
    public Card strength(int v) { this.strength = v; return this; }
    public Card weak(int v) { this.weak = v; return this; }
    public Card exhaust() { this.exhaust = true; return this; }
    public Card multiHit() { this.multiHit = true; return this; }
    public Card energyBurst() { this.energyBurst = true; return this; }

    public void play(Player p, Enemy e) {
        if (energyBurst) {
            // Whirlwind: ใช้ energy ที่เหลือทั้งหมด ดาเมจ = 5 x energy
            int energyLeft = p.getEnergy();
            int totalDmg = p.calculateDamage(5) * energyLeft;
            e.takeDamage(totalDmg);
            p.useEnergy(energyLeft); // หัก energy ทั้งหมด
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
    }

    public void upgrade() {
        if (!isUpgraded) {
            this.name += "+";
            if (this.damage > 0) this.damage += 5;
            if (this.block > 0) this.block += 4;
            if (this.poison > 0) this.poison += 3;
            if (this.heal > 0) this.heal += 4;
            if (this.strength > 0) this.strength += 1;
            this.isUpgraded = true;
        }
    }

    public String getDescription() {
        StringBuilder sb = new StringBuilder();
        if (damage > 0 && !multiHit && !energyBurst) sb.append("[ATK] Deal <b>").append(damage).append("</b> damage<br>");
        if (block > 0)    sb.append("[DEF] Gain <b>").append(block).append("</b> block<br>");
        if (poison > 0)   sb.append("[PSN] Apply <b>").append(poison).append("</b> poison<br>");
        if (heal > 0)     sb.append("[HEL] Heal <b>").append(heal).append("</b> HP<br>");
        if (strength > 0) sb.append("[STR] Gain <b>").append(strength).append("</b> strength<br>");
        if (weak > 0)     sb.append("[WEK] Apply <b>").append(weak).append("</b> weak<br>");
        if (multiHit)    sb.append("[ATK] Hits <b>2x</b> " + damage + " each<br>");
        if (energyBurst) sb.append("[ATK] 5 dmg x remaining Energy<br>");
        if (exhaust)     sb.append("<i>(Exhaust)</i>");
        return sb.toString();
    }

    public String getName() { return name; }
    public CardType getType() { return type; }
    public int getCost() { return cost; }
    public boolean isExhaust() { return exhaust; }
}