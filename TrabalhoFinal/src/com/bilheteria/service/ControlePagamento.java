package com.bilheteria.service;

import com.bilheteria.model.Pedido;

public class ControlePagamento {

    public boolean processarPagamento(String dadosCartao) {
        if (!validarDados(dadosCartao)) {
            return false;
        }
        // Simula processamento em gateway externo seguro
        return true;
    }

    public boolean validarDados(String dadosCartao) {
        if (dadosCartao == null || dadosCartao.trim().isEmpty()) {
            return false;
        }
        // Remove espaços e hífens
        String limpo = dadosCartao.replaceAll("[\\s-]", "");
        // Validação mínima de 13 a 19 dígitos numéricos
        return limpo.matches("\\d{13,19}");
    }

    public boolean efetuarPagamentoPedido(Pedido pedido, String metodo, String dadosCartao) {
        if (processarPagamento(dadosCartao)) {
            pedido.atualizarStatusParaPago();
            if (pedido.getPagamento() != null) {
                pedido.getPagamento().setMetodoPagamento(metodo);
                pedido.getPagamento().confirmarPagamento();
            }
            return true;
        } else {
            if (pedido.getPagamento() != null) {
                pedido.getPagamento().setStatusTransacao("RECUSADO");
            }
            return false;
        }
    }
}
