import javax.swing.*;

public class MapPanel extends JPanel {
    GameWindow gameWindow;

    public MapPanel(GameWindow gameWindow) {
        this.gameWindow = gameWindow;
        SwingUtilities.invokeLater(() -> {
            JFrame window = new JFrame("Java Swing Game Sederhana");
            window.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
            window.setResizable(false);

            MapLoader gamePanel = new MapLoader(gameWindow);
            window.add(gamePanel);

            window.pack(); // Menyesuaikan ukuran window dengan preferredSize dari GamePanel
            window.setLocationRelativeTo(null); // Menampilkan window di tengah layar
            window.setVisible(true);

            gamePanel.startGameThread(); // Memulai game loop
            gamePanel.requestFocusInWindow(); // Agar KeyListener berfungsi
        });
    }
}