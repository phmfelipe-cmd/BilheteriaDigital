package com.bilheteria.model;

import java.util.Date;

public class Pagamento {
    private int idPagamento;
    private String metodoPagamento; // ex: "CARTAO_CREDITO", "CARTAO_DEBITO", "PIX"
    private Date dataPagamento;
    private String statusTransacao; // "PENDENTE", "PAGO", "RECUSADO"
    private double valor;

    public Pagamento(int idPagamento, String metodoPagamento, double valor) {
        this.idPagamento = idPagamento;
        this.metodoPagamento = metodoPagamento;
        this.valor = valor;
        this.dataPagamento = new Date();
        this.statusTransacao = "PENDENTE";
    }

    public boolean criarTransacao() {
        this.statusTransacao = "PENDENTE";
        this.dataPagamento = new Date();
        return true;
    }

    public boolean confirmarPagamento() {
        this.statusTransacao = "PAGO";
        this.dataPagamento = new Date();
        return true;
    }

    // Getters e Setters
    public int getIdPagamento() {
        return idPagamento;
    }

    public void setIdPagamento(int idPagamento) {
        this.idPagamento = idPagamento;
    }

    public String getMetodoPagamento() {
        return metodoPagamento;
    }

    public void setMetodoPagamento(String metodoPagamento) {
        this.metodoPagamento = metodoPagamento;
    }

    public Date getDataPagamento() {
        return dataPagamento;
    }

    public void setDataPagamento(Date dataPagamento) {
        this.dataPagamento = dataPagamento;
    }

    public String getStatusTransacao() {
        return statusTransacao;
    }

    public void setStatusTransacao(String statusTransacao) {
        this.statusTransacao = statusTransacao;
    }

    public double getValor() {
        return valor;
    }

    public void setValor(double valor) {
        this.valor = valor;
    }
}
