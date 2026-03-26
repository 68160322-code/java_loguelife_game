package entity;

import java.util.*;

public class Enemy {
    protected int hp, maxHp, level;
    protected int poison = 0, strength = 0, weak = 0, vulnerable = 0;
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
        // แสดงดาเมจที่แม่นยำ รวม strength และ weak ที่มีอยู่แล้ว
        int displayDmg = intentValue + strength;
        if (weak > 0) displayDmg = (int)(displayDmg * 0.75);
        intent = "[ATK] Attack: " + Math.max(displayDmg, 0) + " dmg";
    }

    // คำนวณดาเมจตอนโจมตีจริง (ใช้ intentValue ที่ประกาศไว้แล้ว เพื่อให้ตรงกับที่แสดง)
    public int attack() {
        if (intentValue <= 0) return 0; // เทิร์นที่ไม่ได้โจมตี
        int dmg = intentValue + strength;
        if (weak > 0) {
            dmg = (int)(dmg * 0.75);
        }
        return Math.max(dmg, 0);
    }

    // อัปเดต intent string ให้ตรงกับดาเมจจริงที่จะโดน (รวม strength + weak แล้ว)
    public void refreshIntentDisplay() {
        if (intentValue <= 0) return; // ไม่ใช่เทิร์นโจมตี ไม่ต้องอัปเดต
        int actualDmg = intentValue + strength;
        if (weak > 0) actualDmg = (int)(actualDmg * 0.75);
        actualDmg = Math.max(actualDmg, 0);
        // แทนที่ตัวเลขเก่าใน intent string ด้วยตัวเลขจริง
        intent = intent.replaceAll("\\d+$", String.valueOf(actualDmg));
    }

    // จัดการดาเมจที่ได้รับ
    public void takeDamage(int dmg) {
        // Apply vulnerable multiplier
        if (vulnerable > 0) {
            dmg = (int)(dmg * 1.5);
        }

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

    public void addVulnerable(int turns) {
        this.vulnerable += turns;
    }

    public void reduceVulnerable() {
        if (vulnerable > 0) vulnerable--;
    }

    public void reduceStatusEffects() {
        reduceWeak();
        reduceVulnerable();
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
    public int getVulnerable() { return vulnerable; }
    public int getMaxHp(){ return maxHp; }
    public int getLevel() { return level; }

    // Setters สำหรับ SaveManager restore enemy state
    public void setHp(int v)          { this.hp = Math.max(0, v); }
    public void setMaxHp(int v)       { this.maxHp = v; }
    public void setPoison(int v)      { this.poison = Math.max(0, v); }
    public void setStrength(int v)    { this.strength = v; }
    public void setWeak(int v)        { this.weak = Math.max(0, v); }
    public void setVulnerable(int v)  { this.vulnerable = Math.max(0, v); }
    public void setName(String n)     { this.name = n; }
    public void setIntent(String s)   { this.intent = s; }
    public void setIntentValue(int v) { this.intentValue = v; }
    public int  getIntentValue()      { return intentValue; }
}