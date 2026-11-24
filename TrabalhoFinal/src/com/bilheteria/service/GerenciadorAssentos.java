package com.bilheteria.service;

import com.bilheteria.model.Assento;
import com.bilheteria.model.Partida;
import com.bilheteria.model.Setor;

import java.util.List;

public class GerenciadorAssentos {

    public List<Setor> buscarSetores(Partida partida) {
        if (partida == null) return List.of();
        return partida.getSetores();
    }

    public boolean bloquearAssentoTemporariamente(Assento assento) {
        if (assento == null) return false;
        return assento.bloquearAssento();
    }

    public boolean liberarAssento(Assento assento) {
        if (assento == null) return false;
        return assento.liberarAssento();
    }

    public void marcarComoVendido(Assento assento) {
        if (assento != null) {
            assento.marcarComoVendido();
        }
    }
}
