public class BossEnemy extends Enemy {
    private int turnCount = 0;
    private boolean isDefending = false;

    public BossEnemy(int level) {
        super(level);
        this.maxHp = 100 + (level * 10); // เลือดไม่เยอะเกินไป
        this.hp = maxHp;
    }

    @Override
    public void decideIntent() {
        turnCount++;
        if (turnCount % 2 == 0) {
            isDefending = true;
            intentValue = 0;
            intent = "[DEF] Iron Shield (+1 STR, half dmg taken)";
        } else {
            isDefending = false;
            intentValue = 10 + (level * 2);
            // แสดงดาเมจจริงรวม strength ที่สะสมมาแล้ว
            int displayDmg = intentValue + strength;
            if (weak > 0) displayDmg = (int)(displayDmg * 0.75);
            intent = "[ATK] Heavy Smash: " + Math.max(displayDmg, 0) + " dmg";
        }
    }

    @Override
    public int attack() {
        if (isDefending) {
            this.addStrength(1);
            // อัปเดต intent เทิร์นถัดไปให้สะท้อน strength ที่เพิ่มขึ้น
            return 0;
        }
        // ใช้ intentValue ที่ตั้งไว้ใน decideIntent() ตรงๆ
        int dmg = intentValue + strength;
        if (weak > 0) dmg = (int)(dmg * 0.75);
        return Math.max(dmg, 0);
    }

    @Override
    public void takeDamage(int dmg) {
        if (isDefending) {
            super.takeDamage(dmg / 2); // ขณะตั้งรับ ดาเมจที่บอสได้รับจะลดลงครึ่งหนึ่ง
        } else {
            super.takeDamage(dmg);
        }
    }

    @Override
    public String getName() {
        return "Guardian Boss";
    }
}