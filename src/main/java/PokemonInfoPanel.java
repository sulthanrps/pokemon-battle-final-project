import javax.swing.*;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;

public class PokemonInfoPanel extends JPanel {
    private Pokemon pokemon;
    private BufferedImage originalCardFrameImage;
    private BufferedImage scaledCardFrameImage;
    private BufferedImage typeSymbolImage;

    private JLabel pokemonGifLabel;
    private ImageIcon pokemonGifIcon;

    private static final String CARD_FRAME_PATH = "/Assets/Showcase/card.png";

    public static final int TARGET_CARD_WIDTH = 600;
    public static final int TARGET_CARD_HEIGHT = 300;

    private static final Map<String, String> typeSymbolPaths = new HashMap<>();
    static {
        typeSymbolPaths.put("electric", "/Assets/Showcase/electric.png");
        typeSymbolPaths.put("fire", "/Assets/Showcase/api.png");
        typeSymbolPaths.put("water", "/Assets/Showcase/air.png");
        typeSymbolPaths.put("grass", "/Assets/Showcase/daun.png");
        typeSymbolPaths.put("normal", "/Assets/Showcase/bintang.png");
    }

    private static final int FRAME_BORDER_LEFT = 18;
    private static final int FRAME_BORDER_TOP = 20;
    private static final int FRAME_BORDER_RIGHT = 18;

    private static final Map<String, String> pokemonGifPaths = new HashMap<>();
    static {
        pokemonGifPaths.put("pikachu", "/Assets/Showcase/pikachu.gif");
        pokemonGifPaths.put("charizard", "/Assets/Showcase/charizard.gif");
        pokemonGifPaths.put("arceus", "/Assets/Showcase/arceus.gif");
    }

    private static final int NAME_X = FRAME_BORDER_LEFT + 50;
    private static final int NAME_Y = FRAME_BORDER_TOP + 100;

    private static final int STAT_X = FRAME_BORDER_LEFT + 50;
    private static final int STAT_HEALTH_Y = NAME_Y + 40;
    private static final int STAT_LINE_SPACING = 25;

    private static final int GIF_X = 300;
    private static final int GIF_Y = 70;
    private static final int GIF_WIDTH = 200;
    private static final int GIF_HEIGHT = 200;

    private static final int TYPE_SYMBOL_WIDTH = 45;
    private static final int TYPE_SYMBOL_HEIGHT = 45;
    private static final int TYPE_SYMBOL_MARGIN_FROM_WHITE_AREA_RIGHT = 50;
    private static final int TYPE_SYMBOL_MARGIN_FROM_WHITE_AREA_TOP = 70;

    private static final float CARD_NAME_FONT_SIZE = 25f;
    private static final float CARD_STAT_FONT_SIZE = 18f;

    public PokemonInfoPanel(Pokemon pokemon) {
        this.pokemon = pokemon;
        this.originalCardFrameImage = ImageLoader.loadImage(CARD_FRAME_PATH);
        setLayout(null);
        setOpaque(false);

        if (this.originalCardFrameImage != null) {
            Image tempScaledFrame = originalCardFrameImage.getScaledInstance(TARGET_CARD_WIDTH, TARGET_CARD_HEIGHT, Image.SCALE_SMOOTH);
            scaledCardFrameImage = new BufferedImage(TARGET_CARD_WIDTH, TARGET_CARD_HEIGHT, BufferedImage.TYPE_INT_ARGB);
            Graphics2D g2d = scaledCardFrameImage.createGraphics();
            g2d.drawImage(tempScaledFrame, 0, 0, null);
            g2d.dispose();

            setPreferredSize(new Dimension(TARGET_CARD_WIDTH, TARGET_CARD_HEIGHT));
        } else {
            setPreferredSize(new Dimension(TARGET_CARD_WIDTH, TARGET_CARD_HEIGHT)); // Fallback size
            System.err.println("Frame kartu gagal dimuat! Menggunakan ukuran target default.");
        }

        String typeKey = pokemon.getType().toString().toLowerCase();
        String symbolPath = typeSymbolPaths.get(typeKey);
        if (symbolPath != null) {
            System.out.println("Mencoba memuat simbol untuk tipe '" + typeKey + "' dari path: " + symbolPath);
            this.typeSymbolImage = ImageLoader.loadImage(symbolPath);
            if (this.typeSymbolImage == null) {
                System.err.println("Gagal memuat gambar simbol tipe dari: " + symbolPath);
            }
        } else {
            System.err.println("Path simbol untuk tipe '" + typeKey + "' tidak ditemukan di map.");
        }
        setOpaque(false);

        String pokemonNameKey = pokemon.getName().toLowerCase();
        String gifPath = pokemonGifPaths.get(pokemonNameKey);

        if (gifPath != null && !gifPath.isEmpty()) {
            try {
                URL gifUrl = getClass().getResource(gifPath);
                if (gifUrl != null) {
                    pokemonGifIcon = new ImageIcon(gifUrl);
                    pokemonGifLabel = new JLabel(pokemonGifIcon);

                    System.out.println("PokemonInfoPanel (" + pokemon.getName() + "): Setting GIF Label bounds to X=" + GIF_X + ", Y=" + GIF_Y);
                    pokemonGifLabel.setBounds(GIF_X, GIF_Y, GIF_WIDTH, GIF_HEIGHT);

                    add(pokemonGifLabel);
                } else {
                    System.err.println("File GIF Pokémon tidak ditemukan di path (dari map): " + gifPath);
                    addPlaceholderGifLabel("No GIF Path", 0, 0);
                }
            } catch (Exception e) {
                System.err.println("Error memuat GIF Pokémon: " + e.getMessage());
                addPlaceholderGifLabel("GIF Error", 0, 0);
            }
        } else {
            System.err.println("Path GIF untuk " + pokemon.getName() + " tidak ada di map pokemonGifPaths.");
            addPlaceholderGifLabel("No GIF", 0, 0);
        }
    }

    private void addPlaceholderGifLabel(String message, int x, int y) {
        if (pokemonGifLabel == null) {
            pokemonGifLabel = new JLabel(message, SwingConstants.CENTER);
            pokemonGifLabel.setBounds(GIF_X, GIF_Y, GIF_WIDTH, GIF_HEIGHT);
            pokemonGifLabel.setBorder(BorderFactory.createLineBorder(Color.GRAY));
            pokemonGifLabel.setFont(new Font("SansSerif", Font.PLAIN, 10));
            add(pokemonGifLabel);
        }
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        Graphics2D g2d = (Graphics2D) g.create();
        g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g2d.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
        g2d.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BILINEAR);

        if (scaledCardFrameImage != null) {
            g2d.drawImage(scaledCardFrameImage, 0, 0, TARGET_CARD_WIDTH, TARGET_CARD_HEIGHT, this);
        } else {
            g2d.setColor(Color.LIGHT_GRAY);
            g2d.fillRect(0, 0, getWidth(), getHeight());
            g2d.setColor(Color.RED);
            g2d.drawString("Frame Error", 10, 20);
        }

        Font basePokemonNameFont = FontManager.BUTTON_FONT;
        Font basePokemonStatFont = FontManager.NORMAL_FONT;

        Font pokemonNameFont = (basePokemonNameFont != null) ? basePokemonNameFont.deriveFont(CARD_NAME_FONT_SIZE) : new Font("SansSerif", Font.BOLD, (int)CARD_NAME_FONT_SIZE);
        Font pokemonStatFont = (basePokemonStatFont != null) ? basePokemonStatFont.deriveFont(CARD_STAT_FONT_SIZE) : new Font("SansSerif", Font.PLAIN, (int)CARD_STAT_FONT_SIZE);

        g2d.setColor(Color.BLACK);

        if (pokemon != null && pokemon.getName() != null) {
            g2d.setFont(pokemonNameFont);
            g2d.drawString(pokemon.getName().toUpperCase(), NAME_X, NAME_Y);
        }

        if (pokemon != null) {
            g2d.setFont(pokemonStatFont);
            int currentStatY = STAT_HEALTH_Y;
            g2d.drawString("Health: " + pokemon.getHealth(), STAT_X, currentStatY);
            currentStatY += STAT_LINE_SPACING;
            g2d.drawString("Attack: " + pokemon.getAttack(), STAT_X, currentStatY);
            currentStatY += STAT_LINE_SPACING;
            g2d.drawString("Defense: " + pokemon.getDefense(), STAT_X, currentStatY);
            currentStatY += STAT_LINE_SPACING;
            g2d.drawString("Type: " + pokemon.getType(), STAT_X, currentStatY);
        }

        if (typeSymbolImage != null) {
            int typeSymbolX = TARGET_CARD_WIDTH - FRAME_BORDER_RIGHT - TYPE_SYMBOL_MARGIN_FROM_WHITE_AREA_RIGHT - TYPE_SYMBOL_WIDTH;
            int typeSymbolY = FRAME_BORDER_TOP + TYPE_SYMBOL_MARGIN_FROM_WHITE_AREA_TOP;

            g2d.drawImage(typeSymbolImage, typeSymbolX, typeSymbolY, TYPE_SYMBOL_WIDTH, TYPE_SYMBOL_HEIGHT, this);
        }
        g2d.dispose();
    }
}