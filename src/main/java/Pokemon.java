import java.util.ArrayList;
import java.util.List;

public class Pokemon {
    private String name;
    private Type type;
    private int health;
    private int maxHealth;
    private int attack;
    private int defense;
    private List<Move> moves;

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
        return health;
    }

    public void setHealth(int health) { // Setter for health (needed for healing)
        this.health = health;
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

    public List<Move> getMoves() {
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
}