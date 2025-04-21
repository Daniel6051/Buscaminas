package Vista;

import Controlador.Controlador;
import javax.swing.*;
import java.awt.*;

public class Interfaz extends JFrame {
    public JPanel panelTablero;
    
    public Interfaz(Controlador aThis) {
        initComponents();
    }

    private void initComponents() {

        panelTablero = new JPanel();
        panelTablero.setLayout(new GridLayout(10, 10));
        add(panelTablero, BorderLayout.CENTER);

     
        setTitle("Buscaminas");
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setSize(600, 600);
        setLocationRelativeTo(null);
        setVisible(true);
    }


}