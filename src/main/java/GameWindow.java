import javax.swing.*;
import java.awt.*;

public class GameWindow extends JFrame {

    public static final String GAME_TITLE = "Pokemon SDG'S";
    private JPanel currentPanel;

    public GameWindow() {
        super(GAME_TITLE);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(700, 600);
        setLocationRelativeTo(null);
        setExtendedState(JFrame.MAXIMIZED_BOTH);
        OnboardingScreen onboardingScreen = new OnboardingScreen(this);
        currentPanel = onboardingScreen;
        add(currentPanel);

//        ShowcaseScreen showcaseScreen = new ShowcaseScreen(this);
//        currentPanel = showcaseScreen;
//        add(currentPanel);

        setVisible(true);
    }

    public void switchPanel(JPanel newPanel) {
        if(newPanel == null) {
            System.err.println("Error: newPanel tidak boleh null di switchPanel.");
            return;
        }
        Container contentPane = getContentPane();

        contentPane.removeAll();

//        if(currentPanel != null){
//            contentPane.remove(currentPanel);
//        }

        currentPanel = newPanel;
        contentPane.add(currentPanel, BorderLayout.CENTER);

        contentPane.revalidate();
        contentPane.repaint();

        SwingUtilities.invokeLater(() -> {
            if(currentPanel != null) {
                currentPanel.requestFocusInWindow();
            }
        });
    }

    // Di dalam GameWindow.java, method startGame() atau yang serupa:
    public void startGame() { // Kirim 'this' (GameWindow)
        MapLoader mapLoaderPanel = new MapLoader(this);
        switchPanel(mapLoaderPanel); // Method switchPanel yang sudah ada
        mapLoaderPanel.startGameThread();

    }
}