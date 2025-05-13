package Elementos.Armas;

import Elementos.Arma;
import Elementos.BalaBounce;
import Elementos.Administradores.AdministradorBalas;
import Elementos.Audio.AudioManager;
import Juegos.Juego;
import Utilz.LoadSave;
import Elementos.AimController;

public class ArmaDelta extends Arma {
    // Cadencia en disparos por segundo
    private float cadenciaDisparo = 1.0f;
    private int contadorRecarga = 0;
    
    // Sistema de munición
    private int municionActual = 20;
    private int capacidadCargador = 20;
    private boolean recargando = false;
    private int tiempoRecargaCompleta = 360; // 2 segundos a 60 FPS
    private int contadorRecargaCompleta = 0;
    
    // Convertimos la cadencia en tiempo entre disparos (en frames)
    private int armaCooldown;
    private static final int FRAMES_POR_SEGUNDO = 60;
    
    // Propiedades para las balas que rebotan
    private int rebotesMaximos = 4;

    public ArmaDelta(AdministradorBalas adminBalas) {
        super("armas/DELTA.png", 30 * Juegos.Juego.SCALE, 2.4f, adminBalas);
        this.nombre = "Eclipse";
        this.armaCooldown = Math.round(FRAMES_POR_SEGUNDO / cadenciaDisparo);
        this.tipoDaño = "Mutagenico";
    }
    
    @Override
    public void disparar() {
        // Verificar si podemos disparar (no en cooldown Y tenemos munición)
        if(contadorRecarga <= 0 && municionActual > 0 && !recargando) {
            System.out.println("¡Disparando arma Delta! Munición restante: " + (municionActual-1));
            AudioManager.getInstance().playSoundEffect("shoot");
            
            // Calcular la posición exacta del origen de la bala
            float[] posicionDisparo = new float[2];
            float distanciaCañon = 20 * Juego.SCALE;
            AimController.getPositionAtDistance(
                x, y,
                distanciaCañon,
                rotacion,
                posicionDisparo
            );
            
            // Crear una bala que rebota
            BalaBounce nuevaBala = new BalaBounce(
                posicionDisparo[0], 
                posicionDisparo[1], 
                rotacion, 
                LoadSave.BULLET_DELTA,
                50,
                2.2f,
                tipoDaño,
                rebotesMaximos
            );
            
            // Añadir la bala al administrador
            adminBalas.agregarBala(nuevaBala);
            
            // Consumir munición
            municionActual--;
            
            // Reiniciar contador de cooldown
            contadorRecarga = armaCooldown;
        } else if (municionActual <= 0 && !recargando) {
            // Auto-recarga cuando nos quedamos sin munición
            iniciarRecarga();
        }
    }
    
    @Override
    public void update(float playerX, float playerY, AimController aimController) {
        super.update(playerX, playerY, aimController);
        
        // Recalcular el cooldown base cuando cambia el modificador
        int cooldownBase = Math.round(FRAMES_POR_SEGUNDO / cadenciaDisparo);
        this.armaCooldown = Math.round(cooldownBase / modificadorCadencia);
        
        // Actualizar contador de cooldown
        if(contadorRecarga > 0) {
            contadorRecarga--;
        }
        
        // Manejar la recarga
        if(recargando) {
            contadorRecargaCompleta--;
            if(contadorRecargaCompleta <= 0) {
                completarRecarga();
            }
        }
    }
    
    // Método para iniciar la recarga manual
    public void iniciarRecarga() {
        if(!recargando && municionActual < capacidadCargador) {
            recargando = true;
            contadorRecargaCompleta = tiempoRecargaCompleta;
            System.out.println("Recargando arma Delta...");
        }
    }
    
    // Método para completar la recarga
    private void completarRecarga() {
        municionActual = capacidadCargador;
        recargando = false;
        System.out.println("¡Recarga completa! Munición: " + municionActual);
    }
    
    // Método para configurar el número máximo de rebotes
    public void setRebotesMaximos(int rebotesMaximos) {
        this.rebotesMaximos = rebotesMaximos;
    }
    
    public int getRebotesMaximos() {
        return rebotesMaximos;
    }
    
    // Getters
    public float getCadenciaDisparo() {
        return cadenciaDisparo;
    }
    
    public int getMunicionActual() {
        return municionActual;
    }
    
    public boolean estaRecargando() {
        return recargando;
    }
    
    public int getCapacidadCargador() {
        return capacidadCargador;
    }
}