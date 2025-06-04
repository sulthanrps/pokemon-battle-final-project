package util;

import java.awt.Font;
import java.awt.FontFormatException;
import java.awt.GraphicsEnvironment;
import java.io.IOException;
import java.io.InputStream;

public class FontManager {
    private static Font basePixelifyRegular;
    private static Font basePixelifyBold;
    private static Font basePixelifySemiBold;
    private static Font basePixelifyMedium;

    public static final Font TITLE_FONT;
    public static final Font NORMAL_FONT;
    public static final Font BUTTON_FONT;

    static {
        String regularFontPath = "/fonts/PixelifySans-Regular.ttf";
        String boldFontPath = "/fonts/PixelifySans-Bold.ttf";
        String mediumFontPath = "/fonts/PixelifySans-Medium.ttf";
        String semiBoldFontPath = "/fonts/PixelifySans-SemiBold.ttf";

        try {
            // Memuat font regular
            InputStream regularStream = FontManager.class.getResourceAsStream(regularFontPath);
            if (regularStream == null) {
                throw new IOException("File font tidak ditemukan: " + regularFontPath);
            }
            // Buat font dari stream, dengan ukuran dasar awal (misalnya 12pt)
            basePixelifyRegular = Font.createFont(Font.TRUETYPE_FONT, regularStream).deriveFont(12f);
            GraphicsEnvironment.getLocalGraphicsEnvironment().registerFont(basePixelifyRegular);
            System.out.println("PixelifySans-Regular.ttf berhasil dimuat dan didaftarkan.");

            // Memuat font bold (jika ada)
            InputStream boldStream = FontManager.class.getResourceAsStream(boldFontPath);
            if (boldStream != null) {
                basePixelifyBold = Font.createFont(Font.TRUETYPE_FONT, boldStream).deriveFont(12f);
                GraphicsEnvironment.getLocalGraphicsEnvironment().registerFont(basePixelifyBold);
                System.out.println("PixelifySans-Bold.ttf berhasil dimuat dan didaftarkan.");
            } else {
                // Jika file bold tidak ditemukan, basePixelifyBold akan null
                // Kita akan menangani ini saat mendefinisikan konstanta font publik
                System.err.println("File font bold tidak ditemukan: " + boldFontPath + ". Akan menggunakan algorithmic bold jika style BOLD diminta dari font regular.");
                basePixelifyBold = null;
            }

            // Memuat font semibold (jika ada)
            InputStream semibBoldStream = FontManager.class.getResourceAsStream(semiBoldFontPath);
            if (semibBoldStream != null) {
                basePixelifySemiBold = Font.createFont(Font.TRUETYPE_FONT, semibBoldStream).deriveFont(12f);
                GraphicsEnvironment.getLocalGraphicsEnvironment().registerFont(basePixelifySemiBold);
                System.out.println("PixelifySans-SemiBold.ttf berhasil dimuat dan didaftarkan.");
            } else {
                // Jika file semibold tidak ditemukan, basePixelifySemiBold akan null
                // Kita akan menangani ini saat mendefinisikan konstanta font publik
                System.err.println("File font semibold tidak ditemukan: " + semiBoldFontPath + ". Akan menggunakan algorithmic semibold jika style semiBOLD diminta dari font regular.");
                basePixelifySemiBold = null;
            }

            // Memuat font medium (jika ada)
            InputStream mediumStream = FontManager.class.getResourceAsStream(mediumFontPath);
            if (mediumStream != null) {
                basePixelifyMedium = Font.createFont(Font.TRUETYPE_FONT, mediumStream).deriveFont(12f);
                GraphicsEnvironment.getLocalGraphicsEnvironment().registerFont(basePixelifyMedium);
                System.out.println("PixelifySans-Medium.ttf berhasil dimuat dan didaftarkan.");
            } else {
                // Jika file medium tidak ditemukan, basePixelifyMedium akan null
                // Kita akan menangani ini saat mendefinisikan konstanta font publik
                System.err.println("File font medium tidak ditemukan: " + mediumFontPath + ". Akan menggunakan algorithmic semibold jika style semiBOLD diminta dari font regular.");
                basePixelifyMedium = null;
            }

        } catch (IOException | FontFormatException e) {
            System.err.println("Error memuat font Pixelify Sans: " + e.getMessage());
            e.printStackTrace();
            // Fallback ke font default SansSerif jika terjadi error
        }

        // Mendefinisikan konstanta font publik
        // Jika basePixelifyBold berhasil dimuat, gunakan itu untuk style BOLD.
        // Jika tidak, gunakan basePixelifyRegular dan terapkan style Font.BOLD (algorithmic bold).
        Font titleBase = (basePixelifyBold != null) ? basePixelifyBold : basePixelifyRegular;
        TITLE_FONT = titleBase.deriveFont(Font.BOLD, 32f);

        NORMAL_FONT = basePixelifyRegular.deriveFont(Font.PLAIN, 18f);
        // Anda bisa memilih apakah label menggunakan versi regular atau bold:
        // LABEL_FONT = titleBase.deriveFont(Font.BOLD, 18f);


        Font buttonBase = (basePixelifyBold != null) ? basePixelifyBold : basePixelifyRegular;
        BUTTON_FONT = buttonBase.deriveFont(Font.BOLD, 18f);
    }

    private FontManager() {

    }
}


