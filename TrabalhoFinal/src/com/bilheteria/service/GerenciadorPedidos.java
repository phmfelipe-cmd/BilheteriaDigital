package com.bilheteria.service;

import com.bilheteria.model.Pedido;
import com.bilheteria.model.Torcedor;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class GerenciadorPedidos {
    private List<Pedido> pedidos;
    private int nextPedidoId = 1;

    public GerenciadorPedidos() {
        this.pedidos = new ArrayList<>();
    }

    public Pedido criarPedido(Torcedor torcedor) {
        Pedido pedido = new Pedido(nextPedidoId++, torcedor);
        pedidos.add(pedido);
        return pedido;
    }

    public List<Pedido> listarPedidosPorTorcedor(Torcedor torcedor) {
        if (torcedor == null) return List.of();
        return pedidos.stream()
                .filter(p -> p.getTorcedor() != null && p.getTorcedor().getIdTorcedor() == torcedor.getIdTorcedor())
                .collect(Collectors.toList());
    }

    public List<Pedido> listarPedidosPagosPorTorcedor(Torcedor torcedor) {
        if (torcedor == null) return List.of();
        return pedidos.stream()
                .filter(p -> p.getTorcedor() != null && p.getTorcedor().getIdTorcedor() == torcedor.getIdTorcedor() && "PAGO".equalsIgnoreCase(p.getStatusPedido()))
                .collect(Collectors.toList());
    }
}
