import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.util.List;

public class ShowcaseScreen extends JPanel {
    private GameWindow gameWindow;
    private BufferedImage backgroundImage;
    private BufferedImage pokemonLogoSmallImage;
    private BufferedImage titleImage;
    private BufferedImage lanjutButtonImage;

    private JButton lanjutButton;
    private List<PokemonInfoPanel> pokemonCardPanels;

    private static final String BACKGROUND_IMG_PATH = "/Assets/Onboarding/bg_onboard.png";
    private static final String POKEMON_LOGO_PATH = "/Assets/Onboarding/logo.png";
    private static final String TITLE_IMG_PATH = "/Assets/Showcase/title.png";
    private static final String LANJUT_BUTTON_IMG_PATH = "/Assets/Showcase/buttonLanjut.png";
    private static final String POKEMON_DB_JSON_PATH = "/resources/pokemon-dataset.json";

    private static final int LOGO_SMALL_WIDTH = 125;
    private static final int LOGO_SMALL_HEIGHT = 50;
    private static final int LOGO_SMALL_X = 28;
    private static final int LOGO_SMALL_Y = 21;

    private static final int TITLE_WIDTH = 450;
    private static final int TITLE_HEIGHT = 80;
    private static final int TITLE_Y_OFFSET = LOGO_SMALL_Y + LOGO_SMALL_HEIGHT + 15;

    private static final int MAX_CARDS_TO_SHOW = 3;
    private static final int CARD_SPACING = 20;
    private static final int CARD_ROW_Y_TOP = TITLE_Y_OFFSET + TITLE_HEIGHT + 30;

    private static final int LANJUT_BUTTON_WIDTH = 240;
    private static final int LANJUT_BUTTON_HEIGHT = 80;
    private static final int LANJUT_BUTTON_MARGIN_RIGHT = 90;
    private static final int LANJUT_BUTTON_MARGIN_BOTTOM = 70;


    public ShowcaseScreen(GameWindow window) {
        this.gameWindow = window;
        setLayout(null);
        backgroundImage = ImageLoader.loadImage(BACKGROUND_IMG_PATH);
        setSize(700, 600);
        setOpaque(true);

        BufferedImage originalPokemonLogo = ImageLoader.loadImage(POKEMON_LOGO_PATH);
        if (originalPokemonLogo != null) {
            Image scaledLogo = originalPokemonLogo.getScaledInstance(LOGO_SMALL_WIDTH, LOGO_SMALL_HEIGHT, Image.SCALE_SMOOTH);
            pokemonLogoSmallImage = new BufferedImage(LOGO_SMALL_WIDTH, LOGO_SMALL_HEIGHT, BufferedImage.TYPE_INT_ARGB);
            Graphics2D g = pokemonLogoSmallImage.createGraphics();
            g.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BILINEAR);
            g.drawImage(scaledLogo, 0, 0, null);
            g.dispose();
        } else {
            System.err.println("Gagal memuat logo Pokémon utama: " + POKEMON_LOGO_PATH);
        }

        titleImage = ImageLoader.loadImage(TITLE_IMG_PATH);
        if (titleImage == null) {
            System.err.println("Gagal memuat gambar judul: " + TITLE_IMG_PATH);
        }

        lanjutButtonImage = ImageLoader.loadImage(LANJUT_BUTTON_IMG_PATH);
        if (lanjutButtonImage == null) {
            System.err.println("Gagal memuat gambar tombol LANJUT: " + LANJUT_BUTTON_IMG_PATH);
        }

        List<Pokemon> allPokemonData = GetPokemon.all();

        List<Pokemon> pokemonsToShow = new ArrayList<>();
        for (int i = 0; i < Math.min(allPokemonData.size(), MAX_CARDS_TO_SHOW); i++) {
            pokemonsToShow.add(allPokemonData.get(i));
        }
        if (pokemonsToShow.isEmpty() && !allPokemonData.isEmpty()) {
            System.err.println("Gagal mengambil data Pokémon spesifik, padahal data JSON ada.");
        } else if (allPokemonData.isEmpty()) {
            System.err.println("Tidak ada data Pokémon yang berhasil dimuat dari JSON.");
        }

        pokemonCardPanels = new ArrayList<>();
        for (Pokemon pd : pokemonsToShow) {
            PokemonInfoPanel cardPanel = new PokemonInfoPanel(pd);
            pokemonCardPanels.add(cardPanel);
            add(cardPanel);
        }

        if (lanjutButtonImage != null) {
            Image scaledLanjutBtn = lanjutButtonImage.getScaledInstance(LANJUT_BUTTON_WIDTH, LANJUT_BUTTON_HEIGHT, Image.SCALE_SMOOTH);
            lanjutButton = new JButton(new ImageIcon(scaledLanjutBtn));
            lanjutButton.setBorder(BorderFactory.createEmptyBorder());
            lanjutButton.setContentAreaFilled(false);
            lanjutButton.setFocusPainted(false);
            lanjutButton.setCursor(new Cursor(Cursor.HAND_CURSOR));
            lanjutButton.setSize(LANJUT_BUTTON_WIDTH, LANJUT_BUTTON_HEIGHT);

            lanjutButton.addActionListener(new ActionListener() {
                @Override
                public void actionPerformed(ActionEvent e) {
                    System.out.println("Tombol LANJUT diklik!");
                    if (gameWindow != null) {
                        PokemonBattleUI pokemonBattleUI = new PokemonBattleUI(gameWindow, new Pokemon());
                        gameWindow.switchPanel(pokemonBattleUI);
                        // Aksi selanjutnya, misalnya berpindah ke layar pemilihan karakter atau battle
                        // gameWindow.showCharacterSelectionScreen();
//                        JOptionPane.showMessageDialog(gameWindow,
//                                "Tombol LANJUT diklik! Fitur selanjutnya belum ada.",
//                                "Info Showcase",
//                                JOptionPane.INFORMATION_MESSAGE);
                    }
                }
            });
            add(lanjutButton);
        }
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        Graphics2D g2d = (Graphics2D) g.create();

        g2d.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BILINEAR);
        g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g2d.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);

        // Gambar Background
        if (backgroundImage != null) {
            g2d.drawImage(backgroundImage, 0, 0, getWidth(), getHeight(), this);
        } else {
            g2d.setColor(Color.DARK_GRAY); // Fallback jika background gagal dimuat
            g2d.fillRect(0, 0, getWidth(), getHeight());
        }

        // Gambar Logo Pokémon Kecil (kiri atas)
        if (pokemonLogoSmallImage != null) {
            g2d.drawImage(pokemonLogoSmallImage, LOGO_SMALL_X, LOGO_SMALL_Y, this);
        }

        // Gambar Judul "SHOWCASE POKEMON" (tengah atas, di bawah logo kecil)
        if (titleImage != null) {
            int titleX = (getWidth() - TITLE_WIDTH) / 2;
            g2d.drawImage(titleImage, titleX, TITLE_Y_OFFSET, TITLE_WIDTH, TITLE_HEIGHT, this);
        }

        // PokemonCardPanel
        if (pokemonCardPanels != null && !pokemonCardPanels.isEmpty()) {
            Dimension cardSize = pokemonCardPanels.get(0).getPreferredSize();
            int cardActualWidth = (int) cardSize.getWidth();
            int cardActualHeight = (int) cardSize.getHeight();

            int groupWidthTwoCards = (2 * cardActualWidth) + CARD_SPACING;
            int card1X = (getWidth() - groupWidthTwoCards) / 2;

            if (card1X < CARD_SPACING) card1X = CARD_SPACING;

            int cardRowYTopActual = CARD_ROW_Y_TOP;

            if(pokemonCardPanels.size() > 0) {
                pokemonCardPanels.get(0).setBounds(card1X, cardRowYTopActual, cardActualWidth, cardActualHeight);
            }

            if (pokemonCardPanels.size() > 1) {
                int card2X = card1X + cardActualWidth + CARD_SPACING;
                pokemonCardPanels.get(1).setBounds(card2X, cardRowYTopActual, cardActualWidth, cardActualHeight);
            }

            if (pokemonCardPanels.size() > 2) {
                int cardRowYBottom = cardRowYTopActual + cardActualHeight + CARD_SPACING;
                pokemonCardPanels.get(2).setBounds(card1X, cardRowYBottom, cardActualWidth, cardActualHeight);
            }
        }

        if (lanjutButton != null) {
            int buttonX = getWidth() - LANJUT_BUTTON_WIDTH - LANJUT_BUTTON_MARGIN_RIGHT;
            int buttonY = getHeight() - LANJUT_BUTTON_HEIGHT - LANJUT_BUTTON_MARGIN_BOTTOM;
            lanjutButton.setLocation(buttonX, buttonY);
        }

        g2d.dispose();
    }

//    public static void main(String[] args) {
//        SwingUtilities.invokeLater(() -> {
//            ShowcaseScreen game = new ShowcaseScreen(new GameWindow());
//            game.setVisible(true);
//        });
//    }
}