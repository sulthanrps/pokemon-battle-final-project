public class Move {
    private String name;
    private Type type;
    private int power;

    public Move(String name, Type type, int power) {
        this.name = name;
        this.type = type;
        this.power = power;
    }

    public String getName() { return name; }
    public Type getType() { return type; }
    public int getPower() { return power; }

    public String toString() {
        return name + " (" + type + ", " + power + " power)";
    }
}