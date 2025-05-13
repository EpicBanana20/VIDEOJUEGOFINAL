package Elementos.Audio;

import java.io.IOException;
import javax.sound.sampled.*;
import java.util.HashMap;
import java.util.Map;

import Juegos.EstadoJuego;

public class AudioManager {
    
    private static AudioManager instance;
    
    private Music currentMusic;
    private Map<String, Music> musicTracks = new HashMap<>();
    private Map<String, SoundEffect> soundEffects = new HashMap<>();
    
    private float musicVolume = 0.1f;
    private float sfxVolume = 0.05f;
    
    private Map<EstadoJuego, String> gameStateMusicMap = new HashMap<>();
    private Map<Integer, String> levelMusicMap = new HashMap<>();
    
    private boolean musicEnabled = true;
    private boolean soundEnabled = true;
    
    private AudioManager() {
        initializeMusicMappings();
    }
    
    private void initializeMusicMappings() {
        gameStateMusicMap.put(EstadoJuego.MENU, "menu");

        levelMusicMap.put(0, "world1");
        levelMusicMap.put(1, "world2");
        levelMusicMap.put(2, "world3");
    }
    
    public static AudioManager getInstance() {
        if (instance == null) {
            instance = new AudioManager();
        }
        return instance;
    }
    
    public void loadMusic(String id, String path) {
        try {
            Music music = new Music(path);
            musicTracks.put(id, music);
            System.out.println("Música cargada: " + id);
        } catch (Exception e) {
            System.err.println("Error al cargar música '" + id + "': " + e.getMessage());
        }
    }
    
    public void loadSoundEffect(String id, String path) {
        try {
            SoundEffect sound = new SoundEffect(path);
            soundEffects.put(id, sound);
            System.out.println("Efecto de sonido cargado: " + id);
        } catch (Exception e) {
            System.err.println("Error al cargar efecto '" + id + "': " + e.getMessage());
        }
    }
    
    public void playMusic(String id) {
        if (!musicEnabled) return;

        System.out.println(">>> Cambiando música a: " + id); // para depuración

        // Detener la música actual si existe
        if (currentMusic != null) {
            currentMusic.stop();  // Esto asegura que la anterior se detenga
            currentMusic.cleanup();  // Cierra completamente el clip anterior
        }

        Music music = musicTracks.get(id);
        if (music != null) {
            currentMusic = music;
            currentMusic.setVolume(musicVolume);
            currentMusic.play();
        } else {
            System.err.println("Música no encontrada: " + id);
        }
    }
    
    public void playSoundEffect(String id) {
        if (!soundEnabled) return;

        SoundEffect sound = soundEffects.get(id);
        if (sound != null) {
            sound.setVolume(sfxVolume);
            sound.play();
        } else {
            System.err.println("Efecto de sonido no encontrado: " + id);
        }
    }
    
    public void stopMusic() {
        if (currentMusic != null) {
            currentMusic.stop();
            currentMusic = null;
        }
    }
    
    public void pauseMusic() {
        if (currentMusic != null) {
            currentMusic.pause();
        }
    }
    
    public void resumeMusic() {
        if (currentMusic != null && musicEnabled) {
            currentMusic.resume();
        }
    }

    public void updateGameState(EstadoJuego state, int currentLevel) {
        if (state == EstadoJuego.PLAYING) {
            String musicId = levelMusicMap.get(currentLevel);
            if (musicId != null) {
                playMusic(musicId);
            }
        } else {
            String musicId = gameStateMusicMap.get(state);
            if (musicId != null) {
                playMusic(musicId);
            } else if (state == EstadoJuego.PAUSA) {
                pauseMusic();
            } else if (state == EstadoJuego.MUERTE) {
                playSoundEffect("death");
                pauseMusic();
            }
        }
    }
    
    public void setMusicVolume(float volume) {
        this.musicVolume = Math.max(0.0f, Math.min(1.0f, volume));
        if (currentMusic != null) {
            currentMusic.setVolume(musicVolume);
        }
    }
    
    public void setSfxVolume(float volume) {
        this.sfxVolume = Math.max(0.0f, Math.min(1.0f, volume));
    }
    
    public void setMusicEnabled(boolean enabled) {
        this.musicEnabled = enabled;
        if (!enabled) {
            stopMusic();
        }
    }
    
    public void setSoundEnabled(boolean enabled) {
        this.soundEnabled = enabled;
    }
    
    public void cleanup() {
        stopMusic();
        for (Music music : musicTracks.values()) {
            music.cleanup();
        }
        for (SoundEffect sound : soundEffects.values()) {
            sound.cleanup();
        }
        musicTracks.clear();
        soundEffects.clear();
    }
}
