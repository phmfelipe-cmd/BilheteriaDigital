package com.bilheteria.model;

import java.util.ArrayList;
import java.util.List;

public class Setor {
    private int idSetor;
    private String nomeSetor;
    private double precoBase;
    private List<Assento> assentos;

    public Setor(int idSetor, String nomeSetor, double precoBase) {
        this.idSetor = idSetor;
        this.nomeSetor = nomeSetor;
        this.precoBase = precoBase;
        this.assentos = new ArrayList<>();
    }

    public List<Assento> listarAssentos() {
        return new ArrayList<>(this.assentos);
    }

    public void adicionarAssento(Assento assento) {
        this.assentos.add(assento);
    }

    // Getters e Setters
    public int getIdSetor() {
        return idSetor;
    }

    public void setIdSetor(int idSetor) {
        this.idSetor = idSetor;
    }

    public String getNomeSetor() {
        return nomeSetor;
    }

    public void setNomeSetor(String nomeSetor) {
        this.nomeSetor = nomeSetor;
    }

    public double getPrecoBase() {
        return precoBase;
    }

    public void setPrecoBase(double precoBase) {
        this.precoBase = precoBase;
    }

    public List<Assento> getAssentos() {
        return assentos;
    }

    public void setAssentos(List<Assento> assentos) {
        this.assentos = assentos;
    }

    @Override
    public String toString() {
        return nomeSetor + " (Preço base: R$ " + String.format("%.2f", precoBase) + ")";
    }
}
