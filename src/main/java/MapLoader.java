import javax.imageio.ImageIO;
import javax.swing.*;
import java.awt.*;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class MapLoader extends JPanel implements Runnable {

    // --- KONFIGURASI MAP (Target Jumlah Tile yang Terlihat) ---
    private static final int TARGET_VISIBLE_COLUMNS = 20; // Berapa kolom tile yang ingin selalu terlihat
    private static final int TARGET_VISIBLE_ROWS = 15;   // Berapa baris tile yang ingin selalu terlihat
    private double currentTileSizeW; // Lebar tile saat ini, dihitung dinamis
    private double currentTileSizeH; // Tinggi tile saat ini, dihitung dinamis

    // --- KONFIGURASI PLAYER ---
    private int originalPlayerSpriteWidth = 11;  // Lebar asli sprite player
    private int originalPlayerSpriteHeight = 18; // Tinggi asli sprite player
    private double playerAspectRatio;             // Rasio aspek player (lebar/tinggi)
    private int displayPlayerWidth;               // Lebar player untuk digambar (dihitung)
    private int displayPlayerHeight;              // Tinggi player untuk digambar (dihitung)
    // Skala player relatif terhadap tinggi tile (misal, player setinggi 80% dari satu tile)
    private static final double PLAYER_SCALE_RELATIVE_TO_TILE_HEIGHT = 0.8;
    private double playerSpeed; // Kecepatan player (dihitung relatif terhadap ukuran tile)
    // Faktor kecepatan player (misal, 1/8 dari ukuran tile per update)
    private static final double PLAYER_SPEED_FACTOR_PER_TILE = 0.125;


    private GameWindow gameWindow;
    private BufferedImage grassTile;

    // --- Variabel Animasi Player ---
    private BufferedImage[] playerUpFrames = new BufferedImage[2];
    private BufferedImage[] playerDownFrames = new BufferedImage[2];
    private BufferedImage[] playerLeftFrames = new BufferedImage[2];
    private BufferedImage[] playerRightFrames = new BufferedImage[2];
    private BufferedImage currentPlayerSprite;
    private int animationFrameIndex = 0;
    private int animationCounter = 0;
    private static final int ANIMATION_SPEED = 5; // Jumlah update sebelum ganti frame animasi

    // --- Variabel Posisi & Arah Player (Koordinat Logis) ---
    // Posisi player sekarang dalam unit "tile" atau "dunia game", bukan piksel mentah
    private double playerLogicalX, playerLogicalY;
    // Posisi player dalam piksel untuk rendering, dihitung dari logicalX/Y dan currentTileSize
    private int playerRenderX, playerRenderY;
    private Direction playerDirection = Direction.DOWN;

    private boolean upPressed, downPressed, leftPressed, rightPressed;

    // --- KONFIGURASI & VARIABEL NPC ---
    private List<PokemonNPC> npcs;
    private BufferedImage pikachuSprite, charizardSprite, greninjaSprite;
    private static final int NUM_PIKACHUS = 1;
    private static final int NUM_CHARIZARDS = 1;
    private static final int NUM_GRENINJAS = 2;
    // Skala NPC relatif terhadap tinggi tile (misal, NPC setinggi 70% dari satu tile)
    private static final double NPC_SCALE_RELATIVE_TO_TILE_HEIGHT = 0.7;
    private Random randomGenerator;

    private boolean isDialogActive = false;
    private PokemonNPC currentlyInteractingNPC = null;

    private Thread gameThread;
    private final int FPS = 30; // Target FPS

    // Enum untuk arah, letakkan di sini atau sebagai kelas terpisah
    enum Direction {UP, DOWN, LEFT, RIGHT}

    private static class PokemonNPC {
        double logicalX, logicalY; // Posisi logis NPC (misal, dalam satuan tile)
        int renderX, renderY;     // Posisi render NPC dalam piksel
        BufferedImage sprite;
        String type;
        int originalWidth, originalHeight;
        double aspectRatio;
        int displayWidth, displayHeight; // Ukuran render NPC dalam piksel

        public PokemonNPC(String type, BufferedImage sprite, double logicalX, double logicalY) {
            this.type = type;
            this.sprite = sprite;
            this.logicalX = logicalX;
            this.logicalY = logicalY;

            if (sprite != null) {
                this.originalWidth = sprite.getWidth();
                this.originalHeight = sprite.getHeight();
                this.aspectRatio = (double) this.originalWidth / this.originalHeight;
            } else {
                // Fallback jika sprite null
                this.originalWidth = 1; // Hindari pembagian dengan nol
                this.originalHeight = 1;
                this.aspectRatio = 1.0;
                System.err.println("Sprite untuk NPC " + type + " adalah null saat pembuatan objek.");
            }
            // displayWidth dan displayHeight akan dihitung di updateNPCSizesAndPositions
        }

        public void updateSizeAndPosition(double tileW, double tileH, double scaleRelativeToTileHeight) {
            // Hitung ukuran display NPC
            this.displayHeight = (int) (tileH * scaleRelativeToTileHeight);
            this.displayWidth = (int) (this.displayHeight * this.aspectRatio);
            if (this.displayWidth < 1) this.displayWidth = 1;
            if (this.displayHeight < 1) this.displayHeight = 1;

            // Hitung posisi render NPC
            this.renderX = (int) (this.logicalX * tileW);
            this.renderY = (int) (this.logicalY * tileH);
        }
    }

    public MapLoader(GameWindow gameWindow) {
        this.gameWindow = gameWindow;
        // setPreferredSize(...); // HAPUS atau jangan gunakan ukuran tetap
        setBackground(Color.BLACK);
        setDoubleBuffered(true);
        setFocusable(true);

        npcs = new ArrayList<>();
        randomGenerator = new Random();

        loadResources(); // Muat resource dulu untuk mendapatkan dimensi asli sprite

        // Tambahkan ComponentListener untuk menangani perubahan ukuran panel
        addComponentListener(new ComponentAdapter() {
            @Override
            public void componentResized(ComponentEvent e) {
                System.out.println("Panel resized to: " + getWidth() + "x" + getHeight());
                updateGameLayoutAndElements();
            }
            @Override
            public void componentShown(ComponentEvent e) {
                // Panggil juga saat pertama kali ditampilkan untuk inisialisasi ukuran
                System.out.println("Panel shown: " + getWidth() + "x" + getHeight());
                if (getWidth() > 0 && getHeight() > 0) { // Pastikan sudah ada ukuran
                    updateGameLayoutAndElements();
                }
            }
        });

        // Inisialisasi player dan NPC akan dipanggil dalam updateGameLayoutAndElements
        // setelah ukuran tile pertama kali dihitung.

        addKeyListener(new PlayerKeyAdapter());
    }

    private BufferedImage loadImage(String path) throws IOException {
        InputStream stream = getClass().getResourceAsStream(path);
        if (stream == null) {
            System.err.println("Peringatan: File tidak ditemukan: " + path + ". Sprite akan null.");
            return null;
        }
        return ImageIO.read(stream);
    }

    private void loadResources() {
        try {
            grassTile = loadImage("/Assets/MapItem/grass_tile.png"); // Pastikan path diawali '/' jika di root classpath
            if (grassTile == null) throw new IOException("grass_tile.png gagal dimuat!");

            playerDownFrames[0] = loadImage("/Assets/MapItem/player_down_1.png");
            playerDownFrames[1] = loadImage("/Assets/MapItem/player_down_2.png");
            playerUpFrames[0] = loadImage("/Assets/MapItem/player_up_1.png");
            playerUpFrames[1] = loadImage("/Assets/MapItem/player_up_2.png");
            playerLeftFrames[0] = loadImage("/Assets/MapItem/player_left_1.png");
            playerLeftFrames[1] = loadImage("/Assets/MapItem/player_left_2.png");
            playerRightFrames[0] = loadImage("/Assets/MapItem/player_right_1.png");
            playerRightFrames[1] = loadImage("/Assets/MapItem/player_right_2.png");

            if (playerDownFrames[0] != null) {
                originalPlayerSpriteWidth = playerDownFrames[0].getWidth();
                originalPlayerSpriteHeight = playerDownFrames[0].getHeight();
                playerAspectRatio = (double) originalPlayerSpriteWidth / originalPlayerSpriteHeight;
            } else {
                System.err.println("player_down_1.png gagal dimuat, menggunakan ukuran asli default dan rasio 1:1.");
                playerAspectRatio = 1.0; // Fallback
            }
            currentPlayerSprite = playerDownFrames[0] != null ? playerDownFrames[0] : new BufferedImage(1,1, BufferedImage.TYPE_INT_ARGB); // Fallback

            pikachuSprite = loadImage("/Assets/MapItem/pikachu.png");
            charizardSprite = loadImage("/Assets/MapItem/charizard.png"); // Ganti dengan file yang benar
            greninjaSprite = loadImage("/Assets/MapItem/greninja.png");   // Ganti dengan file yang benar

        } catch (IOException e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(this, "Gagal memuat resource game: " + e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
            System.exit(1);
        }
    }

    /**
     * Metode sentral untuk menghitung ulang ukuran tile, posisi player, NPC, dll.,
     * setiap kali ukuran panel berubah.
     */
    private void updateGameLayoutAndElements() {
        int panelWidth = getWidth();
        int panelHeight = getHeight();

        if (panelWidth <= 0 || panelHeight <= 0) {
            return; // Jangan lakukan apa-apa jika panel belum punya ukuran
        }

        // 1. Hitung ukuran tile baru
        currentTileSizeW = (double) panelWidth / TARGET_VISIBLE_COLUMNS;
        currentTileSizeH = (double) panelHeight / TARGET_VISIBLE_ROWS;
        // Untuk tile persegi, Anda mungkin ingin memilih yang terkecil:
        // currentTileSize = Math.min(currentTileSizeW, currentTileSizeH);
        // currentTileSizeW = currentTileSize;
        // currentTileSizeH = currentTileSize;


        // 2. Update ukuran dan kecepatan Player
        // Skalakan tinggi player relatif terhadap tinggi tile
        displayPlayerHeight = (int) (currentTileSizeH * PLAYER_SCALE_RELATIVE_TO_TILE_HEIGHT);
        // Hitung lebar player berdasarkan rasio aspeknya
        displayPlayerWidth = (int) (displayPlayerHeight * playerAspectRatio);
        if (displayPlayerWidth < 1) displayPlayerWidth = 1;
        if (displayPlayerHeight < 1) displayPlayerHeight = 1;

        // Kecepatan player diskalakan dengan lebar tile (atau tinggi, atau rata-rata)
        playerSpeed = currentTileSizeW * PLAYER_SPEED_FACTOR_PER_TILE;


        // 3. Inisialisasi atau update posisi logis Player (jika belum) dan posisi render
        // Jika playerLogicalX/Y belum diinisialisasi (misalnya, masih 0 dan ini pemanggilan pertama)
        boolean isFirstLayout = (playerLogicalX == 0 && playerLogicalY == 0 && npcs.isEmpty());
        if (isFirstLayout) {
            initPlayerLogicalPosition();
            initNPCsLogicalPositions(); // Panggil setelah tile size diketahui
        }
        updatePlayerRenderPosition();


        // 4. Update ukuran dan posisi NPC
        for (PokemonNPC npc : npcs) {
            npc.updateSizeAndPosition(currentTileSizeW, currentTileSizeH, NPC_SCALE_RELATIVE_TO_TILE_HEIGHT);
        }

        System.out.println("Layout Updated. TileW: " + String.format("%.2f",currentTileSizeW) +
                ", TileH: " + String.format("%.2f", currentTileSizeH) +
                ", Player Display: " + displayPlayerWidth + "x" + displayPlayerHeight +
                ", Player Speed: " + String.format("%.2f",playerSpeed));
        repaint(); // Gambar ulang panel dengan ukuran dan posisi baru
    }


    private void initPlayerLogicalPosition() {
        // Tempatkan player di tengah-tengah area tile yang terlihat (secara logis)
        playerLogicalX = TARGET_VISIBLE_COLUMNS / 2.0;
        playerLogicalY = TARGET_VISIBLE_ROWS / 2.0;

        playerDirection = Direction.DOWN;
        animationFrameIndex = 0;
        if (playerDownFrames[0] != null) {
            currentPlayerSprite = playerDownFrames[animationFrameIndex];
        }

        upPressed = false;
        downPressed = false;
        leftPressed = false;
        rightPressed = false;
        // updatePlayerRenderPosition() akan dipanggil setelah ini di updateGameLayoutAndElements()
    }

    private void updatePlayerRenderPosition() {
        // Konversi posisi logis player ke posisi render (piksel)
        // Posisi logis dianggap sebagai "pusat" tile atau unit, jadi kita sesuaikan agar sprite tergambar dengan benar
        playerRenderX = (int) (playerLogicalX * currentTileSizeW - displayPlayerWidth / 2.0);
        playerRenderY = (int) (playerLogicalY * currentTileSizeH - displayPlayerHeight / 2.0);
    }


    private void initNPCsLogicalPositions() {
        npcs.clear();
        // Tempatkan NPC pada posisi logis acak di dalam area tile yang terlihat
        spawnSpecificNPCsLogically("Pikachu", pikachuSprite, NUM_PIKACHUS);
        spawnSpecificNPCsLogically("Charizard", charizardSprite, NUM_CHARIZARDS);
        spawnSpecificNPCsLogically("Greninja", greninjaSprite, NUM_GRENINJAS);

        System.out.println("Inisialisasi " + npcs.size() + " NPC (posisi logis).");
        // Ukuran dan posisi render NPC akan diupdate di updateGameLayoutAndElements()
    }

    private void spawnSpecificNPCsLogically(String type, BufferedImage sprite, int count) {
        if (sprite == null) {
            System.err.println("Sprite untuk " + type + " belum dimuat. Melewati spawn.");
            return;
        }
        for (int i = 0; i < count; i++) {
            // Posisi logis acak (antara 0 dan TARGET_VISIBLE_COLUMNS/ROWS)
            double npcLogicalX = randomGenerator.nextDouble() * TARGET_VISIBLE_COLUMNS;
            double npcLogicalY = randomGenerator.nextDouble() * TARGET_VISIBLE_ROWS;
            npcs.add(new PokemonNPC(type, sprite, npcLogicalX, npcLogicalY));
        }
    }


    public void startGameThread() {
        gameThread = new Thread(this);
        gameThread.start();
    }

    @Override
    public void run() {
        double drawInterval = 1000000000.0 / FPS;
        double delta = 0;
        long lastTime = System.nanoTime();
        long currentTime;

        while (gameThread != null) {
            currentTime = System.nanoTime();
            delta += (currentTime - lastTime) / drawInterval;
            lastTime = currentTime;

            if (delta >= 1) {
                updateGameLogic();
                repaint();
                delta--;
            }
        }
    }

    private void updateGameLogic() { // Mengganti nama dari update() agar lebih jelas
        if (isDialogActive) {
            setPlayerToStandingFrame();
            return;
        }

        boolean moved = false;
        double prevLogicalX = playerLogicalX;
        double prevLogicalY = playerLogicalY;

        // Kecepatan sekarang dalam unit logis per update (disesuaikan dari playerSpeed piksel)
        double logicalSpeedX = playerSpeed / currentTileSizeW;
        double logicalSpeedY = playerSpeed / currentTileSizeH;


        if (upPressed) {
            playerLogicalY -= logicalSpeedY;
            playerDirection = Direction.UP;
            moved = true;
        } else if (downPressed) {
            playerLogicalY += logicalSpeedY;
            playerDirection = Direction.DOWN;
            moved = true;
        } else if (leftPressed) {
            playerLogicalX -= logicalSpeedX;
            playerDirection = Direction.LEFT;
            moved = true;
        } else if (rightPressed) {
            playerLogicalX += logicalSpeedX;
            playerDirection = Direction.RIGHT;
            moved = true;
        }

        // Batasan pergerakan player dalam area logis (TARGET_VISIBLE_COLUMNS/ROWS)
        // Perhatikan bahwa playerLogicalX/Y adalah pusat player, jadi kita perlu setengah lebar/tinggi logisnya
        double playerLogicalHalfWidth = (displayPlayerWidth / 2.0) / currentTileSizeW;
        double playerLogicalHalfHeight = (displayPlayerHeight / 2.0) / currentTileSizeH;

        playerLogicalX = Math.max(playerLogicalHalfWidth, Math.min(playerLogicalX, TARGET_VISIBLE_COLUMNS - playerLogicalHalfWidth));
        playerLogicalY = Math.max(playerLogicalHalfHeight, Math.min(playerLogicalY, TARGET_VISIBLE_ROWS - playerLogicalHalfHeight));

        // Update posisi render player setelah posisi logisnya berubah
        updatePlayerRenderPosition();


        if (moved) {
            animationCounter++;
            if (animationCounter >= ANIMATION_SPEED) {
                animationCounter = 0;
                animationFrameIndex = 1 - animationFrameIndex; // Toggle antara 0 dan 1
            }
        } else {
            animationFrameIndex = 0; // Kembali ke frame berdiri jika tidak bergerak
            animationCounter = 0;
        }
        updateCurrentPlayerSprite();

        // Cek Interaksi dengan NPC menggunakan koordinat render
        Rectangle playerBounds = new Rectangle(playerRenderX, playerRenderY, displayPlayerWidth, displayPlayerHeight);
        for (PokemonNPC npc : npcs) {
            Rectangle npcBounds = new Rectangle(npc.renderX, npc.renderY, npc.displayWidth, npc.displayHeight);
            if (playerBounds.intersects(npcBounds)) {
                // Periksa apakah sudah berinteraksi dengan NPC ini di frame sebelumnya untuk menghindari dialog berulang
                if (currentlyInteractingNPC != npc || !isDialogActive) {
                    isDialogActive = true;
                    currentlyInteractingNPC = npc;
                    showEncounterDialog(npc);
                    return; // Keluar dari update setelah dialog muncul
                }
            }
        }
    }

    private void showEncounterDialog(PokemonNPC npc) {
        SwingUtilities.invokeLater(() -> {
            String[] options = {"Lawan", "Menghindar"};
            String message = "Kamu bertemu dengan " + npc.type + "!\nApa yang akan kamu lakukan?";
            // ... (sisa logika dialog tetap sama) ...
            int choice = JOptionPane.showOptionDialog(
                    MapLoader.this, message, "Pertemuan Pokémon!",
                    JOptionPane.YES_NO_OPTION, JOptionPane.QUESTION_MESSAGE,
                    null, options, options[0]);
            handleDialogChoice(choice, npc);
        });
    }

    private void handleDialogChoice(int choice, PokemonNPC encounteredNpc) {
        if (choice == JOptionPane.YES_OPTION) {
            System.out.println("Kamu memilih LAWAN " + encounteredNpc.type + "!");
            // ... logika pertarungan ...
            // Contoh: keluar ke showcase screen
            if (gameWindow != null && gameWindow.isDisplayable()) { // Pastikan gameWindow masih valid
                gameThread = null; // Hentikan game loop MapLoader
                ShowcaseScreen showcaseScreen = new ShowcaseScreen(gameWindow);
                gameWindow.switchPanel(showcaseScreen);
            } else {
                System.err.println("GameWindow tidak valid untuk switch panel.");
            }


        } else if (choice == JOptionPane.NO_OPTION) {
            System.out.println("Kamu memilih MENGHINDAR dari " + encounteredNpc.type + "!");
            // Logika menghindar: mundurkan player sedikit secara logis
            double pushBackLogicalDistance = 0.5; // Mundur setengah tile
            switch (playerDirection) { // Mundur berlawanan arah terakhir player
                case UP: playerLogicalY += pushBackLogicalDistance; break;
                case DOWN: playerLogicalY -= pushBackLogicalDistance; break;
                case LEFT: playerLogicalX += pushBackLogicalDistance; break;
                case RIGHT: playerLogicalX -= pushBackLogicalDistance; break;
            }
            // Pastikan player tetap dalam batas setelah mundur
            double playerLogicalHalfWidth = (displayPlayerWidth / 2.0) / currentTileSizeW;
            double playerLogicalHalfHeight = (displayPlayerHeight / 2.0) / currentTileSizeH;
            playerLogicalX = Math.max(playerLogicalHalfWidth, Math.min(playerLogicalX, TARGET_VISIBLE_COLUMNS - playerLogicalHalfWidth));
            playerLogicalY = Math.max(playerLogicalHalfHeight, Math.min(playerLogicalY, TARGET_VISIBLE_ROWS - playerLogicalHalfHeight));
            updatePlayerRenderPosition(); // Update posisi render setelah menghindar

        } else { // Jika dialog ditutup
            System.out.println("Pertemuan dengan " + encounteredNpc.type + " dibatalkan.");
        }

        currentlyInteractingNPC = null;
        isDialogActive = false;
        MapLoader.this.requestFocusInWindow();
    }


    private void setPlayerToStandingFrame() {
        if (playerDownFrames[0] == null && playerUpFrames[0] == null && playerLeftFrames[0] == null && playerRightFrames[0] == null) return;
        animationFrameIndex = 0;
        updateCurrentPlayerSprite();
    }

    private void updateCurrentPlayerSprite() {
        BufferedImage[] currentFrames = null;
        switch (playerDirection) {
            case UP: currentFrames = playerUpFrames; break;
            case DOWN: currentFrames = playerDownFrames; break;
            case LEFT: currentFrames = playerLeftFrames; break;
            case RIGHT: currentFrames = playerRightFrames; break;
            default: currentFrames = playerDownFrames; // Fallback
        }

        if (currentFrames != null && currentFrames[animationFrameIndex] != null) {
            currentPlayerSprite = currentFrames[animationFrameIndex];
        } else if (currentFrames != null && currentFrames[0] != null) {
            // Fallback ke frame pertama jika frame animasi saat ini null
            currentPlayerSprite = currentFrames[0];
        } else if (playerDownFrames[0] != null) {
            // Fallback umum jika semuanya gagal
            currentPlayerSprite = playerDownFrames[0];
        }
        // Jika currentPlayerSprite masih bisa null, tambahkan fallback ke gambar placeholder
        if (currentPlayerSprite == null && playerDownFrames[0] != null) {
            currentPlayerSprite = playerDownFrames[0]; // Fallback ke sprite default jika ada
        } else if (currentPlayerSprite == null) {
            // Jika semua sprite gagal dimuat, buat gambar placeholder agar tidak error
            // Ini seharusnya tidak terjadi jika loadResources berhasil
            if (displayPlayerWidth > 0 && displayPlayerHeight > 0) {
                currentPlayerSprite = new BufferedImage(displayPlayerWidth, displayPlayerHeight, BufferedImage.TYPE_INT_ARGB);
                Graphics g = currentPlayerSprite.getGraphics();
                g.setColor(Color.MAGENTA); // Warna placeholder
                g.fillRect(0, 0, displayPlayerWidth, displayPlayerHeight);
                g.dispose();
            } else {
                // Darurat terakhir jika ukuran display juga 0
                currentPlayerSprite = new BufferedImage(1,1, BufferedImage.TYPE_INT_ARGB);
            }
        }
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        Graphics2D g2d = (Graphics2D) g;

        // Aktifkan anti-aliasing dan interpolasi untuk scaling yang lebih halus
        g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g2d.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BILINEAR);

        // 1. Gambar Map Tiles
        if (grassTile != null && currentTileSizeW > 0 && currentTileSizeH > 0) {
            // Kita gambar sejumlah TARGET_VISIBLE_COLUMNS/ROWS tiles,
            // dengan ukuran masing-masing currentTileSizeW/H
            for (int row = 0; row < TARGET_VISIBLE_ROWS; row++) {
                for (int col = 0; col < TARGET_VISIBLE_COLUMNS; col++) {
                    // Gunakan Math.ceil untuk memastikan tidak ada celah antar tile karena pembulatan double ke int
                    g2d.drawImage(grassTile,
                            (int) (col * currentTileSizeW),
                            (int) (row * currentTileSizeH),
                            (int) Math.ceil(currentTileSizeW),
                            (int) Math.ceil(currentTileSizeH),
                            null);
                }
            }
        }

        // 2. Gambar NPCs
        if (npcs != null) {
            for (PokemonNPC npc : npcs) {
                if (npc.sprite != null) {
                    g2d.drawImage(npc.sprite, npc.renderX, npc.renderY, npc.displayWidth, npc.displayHeight, null);
                }
            }
        }

        // 3. Gambar Player
        if (currentPlayerSprite != null && displayPlayerWidth > 0 && displayPlayerHeight > 0) {
            // Pastikan playerRenderX dan playerRenderY sudah diupdate
            if (playerRenderX == 0 && playerRenderY == 0 && (playerLogicalX != 0 || playerLogicalY != 0)) {
                // Jika render pos belum diset tapi logical sudah, coba update sekali lagi
                // Ini bisa terjadi jika paintComponent dipanggil sebelum componentResized/Shown selesai
                updatePlayerRenderPosition();
            }
            g2d.drawImage(currentPlayerSprite, playerRenderX, playerRenderY, displayPlayerWidth, displayPlayerHeight, null);
        } else if (currentPlayerSprite == null) {
            System.err.println("currentPlayerSprite is null in paintComponent.");
        }


        g2d.dispose(); // Sebaiknya panggil dispose jika Anda membuat objek Graphics2D sendiri,
        // tapi untuk g yang didapat dari parameter, Swing yang akan mengurusnya.
        // Namun, tidak ada salahnya memanggilnya.
    }


    private class PlayerKeyAdapter extends KeyAdapter {
        @Override
        public void keyPressed(KeyEvent e) {
            int key = e.getKeyCode();
            if (isDialogActive) return; // Jangan proses input jika dialog aktif

            switch (key) {
                case KeyEvent.VK_W: case KeyEvent.VK_UP: upPressed = true; break;
                case KeyEvent.VK_S: case KeyEvent.VK_DOWN: downPressed = true; break;
                case KeyEvent.VK_A: case KeyEvent.VK_LEFT: leftPressed = true; break;
                case KeyEvent.VK_D: case KeyEvent.VK_RIGHT: rightPressed = true; break;
            }
        }

        @Override
        public void keyReleased(KeyEvent e) {
            int key = e.getKeyCode();
            switch (key) {
                case KeyEvent.VK_W: case KeyEvent.VK_UP: upPressed = false; break;
                case KeyEvent.VK_S: case KeyEvent.VK_DOWN: downPressed = false; break;
                case KeyEvent.VK_A: case KeyEvent.VK_LEFT: leftPressed = false; break;
                case KeyEvent.VK_D: case KeyEvent.VK_RIGHT: rightPressed = false; break;
            }
        }
    }
}