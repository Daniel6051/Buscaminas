package Modelo;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collections;//Proporciona métodos estáticos para manipular colecciones "shuffle"
import java.util.List;

public class TableroBuscaminas implements Serializable {
    private Casilla[][] casillas;
    private int numFilas;
    private int numColumnas;
    private int numMinas;
    private int numCasillasAbiertas;
    private boolean juegoTerminado;

    // Constructor
    public TableroBuscaminas(int numFilas, int numColumnas, int numMinas) {
        this.numFilas = numFilas;
        this.numColumnas = numColumnas;
        this.numMinas = numMinas;
        this.numCasillasAbiertas = 0;
        this.juegoTerminado = false;
        inicializarCasillas();
    }

    // Métodos getter y setter
    public int getNumFilas() {
        return numFilas;
    }

    public void setNumFilas(int numFilas) {
        this.numFilas = numFilas;
    }

    public int getNumColumnas() {
        return numColumnas;
    }

    public void setNumColumnas(int numColumnas) {
        this.numColumnas = numColumnas;
    }

    public int getNumMinas() {
        return numMinas;
    }

    public void setNumMinas(int numMinas) {
        this.numMinas = numMinas;
    }
    
    // Inicializa las casillas del tablero
    public void inicializarCasillas() {
        casillas = new Casilla[numFilas][numColumnas];
        for (int i = 0; i < numFilas; i++) {
            for (int j = 0; j < numColumnas; j++) {
                casillas[i][j] = new Casilla(i, j);
            }
        }
        generarMinas();
    }
    
    // Método para obtener una casilla
    public Casilla getCasilla(int fila, int columna) {
        return casillas[fila][columna];
    }
    
    // Obtiene las casillas alrededor de una casilla dada
    private List<Casilla> obtenerCasillasAlrededor(int posFila, int posColumna) {
        List<Casilla> listaCasillas = new ArrayList<>();
        int[][] desplazamientos = {
            {-1, 0}, {-1, 1}, {0, 1}, {1, 1},
            {1, 0}, {1, -1}, {0, -1}, {-1, -1}
        };

        for (int[] desplazamiento : desplazamientos) {
            int tmpPosFila = posFila + desplazamiento[0];
            int tmpPosColumna = posColumna + desplazamiento[1];

            if (tmpPosFila >= 0 && tmpPosFila < numFilas && tmpPosColumna >= 0 && tmpPosColumna < numColumnas) {
                listaCasillas.add(casillas[tmpPosFila][tmpPosColumna]);
            }
        }
        return listaCasillas;
    }
    
    public void deshacerCasilla(int fila, int columna) {
        Casilla casilla = casillas[fila][columna];
        if (casilla.isAbierta()) {
            casilla.setAbierta(false);
            numCasillasAbiertas--;

            // Revertir casillas cercanas si es necesario
            if (casilla.getNumMinasAlrededor() == 0) {
                List<Casilla> casillasAlrededor = obtenerCasillasAlrededor(fila, columna);
                for (Casilla c : casillasAlrededor) {
                    // Verificar si la casilla adyacente estaba abierta
                    if (c.isAbierta()) {
                        deshacerCasilla(c.getPosFila(), c.getPosColumna());
                    }
                }
            }
        }
    }
    
    // Actualiza el número de minas alrededor de cada casilla
    private void actualizarNumeroMinasAlrededor() {
        for (int i = 0; i < numFilas; i++) {
            for (int j = 0; j < numColumnas; j++) {
                if (casillas[i][j].isMina()) {
                    List<Casilla> casillasAlrededor = obtenerCasillasAlrededor(i, j);
                    for (Casilla c : casillasAlrededor) {
                        c.incrementarNumeroMinasAlrededor();
                    }
                }
            }
        }
    }
   
    // Genera las minas en posiciones aleatorias
    private void generarMinas() {
        List<int[]> posicionesDisponibles = new ArrayList<>();
        for (int i = 0; i < numFilas; i++) {
            for (int j = 0; j < numColumnas; j++) {
                posicionesDisponibles.add(new int[]{i, j});
            }
        }
        Collections.shuffle(posicionesDisponibles); // Barajar la lista de posiciones

        for (int i = 0; i < numMinas; i++) {
            int[] pos = posicionesDisponibles.get(i);
            casillas[pos[0]][pos[1]].setMina(true);
        }

        actualizarNumeroMinasAlrededor();
    }

    // Selecciona una casilla y maneja su apertura
    public boolean seleccionarCasilla(int posFila, int posColumna) {
        if (casillas[posFila][posColumna].isAbierta() || juegoTerminado) {
            return false;
        }

        Casilla casilla = casillas[posFila][posColumna];
        casilla.setAbierta(true);
        numCasillasAbiertas++;

        if (casilla.isMina()) {
            juegoTerminado = true;
            return true;
        }

        if (casilla.getNumMinasAlrededor() == 0) {
            List<Casilla> casillasAlrededor = obtenerCasillasAlrededor(posFila, posColumna);
            for (Casilla c : casillasAlrededor) {
                if (!c.isAbierta()) {
                    seleccionarCasilla(c.getPosFila(), c.getPosColumna());
                }
            }
        }

        return false;
    }
    
    // Verifica si el juego está ganado
    public boolean partidaGanada() {
        return numCasillasAbiertas == (numFilas * numColumnas - numMinas);
    }

    // Métodos de acceso para obtener información de las casillas
    public boolean esMina(int fila, int columna) {
        return casillas[fila][columna].isMina();
    }

    public boolean esCasillaAbierta(int fila, int columna) {
        return casillas[fila][columna].isAbierta();
    }

    public int getMinasCercanas(int fila, int columna) {
        return casillas[fila][columna].getNumMinasAlrededor();
    }
}
