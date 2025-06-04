package showcase;

import org.json.JSONArray;
import org.json.JSONObject;
import org.json.JSONTokener;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

public class PokemonDataLoader {
    private static final String POKEMON_DB_JSON_PATH = "pokemon-dataset.json";

    public static List<PokemonDataLoader> loadPokemonData() {
        List<PokemonData> pokemonList = new ArrayList<>();
        InputStream is = null;

        try {
            is = PokemonDataLoader.class.getResourceAsStream(POKEMON_DB_JSON_PATH);

            if (is == null) {
                System.err.println("Tidak dapat menemukan file JSON: " + POKEMON_DB_JSON_PATH +
                        ". Pastikan path sudah benar dan folder 'resources' terkonfigurasi sebagai source root.");
                return pokemonList;
            }

            JSONTokener tokener = new JSONTokener(is);
            JSONArray jsonArray = new JSONArray(tokener);

            for (int i = 0; i < jsonArray.length(); i++) {
                JSONObject pokemonJson = jsonArray.getJSONObject(i);

                String name = pokemonJson.getString("name");
                String type = pokemonJson.getString("type");
                int health = pokemonJson.getInt("health");
                int attack = pokemonJson.getInt("attack");
                int defense = pokemonJson.getInt("defense");
                pokemonList.add(new PokemonData(name, type, health, attack, defense));
            }
        } catch (Exception e) {
            System.err.println("Error saat memuat atau memparsing data Pokémon dari JSON: " + e.getMessage());
            e.printStackTrace();
        } finally {
            // Tutup InputStream jika sudah dibuka dan tidak menggunakan try-with-resources
            // Namun, karena kita menggunakan try-with-resources di versi sebelumnya (dan itu lebih baik),
            // penutupan stream otomatis ditangani. Jika tidak, uncomment bagian ini:
            /*
            if (is != null) {
                try {
                    is.close();
                } catch (java.io.IOException ioe) {
                    ioe.printStackTrace();
                }
            }
            */
        }
        return pokemonList;
    }

    public static void main(String[] args) {
        List<PokemonData> testData = loadPokemonData();
        if (testData.isEmpty()) {
            System.out.println("Tidak ada data yang dimuat atau terjadi error.");
        } else {
            System.out.println("Data Pokémon berhasil dimuat (" + testData.size() + " entri):");
            for (PokemonData p : testData) {
                System.out.println("- Nama: " + p.getName() + ", Tipe: " + p.getType() +
                        ", HP: " + p.getHealthValue() + ", ATK: " + p.getAttack() +
                        ", DEF: " + p.getDefense());
            }
        }
    }
}