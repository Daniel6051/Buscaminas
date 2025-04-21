package Controlador;

import Modelo.Casilla;
import Modelo.TableroBuscaminas;
import Vista.Interfaz;
import java.awt.Color;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.*;
import java.util.Stack;
import java.io.File;
import javax.swing.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import javax.swing.filechooser.FileNameExtensionFilter;

public class Controlador implements ActionListener{
    private TableroBuscaminas tablero;
    private Interfaz vista;
    private Interfaz panelTablero;
    private JButton[][] botonesTablero;
    private Stack<int[]> movimientosAnteriores;
    private Stack<TableroBuscaminas> estadosAnteriores;
    //Constructor
    public Controlador() {
        tablero = new TableroBuscaminas(10, 10, 10);
        vista = new Interfaz(this);
        movimientosAnteriores = new Stack<>();
        estadosAnteriores = new Stack<>();
        cargarTablero();
        
        JMenuBar menuBar = new JMenuBar();
        JMenu menuJuego = new JMenu("Juego");
        JMenuItem menuNuevoJuego = new JMenuItem("Nuevo");
        JMenuItem menuTamano = new JMenuItem("Tamaño");
        JMenuItem menuNumMinas = new JMenuItem("Número Minas");
        JMenuItem menuDeshacer = new JMenuItem("Deshacer");
        JMenuItem menuGuardar = new JMenuItem("Guardar Partida");
        JMenuItem menuCargar = new JMenuItem("Cargar Partida");

        menuJuego.add(menuNuevoJuego);
        menuJuego.add(menuTamano);
        menuJuego.add(menuNumMinas);
        menuJuego.add(menuDeshacer);
        menuJuego.addSeparator();
        menuJuego.add(menuGuardar);
        menuJuego.add(menuCargar);
        menuBar.add(menuJuego);
        //setJMenuBar(menuBar);
        
        menuNuevoJuego.addActionListener(e -> deshacerUltimoMovimiento());    
        menuDeshacer.addActionListener(e -> deshacerUltimoMovimiento());
        menuGuardar.addActionListener(e -> guardarPartida());
        menuCargar.addActionListener(e -> cargarPartida());
    }
    
    public void nuevoJuego() {
        tablero = new TableroBuscaminas(tablero.getNumFilas(), tablero.getNumColumnas(), tablero.getNumMinas());
        movimientosAnteriores.clear();
        cargarTablero();
    }

    public void setTamanoTablero(int tam) {
        tablero.setNumFilas(tam);
        tablero.setNumColumnas(tam);
        nuevoJuego();
    }

    public void setNumMinas(int minas) {
        tablero.setNumMinas(minas);
        nuevoJuego();
    }

    private void cargarTablero() {
        int filas = tablero.getNumFilas();
        int columnas = tablero.getNumColumnas();
        botonesTablero = new JButton[filas][columnas];

        for (int i = 0; i < filas; i++) {
            for (int j = 0; j < columnas; j++) {
                JButton boton = new JButton();
                boton.setName(i + "," + j);

                final int fila = i;
                final int columna = j;
                boton.addActionListener(e -> seleccionarCasilla(fila, columna));

                boton.addMouseListener(new MouseAdapter() {
                    @Override
                    public void mousePressed(MouseEvent e) {
                        if (e.getButton() == MouseEvent.BUTTON3) {
                            colocarQuitarBandera(boton, fila, columna);
                        }
                    }
                });

                botonesTablero[i][j] = boton;
            }
        }
    }

    private void seleccionarCasilla(int fila, int columna) {
        registrarMovimiento(fila, columna);
        boolean esMina = tablero.seleccionarCasilla(fila, columna);
        if (esMina) {
            mostrarMinas();
            deshabilitarTablero();
            JOptionPane.showMessageDialog(vista, "¡Perdiste!");
        } else if (tablero.partidaGanada()) {
            mostrarGanadas();
            deshabilitarTablero();
            JOptionPane.showMessageDialog(vista, "¡Ganaste!");
        }
        actualizarTablero();
    }
    
    private static final int MAX_ESTADOS = 10; // Límite de estados a guardar
    
    private void registrarEstado() {
        try {
            // Crear una copia profunda del tablero actual
            ByteArrayOutputStream baos = new ByteArrayOutputStream();//Se crea un flujo de salida en memoria (baos) para almacenar los datos en forma debytes
            ObjectOutputStream oos = new ObjectOutputStream(baos);
            oos.writeObject(tablero);//El objeto tablero se serializa y se escribe en el flujo oos. 
            oos.close();

            ByteArrayInputStream bais = new ByteArrayInputStream(baos.toByteArray());//Se crea un flujo de entrada de bytes (bais) a partir del contenido del flujo de salida memoria (baos).
            TableroBuscaminas estadoGuardado = (TableroBuscaminas) new ObjectInputStream(bais).readObject();//Se utiliza el método readObject() para deserializar los bytes y reconstruir el objeto tablero

            estadosAnteriores.push(estadoGuardado);
            // Limitar el número de estados guardados
            if (estadosAnteriores.size() > MAX_ESTADOS) {
                estadosAnteriores.removeElementAt(0);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    
    private void registrarMovimiento(int fila, int columna) {
        // Guardar el estado antes de realizar un movimiento
        registrarEstado();
        movimientosAnteriores.push(new int[]{fila, columna});
    }

    public void deshacerUltimoMovimiento() {
        if (!estadosAnteriores.isEmpty()) {
            // Restaurar el último estado guardado
            tablero = estadosAnteriores.pop();

            // Limpiar la pila de movimientos anteriores
            movimientosAnteriores.clear();

            // Recargar el tablero visualmente
            cargarTablero();
            actualizarTablero();
        }
    }
    
    // Método nuevo para actualizar visualmente el tablero al cargar
    private void actualizarTableroAlCargar() {
        for (int i = 0; i < tablero.getNumFilas(); i++) {
            for (int j = 0; j < tablero.getNumColumnas(); j++) {
                Casilla casilla = tablero.getCasilla(i, j);
                JButton boton = botonesTablero[i][j];

                // Resetear apariencia de todos los botones
                boton.setEnabled(true);
                boton.setText("");
                boton.setBackground(null);

                // Si la casilla está abierta
                if (casilla.isAbierta()) {
                    boton.setEnabled(false);
                    if (casilla.isMina()) {
                        boton.setText("*");
                        boton.setBackground(Color.RED);
                    } else {
                        int minasCercanas = tablero.getMinasCercanas(i, j);
                        boton.setText(minasCercanas > 0 ? String.valueOf(minasCercanas) : "");
                    }
                }

                // Si la casilla está marcada
                if (casilla.isMarcada()) {
                    boton.setText("🚩");
                }
            }
        }
    }
    
   public void guardarPartida() {
        try {
            JFileChooser fileChooser = new JFileChooser();
            fileChooser.setDialogTitle("Guardar Partida de Buscaminas");

            // Establecer directorio actual como directorio inicial
            fileChooser.setCurrentDirectory(new File("."));

            // Sugerir nombre de archivo
            fileChooser.setSelectedFile(new File("buscaminas_partida.dat"));

            int resultado = fileChooser.showSaveDialog(vista);

            if (resultado == JFileChooser.APPROVE_OPTION) {
                File archivoSeleccionado = fileChooser.getSelectedFile();

                // Asegurar extensión .dat
                if (!archivoSeleccionado.getName().toLowerCase().endsWith(".dat")) {
                    archivoSeleccionado = new File(archivoSeleccionado.getAbsolutePath() + ".dat");
                }

                try (ObjectOutputStream out = new ObjectOutputStream(new FileOutputStream(archivoSeleccionado))) {
                    out.writeObject(tablero);
                    JOptionPane.showMessageDialog(vista, "Partida guardada exitosamente");
                } catch (IOException e) {
                    JOptionPane.showMessageDialog(vista, "Error al guardar la partida: " + e.getMessage(), 
                                                  "Error", JOptionPane.ERROR_MESSAGE);
                }
            }
        } catch (Exception e) {
            JOptionPane.showMessageDialog(vista, "Error inesperado: " + e.getMessage(), 
                                          "Error", JOptionPane.ERROR_MESSAGE);
        }
    }
    
    public void cargarPartida() {
    try {
        // Mostrar diálogo para seleccionar archivo
        JFileChooser fileChooser = new JFileChooser();
        fileChooser.setDialogTitle("Cargar Partida de Buscaminas");
        
        // Establecer directorio actual como directorio inicial
        fileChooser.setCurrentDirectory(new File("."));
        
        // Solo mostrar archivos .dat
        fileChooser.setFileFilter(new FileNameExtensionFilter("Archivos de Buscaminas", "dat"));
        
        int resultado = fileChooser.showOpenDialog(vista);
        
        if (resultado == JFileChooser.APPROVE_OPTION) {
            File archivoSeleccionado = fileChooser.getSelectedFile();
            
            try (ObjectInputStream in = new ObjectInputStream(new FileInputStream(archivoSeleccionado))) {
                // Cargar el tablero
                tablero = (TableroBuscaminas) in.readObject();
                
                // Limpiar movimientos anteriores
                movimientosAnteriores.clear();
                
                // Recargar el tablero completamente
                cargarTablero();
                
                // Actualizar visualmente todas las casillas
                actualizarTableroAlCargar();
                
                JOptionPane.showMessageDialog(vista, "Partida cargada exitosamente");
            } catch (IOException | ClassNotFoundException e) {
                JOptionPane.showMessageDialog(vista, "Error al cargar la partida: " + e.getMessage(), 
                                              "Error", JOptionPane.ERROR_MESSAGE);
            }
        }
    } catch (Exception e) {
        JOptionPane.showMessageDialog(vista, "Error inesperado: " + e.getMessage(), 
                                      "Error", JOptionPane.ERROR_MESSAGE);
    }
}

    private void mostrarMinas() {
        for (int i = 0; i < tablero.getNumFilas(); i++) {
            for (int j = 0; j < tablero.getNumColumnas(); j++) {
                if (tablero.esMina(i, j)) {
                    botonesTablero[i][j].setText("*");
                    botonesTablero[i][j].setBackground(Color.RED);//Agregamos el color rojo ala casilla
                    botonesTablero[i][j].setOpaque(false); // Asegura que el color de fondo se vea
                    botonesTablero[i][j].setBorderPainted(false); // Quita el borde
                    botonesTablero[i][j].setFocusPainted(false); // Quita el borde de enfoque
                    botonesTablero[i][j].setContentAreaFilled(false); // Quita el área de contenido
                    botonesTablero[i][j].setOpaque(true); // Hace el botón completamente opaco
                }
            }
        }
    }
    private void mostrarGanadas() {
    for (int i = 0; i < tablero.getNumFilas(); i++) {
        for (int j = 0; j < tablero.getNumColumnas(); j++) {
            if (tablero.esMina(i, j)) {  // Solo coloreamos las casillas con minas en verde
                botonesTablero[i][j].setBackground(Color.GREEN);  // Coloca el color verde en las minas
                botonesTablero[i][j].setText("*");  // Deja el texto de la mina
                botonesTablero[i][j].setOpaque(true); // Asegura que el color de fondo se vea
                botonesTablero[i][j].setBorderPainted(false); // Quita el borde
                botonesTablero[i][j].setFocusPainted(false); // Quita el borde de enfoque
                botonesTablero[i][j].setContentAreaFilled(true); // Asegura que el área de contenido sea visible
            }
        }
    }
}


    private void actualizarTablero() {
        for (int i = 0; i < tablero.getNumFilas(); i++) {
            for (int j = 0; j < tablero.getNumColumnas(); j++) {
                if (tablero.esCasillaAbierta(i, j)) {
                    int minasCercanas = tablero.getMinasCercanas(i, j);
                    botonesTablero[i][j].setEnabled(false);
                    botonesTablero[i][j].setText(minasCercanas > 0 ? String.valueOf(minasCercanas) : "");
                }
            }
        }
    }

    // Colocar o quitar la bandera en una casilla con clic derecho
    private void colocarQuitarBandera(JButton boton, int fila, int columna) {
        Casilla casilla = tablero.getCasilla(fila, columna);
        if (!casilla.isAbierta()) { // Si la casilla no está abierta, se permite marcarla
            if (boton.getText().equals("🚩")) { // Si ya tiene una bandera, la quitamos
                boton.setText("");
                casilla.setMarcada(false);
            } else { // Si no tiene bandera, la colocamos
                boton.setText("🚩");
                casilla.setMarcada(true);
            }
        }
    }
    
    // Deshabilitar todos los botones del tablero
    private void deshabilitarTablero() {
        for (int i = 0; i < tablero.getNumFilas(); i++) {
            for (int j = 0; j < tablero.getNumColumnas(); j++) {
                botonesTablero[i][j].setEnabled(false); // Deshabilitar todos los botones
            }
        }
    }

    public static void iniciarAplicacion() {
        new Controlador();
    }
    
        public void cargarTablero(int filas, int columnas, JButton[][] botones) {
        this.vista.panelTablero.removeAll();
        panelTablero.setLayout(new GridLayout(filas, columnas));
        for (int i = 0; i < filas; i++) {
            for (int j = 0; j < columnas; j++) {
                panelTablero.add(botones[i][j]);
            }
        }
        panelTablero.revalidate();
        panelTablero.repaint();
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        throw new UnsupportedOperationException("Not supported yet."); // Generated from nbfs://nbhost/SystemFileSystem/Templates/Classes/Code/GeneratedMethodBody
    }
}
