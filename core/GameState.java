package core;

import entity.Player;
import entity.Enemy;
import entity.BossEnemy;
import entity.RageBoss;
import card.Deck;
import item.Relic;
import map.MapNode;

import java.util.*;

public class GameState {
    private Player player;
    private Enemy enemy;
    private Deck deck;
    private int level = 1;
    private int gold = 120;           // เพิ่มจาก 100 → เริ่มต้นง่ายขึ้น
    private ArrayList<Relic> relics = new ArrayList<>();

    public GameState() {
        this.player = new Player(80);
        this.deck   = new Deck();
        this.deck.startNewBattle();
        spawnEnemy();
    }

    /** Constructor สำหรับ SaveManager — ไม่ spawn enemy หรือ start battle */
    public GameState(boolean loadMode) {
        this.player = new Player(80);
        this.deck   = new Deck();
        this.deck.getMasterDeck().clear(); // ล้าง starter deck ออก SaveManager จะใส่เองz
        spawnEnemy(); // spawn enemy placeholder ไว้ก่อน จะถูก override ตอนเข้าต่อสู้
    }

    // ── Enemy Spawning (balance: ลด HP ศัตรูลง ~15%) ──────────────────────
    public void spawnEnemy() {
        if (level % 7 == 0)      enemy = new RageBoss(level);  // BOSS ทุก 7 ด่าน (เดิม 5)
        else if (level % 4 == 0) enemy = new BossEnemy(level); // Elite ทุก 4 ด่าน (เดิม 3)
        else                     enemy = new Enemy(level);
    }

    public void spawnEnemyByType(MapNode.NodeType type) {
        switch (type) {
            case ELITE: enemy = new BossEnemy(level);  break;
            case BOSS:  enemy = new RageBoss(level);   break;
            default:    enemy = new Enemy(level);      break;
        }
    }

    public void nextLevel() {
        level++;
        player.increaseMaxHp(2);
        addGold(30 + (level * 5));   // Gold รางวัลเยอะขึ้นเล็กน้อย
        spawnEnemy();
        deck.startNewBattle();
    }

    public void nextLevelByType(MapNode.NodeType type) {
        level++;
        player.increaseMaxHp(2);
        addGold(30 + (level * 5));
        spawnEnemyByType(type);
        deck.startNewBattle();
    }

    // ── Gold ──────────────────────────────────────────────────────────────
    public int getGold()             { return gold; }
    public void addGold(int amount)  { gold += amount; }
    public void setGoldDirect(int g) { this.gold = g; }   // สำหรับ SaveManager
    public void setLevelDirect(int l){ this.level = l; }  // สำหรับ SaveManager
    public boolean spendGold(int amount) {
        if (gold >= amount) { gold -= amount; return true; }
        return false;
    }

    // ── Relics ────────────────────────────────────────────────────────────
    public void addRelic(Relic r)         { relics.add(r); }
    public ArrayList<Relic> getRelics()   { return relics; }

    // ── Getters ───────────────────────────────────────────────────────────
    public Player getPlayer() { return player; }
    public Enemy  getEnemy()  { return enemy; }
    public Deck   getDeck()   { return deck; }
    public int    getLevel()  { return level; }
}