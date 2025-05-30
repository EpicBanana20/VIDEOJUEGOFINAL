package Utilz;

import java.awt.Color;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;

import javax.imageio.ImageIO;

public class LoadSave {
    // Atlases de tiles para diferentes niveles
    public static final String LEVEL_ATLAS = "tiles/TILES_1.png";
    public static final String LEVEL_ATLAS2 = "tiles/TILES_2.png";
    public static final String LEVEL_ATLAS3 = "tiles/TILES_3.png";
    
    // Datos de niveles
        public static final String LEVEL_ONE_DATA = "lvlData/LEVEL1.png";
        public static final String LEVEL_TWO_DATA = "lvlData/LEVEL2.png";
        public static final String LEVEL_THREE_DATA = "lvlData/LEVEL3.png";
    // Añade estas constantes para las nuevas rutas
        public static final String LEVEL_ONE_ENTITIES = "lvlData/lvlEntities/LEVEL1_ENTITIES.png";
        public static final String LEVEL_TWO_ENTITIES = "lvlData/lvlEntities/LEVEL2_ENTITIES.png";
        public static final String LEVEL_THREE_ENTITIES = "lvlData/lvlEntities/LEVEL3_ENTITIES.png";

        public static final String LEVEL_ONE_ENVIRONMENT = "lvlData/lvlEnvironment/LEVEL1_ENVIRONMENT.png";
        public static final String LEVEL_TWO_ENVIRONMENT = "lvlData/lvlEnvironment/LEVEL2_ENVIRONMENT.png";
        public static final String LEVEL_THREE_ENVIRONMENT = "lvlData/lvlEnvironment/LEVEL3_ENVIRONMENT.png";
    
    // Fondo de juego
    public static final String PLAYING_BG1_IMG = "BACKGR.png";
    public static final String PLAYING_BG2_IMG = "BACKGR2.png";
    public static final String PLAYING_BG3_IMG = "BACKGR3.png";

    public static final String PLAYER_ATLAS = "personajes/Eclipsa_Sprite.png";
    public static final String PLAYER_ATLAS_HALAN = "personajes/Halan_Sprite.png";
    public static final String PLAYER_ATLAS_VALTHOR = "personajes/Valthor_Sprite.png";

    public static final String PLAYER_ATLAS_ECLIPSA_ENOJADA = "personajes/Eclipsa_Sprite2.png";
    
    public static final String BULLET_MERCURIO = "balas/Bala_Mercurio.png";
    public static final String BULLET_MACHINEGUN = "balas/Bala_Machinegun.png";
    public static final String BULLET_ENEMY = "balas/Bala_Enemigos.png";
    public static final String BULLET_ARANARA = "balas/Bala_Aranara.png";
    public static final String BULLET_HIBIT = "balas/Bala_Hibit.png";
    public static final String BULLET_ELDENE = "balas/Bala_Eldene.png";
    public static final String BULLET_MORDEK = "balas/Bala_Mordek.png";
    public static final String BULLET_PIILIP = "balas/Bala_Piilip.png";
    public static final String BULLET_BOSS1 = "balas/Bala_Boss1.png";
    public static final String BULLET_YIBIRTH = "balas/Bala_Yibirth.png";
    public static final String BULLET_THOR = "balas/Bala_Thor.png";
    public static final String BULLET_ZEFIR = "balas/Bala_Zefir.png";
    public static final String BULLET_ECLIPSE = "balas/Bala_Eclipse.png";
    public static final String BULLET_ION = "balas/Bala_ION.png";
    public static final String BULLET_ESCOPETA = "balas/Bala_Escopeta.png";
    public static final String BULLET_P90 = "balas/Bala_P90.png";
    public static final String BULLET_DELTA = "balas/Bala_Delta.png";
    public static final String BULLET_FIRE = "balas/Bala_Fire.png";
    public static final String BULLET_TERR = "balas/Bala_Terr.png";
    
    
    
    public static final String ARMA_ESCOPETA = "armas/ESCOPETA.png";
    public static final String ARMA_LASER = "armas/LASER.png";
    public static final String ARMA_FRANCOTIRADOR = "armas/FRANCOTIRADOR.png";



    public static final String BARRA_VIDA_SPRITES = "HP.png";
    public static final String BOSS_HEALTH_BAR = "HUDVIDABOSS.png";
    // Información de niveles: maxTileIndex, y lista de tiles sin hitbox
    public static final int[][] LEVEL_INFO = {
        {60, 58, 6, 10}, // Nivel 1: max=60, air=58
        {81, 80, 9, 9}, // Nivel 2: max=70, air=68
        {64, 48, 8, 8}, // Nivel 3: max=80, air=78
    };
    
    // Tiles sin hitbox adicionales para cada nivel (además del tile de aire)
    // Incluye tiles como vegetación, agua poco profunda, decoraciones, etc.
    public static final int[][] TILES_SIN_HITBOX = {
        {}, // Nivel 1: tiles sin hitbox adicionales
        {4,5,6,7,34,45,46,47,48,49,50,51,52,53,54,55,56,57,58,59,60,61,62,63,64,65,66,67,68,69,70,71,72,73,74,75,76,77,78}, // Nivel 2
        {49,50,51,52,53,54,55,56,57,58,59,60,61,62,63}, // Nivel 3
    };
    
    public static final int[][] TILES_PLATAFORMAS = {
        {6,7,8},     // Nivel 1: tiles 15, 16, 17 son plataformas atravesables
        {42,41}, // Nivel 2: tiles 36-39 son plataformas atravesables
        {24,25,26,27,44}      // Nivel 3: tiles 42-44 son plataformas atravesables
    };

    // HashMap para almacenar las imágenes ya cargadas
    private static final Map<String, BufferedImage> imageCache = new HashMap<>();
    public static final String[] LEVEL_BACKGROUND_FILES = null;
    
    public static BufferedImage GetSpriteAtlas(String name) {
        BufferedImage img = imageCache.get(name);

        if (img == null) {
            InputStream is = LoadSave.class.getResourceAsStream("/recursos/" + name);
            try {
                img = ImageIO.read(is);
                imageCache.put(name, img);
            } catch (IOException e) {
                e.printStackTrace();
            } finally {
                try {
                    if (is != null) {
                        is.close();
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
        
        return img;
    }
    
    // Método para limpiar la caché si es necesario (por ejemplo al cambiar de nivel)
    public static void clearCache() {
        imageCache.clear();
        System.out.println("Caché de imágenes limpiada");
    }
    
    // Método para obtener el tamaño actual de la caché
    public static int getCacheSize() {
        return imageCache.size();
    }
    
    public static int[][] GetLevelData(String levelFile, int nivelIndex) {
        BufferedImage img = LoadSave.GetSpriteAtlas(levelFile);
        int[][] lvlData = new int[img.getHeight()][img.getWidth()];
        
        int maxTileIndex = LEVEL_INFO[nivelIndex][0];
        int airTileIndex = LEVEL_INFO[nivelIndex][1];
        
        for (int j = 0; j < img.getHeight(); j++) {
            for (int i = 0; i < img.getWidth(); i++) {
                Color color = new Color(img.getRGB(i, j));
                int valor = color.getRed();
                if (valor >= maxTileIndex)
                    valor = airTileIndex;
                    
                lvlData[j][i] = valor;
            }
        }
        return lvlData;
    }
    
    // Mantener el método original para compatibilidad
    public static int[][] GetLevelData() {
        return GetLevelData(LEVEL_ONE_DATA, 0);
    }
}