import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.json.JSONTokener;

import java.io.*;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.nio.file.*; // Mengimpor semua dari java.nio.file
import java.util.ArrayList;
import java.util.List;

public class GetPokemon {
    private static final String DATA_FILE_NAME = "pokemon-dataset.json";
    // Path ke direktori 'data' di root project
    private static final Path DATA_DIR = Paths.get("data");
    private static final Path WRITE_PATH = DATA_DIR.resolve(DATA_FILE_NAME);

    public static ArrayList<Pokemon> all() {
        ArrayList<Pokemon> pokemonList = new ArrayList<>();
        InputStream inputStream = null;

        try {
            // Pastikan direktori 'data' ada
            if (!Files.exists(DATA_DIR)) {
                Files.createDirectories(DATA_DIR);
                System.out.println("Direktori 'data' dibuat di: " + DATA_DIR.toAbsolutePath());
            }

            // Prioritaskan membaca dari data/pokemon-dataset.json jika sudah ada dan tidak kosong
            if (Files.exists(WRITE_PATH) && Files.size(WRITE_PATH) > 0) {
                System.out.println("Membaca dari file: " + WRITE_PATH.toAbsolutePath());
                inputStream = Files.newInputStream(WRITE_PATH);
            } else {
                // Jika belum ada atau kosong, baca dari resources (hanya read-only)
                // dan salin ke data/pokemon-dataset.json untuk operasi selanjutnya
                if (Files.exists(WRITE_PATH) && Files.size(WRITE_PATH) == 0) {
                    System.out.println("File " + WRITE_PATH.toAbsolutePath() + " kosong. Mencoba memuat dari resources...");
                } else {
                    System.out.println("File " + WRITE_PATH.toAbsolutePath() + " tidak ditemukan. Mencoba membaca dari resources...");
                }

                URL resourceUrl = GetPokemon.class.getClassLoader().getResource(DATA_FILE_NAME);
                if (resourceUrl != null) {
                    System.out.println("Membaca dari resource: " + resourceUrl);
                    // Salin dari resource ke data/pokemon-dataset.json untuk penggunaan pertama kali
                    try (InputStream resourceStream = resourceUrl.openStream()) {
                        Files.copy(resourceStream, WRITE_PATH, StandardCopyOption.REPLACE_EXISTING);
                        System.out.println("Menyalin data awal dari resource ke " + WRITE_PATH.toAbsolutePath());
                    }
                    // Setelah disalin, baca dari file yang baru dibuat
                    inputStream = Files.newInputStream(WRITE_PATH);
                } else {
                    System.err.println("File JSON '" + DATA_FILE_NAME + "' tidak ditemukan di resources. " +
                            "Pastikan file ada di classpath atau direktori 'data' sudah memiliki file yang valid.");
                    return pokemonList; // Kembalikan list kosong jika tidak ada sumber data
                }
            }

            JSONTokener tokener = new JSONTokener(inputStream);
            JSONArray pokemonJsonArray = new JSONArray(tokener); // Ubah nama variabel

            for (int i = 0; i < pokemonJsonArray.length(); i++) {
                JSONObject pokemonJson = pokemonJsonArray.getJSONObject(i);

                String name = pokemonJson.getString("name");
                String typeStr = pokemonJson.getString("type");
                int health = pokemonJson.getInt("health");
                // maxHealth akan di-set sama dengan health oleh konstruktor Pokemon
                int attack = pokemonJson.getInt("attack");
                int defense = pokemonJson.getInt("defense");

                ArrayList<Move> moveSet = new ArrayList<>();
                JSONArray movesArrayJson = pokemonJson.getJSONArray("moves");
                for (int j = 0; j < movesArrayJson.length(); j++) {
                    JSONObject moveJson = movesArrayJson.getJSONObject(j);
                    String moveName = moveJson.getString("name");
                    String moveTypeStr = moveJson.getString("type");
                    int power = moveJson.getInt("power");

                    moveSet.add(new Move(moveName, getTypeFromString(moveTypeStr), power));
                }
                pokemonList.add(new Pokemon(name, getTypeFromString(typeStr), health, attack, defense, moveSet));
            }

        } catch (IOException e) {
            System.err.println("Error I/O saat memproses file Pokemon: " + e.getMessage());
            e.printStackTrace();
        } catch (JSONException e) {
            System.err.println("Error parsing JSON Pokemon: " + e.getMessage());
            System.err.println("Pastikan format file " + WRITE_PATH.toAbsolutePath() + " adalah JSON Array yang valid.");
            e.printStackTrace();
        } finally {
            try {
                if (inputStream != null) {
                    inputStream.close();
                }
            } catch (IOException ignored) {
            }
        }
        return pokemonList;
    }

    /**
     * Mengupdate data Pokemon tertentu dalam file JSON.
     *
     * @param pokemonToUpdate Objek Pokemon dengan data yang sudah diperbarui.
     * @return true jika update berhasil, false jika Pokemon tidak ditemukan.
     */
    public static boolean updatePokemon(Pokemon pokemonToUpdate) {
        List<Pokemon> pokemons = all(); // Baca data terbaru dari file
        boolean updated = false;

        if (pokemons.isEmpty() && !Files.exists(WRITE_PATH)) {
            System.err.println("Tidak dapat update, file " + WRITE_PATH.toAbsolutePath() + " tidak ada dan resources juga gagal dimuat.");
            return false;
        }
        if (pokemons.isEmpty() && Files.exists(WRITE_PATH)) {
            System.out.println("List Pokemon kosong saat mencoba update, mungkin file " + WRITE_PATH.toAbsolutePath() + " kosong atau korup. Tetap mencoba menyimpan.");
            // Jika file ada tapi kosong, mungkin ingin menambahkan Pokemon baru daripada hanya mengupdate.
            // Untuk skenario ini, kita akan mencoba menambahkan/mengganti.
        }


        int foundIndex = -1;
        for (int i = 0; i < pokemons.size(); i++) {
            if (pokemons.get(i).getName().equalsIgnoreCase(pokemonToUpdate.getName())) {
                foundIndex = i;
                break;
            }
        }

        if (foundIndex != -1) {
            pokemons.set(foundIndex, pokemonToUpdate); // Ganti objek Pokemon di list
            updated = true;
        } else {
            // Jika Pokemon tidak ada, bisa pilih untuk menambahkannya atau memberi pesan error.
            // Untuk saat ini, kita anggap update berarti Pokemon sudah ada.
            System.out.println("Pokemon dengan nama '" + pokemonToUpdate.getName() + "' tidak ditemukan untuk diupdate. Tidak ada perubahan yang disimpan.");
            return false; // Tidak melakukan save jika tidak ada yang diupdate
        }

        if (updated) {
            savePokemons(pokemons);
        }
        return updated;
    }

    /**
     * Menyimpan list Pokemon ke file JSON di 'data/pokemon-dataset.json'.
     * File yang ada akan ditimpa.
     *
     * @param pokemons List Pokemon yang akan disimpan.
     */
    public static void savePokemons(List<Pokemon> pokemons) {
        JSONArray jsonArray = new JSONArray();
        for (Pokemon pokemon : pokemons) {
            JSONObject pokemonJsonObj = new JSONObject(); // Ubah nama variabel
            pokemonJsonObj.put("name", pokemon.getName());
            pokemonJsonObj.put("type", formatTypeToString(pokemon.getType()));
            pokemonJsonObj.put("health", pokemon.getHealth()); // Menyimpan health saat ini
            // maxHealth tidak disimpan ke JSON, akan di-set dari health saat load
            pokemonJsonObj.put("attack", pokemon.getAttack());
            pokemonJsonObj.put("defense", pokemon.getDefense());

            JSONArray movesJsonArray = new JSONArray();
            for (Move move : pokemon.getMoves()) {
                JSONObject moveJsonObj = new JSONObject();
                moveJsonObj.put("name", move.getName());
                moveJsonObj.put("type", formatTypeToString(move.getType()));
                moveJsonObj.put("power", move.getPower());
                movesJsonArray.put(moveJsonObj);
            }
            pokemonJsonObj.put("moves", movesJsonArray);
            jsonArray.put(pokemonJsonObj);
        }

        try {
            // Pastikan direktori 'data' ada sebelum menulis
            if (!Files.exists(DATA_DIR)) {
                Files.createDirectories(DATA_DIR);
            }
            // Menggunakan StandardOpenOption.CREATE (buat jika tidak ada) dan TRUNCATE_EXISTING (timpa jika ada)
            try (BufferedWriter writer = Files.newBufferedWriter(WRITE_PATH, StandardCharsets.UTF_8, StandardOpenOption.CREATE, StandardOpenOption.TRUNCATE_EXISTING)) {
                writer.write(jsonArray.toString(4)); // Indentasi 4 spasi untuk keterbacaan
                System.out.println("Berhasil menyimpan data ke " + WRITE_PATH.toAbsolutePath());
            }
        } catch (IOException e) {
            System.err.println("Error saat menyimpan data ke file: " + WRITE_PATH.toAbsolutePath() + " - " + e.getMessage());
            e.printStackTrace();
        } catch (JSONException e) {
            System.err.println("Error membuat JSON saat menyimpan: " + e.getMessage());
            e.printStackTrace();
        }
    }

    /**
     * Mengubah String dari JSON (misal "Fire") menjadi enum Type (misal Type.FIRE).
     *
     * @param typeStr String representasi tipe.
     * @return Enum Type yang sesuai, atau Type.NORMAL jika tidak dikenal.
     */
    private static Type getTypeFromString(String typeStr) {
        if (typeStr == null || typeStr.trim().isEmpty()) {
            System.err.println("Tipe string null atau kosong, menggunakan NORMAL sebagai default.");
            return Type.NORMAL;
        }
        try {
            // Enum.valueOf() bersifat case-sensitive, jadi kita perlu UPPERCASE
            return Type.valueOf(typeStr.trim().toUpperCase());
        } catch (IllegalArgumentException e) {
            // Jika konversi langsung gagal, bisa ditambahkan logika fallback jika diperlukan
            // Misalnya, jika JSON punya "grass" tapi enumnya GRASS
            System.err.println("Tipe tidak dikenal: '" + typeStr + "'. Menggunakan NORMAL sebagai default. Pastikan string tipe di JSON (" + typeStr + ") sesuai dengan nama enum di Type.java (case-insensitive).");
            return Type.NORMAL; // Default jika tidak ada yang cocok
        }
    }

    private static String formatTypeToString(Type type) {
        if (type == null) {
            return "Normal"; // Default jika enum Type null
        }
        // Mengambil nama enum (misal "FIRE") dan mengubahnya ke format TitleCase (misal "Fire")
        String name = type.name(); // e.g., "FIRE", "ELECTRIC"
        if (name.isEmpty()) {
            return "Normal"; // Safety check
        }
        return name.substring(0, 1).toUpperCase() + name.substring(1).toLowerCase(); // e.g., "Fire", "Electric"
    }
}