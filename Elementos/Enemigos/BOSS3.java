package Elementos.Enemigos;

import java.awt.Graphics;
import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.util.Random;

import Elementos.Enemigo;
import Elementos.Audio.AudioManager;
import Juegos.Juego;
import Utilz.LoadSave;
import Utilz.Animaciones;

public class BOSS3 extends Enemigo {
    // Constantes específicas
    private static final int ANCHO_DEFAULT = 246;
    private static final int ALTO_DEFAULT = 190;
    private static final int VIDA_DEFAULT = 1600000;
    
    // Estados del jefe
    private static final int FASE_NORMAL = 0;
    private static final int FASE_ENOJADO = 1;
    private static final int FASE_FURIOSO = 2;
    
    // Estado actual
    private int faseActual = FASE_NORMAL;
    
    private boolean activated = false;
    // Para ataques
    private BufferedImage[] explosion_sprites;
    private int explosion_duracion = 15;
    private int explosion_velocidad = 50; // Controla la velocidad de la animación (más alto = más lento)
    
    // Control de ataques
    private int cooldownExplosion = 0;
    private int maxCooldownExplosion = 120; // 2 segundos a 60 FPS
    private Random random = new Random();
    
    // Lista para manejar múltiples explosiones
    private ArrayList<ExplosionData> explosiones = new ArrayList<>();
    
    // Clase interna para manejar múltiples explosiones
    private class ExplosionData {
        float x, y;
        int frame = 0;
        int tickCount = 0;
        boolean damageApplied = false;
        
        public ExplosionData(float x, float y) {
            this.x = x;
            this.y = y;
        }
    }

    public BOSS3(float x, float y) {
        super(x, y, 
            (int)(ANCHO_DEFAULT * Juego.SCALE), 
            (int)(ALTO_DEFAULT * Juego.SCALE), 
            VIDA_DEFAULT);
        
        // Inicializar propiedades - sin movimiento
        inicializarEnemigo(50, 50, 146, 140, false, false);
        this.patrullando = false; // No patrulla
        this.rangoDeteccionJugador = 300 * Juego.SCALE; // Rango muy amplio
        
        // Cargar sprites de explosión
        cargarSpritesExplosion();
        
        // Cargar animaciones
        cargarAnimaciones();
    }
    
    private void cargarSpritesExplosion() {
        BufferedImage spriteSheet = LoadSave.GetSpriteAtlas("balas/EXPLOSION.png");
        explosion_sprites = new BufferedImage[explosion_duracion];
        
        int frameWidth = 300;
        int frameHeight = 300;
        
        for (int i = 0; i < explosion_duracion; i++) {
            explosion_sprites[i] = spriteSheet.getSubimage(
                i * frameWidth, 0, frameWidth, frameHeight);
        }
    }
    
    @Override
    protected void cargarAnimaciones() {
        // Cargar sprite de Bagarok para las 3 fases, con 3 frames cada fase
        BufferedImage img = LoadSave.GetSpriteAtlas("enemigos/Bagarok 264x190.png");
        
        // 3 fases con 3 frames por fase
        spritesEnemigo = new BufferedImage[3][3];
        
        // Ancho y alto de cada frame
        int frameWidth = 264;
        int frameHeight = 190;
        
        // Extraer cada frame para cada fase
        for (int fase = 0; fase < 3; fase++) {
            for (int frame = 0; frame < 3; frame++) {
                // Calcular la posición en el spritesheet
                // Asumiendo que están organizados en filas, cada fila es una fase
                // y cada columna es un frame de la animación
                spritesEnemigo[fase][frame] = img.getSubimage(
                    frame * frameWidth,  // Columna (frame)
                    fase * frameHeight,  // Fila (fase)
                    frameWidth, frameHeight);
            }
        }
        
        // Crear animaciones
        animaciones = new Animaciones(spritesEnemigo);
        
        // Configurar número de frames por animación
        animaciones.setNumFramesPorAnimacion(FASE_NORMAL, 3);
        animaciones.setNumFramesPorAnimacion(FASE_ENOJADO, 3);
        animaciones.setNumFramesPorAnimacion(FASE_FURIOSO, 3);
        
        // Hacer que la animación sea más lenta
        animaciones.setAnimVelocidad(20);
        
        // Animación inicial
        animaciones.setAccion(FASE_NORMAL);
    }
    
    @Override
    public void update() {
        if (!activated && Juego.jugadorActual != null) {
            // Check if player is within detection range
            float playerX = Juego.jugadorActual.getXCenter();
            float playerY = Juego.jugadorActual.getYCenter();
            float bossX = hitbox.x + hitbox.width/2;
            float bossY = hitbox.y + hitbox.height/2;
            
            float dx = playerX - bossX;
            float dy = playerY - bossY;
            float distance = (float) Math.sqrt(dx*dx + dy*dy);
            
            // Activation range - adjust as needed
            float activationRange = 500 * Juego.SCALE;
            
            if (distance <= activationRange) {
                activated = true;
                System.out.println("¡BOSS3 ha sido activado!");
                AudioManager.getInstance().playMusic("boss3");
            } else {
                // If not activated yet, don't update any boss behaviors
                return;
            }
        }

        if (!activo) return;


        
        // Actualizar fase según vida restante
        actualizarFase();
        
        // Comportamiento según fase
        switch (faseActual) {
            case FASE_NORMAL:
                comportamientoFaseNormal();
                break;
            case FASE_ENOJADO:
                comportamientoFaseEnojado();
                break;
            case FASE_FURIOSO:
                comportamientoFaseFuriosa();
                break;
        }
        
        // Actualizar explosiones
        actualizarExplosiones();
        
        // Actualizar cooldowns
        if (cooldownExplosion > 0) {
            cooldownExplosion--;
        }
        
        // Actualizar animación según fase
        animaciones.setAccion(faseActual);
        animaciones.actualizarAnimacion();
    }
    
    private void actualizarFase() {
        int porcentajeVida = (vida * 100) / vidaMaxima;
        
        if (porcentajeVida < 30 && faseActual != FASE_FURIOSO) {
            cambiarFase(FASE_FURIOSO);
        } else if (porcentajeVida < 65 && faseActual != FASE_ENOJADO && faseActual != FASE_FURIOSO) {
            cambiarFase(FASE_ENOJADO);
        }
    }
    
    private void cambiarFase(int nuevaFase) {
        this.faseActual = nuevaFase;
        
        // Cambiar propiedades según la fase
        switch (faseActual) {
            case FASE_NORMAL:
                this.maxCooldownExplosion = 120;
                break;
            case FASE_ENOJADO:
                this.maxCooldownExplosion = 90;
                break;
            case FASE_FURIOSO:
                this.maxCooldownExplosion = 60;
                break;
        }
    }
    
    private void comportamientoFaseNormal() {
        // Intentar disparar explosión
        if (cooldownExplosion <= 0 && random.nextFloat() < 0.02) {
            lanzarExplosion();
        }
    }
    
    private void comportamientoFaseEnojado() {
        // Intentar disparar explosión con mayor frecuencia
        if (cooldownExplosion <= 0 && random.nextFloat() < 0.04) {
            lanzarExplosion();
            
            // Posibilidad de lanzar una segunda explosión
            if (random.nextFloat() < 0.3) {
                lanzarExplosionAleatoria();
            }
        }
    }
    
    private void comportamientoFaseFuriosa() {
        // Intentar disparar explosión con alta frecuencia
        if (cooldownExplosion <= 0 && random.nextFloat() < 0.06) {
            lanzarExplosion();
            
            // Alta posibilidad de lanzar explosiones adicionales
            if (random.nextFloat() < 0.7) {
                lanzarExplosionAleatoria();
                lanzarExplosionAleatoria();
            }
        }
    }
    
    private void lanzarExplosion() {
        if (Juego.jugadorActual == null) return;
        
        // Obtener la posición del jugador
        float jugadorX = Juego.jugadorActual.getXCenter();
        float jugadorY = Juego.jugadorActual.getYCenter();
        
        // Crear una nueva explosión
        explosiones.add(new ExplosionData(jugadorX, jugadorY));
        
        // Reiniciar cooldown
        cooldownExplosion = maxCooldownExplosion;
    }
    
    private void lanzarExplosionAleatoria() {
        if (Juego.jugadorActual == null) return;
        
        // Obtener la posición del jugador como base
        float jugadorX = Juego.jugadorActual.getXCenter();
        float jugadorY = Juego.jugadorActual.getYCenter();
        
        // Añadir un offset aleatorio
        float offsetX = (random.nextFloat() * 400 - 200) * Juego.SCALE;
        float offsetY = (random.nextFloat() * 400 - 200) * Juego.SCALE;
        
        // Crear una nueva explosión en una posición cercana al jugador
        explosiones.add(new ExplosionData(jugadorX + offsetX, jugadorY + offsetY));
    }
    
    private void actualizarExplosiones() {
        ArrayList<ExplosionData> explosionesTerminadas = new ArrayList<>();
        
        for (ExplosionData explosion : explosiones) {
            // Incrementar contador de ticks
            explosion.tickCount++;
            
            // Avanzar frame solo cuando se alcanza la velocidad deseada
            if (explosion.tickCount >= explosion_velocidad) {
                explosion.frame++;
                explosion.tickCount = 0;
                
                // Aplicar daño en el frame 6
                if (explosion.frame == 6 && !explosion.damageApplied) {
                    aplicarDañoExplosion(explosion);
                    explosion.damageApplied = true;
                }
            }
            
            // Marcar para eliminar si terminó
            if (explosion.frame >= explosion_duracion) {
                explosionesTerminadas.add(explosion);
            }
        }
        
        // Eliminar explosiones terminadas
        explosiones.removeAll(explosionesTerminadas);
    }
    
    private void aplicarDañoExplosion(ExplosionData explosion) {
        if (Juego.jugadorActual == null) return;
        
        // Definir radio de la explosión
        float radioExplosion = 100 * Juego.SCALE;
        
        // Calcular distancia al jugador
        float dx = Juego.jugadorActual.getXCenter() - explosion.x;
        float dy = Juego.jugadorActual.getYCenter() - explosion.y;
        float distancia = (float) Math.sqrt(dx*dx + dy*dy);
        
        // Si el jugador está dentro del radio, aplicar daño
        if (distancia <= radioExplosion) {
            int dañoBase = 15;
            
            // Mayor daño según fase
            switch (faseActual) {
                case FASE_NORMAL:
                    dañoBase = 15;
                    break;
                case FASE_ENOJADO:
                    dañoBase = 20;
                    break;
                case FASE_FURIOSO:
                    dañoBase = 25;
                    break;
            }
            
            // El daño disminuye con la distancia
            float factorDistancia = 1.0f - (distancia / radioExplosion);
            int dañoFinal = Math.max(5, (int)(dañoBase * factorDistancia));
            
            Juego.jugadorActual.recibirDaño(dañoFinal);
        }
    }
    
    @Override
    public void render(Graphics g, int xLvlOffset, int yLvlOffset) {
        // Renderizar al jefe
        if (!activo) return;
        super.render(g, xLvlOffset, yLvlOffset);
        if (activated) {
            renderHealthBar(g, xLvlOffset, yLvlOffset);
        }
        
        // Dibujar BOSS3
        int drawX = (int) (hitbox.x - xDrawOffset) - xLvlOffset;
        int drawY = (int) (hitbox.y - yDrawOffset) - yLvlOffset;
        
        // No necesita voltear la imagen ya que no se mueve
        g.drawImage(animaciones.getImagenActual(),
            drawX, drawY,
            w, h, null);
        
        // Renderizar explosiones activas
        for (ExplosionData explosion : explosiones) {
            if (explosion.frame < explosion_duracion) {
                int explosionSize = (int)(150 * Juego.SCALE);
                
                // Dibujar la explosión centrada en su posición
                g.drawImage(explosion_sprites[explosion.frame],
                    (int)(explosion.x - explosionSize/2) - xLvlOffset,
                    (int)(explosion.y - explosionSize/2) - yLvlOffset,
                    explosionSize, explosionSize, null);
            }
        }
    }

    @Override
    protected void renderHealthBar(Graphics g, int xLvlOffset, int yLvlOffset) {
        if (!healthBarLoaded) {
            loadHealthBarSprites();
        }
        
        // Calculate health percentage
        float healthPercentage = (float) vida / vidaMaxima;
        
        // Select the appropriate sprite (0 = full health, 10 = empty)
        int spriteIndex = Math.min(10, 10 - (int)(healthPercentage * 10));
        
        // Position the health bar above the enemy
        int barX = (int) (hitbox.x + hitbox.width/2 - 32*Juego.SCALE) - xLvlOffset;
        int barY = (int) (hitbox.y - 60*Juego.SCALE) - yLvlOffset;
        
        // Draw the health bar with scaling
        g.drawImage(healthBarSprites[spriteIndex], 
                    barX, barY, 
                    (int)(64*Juego.SCALE), (int)(32*Juego.SCALE), null);
    }

    @Override
    public void recibirDaño(int cantidad, String tipoDaño) {
        if (!activo)
            return;

        float multiplicador = obtenerMultiplicadorDaño(tipoDaño);
        int dañoFinal = (int)(cantidad * multiplicador);

        vida -= dañoFinal;


        if (vida <= 0) {
            vida = 0;
            morir();
        }
    }

    @Override
    protected void disparar(float angulo) {
        // No tiene disparos convencionales
    }

    @Override
    protected void determinarAnimacion() {
        // No necesitamos lógica adicional, ya que la animación se controla por fase
    }
    
    @Override
    protected float obtenerMultiplicadorDaño(String tipoDaño) {
        if (tipoDaño == null) return 1.0f;
        
        switch (tipoDaño) {
            case "Corrosivo":
                return 0.5f; // Resistente a corrosión
            case "Luz":
                return 0.5f; // Débil a luz
            case "Mutagenico":
                return 1.5f; // Muy resistente a fuego
            default:
                return 1.0f;
        }
    }

    
    public boolean isActivated() {
        return activated;
    }
}