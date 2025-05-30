package Eventos;

import java.awt.event.*;

import Juegos.EstadoJuego;
import Juegos.PanelJuego;

public class EventoTeclado implements KeyListener {
    private PanelJuego pan;
    private EventoGamepad gamepadController;

    public EventoTeclado(PanelJuego pan, EventoGamepad gamepadController) {
        this.pan = pan;
        this.gamepadController = gamepadController;
    }

    @Override
    public void keyTyped(KeyEvent e) {

    }

    @Override
    public void keyPressed(KeyEvent e) {
        if (e.getKeyCode() == KeyEvent.VK_F10) {
            gamepadController.toggleGamepad();
            return;
        }
        switch (pan.getGame().getEstadoJuego()) {
            case SELECCION_PERSONAJE:
                pan.getGame().getSelectorPersonajes().keyPressed(e);
                return;
            case PLAYING:
                switch (e.getKeyCode()) {
                    case KeyEvent.VK_W:
                        pan.getGame().getPlayer().setUp(true);
                        break;
                    case KeyEvent.VK_S:
                        pan.getGame().getPlayer().setDown(true);
                        break;
                    case KeyEvent.VK_A:
                        pan.getGame().getPlayer().setLeft(true);
                        break;
                    case KeyEvent.VK_D:
                        pan.getGame().getPlayer().setRight(true);
                        break;
                    case KeyEvent.VK_SPACE:
                        pan.getGame().getPlayer().setJump(true);
                        break;
                    case KeyEvent.VK_Q:
                        pan.getGame().getPlayer().cambiarArma();
                        break;
                    case KeyEvent.VK_E:
                            pan.getGame().interactuarConEstacionQuimica();
                        break;
                    case KeyEvent.VK_SHIFT:
                        pan.getGame().getPlayer().setHacerDodgeRoll(true);
                        break;
                }
                default:
                break;
            }
            if (e.getKeyCode() == KeyEvent.VK_ESCAPE) {
                if (pan.getGame().getEstadoJuego() == EstadoJuego.PLAYING) {
                    pan.getGame().setEstadoJuego(EstadoJuego.PAUSA);
                } else if (pan.getGame().getEstadoJuego() == EstadoJuego.PAUSA) {
                    pan.getGame().setEstadoJuego(EstadoJuego.PLAYING);
                }
            }
    }

    @Override
    public void keyReleased(KeyEvent e) {
        switch (e.getKeyCode()) {
            case KeyEvent.VK_W:
                pan.getGame().getPlayer().setUp(false);
                break;
            case KeyEvent.VK_S:
                pan.getGame().getPlayer().setDown(false);
                break;
            case KeyEvent.VK_A:
                pan.getGame().getPlayer().setLeft(false);
                break;
            case KeyEvent.VK_D:
                pan.getGame().getPlayer().setRight(false);
                break;
            case KeyEvent.VK_SPACE:
                pan.getGame().getPlayer().setJump(false);;
                break;
            case KeyEvent.VK_SHIFT:
                pan.getGame().getPlayer().setHacerDodgeRoll(false);
                break;
        }
        if (e.getKeyCode() >= KeyEvent.VK_1 && e.getKeyCode() <= KeyEvent.VK_9 || 
            e.getKeyCode() == KeyEvent.VK_ESCAPE) {
                System.out.println(e.getKeyCode());
            pan.getGame().procesarTeclaEstacionQuimica(e.getKeyCode());
        }
    }

}
