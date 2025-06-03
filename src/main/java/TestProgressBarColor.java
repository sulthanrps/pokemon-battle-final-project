import javax.swing.*;
import java.awt.*;

public class TestProgressBarColor {
    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            JFrame frame = new JFrame("Test Progress Bar Color");
            frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
            frame.setLayout(new FlowLayout());
            frame.setSize(400, 200);

            JProgressBar pbGreen = new JProgressBar(0, 100);
            pbGreen.setValue(75); // > 60%
            pbGreen.setForeground(Color.GREEN);
            pbGreen.setBackground(Color.LIGHT_GRAY);
            pbGreen.setOpaque(true);
            // pbGreen.setUI(new BasicProgressBarUI()); // Coba dengan atau tanpa ini
            frame.add(new JLabel("Green (75%):"));
            frame.add(pbGreen);

            JProgressBar pbYellow = new JProgressBar(0, 100);
            pbYellow.setValue(50); // > 30% and <= 60%
            pbYellow.setForeground(Color.YELLOW);
            pbYellow.setBackground(Color.LIGHT_GRAY);
            pbYellow.setOpaque(true);
            // pbYellow.setUI(new BasicProgressBarUI()); // Coba dengan atau tanpa ini
            frame.add(new JLabel("Yellow (50%):"));
            frame.add(pbYellow);

            JProgressBar pbRed = new JProgressBar(0, 100);
            pbRed.setValue(20); // <= 30%
            pbRed.setForeground(Color.RED);
            pbRed.setBackground(Color.LIGHT_GRAY);
            pbRed.setOpaque(true);
            // pbRed.setUI(new BasicProgressBarUI()); // Coba dengan atau tanpa ini
            frame.add(new JLabel("Red (20%):"));
            frame.add(pbRed);

            frame.setLocationRelativeTo(null);
            frame.setVisible(true);
        });
    }
}