package Elementos.Decoraciones;

import java.awt.Graphics;
import java.awt.Color;
import java.awt.Font;
import java.awt.image.BufferedImage;
import Juegos.Juego;
import Elementos.Quimica.SistemaQuimico;
import Elementos.Quimica.RecetaCompuesto;
import Utilz.LoadSave;
import java.util.List;
import java.util.Map;

public class EstacionQuimica extends Decoracion {
    private BufferedImage[] sprites; // Array para los dos estados (inactivo/activo)
    private int spriteActual = 0; // 0 = inactivo, 1 = activo
    private float distanciaInteraccion = 50f * Juego.SCALE;
    private boolean jugadorCerca = false;
    private boolean estacionAbierta = false;
    private SistemaQuimico sistemaQuimico;
    private BufferedImage upgradeMenuImg; // Imagen para el menú de mejoras

    public EstacionQuimica(float x, float y, int width, int height, BufferedImage[] sprites) {
        super(x, y, width, height, sprites[0]);
        this.sprites = sprites;
        this.sistemaQuimico = Juego.jugadorActual != null ? Juego.jugadorActual.getSistemaQuimico() : null;
        this.upgradeMenuImg = LoadSave.GetSpriteAtlas("UPGRADE.png"); // Cargar la imagen del menú
    }

    @Override
    public void update() {
        // Verificar si el jugador está cerca para poder interactuar
        if (Juego.jugadorActual != null) {
            float playerX = Juego.jugadorActual.getXCenter();
            float playerY = Juego.jugadorActual.getYCenter();

            float estacionX = x + width / 2;
            float estacionY = y + height / 2;

            float distX = estacionX - playerX;
            float distY = estacionY - playerY;
            float distancia = (float) Math.sqrt(distX * distX + distY * distY);

            // Actualizar estado según distancia
            jugadorCerca = distancia <= distanciaInteraccion;
            spriteActual = jugadorCerca ? 1 : 0;
            sprite = sprites[spriteActual];

            // Actualizar referencia al sistema químico
            if (sistemaQuimico == null && Juego.jugadorActual.getSistemaQuimico() != null) {
                sistemaQuimico = Juego.jugadorActual.getSistemaQuimico();
            }
        }
    }

    @Override
    public void render(Graphics g, int xLvlOffset, int yLvlOffset) {
        super.render(g, xLvlOffset, yLvlOffset);

        // Indicar visualmente que se puede interactuar
        if (jugadorCerca) {
            g.setColor(Color.WHITE);
            String mensaje = "Presiona E para interactuar";
            int textX = (int) (x + width / 2 - g.getFontMetrics().stringWidth(mensaje) / 2) - xLvlOffset;
            int textY = (int) (y - 20) - yLvlOffset;
            g.drawString(mensaje, textX, textY);
        }

        // Si la estación está abierta, mostrar interfaz
        if (estacionAbierta && sistemaQuimico != null) {
            renderizarInterfazCrafteo(g, xLvlOffset, yLvlOffset);
        }
    }

    private void renderizarInterfazCrafteo(Graphics g, int xLvlOffset, int yLvlOffset) {

        // Centrar el menú en la pantalla
    
        int panelX = Juego.GAME_WIDTH / 2 - upgradeMenuImg.getWidth() / 2;
    
        int panelY = Juego.GAME_HEIGHT / 2 - upgradeMenuImg.getHeight() / 2;
    
    
        // Definir el nuevo tamaño para la imagen
    
        int nuevoAncho = upgradeMenuImg.getWidth() * 2; // Cambia 2 por el factor de escala que desees
    
        int nuevoAlto = upgradeMenuImg.getHeight() * 2; // Cambia 2 por el factor de escala que desees
    
    
        // Dibujar imagen de fondo del menú escalada
    
        g.drawImage(upgradeMenuImg, panelX, panelY, nuevoAncho, nuevoAlto, null);
    
    }

    public void interactuar() {
        if (jugadorCerca) {
        // Asegurarse de que sistemaQuimico esté actualizado al momento de interactuar
        if (Juego.jugadorActual != null && Juego.jugadorActual.getSistemaQuimico() != null) {
            sistemaQuimico = Juego.jugadorActual.getSistemaQuimico();
        }
        estacionAbierta = !estacionAbierta;
    }
    }

    public boolean procesarTecla(int keyCode) {
        if (!estacionAbierta)
            return false;

        if (sistemaQuimico == null && Juego.jugadorActual != null) {
        sistemaQuimico = Juego.jugadorActual.getSistemaQuimico();
    }

        boolean compuestoCreado = false;

        // Teclas del 1 al 9 (49-57) para crear compuestos
        if (keyCode >= 49 && keyCode <= 57) {
            int index = keyCode - 49;
            List<RecetaCompuesto> recetas = sistemaQuimico.getRecetasDisponibles();
            if (index < recetas.size()) {
                String formula = recetas.get(index).getFormula();
                System.out.println(formula);
                compuestoCreado = sistemaQuimico.crearCompuesto(formula);
                System.out.println(compuestoCreado);
            }
        }

        // ESC para cerrar
        if (keyCode == 27) {
            estacionAbierta = false;
            return false;
        }

        return compuestoCreado;
    }

    public boolean isJugadorCerca() {
        return jugadorCerca;
    }

    public boolean isEstacionAbierta() {
        return estacionAbierta;
    }
}