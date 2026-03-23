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
    private static final String KEY_MAP_COL   = "map_col";
    private static final String KEY_MAP_ROW   = "map_row";
    private static final String KEY_DECK      = "deck";
    private static final String KEY_RELICS    = "relics";
    private static final String SEPARATOR     = "|||";

    // ── Check ─────────────────────────────────────────────────────────────────
    public static boolean hasSave() {
        return PREFS.getBoolean(KEY_EXISTS, false);
    }

    // ── Save ──────────────────────────────────────────────────────────────────
    public static void save(core.GameState state) {
        save(state, null, -1, -1);
    }

    public static void save(core.GameState state, map.MapScreen mapScreen, int mapCol, int mapRow) {
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

            // Deck
            ArrayList<Card> deck = state.getDeck().getMasterDeck();
            StringBuilder deckSB = new StringBuilder();
            for (Card c : deck) {
                if (deckSB.length() > 0) deckSB.append(SEPARATOR);
                deckSB.append(c.getName())
                        .append(",").append(c.getType().name())
                        .append(",").append(c.getRarity().name())
                        .append(",").append(c.getCost());
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

            // โหลด Deck
            String deckStr = PREFS.get(KEY_DECK, "");
            if (!deckStr.isEmpty()) {
                for (String entry : deckStr.split("\\|\\|\\|")) {
                    String[] parts = entry.split(",", 4);
                    if (parts.length < 3) continue;
                    String cardName = parts[0];
                    CardType type   = CardType.valueOf(parts[1]);
                    Card.Rarity rar = Card.Rarity.valueOf(parts[2]);
                    int cost        = parts.length >= 4 ? Integer.parseInt(parts[3]) : 1;
                    Card found = findCardByName(cardName, type, rar);
                    if (found == null) found = new Card(cardName, type, rar).cost(cost);
                    state.getDeck().addToMasterDeck(found);
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
        // ค้นหาจาก CardLibrary ก่อน
        ArrayList<Card> all = CardLibrary.getAllCards();
        for (Card c : all) {
            if (c.getName().equals(name) || c.getName().equals(name.replace("+", ""))) {
                // ถ้าชื่อมี + แสดงว่า upgraded
                if (name.endsWith("+")) c.upgrade();
                return c;
            }
        }
        return null; // ไม่เจอ
    }
}