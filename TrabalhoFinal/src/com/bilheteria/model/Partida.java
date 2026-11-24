package com.bilheteria.model;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

public class Partida {
    private int idPartida;
    private LocalDateTime dataHora;
    private String timeMandante;
    private String adversario; // Time Visitante
    private String campeonato;
    private String estadio;
    private List<Setor> setores;

    public Partida(int idPartida, LocalDateTime dataHora, String adversario, String campeonato) {
        this(idPartida, dataHora, "Clube Principal", adversario, campeonato, "ESTÁDIO NACIONAL");
    }

    public Partida(int idPartida, LocalDateTime dataHora, String timeMandante, String adversario, String campeonato, String estadio) {
        this.idPartida = idPartida;
        this.dataHora = dataHora;
        this.timeMandante = (timeMandante != null && !timeMandante.trim().isEmpty()) ? timeMandante : "Time da Casa";
        this.adversario = adversario;
        this.campeonato = campeonato;
        this.estadio = (estadio != null && !estadio.trim().isEmpty()) ? estadio : "ESTÁDIO NACIONAL";
        this.setores = new ArrayList<>();
    }

    public List<Partida> listarPartidas() {
        return new ArrayList<>();
    }

    public void adicionarSetor(Setor setor) {
        this.setores.add(setor);
    }

    public Setor buscarSetorPorId(int idSetor) {
        for (Setor s : setores) {
            if (s.getIdSetor() == idSetor) {
                return s;
            }
        }
        return null;
    }

    public Assento buscarAssento(int idAssento) {
        for (Setor s : setores) {
            for (Assento a : s.getAssentos()) {
                if (a.getIdAssento() == idAssento) {
                    return a;
                }
            }
        }
        return null;
    }

    // Getters e Setters
    public int getIdPartida() {
        return idPartida;
    }

    public void setIdPartida(int idPartida) {
        this.idPartida = idPartida;
    }

    public LocalDateTime getDataHora() {
        return dataHora;
    }

    public void setDataHora(LocalDateTime dataHora) {
        this.dataHora = dataHora;
    }

    public String getTimeMandante() {
        return timeMandante;
    }

    public void setTimeMandante(String timeMandante) {
        this.timeMandante = timeMandante;
    }

    public String getAdversario() {
        return adversario;
    }

    public void setAdversario(String adversario) {
        this.adversario = adversario;
    }

    public String getCampeonato() {
        return campeonato;
    }

    public void setCampeonato(String campeonato) {
        this.campeonato = campeonato;
    }

    public String getEstadio() {
        return estadio;
    }

    public void setEstadio(String estadio) {
        this.estadio = estadio;
    }

    public List<Setor> getSetores() {
        return setores;
    }

    public void setSetores(List<Setor> setores) {
        this.setores = setores;
    }

    public String getNomeConfronto() {
        return timeMandante + " vs " + adversario;
    }

    @Override
    public String toString() {
        DateTimeFormatter dtf = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");
        return "[#" + idPartida + "] " + campeonato + ": " + getNomeConfronto() + " em " + dataHora.format(dtf);
    }
}
