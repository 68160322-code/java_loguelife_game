package map;

public class MapNode {
    public enum NodeType { BATTLE, ELITE, BOSS, EVENT, SHOP, REST }

    private NodeType type;
    private int col, row;
    private boolean visited = false;
    private boolean available = false;
    private boolean locked = false;

    public MapNode(NodeType type, int col, int row) {
        this.type = type; this.col = col; this.row = row;
    }

    public NodeType getType()           { return type; }
    public void setType(NodeType t)     { this.type = t; }
    public int getCol()                 { return col; }
    public int getRow()                 { return row; }
    public boolean isVisited()          { return visited; }
    public void setVisited(boolean v)   { this.visited = v; }
    public boolean isAvailable()        { return available; }
    public void setAvailable(boolean a) { this.available = a; }
    public boolean isLocked()           { return locked; }
    public void setLocked(boolean l)    { this.locked = l; }

    public String getIcon() {
        switch (type) {
            case BATTLE: return "\u2694";  // ⚔
            case ELITE:  return "\u2620";  // ☠
            case BOSS:   return "\u265B";  // ♛
            case EVENT:  return "?";
            case SHOP:   return "$";
            case REST:   return "\u2668";  // ♨
            default: return "?";
        }
    }

    public String getLabel() {
        switch (type) {
            case BATTLE: return "\n" +
                    "Monster";    // ต่อสู้
            case ELITE:  return "Elite Monster";           // เอลิท
            case BOSS:   return "Boss";                        // บอส
            case EVENT:  return "Event"; // เหตุการณ์
            case SHOP:   return "Shop"; // ร้านค้า
            case REST:   return "Rest"; // พักผ่อน
            default: return "???";
        }
    }

    public java.awt.Color getColor() {
        switch (type) {
            case BATTLE: return new java.awt.Color(160, 55, 55);
            case ELITE:  return new java.awt.Color(130, 30, 160);
            case BOSS:   return new java.awt.Color(210, 30, 30);
            case EVENT:  return new java.awt.Color(40, 110, 190);
            case SHOP:   return new java.awt.Color(170, 140, 20);
            case REST:   return new java.awt.Color(30, 140, 80);
            default:     return new java.awt.Color(80, 80, 80);
        }
    }
}