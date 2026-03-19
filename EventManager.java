import javax.swing.*;
import java.util.*;

/**
 * EventManager — random story events triggered when player visits an EVENT node.
 * Each event has 3 choices with different risk/reward tradeoffs.
 */
public class EventManager {
    private static final Random rand = new Random();

    public static void triggerEvent(JFrame parent, GameState state) {
        int idx = rand.nextInt(8);
        switch (idx) {
            case 0: eventAncientAltar(parent, state);    break;
            case 1: eventMysteriousTrader(parent, state); break;
            case 2: eventCursedTome(parent, state);      break;
            case 3: eventAbandonedCamp(parent, state);   break;
            case 4: eventBloodPact(parent, state);       break;
            case 5: eventFallenHero(parent, state);      break;
            case 6: eventShrineOfStrength(parent, state); break;
            case 7: eventPoisonedWell(parent, state);    break;
        }
    }

    // ── Event 1: Ancient Altar ───────────────────────────────────────────────
    private static void eventAncientAltar(JFrame p, GameState s) {
        String[] opts = {
                "Offer 25 HP  →  Gain a Relic",
                "Offer 40G   →  Gain 2 Upgraded Cards",
                "Leave in Peace"
        };
        int c = pick(p, "「 Ancient Altar 」\n\nA stone altar pulses with faint light.\nA cold voice whispers: \"What will you give?\"",
                "Ancient Altar  ✦", opts);
        if (c == 0) {
            int cost = Math.min(25, s.getPlayer().getHp() - 1);
            s.getPlayer().takeDamage(cost);
            Relic r = randomRelic();
            s.getPlayer().addRelic(r);
            msg(p, "You spill " + cost + " HP on the altar.\nReceived: [" + r.getName() + "]\n\"" + r.getDescription() + "\"", "Offering Accepted");
        } else if (c == 1) {
            if (s.spendGold(40)) {
                s.getDeck().addToMasterDeck(upgradedCard());
                s.getDeck().addToMasterDeck(upgradedCard());
                msg(p, "Gold dissolves into the stone.\nTwo powerful cards appear in your deck!", "Cards Received");
            } else msg(p, "Not enough gold.", "Nothing Happens");
        }
    }

    // ── Event 2: Mysterious Trader ───────────────────────────────────────────
    private static void eventMysteriousTrader(JFrame p, GameState s) {
        String[] opts = {
                "Trade a card  →  Gain 70G",
                "Buy Mystery Relic  (50G)",
                "Ignore"
        };
        int c = pick(p, "「 Mysterious Trader 」\n\nA hooded figure steps from the shadow.\n\"I deal in rarities... Interested?\"",
                "Mysterious Trader  ✦", opts);
        if (c == 0) {
            ArrayList<Card> deck = s.getDeck().getMasterDeck();
            if (deck.size() > 1) {
                Card gone = deck.remove(rand.nextInt(deck.size()));
                s.addGold(70);
                msg(p, "The trader pockets \"" + gone.getName() + "\".\nYou gain 70 Gold!", "Trade Complete");
            } else msg(p, "\"Your deck is too small to trade from.\"", "No Deal");
        } else if (c == 1) {
            if (s.spendGold(50)) {
                Relic r = randomRelic();
                s.getPlayer().addRelic(r);
                msg(p, "The trader grins under the hood.\n[" + r.getName() + "]\n\"" + r.getDescription() + "\"", "Mystery Relic!");
            } else msg(p, "Not enough gold.", "Declined");
        }
    }

    // ── Event 3: Cursed Tome ─────────────────────────────────────────────────
    private static void eventCursedTome(JFrame p, GameState s) {
        String[] opts = {
                "Read it  →  +3 Upgraded Cards, -15 HP",
                "Burn it  →  Gain 45G",
                "Leave"
        };
        int c = pick(p, "「 Cursed Tome 」\n\nA black book lies open on a pedestal.\nThe pages shimmer with forbidden knowledge.",
                "Cursed Tome  ✦", opts);
        if (c == 0) {
            s.getPlayer().takeDamage(15);
            for (int i = 0; i < 3; i++) s.getDeck().addToMasterDeck(upgradedCard());
            msg(p, "Dark knowledge floods your mind.\n−15 HP  |  +3 powerful cards added!", "Knowledge is Pain");
        } else if (c == 1) {
            s.addGold(45);
            msg(p, "The book burns with purple flame.\nThe ash is worth 45 Gold.", "Book Burned");
        }
    }

    // ── Event 4: Abandoned Camp ──────────────────────────────────────────────
    private static void eventAbandonedCamp(JFrame p, GameState s) {
        String[] opts = {
                "Rest  →  Heal 25 HP",
                "Scavenge  →  Find 30–60 Gold",
                "Train  →  Max HP +8"
        };
        int c = pick(p, "「 Abandoned Camp 」\n\nEmbers still glow in an old fire pit.\nSomeone was here — not long ago.",
                "Abandoned Camp  ✦", opts);
        if (c == 0) {
            s.getPlayer().heal(25);
            msg(p, "You rest by the warmth and recover 25 HP.", "Rested");
        } else if (c == 1) {
            int found = 30 + rand.nextInt(31);
            s.addGold(found);
            msg(p, "You dig through old packs and find " + found + " Gold!", "Loot Found!");
        } else if (c == 2) {
            s.getPlayer().increaseMaxHp(8);
            msg(p, "You train by the fire through the night.\nMax HP +8!", "Hardened");
        }
    }

    // ── Event 5: Blood Pact ──────────────────────────────────────────────────
    private static void eventBloodPact(JFrame p, GameState s) {
        String[] opts = {
                "Sign it  →  +2 permanent Strength, −20 HP",
                "Decline"
        };
        int c = pick(p, "「 Blood Pact 」\n\nA demon holds out a glowing contract.\n\"Power for price, warrior. Simple enough.\"",
                "Blood Pact  ✦", opts);
        if (c == 0) {
            s.getPlayer().addStrength(2);
            s.getPlayer().takeDamage(20);
            s.getPlayer().addRelic(new Relic("Demon Mark", "Signed in blood: +2 Strength always active"));
            msg(p, "You sign in blood.\n+2 Strength  |  −20 HP\n(Relic 'Demon Mark' added)", "Pact Sealed");
        } else {
            msg(p, "\"Wise... for now.\" The demon vanishes in smoke.", "Declined");
        }
    }

    // ── Event 6: Fallen Hero ─────────────────────────────────────────────────
    private static void eventFallenHero(JFrame p, GameState s) {
        int gold = 25 + rand.nextInt(35);
        Card heroCard = upgradedCard();
        s.addGold(gold);
        s.getDeck().addToMasterDeck(heroCard);
        msg(p, "「 Fallen Hero 」\n\nA warrior lies slumped against the wall.\nYou take what they no longer need.\n\n+"
                + gold + " Gold  |  \"" + heroCard.getName() + "\" added to deck.", "Loot Collected");
    }

    // ── Event 7: Shrine of Strength ──────────────────────────────────────────
    private static void eventShrineOfStrength(JFrame p, GameState s) {
        String[] opts = {
                "Pray  →  50%: +3 Strength  |  50%: nothing",
                "Leave Offering (35G)  →  +1 Strength guaranteed",
                "Ignore"
        };
        int c = pick(p, "「 Shrine of Strength 」\n\nA warrior idol stands in a shaft of red light.\nIts eyes glow like hot coals.",
                "Shrine of Strength  ✦", opts);
        if (c == 0) {
            if (rand.nextBoolean()) {
                s.getPlayer().addStrength(3);
                msg(p, "The idol blazes!\n+3 Strength bestowed upon you!", "Blessed!");
            } else msg(p, "The idol remains cold and still.\nNothing happens.", "Silence");
        } else if (c == 1) {
            if (s.spendGold(35)) {
                s.getPlayer().addStrength(1);
                msg(p, "Gold melts into the idol's base.\n+1 Strength!", "Offering Accepted");
            } else msg(p, "Not enough gold.", "Declined");
        }
    }

    // ── Event 8: Poisoned Well ───────────────────────────────────────────────
    private static void eventPoisonedWell(JFrame p, GameState s) {
        String[] opts = {
                "Drink  →  50%: Heal 30HP  |  50%: −20HP",
                "Bottle it  →  Add 'Venom Flask' to deck",
                "Leave"
        };
        int c = pick(p, "「 Poisoned Well 」\n\nA well glows sickly green.\nSomeone — or something — has tainted the water.",
                "Poisoned Well  ✦", opts);
        if (c == 0) {
            if (rand.nextBoolean()) {
                s.getPlayer().heal(30);
                msg(p, "The toxin reacts like medicine!\n+30 HP.", "Lucky!");
            } else {
                s.getPlayer().takeDamage(20);
                msg(p, "The poison burns through you.\n−20 HP.", "Poisoned!");
            }
        } else if (c == 1) {
            s.getDeck().addToMasterDeck(new Card("Venom Flask", CardType.SKILL).poison(8).cost(1));
            msg(p, "You fill a vial carefully.\n'Venom Flask' (Apply 8 Poison, 1 Energy) added!", "Bottled");
        }
    }

    // ── Helpers ───────────────────────────────────────────────────────────────
    private static int pick(JFrame parent, String body, String title, String[] opts) {
        return JOptionPane.showOptionDialog(parent, body, title,
                JOptionPane.DEFAULT_OPTION, JOptionPane.PLAIN_MESSAGE, null, opts, opts[0]);
    }
    private static void msg(JFrame parent, String text, String title) {
        JOptionPane.showMessageDialog(parent, text, title, JOptionPane.INFORMATION_MESSAGE);
    }

    public static Relic randomRelic() {
        Relic[] pool = {
                new Relic("Vampiric Fang",   "Heal 2 HP each time you play an Attack card"),
                new Relic("Iron Gauntlet",   "Your Block cards give +3 extra Block"),
                new Relic("Rage Crystal",    "Start each battle with +1 Strength"),
                new Relic("Poison Vial",     "All Poison effects deal +2 extra damage"),
                new Relic("Tome of Energy",  "Start each battle with +1 Energy"),
                new Relic("Warrior's Crest", "Gain 10 HP when you defeat an enemy"),
                new Relic("Gold Medallion",  "Shop items cost 15G less"),
                new Relic("Bone Charm",      "Take 1 less damage from all attacks"),
        };
        return pool[rand.nextInt(pool.length)];
    }

    public static Card upgradedCard() {
        Card[] pool = {
                new Card("Heavy Blade+",  CardType.ATTACK).damage(25).cost(2),
                new Card("Iron Wave+",    CardType.ATTACK).damage(12).block(12).cost(1),
                new Card("Twin Strike+",  CardType.ATTACK).damage(9).multiHit().cost(1),
                new Card("Uppercut+",     CardType.ATTACK).damage(18).weak(1).cost(2),
                new Card("Whirlwind+",    CardType.ATTACK).energyBurst().cost(0),
                new Card("Fortify+",      CardType.SKILL).block(24).cost(2),
                new Card("Battle Surge",  CardType.SKILL).strength(3).cost(2),
                new Card("Deadly Mist",   CardType.SKILL).poison(10).cost(2),
                new Card("Life Tap",      CardType.HEAL).heal(14).cost(1),
        };
        return pool[new Random().nextInt(pool.length)];
    }
}