import java.util.ArrayList;

public class Pokemon {
    private String name;
    private Type type;
    private int health;
    private int maxHealth;
    private int attack;
    private int defense;
    private ArrayList<Move> moves;

    public Pokemon(String name, Type type, int health, int attack, int defense, ArrayList<Move> moves) {
        this.name = name;
        this.type = type;
        this.health = health;
        this.maxHealth = health;
        this.attack = attack;
        this.defense = defense;
        this.moves = moves;
    }

    public Pokemon() {

    }

    public Pokemon(String name, Type type, int health, int attack, int defense) {
        this.name = name;
        this.type = type;
        this.health = health;
        this.maxHealth = health;
        this.attack = attack;
        this.defense = defense;
        this.moves = new ArrayList<>();
    }

    // ***Getters and Setters (Crucial!)***
    public String getName() {
        return name;
    }

    public Type getType() {
        return type;
    }

    public int getHealth() {
        if(health < 0) {
            health = 0;
        }
        return health;
    }

    public void setHealth(int health) { // Setter for health (needed for healing)
        this.health = health;
    }

    public void revivePokemon() {
        this.health = this.maxHealth;
    }

    public int getMaxHealth() {
        return maxHealth;
    }

    public int getAttack() {
        return attack;
    }

    public int getDefense() {
        return defense;
    }

    public ArrayList<Move> getMoves() {
        return moves;
    }

    public void addMove(Move move) {
        moves.add(move);
    }

    public void attack(Pokemon target, Move move) {
        int damage = BattleMechanism.calculateDamage(this, move, target);
        target.takeDamage(damage);
        System.out.println(this.name + " used " + move.getName() + " on " + target.getName() + " for " + damage + " damage!");
    }

    public void takeDamage(int damage) {
        this.health -= damage;
        if (this.health <= 0) {
            System.out.println(this.name + " fainted!");
        }
    }

    public boolean isFainted() {
        return health <= 0;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("Pokemon{\n");
        sb.append("  name='").append(name).append("',\n");
        sb.append("  type=").append(type).append(",\n"); // Asumsikan Type.toString() sudah baik
        sb.append("  health=").append(health).append("/").append(maxHealth).append(",\n");
        sb.append("  attack=").append(attack).append(",\n");
        sb.append("  defense=").append(defense).append(",\n");
        sb.append("  moves=[\n");
        if (moves != null && !moves.isEmpty()) {
            for (int i = 0; i < moves.size(); i++) {
                sb.append("    ").append(moves.get(i).toString()); // Asumsikan Move.toString() sudah baik
                if (i < moves.size() - 1) {
                    sb.append(",");
                }
                sb.append("\n");
            }
        } else {
            sb.append("    (no moves)\n");
        }
        sb.append("  ]\n");
        sb.append("}");
        return sb.toString();
    }
}