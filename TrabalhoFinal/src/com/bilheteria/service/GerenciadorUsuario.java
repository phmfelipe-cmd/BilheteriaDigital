package com.bilheteria.service;

import com.bilheteria.model.Torcedor;

import java.util.ArrayList;
import java.util.List;

public class GerenciadorUsuario {
    private List<Torcedor> torcedores;
    private int nextId = 1;

    public GerenciadorUsuario() {
        this.torcedores = new ArrayList<>();
        // Mock de torcedor inicial para testes
        cadastrarUsuario("Torcedor Exemplo", "111.222.333-44", "torcedor@teste.com", "123456");
    }

    public boolean verificarExistenciaDocumento(String cpf) {
        for (Torcedor t : torcedores) {
            if (t.getCpf() != null && t.getCpf().replaceAll("\\D", "").equals(cpf.replaceAll("\\D", ""))) {
                return true;
            }
        }
        return false;
    }

    public boolean verificarExistenciaEmail(String email) {
        for (Torcedor t : torcedores) {
            if (t.getEmail() != null && t.getEmail().equalsIgnoreCase(email)) {
                return true;
            }
        }
        return false;
    }

    public Torcedor cadastrarUsuario(String nome, String cpf, String email, String senha) {
        Torcedor torcedor = new Torcedor(nextId++, nome, cpf, email, senha);
        torcedores.add(torcedor);
        return torcedor;
    }

    public Torcedor autenticar(String email, String senha) {
        for (Torcedor t : torcedores) {
            if (t.autenticar(email, senha)) {
                return t;
            }
        }
        return null;
    }

    public boolean redefinirSenha(String cpf, String novaSenha) {
        for (Torcedor t : torcedores) {
            if (t.getCpf() != null && t.getCpf().replaceAll("\\D", "").equals(cpf.replaceAll("\\D", ""))) {
                t.setSenha(Torcedor.hashSenha(novaSenha));
                return true;
            }
        }
        return false;
    }
}
