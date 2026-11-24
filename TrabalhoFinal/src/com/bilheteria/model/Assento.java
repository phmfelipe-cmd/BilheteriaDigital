package com.bilheteria.model;


public class Assento {
    private int idAssento;
    private String numero;
    private String fila;
    private String status; // "DISPONIVEL", "RESERVADO", "VENDIDO"
    private int idSetor;
    private int idPartida;

    public Assento(int idAssento, String numero, String fila, int idSetor, int idPartida) {
        this.idAssento = idAssento;
        this.numero = numero;
        this.fila = fila;
        this.status = "DISPONIVEL";
        this.idSetor = idSetor;
        this.idPartida = idPartida;
    }

    public boolean bloquearAssento() {
        if ("DISPONIVEL".equalsIgnoreCase(this.status)) {
            this.status = "RESERVADO";
            return true;
        }
        return false;
    }

    public boolean liberarAssento() {
        if ("RESERVADO".equalsIgnoreCase(this.status)) {
            this.status = "DISPONIVEL";
            return true;
        }
        return false;
    }

    public void marcarComoVendido() {
        this.status = "VENDIDO";
    }

    // Getters e Setters
    public int getIdAssento() {
        return idAssento;
    }

    public void setIdAssento(int idAssento) {
        this.idAssento = idAssento;
    }

    public String getNumero() {
        return numero;
    }

    public void setNumero(String numero) {
        this.numero = numero;
    }

    public String getFila() {
        return fila;
    }

    public void setFila(String fila) {
        this.fila = fila;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public int getIdSetor() {
        return idSetor;
    }

    public void setIdSetor(int idSetor) {
        this.idSetor = idSetor;
    }

    public int getIdPartida() {
        return idPartida;
    }

    public void setIdPartida(int idPartida) {
        this.idPartida = idPartida;
    }

    @Override
    public String toString() {
        return fila + numero + " [" + status + "]";
    }
}
