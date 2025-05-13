package Elementos.Armas;

import Elementos.Arma;
import Elementos.Bala;
import Elementos.Administradores.AdministradorBalas;
import Elementos.Audio.AudioManager;
import Juegos.Juego;
import Utilz.LoadSave;
import Elementos.AimController;

public class ArmaTerr extends Arma {
    // Cadencia en disparos por segundo
    private float cadenciaDisparo = 1.0f;
    private int contadorRecarga = 0;
    
    // Sistema de munición
    private int municionActual = 30;
    private int capacidadCargador = 30;
    private boolean recargando = false;
    private int tiempoRecargaCompleta = 120; // 2 segundos a 60 FPS
    private int contadorRecargaCompleta = 0;
    
    // Convertimos la cadencia en tiempo entre disparos (en frames)
    private int armaCooldown;
    private static final int FRAMES_POR_SEGUNDO = 60;
    
    // Parámetros para el disparo en cono
    private float anguloDispersion = 0.08f; // Ángulo de dispersión en grados (ajustable)

    public ArmaTerr(AdministradorBalas adminBalas) {
        super("armas/TERR.png", 30 * Juegos.Juego.SCALE, 3.0f, adminBalas);
        this.nombre = "Eclipse";
        this.armaCooldown = Math.round(FRAMES_POR_SEGUNDO / cadenciaDisparo);
        this.tipoDaño = "Corrosivo";
    }
    
    @Override
    public void disparar() {
        // Verificar si podemos disparar (no en cooldown Y tenemos munición)
        if(contadorRecarga <= 0 && municionActual > 0 && !recargando) {
            System.out.println("¡Disparando ametralladora! Munición restante: " + (municionActual-1));
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
            
            // Crear dos balas con ángulos ligeramente diferentes para formar un cono
            // Primera bala - desviada a la izquierda
            Bala bala1 = new Bala(
                posicionDisparo[0], 
                posicionDisparo[1], 
                rotacion - anguloDispersion / 2, // Rotación ligeramente a la izquierda
                LoadSave.BULLET_TERR,
                10,
                2.2f
            );
            
            // Segunda bala - desviada a la derecha
            Bala bala2 = new Bala(
                posicionDisparo[0], 
                posicionDisparo[1], 
                rotacion + anguloDispersion / 2, // Rotación ligeramente a la derecha
                LoadSave.BULLET_TERR,
                10,
                2.2f
            );
            
            // Añadir las balas al administrador
            adminBalas.agregarBala(bala1);
            adminBalas.agregarBala(bala2);
            
            // Consumir munición (solo se consume una bala aunque dispare dos)
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
            System.out.println("Recargando ametralladora...");
        }
    }
    
    // Método para completar la recarga
    private void completarRecarga() {
        municionActual = capacidadCargador;
        recargando = false;
        System.out.println("¡Recarga completa! Munición: " + municionActual);
    }
    
    // Métodos para ajustar el ángulo de dispersión
    public void setAnguloDispersion(float angulo) {
        this.anguloDispersion = angulo;
    }
    
    public float getAnguloDispersion() {
        return anguloDispersion;
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