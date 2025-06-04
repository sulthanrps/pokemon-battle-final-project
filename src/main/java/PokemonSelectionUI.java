//import javax.swing.*;
//import javax.swing.border.EmptyBorder;
//import java.awt.*;
//import java.util.ArrayList;
//import java.util.List;
//
//public class PokemonSelectionUI {
//    private JFrame frame;
//    public PokemonSelectionUI() {
//        frame = new JFrame("Pilih Pokémon");
//        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
//        frame.setSize(950, 400);
//        frame.setLocationRelativeTo(null);
//
//        // Panel utama horizontal
//        JPanel mainPanel = new JPanel();
//        mainPanel.setLayout(new FlowLayout(FlowLayout.LEFT, 20, 20));
//        mainPanel.setBackground(Color.WHITE);
//        mainPanel.setAlignmentX(Component.CENTER_ALIGNMENT);
//
//        // Dataset Pokémon
//        List<Pokemon> pokemons = new ArrayList<>();
////        Pokemon pikachu = new Pokemon("Pikachu", Type.ELECTRIC, 100, 90, 55);
////        pikachu.addMove(new Move("Thunderbolt", Type.ELECTRIC, 90));
////
////        Pokemon charizard = new Pokemon("Charizard", Type.FIRE, 120, 100, 80);
////        charizard.addMove(new Move("Quick Attack", Type.NORMAL, 40));
////
////        Pokemon greninja = new Pokemon("Greninja", Type.WATER, 110, 105, 70);
////        greninja.addMove(new Move("Quick Attack", Type.NORMAL, 40));
//
//        pokemons.add(pikachu);
//        pokemons.add(charizard);
//        pokemons.add(greninja);
//
//        for (Pokemon p : pokemons) {
//            JPanel card = createPokemonCard(p);
//            mainPanel.add(card);
//        }
//
//        JScrollPane scrollPane = new JScrollPane(mainPanel);
//        scrollPane.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_ALWAYS);
//        scrollPane.setBorder(null);
//
//        frame.add(scrollPane);
//        frame.setVisible(true);
//    }
//
//    private JPanel createPokemonCard(Pokemon p) {
//        JPanel panel = new JPanel();
//        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
//        panel.setBackground(new Color(245, 245, 245));
//        panel.setPreferredSize(new Dimension(280, 300));
//        panel.setBorder(BorderFactory.createCompoundBorder(
//                BorderFactory.createLineBorder(new Color(200, 200, 200), 1),
//                new EmptyBorder(10, 10, 10, 10)));
//
//        JPanel topPanel = new JPanel();
//        topPanel.setLayout(new BoxLayout(topPanel, BoxLayout.Y_AXIS));
//        topPanel.setOpaque(false); // biar transparan dan tidak nutup background utama
//        topPanel.setAlignmentX(Component.LEFT_ALIGNMENT);
//
//        JLabel nameLabel = new JLabel(p.getName(), SwingConstants.CENTER);
//        nameLabel.setFont(new Font("Arial", Font.BOLD, 16));
//        nameLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
//
//        ImageIcon pokemonImg = loadPokemonImage(p.getName());
//        JLabel imageLabel = new JLabel(pokemonImg);
//        imageLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
//
//        topPanel.add(nameLabel);
//        topPanel.add(Box.createVerticalStrut(5));
//        topPanel.add(imageLabel);
//
//        JLabel typeLabel = new JLabel(String.valueOf(p.getType()));
//        typeLabel.setAlignmentX(Component.LEFT_ALIGNMENT);
//
//        JLabel healthTextLabel = new JLabel("Health:");
//        healthTextLabel.setAlignmentX(Component.LEFT_ALIGNMENT);
//
//        JProgressBar healthBar = new JProgressBar(0, p.getMaxHealth());
//        healthBar.setValue(p.getHealth());
//        healthBar.setStringPainted(true);
//        healthBar.setForeground(new Color(76, 175, 80));
//        healthBar.setAlignmentX(Component.LEFT_ALIGNMENT);
//
//        JLabel attackLabel = new JLabel("Attack: " + p.getAttack());
//        attackLabel.setAlignmentX(Component.LEFT_ALIGNMENT);
//
//        JLabel defenseLabel = new JLabel("Defense: " + p.getDefense());
//        defenseLabel.setAlignmentX(Component.LEFT_ALIGNMENT);
//
//        JLabel movesLabel = new JLabel("Moves:");
//        movesLabel.setAlignmentX(Component.LEFT_ALIGNMENT);
//
//        DefaultListModel<String> moveModel = new DefaultListModel<>();
//        for (Move move : p.getMoves()) {
//            moveModel.addElement("• " + move);
//        }
//        JList<String> moveList = new JList<>(moveModel);
//        moveList.setEnabled(false);
//        JScrollPane moveScroll = new JScrollPane(moveList);
//        moveScroll.setAlignmentX(Component.LEFT_ALIGNMENT);
//        moveScroll.setPreferredSize(new Dimension(250, 80));
//
//        // Add all components
//        panel.add(topPanel);
//        panel.add(Box.createVerticalStrut(5));
//        panel.add(typeLabel);
//        panel.add(Box.createVerticalStrut(10));
//        panel.add(healthTextLabel);
//        panel.add(healthBar);
//        panel.add(Box.createVerticalStrut(5));
//        panel.add(attackLabel);
//        panel.add(defenseLabel);
//        panel.add(Box.createVerticalStrut(10));
//        panel.add(movesLabel);
//        panel.add(moveScroll);
//
//        return panel;
//    }
//
//    private ImageIcon loadPokemonImage(String name) {
//        String path = "/Assets/Pokemons/" + name.toLowerCase() + ".png"; // pakai '/' diawali
//        java.net.URL imgURL = getClass().getResource(path);
//        if (imgURL != null) {
//            ImageIcon icon = new ImageIcon(imgURL);
//            Image image = icon.getImage().getScaledInstance(100, 100, Image.SCALE_SMOOTH);
//            return new ImageIcon(image);
//        } else {
//            System.err.println("Couldn't find file: " + path);
//            return null;
//        }
//    }
//
//    public static void main(String[] args) {
//        SwingUtilities.invokeLater(PokemonSelectionUI::new);
//    }
//}
//
