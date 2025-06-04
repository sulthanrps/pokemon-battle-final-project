package showcase;

import javax.swing.*;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.util.HashMap;
import java.util.Map;
import util.ImageLoader;
import util.FontManager;

public class PokemonInfoPanel extends JPanel {

    private PokemonData pokemon;
    private BufferedImage cardFrameImage;
    private BufferedImage typeSymbolImage;

    private static final String CARD_FRAME_PATH = "/Assets/Showcase/card.png";

    private static final Map<String, String> typeSymbolPaths = new HashMap<>();
    static {
        typeSymbolPaths.put("Electric", "/Assets/Showcase/electric.jpg");
        typeSymbolPaths.put("Fire", "/Assets/Showcase/api.jpg");
        typeSymbolPaths.put("Water", "/Assets/Showcase/air.jpg");
        typeSymbolPaths.put("Grass", "/Assets/Showcase/daun.jpg");
        typeSymbolPaths.put("Normal", "/Assets/Showcase/bintang.jpg");
    }

    private static final int NAME_X = 25;
    private static final int NAME_Y = 45;

    private static final int STAT_X = 25;
    private static final int STAT_HEALTH_Y = 80;
    private static final int STAT_LINE_SPACING = 22;

    // Posisi dan Ukuran untuk Gambar Utama Pokémon
    // private static final int POKEMON_IMG_X = 160;
    // private static final int POKEMON_IMG_Y = 60;
    // private static final int POKEMON_IMG_WIDTH = 80;
    // private static final int POKEMON_IMG_HEIGHT = 80;

    // Simbol elemen
    private static final int TYPE_SYMBOL_WIDTH = 28;
    private static final int TYPE_SYMBOL_HEIGHT = 28;
    private static final int TYPE_SYMBOL_MARGIN_FROM_RIGHT_EDGE = 18;
    private static final int TYPE_SYMBOL_MARGIN_FROM_TOP_EDGE = 12;

    public PokemonInfoPanel(PokemonData pokemon) {
        this.pokemon = pokemon;
        this.cardFrameImage = ImageLoader.loadImage(CARD_FRAME_PATH);

        String symbolPath = typeSymbolPaths.get(pokemon.getType());
        if (symbolPath != null) {
            this.typeSymbolImage = ImageLoader.loadImage(symbolPath);
        } else {
            System.err.println("Simbol untuk tipe " + pokemon.getType() + " tidak ditemukan.");
        }

        if (cardFrameImage != null) {
            setPreferredSize(new Dimension(cardFrameImage.getWidth(), cardFrameImage.getHeight()));
        } else {
            setPreferredSize(new Dimension(260, 190));
            System.err.println("Frame kartu gagal dimuat! Menggunakan ukuran default.");
        }
        setOpaque(false);
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        Graphics2D g2d = (Graphics2D) g.create();
        g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g2d.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);

        if (cardFrameImage != null) {
            g2d.drawImage(cardFrameImage, 0, 0, getWidth(), getHeight(), this);
        } else {
            g2d.setColor(Color.LIGHT_GRAY);
            g2d.fillRect(0,0, getWidth(), getHeight());
            g2d.setColor(Color.RED);
            g2d.drawString("Frame Gagal", 10, 20);
        }

        Font pokemonNameFont = FontManager.BUTTON_FONT;
        Font pokemonStatFont = FontManager.NORMAL_FONT;

        g2d.setColor(Color.BLACK);

        if (pokemonNameFont != null) g2d.setFont(pokemonNameFont);
        g2d.drawString(pokemon.getName().toUpperCase(), NAME_X, NAME_Y);

        if (pokemonStatFont != null) g2d.setFont(pokemonStatFont);
        int currentStatY = STAT_HEALTH_Y;

        // Dapatkan FontMetrics untuk penempatan yang lebih baik jika perlu (misal, rata kanan untuk nilai)
        // FontMetrics fmStat = g2d.getFontMetrics(pokemonStatFont);

        g2d.drawString("Health: " + pokemon.getHealthValue(), STAT_X, currentStatY);
        currentStatY += STAT_LINE_SPACING;
        g2d.drawString("Attack: " + pokemon.getAttack(), STAT_X, currentStatY);
        currentStatY += STAT_LINE_SPACING;
        g2d.drawString("Defense: " + pokemon.getDefense(), STAT_X, currentStatY);

        // 3. Gambar Pokémon Utama (Area ini sekarang kosong atau bisa diisi placeholder)
        // Karena tidak ada imagePath dari JSON, kita tidak menggambar pokemonDisplayImage
        // Jika ingin ada placeholder:
        /*
        if (POKEMON_IMG_WIDTH > 0 && POKEMON_IMG_HEIGHT > 0) { // Cek jika dimensi placeholder valid
            g2d.setColor(Color.LIGHT_GRAY);
            // g2d.fillRect(POKEMON_IMG_X, POKEMON_IMG_Y, POKEMON_IMG_WIDTH, POKEMON_IMG_HEIGHT);
            g2d.drawRect(POKEMON_IMG_X, POKEMON_IMG_Y, POKEMON_IMG_WIDTH, POKEMON_IMG_HEIGHT);
            g2d.setColor(Color.DARK_GRAY);
            if (pokemonStatFont != null) g2d.setFont(pokemonStatFont.deriveFont(10f)); // Font kecil
            // String noImgText = "Gambar";
            // int textWidth = fmStat.deriveFont(10f).stringWidth(noImgText);
            // g2d.drawString(noImgText, POKEMON_IMG_X + (POKEMON_IMG_WIDTH - textWidth) / 2, POKEMON_IMG_Y + POKEMON_IMG_HEIGHT / 2);
        }
        */

        // Simbol Elemen
        if (typeSymbolImage != null) {
            int typeSymbolX = getWidth() - TYPE_SYMBOL_MARGIN_FROM_RIGHT_EDGE - TYPE_SYMBOL_WIDTH;
            int typeSymbolY = TYPE_SYMBOL_MARGIN_FROM_TOP_EDGE;

            g2d.drawImage(typeSymbolImage, typeSymbolX, typeSymbolY, TYPE_SYMBOL_WIDTH, TYPE_SYMBOL_HEIGHT, this);
        }
        g2d.dispose();
    }
}
