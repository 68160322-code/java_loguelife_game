public class Card {
    private String name;
    private CardType type;
    private int damage, heal, block, cost, poison, strength, weak;
    private boolean exhaust;

    public Card(String name, CardType type) {
        this.name = name;
        this.type = type;
    }

    // Builder Methods (เหมือนเดิมของคุณ)
    public Card damage(int v) { this.damage = v; return this; }
    public Card block(int v) { this.block = v; return this; }
    public Card cost(int v) { this.cost = v; return this; }
    public Card poison(int v) { this.poison = v; return this; }
    public Card exhaust() { this.exhaust = true; return this; }

    public void play(Player p, Enemy e) {
        if (damage > 0) e.takeDamage(p.calculateDamage(damage));
        if (heal > 0) p.heal(heal);
        if (block > 0) p.addBlock(block);
        if (poison > 0) e.addPoison(poison);
        if (weak > 0) e.addWeak(weak);
    }

    public String getDescription() {
        StringBuilder sb = new StringBuilder();
        if (damage > 0) sb.append("Dmg:").append(damage).append(" ");
        if (block > 0) sb.append("Blk:").append(block).append(" ");
        if (poison > 0) sb.append("Psn:").append(poison).append(" ");
        if (exhaust) sb.append("(Exhaust)");
        return sb.toString();
    }

    public String getName() { return name; }
    public int getCost() { return cost; }
    public boolean isExhaust() { return exhaust; }
}