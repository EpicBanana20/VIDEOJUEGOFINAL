package Elementos.Armas;

import Elementos.Arma;
import Elementos.Bala;
import Elementos.Administradores.AdministradorBalas;
import Elementos.Audio.AudioManager;
import Juegos.Juego;
import Utilz.LoadSave;
import Elementos.AimController;

public class ArmaFrancotirador extends Arma {
    private int municionActual = 5;
    private int capacidadCargador = 5;
    private boolean recargando = false;
    private int tiempoRecargaCompleta = 240;
    private int contadorRecargaCompleta = 0;
    private int armaCooldown;
    private static final int FRAMES_POR_SEGUNDO = 60;
    private int contadorRecarga = 0;
    private float cadenciaDisparo = 0.1f;
    
    public ArmaFrancotirador(AdministradorBalas adminBalas) {
        super("armas/francotirador.png", 35 * Juego.SCALE, 2.0f, adminBalas);
        this.nombre = "Francotirador";
        this.tipoDaño = "Mutagenico";
    }
    
    @Override
    public void disparar() {
        if (contadorRecarga <= 0 && municionActual > 0 && !recargando) {
            // Calcular posición del disparo
            float[] posicionDisparo = new float[2];
            float distanciaCañon = 30 * Juego.SCALE;
            AudioManager.getInstance().playSoundEffect("shootfranco");
            
            AimController.getPositionAtDistance(
                x, y,
                distanciaCañon,
                rotacion,
                posicionDisparo
            );
            
            // Crear bala de alta velocidad y daño
            Bala balaFrancotirador = new Bala(
                posicionDisparo[0], 
                posicionDisparo[1], 
                rotacion,
                LoadSave.BULLET_MACHINEGUN, // Usar sprite temporal
                1000, // Alto daño
                10.0f // Alta velocidad
            );
            
            adminBalas.agregarBala(balaFrancotirador);
            
            // Consumir munición y establecer cooldown
            municionActual--;
            contadorRecarga = armaCooldown;
            
            System.out.println("¡Disparo de francotirador! Munición: " + municionActual);
        } else if (municionActual <= 0 && !recargando) {
            iniciarRecarga();
        }
    }
    
    @Override
    public void update(float playerX, float playerY, AimController aimController) {
        super.update(playerX, playerY, aimController);
        
        int cooldownBase = Math.round(FRAMES_POR_SEGUNDO / cadenciaDisparo);
        this.armaCooldown = Math.round(cooldownBase / modificadorCadencia);

        if (contadorRecarga > 0) {
            contadorRecarga--;
        }
        
        if (recargando) {
            contadorRecargaCompleta--;
            if (contadorRecargaCompleta <= 0) {
                completarRecarga();
            }
        }
    }
    
    private void iniciarRecarga() {
        if (!recargando && municionActual < capacidadCargador) {
            recargando = true;
            contadorRecargaCompleta = tiempoRecargaCompleta;
            System.out.println("Recargando rifle de francotirador...");
        }
    }
    
    private void completarRecarga() {
        municionActual = capacidadCargador;
        recargando = false;
        System.out.println("¡Rifle de francotirador recargado!");
    }
    
    public int getMunicionActual() {
        return municionActual;
    }
    
    public int getCapacidadCargador() {
        return capacidadCargador;
    }
}