import javax.swing.*;
import java.awt.*;
import java.net.URL;

public class TampilkanGif extends JFrame {

    public TampilkanGif() {
        setTitle("Contoh Menampilkan GIF");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(400, 400); // Sesuaikan ukuran frame
        setLocationRelativeTo(null);
        setLayout(new FlowLayout()); // Atau layout manager lain yang sesuai

        JLabel labelUntukGif = new JLabel();
        labelUntukGif.setOpaque(false);
        JLabel labelStatus = new JLabel(); // Untuk pesan status

        // Ganti "/gifs/efek_serangan.gif" dengan path ke file GIF Anda di folder resources
        String pathGif = "/Assets/Pokemons/snorlax.gif";
        URL urlGif = getClass().getResource(pathGif);

        if (urlGif != null) {
            // Buat ImageIcon dari URL GIF
            ImageIcon iconAnimasi = new ImageIcon(urlGif);
            labelUntukGif.setIcon(iconAnimasi);

            // Opsional: Atur ukuran preferensi label sesuai ukuran GIF jika diperlukan
//             labelUntukGif.setPreferredSize(new Dimension((iconAnimasi.getIconWidth() * 2), (iconAnimasi.getIconHeight() * 2)));

            labelStatus.setText("GIF berhasil dimuat!");
        } else {
            String pesanError = "File GIF tidak ditemukan di: " + pathGif;
            labelUntukGif.setText(pesanError); // Tampilkan pesan error di label jika GIF tidak ada
            System.err.println(pesanError);
            labelStatus.setText("Gagal memuat GIF.");
        }

        add(labelUntukGif);
        add(labelStatus); // Tambahkan label status untuk informasi
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            new TampilkanGif().setVisible(true);
        });
    }
}