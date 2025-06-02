import javax.swing.*;
import java.awt.*;

class BackgroundPanel extends JPanel {
    private Image backgroundImage;

    public BackgroundPanel(String imagePath) {
        try {
            // Gunakan ClassLoader untuk resource di dalam JAR
            backgroundImage = new ImageIcon(getClass().getClassLoader().getResource(imagePath)).getImage();
            // Atau untuk path eksternal/langsung:
            // backgroundImage = new ImageIcon(imagePath).getImage();
        } catch (Exception e) {
            e.printStackTrace();
            System.err.println("Gagal memuat gambar background: " + imagePath);
        }
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g); // Penting!
        if (backgroundImage != null) {
            g.drawImage(backgroundImage, 0, 0, getWidth(), getHeight(), this);
        }
    }
}