package Eventos;

import org.lwjgl.glfw.*;
import static org.lwjgl.glfw.GLFW.*;

import Juegos.PanelJuego;
import Juegos.EstadoJuego;

public class EventoGamepad {
    private PanelJuego panelJuego;
    private int gamepadID = -1;
    private GLFWGamepadState gamepadState;
    private float deadzone = 0.25f;
    
    // Para manejar estado anterior de botones
    private boolean[] prevButtonState = new boolean[GLFW_GAMEPAD_BUTTON_LAST + 1];
    private float lastValidRightX = 0;
    private float lastValidRightY = 0;
    private boolean joystickAimActive = false;

    private boolean gamepadEnabled = true;
    private long lastMenuNavigationTime = 0;
    private static final long MENU_NAVIGATION_COOLDOWN = 200;
    private static final long SELECTOR_NAVIGATION_COOLDOWN = 800;
    
    public EventoGamepad(PanelJuego panelJuego) {
        this.panelJuego = panelJuego;
        
        // Inicializar GLFW para detección de gamepad
        if (!glfwInit()) {
            System.err.println("Error al inicializar GLFW para detección de gamepad");
            return;
        }
        
        this.gamepadState = GLFWGamepadState.create();
        detectarGamepad();
    }
    
    public void detectarGamepad() {
        // Buscar mandos conectados
        for (int i = GLFW_JOYSTICK_1; i <= GLFW_JOYSTICK_LAST; i++) {
            if (glfwJoystickPresent(i) && glfwJoystickIsGamepad(i)) {
                gamepadID = i;
                System.out.println("Mando detectado: " + glfwGetJoystickName(i));
                return;
            }
        }
        System.out.println("No se detectó ningún mando. Conecta un mando y reinicia el juego.");
        gamepadID = -1;
    }
    
    public void update() {
        if(!gamepadEnabled || gamepadID == -1) {
            return; // Si el gamepad está deshabilitado, no procesar entradas
        }

        // Comprobar si el mando sigue conectado
        if (!glfwJoystickPresent(gamepadID)) {
            System.out.println("Mando desconectado");
            gamepadID = -1;
            return;
        }
        
        // Obtener estado del gamepad
        if (!glfwGetGamepadState(gamepadID, gamepadState)) {
            return;
        }

        switch (panelJuego.getGame().getEstadoJuego()) {
            case PLAYING:
                procesarInputsJuego();
                break;
            case MENU:
                procesarInputsMenu();
                break;
            case PAUSA:
                procesarInputsPausa();
                break;
            case MUERTE:
                procesarInputsMuerte();
                break;
            case SELECCION_PERSONAJE:
                procesarInputsSeleccionPersonaje();
                break;
            default:
                break;
        }
        
        // Actualizar estado anterior
        for (int i = 0; i < GLFW_GAMEPAD_BUTTON_LAST; i++) {
            prevButtonState[i] = gamepadState.buttons(i) == 1;
        }
    }
    
    private void procesarInputsJuego() {
        // Movimiento (stick izquierdo)
        float axisX = gamepadState.axes(GLFW_GAMEPAD_AXIS_LEFT_X);
        float axisY = gamepadState.axes(GLFW_GAMEPAD_AXIS_LEFT_Y);
        
        panelJuego.getGame().getPlayer().setLeft(axisX < -deadzone);
        panelJuego.getGame().getPlayer().setRight(axisX > deadzone);
        panelJuego.getGame().getPlayer().setDown(axisY > deadzone);
        
        // Salto
        panelJuego.getGame().getPlayer().setJump(axisY < -deadzone);

        // Disparar (botón RB)
        panelJuego.getGame().getPlayer().setAttacking(gamepadState.axes(GLFW_GAMEPAD_AXIS_RIGHT_TRIGGER) > 0.5f);
        
        // Recargar (botón B)
        panelJuego.getGame().getPlayer().setHacerDodgeRoll(gamepadState.buttons(GLFW_GAMEPAD_BUTTON_X) == 1);

        if(gamepadState.axes(GLFW_GAMEPAD_AXIS_LEFT_TRIGGER) > 0.5f) {
            panelJuego.getGame().getPlayer().usarHabilidadEspecial(); 
        }


        
        // Cambiar arma (botón Y - solo al presionar)
        if (gamepadState.buttons(GLFW_GAMEPAD_BUTTON_Y) == 1 && !prevButtonState[GLFW_GAMEPAD_BUTTON_Y]) {
            panelJuego.getGame().getPlayer().cambiarArma();
            panelJuego.getGame().procesarTeclaEstacionQuimica(49);
        }
        
        if (gamepadState.buttons(GLFW_GAMEPAD_BUTTON_B) == 1 && !prevButtonState[GLFW_GAMEPAD_BUTTON_B]) {
            panelJuego.getGame().procesarTeclaEstacionQuimica(50);
        }

        if (gamepadState.buttons(GLFW_GAMEPAD_BUTTON_A) == 1 && !prevButtonState[GLFW_GAMEPAD_BUTTON_A]) {
            panelJuego.getGame().procesarTeclaEstacionQuimica(51);
        }

        // Interactuar (botón X - solo al presionar)
        if (gamepadState.buttons(GLFW_GAMEPAD_BUTTON_X) == 1 && !prevButtonState[GLFW_GAMEPAD_BUTTON_X]) {
            panelJuego.getGame().procesarTeclaEstacionQuimica(52);
        }

        if (gamepadState.buttons(GLFW_GAMEPAD_BUTTON_DPAD_UP) == 1 && !prevButtonState[GLFW_GAMEPAD_BUTTON_DPAD_UP]) {
            panelJuego.getGame().procesarTeclaEstacionQuimica(53);
        }
        
        if (gamepadState.buttons(GLFW_GAMEPAD_BUTTON_DPAD_RIGHT) == 1 && !prevButtonState[GLFW_GAMEPAD_BUTTON_DPAD_RIGHT]) {
            panelJuego.getGame().procesarTeclaEstacionQuimica(54);
        }
        
        if (gamepadState.buttons(GLFW_GAMEPAD_BUTTON_DPAD_DOWN) == 1 && !prevButtonState[GLFW_GAMEPAD_BUTTON_DPAD_DOWN]) {
            panelJuego.getGame().procesarTeclaEstacionQuimica(55);
        }

        if (gamepadState.buttons(GLFW_GAMEPAD_BUTTON_DPAD_LEFT) == 1 && !prevButtonState[GLFW_GAMEPAD_BUTTON_DPAD_LEFT]) {
            panelJuego.getGame().procesarTeclaEstacionQuimica(56);
        }
        
        if (gamepadState.buttons(GLFW_GAMEPAD_BUTTON_RIGHT_BUMPER) == 1 && !prevButtonState[GLFW_GAMEPAD_BUTTON_RIGHT_BUMPER]){
            panelJuego.getGame().procesarTeclaEstacionQuimica(57);
        }

        if (gamepadState.buttons(GLFW_GAMEPAD_BUTTON_LEFT_BUMPER) == 1 && !prevButtonState[GLFW_GAMEPAD_BUTTON_LEFT_BUMPER]){
            panelJuego.getGame().interactuarConEstacionQuimica();
        }
        
        
        // Apuntar con stick derecho
        float rightX = gamepadState.axes(GLFW_GAMEPAD_AXIS_RIGHT_X);
        float rightY = gamepadState.axes(GLFW_GAMEPAD_AXIS_RIGHT_Y);

        if (Math.abs(rightX) > deadzone || Math.abs(rightY) > deadzone) {
            // Al mover el stick fuera del deadzone, actualizar las últimas posiciones válidas
            lastValidRightX = rightX;
            lastValidRightY = rightY;
            joystickAimActive = true;
        }
        
        // Siempre que el joystick esté activo, usamos la última posición válida
        if (joystickAimActive) {
            panelJuego.getGame().updateAimFromGamepad(lastValidRightX, lastValidRightY);
        }
        // Pausa (botón Start)
        if (gamepadState.buttons(GLFW_GAMEPAD_BUTTON_START) == 1 && !prevButtonState[GLFW_GAMEPAD_BUTTON_START]) {
            panelJuego.getGame().setEstadoJuego(EstadoJuego.PAUSA);
        }
    }

    private void procesarInputsMenu() {
        // Cooldown para la navegación en el menú
        long currentTime = System.currentTimeMillis();
        boolean cooldownElapsed = (currentTime - lastMenuNavigationTime) > MENU_NAVIGATION_COOLDOWN;
        
        // Navegación vertical (stick izquierdo o d-pad)
        float axisY = gamepadState.axes(GLFW_GAMEPAD_AXIS_LEFT_Y);
        boolean dpadUp = gamepadState.buttons(GLFW_GAMEPAD_BUTTON_DPAD_UP) == 1;
        boolean dpadDown = gamepadState.buttons(GLFW_GAMEPAD_BUTTON_DPAD_DOWN) == 1;
        
        if (cooldownElapsed && (axisY < -deadzone || dpadUp) && !prevButtonState[GLFW_GAMEPAD_BUTTON_DPAD_UP]) {
            panelJuego.getGame().getMenu().navegarArriba();
            lastMenuNavigationTime = currentTime; // Actualizar tiempo de último movimiento
            prevButtonState[GLFW_GAMEPAD_BUTTON_DPAD_UP] = true;
        } else if (cooldownElapsed && (axisY > deadzone || dpadDown) && !prevButtonState[GLFW_GAMEPAD_BUTTON_DPAD_DOWN]) {
            panelJuego.getGame().getMenu().navegarAbajo();
            lastMenuNavigationTime = currentTime; // Actualizar tiempo de último movimiento
            prevButtonState[GLFW_GAMEPAD_BUTTON_DPAD_DOWN] = true;
        }
        
        // Resetear estado prev si se suelta el botón
        if (gamepadState.buttons(GLFW_GAMEPAD_BUTTON_DPAD_UP) == 0) {
            prevButtonState[GLFW_GAMEPAD_BUTTON_DPAD_UP] = false;
        }
        if (gamepadState.buttons(GLFW_GAMEPAD_BUTTON_DPAD_DOWN) == 0) {
            prevButtonState[GLFW_GAMEPAD_BUTTON_DPAD_DOWN] = false;
        }
        
        // Seleccionar botón con A
        if (gamepadState.buttons(GLFW_GAMEPAD_BUTTON_A) == 1 && !prevButtonState[GLFW_GAMEPAD_BUTTON_A]) {
            // Solo marcar el botón como presionado visualmente
            panelJuego.getGame().getMenu().presionarBotonSeleccionado();
            prevButtonState[GLFW_GAMEPAD_BUTTON_A] = true;
        } 
        else if (gamepadState.buttons(GLFW_GAMEPAD_BUTTON_A) == 0 && prevButtonState[GLFW_GAMEPAD_BUTTON_A]) {
            // Soltar botón - ahora ejecutar la acción
            panelJuego.getGame().getMenu().ejecutarBotonSeleccionado();
            prevButtonState[GLFW_GAMEPAD_BUTTON_A] = false;
        }
    }

    private void procesarInputsSeleccionPersonaje() {
        // Usar el mismo cooldown del menú
        long currentTime = System.currentTimeMillis();
        boolean cooldownElapsed = (currentTime - lastMenuNavigationTime) > SELECTOR_NAVIGATION_COOLDOWN;
        
        // Navegación con stick izquierdo o d-pad
        float axisY = gamepadState.axes(GLFW_GAMEPAD_AXIS_LEFT_Y);
        boolean dpadUp = gamepadState.buttons(GLFW_GAMEPAD_BUTTON_DPAD_UP) == 1;
        boolean dpadDown = gamepadState.buttons(GLFW_GAMEPAD_BUTTON_DPAD_DOWN) == 1;
        
        // Solo navegar si ha pasado el tiempo de cooldown
        if (cooldownElapsed && (axisY < -deadzone || dpadUp) && !prevButtonState[GLFW_GAMEPAD_BUTTON_DPAD_UP]) {
            panelJuego.getGame().getSelectorPersonajes().seleccionarAnterior();
            lastMenuNavigationTime = currentTime; // Actualizar tiempo
            prevButtonState[GLFW_GAMEPAD_BUTTON_DPAD_UP] = true;
        } else if (cooldownElapsed && (axisY > deadzone || dpadDown) && !prevButtonState[GLFW_GAMEPAD_BUTTON_DPAD_DOWN]) {
            panelJuego.getGame().getSelectorPersonajes().seleccionarSiguiente();
            lastMenuNavigationTime = currentTime; // Actualizar tiempo
            prevButtonState[GLFW_GAMEPAD_BUTTON_DPAD_DOWN] = true;
        }
        
        // Resetear estado prev si se suelta el botón
        if (gamepadState.buttons(GLFW_GAMEPAD_BUTTON_DPAD_UP) == 0) {
            prevButtonState[GLFW_GAMEPAD_BUTTON_DPAD_UP] = false;
        }
        if (gamepadState.buttons(GLFW_GAMEPAD_BUTTON_DPAD_DOWN) == 0) {
            prevButtonState[GLFW_GAMEPAD_BUTTON_DPAD_DOWN] = false;
        }
        
        // Seleccionar personaje con A
        if (gamepadState.buttons(GLFW_GAMEPAD_BUTTON_A) == 1 && !prevButtonState[GLFW_GAMEPAD_BUTTON_A]) {
            panelJuego.getGame().getSelectorPersonajes().iniciarJuegoConPersonaje();
        }
        
        // Volver al menú principal con B
        if (gamepadState.buttons(GLFW_GAMEPAD_BUTTON_B) == 1 && !prevButtonState[GLFW_GAMEPAD_BUTTON_B]) {
            panelJuego.getGame().setEstadoJuego(EstadoJuego.MENU);
        }
    }

    private void procesarInputsPausa() {
        // Cooldown para la navegación en el menú
        long currentTime = System.currentTimeMillis();
        boolean cooldownElapsed = (currentTime - lastMenuNavigationTime) > MENU_NAVIGATION_COOLDOWN;
        
        // Navegación vertical (stick izquierdo o d-pad)
        float axisY = gamepadState.axes(GLFW_GAMEPAD_AXIS_LEFT_Y);
        boolean dpadUp = gamepadState.buttons(GLFW_GAMEPAD_BUTTON_DPAD_UP) == 1;
        boolean dpadDown = gamepadState.buttons(GLFW_GAMEPAD_BUTTON_DPAD_DOWN) == 1;
        
        if (cooldownElapsed && (axisY < -deadzone || dpadUp) && !prevButtonState[GLFW_GAMEPAD_BUTTON_DPAD_UP]) {
            panelJuego.getGame().getMenuPausa().navegarArriba();
            lastMenuNavigationTime = currentTime; // Actualizar tiempo de último movimiento
            prevButtonState[GLFW_GAMEPAD_BUTTON_DPAD_UP] = true;
        } else if (cooldownElapsed && (axisY > deadzone || dpadDown) && !prevButtonState[GLFW_GAMEPAD_BUTTON_DPAD_DOWN]) {
            panelJuego.getGame().getMenuPausa().navegarAbajo();
            lastMenuNavigationTime = currentTime; // Actualizar tiempo de último movimiento
            prevButtonState[GLFW_GAMEPAD_BUTTON_DPAD_DOWN] = true;
        }
        
        // Resetear estado prev si se suelta el botón
        if (gamepadState.buttons(GLFW_GAMEPAD_BUTTON_DPAD_UP) == 0) {
            prevButtonState[GLFW_GAMEPAD_BUTTON_DPAD_UP] = false;
        }
        if (gamepadState.buttons(GLFW_GAMEPAD_BUTTON_DPAD_DOWN) == 0) {
            prevButtonState[GLFW_GAMEPAD_BUTTON_DPAD_DOWN] = false;
        }
        
        // Seleccionar botón con A
        if (gamepadState.buttons(GLFW_GAMEPAD_BUTTON_A) == 1 && !prevButtonState[GLFW_GAMEPAD_BUTTON_A]) {
            // Solo marcar el botón como presionado visualmente
            panelJuego.getGame().getMenuPausa().presionarBotonSeleccionado();
            prevButtonState[GLFW_GAMEPAD_BUTTON_A] = true;
        } 
        else if (gamepadState.buttons(GLFW_GAMEPAD_BUTTON_A) == 0 && prevButtonState[GLFW_GAMEPAD_BUTTON_A]) {
            // Soltar botón - ahora ejecutar la acción
            panelJuego.getGame().getMenuPausa().ejecutarBotonSeleccionado();
            prevButtonState[GLFW_GAMEPAD_BUTTON_A] = false;
        }
        
        // Salir del menú de pausa con B o Start
        if ((gamepadState.buttons(GLFW_GAMEPAD_BUTTON_B) == 1 && !prevButtonState[GLFW_GAMEPAD_BUTTON_B]) ||
            (gamepadState.buttons(GLFW_GAMEPAD_BUTTON_START) == 1 && !prevButtonState[GLFW_GAMEPAD_BUTTON_START])) {
            panelJuego.getGame().setEstadoJuego(EstadoJuego.PLAYING);
        }
    }

    private void procesarInputsMuerte() {
        // Cooldown para la navegación en el menú
        long currentTime = System.currentTimeMillis();
        boolean cooldownElapsed = (currentTime - lastMenuNavigationTime) > MENU_NAVIGATION_COOLDOWN;
        
        // Navegación vertical (stick izquierdo o d-pad)
        float axisY = gamepadState.axes(GLFW_GAMEPAD_AXIS_LEFT_Y);
        boolean dpadUp = gamepadState.buttons(GLFW_GAMEPAD_BUTTON_DPAD_UP) == 1;
        boolean dpadDown = gamepadState.buttons(GLFW_GAMEPAD_BUTTON_DPAD_DOWN) == 1;
        
        if (cooldownElapsed && (axisY < -deadzone || dpadUp) && !prevButtonState[GLFW_GAMEPAD_BUTTON_DPAD_UP]) {
            panelJuego.getGame().getMenuMuerte().navegarArriba();
            lastMenuNavigationTime = currentTime;
            prevButtonState[GLFW_GAMEPAD_BUTTON_DPAD_UP] = true;
        } else if (cooldownElapsed && (axisY > deadzone || dpadDown) && !prevButtonState[GLFW_GAMEPAD_BUTTON_DPAD_DOWN]) {
            panelJuego.getGame().getMenuMuerte().navegarAbajo();
            lastMenuNavigationTime = currentTime;
            prevButtonState[GLFW_GAMEPAD_BUTTON_DPAD_DOWN] = true;
        }
        
        // Resetear estado prev si se suelta el botón
        if (gamepadState.buttons(GLFW_GAMEPAD_BUTTON_DPAD_UP) == 0) {
            prevButtonState[GLFW_GAMEPAD_BUTTON_DPAD_UP] = false;
        }
        if (gamepadState.buttons(GLFW_GAMEPAD_BUTTON_DPAD_DOWN) == 0) {
            prevButtonState[GLFW_GAMEPAD_BUTTON_DPAD_DOWN] = false;
        }
        
        // Seleccionar botón con A
        if (gamepadState.buttons(GLFW_GAMEPAD_BUTTON_A) == 1 && !prevButtonState[GLFW_GAMEPAD_BUTTON_A]) {
            panelJuego.getGame().getMenuMuerte().presionarBotonSeleccionado();
            prevButtonState[GLFW_GAMEPAD_BUTTON_A] = true;
        } 
        else if (gamepadState.buttons(GLFW_GAMEPAD_BUTTON_A) == 0 && prevButtonState[GLFW_GAMEPAD_BUTTON_A]) {
            panelJuego.getGame().getMenuMuerte().ejecutarBotonSeleccionado();
            prevButtonState[GLFW_GAMEPAD_BUTTON_A] = false;
        }
    }

    public void toggleGamepad() {
        gamepadEnabled = !gamepadEnabled;
        
        if (gamepadEnabled) {
            // Re-detect gamepad only when specifically toggling on
            detectarGamepad();
            if (gamepadID != -1) {
                System.out.println("Gamepad activado");
            } else {
                gamepadEnabled = false; // Couldn't find a gamepad
            }
        } else {
            System.out.println("Gamepad desactivado - Usando teclado y mouse");
            joystickAimActive = false; // Reset aim state
        }
    }
    
    public void shutdown() {
        glfwTerminate();
    }

    public boolean isGamepadEnabled() {
        return gamepadEnabled;
    }
    
    public boolean isJoystickAimActive() {
        return joystickAimActive;
    }
}