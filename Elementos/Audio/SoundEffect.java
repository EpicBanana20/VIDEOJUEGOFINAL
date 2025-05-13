package Elementos.Audio;

import java.io.IOException;
import javax.sound.sampled.*;
import java.io.File;
import java.net.URL;

public class SoundEffect {
    private Clip clip;
    private FloatControl volumeControl;
    
    public SoundEffect(String path) throws UnsupportedAudioFileException, IOException, LineUnavailableException {
        try {
            // Intentar cargar como recurso interno
            URL url = getClass().getClassLoader().getResource(path);
            
            // Si no se encuentra, intentar como archivo directo
            if (url == null) {
                File file = new File("recursos/" + path);
                if (file.exists()) {
                    AudioInputStream audioStream = AudioSystem.getAudioInputStream(file);
                    init(audioStream);
                } else {
                    throw new IOException("No se pudo encontrar el archivo de audio: " + path);
                }
            } else {
                AudioInputStream audioStream = AudioSystem.getAudioInputStream(url);
                init(audioStream);
            }
        } catch (Exception e) {
            System.err.println("Error cargando efecto de sonido: " + e.getMessage());
            throw e;
        }
    }
    
    private void init(AudioInputStream audioStream) throws LineUnavailableException, IOException {
        clip = AudioSystem.getClip();
        clip.open(audioStream);
        
        // Configurar volumen si está disponible
        try {
            volumeControl = (FloatControl) clip.getControl(FloatControl.Type.MASTER_GAIN);
        } catch (IllegalArgumentException e) {
            System.err.println("Control de volumen no disponible: " + e.getMessage());
        }
        
        // Cerrar el stream después de obtener los datos
        audioStream.close();
    }
    
    public void play() {
        if (clip != null) {
            // Detener si ya está sonando y reiniciar
            if (clip.isRunning()) {
                clip.stop();
            }
            clip.setFramePosition(0);
            clip.start();
        }
    }
    
    public void stop() {
        if (clip != null && clip.isRunning()) {
            clip.stop();
        }
    }
    
    public void setVolume(float volume) {
        if (volumeControl != null) {
            // Convertir el volumen lineal (0.0 a 1.0) a decibeles (típicamente -80.0 a 6.0)
            float dB = 20f * (float) Math.log10(volume);
            
            // Limitar el volumen a los rangos válidos
            float min = volumeControl.getMinimum();
            float max = volumeControl.getMaximum();
            
            if (dB < min) {
                dB = min;
            } else if (dB > max) {
                dB = max;
            }
            
            volumeControl.setValue(dB);
        }
    }
    
    public void cleanup() {
        if (clip != null) {
            clip.stop();
            clip.close();
        }
    }
}