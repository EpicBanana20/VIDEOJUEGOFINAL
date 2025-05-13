package Elementos;

import Juegos.Juego;

public class BalaCreciente extends Bala {
    // Parámetros para el crecimiento
    private float factorCrecimientoInicial;
    private float factorCrecimientoMax;
    private float factorCrecimientoActual;
    private float velocidadCrecimiento;
    
    // Almacenar tamaños originales
    private float anchoHitboxOriginal;
    private float altoHitboxOriginal;
    private int anchoSpriteOriginal;
    private int altoSpriteOriginal;
    
    // Almacenar daño original
    private int dañoBase;
    
    // Distancia recorrida (para referencia)
    private float distanciaRecorrida = 0;

    public BalaCreciente(float x, float y, float angulo, String tipoSprite, int daño, 
                          float velocidad, float factorInicial, float factorMax, float velCrecimiento) {
        super(x, y, angulo, tipoSprite, daño, velocidad);
        
        // Inicializar parámetros de crecimiento
        this.factorCrecimientoInicial = factorInicial;
        this.factorCrecimientoMax = factorMax;
        this.factorCrecimientoActual = factorInicial;
        this.velocidadCrecimiento = velCrecimiento;
        
        // Guardar daño base
        this.dañoBase = daño;
        
        // Guardar tamaños originales
        this.anchoHitboxOriginal = super.hitbox.width;
        this.altoHitboxOriginal = super.hitbox.height;
        this.anchoSpriteOriginal = (int)(16 * Juego.SCALE); // Basado en el valor de la clase Bala
        this.altoSpriteOriginal = (int)(16 * Juego.SCALE);
        
        // Aplicar tamaño inicial
        actualizarTamaño();
    }
    
    // Sobrecarga para incluir tipoDaño
    public BalaCreciente(float x, float y, float angulo, String tipoSprite, int daño, 
                          float velocidad, String tipoDaño, float factorInicial, 
                          float factorMax, float velCrecimiento) {
        super(x, y, angulo, tipoSprite, daño, velocidad, tipoDaño);
        
        // Inicializar parámetros de crecimiento
        this.factorCrecimientoInicial = factorInicial;
        this.factorCrecimientoMax = factorMax;
        this.factorCrecimientoActual = factorInicial;
        this.velocidadCrecimiento = velCrecimiento;
        
        // Guardar daño base
        this.dañoBase = daño;
        
        // Guardar tamaños originales
        this.anchoHitboxOriginal = super.hitbox.width;
        this.altoHitboxOriginal = super.hitbox.height;
        this.anchoSpriteOriginal = (int)(16 * Juego.SCALE);
        this.altoSpriteOriginal = (int)(16 * Juego.SCALE);
        
        // Aplicar tamaño inicial
        actualizarTamaño();
    }
    
    @Override
    public void update() {
        // Guardar posición antes de actualizar
        float oldX = hitbox.x;
        float oldY = hitbox.y;
        
        // Llamar al update normal
        super.update();
        
        // Calcular distancia recorrida en este frame
        float dx = hitbox.x - oldX;
        float dy = hitbox.y - oldY;
        float distanciaFrame = (float) Math.sqrt(dx*dx + dy*dy);
        distanciaRecorrida += distanciaFrame;
        
        // Incrementar el factor de crecimiento
        factorCrecimientoActual += velocidadCrecimiento;
        
        // Limitar al máximo
        if (factorCrecimientoActual > factorCrecimientoMax) {
            factorCrecimientoActual = factorCrecimientoMax;
        }
        
        // Actualizar el tamaño basado en el nuevo factor
        actualizarTamaño();
    }
    
    // Método para actualizar el tamaño de la bala basado en el factor de crecimiento
    private void actualizarTamaño() {
        // Calcular nuevo tamaño para hitbox
        float nuevoAnchoHitbox = anchoHitboxOriginal * factorCrecimientoActual;
        float nuevoAltoHitbox = altoHitboxOriginal * factorCrecimientoActual;
        
        // Calcular nuevo tamaño para sprite
        int nuevoAnchoSprite = (int)(anchoSpriteOriginal * factorCrecimientoActual);
        int nuevoAltoSprite = (int)(altoSpriteOriginal * factorCrecimientoActual);
        
        // Obtener la posición central antes de cambiar el tamaño
        float centerX = hitbox.x + hitbox.width / 2;
        float centerY = hitbox.y + hitbox.height / 2;
        
        // Actualizar tamaño del hitbox
        hitbox.width = nuevoAnchoHitbox;
        hitbox.height = nuevoAltoHitbox;
        
        // Reposicionar para mantener centrado
        hitbox.x = centerX - nuevoAnchoHitbox / 2;
        hitbox.y = centerY - nuevoAltoHitbox / 2;
        
        // Actualizar posición
        x = hitbox.x;
        y = hitbox.y;
        
        // Actualizar tamaño del sprite
        try {
            // Usar reflexión para acceder a los campos privados
            java.lang.reflect.Field anchoField = Bala.class.getDeclaredField("anchoSprite");
            java.lang.reflect.Field altoField = Bala.class.getDeclaredField("altoSprite");
            
            anchoField.setAccessible(true);
            altoField.setAccessible(true);
            
            anchoField.set(this, nuevoAnchoSprite);
            altoField.set(this, nuevoAltoSprite);
        } catch (Exception e) {
            System.err.println("Error al ajustar el tamaño del sprite: " + e.getMessage());
            // Fallback si la reflexión falla
            try {
                this.getClass().getSuperclass().getDeclaredField("anchoSprite").set(this, nuevoAnchoSprite);
                this.getClass().getSuperclass().getDeclaredField("altoSprite").set(this, nuevoAltoSprite);
            } catch (Exception ex) {
                System.err.println("Error en fallback: " + ex.getMessage());
            }
        }
    }
    
    // Sobrescribir el método getDaño para que el daño aumente con el tamaño
    @Override
    public int getDaño() {
        // Calcular el daño basado en el factor de crecimiento actual
        // Proporción directa: si la bala es el doble de grande, hace el doble de daño
        int dañoActual = (int)(dañoBase * (factorCrecimientoActual / factorCrecimientoInicial));
        
        // Asegurar un daño mínimo igual al daño base
        return Math.max(dañoBase, dañoActual);
    }
    
    // Getters y setters adicionales
    public float getFactorCrecimientoActual() {
        return factorCrecimientoActual;
    }
    
    public void setVelocidadCrecimiento(float velocidad) {
        this.velocidadCrecimiento = velocidad;
    }
    
    public void setFactorCrecimientoMax(float factor) {
        this.factorCrecimientoMax = factor;
    }
}