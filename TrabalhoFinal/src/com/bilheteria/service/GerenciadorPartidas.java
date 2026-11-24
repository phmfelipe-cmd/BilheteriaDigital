package com.bilheteria.service;

import com.bilheteria.model.Assento;
import com.bilheteria.model.Partida;
import com.bilheteria.model.Setor;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class GerenciadorPartidas {
    private List<Partida> partidas;
    private int nextPartidaId = 1;
    private int nextAssentoId = 100;
    private String adminSenhaHash;

    public GerenciadorPartidas() {
        this.partidas = new ArrayList<>();
        this.adminSenhaHash = com.bilheteria.model.Torcedor.hashSenha("admin123");
        inicializarPartidasPadrao();
    }

    public boolean autenticarAdmin(String senhaDigitada) {
        return this.adminSenhaHash.equals(com.bilheteria.model.Torcedor.hashSenha(senhaDigitada));
    }

    public void alterarSenhaAdmin(String novaSenha) {
        this.adminSenhaHash = com.bilheteria.model.Torcedor.hashSenha(novaSenha);
    }

    private void inicializarPartidasPadrao() {
        // Partidas demonstrativas iniciais com simulação visual
        criarPartidaInterna(LocalDateTime.now().plusDays(2).withHour(16).withMinute(0), "Vasco da Gama", "Flamengo", "GRANDE CLÁSSICO", "ESTÁDIO NACIONAL", true);
        criarPartidaInterna(LocalDateTime.now().plusDays(6).withHour(21).withMinute(30), "Palmeiras", "São Paulo", "COPA DO BRASIL", "ALLIANZ ARENA", true);
        criarPartidaInterna(LocalDateTime.now().plusDays(12).withHour(19).withMinute(0), "Fluminense", "Boca Juniors", "COPA LIBERTADORES", "MARACANÃ", true);
    }

    public Partida criarNovaPartida(LocalDateTime dataHora, String adversario, String campeonato) {
        return criarNovaPartida(dataHora, "Clube da Casa", adversario, campeonato, "ESTÁDIO NACIONAL");
    }

    public Partida criarNovaPartida(LocalDateTime dataHora, String timeMandante, String adversario, String campeonato) {
        return criarNovaPartida(dataHora, timeMandante, adversario, campeonato, "ESTÁDIO NACIONAL");
    }

    // Método chamado pelo painel administrativo para criar novas partidas:
    // Todas as poltronas são geradas 100% DISPONÍVEIS (livres) pois acabou de ser criada!
    public Partida criarNovaPartida(LocalDateTime dataHora, String timeMandante, String adversario, String campeonato, String estadio) {
        return criarPartidaInterna(dataHora, timeMandante, adversario, campeonato, estadio, false);
    }

    private Partida criarPartidaInterna(LocalDateTime dataHora, String timeMandante, String adversario, String campeonato, String estadio, boolean simularVendasIniciais) {
        Partida partida = new Partida(nextPartidaId++, dataHora, timeMandante, adversario, campeonato, estadio);

        // Criando setores da arena
        Setor setorLesteSup = new Setor(1, "Setor Leste - Superior", 80.00);
        gerarGradeAssentos(setorLesteSup, partida.getIdPartida(), 6, 14, simularVendasIniciais);

        Setor setorLesteInf = new Setor(2, "Setor Leste - Inferior", 120.00);
        gerarGradeAssentos(setorLesteInf, partida.getIdPartida(), 6, 14, simularVendasIniciais);

        Setor setorOeste = new Setor(3, "Setor Oeste", 100.00);
        gerarGradeAssentos(setorOeste, partida.getIdPartida(), 6, 14, simularVendasIniciais);

        Setor setorNorte = new Setor(4, "Setor Norte", 60.00);
        gerarGradeAssentos(setorNorte, partida.getIdPartida(), 6, 14, simularVendasIniciais);

        Setor setorVIP = new Setor(5, "Setor VIP", 250.00);
        gerarGradeAssentos(setorVIP, partida.getIdPartida(), 4, 12, simularVendasIniciais);

        partida.adicionarSetor(setorLesteInf);
        partida.adicionarSetor(setorLesteSup);
        partida.adicionarSetor(setorOeste);
        partida.adicionarSetor(setorNorte);
        partida.adicionarSetor(setorVIP);

        partidas.add(partida);
        return partida;
    }

    private void gerarGradeAssentos(Setor setor, int idPartida, int linhas, int colunas, boolean simularVendasIniciais) {
        String[] letras = {"A", "B", "C", "D", "E", "F", "G", "H"};
        for (int r = 0; r < linhas; r++) {
            String fila = (r < letras.length) ? letras[r] : "R" + r;
            for (int c = 1; c <= colunas; c++) {
                Assento a = new Assento(nextAssentoId++, String.valueOf(c), fila, setor.getIdSetor(), idPartida);
                
                // Se for nova partida criada, NENHUM assento é marcado como vendido (todas as poltronas vazias)
                if (simularVendasIniciais) {
                    double rand = Math.random();
                    if (rand > 0.35) {
                        a.marcarComoVendido();
                    }
                }

                setor.adicionarAssento(a);
            }
        }
    }

    public List<Partida> listarPartidasFuturas() {
        LocalDateTime agora = LocalDateTime.now();
        return partidas.stream()
                .filter(p -> p.getDataHora().isAfter(agora))
                .sorted((p1, p2) -> p1.getDataHora().compareTo(p2.getDataHora()))
                .collect(Collectors.toList());
    }

    public List<Partida> listarTodasPartidas() {
        return new ArrayList<>(partidas);
    }

    public Partida buscarPorId(int idPartida) {
        for (Partida p : partidas) {
            if (p.getIdPartida() == idPartida) return p;
        }
        return null;
    }

    public boolean removerPartida(int idPartida) {
        return partidas.removeIf(p -> p.getIdPartida() == idPartida);
    }
}
