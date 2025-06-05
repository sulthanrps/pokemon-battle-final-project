// package com.yourpackage; // Sesuaikan dengan struktur package Anda

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
import java.net.URL;
import java.util.*;
import java.util.List;

public class MapLoader extends JPanel implements Runnable {

    // --- KONFIGURASI MAP (Target Jumlah Tile yang Terlihat) ---
    private static final int TARGET_VISIBLE_COLUMNS = 20;
    private static final int TARGET_VISIBLE_ROWS = 15;
    private double currentTileSizeW;
    private double currentTileSizeH;

    // --- KONFIGURASI PLAYER ---
    private int originalPlayerSpriteWidth = 11;
    private int originalPlayerSpriteHeight = 18;
    private double playerAspectRatio;
    private int displayPlayerWidth;
    private int displayPlayerHeight;
    private static final double PLAYER_SCALE_RELATIVE_TO_TILE_HEIGHT = 0.8;
    private double playerSpeed;
    private static final double PLAYER_SPEED_FACTOR_PER_TILE = 0.125;

    private GameWindow gameWindow; // Asumsi ada kelas GameWindow
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

    // --- Variabel Posisi & Arah Player (Koordinat Logis) ---
    private double playerLogicalX, playerLogicalY;
    private int playerRenderX, playerRenderY;
    private Direction playerDirection = Direction.DOWN;

    private boolean upPressed, downPressed, leftPressed, rightPressed;

    // --- KONFIGURASI & VARIABEL NPC ---
    private List<PokemonNPC> npcs;
    private ImageIcon turtwigGif, electivireGif, infernapeGif; // Untuk GIF NPC

    // Jumlah NPC baru
    private static final int NUM_TURTWIGS = 3;
    private static final int NUM_ELECTIVIRES = 3;
    private static final int NUM_INFERNAPES = 4;

    private static final double NPC_SCALE_RELATIVE_TO_TILE_HEIGHT = 0.9; // Skala NPC sedikit lebih besar
    private Random randomGenerator;

    private boolean isDialogActive = false;
    private PokemonNPC currentlyInteractingNPC = null;

    private Thread gameThread;
    private final int FPS = 30;

    enum Direction {UP, DOWN, LEFT, RIGHT}

    private static final Map<String, Pokemon> npcPokemons = new HashMap<>();
    static {
        npcPokemons.put("Electivire", new Pokemon("Electivire", Type.ELECTRIC, 130, 100, 70));
        npcPokemons.put("Infernape", new Pokemon("Infernape", Type.FIRE, 110, 110, 60));
        npcPokemons.put("Turtwig", new Pokemon("Turtwig", Type.GRASS, 90, 60, 50));
    }

    private static class PokemonNPC {
        double logicalX, logicalY;
        int renderX, renderY;
        ImageIcon gifSprite;
        String type; // Nama Pokemon (misal "Turtwig")
        int displayWidth, displayHeight;

        public PokemonNPC(String type, ImageIcon gifSprite, double logicalX, double logicalY) {
            this.type = type;
            this.gifSprite = gifSprite;
            this.logicalX = logicalX;
            this.logicalY = logicalY;
        }

        public void updateSizeAndPosition(double tileW, double tileH, double scaleRelativeToTileHeight, Component c) {
            if (gifSprite == null) {
                this.displayWidth = (int) (tileW * scaleRelativeToTileHeight * 0.5); // Ukuran fallback
                this.displayHeight = (int) (tileH * scaleRelativeToTileHeight);     // Ukuran fallback
                if (this.displayWidth < 1) this.displayWidth = 1;
                if (this.displayHeight < 1) this.displayHeight = 1;
                this.renderX = (int) (this.logicalX * tileW - this.displayWidth / 2.0);
                this.renderY = (int) (this.logicalY * tileH - this.displayHeight / 2.0);
                return;
            }

            int originalGifWidth = gifSprite.getIconWidth();
            int originalGifHeight = gifSprite.getIconHeight();
            double currentAspectRatio = 1.0;
            if (originalGifHeight > 0) {
                currentAspectRatio = (double) originalGifWidth / originalGifHeight;
            }

            this.displayHeight = (int) (tileH * scaleRelativeToTileHeight);
            this.displayWidth = (int) (this.displayHeight * currentAspectRatio);

            if (this.displayWidth < 1) this.displayWidth = 1;
            if (this.displayHeight < 1) this.displayHeight = 1;

            this.renderX = (int) (this.logicalX * tileW - this.displayWidth / 2.0);
            this.renderY = (int) (this.logicalY * tileH - this.displayHeight / 2.0);
        }

        public ImageIcon getGifSprite() {
            return gifSprite;
        }
    }

    public MapLoader(GameWindow gameWindow) {
        startGameThread();
        this.gameWindow = gameWindow;
        setBackground(Color.BLACK);
        setDoubleBuffered(true);
        setFocusable(true);

        npcs = new ArrayList<>();
        randomGenerator = new Random();

        loadResources();

        addComponentListener(new ComponentAdapter() {
            @Override
            public void componentResized(ComponentEvent e) {
                System.out.println("Panel resized to: " + getWidth() + "x" + getHeight());
                updateGameLayoutAndElements();
            }
            @Override
            public void componentShown(ComponentEvent e) {
                System.out.println("Panel shown: " + getWidth() + "x" + getHeight());
                if (getWidth() > 0 && getHeight() > 0) {
                    updateGameLayoutAndElements();
                }
            }
        });
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

    private ImageIcon loadGifIcon(String path) {
        URL imgURL = getClass().getResource(path);
        if (imgURL != null) {
            return new ImageIcon(imgURL);
        } else {
            System.err.println("Peringatan: File GIF tidak ditemukan: " + path);
            return null;
        }
    }

    private void loadResources() {
        try {
            grassTile = loadImage("/Assets/MapItem/grass_tile.png");
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
                playerAspectRatio = 1.0;
            }
            currentPlayerSprite = playerDownFrames[0] != null ? playerDownFrames[0] : new BufferedImage(1,1, BufferedImage.TYPE_INT_ARGB);

            turtwigGif = loadGifIcon("/Assets/Pokemons/turtwig.gif");
            electivireGif = loadGifIcon("/Assets/Pokemons/electivire.gif");
            infernapeGif = loadGifIcon("/Assets/Pokemons/infernape.gif");

            if (turtwigGif == null) System.err.println("Turtwig.gif gagal dimuat.");
            if (electivireGif == null) System.err.println("Electivire.gif gagal dimuat.");
            if (infernapeGif == null) System.err.println("Infernape.gif gagal dimuat.");

        } catch (IOException e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(this, "Gagal memuat resource game: " + e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
            System.exit(1);
        }
    }

    private void updateGameLayoutAndElements() {
        int panelWidth = getWidth();
        int panelHeight = getHeight();

        if (panelWidth <= 0 || panelHeight <= 0) return;

        currentTileSizeW = (double) panelWidth / TARGET_VISIBLE_COLUMNS;
        currentTileSizeH = (double) panelHeight / TARGET_VISIBLE_ROWS;

        displayPlayerHeight = (int) (currentTileSizeH * PLAYER_SCALE_RELATIVE_TO_TILE_HEIGHT);
        displayPlayerWidth = (int) (displayPlayerHeight * playerAspectRatio);
        if (displayPlayerWidth < 1) displayPlayerWidth = 1;
        if (displayPlayerHeight < 1) displayPlayerHeight = 1;
        playerSpeed = currentTileSizeW * PLAYER_SPEED_FACTOR_PER_TILE;

        boolean isFirstLayout = (playerLogicalX == 0 && playerLogicalY == 0 && npcs.isEmpty());
        if (isFirstLayout) {
            initPlayerLogicalPosition();
            initNPCsLogicalPositions();
        }
        updatePlayerRenderPosition();

        for (PokemonNPC npc : npcs) {
            npc.updateSizeAndPosition(currentTileSizeW, currentTileSizeH, NPC_SCALE_RELATIVE_TO_TILE_HEIGHT, this);
        }

        System.out.println("Layout Updated. TileW: " + String.format("%.2f",currentTileSizeW) +
                ", TileH: " + String.format("%.2f", currentTileSizeH) +
                ", Player Display: " + displayPlayerWidth + "x" + displayPlayerHeight +
                ", Player Speed: " + String.format("%.2f",playerSpeed));
        repaint();
    }

    private void initPlayerLogicalPosition() {
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
    }

    private void updatePlayerRenderPosition() {
        playerRenderX = (int) (playerLogicalX * currentTileSizeW - displayPlayerWidth / 2.0);
        playerRenderY = (int) (playerLogicalY * currentTileSizeH - displayPlayerHeight / 2.0);
    }

    private void initNPCsLogicalPositions() {
        npcs.clear();
        spawnSpecificNPCsLogically("Turtwig", turtwigGif, NUM_TURTWIGS);
        spawnSpecificNPCsLogically("Electivire", electivireGif, NUM_ELECTIVIRES);
        spawnSpecificNPCsLogically("Infernape", infernapeGif, NUM_INFERNAPES);
        System.out.println("Inisialisasi " + npcs.size() + " NPC GIF (posisi logis).");
    }

    private void spawnSpecificNPCsLogically(String type, ImageIcon gifIcon, int count) {
        if (gifIcon == null) {
            System.err.println("ImageIcon untuk " + type + " belum dimuat. Melewati spawn.");
            return;
        }
        for (int i = 0; i < count; i++) {
            double npcLogicalX = randomGenerator.nextDouble() * TARGET_VISIBLE_COLUMNS;
            double npcLogicalY = randomGenerator.nextDouble() * TARGET_VISIBLE_ROWS;
            npcs.add(new PokemonNPC(type, gifIcon, npcLogicalX, npcLogicalY));
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
                repaint(); // Panggil repaint di sini, di dalam game loop utama
                delta--;
            }
        }
    }

    private void updateGameLogic() {
        if (isDialogActive) {
            setPlayerToStandingFrame();
            return;
        }

        boolean moved = false;
        // Kecepatan logis dihitung berdasarkan kecepatan piksel dan ukuran tile saat ini
        double logicalSpeedX = (currentTileSizeW > 0) ? playerSpeed / currentTileSizeW : 0;
        double logicalSpeedY = (currentTileSizeH > 0) ? playerSpeed / currentTileSizeH : 0;


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

        // Batasan pergerakan player
        double playerLogicalHalfWidth = (currentTileSizeW > 0) ? (displayPlayerWidth / 2.0) / currentTileSizeW : 0;
        double playerLogicalHalfHeight = (currentTileSizeH > 0) ? (displayPlayerHeight / 2.0) / currentTileSizeH : 0;

        playerLogicalX = Math.max(playerLogicalHalfWidth, Math.min(playerLogicalX, TARGET_VISIBLE_COLUMNS - playerLogicalHalfWidth));
        playerLogicalY = Math.max(playerLogicalHalfHeight, Math.min(playerLogicalY, TARGET_VISIBLE_ROWS - playerLogicalHalfHeight));

        updatePlayerRenderPosition();

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
        updateCurrentPlayerSprite();

        Rectangle playerBounds = new Rectangle(playerRenderX, playerRenderY, displayPlayerWidth, displayPlayerHeight);
        for (PokemonNPC npc : npcs) {
            Rectangle npcBounds = new Rectangle(npc.renderX, npc.renderY, npc.displayWidth, npc.displayHeight);
            if (playerBounds.intersects(npcBounds)) {
                if (currentlyInteractingNPC != npc || !isDialogActive) {
                    isDialogActive = true;
                    currentlyInteractingNPC = npc;
                    showEncounterDialog(npc);
                    return;
                }
            }
        }
    }

    private void showEncounterDialog(PokemonNPC npc) {
        SwingUtilities.invokeLater(() -> {
            String[] options = {"Lawan", "Menghindar"};
            String message = "Kamu bertemu dengan " + npc.type + " ("+npc.getGifSprite().getDescription()+")!\nApa yang akan kamu lakukan?";
            if (npc.getGifSprite() != null && npc.getGifSprite().getDescription() != null) {
                message = "Kamu bertemu dengan " + npc.type + "!\nApa yang akan kamu lakukan?";
            } else {
                message = "Kamu bertemu dengan " + npc.type + "!\nApa yang akan kamu lakukan?";
            }


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
            if (gameWindow != null && gameWindow.isDisplayable()) {
                gameThread = null;
                System.out.println(encounteredNpc.type + " >>> type NPC");
                // Asumsi ada kelas ShowcaseScreen dan metode switchPanel di GameWindow
                ShowcaseScreen showcaseScreen = new ShowcaseScreen(gameWindow, npcPokemons.get(encounteredNpc.type));
                gameWindow.switchPanel(showcaseScreen);
//                JOptionPane.showMessageDialog(this, "Pertarungan dengan " + encounteredNpc.type + " dimulai! (Logika belum diimplementasi)");
                // Untuk sekarang, kita hanya tutup dialog dan lanjutkan
                isDialogActive = false;
                currentlyInteractingNPC = null;
                MapLoader.this.requestFocusInWindow();


            } else {
                System.err.println("GameWindow tidak valid untuk switch panel.");
                isDialogActive = false; // Reset dialog jika gagal switch
                currentlyInteractingNPC = null;
            }
        } else if (choice == JOptionPane.NO_OPTION) {
            System.out.println("Kamu memilih MENGHINDAR dari " + encounteredNpc.type + "!");
            double pushBackLogicalDistance = 1;
            switch (playerDirection) {
                case UP: playerLogicalY += pushBackLogicalDistance; break;
                case DOWN: playerLogicalY -= pushBackLogicalDistance; break;
                case LEFT: playerLogicalX += pushBackLogicalDistance; break;
                case RIGHT: playerLogicalX -= pushBackLogicalDistance; break;
            }
            double playerLogicalHalfWidth = (currentTileSizeW > 0) ? (displayPlayerWidth / 2.0) / currentTileSizeW : 0;
            double playerLogicalHalfHeight = (currentTileSizeH > 0) ? (displayPlayerHeight / 2.0) / currentTileSizeH : 0;
            playerLogicalX = Math.max(playerLogicalHalfWidth, Math.min(playerLogicalX, TARGET_VISIBLE_COLUMNS - playerLogicalHalfWidth));
            playerLogicalY = Math.max(playerLogicalHalfHeight, Math.min(playerLogicalY, TARGET_VISIBLE_ROWS - playerLogicalHalfHeight));
            updatePlayerRenderPosition();
            isDialogActive = false;
            currentlyInteractingNPC = null;
        } else {
            System.out.println("Pertemuan dengan " + encounteredNpc.type + " dibatalkan.");
            isDialogActive = false;
            currentlyInteractingNPC = null;
        }
        MapLoader.this.requestFocusInWindow();
    }

    private void setPlayerToStandingFrame() {
        if (playerDownFrames[0] == null && playerUpFrames[0] == null && playerLeftFrames[0] == null && playerRightFrames[0] == null) return;
        animationFrameIndex = 0;
        updateCurrentPlayerSprite();
    }

    private void updateCurrentPlayerSprite() {
        BufferedImage[] currentFrames = switch (playerDirection) {
            case UP -> playerUpFrames;
            case DOWN -> playerDownFrames;
            case LEFT -> playerLeftFrames;
            case RIGHT -> playerRightFrames;
        };

        if (currentFrames != null && animationFrameIndex < currentFrames.length && currentFrames[animationFrameIndex] != null) {
            currentPlayerSprite = currentFrames[animationFrameIndex];
        } else if (currentFrames != null && currentFrames.length > 0 && currentFrames[0] != null) {
            currentPlayerSprite = currentFrames[0];
        } else if (playerDownFrames[0] != null) {
            currentPlayerSprite = playerDownFrames[0];
        } else {
            if (displayPlayerWidth > 0 && displayPlayerHeight > 0) {
                currentPlayerSprite = new BufferedImage(displayPlayerWidth, displayPlayerHeight, BufferedImage.TYPE_INT_ARGB);
                Graphics g = currentPlayerSprite.getGraphics();
                g.setColor(Color.MAGENTA);
                g.fillRect(0, 0, displayPlayerWidth, displayPlayerHeight);
                g.dispose();
            } else {
                currentPlayerSprite = new BufferedImage(1,1, BufferedImage.TYPE_INT_ARGB);
            }
        }
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        Graphics2D g2d = (Graphics2D) g;

        g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g2d.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BILINEAR);

        if (grassTile != null && currentTileSizeW > 0 && currentTileSizeH > 0) {
            for (int row = 0; row < TARGET_VISIBLE_ROWS; row++) {
                for (int col = 0; col < TARGET_VISIBLE_COLUMNS; col++) {
                    g2d.drawImage(grassTile,
                            (int) (col * currentTileSizeW),
                            (int) (row * currentTileSizeH),
                            (int) Math.ceil(currentTileSizeW),
                            (int) Math.ceil(currentTileSizeH),
                            null);
                }
            }
        }

        if (npcs != null) {
            for (PokemonNPC npc : npcs) {
                ImageIcon npcIcon = npc.getGifSprite();
                if (npcIcon != null) {
                    g2d.drawImage(npcIcon.getImage(),
                            npc.renderX,
                            npc.renderY,
                            npc.displayWidth,
                            npc.displayHeight,
                            this); // 'this' adalah ImageObserver
                }
            }
        }

        if (currentPlayerSprite != null && displayPlayerWidth > 0 && displayPlayerHeight > 0) {
            if (playerRenderX == 0 && playerRenderY == 0 && (playerLogicalX != 0 || playerLogicalY != 0) && currentTileSizeW > 0) {
                updatePlayerRenderPosition(); // Coba update lagi jika belum pas
            }
            g2d.drawImage(currentPlayerSprite, playerRenderX, playerRenderY, displayPlayerWidth, displayPlayerHeight, null);
        } else if (currentPlayerSprite == null) {
            System.err.println("currentPlayerSprite is null in paintComponent.");
        }
        // g2d.dispose(); // Umumnya tidak perlu untuk Graphics dari parameter paintComponent
    }

    private class PlayerKeyAdapter extends KeyAdapter {
        @Override
        public void keyPressed(KeyEvent e) {
            int key = e.getKeyCode();
            if (isDialogActive) return;

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

    // Anda mungkin memerlukan kelas GameWindow dan ShowcaseScreen seperti ini (sangat sederhana):
    /*
    static class GameWindow extends JFrame {
        public GameWindow() {
            setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
            setTitle("Game Map Loader");
            MapLoader mapLoader = new MapLoader(this);
            add(mapLoader, BorderLayout.CENTER);
            setPreferredSize(new Dimension(800, 600));
            pack();
            setLocationRelativeTo(null);
            setVisible(true);
            mapLoader.startGameThread();
        }

        public void switchPanel(JPanel panel) {
            // Logika untuk mengganti panel
            System.out.println("Switching panel to: " + panel.getClass().getSimpleName());
            getContentPane().removeAll();
            getContentPane().add(panel, BorderLayout.CENTER);
            revalidate();
            repaint();
            panel.requestFocusInWindow();
        }

        public static void main(String[] args) {
            SwingUtilities.invokeLater(GameWindow::new);
        }
    }

    static class ShowcaseScreen extends JPanel {
        public ShowcaseScreen(GameWindow gw, String pokemonName) {
            JLabel label = new JLabel("Showcase/Battle with: " + pokemonName);
            add(label);
            JButton backButton = new JButton("Back to Map");
            backButton.addActionListener(e -> {
                MapLoader newMapLoader = new MapLoader(gw);
                gw.switchPanel(newMapLoader);
                newMapLoader.startGameThread();
            });
            add(backButton);
        }
    }
    */
}