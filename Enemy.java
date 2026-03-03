import java.util.*;

public class Enemy {
    protected int hp, maxHp, level;
    protected int poison = 0, strength = 0, weak = 0;
    protected String intent = "";
    protected int intentValue = 0;
    protected String name;


    public Enemy(int level) {
        this.level = level;
        this.maxHp = 40 + (level * 10);
        this.hp = maxHp;

        // สุ่มชื่อมอนสเตอร์ทั่วไป
        String[] names = {"Slime", "Goblin", "Skeleton", "Angry Bat", "Ghost"};
        this.name = names[new Random().nextInt(names.length)];
    }

    public String getName() {
        return name;
    }

    // คิดท่าโจมตีล่วงหน้า
    public void decideIntent() {
        intentValue = 5 + (level * 2);
        intent = "Attack " + intentValue;
    }

    // คำนวณดาเมจตอนโจมตีจริง
    public int attack() {
        int dmg = intentValue + strength;
        if (weak > 0) {
            dmg = (int)(dmg * 0.75); // ติดสถานะอ่อนแอ ดาเมจลดลง 25%
        }
        return Math.max(dmg, 0);
    }

    // จัดการดาเมจที่ได้รับ
    public void takeDamage(int dmg) {
        this.hp -= dmg;
        if (this.hp < 0) this.hp = 0; // ป้องกัน HP ติดลบ
    }

    // ระบบสถานะผิดปกติ (Status Effects)
    public void applyPoison() {
        if (poison > 0) {
            hp -= poison;
            poison--;
        }
    }

    public void addPoison(int amount) {
        this.poison += amount;
    }

    public void addStrength(int amount) {
        this.strength += amount;
    }

    public void addWeak(int turns) {
        this.weak += turns;
    }

    public void reduceWeak() {
        if (weak > 0) weak--;
    }

    // เช็คสถานะตัวละคร
    public boolean isDead() {
        return hp <= 0;
    }

    // Getters สำหรับเรียกดูข้อมูล
    public String getIntent() { return intent; }
    public int getHp() { return hp; }
    public int getPoison() { return poison; }
    public int getWeak() { return weak; }
    public int getStrength() { return strength; }
    public int getMaxHp(){ return maxHp; }
}