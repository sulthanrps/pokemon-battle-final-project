package showcase;

public class PokemonData {
    private String name;
    private String type;
    private int health;
    private int attack;
    private int defense;


    public PokemonData(String name, String type, int health, int attack, int defense) {
        this.name = name;
        this.type = type;
        this.health = health;
        this.attack = attack;
        this.defense = defense;
    }

    public String getName() { return name; }
    public String getType() { return type; }
    public int getHealthValue() { return health; }
    public String getHealthFormatted() { return String.valueOf(health); }
    public int getAttack() { return attack; }
    public int getDefense() { return defense; }
}