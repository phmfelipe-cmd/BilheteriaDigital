package com.bilheteria.model;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

public class Torcedor {
    private int idTorcedor;
    private String nome;
    private String cpf;
    private String email;
    private String senha; // Armazenada com hash SHA-256 (criptografada)

    public Torcedor(int idTorcedor, String nome, String cpf, String email, String senhaPlana) {
        this.idTorcedor = idTorcedor;
        this.nome = nome;
        this.cpf = cpf;
        this.email = email;
        this.senha = hashSenha(senhaPlana);
    }

    public static String hashSenha(String senhaPlana) {
        try {
            MessageDigest md = MessageDigest.getInstance("SHA-256");
            byte[] hashBytes = md.digest(senhaPlana.getBytes());
            StringBuilder sb = new StringBuilder();
            for (byte b : hashBytes) {
                sb.append(String.format("%02x", b));
            }
            return sb.toString();
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException("Erro ao criptografar senha", e);
        }
    }

    public void cadastrar() {
        // Método representativo do diagrama de classes
    }

    public boolean autenticar(String emailDigitado, String senhaDigitada) {
        if (this.email != null && this.email.equalsIgnoreCase(emailDigitado)) {
            String hashTentativa = hashSenha(senhaDigitada);
            return this.senha.equals(hashTentativa);
        }
        return false;
    }

    // Getters e Setters
    public int getIdTorcedor() {
        return idTorcedor;
    }

    public void setIdTorcedor(int idTorcedor) {
        this.idTorcedor = idTorcedor;
    }

    public String getNome() {
        return nome;
    }

    public void setNome(String nome) {
        this.nome = nome;
    }

    public String getCpf() {
        return cpf;
    }

    public void setCpf(String cpf) {
        this.cpf = cpf;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getSenha() {
        return senha;
    }

    public void setSenha(String senha) {
        this.senha = senha;
    }
}
