package Elementos.Armas;

import Elementos.Arma;
import Elementos.BalaCreciente;
import Elementos.Administradores.AdministradorBalas;
import Juegos.Juego;
import Utilz.LoadSave;
import Elementos.AimController;

public class ArmaFire extends Arma {
    // Cadencia en disparos por segundo
    private float cadenciaDisparo = 1.0f;
    private int contadorRecarga = 0;
    
    // Sistema de munición
    private int municionActual = 10;
    private int capacidadCargador = 10;
    private boolean recargando = false;
    private int tiempoRecargaCompleta = 220; // 2 segundos a 60 FPS
    private int contadorRecargaCompleta = 0;
    
    // Convertimos la cadencia en tiempo entre disparos (en frames)
    private int armaCooldown;
    private static final int FRAMES_POR_SEGUNDO = 60;
    
    // Parámetros para el crecimiento de balas
    private float factorCrecimientoInicial = 0.4f;  // Tamaño inicial (70% del normal)
    private float factorCrecimientoMax = 5.0f;      // Tamaño máximo (250% del normal)
    private float velocidadCrecimiento = 0.010f;     // Qué tan rápido crece la bala

    public ArmaFire(AdministradorBalas adminBalas) {
        super("armas/FIRE.png", 30 * Juegos.Juego.SCALE, 3.0f, adminBalas);
        this.nombre = "Fire";
        this.armaCooldown = Math.round(FRAMES_POR_SEGUNDO / cadenciaDisparo);
        this.tipoDaño = "Luz";
    }
    
    @Override
    public void disparar() {
        // Verificar si podemos disparar (no en cooldown Y tenemos munición)
        if(contadorRecarga <= 0 && municionActual > 0 && !recargando) {
            System.out.println("¡Disparando arma de fuego! Munición restante: " + (municionActual-1));
            
            // Calcular la posición exacta del origen de la bala
            float[] posicionDisparo = new float[2];
            float distanciaCañon = 20 * Juego.SCALE;
            
            AimController.getPositionAtDistance(
                x, y,
                distanciaCañon,
                rotacion,
                posicionDisparo
            );
            
            // Crear una bala creciente en lugar de una bala normal
            BalaCreciente nuevaBala = new BalaCreciente(
                posicionDisparo[0], 
                posicionDisparo[1], 
                rotacion, 
                LoadSave.BULLET_FIRE,
                4,
                2.2f,
                tipoDaño,
                factorCrecimientoInicial,
                factorCrecimientoMax,
                velocidadCrecimiento
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
            System.out.println("Recargando arma de fuego...");
        }
    }
    
    // Método para completar la recarga
    private void completarRecarga() {
        municionActual = capacidadCargador;
        recargando = false;
        System.out.println("¡Recarga completa! Munición: " + municionActual);
    }
    
    // Métodos para ajustar el crecimiento de las balas
    public void setFactorCrecimientoInicial(float factor) {
        this.factorCrecimientoInicial = factor;
    }
    
    public void setFactorCrecimientoMax(float factor) {
        this.factorCrecimientoMax = factor;
    }
    
    public void setVelocidadCrecimiento(float velocidad) {
        this.velocidadCrecimiento = velocidad;
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