public class RageBoss extends Enemy {
    private boolean isEnraged = false;

    public RageBoss(int level) {
        super(level);
        this.maxHp = 120 + (level * 15);
        this.hp = maxHp;
    }

    @Override
    public void takeDamage(int dmg) {
        super.takeDamage(dmg);
        // Mechanic: เมื่อเลือดต่ำกว่า 50% จะเข้าสู่โหมดบ้าคลั่งทันที
        if (hp < maxHp / 2 && !isEnraged) {
            isEnraged = true;
            this.addStrength(5); // เพิ่มดาเมจมหาศาล
            this.intent = "💢 ENRAGED!";
            // สามารถเพิ่ม Effect เสียงหรือ Log ใน GameFrame ได้
        }
    }

    @Override
    public int attack() {
        // ถ้าบ้าคลั่ง จะโจมตี 2 ครั้ง (Double Attack)
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