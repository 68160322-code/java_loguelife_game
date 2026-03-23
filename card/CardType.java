package card;

public enum CardType {
    ATTACK, HEAL, SKILL, POISON;

    @Override
    public String toString() {
        switch (this) {
            case ATTACK: return "Attack";
            case SKILL:  return "Skill";
            case HEAL:   return "Heal";
            case POISON: return "Poison";
            default:     return name();
        }
    }
}