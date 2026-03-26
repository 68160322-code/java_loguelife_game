package core;

import card.Card;
import card.CardType;
import card.CardLibrary;
import item.Relic;
import item.RelicLibrary;

import java.util.prefs.Preferences;
import java.util.ArrayList;

/**
 * SaveManager — บันทึก/โหลด game state ผ่าน Java Preferences API
 * เก็บข้อมูลใน registry (Windows) หรือ ~/.java/.userPrefs (Mac/Linux)
 * ไม่ต้องใช้ไฟล์ภายนอก
 *
 * สิ่งที่บันทึก:
 *  - HP, MaxHP, Gold, Level
 *  - Deck (ชื่อการ์ดแต่ละใบ คั่นด้วย |)
 *  - Relics (ชื่อ relic แต่ละชิ้น คั่นด้วย |)
 */
public class SaveManager {

    private static final Preferences PREFS = Preferences.userRoot().node("RogueCardGame");

    private static final String KEY_EXISTS    = "save_exists";
    private static final String KEY_HP        = "hp";
    private static final String KEY_MAX_HP    = "max_hp";
    private static final String KEY_GOLD      = "gold";
    private static final String KEY_LEVEL     = "level";
    private static final String KEY_MAX_ENERGY= "max_energy";
    private static final String KEY_MAP_COL    = "map_col";
    private static final String KEY_MAP_ROW    = "map_row";
    private static final String KEY_DECK       = "deck";
    private static final String KEY_RELICS     = "relics";
    private static final String KEY_IN_BATTLE   = "in_battle";
    private static final String KEY_BATTLE_TYPE = "battle_type";
    private static final String KEY_ENEMY_HP    = "enemy_hp";
    private static final String KEY_ENEMY_MAX_HP= "enemy_max_hp";
    private static final String KEY_ENEMY_NAME  = "enemy_name";
    private static final String KEY_ENEMY_CLASS = "enemy_class";   // "Enemy","BossEnemy","RageBoss","ImprovedEnemy"
    private static final String KEY_ENEMY_STR   = "enemy_str";
    private static final String KEY_ENEMY_PSN   = "enemy_psn";
    private static final String KEY_ENEMY_WEAK  = "enemy_weak";
    private static final String KEY_ENEMY_VUL   = "enemy_vul";
    private static final String KEY_ENEMY_INTENT= "enemy_intent";
    private static final String KEY_ENEMY_INTV  = "enemy_intv";
    private static final String SEPARATOR       = "|||";

    // ── Check ─────────────────────────────────────────────────────────────────
    public static boolean hasSave() {
        return PREFS.getBoolean(KEY_EXISTS, false);
    }

    // ── Save ──────────────────────────────────────────────────────────────────
    public static void save(core.GameState state) {
        save(state, null, -1, -1);
    }

    public static void save(core.GameState state, map.MapScreen mapScreen, int mapCol, int mapRow) {
        save(state, mapScreen, mapCol, mapRow, false, null);
    }

    public static void save(core.GameState state, map.MapScreen mapScreen, int mapCol, int mapRow,
                            boolean inBattle, map.MapNode.NodeType battleType) {
        try {
            entity.Player p = state.getPlayer();

            PREFS.putBoolean(KEY_EXISTS,     true);
            PREFS.putInt(KEY_HP,             p.getHp());
            PREFS.putInt(KEY_MAX_HP,         p.getMaxHp());
            PREFS.putInt(KEY_GOLD,           state.getGold());
            PREFS.putInt(KEY_LEVEL,          state.getLevel());
            PREFS.putInt(KEY_MAX_ENERGY,     p.getMaxEnergy());
            PREFS.putInt(KEY_MAP_COL,        mapCol);
            PREFS.putInt(KEY_MAP_ROW,        mapRow);
            PREFS.putBoolean(KEY_IN_BATTLE,  inBattle);
            PREFS.put(KEY_BATTLE_TYPE,       battleType != null ? battleType.name() : "BATTLE");

            // บันทึก enemy state (ถ้ากำลังสู้อยู่)
            if (inBattle && state.getEnemy() != null) {
                entity.Enemy e = state.getEnemy();
                PREFS.putInt(KEY_ENEMY_HP,      e.getHp());
                PREFS.putInt(KEY_ENEMY_MAX_HP,  e.getMaxHp());
                PREFS.putInt(KEY_ENEMY_STR,     e.getStrength());
                PREFS.putInt(KEY_ENEMY_PSN,     e.getPoison());
                PREFS.putInt(KEY_ENEMY_WEAK,    e.getWeak());
                PREFS.putInt(KEY_ENEMY_VUL,     e.getVulnerable());
                PREFS.put(KEY_ENEMY_NAME,       e.getName());
                PREFS.put(KEY_ENEMY_INTENT,     e.getIntent());
                PREFS.putInt(KEY_ENEMY_INTV,    e.getIntentValue());
                // บันทึก class ของ enemy เพื่อ restore ถูกประเภท
                String eClass = "ImprovedEnemy";
                if (e instanceof entity.RageBoss)      eClass = "RageBoss";
                else if (e instanceof entity.BossEnemy) eClass = "BossEnemy";
                PREFS.put(KEY_ENEMY_CLASS, eClass);
            }

            // Deck — บันทึก stats ครบทุก field แทนการเก็บแค่ชื่อ
            ArrayList<Card> deck = state.getDeck().getMasterDeck();
            StringBuilder deckSB = new StringBuilder();
            for (Card c : deck) {
                if (deckSB.length() > 0) deckSB.append(SEPARATOR);
                // format: name|type|rarity|cost|dmg|heal|blk|psn|str|weak|vul|exhaust|multiHit|energyBurst|upgraded
                deckSB.append(c.getName())
                        .append("|").append(c.getType().name())
                        .append("|").append(c.getRarity().name())
                        .append("|").append(c.getCost())
                        .append("|").append(c.getDamage())
                        .append("|").append(c.getHeal())
                        .append("|").append(c.getBlock())
                        .append("|").append(c.getPoison())
                        .append("|").append(c.getStrength())
                        .append("|").append(c.getWeak())
                        .append("|").append(c.getVulnerable())
                        .append("|").append(c.isExhaust()     ? 1 : 0)
                        .append("|").append(c.isMultiHit()    ? 1 : 0)
                        .append("|").append(c.isEnergyBurst() ? 1 : 0)
                        .append("|").append(c.isUpgraded()    ? 1 : 0);
            }
            PREFS.put(KEY_DECK, deckSB.toString());

            // Relics
            StringBuilder relicSB = new StringBuilder();
            for (Relic r : p.getRelics()) {
                if (relicSB.length() > 0) relicSB.append(SEPARATOR);
                relicSB.append(r.getName())
                        .append(",").append(r.getType().name())
                        .append(",").append(r.getRarity().name())
                        .append(",").append(r.getDescription().replace(",", ";"));
            }
            PREFS.put(KEY_RELICS, relicSB.toString());

            // Map node states — "col,row,V/L/A/N" สำหรับ visited/locked/available/none
            if (mapScreen != null) {
                StringBuilder mapSB = new StringBuilder();
                for (int c = 0; c < map.MapScreen.COLS; c++) {
                    for (int r = 0; r < map.MapScreen.ROWS; r++) {
                        map.MapNode n = mapScreen.getNode(c, r);
                        if (n == null) continue;
                        String status = n.isVisited()   ? "V"
                                : n.isLocked()    ? "L"
                                : n.isAvailable() ? "A" : "N";
                        if (mapSB.length() > 0) mapSB.append(SEPARATOR);
                        mapSB.append(c).append(",").append(r).append(",").append(status)
                                .append(",").append(n.getType().name());
                    }
                }
                PREFS.put("map_state", mapSB.toString());
            }

            PREFS.flush();
            System.out.println("[Save] Game saved — Level " + state.getLevel() + " col=" + mapCol + " row=" + mapRow);
        } catch (Exception e) {
            System.err.println("[Save] Failed: " + e.getMessage());
        }
    }

    // ── Load ──────────────────────────────────────────────────────────────────
    public static core.GameState load() {
        try {
            int hp        = PREFS.getInt(KEY_HP,         80);
            int maxHp     = PREFS.getInt(KEY_MAX_HP,     80);
            int gold      = PREFS.getInt(KEY_GOLD,       120);
            int level     = PREFS.getInt(KEY_LEVEL,      1);
            int maxEnergy = PREFS.getInt(KEY_MAX_ENERGY, 3);

            // ใช้ loadMode constructor — ไม่ reset deck หรือ spawn enemy ใหม่
            core.GameState state = new core.GameState(true);
            state.setLevelDirect(level);
            state.setGoldDirect(gold);

            entity.Player p = state.getPlayer();
            p.setStats(hp, maxHp, maxEnergy);

            // โหลด Deck — อ่านจาก stats โดยตรง ไม่ต้องค้น library
            String deckStr = PREFS.get(KEY_DECK, "");
            if (!deckStr.isEmpty()) {
                for (String entry : deckStr.split("\\|\\|\\|")) {
                    if (entry.isEmpty()) continue;
                    String[] p2 = entry.split("\\|", -1);

                    if (p2.length >= 15) {
                        // รูปแบบใหม่ — มี stats ครบ
                        try {
                            String   cName    = p2[0];
                            CardType cType    = CardType.valueOf(p2[1]);
                            Card.Rarity cRar  = Card.Rarity.valueOf(p2[2]);
                            int cost          = Integer.parseInt(p2[3]);
                            int dmg           = Integer.parseInt(p2[4]);
                            int heal          = Integer.parseInt(p2[5]);
                            int blk           = Integer.parseInt(p2[6]);
                            int psn           = Integer.parseInt(p2[7]);
                            int str           = Integer.parseInt(p2[8]);
                            int weak          = Integer.parseInt(p2[9]);
                            int vul           = Integer.parseInt(p2[10]);
                            boolean exhaust   = p2[11].equals("1");
                            boolean multiHit  = p2[12].equals("1");
                            boolean energyBst = p2[13].equals("1");
                            boolean upgraded  = p2[14].equals("1");

                            Card c = new Card(cName, cType, cRar)
                                    .cost(cost);
                            if (dmg  > 0) c.damage(dmg);
                            if (heal > 0) c.heal(heal);
                            if (blk  > 0) c.block(blk);
                            if (psn  > 0) c.poison(psn);
                            if (str  > 0) c.strength(str);
                            if (weak > 0) c.weak(weak);
                            if (vul  > 0) c.vulnerable(vul);
                            if (exhaust)   c.exhaust();
                            if (multiHit)  c.multiHit();
                            if (energyBst) c.energyBurst();
                            // การ์ดที่ upgraded แล้ว — ชื่อมี + อยู่แล้ว ไม่ต้อง upgrade ซ้ำ
                            state.getDeck().addToMasterDeck(c);
                        } catch (Exception ex) {
                            System.err.println("[Load] Card parse error: " + ex.getMessage() + " entry=" + entry);
                        }
                    } else {
                        // รูปแบบเก่า (compat) — ค้น library ตามชื่อ
                        String[] parts = entry.split(",", 4);
                        if (parts.length < 3) continue;
                        String cardName = parts[0];
                        CardType type   = CardType.valueOf(parts[1]);
                        Card.Rarity rar = Card.Rarity.valueOf(parts[2]);
                        int cost        = parts.length >= 4 ? Integer.parseInt(parts[3]) : 1;
                        Card found = findCardByName(cardName, type, rar);
                        if (found == null) {
                            found = new Card(cardName, type, rar).cost(cost);
                            System.err.println("[Load] Fallback card (no stats): " + cardName);
                        }
                        state.getDeck().addToMasterDeck(found);
                    }
                }
            }

            // โหลด Relics
            String relicStr = PREFS.get(KEY_RELICS, "");
            if (!relicStr.isEmpty()) {
                for (String entry : relicStr.split("\\|\\|\\|")) {
                    String[] parts = entry.split(",", 4);
                    if (parts.length < 3) continue;
                    String name              = parts[0];
                    Relic.RelicType relicType = Relic.RelicType.valueOf(parts[1]);
                    Relic.Rarity rar         = Relic.Rarity.valueOf(parts[2]);
                    String desc              = parts.length >= 4 ? parts[3].replace(";", ",") : "";
                    p.addRelic(new Relic(name, desc, relicType, rar));
                }
            }

            System.out.println("[Load] Game loaded — Level " + level + " HP " + hp + "/" + maxHp);
            return state;

        } catch (Exception e) {
            System.err.println("[Load] Failed: " + e.getMessage());
            return new core.GameState();
        }
    }

    /** โหลดตำแหน่งบน map ที่บันทึกไว้ */
    public static int loadMapCol() { return PREFS.getInt(KEY_MAP_COL, -1); }
    public static int loadMapRow() { return PREFS.getInt(KEY_MAP_ROW, -1); }
    /** โหลดสถานะว่ากำลังสู้อยู่ไหมตอน save */
    public static boolean loadInBattle() { return PREFS.getBoolean(KEY_IN_BATTLE, false); }
    /** โหลดประเภทการต่อสู้ที่ค้างอยู่ */
    public static map.MapNode.NodeType loadBattleType() {
        try {
            return map.MapNode.NodeType.valueOf(PREFS.get(KEY_BATTLE_TYPE, "BATTLE"));
        } catch (Exception e) {
            return map.MapNode.NodeType.BATTLE;
        }
    }

    /**
     * Restore enemy state ที่บันทึกไว้เข้า GameState โดยตรง
     * เรียกหลัง startBattle() เพื่อ overwrite enemy ที่ spawn ใหม่
     */
    public static void restoreEnemyState(core.GameState state) {
        try {
            String eClass  = PREFS.get(KEY_ENEMY_CLASS, "");
            if (eClass.isEmpty()) return;

            int eHp    = PREFS.getInt(KEY_ENEMY_HP,     1);
            int eMaxHp = PREFS.getInt(KEY_ENEMY_MAX_HP, 1);
            int eStr   = PREFS.getInt(KEY_ENEMY_STR,    0);
            int ePsn   = PREFS.getInt(KEY_ENEMY_PSN,    0);
            int eWeak  = PREFS.getInt(KEY_ENEMY_WEAK,   0);
            int eVul   = PREFS.getInt(KEY_ENEMY_VUL,    0);
            int eIntV  = PREFS.getInt(KEY_ENEMY_INTV,   0);
            String eName   = PREFS.get(KEY_ENEMY_NAME,   "Enemy");
            String eIntent = PREFS.get(KEY_ENEMY_INTENT, "");

            entity.Enemy e = state.getEnemy();
            if (e == null) return;

            e.setHp(eHp);
            e.setMaxHp(eMaxHp);
            e.setStrength(eStr);
            e.setPoison(ePsn);
            e.setWeak(eWeak);
            e.setVulnerable(eVul);
            e.setName(eName);
            e.setIntentValue(eIntV);
            if (!eIntent.isEmpty()) e.setIntent(eIntent);

            System.out.println("[Load] Enemy restored: " + eName + " HP=" + eHp + "/" + eMaxHp);
        } catch (Exception ex) {
            System.err.println("[Load] Enemy restore failed: " + ex.getMessage());
        }
    }

    /** โหลด map state แล้ว apply ให้ MapScreen — restore ทั้ง type และ state */
    public static void restoreMapState(map.MapScreen mapScreen) {
        String mapStr = PREFS.get("map_state", "");
        if (mapStr.isEmpty()) return;
        try {
            for (String entry : mapStr.split("\\|\\|\\|")) {
                String[] parts = entry.split(",", 4);
                if (parts.length < 4) continue;
                int c      = Integer.parseInt(parts[0]);
                int r      = Integer.parseInt(parts[1]);
                String st  = parts[2];
                map.MapNode n = mapScreen.getNode(c, r);
                if (n == null) continue;

                // restore node type — สำคัญมาก ไม่งั้น generateMap สุ่มใหม่
                try {
                    map.MapNode.NodeType t = map.MapNode.NodeType.valueOf(parts[3]);
                    n.setType(t);
                } catch (Exception ignored) {}

                // restore state
                switch (st) {
                    case "V": n.setVisited(true);    n.setAvailable(false); n.setLocked(false); break;
                    case "L": n.setLocked(true);     n.setAvailable(false); break;
                    case "A": n.setAvailable(true);  n.setLocked(false);    break;
                    default:  n.setAvailable(false);                        break;
                }
            }
        } catch (Exception e) {
            System.err.println("[Load] Map restore failed: " + e.getMessage());
        }
    }

    // ── Clear ─────────────────────────────────────────────────────────────────
    public static void deleteSave() {
        try {
            PREFS.putBoolean(KEY_EXISTS, false);
            PREFS.remove(KEY_DECK);
            PREFS.remove(KEY_RELICS);
            PREFS.remove("map_state");
            PREFS.flush();
            System.out.println("[Save] Save deleted.");
        } catch (Exception e) {
            System.err.println("[Save] Delete failed: " + e.getMessage());
        }
    }

    // ── Helper ────────────────────────────────────────────────────────────────
    private static Card findCardByName(String name, CardType type, Card.Rarity rarity) {
        boolean upgraded = name.endsWith("+");
        String baseName  = upgraded ? name.substring(0, name.length() - 1) : name;

        // 1. ค้นหาจาก CardLibrary หลัก
        for (Card c : CardLibrary.getAllCards()) {
            if (c.getName().equals(name) || c.getName().equals(baseName)) {
                if (upgraded) c.upgrade();
                return c;
            }
        }

        // 2. ค้นหาจาก ExtendedCardLibrary (การ์ดจาก shop/reward)
        for (Card c : card.ExtendedCardLibrary.getAllExtendedCards()) {
            if (c.getName().equals(name) || c.getName().equals(baseName)) {
                if (upgraded) c.upgrade();
                return c;
            }
        }

        // 3. ไม่เจอเลย — log warning แล้วคืน null
        System.err.println("[Load] Warning: card not found in any library: \"" + name + "\"");
        return null;
    }
}