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
import javax.swing.plaf.basic.BasicProgressBarUI;// Pastikan FontManager Anda sudah benar

public class PokemonBattleUI extends JFrame {

    // Konstanta untuk warna dan font
    private static final Color HP_BAR_BACKGROUND = new Color(220, 220, 220); // Warna abu-abu muda untuk latar belakang HP bar (target)
    private static final Color HP_BAR_COLOR_GREEN = new Color(0, 200, 0);
    private static final Color HP_BAR_COLOR_YELLOW = new Color(255, 204, 51); // Kuning di target
    private static final Color HP_BAR_COLOR_RED = new Color(255, 69, 0);
    private static final Color BUTTON_BG_COLOR = new Color(106, 190, 48); // Warna hijau tombol di target
    private static final Color CHARACTER_NAME_BG_PLAYER = new Color(106, 190, 48); // Hijau untuk label "KAMU"
    private static final Color CHARACTER_NAME_BG_ENEMY = new Color(50, 50, 50);    // Hitam/Abu tua untuk label "MUSUH"
    private static final Color CHARACTER_NAME_FG = Color.WHITE;

    // Komponen UI
    private JLabel playerHealthTextLabel;
    private JProgressBar playerHealthBar;
    private JLabel enemyHealthTextLabel;
    private JProgressBar enemyHealthBar;
    private JPanel mainPanel;

    private int playerCurrentHP = 100; // Sesuaikan dengan HP awal di target
    private int enemyCurrentHP = 100;  // Sesuaikan dengan HP awal di target
    private final int MAX_HP = 100;

    class BackgroundPanel extends JPanel {
        private Image bgImage;
        public BackgroundPanel(String imagePath) {
            try {
                URL imageUrl = getClass().getResource(imagePath);
                if (imageUrl != null) {
                    bgImage = ImageIO.read(imageUrl);
                } else {
                    System.err.println("Resource gambar latar tidak ditemukan: " + imagePath);
                }
            } catch (IOException e) {
                System.err.println("Gagal memuat gambar latar: " + imagePath);
                e.printStackTrace();
            }
            setLayout(new BorderLayout());
        }
        @Override
        protected void paintComponent(Graphics g) {
            super.paintComponent(g);
            if (bgImage != null) {
                g.drawImage(bgImage, 0, 0, getWidth(), getHeight(), this);
            } else {
                g.setColor(new Color(90, 170, 75));
                g.fillRect(0, 0, getWidth(), getHeight());
            }
        }
    }

    public PokemonBattleUI() {
        setTitle("Pertarungan Pokemon");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(700, 600);
        setLocationRelativeTo(null);

        BackgroundPanel backgroundContentPane = new BackgroundPanel("/Assets/backgroundBattle.png"); // Pastikan path ini benar
        setContentPane(backgroundContentPane);

        mainPanel = new JPanel(new GridBagLayout());
        mainPanel.setOpaque(false);
        mainPanel.setBorder(new EmptyBorder(15, 15, 15, 15)); // Padding disesuaikan
        GridBagConstraints gbc = new GridBagConstraints();

        // --- Panel Status Game (Kiri Atas) ---
        JPanel statusTextPanel = new JPanel();
        statusTextPanel.setLayout(new BoxLayout(statusTextPanel, BoxLayout.Y_AXIS));
        statusTextPanel.setOpaque(false);

        JLabel statusLine1 = new JLabel("SERANG");
        statusLine1.setFont(FontManager.TITLE_FONT); // Gunakan font dari FontManager
        statusLine1.setForeground(Color.WHITE);
        statusLine1.setAlignmentX(Component.LEFT_ALIGNMENT);

        JLabel statusLine2 = new JLabel("DAN KALAHKAN");
        statusLine2.setFont(FontManager.TITLE_FONT); // Gunakan font dari FontManager
        statusLine2.setForeground(Color.WHITE);
        statusLine2.setAlignmentX(Component.LEFT_ALIGNMENT);

        statusTextPanel.add(statusLine1);
        statusTextPanel.add(statusLine2);

        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.weightx = 0.5; // Beri bobot agar bisa mendorong panel musuh ke kanan
        gbc.anchor = GridBagConstraints.NORTHWEST;
        gbc.insets = new Insets(10, 10, 0, 0);
        mainPanel.add(statusTextPanel, gbc);

        // --- Panel Musuh (Kanan Atas) ---
        JPanel enemyPanel = createCharacterPanel("ELECTIVIRE", true);
        enemyPanel.setOpaque(false);
        gbc.gridx = 1;
        gbc.gridy = 0;
        gbc.weightx = 0.5; // Beri bobot
        gbc.weighty = 0.2;
        gbc.anchor = GridBagConstraints.NORTHEAST;
//        gbc.fill = GridBagConstraints.BOTH;
        gbc.insets = new Insets(10, 0, 0, 10);
        mainPanel.add(enemyPanel, gbc);

        // --- Panel Pemain (Kiri Bawah) ---
        JPanel playerPanel = createCharacterPanel("SNORLAX", false);
        playerPanel.setOpaque(false);
        gbc.gridx = 0;
        gbc.gridy = 2;
        gbc.weightx = 0.5;
        gbc.weighty = 1; // Agar mendorong panel pemain ke bawah
        gbc.anchor = GridBagConstraints.SOUTHWEST;
//        gbc.fill = GridBagConstraints.BOTH;
        gbc.insets = new Insets(0, 10, 0, 0); // Padding bawah disesuaikan
        mainPanel.add(playerPanel, gbc);

        // --- Panel Tombol Aksi (Bawah Tengah) ---
        JPanel actionPanel = new JPanel(new GridLayout(2, 2, 10, 10)); // Spasi antar tombol disesuaikan
        actionPanel.setOpaque(false);
        // actionPanel.setBorder(new EmptyBorder(0, 0, 10, 0)); // Dihapus agar lebih fleksibel dengan anchor SOUTH

        String[] attackNames = {"THUNDER", "IRON TAIL", "ELECTRO BALL", "QUICK ATTACK"};
        for (String attackName : attackNames) {
            JButton attackButton = createActionButton(attackName);
            actionPanel.add(attackButton);
        }
        gbc.gridx = 0;
        gbc.gridy = 2;
        gbc.gridwidth = 2; // Melintasi dua kolom
        gbc.weightx = 0.5; // Agar panel tombol bisa mengisi horizontal jika perlu
        gbc.weighty = 0;
        gbc.anchor = GridBagConstraints.SOUTHEAST;
        gbc.fill = GridBagConstraints.NONE; // Tidak mengisi, hanya seukuran preferensinya
        gbc.insets = new Insets(0, 0, 10, 0); // Padding atas dan bawah untuk panel tombol
        mainPanel.add(actionPanel, gbc);

        backgroundContentPane.add(mainPanel, BorderLayout.CENTER);
        updateHealthBars();
    }

    private JPanel createCharacterPanel(String characterName, boolean isEnemy) {
        JPanel panel = new JPanel();
        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
        panel.setOpaque(false); // Panel karakter utama transparan

        // --- Panel untuk HP (Teks HP di atas Bar HP) ---
        JPanel hpContainerPanel = new JPanel();
        hpContainerPanel.setLayout(new BoxLayout(hpContainerPanel, BoxLayout.Y_AXIS));
        hpContainerPanel.setOpaque(false);

        JLabel hpText = new JLabel("HP: " + (isEnemy ? enemyCurrentHP : playerCurrentHP) + "%");
        hpText.setFont(FontManager.NORMAL_FONT.deriveFont(12f)); // Ukuran font HP lebih kecil
        hpText.setForeground(Color.WHITE);
        hpText.setAlignmentX(Component.LEFT_ALIGNMENT); // Pusatkan teks HP

        JProgressBar healthBar = new JProgressBar(0, MAX_HP);
        healthBar.setUI(new BasicProgressBarUI());
        healthBar.setPreferredSize(new Dimension(100, 18)); // Ukuran HP bar disesuaikan target
        healthBar.setBackground(HP_BAR_BACKGROUND);
        healthBar.setOpaque(true);
        healthBar.setValue(isEnemy ? enemyCurrentHP : playerCurrentHP); // Set nilai awal
        setHealthBarColor(healthBar, isEnemy ? enemyCurrentHP : playerCurrentHP); // Set warna awal
        healthBar.setAlignmentX(Component.CENTER_ALIGNMENT); // Pusatkan HP bar

        hpContainerPanel.add(hpText);
        hpContainerPanel.add(Box.createRigidArea(new Dimension(0, 2))); // Spasi kecil antara teks dan bar
        hpContainerPanel.add(healthBar);
        hpContainerPanel.setAlignmentX(Component.CENTER_ALIGNMENT);
        panel.add(hpContainerPanel);

        // --- Sprite Container (Pokemon di atas Teratai) ---
        JLayeredPane spriteContainerPane = new JLayeredPane();
        spriteContainerPane.setOpaque(false);

        final int LILYPAD_TARGET_WIDTH = 130; // Ukuran target untuk teratai
        final int LILYPAD_TARGET_HEIGHT = 50;

        int actualPokemonWidth = 100;  // Default jika GIF gagal load
        int actualPokemonHeight = 100;

        // Ukuran container disesuaikan agar pokemon dan teratai pas
        JLabel pokemonSpriteLabel = new JLabel();
        pokemonSpriteLabel.setOpaque(false);
        String spritePath = isEnemy ? "/Assets/Pokemons/electivire.gif" : "/Assets/Pokemons/pikachu.gif";
        URL imageUrl = getClass().getResource(spritePath);
        ImageIcon pokemonIcon = null;

        if (imageUrl != null) {
            System.out.println("Memuat Pokemon GIF: " + spritePath);
            pokemonIcon = new ImageIcon(imageUrl);
            if (pokemonIcon.getIconWidth() > 0 && pokemonIcon.getIconHeight() > 0) {
                actualPokemonWidth = pokemonIcon.getIconWidth();
                actualPokemonHeight = pokemonIcon.getIconHeight();
                pokemonSpriteLabel.setIcon(pokemonIcon);
            } else {
                System.err.println("ImageIcon gagal memuat gambar dari URL: " + spritePath);
                pokemonSpriteLabel.setText(isEnemy ? "[M-LOAD-ERR]" : "[P-LOAD-ERR]");
            }
        } else {
            pokemonSpriteLabel.setText(isEnemy ? "[M-PATH-ERR]" : "[P-PATH-ERR]");
            System.err.println("Resource sprite Pokemon tidak ditemukan (URL null): " + spritePath);
        }

        // Ukuran container sekarang dihitung berdasarkan ukuran aktual Pokemon dan target teratai
        // Lebar container: maksimum dari lebar teratai atau lebar pokemon
        int containerWidth = Math.max(LILYPAD_TARGET_WIDTH, actualPokemonWidth);
        // Tinggi container: tinggi pokemon + bagian teratai yang terlihat di bawah pokemon
        // Misal, kita ingin sekitar 1/3 tinggi teratai terlihat di bawah pokemon
        int visibleLilypadHeightOverlap = LILYPAD_TARGET_HEIGHT / 3;
        int containerHeight = actualPokemonHeight + visibleLilypadHeightOverlap;
        spriteContainerPane.setPreferredSize(new Dimension(containerWidth, containerHeight));
        spriteContainerPane.setMinimumSize(new Dimension(containerWidth, containerHeight));


        JLabel lilypadLabel = new JLabel();
        lilypadLabel.setOpaque(false);
        String lilypadPath = "/Assets/terataiPlayer.png";
        URL lilypadUrl = getClass().getResource(lilypadPath);
        if (lilypadUrl != null) {
            try {
                Image lilypadImage = ImageIO.read(lilypadUrl);
                if (lilypadImage != null) {
                    Image lilypadScaledImg = lilypadImage.getScaledInstance(LILYPAD_TARGET_WIDTH, LILYPAD_TARGET_HEIGHT, Image.SCALE_SMOOTH);
                    lilypadLabel.setIcon(new ImageIcon(lilypadScaledImg));
                } else {
                    System.err.println("ImageIO.read mengembalikan null untuk teratai: " + lilypadPath);
                    lilypadLabel.setText("[T-LOAD-ERR]");
                }
            } catch (IOException e) {
                lilypadLabel.setText("[LT-IO-ERR]");
                e.printStackTrace();
            }
        } else {
            lilypadLabel.setText("[T-PATH-ERR]");
            System.err.println("Resource teratai tidak ditemukan (URL null): " + lilypadPath);
        }

        // Posisikan teratai:
        // X: di tengah container
        int lilypadX = (containerWidth - LILYPAD_TARGET_WIDTH) / 2;
        // Y: di bagian bawah container
        int lilypadY = containerHeight - LILYPAD_TARGET_HEIGHT;
        lilypadLabel.setBounds(lilypadX, lilypadY, LILYPAD_TARGET_WIDTH, LILYPAD_TARGET_HEIGHT);

        // Posisikan Pokemon:
        // X: di tengah container
        int pokemonX = (containerWidth - actualPokemonWidth) / 2;
        // Y: di bagian atas container, agar bisa menimpa teratai
        int pokemonY = 0;
        pokemonSpriteLabel.setBounds(pokemonX, pokemonY, actualPokemonWidth, actualPokemonHeight);

        spriteContainerPane.add(lilypadLabel, Integer.valueOf(0));
        spriteContainerPane.add(pokemonSpriteLabel, Integer.valueOf(1));
        spriteContainerPane.setAlignmentX(Component.CENTER_ALIGNMENT);
        panel.add(Box.createVerticalStrut(5));
        panel.add(spriteContainerPane);


        // --- Label Nama Karakter ---
        JLabel nameLabel = new JLabel(characterName, SwingConstants.CENTER);
        nameLabel.setFont(FontManager.NORMAL_FONT.deriveFont(14f)); // Ukuran font nama
        nameLabel.setForeground(CHARACTER_NAME_FG);
        nameLabel.setBackground(isEnemy ? CHARACTER_NAME_BG_ENEMY : CHARACTER_NAME_BG_PLAYER);
        nameLabel.setOpaque(true); // Penting agar background terlihat
        // Border disesuaikan agar lebih mirip target (radius lebih besar)
        nameLabel.setBorder(new RoundedBorder(30, isEnemy ? CHARACTER_NAME_BG_ENEMY : CHARACTER_NAME_BG_PLAYER, 1, isEnemy ? Color.GRAY: Color.DARK_GRAY));
        nameLabel.setPreferredSize(new Dimension(120, 35)); // Ukuran label nama disesuaikan
        nameLabel.setMinimumSize(new Dimension(120,35)); // Agar tidak terlalu kecil
        nameLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
        panel.add(Box.createVerticalStrut(5));
        panel.add(nameLabel);

        if (isEnemy) {
            enemyHealthTextLabel = hpText;
            enemyHealthBar = healthBar;
        } else {
            playerHealthTextLabel = hpText;
            playerHealthBar = healthBar;
        }
        return panel;
    }

    private JButton createActionButton(String actionName) {
        final int cornerRadius = 35; // Radius lebih besar untuk tombol lebih bulat (pill shape)

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

        button.setFont(FontManager.BUTTON_FONT); // Gunakan dari FontManager
        button.setBackground(BUTTON_BG_COLOR);
        button.setForeground(Color.DARK_GRAY); // Teks gelap di target

        button.setFocusPainted(false);
        // Border disesuaikan dengan target (warna garis border)
        button.setBorder(new RoundedBorder(cornerRadius, null, 2, new Color(70,120,30))); // Border lebih tebal dan warna berbeda

        button.setOpaque(false);
        button.setContentAreaFilled(false);

        button.setPreferredSize(new Dimension(160, 50)); // Ukuran tombol disesuaikan
        button.setCursor(new Cursor(Cursor.HAND_CURSOR));

        button.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                System.out.println(actionName + " digunakan!");
                int damageToEnemy = (int) (Math.random() * 11) + 10; // Antara 10 dan 20
                enemyCurrentHP -= damageToEnemy;
                if (enemyCurrentHP < 0) enemyCurrentHP = 0;

                if (enemyCurrentHP > 0) {
                    int damageToPlayer = (int) (Math.random() * 11) + 5; // Antara 5 dan 15
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
        if (percentage > 0.6f) {
            healthBar.setForeground(HP_BAR_COLOR_GREEN);
        } else if (percentage > 0.3f) {
            healthBar.setForeground(HP_BAR_COLOR_YELLOW);
        } else {
            healthBar.setForeground(HP_BAR_COLOR_RED);
        }
        healthBar.repaint();
    }

    private void checkGameEnd() {
        String message1 = "";
        String message2 = "";
        if (playerCurrentHP <= 0 && enemyCurrentHP <= 0) {
            message1 = "Seri! Kedua petarung kalah!";
        } else if (playerCurrentHP <= 0) {
            message1 = "KAMU KALAH! Musuh menang.";
        } else if (enemyCurrentHP <= 0) {
            message1 = "KAMU MENANG!";
            message2 = "Musuh telah dikalahkan.";
        }

        if (!message1.isEmpty()) {
            // Logika untuk menonaktifkan tombol (sudah ada dan seharusnya berfungsi)
            Component mainPanelComponent = getContentPane().getComponent(0);
            if (mainPanelComponent instanceof JPanel) {
                JPanel actualMainPanel = (JPanel) mainPanelComponent;
                for(Component potentialActionPanel : actualMainPanel.getComponents()){
                    if(potentialActionPanel instanceof JPanel && ((JPanel) potentialActionPanel).getLayout() instanceof GridLayout){
                        boolean allButtons = true;
                        int buttonCount = 0;
                        for(Component comp : ((JPanel) potentialActionPanel).getComponents()){
                            if(!(comp instanceof JButton)){ allButtons = false; break; }
                            buttonCount++;
                        }
                        if(allButtons && buttonCount == 4){
                            for (Component comp : ((JPanel) potentialActionPanel).getComponents()) {
                                if (comp instanceof JButton) { comp.setEnabled(false); }
                            }
                            break;
                        }
                    }
                }
            }
            // Update status teks utama
            // Asumsi statusTextPanel adalah komponen pertama di mainPanel
            if (mainPanel.getComponentCount() > 0 && mainPanel.getComponent(0) instanceof JPanel) {
                JPanel statusPanel = (JPanel) mainPanel.getComponent(0);
                if (statusPanel.getComponentCount() == 2) {
                    ((JLabel)statusPanel.getComponent(0)).setText(message1.toUpperCase());
                    ((JLabel)statusPanel.getComponent(1)).setText(message2.toUpperCase());
                }
            }
            JOptionPane.showMessageDialog(this, message1, "Permainan Selesai", JOptionPane.INFORMATION_MESSAGE);
        }
    }

    private static class RoundedBorder extends AbstractBorder {
        private int radius;
        private Color backgroundColor; // Tidak lagi digunakan untuk fill oleh border ini
        private int thickness;
        private Color borderColor;

        public RoundedBorder(int radius, Color backgroundColor, int thickness, Color borderColor) {
            this.radius = radius;
            this.backgroundColor = backgroundColor;
            this.thickness = thickness;
            this.borderColor = borderColor;
        }

        @Override
        public void paintBorder(Component c, Graphics g, int x, int y, int width, int height) {
            Graphics2D g2d = (Graphics2D) g.create();
            g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            Shape outer = new RoundRectangle2D.Float(x, y, width - 1, height - 1, radius, radius);
            if (thickness > 0 && borderColor != null) {
                // Untuk border yang lebih baik, gambar bentuk luar lalu kurangi bentuk dalam
                // atau gambar garis dengan ketebalan tertentu
                g2d.setColor(borderColor);
                g2d.setStroke(new BasicStroke(thickness)); // Atur ketebalan garis
                g2d.draw(outer); // Gambar garis luar
            }
            g2d.dispose();
        }
        @Override
        public Insets getBorderInsets(Component c) {
            return new Insets(thickness + radius/3, thickness + radius/3, thickness + radius/3, thickness + radius/3); // Disesuaikan
        }
        @Override
        public Insets getBorderInsets(Component c, Insets insets) {
            insets.left = insets.top = insets.right = insets.bottom = thickness + radius/3; // Disesuaikan
            return insets;
        }
        @Override
        public boolean isBorderOpaque() { return false; } // Border tidak mengisi background
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