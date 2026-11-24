package com.bilheteria.service;

import com.bilheteria.model.Torcedor;

public class ControladorCadastro {
    private GerenciadorUsuario gerenciadorUsuario;

    public ControladorCadastro(GerenciadorUsuario gerenciadorUsuario) {
        this.gerenciadorUsuario = gerenciadorUsuario;
    }

    public boolean verificarExistenciaDocumento(String cpf) {
        return gerenciadorUsuario.verificarExistenciaDocumento(cpf);
    }

    public Torcedor efetivarCriacaoConta(String nome, String cpf, String email, String senha) {
        return gerenciadorUsuario.cadastrarUsuario(nome, cpf, email, senha);
    }
}
