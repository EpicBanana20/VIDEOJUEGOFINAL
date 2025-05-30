package Elementos.Enemigos;

import java.awt.Graphics;
import java.awt.image.BufferedImage;

import Elementos.Bala;
import Elementos.Enemigo;
import Elementos.Jugador;
import Juegos.Juego;
import Utilz.LoadSave;
import Utilz.Animaciones;

public class EnemigoPliip extends Enemigo{
    // Constantes específicas de este tipo de enemigo
    private static final int ANCHO_DEFAULT = 96;
    private static final int ALTO_DEFAULT = 72;
    private static final int VIDA_DEFAULT = 600;

    private boolean disparoEnProceso = false;
    private int frameDisparo = 1; // El disparo ocurrirá en el tercer frame (0,1,2,3)
    private boolean disparoPendiente = false;
    private float anguloDisparo = 0;
    
    // Ajuste específico para este enemigo
    private int ajuste = -30;
    
    // Configuración del patrón de escopeta
    private static final int NUM_BALAS_ESCOPETA = 3;
    private static final float ANGULO_DISPERSION = 0.4f; // Ángulo de dispersión en radianes (aproximadamente 23 grados)
    
    public EnemigoPliip(float x, float y) {
        super(x, y, 
            (int)(ANCHO_DEFAULT * Juego.SCALE), 
            (int)(ALTO_DEFAULT * Juego.SCALE), 
            VIDA_DEFAULT);
        
        // Configurar propiedades específicas
        inicializarEnemigo(28, 20, 48, 50, true, true);
        this.velocidadMovimiento = 0.5f * Juego.SCALE;
        this.velocidadX = -velocidadMovimiento; // Iniciar moviéndose a la izquierda
        this.checkOffset = 20 * Juego.SCALE; // Ajustar el offset de verificación para el salto

        this.puedeDisparar = true;
        this.disparoMaxCooldown = 40; // Cada 3 segundos
        this.rangoDeteccionJugador = 400 * Juego.SCALE; // Mayor rango
        
        // Cargar animaciones
        cargarAnimaciones();
    }
    
    @Override
    protected void determinarAnimacion() {
        // Si estamos disparando, priorizar esa animación
        if (disparoEnProceso) {
            if (!animaciones.esUltimoFrame()) {
                return;
            } else {
                disparoEnProceso = false;
            }
        }
        
        // Si recibimos daño, mostrar animación de daño
        if (animaciones.getAccionActual() == HERIDO && !animaciones.esUltimoFrame()) {
            return;
        }
        
        // Determinar animación por estado
        int nuevaAnimacion;
        
        if (enAire) {
            nuevaAnimacion = CORRER;
        } else if (velocidadX != 0) {
            nuevaAnimacion = CORRER;
        } else {
            nuevaAnimacion = INACTIVO; // Cuando está quieto
        }
        
        animaciones.setAccion(nuevaAnimacion);
    }
    
    @Override
    protected void cargarAnimaciones() {
        // Cargar la hoja de sprites del enemigo verde
        BufferedImage img = LoadSave.GetSpriteAtlas("enemigos/PLIIP120x120.png");
        
        // Basado en la imagen, tiene 3 filas (acciones) con varios frames cada una
        // Fila 1 (índice 0): 6 frames - Inactivo/Idle
        // Fila 2 (índice 1): 6 frames - Correr/Moverse
        // Fila 3 (índice 2): 2 frames - Daño/Herido
        spritesEnemigo = new BufferedImage[4][12]; // 3 acciones, máximo 6 frames
        
        // Ancho y alto de cada frame del sprite 
        int frameWidth = 120;
        int frameHeight = 120;
        int framesEnFila = 0;
        // Extraer cada frame de la hoja de sprites
        for (int j = 0; j < spritesEnemigo.length; j++) {
            switch (j) {
                case 0 -> framesEnFila = 8; // Inactivo/Idle
                case 1 -> framesEnFila = 8; // Correr/Moverse
                case 2 -> framesEnFila = 2; // Daño/Herido
                case 3 -> framesEnFila = 12; // Disparo
            }
            for (int i = 0; i < framesEnFila; i++) {
                spritesEnemigo[j][i] = img.getSubimage(i * frameWidth, j * frameHeight, frameWidth, frameHeight);
            }
        }
        
        // Crear animaciones
        animaciones = new Animaciones(spritesEnemigo);
        
        // Configurar el número correcto de frames para cada animación
        animaciones.setNumFramesPorAnimacion(INACTIVO, 8); // 6 frames para inactivo/idle
        animaciones.setNumFramesPorAnimacion(CORRER, 8);   // 6 frames para correr/moverse
        animaciones.setNumFramesPorAnimacion(HERIDO, 2);   // 2 frames para herido
        animaciones.setNumFramesPorAnimacion(DISPARO, 12);   // 2 frames para disparo
        
        // Establecer animación inicial
        animaciones.setAccion(CORRER);  // Comenzamos en animación de correr ya que estará en movimiento
    }
    
    // Sobrescribir renderizado para aplicar el ajuste específico
    @Override
    protected void renderizarConAnimacion(Graphics g, int xLvlOffset, int yLvlOffset) {
        if (!activo) return;
        
        int drawX = (int) (hitbox.x - xDrawOffset) - xLvlOffset;
        int drawY = (int) (hitbox.y - yDrawOffset) - yLvlOffset;
        
        if (movimientoHaciaIzquierda) {
            // Dibujar volteado horizontalmente con ajuste
            g.drawImage(animaciones.getImagenActual(),
                drawX + w - ajuste, drawY,
                -w, h, null);
        } else {
            // Dibujar normal
            g.drawImage(animaciones.getImagenActual(),
                drawX, drawY,
                w, h, null);
        }
    }

    @Override
    protected void disparar(float angulo) {
        // En lugar de crear la bala inmediatamente, iniciamos la animación
        disparoEnProceso = true;
        disparoPendiente = true;
        anguloDisparo = angulo;
        animaciones.setAccion(DISPARO);
        animaciones.resetearAnimacion();
        
        // Detenemos temporalmente el movimiento durante el disparo
        float velocidadOriginal = velocidadX;
        velocidadX = 0;
        
        // Programamos un timer para restaurar la velocidad cuando termine la animación
        new java.util.Timer().schedule(
            new java.util.TimerTask() {
                @Override
                public void run() {
                    velocidadX = velocidadOriginal;
                }
            },
            animaciones.getNumFramesPorAnimacion(DISPARO) * (animaciones.getAnimVelocidad() * 16)
        );
    }

    @Override
    protected void manejarDisparo(Jugador jugador) {
        if (!puedeDisparar || !activo) return;
        
        // Reducir cooldown si está activo
        if (disparoCooldown > 0) {
            disparoCooldown--;
            return;
        }
        
        // Verificar si el jugador está en rango
        if (puedeVerJugador(jugador)) {
            patrullando = false;
            velocidadX = 0;
            
            // Orientar hacia el jugador
            float jugadorX = jugador.getXCenter();
            float enemigoX = hitbox.x + hitbox.width/2;
            movimientoHaciaIzquierda = jugadorX < enemigoX;
            
            // Disparar
            float angulo = calcularAnguloHaciaJugador(jugador);
            disparar(angulo);
            disparoCooldown = disparoMaxCooldown;
            
            // Reanudar movimiento después de un tiempo, pero verificando si es seguro
            new java.util.Timer().schedule(
                new java.util.TimerTask() {
                    @Override
                    public void run() {
                        // Solo reanudar patrulla y movimiento si es seguro
                        patrullando = true;
                        
                        if (esSeguroMoverse()) {
                            velocidadX = movimientoHaciaIzquierda ? -velocidadMovimiento : velocidadMovimiento;
                        } else {
                            // No es seguro moverse, mantener quieto pero seguir orientado al jugador
                            velocidadX = 0;
                        }
                    }
                },
                1000 // Reanudar después de 1 segundo
            );
        }
    }

    @Override
    public void update() {
        super.update();
        
        // Verificar si es momento de crear la bala durante la animación
        if (disparoPendiente && disparoEnProceso && 
            animaciones.getAccionActual() == DISPARO && 
            animaciones.getAnimIndice() == frameDisparo) {
            
            // Calcular posición de origen de la bala
            float origenX = hitbox.x + hitbox.width/2;
            float origenY = hitbox.y + hitbox.height/2;
            
            // Ajustar el origen según la dirección
            if (movimientoHaciaIzquierda) {
                origenX -= 20 * Juego.SCALE;
            } else {
                origenX += 20 * Juego.SCALE;
            }
            
            // NUEVO: Disparar patrón de escopeta en lugar de una sola bala
            dispararPatronEscopeta(origenX, origenY, anguloDisparo);
            
            // Ya disparamos, no repetir hasta la próxima animación
            disparoPendiente = false;
        }
        
        // Intentar detectar al jugador y disparar
        if (!disparoEnProceso && Juego.jugadorActual != null) {
            manejarDisparo(Juego.jugadorActual);
        }
    }
    
    // Método nuevo para disparar múltiples balas en patrón de escopeta
    private void dispararPatronEscopeta(float origenX, float origenY, float anguloCentral) {
        // Calcular el ángulo entre cada bala
        float anguloEntreBala = ANGULO_DISPERSION / (NUM_BALAS_ESCOPETA - 1);
        
        // Calcular el ángulo inicial (para centrar el patrón)
        float anguloInicial = anguloCentral - (ANGULO_DISPERSION / 2);
        
        // Disparar cada bala con su ángulo correspondiente
        for (int i = 0; i < NUM_BALAS_ESCOPETA; i++) {
            float anguloActual = anguloInicial + (anguloEntreBala * i);
            
            // Añadir un poco de variación aleatoria para hacer el patrón más natural
            float variacionAleatoria = (float)(Math.random() - 0.5) * 0.05f;
            anguloActual += variacionAleatoria;
            
            // Crear la bala con el ángulo calculado
            Bala nuevaBala = new Bala(
                origenX, 
                origenY, 
                anguloActual,
                LoadSave.BULLET_PIILIP,
                2, // Daño reducido por bala (de 4 a 2) para balancear
                1.8f // Velocidad
            );
            
            adminBalas.agregarBala(nuevaBala);
        }
    }
}