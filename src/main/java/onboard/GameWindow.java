package onboard;

import javax.swing.*;
import showcase.ShowcaseScreen;

public class GameWindow extends JFrame {

    public static final String GAME_TITLE = "Pokemon SDG'S";
    private OnboardingScreen onboardingScreen;

    public GameWindow() {
        super(GAME_TITLE);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

        setExtendedState(JFrame.MAXIMIZED_BOTH);
        onboardingScreen = new OnboardingScreen(this);
        add(onboardingScreen);

        setVisible(true);
    }

    public void switchPanel(JPanel newPanel) {
        getContentPane().removeAll();
        getContentPane().add(newPanel);
        revalidate();
        repaint();
    }

    // Di dalam GameWindow.java, method startGame() atau yang serupa:
    public void startGame() {
        ShowcaseScreen showcaseScreen = new ShowcaseScreen(this); // Kirim 'this' (GameWindow)
        switchPanel(showcaseScreen); // Method switchPanel yang sudah ada
    }
}