package orbit.render.audio;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.audio.Sound;
import com.badlogic.gdx.files.FileHandle;
import com.badlogic.gdx.utils.Disposable;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;

/**
 * AudioManager provides centralized sound management for ORBIT's cinematic frontend.
 * Manages UI interaction audio, audio volume channels, and fallback procedural audio synthesis.
 *
 * Guarantees zero runtime crashes even in headless environments or systems without sound cards.
 */
public class AudioManager implements Disposable {

    private static final int SAMPLE_RATE = 44100;

    private float masterVolume = 0.85f;
    private float sfxVolume = 0.70f;
    private float musicVolume = 0.60f;
    private boolean muted = false;

    private Sound hoverSound;
    private Sound selectSound;
    private Sound confirmSound;
    private Sound cancelSound;
    private Sound alertSound;

    private boolean initialized = false;

    public AudioManager() {
        initAudio();
    }

    private void initAudio() {
        try {
            ensureSoundFilesExist();

            hoverSound = loadSound("audio/ui_hover.wav");
            selectSound = loadSound("audio/ui_select.wav");
            confirmSound = loadSound("audio/ui_confirm.wav");
            cancelSound = loadSound("audio/ui_cancel.wav");
            alertSound = loadSound("audio/ui_alert.wav");

            initialized = true;
            System.out.println("[ORBIT-AUDIO] Audio subsystem online. UI sound banks loaded.");
        } catch (Throwable t) {
            System.out.println("[ORBIT-AUDIO] Sound hardware unavailable or restricted. Audio running in silent mode: " + t.getMessage());
            initialized = false;
        }
    }

    private Sound loadSound(String internalPath) {
        try {
            FileHandle handle = Gdx.files.internal(internalPath);
            if (handle.exists()) {
                return Gdx.audio.newSound(handle);
            }
        } catch (Throwable ignored) {}
        return null;
    }

    /**
     * Synthesizes tactical avionics waveform audio files into assets/audio/ if missing.
     */
    private void ensureSoundFilesExist() {
        File audioDir = new File("assets/audio");
        if (!audioDir.exists()) {
            audioDir.mkdirs();
        }

        generateWavIfMissing(new File(audioDir, "ui_hover.wav"), generateHoverWave());
        generateWavIfMissing(new File(audioDir, "ui_select.wav"), generateSelectWave());
        generateWavIfMissing(new File(audioDir, "ui_confirm.wav"), generateConfirmWave());
        generateWavIfMissing(new File(audioDir, "ui_cancel.wav"), generateCancelWave());
        generateWavIfMissing(new File(audioDir, "ui_alert.wav"), generateAlertWave());
    }

    private void generateWavIfMissing(File file, byte[] data) {
        if (file.exists() && file.length() > 0) {
            return;
        }
        try (FileOutputStream fos = new FileOutputStream(file)) {
            fos.write(data);
        } catch (IOException e) {
            System.out.println("[ORBIT-AUDIO] Notice: Unable to write sound file " + file.getName() + ": " + e.getMessage());
        }
    }

    // --- Procedural PCM Waveform Generators (Clean Sci-Fi UI Tones) ---

    private byte[] generateHoverWave() {
        int samples = (int) (SAMPLE_RATE * 0.030f); // 30ms subtle pip
        short[] pcm = new short[samples];
        for (int i = 0; i < samples; i++) {
            float t = (float) i / samples;
            float env = (1.0f - t) * (1.0f - t); // Exponential decay
            float wave = (float) Math.sin(2.0 * Math.PI * 1100.0 * (i / (double) SAMPLE_RATE));
            pcm[i] = (short) (wave * env * 4500); // Soft volume
        }
        return encodeWav(pcm);
    }

    private byte[] generateSelectWave() {
        int samples = (int) (SAMPLE_RATE * 0.045f); // 45ms tactile pitch chirp
        short[] pcm = new short[samples];
        for (int i = 0; i < samples; i++) {
            float t = (float) i / samples;
            float freq = 600.0f + t * 400.0f; // 600Hz -> 1000Hz
            float env = 1.0f - t;
            float wave = (float) Math.sin(2.0 * Math.PI * freq * (i / (double) SAMPLE_RATE));
            pcm[i] = (short) (wave * env * 9000);
        }
        return encodeWav(pcm);
    }

    private byte[] generateConfirmWave() {
        int samples = (int) (SAMPLE_RATE * 0.120f); // 120ms two-tone chime
        short[] pcm = new short[samples];
        int half = samples / 2;
        for (int i = 0; i < samples; i++) {
            float freq = (i < half) ? 587.33f : 880.00f; // D5 -> A5 harmonic
            float t = (i < half) ? ((float) i / half) : ((float) (i - half) / half);
            float env = (1.0f - t);
            float wave = (float) Math.sin(2.0 * Math.PI * freq * (i / (double) SAMPLE_RATE));
            pcm[i] = (short) (wave * env * 11000);
        }
        return encodeWav(pcm);
    }

    private byte[] generateCancelWave() {
        int samples = (int) (SAMPLE_RATE * 0.075f); // 75ms subdued tone
        short[] pcm = new short[samples];
        for (int i = 0; i < samples; i++) {
            float t = (float) i / samples;
            float freq = 360.0f - t * 140.0f; // 360Hz -> 220Hz
            float env = (1.0f - t);
            float wave = (float) Math.sin(2.0 * Math.PI * freq * (i / (double) SAMPLE_RATE));
            pcm[i] = (short) (wave * env * 7500);
        }
        return encodeWav(pcm);
    }

    private byte[] generateAlertWave() {
        int samples = (int) (SAMPLE_RATE * 0.140f); // 140ms warning blip
        short[] pcm = new short[samples];
        for (int i = 0; i < samples; i++) {
            float t = (float) i / samples;
            float env = (float) Math.sin(t * Math.PI);
            float wave = (float) Math.sin(2.0 * Math.PI * 880.0 * (i / (double) SAMPLE_RATE));
            pcm[i] = (short) (wave * env * 12000);
        }
        return encodeWav(pcm);
    }

    private byte[] encodeWav(short[] samples) {
        ByteArrayOutputStream out = new ByteArrayOutputStream(44 + samples.length * 2);
        try {
            int dataSize = samples.length * 2;
            int chunkSize = 36 + dataSize;

            out.write("RIFF".getBytes());
            writeLittleEndianInt(out, chunkSize);
            out.write("WAVEfmt ".getBytes());
            writeLittleEndianInt(out, 16);           // Subchunk1Size
            writeLittleEndianShort(out, (short) 1);  // PCM format
            writeLittleEndianShort(out, (short) 1);  // Mono
            writeLittleEndianInt(out, SAMPLE_RATE);
            writeLittleEndianInt(out, SAMPLE_RATE * 2); // Byte rate
            writeLittleEndianShort(out, (short) 2);  // Block align
            writeLittleEndianShort(out, (short) 16); // Bits per sample
            out.write("data".getBytes());
            writeLittleEndianInt(out, dataSize);

            for (short sample : samples) {
                writeLittleEndianShort(out, sample);
            }
        } catch (IOException ignored) {}
        return out.toByteArray();
    }

    private void writeLittleEndianInt(ByteArrayOutputStream out, int value) {
        out.write(value & 0xFF);
        out.write((value >> 8) & 0xFF);
        out.write((value >> 16) & 0xFF);
        out.write((value >> 24) & 0xFF);
    }

    private void writeLittleEndianShort(ByteArrayOutputStream out, short value) {
        out.write(value & 0xFF);
        out.write((value >> 8) & 0xFF);
    }

    // --- Public Playback Facilities ---

    public void playHover() {
        playSound(hoverSound, 0.40f);
    }

    public void playSelect() {
        playSound(selectSound, 0.85f);
    }

    public void playConfirm() {
        playSound(confirmSound, 0.90f);
    }

    public void playCancel() {
        playSound(cancelSound, 0.70f);
    }

    public void playAlert() {
        playSound(alertSound, 1.00f);
    }

    private void playSound(Sound sound, float volumeScale) {
        if (muted || !initialized || sound == null) return;
        try {
            sound.play(masterVolume * sfxVolume * volumeScale);
        } catch (Throwable ignored) {}
    }

    // --- Settings & Lifecycle ---

    public void setMasterVolume(float volume) {
        this.masterVolume = Math.max(0f, Math.min(1f, volume));
    }

    public void setSfxVolume(float volume) {
        this.sfxVolume = Math.max(0f, Math.min(1f, volume));
    }

    public void setMuted(boolean muted) {
        this.muted = muted;
    }

    public boolean isMuted() {
        return muted;
    }

    @Override
    public void dispose() {
        if (hoverSound != null) hoverSound.dispose();
        if (selectSound != null) selectSound.dispose();
        if (confirmSound != null) confirmSound.dispose();
        if (cancelSound != null) cancelSound.dispose();
        if (alertSound != null) alertSound.dispose();
        initialized = false;
    }
}

