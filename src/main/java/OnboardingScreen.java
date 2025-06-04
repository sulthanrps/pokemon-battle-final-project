import javax.swing.*;
import java.awt.*;
import java.awt.image.BufferedImage;

public class OnboardingScreen extends JPanel {
    private BufferedImage backgroundImage;
    private BufferedImage pokemonLogoImage;
    private BufferedImage creditsPanelImage;
    private BufferedImage playButtonOriginalImage;
    private JButton playGameButton;
    private GameWindow gameWindow;

    private static final String BACKGROUND_IMG_PATH = "/Assets/Onboarding/bg_onboard.png";
    private static final String LOGO_IMG_PATH = "/Assets/Onboarding/logo.png";
    private static final String CREDITS_PANEL_IMG_PATH = "/Assets/Onboarding/creditspanel.png";
    private static final String PLAY_BUTTON_IMG_PATH = "/Assets/Onboarding/buttonbermain.png";

    private static final int TARGET_LOGO_WIDTH = 560;
    private static final int TARGET_LOGO_HEIGHT = 216;
    private static final int LOGO_MARGIN_TOP = 40;

    private static final int TARGET_CREDITS_WIDTH = 300;
    private static final int TARGET_CREDITS_HEIGHT = 170;
    private static final double CREDITS_PANEL_VERTICAL_POSITION_FACTOR = 0.42;

    private static final int TARGET_BUTTON_WIDTH = 240;
    private static final int TARGET_BUTTON_HEIGHT = 84;
    private static final int BUTTON_MARGIN_BOTTOM = 100;

    public OnboardingScreen(GameWindow window) {
        this.gameWindow = window;
        setOpaque(true);

        backgroundImage = ImageLoader.loadImage(BACKGROUND_IMG_PATH);
        pokemonLogoImage = ImageLoader.loadImage(LOGO_IMG_PATH);
        creditsPanelImage = ImageLoader.loadImage(CREDITS_PANEL_IMG_PATH);
        playButtonOriginalImage = ImageLoader.loadImage(PLAY_BUTTON_IMG_PATH);

        // Error checking
        if (backgroundImage == null) System.err.println("Gagal memuat: " + BACKGROUND_IMG_PATH);
        if (pokemonLogoImage == null) System.err.println("Gagal memuat: " + LOGO_IMG_PATH);
        if (creditsPanelImage == null) System.err.println("Gagal memuat: " + CREDITS_PANEL_IMG_PATH);
        if (playButtonOriginalImage == null) System.err.println("Gagal memuat: " + PLAY_BUTTON_IMG_PATH);

        setLayout(null);

        if (playButtonOriginalImage != null) {
            Image scaledButtonImage = playButtonOriginalImage.getScaledInstance(
                    TARGET_BUTTON_WIDTH, TARGET_BUTTON_HEIGHT, Image.SCALE_SMOOTH);

            playGameButton = new JButton(new ImageIcon(scaledButtonImage));
            playGameButton.setBorder(BorderFactory.createEmptyBorder());
            playGameButton.setContentAreaFilled(false);
            playGameButton.setFocusPainted(false);
            playGameButton.setCursor(new Cursor(Cursor.HAND_CURSOR));
            playGameButton.setSize(TARGET_BUTTON_WIDTH, TARGET_BUTTON_HEIGHT);
            playGameButton.addActionListener(e -> {
                if (gameWindow != null) {
                    gameWindow.startGame();
                }
            });
            add(playGameButton);
        }
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        Graphics2D g2d = (Graphics2D) g.create();
        g2d.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BILINEAR);
        g2d.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);
        g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        int panelWidth = getWidth();
        int panelHeight = getHeight();

        // Background Image
        if (backgroundImage != null) {
            g2d.drawImage(backgroundImage, 0, 0, panelWidth, panelHeight, this);
        } else {
            g2d.setColor(Color.GRAY);
            g2d.fillRect(0, 0, panelWidth, panelHeight);
            g2d.setColor(Color.WHITE);
            g2d.drawString("Gagal memuat background image.", 20, 20);
        }

        // Logo Pokemon
        if (pokemonLogoImage != null) {
            int logoActualHeight = TARGET_LOGO_HEIGHT;
            if (TARGET_LOGO_HEIGHT <= 0) {
                logoActualHeight = (int) (((double)pokemonLogoImage.getHeight() / pokemonLogoImage.getWidth()) * TARGET_LOGO_WIDTH);
            }
            int logoX = (panelWidth - TARGET_LOGO_WIDTH) / 2;
            int logoY = LOGO_MARGIN_TOP;
            g2d.drawImage(pokemonLogoImage, logoX, logoY, TARGET_LOGO_WIDTH, logoActualHeight, this);
        }

        // Credits Panel
        if (creditsPanelImage != null) {
            int creditsActualHeight = TARGET_CREDITS_HEIGHT;
            if (TARGET_CREDITS_HEIGHT <= 0) {
                creditsActualHeight = (int) (((double)creditsPanelImage.getHeight() / creditsPanelImage.getWidth()) * TARGET_CREDITS_WIDTH);
            }
            int creditsImgX = (panelWidth - TARGET_CREDITS_WIDTH) / 2;
            int creditsCenterY = (int) (panelHeight - CREDITS_PANEL_VERTICAL_POSITION_FACTOR) / 2;
            int creditsImgY = creditsCenterY - (creditsActualHeight / 2);
            g2d.drawImage(creditsPanelImage, creditsImgX, creditsImgY, TARGET_CREDITS_WIDTH, creditsActualHeight, this);
        }

        // Button play
        if (playGameButton != null) {
            int buttonX = (panelWidth - TARGET_BUTTON_WIDTH) / 2;
            int buttonY = panelHeight - TARGET_BUTTON_HEIGHT - BUTTON_MARGIN_BOTTOM;
            playGameButton.setBounds(buttonX, buttonY, TARGET_BUTTON_WIDTH, TARGET_BUTTON_HEIGHT);
        }

        g2d.dispose();
    }
}