// Pastikan package ini sesuai dengan struktur foldermuimport javax.imageio.ImageIO;
import javax.swing.*;
import java.awt.*;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList; // Import ArrayList
import java.util.List;    // Import List
import java.util.Random;   // Import Random
import javax.imageio.ImageIO;

public class MapLoader extends JPanel implements Runnable {

    // --- KONFIGURASI MAP ---
    private static final int TILE_SIZE = 32;
    private static final int MAP_COLUMNS = 20;
    private static final int MAP_ROWS = 15;

    // --- KONFIGURASI PLAYER ---
    private int originalPlayerSpriteWidth = 11;
    private int originalPlayerSpriteHeight = 18;
    private int displayPlayerWidth;
    private int displayPlayerHeight;
    private static final double PLAYER_SCALE_FACTOR = 0.3;
    private static final int PLAYER_SPEED = 4;

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
    private static final int ANIMATION_SPEED = 5;

    // --- Variabel Posisi & Arah Player ---
    private int playerX, playerY;
    private Direction playerDirection = Direction.DOWN;

    // --- Variabel Status Input ---
    private boolean upPressed, downPressed, leftPressed, rightPressed;

    // --- KONFIGURASI & VARIABEL NPC ---
    private List<PokemonNPC> npcs;
    private BufferedImage pikachuSprite, charizardSprite, greninjaSprite; // Sprite dasar NPC
    private static final int NUM_PIKACHUS = 1;    // Jumlah Pikachu yang akan muncul
    private static final int NUM_CHARIZARDS = 1;  // Jumlah Charizard
    private static final int NUM_GRENINJAS = 2;   // Jumlah Greninja
    private static final double NPC_SCALE_FACTOR = 0.3; // Skala untuk NPC (misal, 0.5x ukuran asli)
    // Pikachu 64x64 -> 32x32 dengan skala 0.5
    private Random randomGenerator;


    // --- Variabel Status Interaksi NPC ---
    private boolean isDialogActive = false;
    private PokemonNPC currentlyInteractingNPC = null; // Menyimpan NPC yang sedang diajak interaksi


    private Thread gameThread;
    private final int FPS = 30;

    // Inner class untuk NPC (letakkan di sini atau di akhir file GamePanel)
    private static class PokemonNPC {
        // ... (definisi kelas PokemonNPC seperti di atas) ...
        int x, y;
        BufferedImage sprite;
        String type;
        int originalWidth, originalHeight;
        int displayWidth, displayHeight;

        public PokemonNPC(String type, BufferedImage sprite, int x, int y, double scaleFactor) {
            this.type = type;
            this.sprite = sprite;
            this.x = x;
            this.y = y;

            if (sprite != null) {
                this.originalWidth = sprite.getWidth();
                this.originalHeight = sprite.getHeight();
                this.displayWidth = (int) (this.originalWidth * scaleFactor);
                this.displayHeight = (int) (this.originalHeight * scaleFactor);

                if (this.displayWidth < 1) this.displayWidth = 1;
                if (this.displayHeight < 1) this.displayHeight = 1;
            } else {
                this.originalWidth = 32;
                this.originalHeight = 32;
                this.displayWidth = (int) (this.originalWidth * scaleFactor);
                this.displayHeight = (int) (this.originalHeight * scaleFactor);
                System.err.println("Sprite untuk NPC " + type + " adalah null saat pembuatan objek.");
            }
        }
    }


    public MapLoader(GameWindow gameWindow) {
        this.gameWindow = gameWindow;
        setPreferredSize(new Dimension(MAP_COLUMNS * TILE_SIZE, MAP_ROWS * TILE_SIZE));
        setBackground(Color.BLACK);
        setDoubleBuffered(true);
        setFocusable(true);

        npcs = new ArrayList<>(); // Inisialisasi list NPC
        randomGenerator = new Random(); // Inisialisasi generator angka acak

        loadResources(); // Memuat sprite player DAN NPC
        initPlayer();    // Inisialisasi player
        initNPCs();      // Inisialisasi NPC setelah resource dimuat

        addKeyListener(new PlayerKeyAdapter());
    }

    private BufferedImage loadImage(String path) throws IOException {
        InputStream stream = getClass().getResourceAsStream(path);
        if (stream == null) {
            // Jangan throw exception di sini jika beberapa NPC opsional, cukup return null dan tangani
            System.err.println("Peringatan: File tidak ditemukan: " + path + ". Sprite akan null.");
            return null;
        }
        return ImageIO.read(stream);
    }

    private void loadResources() {
        try {
            grassTile = loadImage("Assets/MapItem/grass_tile.png");
            if (grassTile == null) throw new IOException("grass_tile.png gagal dimuat!"); // Map tile harus ada

            // Player sprites
            playerDownFrames[0] = loadImage("Assets/MapItem/player_down_1.png");
            playerDownFrames[1] = loadImage("Assets/MapItem/player_down_2.png");
            // ... (muat semua frame player lainnya) ...
            playerUpFrames[0] = loadImage("Assets/MapItem/player_up_1.png");
            playerUpFrames[1] = loadImage("Assets/MapItem/player_up_2.png");
            playerLeftFrames[0] = loadImage("Assets/MapItem/player_left_1.png");
            playerLeftFrames[1] = loadImage("Assets/MapItem/player_left_2.png");
            playerRightFrames[0] = loadImage("Assets/MapItem/player_right_1.png");
            playerRightFrames[1] = loadImage("Assets/MapItem/player_right_2.png");


            if (playerDownFrames[0] != null) {
                originalPlayerSpriteWidth = playerDownFrames[0].getWidth();
                originalPlayerSpriteHeight = playerDownFrames[0].getHeight();
            } else {
                System.err.println("player_down_1.png (player) gagal dimuat, menggunakan ukuran asli default.");
            }
            displayPlayerWidth = (int) (originalPlayerSpriteWidth * PLAYER_SCALE_FACTOR);
            displayPlayerHeight = (int) (originalPlayerSpriteHeight * PLAYER_SCALE_FACTOR);
            if (displayPlayerWidth < 1) displayPlayerWidth = 1;
            if (displayPlayerHeight < 1) displayPlayerHeight = 1;
            currentPlayerSprite = playerDownFrames[0]; // Asumsi player_down_1.png ada

            // NPC Sprites
            // TODO: Ganti "pikachu.png" dengan nama file gambar Pikachu-mu (misal, dari image_ceb734.png)
            pikachuSprite = loadImage("Assets/MapItem/pikachu.png");
            // TODO: Kamu perlu menyediakan file charizard.png dan greninja.png
            charizardSprite = loadImage("Assets/MapItem/pikachu.png");
            greninjaSprite = loadImage("Assets/MapItem/pikachu.png");

        } catch (IOException e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(this, "Gagal memuat resource game: " + e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
            System.exit(1); // Keluar jika resource penting gagal dimuat
        }
    }

    private void initNPCs() {
        npcs.clear(); // Bersihkan NPC lama jika ada (berguna jika ingin reset)
        int panelWidth = MAP_COLUMNS * TILE_SIZE;
        int panelHeight = MAP_ROWS * TILE_SIZE;

        // Spawn Pikachus
        spawnSpecificNPCs("Pikachu", pikachuSprite, NUM_PIKACHUS, panelWidth, panelHeight);
        // Spawn Charizards
        spawnSpecificNPCs("Charizard", charizardSprite, NUM_CHARIZARDS, panelWidth, panelHeight);
        // Spawn Greninjas
        spawnSpecificNPCs("Greninja", greninjaSprite, NUM_GRENINJAS, panelWidth, panelHeight);

        System.out.println("Inisialisasi " + npcs.size() + " NPC.");
    }

    private void spawnSpecificNPCs(String type, BufferedImage sprite, int count, int panelWidth, int panelHeight) {
        if (sprite == null) {
            System.err.println("Sprite untuk " + type + " belum dimuat. Melewati spawn untuk tipe ini.");
            return;
        }

        for (int i = 0; i < count; i++) {
            int npcDisplayWidth = (int) (sprite.getWidth() * NPC_SCALE_FACTOR);
            int npcDisplayHeight = (int) (sprite.getHeight() * NPC_SCALE_FACTOR);
            if (npcDisplayWidth < 1) npcDisplayWidth = 1;
            if (npcDisplayHeight < 1) npcDisplayHeight = 1;

            if (panelWidth <= npcDisplayWidth || panelHeight <= npcDisplayHeight) {
                System.err.println("Panel terlalu kecil untuk menempatkan " + type + " atau sprite terlalu besar.");
                continue;
            }

            int npcX = randomGenerator.nextInt(panelWidth - npcDisplayWidth);
            int npcY = randomGenerator.nextInt(panelHeight - npcDisplayHeight);
            npcs.add(new PokemonNPC(type, sprite, npcX, npcY, NPC_SCALE_FACTOR));
        }
    }


    private void initPlayer() {
        playerX = (MAP_COLUMNS * TILE_SIZE) / 2 - displayPlayerWidth / 2;
        playerY = (MAP_ROWS * TILE_SIZE) / 2 - displayPlayerHeight / 2;
        playerDirection = Direction.DOWN;
        animationFrameIndex = 0;
        if (playerDownFrames[0] != null) {
            currentPlayerSprite = playerDownFrames[animationFrameIndex];
        } else if (playerUpFrames[0] != null) { // Fallback jika down frame tidak ada
            currentPlayerSprite = playerUpFrames[animationFrameIndex];
        } // Tambahkan fallback lain jika perlu


        upPressed = false;
        downPressed = false;
        leftPressed = false;
        rightPressed = false;
    }

    public void startGameThread() {
        gameThread = new Thread(this);
        gameThread.start();
    }

    @Override
    public void run() {
        // ... (kode run() tetap sama) ...
        double drawInterval = 1000000000.0 / FPS;
        double delta = 0;
        long lastTime = System.nanoTime();
        long currentTime;

        while (gameThread != null) {
            currentTime = System.nanoTime();
            delta += (currentTime - lastTime) / drawInterval;
            lastTime = currentTime;

            if (delta >= 1) {
                update();
                repaint();
                delta--;
            }
        }
    }

    private void update() {
        // Jika dialog sedang aktif, hentikan update logika game utama (pergerakan, animasi player)
        if (isDialogActive) {
            setPlayerToStandingFrame(); // Buat player tampak diam
            return;
        }

        // --- Logika Gerakan Player ---
        int oldPlayerX = playerX;
        int oldPlayerY = playerY;

        // (Blok kode gerakan player if (upPressed) ... else if ... tetap sama)
        if (upPressed) {
            playerY -= PLAYER_SPEED;
            playerDirection = Direction.UP;
        } else if (downPressed) {
            playerY += PLAYER_SPEED;
            playerDirection = Direction.DOWN;
        } else if (leftPressed) {
            playerX -= PLAYER_SPEED;
            playerDirection = Direction.LEFT;
        } else if (rightPressed) {
            playerX += PLAYER_SPEED;
            playerDirection = Direction.RIGHT;
        }

        playerX = Math.max(0, Math.min(playerX, getWidth() - displayPlayerWidth));
        playerY = Math.max(0, Math.min(playerY, getHeight() - displayPlayerHeight));
        boolean moved = (playerX != oldPlayerX || playerY != oldPlayerY);

        // --- Logika Animasi Player ---
        if (moved) {
            animationCounter++;
            if (animationCounter >= ANIMATION_SPEED) {
                animationCounter = 0;
                animationFrameIndex = 1 - animationFrameIndex;
            }
        } else {
            animationFrameIndex = 0;
            animationCounter = 0;
        }
        updateCurrentPlayerSprite(); // Panggil method baru untuk update sprite player

        // --- Cek Interaksi dengan NPC ---
        // Iterasi menggunakan salinan list jika kamu berencana menghapus NPC dari dalam loop
        for (PokemonNPC npc : new ArrayList<>(npcs)) {
            Rectangle playerBounds = new Rectangle(playerX, playerY, displayPlayerWidth, displayPlayerHeight);
            Rectangle npcBounds = new Rectangle(npc.x, npc.y, npc.displayWidth, npc.displayHeight);

            if (playerBounds.intersects(npcBounds)) {
                isDialogActive = true; // Penting: Set flag SEBELUM panggil dialog
                currentlyInteractingNPC = npc;
                showEncounterDialog(npc);
                return; // Hentikan update lebih lanjut di frame ini, dialog akan muncul
            }
        }
    }

    private void showEncounterDialog(PokemonNPC npc) {
        // Menggunakan SwingUtilities.invokeLater untuk memastikan dialog dijalankan di Event Dispatch Thread (EDT)
        SwingUtilities.invokeLater(() -> {
            String[] options = {"Lawan", "Menghindar"};
            String message = "Kamu bertemu dengan " + npc.type + "!\nApa yang akan kamu lakukan?";

            // Simpan status tombol sebelum dialog muncul
            boolean prevUp = upPressed, prevDown = downPressed, prevLeft = leftPressed, prevRight = rightPressed;
            // Reset status tombol agar player tidak bergerak otomatis setelah dialog
            upPressed = downPressed = leftPressed = rightPressed = false;


            int choice = JOptionPane.showOptionDialog(
                    MapLoader.this, // Parent component
                    message,
                    "Pertemuan Pokémon!",
                    JOptionPane.YES_NO_OPTION,
                    JOptionPane.QUESTION_MESSAGE,
                    null, // Icon (bisa null atau gunakan sprite NPC jika ingin)
                    options,
                    options[0] // Pilihan default
            );

            // Kembalikan status tombol jika perlu (opsional, tergantung desain)
            // Atau biarkan false agar player harus menekan ulang

            handleDialogChoice(choice, npc);
        });
    }

    private void handleDialogChoice(int choice, PokemonNPC encounteredNpc) {
        if (choice == JOptionPane.YES_OPTION) { // Pilihan "Lawan"
            System.out.println("Kamu memilih LAWAN " + encounteredNpc.type + "!");
            // TODO: Implementasikan logika pertarungan di sini
            // Untuk sekarang, kita hapus NPC seolah-olah telah dikalahkan
//            if (encounteredNpc != null) {
//                npcs.remove(encounteredNpc);
//            }
//            System.out.println(encounteredNpc.type + " telah dikalahkan (dihapus).");
            gameThread = null;
            ShowcaseScreen showcaseScreen = new ShowcaseScreen(gameWindow);
            gameWindow.switchPanel(showcaseScreen);

        } else if (choice == JOptionPane.NO_OPTION) { // Pilihan "Menghindar"
            System.out.println("Kamu memilih MENGHINDAR dari " + encounteredNpc.type + "!");
            // TODO: Implementasikan logika menghindar (misalnya, player mundur sedikit)
            // Untuk sekarang, player hanya melanjutkan.
            // Contoh sederhana: mundurkan player sedikit dari arah dia datang
            int pushBackDistance = TILE_SIZE / 2;
            switch(playerDirection) { // Mundur berlawanan arah terakhir player
                case UP: playerY += pushBackDistance; break;
                case DOWN: playerY -= pushBackDistance; break;
                case LEFT: playerX += pushBackDistance; break;
                case RIGHT: playerX -= pushBackDistance; break;
            }
            // Pastikan player tetap dalam batas peta setelah mundur
            playerX = Math.max(0, Math.min(playerX, getWidth() - displayPlayerWidth));
            playerY = Math.max(0, Math.min(playerY, getHeight() - displayPlayerHeight));


        } else { // Jika dialog ditutup (misalnya menekan tombol close window)
            System.out.println("Pertemuan dengan " + encounteredNpc.type + " dibatalkan.");
            // Mungkin perlu logika serupa dengan "Menghindar" jika dialog ditutup paksa
        }

        currentlyInteractingNPC = null; // Tidak ada NPC yang diajak interaksi lagi
        isDialogActive = false;         // Aktifkan kembali logika game

        // Penting: Kembalikan fokus ke GamePanel agar bisa menerima input keyboard lagi
        MapLoader.this.requestFocusInWindow();
    }

    // Method baru untuk membuat player tampak diam saat dialog aktif
    private void setPlayerToStandingFrame() {
        if (playerDownFrames[0] == null) return; // Pastikan sprite sudah dimuat

        animationFrameIndex = 0; // Selalu frame pertama (berdiri)
        // Update sprite berdasarkan arah terakhir player
        updateCurrentPlayerSprite();
    }

    // Method baru untuk mengupdate sprite player (dipanggil dari update() dan setPlayerToStandingFrame())
    private void updateCurrentPlayerSprite() {
        BufferedImage targetSprite = null;
        switch (playerDirection) {
            case UP:
                targetSprite = (playerUpFrames != null && playerUpFrames.length > animationFrameIndex && playerUpFrames[animationFrameIndex] != null) ? playerUpFrames[animationFrameIndex] : null;
                break;
            case DOWN:
                targetSprite = (playerDownFrames != null && playerDownFrames.length > animationFrameIndex && playerDownFrames[animationFrameIndex] != null) ? playerDownFrames[animationFrameIndex] : null;
                break;
            case LEFT:
                targetSprite = (playerLeftFrames != null && playerLeftFrames.length > animationFrameIndex && playerLeftFrames[animationFrameIndex] != null) ? playerLeftFrames[animationFrameIndex] : null;
                break;
            case RIGHT:
                targetSprite = (playerRightFrames != null && playerRightFrames.length > animationFrameIndex && playerRightFrames[animationFrameIndex] != null) ? playerRightFrames[animationFrameIndex] : null;
                break;
        }

        if (targetSprite != null) {
            currentPlayerSprite = targetSprite;
        } else if (playerDownFrames != null && playerDownFrames.length > 0 && playerDownFrames[0] != null) {
            currentPlayerSprite = playerDownFrames[0]; // Fallback umum
        }
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        Graphics2D g2d = (Graphics2D) g;

        // g2d.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_NEAREST_NEIGHBOR);

        // 1. Gambar Map
        if (grassTile != null) {
            for (int row = 0; row < MAP_ROWS; row++) {
                for (int col = 0; col < MAP_COLUMNS; col++) {
                    g2d.drawImage(grassTile, col * TILE_SIZE, row * TILE_SIZE, TILE_SIZE, TILE_SIZE, null);
                }
            }
        }

        // 2. Gambar NPCs (gambar NPC sebelum player agar player tampak di atas NPC jika overlap)
        // Atau gambar player dulu jika ingin NPC di atas player. Sesuaikan urutannya.
        if (npcs != null) {
            for (PokemonNPC npc : npcs) {
                if (npc.sprite != null) {
                    g2d.drawImage(npc.sprite, npc.x, npc.y, npc.displayWidth, npc.displayHeight, null);
                }
            }
        }

        // 3. Gambar Player
        if (currentPlayerSprite != null) {
            g2d.drawImage(currentPlayerSprite, playerX, playerY, displayPlayerWidth, displayPlayerHeight, null);
        }
        g2d.dispose();
    }

    // PlayerKeyAdapter tetap sama
    private class PlayerKeyAdapter extends KeyAdapter {
        // ... (kode KeyAdapter tetap sama) ...
        @Override
        public void keyPressed(KeyEvent e) {
            int key = e.getKeyCode();
            switch (key) {
                case KeyEvent.VK_W:
                case KeyEvent.VK_UP:
                    upPressed = true;
                    break;
                case KeyEvent.VK_S:
                case KeyEvent.VK_DOWN:
                    downPressed = true;
                    break;
                case KeyEvent.VK_A:
                case KeyEvent.VK_LEFT:
                    leftPressed = true;
                    break;
                case KeyEvent.VK_D:
                case KeyEvent.VK_RIGHT:
                    rightPressed = true;
                    break;
            }
        }

        @Override
        public void keyReleased(KeyEvent e) {
            int key = e.getKeyCode();
            switch (key) {
                case KeyEvent.VK_W:
                case KeyEvent.VK_UP:
                    upPressed = false;
                    break;
                case KeyEvent.VK_S:
                case KeyEvent.VK_DOWN:
                    downPressed = false;
                    break;
                case KeyEvent.VK_A:
                case KeyEvent.VK_LEFT:
                    leftPressed = false;
                    break;
                case KeyEvent.VK_D:
                case KeyEvent.VK_RIGHT:
                    rightPressed = false;
                    break;
            }
        }
    }
    // enum Direction { UP, DOWN, LEFT, RIGHT } // Pastikan class/enum Direction ada
}