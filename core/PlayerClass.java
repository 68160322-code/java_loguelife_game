package core;

import card.Card;
import card.CardType;

import java.util.ArrayList;

/**
 * PlayerClass — กำหนด class ของผู้เล่น
 *
 * KNIGHT (อัศวิน):
 *   - Block ไม่ reset ตอนจบเทิร์น
 *   - Block card ให้ +2 เพิ่มเติม
 *   - การ์ด Skill ที่มี poison → CardType.POISON (ใช้ไม่ได้ในสนามรบ)
 *
 * ROGUE (นักล่า) — เพิ่มในอนาคต
 */
public enum PlayerClass {

    KNIGHT("Knight",
            "Iron Defender",
            "Block persists between turns. +2 bonus to all Block cards.\nCannot use Poison Skill cards.",
            new java.awt.Color(120, 140, 180)),

    ROGUE("Rogue",
            "Shadow Stalker",
            "Coming soon...",
            new java.awt.Color(140, 80, 160));

    // ── Fields ────────────────────────────────────────────────────────────────
    public final String name;
    public final String title;
    public final String description;
    public final java.awt.Color color;

    PlayerClass(String name, String title, String description, java.awt.Color color) {
        this.name = name;
        this.title = title;
        this.description = description;
        this.color = color;
    }

    // ── Class-specific rules ─────────────────────────────────────────────────

    /** Knight: Block card ได้ +2 เพิ่มเติม */
    public int blockBonus() {
        return this == KNIGHT ? 2 : 0;
    }

    /** Knight: Block ไม่ reset ตอนจบเทิร์น */
    public boolean persistsBlock() {
        return this == KNIGHT;
    }

    /**
     * Knight: การ์ด Skill ที่มี poison → เปลี่ยน type เป็น POISON
     * (render สีต่าง ไม่สามารถเล่นได้)
     * เรียกตอน build deck — แก้ทุกใบใน master deck
     */
    public void applyClassRules(ArrayList<Card> deck) {
        if (this != KNIGHT) return;
        for (Card c : deck) {
            if (c.getType() == CardType.SKILL && c.hasPoison()) {
                c.setType(CardType.POISON);
            }
        }
    }
}