public class RageBoss extends Enemy {
    private boolean isEnraged = false;

    public RageBoss(int level) {
        super(level);
        this.maxHp = 120 + (level * 15);
        this.hp = maxHp;
    }

    @Override
    public void decideIntent() {
        if (isEnraged) {
            // โหมดบ้าคลั่ง: แสดงดาเมจจริงรวม strength
            int rageDmg = intentValue + strength;
            if (weak > 0) rageDmg = (int)(rageDmg * 0.75);
            intent = "[RAGE] Attack: " + Math.max(rageDmg + 5, 0) + " dmg (boosted)";
        } else {
            // โหมดปกติ
            intentValue = 8 + (level * 2);
            int displayDmg = intentValue + strength;
            if (weak > 0) displayDmg = (int)(displayDmg * 0.75);
            intent = "[ATK] Attack: " + Math.max(displayDmg, 0) + " dmg";
        }
    }

    @Override
    public void takeDamage(int dmg) {
        super.takeDamage(dmg);
        // Mechanic: เมื่อเลือดต่ำกว่า 50% จะเข้าสู่โหมดบ้าคลั่งทันที
        if (hp < maxHp / 2 && !isEnraged) {
            isEnraged = true;
            this.addStrength(5);
            // อัปเดต intent ให้สะท้อนดาเมจจริงหลัง enrage ทันที
            int rageDmg = intentValue + strength;
            if (weak > 0) rageDmg = (int)(rageDmg * 0.75);
            this.intent = "[RAGE] Attack: " + Math.max(rageDmg + 5, 0) + " dmg (boosted)";
        }
    }

    @Override
    public int attack() {
        if (isEnraged) {
            return super.attack() + 5;
        }
        return super.attack();
    }

    @Override
    public String getName() {
        return "Berserker Boss";
    }
}