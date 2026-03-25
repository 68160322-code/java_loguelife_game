package entity;

import java.util.*;

/**
 * Enhanced Enemy Class
 *
 * Improvements:
 * - Added Vulnerable status effect (take 50% more damage)
 * - Added Block mechanic for defensive enemies
 * - Better AI with multiple attack patterns
 * - Special abilities for higher level enemies
 * - More varied monster types with unique abilities
 */
public class ImprovedEnemy extends Enemy {
    protected int block = 0;
    protected int vulnerable = 0;
    protected EnemyType enemyType;
    protected boolean hasSpecialAbility = false;
    protected int specialAbilityCooldown = 0;

    public enum EnemyType {
        SLIME,      // Basic enemy
        GOBLIN,     // Fast attacks
        SKELETON,   // Defensive with block
        BAT,        // Multi-hit attacks
        GHOST,      // Applies weak
        CULTIST,    // Gains strength over time
        MUSHROOM    // Applies poison
    }

    public ImprovedEnemy(int level) {
        super(level);

        // Randomly assign enemy type
        EnemyType[] types = EnemyType.values();
        this.enemyType = types[new Random().nextInt(types.length)];

        // Set name and stats based on type
        initializeEnemyType();

        // Higher level enemies have special abilities
        if (level >= 3) {
            hasSpecialAbility = true;
        }
    }

    private void initializeEnemyType() {
        Random rand = new Random();

        switch (enemyType) {
            case SLIME:
                this.name = "Slime";
                this.maxHp = 35 + (level * 8);
                break;
            case GOBLIN:
                this.name = "Goblin Scout";
                this.maxHp = 30 + (level * 7);
                this.strength = 1; // Starts with +1 strength
                break;
            case SKELETON:
                this.name = "Skeleton Warrior";
                this.maxHp = 40 + (level * 10);
                break;
            case BAT:
                this.name = "Angry Bat";
                this.maxHp = 25 + (level * 6);
                break;
            case GHOST:
                this.name = "Phantom";
                this.maxHp = 28 + (level * 7);
                break;
            case CULTIST:
                this.name = "Dark Cultist";
                this.maxHp = 45 + (level * 11);
                break;
            case MUSHROOM:
                this.name = "Toxic Mushroom";
                this.maxHp = 33 + (level * 9);
                this.poison = 2; // Starts with self-poison (represents spores)
                break;
        }

        this.hp = maxHp;
    }

    @Override
    public void decideIntent() {
        Random rand = new Random();

        // Special ability check
        if (hasSpecialAbility && specialAbilityCooldown == 0 && rand.nextInt(100) < 30) {
            useSpecialAbility();
            specialAbilityCooldown = 3;
            return;
        }

        // Reduce cooldown
        if (specialAbilityCooldown > 0) {
            specialAbilityCooldown--;
        }

        // Type-specific behaviors
        switch (enemyType) {
            case GOBLIN:
                // Goblins attack frequently with low damage
                intentValue = 4 + (level * 2);
                intent = "[ATK] Quick Strike: " + calculateDisplayDamage() + " dmg";
                break;

            case SKELETON:
                // Skeletons alternate between blocking and attacking
                if (rand.nextBoolean()) {
                    intentValue = 0;
                    int blockAmt = 6 + (level * 2);
                    intent = "[DEF] Defend: Gain " + blockAmt + " block";
                } else {
                    intentValue = 6 + (level * 3);
                    intent = "[ATK] Bone Strike: " + calculateDisplayDamage() + " dmg";
                }
                break;

            case BAT:
                // Bats do multiple weak hits
                intentValue = 3 + level;
                intent = "[ATK] Flutter Attack: " + calculateDisplayDamage() + " dmg x2";
                break;

            case GHOST:
                // Ghosts apply weak
                if (rand.nextInt(100) < 40) {
                    intentValue = 3 + (level * 2);
                    intent = "[DEB] Haunt: " + calculateDisplayDamage() + " dmg + 2 Weak";
                } else {
                    intentValue = 5 + (level * 2);
                    intent = "[ATK] Spirit Touch: " + calculateDisplayDamage() + " dmg";
                }
                break;

            case CULTIST:
                // Cultists gain strength
                if (rand.nextInt(100) < 35) {
                    intentValue = 0;
                    intent = "[BUF] Dark Ritual: Gain 2 Strength";
                } else {
                    intentValue = 4 + (level * 2);
                    intent = "[ATK] Sacrificial Blade: " + calculateDisplayDamage() + " dmg";
                }
                break;

            case MUSHROOM:
                // Mushrooms apply poison
                if (rand.nextInt(100) < 45) {
                    intentValue = 2 + level;
                    intent = "[PSN] Spore Cloud: " + calculateDisplayDamage() + " dmg + 3 Poison";
                } else {
                    intentValue = 5 + (level * 2);
                    intent = "[ATK] Spore Burst: " + calculateDisplayDamage() + " dmg";
                }
                break;

            default: // SLIME
                intentValue = 5 + (level * 2);
                intent = "[ATK] Tackle: " + calculateDisplayDamage() + " dmg";
                break;
        }
    }

    private void useSpecialAbility() {
        switch (enemyType) {
            case GOBLIN:
                intentValue = 8 + (level * 3);
                intent = "[SPE] Frenzied Strike: " + calculateDisplayDamage() + " dmg x2";
                break;
            case SKELETON:
                intentValue = 0;
                int bigBlock = 12 + (level * 4);
                intent = "[SPE] Bone Fortress: Gain " + bigBlock + " block";
                break;
            case BAT:
                intentValue = 2 + level;
                intent = "[SPE] Swarm Attack: " + calculateDisplayDamage() + " dmg x3";
                break;
            case GHOST:
                intentValue = 6 + (level * 3);
                intent = "[SPE] Possession: " + calculateDisplayDamage() + " dmg + 3 Weak + 2 Vulnerable";
                break;
            case CULTIST:
                intentValue = 0;
                intent = "[SPE] Blood Pact: Gain 4 Strength";
                break;
            case MUSHROOM:
                intentValue = 4 + (level * 2);
                intent = "[SPE] Toxic Explosion: " + calculateDisplayDamage() + " dmg + 5 Poison";
                break;
            default:
                intentValue = 10 + (level * 4);
                intent = "[SPE] Power Attack: " + calculateDisplayDamage() + " dmg";
                break;
        }
    }

    private int calculateDisplayDamage() {
        int dmg = intentValue + strength;
        if (weak > 0) dmg = (int)(dmg * 0.75);
        return Math.max(dmg, 0);
    }

    @Override
    public int attack() {
        if (intentValue <= 0) {
            // Execute non-attack intents
            executeNonAttackIntent();
            return 0;
        }

        int dmg = intentValue + strength;
        if (weak > 0) dmg = (int)(dmg * 0.75);
        return Math.max(dmg, 0);
    }

    private void executeNonAttackIntent() {
        if (intent.contains("Defend") || intent.contains("Fortress")) {
            int blockAmt = intent.contains("Fortress") ? 12 + (level * 4) : 6 + (level * 2);
            addBlock(blockAmt);
        } else if (intent.contains("Ritual")) {
            addStrength(2);
        } else if (intent.contains("Blood Pact")) {
            addStrength(4);
        }
    }

    /**
     * Execute special effects after dealing damage
     */
    public void executePostAttackEffects(Player player) {
        if (intent.contains("Haunt") || intent.contains("Possession")) {
            int weakAmt = intent.contains("Possession") ? 3 : 2;
            player.addWeak(weakAmt);
        }

        if (intent.contains("Spore") || intent.contains("Toxic")) {
            int poisonAmt = intent.contains("Toxic Explosion") ? 5 : 3;
            player.addPoison(poisonAmt);
        }

        if (intent.contains("Possession")) {
            player.addVulnerable(2);
        }
    }

    /**
     * Check if this is a multi-hit attack
     */
    public int getAttackHits() {
        if (intent.contains("x2")) return 2;
        if (intent.contains("x3")) return 3;
        return 1;
    }

    @Override
    public void takeDamage(int dmg) {
        // Apply vulnerable multiplier
        if (vulnerable > 0) {
            dmg = (int)(dmg * 1.5);
        }

        // Apply block
        if (block > 0) {
            if (block >= dmg) {
                block -= dmg;
                dmg = 0;
            } else {
                dmg -= block;
                block = 0;
            }
        }

        this.hp -= dmg;
        if (this.hp < 0) this.hp = 0;
    }

    public void addBlock(int amt) {
        this.block += amt;
    }

    public void addVulnerable(int turns) {
        this.vulnerable += turns;
    }

    public void reduceStatusEffects() {
        if (weak > 0) weak--;
        if (vulnerable > 0) vulnerable--;
    }

    public void resetBlock() {
        this.block = 0;
    }

    // Additional getters
    public int getBlock() { return block; }
    public int getVulnerable() { return vulnerable; }
    public EnemyType getEnemyType() { return enemyType; }
    public String getTypeDescription() {
        switch (enemyType) {
            case SLIME: return "Basic enemy";
            case GOBLIN: return "Fast attacker";
            case SKELETON: return "Defensive fighter";
            case BAT: return "Multi-hit striker";
            case GHOST: return "Applies debuffs";
            case CULTIST: return "Gains strength";
            case MUSHROOM: return "Poisons enemies";
            default: return "";
        }
    }
}
