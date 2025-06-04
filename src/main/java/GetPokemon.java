import org.json.JSONArray;
import org.json.JSONObject;
import org.json.JSONTokener;

import java.io.FileInputStream;
import java.io.InputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;

public class GetPokemon {
    public static ArrayList<Pokemon> all(){
        ArrayList<Pokemon> pokemonList = new ArrayList<>();
        String namaFileJson = "pokemon-dataset.json"; // Nama file di folder resources

        try (InputStream inputStream = GetPokemon.class.getClassLoader().getResourceAsStream(namaFileJson)) {

            if (inputStream == null) {
                System.err.println("Tidak dapat menemukan file: " + namaFileJson);
                return null;
            }

            // Menggunakan JSONTokener untuk membaca langsung dari InputStream
            JSONTokener tokener = new JSONTokener(inputStream);
            JSONArray pokemonToken = new JSONArray(tokener);

            // Sekarang pokemonList berisi data dari file JSON
            // Kamu bisa melanjutkan untuk mengakses datanya seperti contoh sebelumnya:

            for (int i = 0; i < pokemonToken.length(); i++) {
                JSONObject pokemon = pokemonToken.getJSONObject(i);

                String pokemonName = pokemon.getString("name");
                String pokemonType = pokemon.getString("type");
                int health = pokemon.getInt("health");
                int attack = pokemon.getInt("attack");
                int defense = pokemon.getInt("defense");

                ArrayList<Move> moveSet  = new ArrayList<>();

                JSONArray movesArray = pokemon.getJSONArray("moves");
                for (int j = 0; j < movesArray.length(); j++) {
                    JSONObject move = movesArray.getJSONObject(j);
                    String moveName = move.getString("name");
                    String moveType = move.getString("type");
                    int power = move.getInt("power");

                    Move newMove = new Move(moveName, getType(moveType), power);
                    moveSet.add(newMove);
                }

                Type tipe = getType(pokemonType);
                Pokemon newPokemon = new Pokemon(pokemonName, tipe, health, attack, defense, moveSet);
                pokemonList.add(newPokemon);
            }

        } catch (IOException e) {
            System.err.println("Error saat membaca file: " + e.getMessage());
            e.printStackTrace();
        } catch (org.json.JSONException e) {
            System.err.println("Error saat parsing JSON: " + e.getMessage());
            e.printStackTrace();
        }
        return pokemonList;
    }

    private static Type getType(String checker) {
        Type tipe = Type.NORMAL;
        if(checker.equalsIgnoreCase("normal")) tipe = Type.NORMAL;
        else if(checker.equalsIgnoreCase("fire")) tipe = Type.FIRE;
        else if(checker.equalsIgnoreCase("water")) tipe = Type.WATER;
        else if(checker.equalsIgnoreCase("grass")) tipe = Type.GRASS;
        else if(checker.equalsIgnoreCase("electric")) tipe = Type.ELECTRIC;

        return tipe;
    }

    public static void main(String[] args) {
        ArrayList<Pokemon> allPokemon = all();
        System.out.println(allPokemon);
    }
}
