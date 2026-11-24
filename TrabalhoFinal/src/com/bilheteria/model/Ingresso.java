package com.bilheteria.model;

import java.util.UUID;

public class Ingresso {
    private int idIngresso;
    private int tipoCategoria; // 1 = Inteira, 2 = Meia-Entrada
    private String qrCode;
    private double valorPago;
    private Assento assento;
    private String nomeBeneficiario; // Dados do destinatário/comprovante se meia-entrada
    private String documentoBeneficiario;

    public Ingresso(int idIngresso, int tipoCategoria, double valorPago, Assento assento, String nomeBeneficiario, String documentoBeneficiario) {
        this.idIngresso = idIngresso;
        this.tipoCategoria = tipoCategoria;
        this.valorPago = valorPago;
        this.assento = assento;
        this.nomeBeneficiario = nomeBeneficiario;
        this.documentoBeneficiario = documentoBeneficiario;
        this.qrCode = gerarQRCode();
    }

    public String gerarQRCode() {
        return "TICKET-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase() + "-ASSENTO-" + (assento != null ? assento.getNumero() : "0");
    }

    public boolean validarEntrada() {
        return this.qrCode != null && !this.qrCode.isEmpty();
    }

    // Getters e Setters
    public int getIdIngresso() {
        return idIngresso;
    }

    public void setIdIngresso(int idIngresso) {
        this.idIngresso = idIngresso;
    }

    public int getTipoCategoria() {
        return tipoCategoria;
    }

    public void setTipoCategoria(int tipoCategoria) {
        this.tipoCategoria = tipoCategoria;
    }

    public String getQrCode() {
        return qrCode;
    }

    public void setQrCode(String qrCode) {
        this.qrCode = qrCode;
    }

    public double getValorPago() {
        return valorPago;
    }

    public void setValorPago(double valorPago) {
        this.valorPago = valorPago;
    }

    public Assento getAssento() {
        return assento;
    }

    public void setAssento(Assento assento) {
        this.assento = assento;
    }

    public String getNomeBeneficiario() {
        return nomeBeneficiario;
    }

    public void setNomeBeneficiario(String nomeBeneficiario) {
        this.nomeBeneficiario = nomeBeneficiario;
    }

    public String getDocumentoBeneficiario() {
        return documentoBeneficiario;
    }

    public void setDocumentoBeneficiario(String documentoBeneficiario) {
        this.documentoBeneficiario = documentoBeneficiario;
    }

    @Override
    public String toString() {
        String tipoStr = (tipoCategoria == 2) ? "Meia-Entrada (Beneficiário: " + nomeBeneficiario + ", Doc: " + documentoBeneficiario + ")" : "Inteira";
        return "Ingresso #" + idIngresso + " | Assento: " + (assento != null ? assento.getFila() + assento.getNumero() : "N/A") +
                " | Tipo: " + tipoStr + " | Valor: R$ " + String.format("%.2f", valorPago) + " | QR Code: " + qrCode;
    }
}
