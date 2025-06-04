package util;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.io.InputStream; // Untuk memuat resource dari dalam JAR

public class ImageLoader {

    /**
     * Memuat gambar dari path yang diberikan (relatif terhadap classpath).
     * @param path Path ke file gambar (e.g., "/assets/images/logo.png")
     * @return BufferedImage jika berhasil, atau null jika gagal.
     */
    public static BufferedImage loadImage(String path) {
        try {
            // Menggunakan getResourceAsStream untuk kompatibilitas saat di-package menjadi JAR
            InputStream inputStream = ImageLoader.class.getResourceAsStream(path);
            if (inputStream == null) {
                System.err.println("Error: Tidak dapat menemukan resource gambar di: " + path);
                return null;
            }
            return ImageIO.read(inputStream);
        } catch (IOException e) {
            System.err.println("Error memuat gambar dari path: " + path);
            e.printStackTrace(); // Mencetak detail error
            return null;
        }
    }
}