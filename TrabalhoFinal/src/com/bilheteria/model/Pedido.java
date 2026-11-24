package com.bilheteria.model;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class Pedido {
    private int idPedido;
    private Date dataHora;
    private double valorTotal;
    private String statusPedido; // "CARRINHO", "PAGO", "CANCELADO"
    private Torcedor torcedor;
    private List<Ingresso> ingressos;
    private Pagamento pagamento;

    public Pedido(int idPedido, Torcedor torcedor) {
        this.idPedido = idPedido;
        this.torcedor = torcedor;
        this.dataHora = new Date();
        this.statusPedido = "CARRINHO";
        this.ingressos = new ArrayList<>();
        this.valorTotal = 0.0;
    }

    public void atualizarStatusParaPago() {
        this.statusPedido = "PAGO";
    }

    public String obterResumo() {
        StringBuilder sb = new StringBuilder();
        sb.append("Pedido #").append(idPedido).append(" | Torcedor: ").append(torcedor != null ? torcedor.getNome() : "").append("\n");
        sb.append("Status: ").append(statusPedido).append(" | Data: ").append(dataHora).append("\n");
        sb.append("Ingressos:\n");
        for (Ingresso ing : ingressos) {
            sb.append(" - ").append(ing.toString()).append("\n");
        }
        sb.append("Valor Total: R$ ").append(String.format("%.2f", calcularTotal()));
        return sb.toString();
    }

    public double calcularTotal() {
        double total = 0.0;
        for (Ingresso ing : ingressos) {
            total += ing.getValorPago();
        }
        this.valorTotal = total;
        return total;
    }

    public void adicionarIngresso(Ingresso ingresso) {
        this.ingressos.add(ingresso);
        calcularTotal();
    }

    public void removerIngresso(Ingresso ingresso) {
        this.ingressos.remove(ingresso);
        calcularTotal();
    }

    public void limparCarrinho() {
        this.ingressos.clear();
        this.valorTotal = 0.0;
    }

    // Getters e Setters
    public int getIdPedido() {
        return idPedido;
    }

    public void setIdPedido(int idPedido) {
        this.idPedido = idPedido;
    }

    public Date getDataHora() {
        return dataHora;
    }

    public void setDataHora(Date dataHora) {
        this.dataHora = dataHora;
    }

    public double getValorTotal() {
        return valorTotal;
    }

    public void setValorTotal(double valorTotal) {
        this.valorTotal = valorTotal;
    }

    public String getStatusPedido() {
        return statusPedido;
    }

    public void setStatusPedido(String statusPedido) {
        this.statusPedido = statusPedido;
    }

    public Torcedor getTorcedor() {
        return torcedor;
    }

    public void setTorcedor(Torcedor torcedor) {
        this.torcedor = torcedor;
    }

    public List<Ingresso> getIngressos() {
        return ingressos;
    }

    public void setIngressos(List<Ingresso> ingressos) {
        this.ingressos = ingressos;
    }

    public Pagamento getPagamento() {
        return pagamento;
    }

    public void setPagamento(Pagamento pagamento) {
        this.pagamento = pagamento;
    }
}
