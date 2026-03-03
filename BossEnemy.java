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
        // Mechanic: สลับโหมดทุกๆ 2 เทิร์น
        if (turnCount % 2 == 0) {
            isDefending = true;
            intentValue = 0;
            intent = "🛡️ Iron Shield (Gain Strength & Block)";
        } else {
            isDefending = false;
            intentValue = 10 + (level * 2);
            intent = "⚔️ Heavy Smash " + intentValue;
        }
    }

    @Override
    public int attack() {
        if (isDefending) {
            this.addStrength(1); // บอสแข็งแกร่งขึ้นเรื่อยๆ ถ้าปล่อยให้ตั้งรับ
            return 0; // เทิร์นนี้ไม่โจมตี แต่ไปเพิ่มบัฟแทน
        }
        return super.attack(); // เทิร์นปกติโจมตีตาม intentValue
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