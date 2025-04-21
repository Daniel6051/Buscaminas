package Modelo;

import java.io.Serializable;//Significa que el objeto puede convertirse en un flujo de bytes para guardar o transmitir


public class Casilla implements Serializable {
    private int posFila;
    private int posColumna;
    private boolean mina;
    private int numMinasAlrededor;
    private boolean abierta;
    private boolean marcada;
    //Constructor
    public Casilla(int posFila, int posColumna) {
        this.posFila = posFila;
        this.posColumna = posColumna;
        this.numMinasAlrededor = 0;
        this.abierta = false;
        this.mina = false;
        this.marcada = false; // Inicialmente no está marcada
    }

    public boolean isMarcada() {
        return marcada;
    }

    public void setMarcada(boolean marcada) {
        this.marcada = marcada;
    }

    public int getPosFila() {
        return posFila;
    }

    public int getPosColumna() {
        return posColumna;
    }

    public boolean isMina() {
        return mina;
    }

    public void setMina(boolean mina) {
        this.mina = mina;
    }

    public int getNumMinasAlrededor() {
        return numMinasAlrededor;
    }

    public void setNumMinasAlrededor(int numMinasAlrededor) {
        this.numMinasAlrededor = numMinasAlrededor;
    }

    public boolean isAbierta() {
        return abierta;
    }

    public void setAbierta(boolean abierta) {
        this.abierta = abierta;
    }
    //Metodo
    public void incrementarNumeroMinasAlrededor() {
        this.numMinasAlrededor++;
    }
}
