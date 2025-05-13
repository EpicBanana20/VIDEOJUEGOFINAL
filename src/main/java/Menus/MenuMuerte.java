package Menus;

import java.awt.Graphics;
import java.awt.Color;
import java.awt.event.MouseEvent;
import java.awt.image.BufferedImage;

import Elementos.Audio.AudioManager;
import Juegos.EstadoJuego;
import Juegos.Juego;
import Utilz.LoadSave;

public class MenuMuerte {
    private Juego juego;
    private Boton[] botones = new Boton[2];
    private BufferedImage[][] botonesImgs;

    // Constantes para posiciones de botones
    private final int BOTON_SPACING = 200;
    private final int ALTURA_PRIMER_BOTON = 600;
    private final int BOTON_X_POSICION = Juego.GAME_WIDTH / 2;

    // Índices de botones
    private static final int BOTON_REINTENTAR = 0;
    private static final int BOTON_MENU = 1;

    // Índices reales en el sprite sheet
    private static final int SPRITE_REINTENTAR = 4;
    private static final int SPRITE_MENU = 5;

    private BufferedImage gameOverImg;

    private int botonSeleccionadoIndex = 0;

    public MenuMuerte(Juego juego) {
        this.juego = juego;
        cargarImagenes();
        cargarBotones();
    }

    private void cargarImagenes() {
        // Cargar imagen de GAME OVER (deberá existir en recursos)
        gameOverImg = LoadSave.GetSpriteAtlas("MUERTE.png");

        // Cargar sprites de botones
        BufferedImage botonesSprite = LoadSave.GetSpriteAtlas("Botones 40x25.png");
        botonesImgs = new BufferedImage[6][3];

        for (int j = 0; j < botonesImgs.length; j++) {
            for (int i = 0; i < botonesImgs[j].length; i++) {
                botonesImgs[j][i] = botonesSprite.getSubimage(
                        i * 40, j * 25, 40, 25);
            }
        }
    }

    private void cargarBotones() {
        // Botón Reintentar
        botones[BOTON_REINTENTAR] = new Boton(
                BOTON_X_POSICION,
                ALTURA_PRIMER_BOTON,
                BOTON_REINTENTAR,
                botonesImgs[SPRITE_REINTENTAR]);

        // Botón Menú
        botones[BOTON_MENU] = new Boton(
                BOTON_X_POSICION,
                ALTURA_PRIMER_BOTON + BOTON_SPACING,
                BOTON_MENU,
                botonesImgs[SPRITE_MENU]);
    }

    public void update() {
        for (Boton b : botones)
            b.update();
    }

    public void draw(Graphics g) {
        // Dibujar fondo semitransparente oscuro
        g.setColor(new Color(0, 0, 0, 200));
        g.fillRect(0, 0, Juego.GAME_WIDTH, Juego.GAME_HEIGHT);

        // Dibujar imagen de GAME OVER (centrada horizontalmente)
        int gameOverX = Juego.GAME_WIDTH / 2 - gameOverImg.getWidth() / 2;
        int gameOverY = ALTURA_PRIMER_BOTON - 600; // 300 píxeles arriba del primer botón
        g.drawImage(gameOverImg, gameOverX, gameOverY, null);

        // Dibujar botones
        for (Boton b : botones)
            b.draw(g);
    }

    public void mousePressed(MouseEvent e) {
        for (int i = 0; i < botones.length; i++) {
            if (estaDentroBoton(e, botones[i])) {
                botones[i].setMousePressed(true);
                break;
            }
        }
    }

    public void mouseReleased(MouseEvent e) {
        for (int i = 0; i < botones.length; i++) {
            if (e == null && i == botonSeleccionadoIndex || 
                e != null && estaDentroBoton(e, botones[i])) {
                if (botones[i].isMousePressed()) {
                    // Acciones según el botón presionado
                    switch (i) {
                        case BOTON_REINTENTAR:
                            juego.reiniciarJuego();
                            juego.setEstadoJuego(EstadoJuego.PLAYING);
                            break;
                        case BOTON_MENU:
                            juego.setEstadoJuego(EstadoJuego.MENU);
                            break;
                    }
                }
                break;
            }
        }
        
        resetBotones();
    }

    // Métodos para navegación con gamepad
    public void navegarArriba() {
        botonSeleccionadoIndex--;
        if (botonSeleccionadoIndex < 0)
            botonSeleccionadoIndex = botones.length - 1;

        actualizarBotonesSeleccionados();
    }

    public void navegarAbajo() {
        botonSeleccionadoIndex++;
        if (botonSeleccionadoIndex >= botones.length)
            botonSeleccionadoIndex = 0;

        actualizarBotonesSeleccionados();
    }

    public void actualizarBotonesSeleccionados() {
        for (int i = 0; i < botones.length; i++) {
            botones[i].setMouseOver(i == botonSeleccionadoIndex);
        }
    }

    public void ejecutarBotonSeleccionado() {
        if (botonSeleccionadoIndex >= 0 && botonSeleccionadoIndex < botones.length) {
            botones[botonSeleccionadoIndex].setMousePressed(true);
            mouseReleased(null);
        }
    }

    public void presionarBotonSeleccionado() {
        if (botonSeleccionadoIndex >= 0 && botonSeleccionadoIndex < botones.length) {
            botones[botonSeleccionadoIndex].setMousePressed(true);
        }
    }

    public void mouseMoved(MouseEvent e) {
    for (int i = 0; i < botones.length; i++) {
        boolean wasHighlighted = botones[i].isMouseOver();
        botones[i].setMouseOver(false);
        
        if (estaDentroBoton(e, botones[i])) {
            botones[i].setMouseOver(true);
            // Si el botón no estaba resaltado antes, reproducir sonido
            if (!wasHighlighted) {
                AudioManager.getInstance().playSoundEffect("select");
            }
        }
    }
}

    private void resetBotones() {
        for (Boton b : botones)
            b.resetBools();
    }

    private boolean estaDentroBoton(MouseEvent e, Boton b) {
        return b.getBounds().contains(e.getX(), e.getY());
    }
}