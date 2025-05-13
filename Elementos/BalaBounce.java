package Elementos;

import Juegos.Juego;

public class BalaBounce extends Bala {
    private int rebotesActuales = 0;
    private int rebotesMaximos = 15;
    private float anguloInterno; // Guarda el ángulo real de movimiento
    private int cooldownRebote = 5; // Aumentado para evitar rebotes múltiples
    
    // Estas variables las necesitamos para poder modificar las velocidades en la clase padre
    private float velocidad;

    public BalaBounce(float x, float y, float angulo, String tipoSprite, int daño, float velocidad) {
        super(x, y, angulo, tipoSprite, daño, velocidad);
        this.anguloInterno = angulo;
        this.velocidad = velocidad * Juego.SCALE; // Guardar la velocidad original
    }

    public BalaBounce(float x, float y, float angulo, String tipoSprite, int daño, float velocidad, String tipoDaño) {
        super(x, y, angulo, tipoSprite, daño, velocidad, tipoDaño);
        this.anguloInterno = angulo;
        this.velocidad = velocidad * Juego.SCALE; // Guardar la velocidad original
    }

    public BalaBounce(float x, float y, float angulo, String tipoSprite, int daño, float velocidad, String tipoDaño, int rebotesMaximos) {
        super(x, y, angulo, tipoSprite, daño, velocidad, tipoDaño);
        this.rebotesMaximos = rebotesMaximos;
        this.anguloInterno = angulo;
        this.velocidad = velocidad * Juego.SCALE; // Guardar la velocidad original
    }

    // Método auxiliar para actualizar las velocidades según el ángulo
    private void actualizarVelocidades(float nuevoAngulo) {
        try {
            // Usamos reflexión para acceder a los campos privados de la clase padre
            java.lang.reflect.Field velocidadXField = Bala.class.getDeclaredField("velocidadX");
            java.lang.reflect.Field velocidadYField = Bala.class.getDeclaredField("velocidadY");
            
            velocidadXField.setAccessible(true);
            velocidadYField.setAccessible(true);
            
            // Calcular nuevas velocidades basadas en el ángulo
            float nuevaVelocidadX = (float) Math.cos(nuevoAngulo) * velocidad;
            float nuevaVelocidadY = (float) Math.sin(nuevoAngulo) * velocidad;
            
            // Actualizar las velocidades en la clase padre
            velocidadXField.set(this, nuevaVelocidadX);
            velocidadYField.set(this, nuevaVelocidadY);
        } catch (Exception e) {
            System.err.println("Error al actualizar velocidades de la bala: " + e.getMessage());
        }
    }

    @Override
    public void update() {
        if (cooldownRebote > 0) {
            cooldownRebote--;
        }

        float oldX = hitbox.x;
        float oldY = hitbox.y;

        super.update();

        // Actualizar el ángulo interno basado en el movimiento real
        float movX = hitbox.x - oldX;
        float movY = hitbox.y - oldY;

        if (movX != 0 || movY != 0) {
            this.anguloInterno = (float) Math.atan2(movY, movX);
        }
    }

    @Override
    public boolean colisionConBloque() {
        if (cooldownRebote > 0) return false;

        // Verificar colisiones en varios puntos para mayor precisión
        float centerX = hitbox.x + hitbox.width / 2;
        float centerY = hitbox.y + hitbox.height / 2;
        
        boolean colisionArriba = Utilz.MetodoAyuda.isSolido(centerX, hitbox.y, Juego.NIVEL_ACTUAL_DATA);
        boolean colisionAbajo = Utilz.MetodoAyuda.isSolido(centerX, hitbox.y + hitbox.height, Juego.NIVEL_ACTUAL_DATA);
        boolean colisionIzquierda = Utilz.MetodoAyuda.isSolido(hitbox.x, centerY, Juego.NIVEL_ACTUAL_DATA);
        boolean colisionDerecha = Utilz.MetodoAyuda.isSolido(hitbox.x + hitbox.width, centerY, Juego.NIVEL_ACTUAL_DATA);

        if (colisionArriba || colisionAbajo || colisionIzquierda || colisionDerecha) {
            // Si alcanzamos el máximo de rebotes, desactivar
            if (rebotesActuales >= rebotesMaximos) {
                this.desactivar();
                return true;
            }

            rebotesActuales++;
            
            // Obtener componentes de dirección actuales
            float dx = (float) Math.cos(anguloInterno);
            float dy = (float) Math.sin(anguloInterno);
            
            // Determinar qué lado debe reflejar basado en la dirección principal
            boolean moviendoMasHorizontal = Math.abs(dx) > Math.abs(dy);
            
            if (moviendoMasHorizontal) {
                // Moviéndose principalmente horizontal
                if ((dx > 0 && colisionDerecha) || (dx < 0 && colisionIzquierda)) {
                    dx = -dx; // Invertir componente horizontal
                } 
                // Ver si también hay colisión vertical
                if ((dy > 0 && colisionAbajo) || (dy < 0 && colisionArriba)) {
                    dy = -dy; // Invertir componente vertical
                }
            } else {
                // Moviéndose principalmente vertical
                if ((dy > 0 && colisionAbajo) || (dy < 0 && colisionArriba)) {
                    dy = -dy; // Invertir componente vertical
                }
                // Ver si también hay colisión horizontal
                if ((dx > 0 && colisionDerecha) || (dx < 0 && colisionIzquierda)) {
                    dx = -dx; // Invertir componente horizontal
                }
            }
            
            // Calcular nuevo ángulo
            float nuevoAngulo = (float) Math.atan2(dy, dx);
            
            // Actualizar ángulos
            this.angulo = nuevoAngulo;
            this.anguloInterno = nuevoAngulo;
            
            // IMPORTANTE: Actualizar las velocidades para cambiar la trayectoria real
            actualizarVelocidades(nuevoAngulo);
            
            // Alejar la bala de la colisión para evitar quedarse atrapada
            float distanciaEmpuje = 10.0f; 
            hitbox.x += dx * distanciaEmpuje;
            hitbox.y += dy * distanciaEmpuje;
            
            // Establecer cooldown para evitar múltiples rebotes
            cooldownRebote = 5;
            
            return true;
        }

        return false;
    }

    public void setRebotesMaximos(int rebotesMaximos) {
        this.rebotesMaximos = rebotesMaximos;
    }

    public int getRebotesMaximos() {
        return rebotesMaximos;
    }

    public int getRebotesActuales() {
        return rebotesActuales;
    }
}