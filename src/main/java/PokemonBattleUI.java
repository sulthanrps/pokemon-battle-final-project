import javax.swing.*;
import javax.swing.border.AbstractBorder;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.geom.Area;
import java.awt.geom.RoundRectangle2D;
import java.io.IOException;
import java.net.URL;
import javax.imageio.ImageIO;
import javax.swing.plaf.basic.BasicProgressBarUI;
import java.awt.Graphics; // Pastikan import ini ada
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import util.FontManager;

public class PokemonBattleUI extends JFrame {

    // Konstanta untuk warna dan font
    private static final Color HP_BAR_BACKGROUND = new Color(211, 211, 211); // Warna abu-abu muda untuk latar belakang HP bar
    private static final Color HP_BAR_COLOR_GREEN = new Color(0, 200, 0); // Hijau terang
    private static final Color HP_BAR_COLOR_YELLOW = new Color(255, 215, 0); // Kuning emas
    private static final Color HP_BAR_COLOR_RED = new Color(255, 69, 0);   // Merah oranye
    private static final Color BUTTON_BG_COLOR = new Color(66, 153, 45); // Warna hijau muda untuk tombol

    // Komponen UI
    private JLabel playerHealthTextLabel; // Untuk teks "HP: 70%"
    private JProgressBar playerHealthBar;
    private JLabel enemyHealthTextLabel; // Untuk teks "HP: 70%"
    private JProgressBar enemyHealthBar;
    private JLabel gameStatusLabel;
    private JLabel gameStatusLabelBottom;

    private int playerCurrentHP = 100;
    private int enemyCurrentHP = 100;
    private final int MAX_HP = 100;

    // Panel Latar Belakang Kustom
    class BackgroundPanel extends JPanel {
        private Image bgImage;

        public BackgroundPanel(String imagePath) {
            try {
                URL imageUrl = getClass().getResource(imagePath);
                if (imageUrl != null) {
                    bgImage = ImageIO.read(imageUrl);
                } else {
                    System.err.println("Resource gambar latar tidak ditemukan: " + imagePath);
                    bgImage = null; // Atau set gambar default
                }
            } catch (IOException e) {
                System.err.println("Gagal memuat gambar latar: " + imagePath);
                e.printStackTrace();
                bgImage = null;
            }
            setLayout(new BorderLayout()); // Penting untuk menampung panel lain
        }

        @Override
        protected void paintComponent(Graphics g) {
            super.paintComponent(g);
            if (bgImage != null) {
                // Gambar latar belakang agar mengisi seluruh panel
                g.drawImage(bgImage, 0, 0, getWidth(), getHeight(), this);
            } else {
                // Fallback jika gambar tidak ada
                g.setColor(new Color(90, 170, 75)); // Warna hijau rumput solid
                g.fillRect(0, 0, getWidth(), getHeight());
            }
        }
    }

    public PokemonBattleUI() {
        setTitle("Pertarungan Pokemon");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(900, 700); // Ukuran frame disesuaikan agar lebih lega
        setLocationRelativeTo(null);

        // Menggunakan BackgroundPanel sebagai content pane
        // Ganti "/battle_background.png" dengan path ke gambar latar Anda di folder resources
        // Jika tidak ada gambar, akan menggunakan warna solid hijau.
        BackgroundPanel backgroundContentPane = new BackgroundPanel("Assets/backgroundBattle.png");
        setContentPane(backgroundContentPane);

        // Panel utama yang akan menampung semua elemen game, dibuat transparan
        JPanel mainPanel = new JPanel(new GridBagLayout());
        mainPanel.setOpaque(false); // Transparan agar gambar latar terlihat
        mainPanel.setBorder(new EmptyBorder(20, 20, 20, 20));
        GridBagConstraints gbc = new GridBagConstraints();

        // --- Teks "SERANG DAN KALAHKAN" (Kiri Atas) ---
        gameStatusLabel = new JLabel("ATTACK");
        gameStatusLabel.setFont(FontManager.TITLE_FONT);
        gameStatusLabel.setForeground(Color.WHITE);
        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.anchor = GridBagConstraints.NORTHWEST;
        gbc.insets = new Insets(10, 10, 0, 0); // Padding atas dan kiri
        mainPanel.add(gameStatusLabel, gbc);

        gameStatusLabelBottom = new JLabel("AND CONQUER THE BATTLE");
        gameStatusLabelBottom.setFont(FontManager.TITLE_FONT);
        gameStatusLabelBottom.setForeground(Color.WHITE);
        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.anchor = GridBagConstraints.NORTHWEST;
        gbc.insets = new Insets(40, 10, 0, 0); // Padding atas dan kiri
        mainPanel.add(gameStatusLabelBottom, gbc);

        // --- Panel Musuh (Kanan Atas) ---
        JPanel enemyPanel = createCharacterPanel("MUSUH", true);
        enemyPanel.setOpaque(false);
        gbc.gridx = 1;
        gbc.gridy = 0;
        gbc.anchor = GridBagConstraints.NORTHEAST;
        gbc.insets = new Insets(10, 0, 0, 10); // Padding atas dan kanan
        mainPanel.add(enemyPanel, gbc);

        // --- Panel Pemain (Kiri Bawah) ---
        JPanel playerPanel = createCharacterPanel("KAMU", false);
        playerPanel.setOpaque(false);
        gbc.gridx = 0;
        gbc.gridy = 1;
        gbc.weighty = 1.0; // Agar mendorong panel pemain ke bawah
        gbc.anchor = GridBagConstraints.SOUTHWEST;
        gbc.insets = new Insets(0, 10, 60, 0); // Padding bawah dan kiri (memberi ruang untuk tombol)
        mainPanel.add(playerPanel, gbc);

        // --- Panel Tombol Aksi (Bawah Tengah) ---
        JPanel actionPanel = new JPanel(new GridLayout(2, 2, 15, 15)); // Grid untuk tombol
        actionPanel.setOpaque(false);
        actionPanel.setBorder(new EmptyBorder(0, 0, 10, 0)); // Padding bawah

        String[] attackNames = {"THUNDER", "IRON TAIL", "ELECTRO BALL", "QUICK ATTACK"};
        for (String attackName : attackNames) {
            JButton attackButton = createActionButton(attackName);
            actionPanel.add(attackButton);
        }
        gbc.gridx = 0;
        gbc.gridy = 2;
        gbc.gridwidth = 2; // Melintasi dua kolom
        gbc.weighty = 0;
        gbc.anchor = GridBagConstraints.SOUTH; // Di bagian bawah tengah
        gbc.fill = GridBagConstraints.NONE;
        gbc.insets = new Insets(10, 0, 20, 0); // Padding atas dan bawah untuk panel tombol
        mainPanel.add(actionPanel, gbc);

        // Menambahkan mainPanel ke backgroundContentPane
        backgroundContentPane.add(mainPanel, BorderLayout.CENTER);

        updateHealthBars();
    }

    private JPanel createCharacterPanel(String characterName, boolean isEnemy) {
        System.out.println("Creating Character Panel for: " + characterName); // Debugging tambahan

        // Panel utama untuk karakter ini, buat transparan agar lebih mudah melihat efeknya
        JPanel panel = new JPanel(); // Gunakan FlowLayout sederhana untuk tes
        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
        panel.setOpaque(true); // Biarkan transparan terhadap background utama game

        // Label HP sederhana untuk konteks
        JLabel hpTextLabel = new JLabel("HP (" + characterName + "):");

        // JProgressBar yang akan kita tes
        JProgressBar healthBar = new JProgressBar(0, MAX_HP);
        healthBar.setUI(new BasicProgressBarUI()); // PASTIKAN INI BENAR-BENAR DITERAPKAN
        healthBar.setPreferredSize(new Dimension(150, 25)); // Beri ukuran yang cukup
        healthBar.setBackground(HP_BAR_BACKGROUND); // Warna trek (bagian kosong)
        healthBar.setOpaque(true); // Penting agar background dan foreground tergambar

        JPanel hpDisplayPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 5, 0));
        hpDisplayPanel.setOpaque(false);
        hpDisplayPanel.add(hpTextLabel);
        hpDisplayPanel.add(healthBar);

        hpDisplayPanel.setAlignmentX(Component.CENTER_ALIGNMENT);
        hpDisplayPanel.setOpaque(false); // Perlu true jika ingin setBackground terlihat solid
        hpDisplayPanel.setBackground(new Color(50, 50, 50, 200)); // Warna semi-transparan Anda
        panel.add(hpDisplayPanel);
        // HAPUS SEMENTARA border kustom untuk melihat apakah ini berpengaruh
        // healthBar.setBorder(BorderFactory.createLineBorder(Color.RED, 2));

        // Set nilai awal berdasarkan HP saat ini
        int currentCharacterHP = isEnemy ? enemyCurrentHP : playerCurrentHP;
        healthBar.setValue(currentCharacterHP);
        System.out.println("Initial HP for " + characterName + ": " + currentCharacterHP);

        // Assign ke field instance agar updateHealthBars bisa bekerja
        if (isEnemy) {
            enemyHealthBar = healthBar;
            enemyHealthTextLabel = hpTextLabel; // Update referensi ini juga
        } else {
            playerHealthBar = healthBar;
            playerHealthTextLabel = hpTextLabel; // Update referensi ini juga
        }

        // Panggil setHealthBarColor SEGERA untuk mengatur warna awal saat pembuatan panel
        // Ini PENTING untuk tes render awal. updateHealthBars() akan dipanggil lagi nanti.
        System.out.println("Applying initial color for " + characterName + " in createCharacterPanel");
        setHealthBarColor(healthBar, currentCharacterHP);

        panel.add(hpTextLabel);
        panel.add(healthBar);

        JLayeredPane spriteContainerPane = new JLayeredPane();

        final int LILYPAD_WIDTH = 150;
        final int LILYPAD_HEIGHT = 60;
        final int POKEMON_WIDTH = 120;
        final int POKEMON_HEIGHT = 120;

        spriteContainerPane.setPreferredSize(new Dimension(LILYPAD_WIDTH, LILYPAD_HEIGHT + POKEMON_HEIGHT / 2));

        JLabel lilypadLabel = new JLabel();
        try {
            URL lilypadUrl = getClass().getResource("/Assets/terataiPlayer.png");
            if (lilypadUrl != null) {
                ImageIcon lilypadIcon = new ImageIcon(ImageIO.read(lilypadUrl));
                Image lilypadImg = lilypadIcon.getImage().getScaledInstance(LILYPAD_WIDTH, LILYPAD_HEIGHT, Image.SCALE_SMOOTH);
                lilypadLabel.setIcon(new ImageIcon(lilypadImg));
            } else {
                lilypadLabel.setText("[IMG TERATAI]");
                System.err.println("Resource gambar teratai tidak ditemukan.");
            }
        } catch (IOException e) {
            lilypadLabel.setText("[GAGAL LOAD TERATAI]");
            e.printStackTrace();
        }
        lilypadLabel.setBounds(0, POKEMON_HEIGHT / 2 + 35, LILYPAD_WIDTH, LILYPAD_HEIGHT - 35);

        JLabel pokemonSpriteLabel = new JLabel();
        String spritePath = isEnemy ? "/Assets/Pokemons/charizard.png" : "/Assets/Pokemons/pikachu.png";
        try {
            URL imageUrl = getClass().getResource(spritePath);
            if (imageUrl != null) {
                ImageIcon icon = new ImageIcon(ImageIO.read(imageUrl));
                Image img = icon.getImage().getScaledInstance(POKEMON_WIDTH, POKEMON_WIDTH, Image.SCALE_SMOOTH);
                pokemonSpriteLabel.setIcon(new ImageIcon(img));
            } else {
                pokemonSpriteLabel.setText(isEnemy ? "[IMG MUSUH]" : "[IMG KAMU]");
                System.err.println("Resource gambar sprite tidak ditemukan: " + spritePath);
            }
        } catch (IOException e) {
            pokemonSpriteLabel.setText(isEnemy ? "[GAGAL LOAD MUSUH]" : "[GAGAL LOAD KAMU]");
            e.printStackTrace();
        }
        pokemonSpriteLabel.setHorizontalAlignment(SwingConstants.CENTER);

        int pikachuX = (LILYPAD_WIDTH - POKEMON_WIDTH) / 2;
        int pikachuY = 0;
        pokemonSpriteLabel.setBounds(pikachuX, pikachuY, POKEMON_WIDTH, POKEMON_HEIGHT);

        spriteContainerPane.add(lilypadLabel, Integer.valueOf(0));
        spriteContainerPane.add(pokemonSpriteLabel, Integer.valueOf(1));

        spriteContainerPane.setAlignmentX(Component.CENTER_ALIGNMENT);
        panel.add(Box.createVerticalStrut(5));
        panel.add(spriteContainerPane);
        panel.add(Box.createVerticalStrut(5));

        System.out.println("Finished Creating Character Panel for: " + characterName + " with bar value: " + healthBar.getValue());
        return panel;
    }

    private JButton createActionButton(String actionName) {
        final int cornerRadius = 20;

        JButton button = new JButton(actionName) {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

                if (getModel().isArmed()) {
                    g2.setColor(getBackground().darker());
                } else {
                    g2.setColor(getBackground());
                }

                g2.fillRoundRect(0, 0, getWidth(), getHeight(), cornerRadius, cornerRadius);

                g2.dispose();

                super.paintComponent(g);
            }
        };

        button.setFont(FontManager.BUTTON_FONT);
        button.setBackground(BUTTON_BG_COLOR);
        button.setForeground(Color.WHITE);

        button.setFocusPainted(false);
        button.setBorder(new RoundedBorder(cornerRadius, null, 1, Color.DARK_GRAY));

        button.setOpaque(false);
        button.setContentAreaFilled(false);

        button.setPreferredSize(new Dimension(150, 50));
        button.setCursor(new Cursor(Cursor.HAND_CURSOR));

        button.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                System.out.println(actionName + " digunakan!");
                int damageToEnemy = (int) (Math.random() * 11) + 10;
                enemyCurrentHP -= damageToEnemy;
                if (enemyCurrentHP < 0) enemyCurrentHP = 0;

                if (enemyCurrentHP > 0) {
                    int damageToPlayer = (int) (Math.random() * 11) + 5;
                    playerCurrentHP -= damageToPlayer;
                    if (playerCurrentHP < 0) playerCurrentHP = 0;
                }
                updateHealthBars();
                checkGameEnd();
            }
        });
        return button;
    }

    private void updateHealthBars() {
        if (playerHealthBar != null && playerHealthTextLabel != null) {
            playerHealthBar.setValue(playerCurrentHP);
            playerHealthTextLabel.setFont(FontManager.NORMAL_FONT);
            playerHealthTextLabel.setText("HP: " + (playerCurrentHP * 100 / MAX_HP) + "%");
            setHealthBarColor(playerHealthBar, playerCurrentHP);
        }
        if (enemyHealthBar != null && enemyHealthTextLabel != null) {
            enemyHealthBar.setValue(enemyCurrentHP);
            enemyHealthTextLabel.setFont(FontManager.NORMAL_FONT);
            enemyHealthTextLabel.setText("HP: " + (enemyCurrentHP * 100 / MAX_HP) + "%");
            setHealthBarColor(enemyHealthBar, enemyCurrentHP);
        }
    }

    private void setHealthBarColor(JProgressBar healthBar, int currentHP) {
        float percentage = (float) currentHP / MAX_HP;
        System.out.println("HP: " + currentHP + ", Percentage: " + percentage);
        if (percentage > 0.6f) { // Batas hijau lebih tinggi
            healthBar.setForeground(HP_BAR_COLOR_GREEN);
            System.out.println("  -> Set to GREEN");
        } else if (percentage > 0.3f) { // Batas kuning
            healthBar.setForeground(HP_BAR_COLOR_YELLOW);
            System.out.println("  -> Set to YELLOW");
        } else {
            healthBar.setForeground(HP_BAR_COLOR_RED);
            System.out.println("  -> Set to RED");
        }

        healthBar.repaint();
    }

    private void checkGameEnd() {
        String message = "";
        if (playerCurrentHP <= 0 && enemyCurrentHP <= 0) {
            message = "Seri! Kedua petarung kalah!";
        } else if (playerCurrentHP <= 0) {
            message = "KAMU KALAH! Musuh menang.";
        } else if (enemyCurrentHP <= 0) {
            message = "KAMU MENANG! Musuh telah dikalahkan.";
        }

        if (!message.isEmpty()) {
            Component mainPanelComponent = getContentPane().getComponent(0); // Ini adalah mainPanel di dalam BackgroundPanel
            if (mainPanelComponent instanceof JPanel) {
                JPanel actualMainPanel = (JPanel) mainPanelComponent;
                // Cari actionPanel di dalam actualMainPanel. Berdasarkan GridBagLayout, ini mungkin lebih rumit.
                // Asumsi actionPanel adalah komponen terakhir atau salah satu dari beberapa komponen terakhir.
                // Cara yang lebih aman adalah menyimpan referensi ke actionPanel.
                // Untuk sekarang, kita coba cari berdasarkan tipe komponen.
                for(Component potentialActionPanel : actualMainPanel.getComponents()){
                    if(potentialActionPanel instanceof JPanel){
                        boolean allButtons = true;
                        int buttonCount = 0;
                        for(Component comp : ((JPanel) potentialActionPanel).getComponents()){
                            if(!(comp instanceof JButton)){
                                allButtons = false;
                                break;
                            }
                            buttonCount++;
                        }
                        if(allButtons && buttonCount == 4){ // Asumsi panel tombol punya 4 button
                            for (Component comp : ((JPanel) potentialActionPanel).getComponents()) {
                                if (comp instanceof JButton) {
                                    comp.setEnabled(false);
                                }
                            }
                            break; // Keluar setelah menemukan dan menonaktifkan tombol
                        }
                    }
                }
            }

            gameStatusLabel.setText(message.toUpperCase());
            gameStatusLabelBottom.setText("");
            JOptionPane.showMessageDialog(this, message, "Permainan Selesai", JOptionPane.INFORMATION_MESSAGE);
        }
    }

    // Custom Border untuk elemen rounded
    private static class RoundedBorder extends AbstractBorder {
        private int radius;
        private Color backgroundColor;
        private int thickness;
        private Color borderColor;

        public RoundedBorder(int radius, Color backgroundColor, int thickness, Color borderColor) {
            this.radius = radius;
            this.backgroundColor = backgroundColor; // Warna isi
            this.thickness = thickness; // Ketebalan garis tepi
            this.borderColor = borderColor; // Warna garis tepi
        }

        @Override
        public void paintBorder(Component c, Graphics g, int x, int y, int width, int height) {
            Graphics2D g2d = (Graphics2D) g.create();
            g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

            Shape outer = new RoundRectangle2D.Float(x, y, width -1 , height -1 , radius, radius);
            Shape inner = new RoundRectangle2D.Float(x + thickness, y + thickness, width - 1 - thickness * 2, height - 1 - thickness * 2, radius - thickness, radius - thickness);

            // Gambar border
            if (thickness > 0 && borderColor != null) {
                Area borderArea = new Area(outer);
                borderArea.subtract(new Area(inner));
                g2d.setColor(borderColor);
                g2d.fill(borderArea);
            }
            g2d.dispose();
        }

        @Override
        public Insets getBorderInsets(Component c) {
            return new Insets(radius / 2 + thickness, radius / 2 + thickness, radius / 2 + thickness, radius / 2 + thickness);
        }

        @Override
        public Insets getBorderInsets(Component c, Insets insets) {
            insets.left = insets.top = insets.right = insets.bottom = radius / 2 + thickness;
            return insets;
        }

        @Override
        public boolean isBorderOpaque() {
            return true; // Jika false, background komponen induk bisa tembus
        }
    }


    public static void main(String[] args) {
        try {
            UIManager.setLookAndFeel(UIManager.getCrossPlatformLookAndFeelClassName());
        } catch (Exception e) {
            e.printStackTrace();
        }
        SwingUtilities.invokeLater(() -> {
            PokemonBattleUI game = new PokemonBattleUI();
            game.setVisible(true);
        });
    }
}