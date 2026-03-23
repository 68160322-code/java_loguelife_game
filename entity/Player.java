package entity;

import item.Relic;

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
    public void addRelic(Relic relic) {
        relics.add(relic);
        // apply one-time effects when relic is first obtained
        if (relic.getType() == Relic.RelicType.CURSED_HOURGLASS) {
            maxHp = Math.max(10, maxHp - 10);
            hp    = Math.min(hp, maxHp);
        }
    }
    public ArrayList<Relic> getRelics() { return relics; }

    /** เช็คว่ามี Relic type นี้อยู่มั้ย */
    public boolean hasRelic(Relic.RelicType type) {
        for (Relic r : relics)
            if (r.getType() == type) return true;
        return false;
    }

    private boolean phoenixUsed    = false;
    private boolean soulAnchorUsed = false;

    /** เรียกครั้งเดียวตอนเริ่ม battle — apply passive relics */
    public void applyBattleStartRelics() {
        if (hasRelic(Relic.RelicType.RAGE_CRYSTAL))     addStrength(1);
        if (hasRelic(Relic.RelicType.DEMON_MARK))       addStrength(2);
        if (hasRelic(Relic.RelicType.TITANS_CORE))      addStrength(3);
        // energy relics: เพิ่ม battleMaxEnergy แล้วค่อย reset ใหม่
        if (hasRelic(Relic.RelicType.TOME_OF_ENERGY))   battleEnergyBonus += 1;
        if (hasRelic(Relic.RelicType.CURSED_HOURGLASS)) battleEnergyBonus += 2;
        // Cursed Hourglass ลด max HP ครั้งแรกที่รับ (ทำใน GameState/Player ต้องเช็คว่ายังไม่ได้ลด)
    }

    // bonus energy จาก relic ที่ใช้ใน battle นี้ — reset ทุก battle ใหม่
    private int battleEnergyBonus = 0;

    public void resetBattleState() {
        battleEnergyBonus = 0;
        strength = 0; // reset strength ทุก battle
    }

    public void resetEnergy() {
        energy = maxEnergy + battleEnergyBonus;
    }

    /** เรียกตอนเล่น Attack */
    public void onAttackPlayed() {
        if (hasRelic(Relic.RelicType.VAMPIRIC_FANG)) heal(2);
    }

    /** โบนัสดาเมจจาก Blood Stone */
    public int getAttackBonus() {
        return hasRelic(Relic.RelicType.BLOOD_STONE) ? 3 : 0;
    }

    // ── Player Class ──────────────────────────────────────────────────────────
    private core.PlayerClass playerClass = null;
    public void setPlayerClass(core.PlayerClass pc) { this.playerClass = pc; }
    public core.PlayerClass getPlayerClass()         { return playerClass; }

    /** addBlock + relic bonus + class bonus (Knight +2) */
    public void addBlock(int amount) {
        if (hasRelic(Relic.RelicType.IRON_GAUNTLET)) amount += 3;
        if (hasRelic(Relic.RelicType.TITANS_CORE))   amount += 6;
        if (playerClass != null) amount += playerClass.blockBonus();
        block += amount;
    }

    /** Knight: Block ไม่ reset ตอนจบเทิร์น */
    public void resetBlock() {
        if (playerClass != null && playerClass.persistsBlock()) return;
        block = 0;
    }

    /** reset block เสมอ — เรียกตอนจบ battle (ทุก class) */
    public void forceResetBlock() { block = 0; }

    /** takeDamage พร้อม Bone Charm, Phoenix Feather, Soul Anchor */
    public void takeDamage(int dmg) {
        if (hasRelic(Relic.RelicType.BONE_CHARM)) dmg = Math.max(0, dmg - 1);
        if (block > 0) {
            if (block >= dmg) { block -= dmg; dmg = 0; }
            else { dmg -= block; block = 0; }
        }
        if (!soulAnchorUsed && hasRelic(Relic.RelicType.SOUL_ANCHOR) && hp - dmg <= 0) {
            hp = 1; soulAnchorUsed = true; return;
        }
        hp = Math.max(0, hp - dmg);
        if (hp <= 0 && !phoenixUsed && hasRelic(Relic.RelicType.PHOENIX_FEATHER)) {
            hp = 20; phoenixUsed = true;
        }
    }

    public void onEnemyDefeated() {
        if (hasRelic(Relic.RelicType.WARRIORS_CREST)) heal(10);
    }

    public int getGold() { return gold; }
    public void addGold(int amount) { gold += amount; }
    public boolean spendGold(int amount) {
        if (gold >= amount) {
            gold -= amount;
            return true;
        }
        return false;
    }

    public void resetStrength() { strength = 0; }
    public void reduceStrength() { if (strength > 0) strength--; }
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
    public int getHp()        { return hp; }
    public int getMaxHp()     { return maxHp; }
    public int getEnergy()    { return energy; }
    public int getMaxEnergy() { return maxEnergy; }
    public int getBlock()     { return block; }
    public int getStrength()  { return strength; }
    public int getWeak()      { return weak; }
    public boolean isDead()   { return hp <= 0; }

    /** SaveManager ใช้เซ็ต stats โดยตรงตอนโหลด save */
    public void setStats(int hp, int maxHp, int maxEnergy) {
        this.hp        = hp;
        this.maxHp     = maxHp;
        this.maxEnergy = maxEnergy;
        this.energy    = maxEnergy;
    }
}