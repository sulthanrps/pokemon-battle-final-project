import javax.swing.*;
import java.awt.*;
import javax.sound.sampled.*;
import java.io.IOException;
import java.net.URL;

public class GameWindow extends JFrame {

    public static final String GAME_TITLE = "Pokemon SDG'S";
    private JPanel currentPanel;

    private Clip backgroundMusicClip;

    private static final String BACKGROUND_MUSIC_PATH = "/assets/music/background_music.wav";

    public GameWindow() {
        super(GAME_TITLE);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

        setLocationRelativeTo(null);
        setExtendedState(JFrame.MAXIMIZED_BOTH);

        OnboardingScreen onboardingScreen = new OnboardingScreen(this);
        currentPanel = onboardingScreen;
        add(currentPanel);

        playBackgroundMusic(BACKGROUND_MUSIC_PATH);
//        setSize(700, 600);
        setLocationRelativeTo(null);
        setExtendedState(JFrame.MAXIMIZED_BOTH);
        OnboardingScreen onboardingScreen = new OnboardingScreen(this);
        currentPanel = onboardingScreen;
        add(currentPanel);

        setVisible(true);
    }

    private void playBackgroundMusic(String filePath) {
        new Thread(() -> {
            try {
                URL musicURL = getClass().getResource(filePath);
                if (musicURL == null) {
                    System.err.println("File musik tidak ditemukan di: " + filePath);
                    return;
                }

                AudioInputStream audioStream = AudioSystem.getAudioInputStream(musicURL);
                backgroundMusicClip = AudioSystem.getClip();
                backgroundMusicClip.open(audioStream);

                backgroundMusicClip.loop(Clip.LOOP_CONTINUOUSLY);

                backgroundMusicClip.start();

                System.out.println("Musik latar dimulai: " + filePath);

            } catch (UnsupportedAudioFileException e) {
                System.err.println("Format file musik tidak didukung: " + e.getMessage());
                e.printStackTrace();
            } catch (IOException e) {
                System.err.println("Error I/O saat memutar musik: " + e.getMessage());
                e.printStackTrace();
            } catch (LineUnavailableException e) {
                System.err.println("Line untuk audio tidak tersedia: " + e.getMessage());
                e.printStackTrace();
            } catch (Exception e) {
                System.err.println("Terjadi kesalahan tak terduga saat memutar musik: " + e.getMessage());
                e.printStackTrace();
            }
        }).start();
    }

    public void stopBackgroundMusic() {
        if (backgroundMusicClip != null && backgroundMusicClip.isRunning()) {
            backgroundMusicClip.stop();
            backgroundMusicClip.close();
            System.out.println("Musik latar dihentikan.");
        }
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

    public void startGame() {
        MapLoader mapLoaderPanel = new MapLoader(this);
        switchPanel(mapLoaderPanel);
        mapLoaderPanel.startGameThread();
    }
}