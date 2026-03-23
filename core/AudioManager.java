package core;

import javax.sound.sampled.*;
import java.io.File;
import java.io.IOException;

/**
 * AudioManager — ระบบเพลงและเสียงสำหรับเกม
 *
 * วิธีใช้:
 *   1. วางไฟล์เพลง (.wav) ในโฟลเดอร์ assets/music/ และ assets/sfx/
 *   2. เรียก AudioManager.playMusic(Track.LOBBY) เพื่อเล่นเพลง
 *   3. เรียก AudioManager.playSfx(Sfx.CARD_PLAY) เพื่อเล่นเสียงเอฟเฟกต์
 *
 * รองรับ format: .wav (แนะนำ), .aiff
 * ไม่รองรับ .mp3 โดยตรง — ต้องแปลงเป็น .wav ก่อน
 * แปลง mp3 → wav ได้ฟรีที่ https://cloudconvert.com/mp3-to-wav
 *
 * โครงสร้างโฟลเดอร์ที่แนะนำ:
 *   game/
 *   └── assets/
 *       ├── music/
 *       │   ├── lobby.wav        ← เพลงหน้าหลัก
 *       │   ├── map.wav          ← เพลงหน้าแผนที่
 *       │   ├── battle.wav       ← เพลงต่อสู้ปกติ
 *       │   ├── boss.wav         ← เพลง Boss
 *       │   └── victory.wav      ← เพลงชนะ
 *       └── sfx/
 *           ├── card_play.wav    ← เสียงเล่นการ์ด
 *           ├── card_draw.wav    ← เสียงจั่วการ์ด
 *           ├── enemy_hit.wav    ← เสียงศัตรูโดนตี
 *           ├── player_hit.wav   ← เสียงผู้เล่นโดนตี
 *           ├── enemy_die.wav    ← เสียงศัตรูตาย
 *           ├── level_up.wav     ← เสียงผ่านด่าน
 *           ├── buy.wav          ← เสียงซื้อของ
 *           └── button.wav       ← เสียงกดปุ่ม
 */
public class AudioManager {

    // ── Track enum ─────────────────────────────────────────────────────────────
    public enum Track {
        LOBBY   ("assets/music/lobby.wav"),
        MAP     ("assets/music/map.wav"),
        BATTLE  ("assets/music/battle.wav"),
        BOSS    ("assets/music/boss.wav"),
        VICTORY ("assets/music/victory.wav");

        final String path;
        Track(String path) { this.path = path; }
    }

    // ── SFX enum ───────────────────────────────────────────────────────────────
    public enum Sfx {
        CARD_PLAY  ("assets/sfx/card_play.wav"),
        CARD_DRAW  ("assets/sfx/card_draw.wav"),
        ENEMY_HIT  ("assets/sfx/enemy_hit.wav"),
        PLAYER_HIT ("assets/sfx/player_hit.wav"),
        ENEMY_DIE  ("assets/sfx/enemy_die.wav"),
        LEVEL_UP   ("assets/sfx/level_up.wav"),
        BUY        ("assets/sfx/buy.wav"),
        BUTTON     ("assets/sfx/button.wav");

        final String path;
        Sfx(String path) { this.path = path; }
    }

    // ── State ──────────────────────────────────────────────────────────────────
    private static Clip    currentMusic  = null;
    private static Track   currentTrack  = null;
    private static float   musicVolume   = 0.7f;   // 0.0 - 1.0
    private static float   sfxVolume     = 0.8f;
    private static boolean musicEnabled  = true;
    private static boolean sfxEnabled    = true;

    // ── Music ──────────────────────────────────────────────────────────────────

    /**
     * เล่นเพลง loop ต่อเนื่อง
     * ถ้า track เดิมกำลังเล่นอยู่ จะไม่ restart
     */
    public static void playMusic(Track track) {
        if (!musicEnabled) return;
        if (track == currentTrack && currentMusic != null && currentMusic.isRunning()) return;

        stopMusic();
        currentTrack = track;

        Clip clip = loadClip(track.path);
        if (clip == null) return;

        setVolume(clip, musicVolume);
        clip.loop(Clip.LOOP_CONTINUOUSLY);
        clip.start();
        currentMusic = clip;
    }

    /** หยุดเพลงที่กำลังเล่นอยู่ */
    public static void stopMusic() {
        if (currentMusic != null) {
            currentMusic.stop();
            currentMusic.close();
            currentMusic = null;
        }
        currentTrack = null;
    }

    /** หยุดชั่วคราว */
    public static void pauseMusic() {
        if (currentMusic != null && currentMusic.isRunning()) {
            currentMusic.stop();
        }
    }

    /** เล่นต่อจากที่หยุด */
    public static void resumeMusic() {
        if (!musicEnabled) return;
        if (currentMusic != null && !currentMusic.isRunning()) {
            currentMusic.start();
        }
    }

    // ── SFX ────────────────────────────────────────────────────────────────────

    /** เล่นเสียง effect ครั้งเดียว (ไม่ loop) */
    public static void playSfx(Sfx sfx) {
        if (!sfxEnabled) return;
        Clip clip = loadClip(sfx.path);
        if (clip == null) return;
        setVolume(clip, sfxVolume);
        clip.start();
        // ปิด clip อัตโนมัติเมื่อเล่นจบ
        clip.addLineListener(e -> {
            if (e.getType() == LineEvent.Type.STOP) clip.close();
        });
    }

    // ── Volume ─────────────────────────────────────────────────────────────────

    public static void setMusicVolume(float vol) {
        musicVolume = Math.max(0f, Math.min(1f, vol));
        if (currentMusic != null) setVolume(currentMusic, musicVolume);
    }

    public static void setSfxVolume(float vol) {
        sfxVolume = Math.max(0f, Math.min(1f, vol));
    }

    public static float getMusicVolume() { return musicVolume; }
    public static float getSfxVolume()   { return sfxVolume; }

    // ── Toggle ─────────────────────────────────────────────────────────────────

    public static void setMusicEnabled(boolean enabled) {
        musicEnabled = enabled;
        if (!enabled) pauseMusic();
        else resumeMusic();
    }

    public static void setSfxEnabled(boolean enabled) { sfxEnabled = enabled; }
    public static boolean isMusicEnabled() { return musicEnabled; }
    public static boolean isSfxEnabled()   { return sfxEnabled; }
    public static Track   getCurrentTrack() { return currentTrack; }

    // ── Helpers ────────────────────────────────────────────────────────────────

    private static Clip loadClip(String path) {
        try {
            File file = new File(path);
            if (!file.exists()) {
                // ไม่มีไฟล์ — ไม่ crash แค่ silent
                return null;
            }
            AudioInputStream stream = AudioSystem.getAudioInputStream(file);
            Clip clip = AudioSystem.getClip();
            clip.open(stream);
            return clip;
        } catch (UnsupportedAudioFileException | IOException | LineUnavailableException e) {
            System.out.println("[AudioManager] Cannot load: " + path + " — " + e.getMessage());
            return null;
        }
    }

    private static void setVolume(Clip clip, float volume) {
        try {
            FloatControl fc = (FloatControl) clip.getControl(FloatControl.Type.MASTER_GAIN);
            // แปลง 0.0-1.0 → dB
            float dB = volume <= 0f ? fc.getMinimum()
                    : (float)(Math.log10(volume) * 20.0);
            fc.setValue(Math.max(fc.getMinimum(), Math.min(fc.getMaximum(), dB)));
        } catch (IllegalArgumentException e) {
            // บาง clip ไม่มี MASTER_GAIN control — ข้ามไป
        }
    }
}