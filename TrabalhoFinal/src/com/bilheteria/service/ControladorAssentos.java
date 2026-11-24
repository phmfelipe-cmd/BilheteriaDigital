package com.bilheteria.service;

import com.bilheteria.model.Assento;
import com.bilheteria.model.Partida;
import com.bilheteria.model.Setor;

import java.util.List;

public class ControladorAssentos {
    private GerenciadorAssentos gerenciadorAssentos;

    public ControladorAssentos() {
        this.gerenciadorAssentos = new GerenciadorAssentos();
    }

    public List<Setor> obterSetoresDisponiveis(Partida partida) {
        return gerenciadorAssentos.buscarSetores(partida);
    }

    public List<Assento> exibirAssentosDisponiveis(Setor setor) {
        if (setor == null) return List.of();
        return setor.listarAssentos();
    }

    public boolean selecionarAssento(Assento assento) {
        return gerenciadorAssentos.bloquearAssentoTemporariamente(assento);
    }

    public boolean desmarcarAssento(Assento assento) {
        return gerenciadorAssentos.liberarAssento(assento);
    }

    public void confirmarVendaAssento(Assento assento) {
        gerenciadorAssentos.marcarComoVendido(assento);
    }
}
