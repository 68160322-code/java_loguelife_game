public class Player {
    private int hp;
    private int maxHp;
    private int energy;
    private int maxEnergy = 3;
    private int block = 0;
    private int strength = 0;
    private int weak = 0;

    public Player(int maxHp) {
        this.maxHp = maxHp;
        this.hp = maxHp;
        resetEnergy();
    }

    public void resetEnergy() { energy = maxEnergy; }

    public boolean useEnergy(int cost) {
        if (energy >= cost) {
            energy -= cost;
            return true;
        }
        return false;
    }

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
        hp -= dmg;
        if (hp < 0) hp = 0;
    }

    public int calculateDamage(int baseDamage) {
        // สูตร: (ดาเมจพื้นฐาน + Strength) * ตัวคูณ Weak
        double finalDmg = baseDamage + strength;
        if (weak > 0) {
            finalDmg *= 0.75; // ลดดาเมจลง 25%
        }
        return (int) finalDmg;
    }

    public void fullHeal() {
        this.hp = maxHp;
    }

    public void healPercent(int percent) {
        int amount = (int)(maxHp * (percent / 100.0));
        this.hp = Math.min(maxHp, hp + amount);
    }

    public void increaseMaxHp(int amount) {
        this.maxHp += amount;
        this.hp += amount;
    }

    public void increaseMaxEnergy(int amount) {
        this.maxEnergy += amount;
    }

    public void addBlock(int amount) { block += amount; }
    public void resetBlock() { block = 0; }
    public void heal(int amount) { hp = Math.min(maxHp, hp + amount); }
    public boolean isDead() { return hp <= 0; }

    // Status Management
    public void addStrength(int amount) { strength += amount; }
    public void addWeak(int turns) { weak += turns; }
    public void reduceWeak() { if (weak > 0) weak--; }

    // Getters
    public int getHp() { return hp; }
    public int getMaxHp(){ return maxHp; }
    public int getEnergy() { return energy; }
    public int getBlock() { return block; }
    public int getStrength() { return strength; }
    public int getWeak() { return weak; }
}