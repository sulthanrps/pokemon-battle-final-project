import javax.swing.*;
import javax.swing.border.AbstractBorder;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.geom.Area;
import java.awt.geom.Rectangle2D;
import java.awt.geom.RoundRectangle2D;
import java.io.IOException;
import java.net.URL;
import javax.imageio.ImageIO;

public class PokemonBattleUI extends JFrame {

    // Konstanta untuk warna dan font
    private static final Color HP_BAR_BACKGROUND = new Color(211, 211, 211); // Warna abu-abu muda untuk latar belakang HP bar
    private static final Color HP_BAR_COLOR_GREEN = new Color(0, 200, 0); // Hijau terang
    private static final Color HP_BAR_COLOR_YELLOW = new Color(255, 215, 0); // Kuning emas
    private static final Color HP_BAR_COLOR_RED = new Color(255, 69, 0);   // Merah oranye
    private static final Font TITLE_FONT = new Font("Verdana", Font.BOLD, 28); // Font lebih besar dan tebal
    private static final Font LABEL_FONT = new Font("Verdana", Font.BOLD, 14);
    private static final Font BUTTON_FONT = new Font("Verdana", Font.BOLD, 12); // Font lebih kecil untuk tombol
    private static final Color CHARACTER_NAME_BG = new Color(46, 204, 113, 200); // Hijau semi-transparan untuk label nama
    private static final Color BUTTON_BG_COLOR = new Color(120, 220, 90); // Warna hijau muda untuk tombol
    private static final Color BUTTON_TEXT_COLOR = Color.DARK_GRAY;

    // Komponen UI
    private JLabel playerHealthTextLabel; // Untuk teks "HP: 70%"
    private JProgressBar playerHealthBar;
    private JLabel enemyHealthTextLabel; // Untuk teks "HP: 70%"
    private JProgressBar enemyHealthBar;
    private JLabel playerSpriteLabel;
    private JLabel enemySpriteLabel;
    private JLabel gameStatusLabel;

    private int playerCurrentHP = 70;
    private int enemyCurrentHP = 70;
    private final int MAX_HP = 100;

    private Image backgroundImage; // Untuk gambar latar belakang

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
        setTitle("Pertarungan Pokemon - Mirip Gambar");
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
        gameStatusLabel = new JLabel("SERANG " +
                "DAN KALAHKAN");
        gameStatusLabel.setFont(TITLE_FONT);
        gameStatusLabel.setForeground(Color.WHITE);
        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.anchor = GridBagConstraints.NORTHWEST;
        gbc.insets = new Insets(10, 10, 0, 0); // Padding atas dan kiri
        mainPanel.add(gameStatusLabel, gbc);

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
        JPanel panel = new JPanel();
        panel.setOpaque(true); // Panel karakter transparan
        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS)); // Susun vertikal: HP, Sprite, Nama

        // Panel untuk HP (Teks HP dan Bar HP) - disusun horizontal
        JPanel hpDisplayPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 5, 0));
        hpDisplayPanel.setBackground(new Color(50, 50, 50, 200));

        JLabel hpText = new JLabel("HP: " + (isEnemy ? enemyCurrentHP : playerCurrentHP) + "%");
        hpText.setFont(LABEL_FONT);
        hpText.setForeground(Color.WHITE);

        JProgressBar healthBar = new JProgressBar(0, MAX_HP);
        healthBar.setValue(MAX_HP);
        healthBar.setStringPainted(false); // Tidak menampilkan string di bar itu sendiri
        healthBar.setPreferredSize(new Dimension(120, 20)); // Ukuran HP bar
        healthBar.setBackground(HP_BAR_BACKGROUND); // Warna latar belakang bar
        healthBar.setBorder(BorderFactory.createLineBorder(Color.RED, 2));

        healthBar.setOpaque(true);

        hpDisplayPanel.add(hpText);
        hpDisplayPanel.add(healthBar);

        // Sprite (Gambar Karakter)
        JLabel spriteLabel = new JLabel();
        // Ganti dengan path ke gambar sprite Anda di folder resources
        String spritePath = isEnemy ? "Assets/Pokemons/bulbasaur.png" : "Assets/Pokemons/pikachu.png";
        try {
            URL imageUrl = getClass().getResource(spritePath);
            if (imageUrl != null) {
                ImageIcon icon = new ImageIcon(ImageIO.read(imageUrl));
                // Resize gambar jika perlu (contoh: 100x100)
                Image img = icon.getImage().getScaledInstance(120, 120, Image.SCALE_SMOOTH);
                spriteLabel.setIcon(new ImageIcon(img));
            } else {
                spriteLabel.setText(isEnemy ? "[IMG MUSUH]" : "[IMG KAMU]");
                spriteLabel.setPreferredSize(new Dimension(120, 120));
            }
        } catch (IOException e) {
            spriteLabel.setText(isEnemy ? "[GAGAL LOAD MUSUH]" : "[GAGAL LOAD KAMU]");
            spriteLabel.setPreferredSize(new Dimension(120, 120));
            e.printStackTrace();
        }
        spriteLabel.setHorizontalAlignment(SwingConstants.CENTER);
        spriteLabel.setBorder(new EmptyBorder(10,0,10,0)); // Padding atas-bawah sprite

        // Label Nama Karakter (KAMU/MUSUH) dengan latar belakang khusus
        JLabel nameLabel = new JLabel(characterName, SwingConstants.CENTER);
        nameLabel.setFont(LABEL_FONT);
        nameLabel.setForeground(Color.WHITE);
        nameLabel.setOpaque(true);
        nameLabel.setBackground(CHARACTER_NAME_BG);
        nameLabel.setBorder(new RoundedBorder(15, CHARACTER_NAME_BG, 2, Color.DARK_GRAY)); // Border rounded
        nameLabel.setPreferredSize(new Dimension(150, 40)); // Ukuran tetap untuk konsistensi


        if (isEnemy) {
            enemyHealthTextLabel = hpText;
            enemyHealthBar = healthBar;
            enemySpriteLabel = spriteLabel;
            // Susunan untuk Musuh: HP, Sprite, Nama
            panel.add(hpDisplayPanel);
            panel.add(Box.createVerticalStrut(5)); // Spasi kecil
            panel.add(enemySpriteLabel);
            panel.add(Box.createVerticalStrut(5));
            panel.add(nameLabel);
        } else {
            playerHealthTextLabel = hpText;
            playerHealthBar = healthBar;
            playerSpriteLabel = spriteLabel;
            // Susunan untuk Pemain: HP, Sprite, Nama
            panel.add(hpDisplayPanel);
            panel.add(Box.createVerticalStrut(5));
            panel.add(playerSpriteLabel);
            panel.add(Box.createVerticalStrut(5));
            panel.add(nameLabel);
        }
        // Menambahkan alignment agar komponen di tengah panel BoxLayout
        hpDisplayPanel.setAlignmentX(Component.CENTER_ALIGNMENT);
        spriteLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
        nameLabel.setAlignmentX(Component.CENTER_ALIGNMENT);


        return panel;
    }

    private JButton createActionButton(String actionName) {
        JButton button = new JButton(actionName);
        button.setFont(BUTTON_FONT);
        button.setBackground(BUTTON_BG_COLOR);
        button.setForeground(BUTTON_TEXT_COLOR);
        button.setFocusPainted(false);
        // Menggunakan custom border untuk tampilan rounded
        button.setBorder(new RoundedBorder(20, BUTTON_BG_COLOR, 2, Color.DARK_GRAY)); // radius, bgColor, thickness, borderColor
        button.setOpaque(true); // Penting agar background terlihat dengan custom border
        button.setContentAreaFilled(false); // Agar custom painting border bekerja baik
        button.setPreferredSize(new Dimension(150, 50)); // Ukuran tombol
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
            playerHealthTextLabel.setText("HP: " + (playerCurrentHP * 100 / MAX_HP) + "%");
            setHealthBarColor(playerHealthBar, playerCurrentHP);
        }
        if (enemyHealthBar != null && enemyHealthTextLabel != null) {
            enemyHealthBar.setValue(enemyCurrentHP);
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

            // Gambar background (fill)
            if (c.isOpaque() || backgroundColor != null) { // Hanya gambar background jika komponen opaque atau warna background diset
                Area area = new Area(outer);
                if (c instanceof JButton && !((JButton)c).getModel().isArmed()) { // Untuk tombol, agar efek klik tetap terlihat
                    g2d.setColor(backgroundColor != null ? backgroundColor : c.getBackground());
                } else if (!(c instanceof JButton)) {
                    g2d.setColor(backgroundColor != null ? backgroundColor : c.getBackground());
                }
                g2d.fill(area);
            }


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
        SwingUtilities.invokeLater(() -> {
            PokemonBattleUI game = new PokemonBattleUI();
            game.setVisible(true);
        });
    }
}