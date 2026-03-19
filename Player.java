import java.util.ArrayList; // 🔥 ต้องมีอันนี้

public class Player {
    private int hp;
    private int maxHp;
    private int energy;
    private int maxEnergy = 3;
    private int block = 0;
    private int strength = 0;
    private int weak = 0;
    private int gold = 100;

    // ต้องอยู่ใน Player.java เท่านั้น!
    public void heal(int amount) {
        this.hp = Math.min(this.maxHp, this.hp + amount);
    }

    public int calculateDamage(int baseDamage) {
        double finalDmg = baseDamage + strength;
        if (weak > 0) {
            finalDmg *= 0.75;
        }
        return (int) finalDmg;
    }

    // ระบบ Relic
    private ArrayList<Relic> relics = new ArrayList<>();

    public Player(int maxHp) {
        this.maxHp = maxHp;
        this.hp = maxHp;
        this.energy = maxEnergy;
    }

    // --- Relic & Gold Management ---
    public void addRelic(Relic relic) { relics.add(relic); }
    public ArrayList<Relic> getRelics() { return relics; }

    public int getGold() { return gold; }
    public void addGold(int amount) { gold += amount; }
    public boolean spendGold(int amount) {
        if (gold >= amount) {
            gold -= amount;
            return true;
        }
        return false;
    }

    // --- Battle Logic ---
    public void takeDamage(int dmg) {
        if (block > 0) {
            if (block >= dmg) {
                block -= dmg;
                dmg = 0;
            } else {
                dmg -= block;
                block = 0;
            }
        }
        hp = Math.max(0, hp - dmg);
    }

    public void addBlock(int amount) { block += amount; }
    public void resetBlock() { block = 0; }
    public void resetStrength() { strength = 0; }
    public void reduceStrength() { if (strength > 0) strength--; }
    public void resetEnergy() { energy = maxEnergy; }
    public boolean useEnergy(int cost) {
        if (energy >= cost) {
            energy -= cost;
            return true;
        }
        return false;
    }

    // --- Status Effects ---
    public void addStrength(int amount) { strength += amount; }
    public void addWeak(int turns) { weak += turns; }
    public void reduceWeak() { if (weak > 0) weak--; }

    // --- Level Up & Healing ---
    public void healPercent(int percent) {
        int amount = (int)(maxHp * (percent / 100.0));
        hp = Math.min(maxHp, hp + amount);
    }
    public void increaseMaxHp(int amount) {
        maxHp += amount;
        hp += amount;
    }
    public void increaseMaxEnergy(int amount) { maxEnergy += amount; }

    // --- Getters ---
    public int getHp() { return hp; }
    public int getMaxHp() { return maxHp; }
    public int getEnergy() { return energy; }
    public int getBlock() { return block; }
    public int getStrength() { return strength; }
    public int getWeak() { return weak; }
    public boolean isDead() { return hp <= 0; }
}