public class MapNode {
    public enum NodeType { BATTLE, ELITE, BOSS, EVENT, SHOP, REST }

    private NodeType type;
    private int col, row;
    private boolean visited = false;
    private boolean available = false;
    private boolean locked = false;  // ถูกปิดถาวรเพราะเลือก node อื่นในคอลัมน์นี้แล้ว

    public MapNode(NodeType type, int col, int row) {
        this.type = type; this.col = col; this.row = row;
    }

    public NodeType getType()           { return type; }
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
            case BATTLE: return "⚔";  case ELITE: return "☠";
            case BOSS:   return "♛";  case EVENT: return "?";
            case SHOP:   return "$";  case REST:  return "♨";
            default: return "?";
        }
    }
    public String getLabel() {
        switch (type) {
            case BATTLE: return "Battle"; case ELITE: return "Elite";
            case BOSS:   return "BOSS";   case EVENT: return "Event";
            case SHOP:   return "Shop";   case REST:  return "Rest";
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