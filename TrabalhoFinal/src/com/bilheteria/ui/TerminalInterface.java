package com.bilheteria.ui;

import com.bilheteria.model.*;
import com.bilheteria.service.*;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;

public class TerminalInterface {
    private Scanner scanner;
    private GerenciadorUsuario gerenciadorUsuario;
    private ControladorCadastro controladorCadastro;
    private GerenciadorPartidas gerenciadorPartidas;
    private ControladorAssentos controladorAssentos;
    private ControlePagamento controlePagamento;
    private GerenciadorPedidos gerenciadorPedidos;

    private Torcedor torcedorLogado = null;
    private Pedido carrinhoAtual = null;
    private int nextIngressoId = 1;

    public TerminalInterface() {
        this.scanner = new Scanner(System.in);
        this.gerenciadorUsuario = new GerenciadorUsuario();
        this.controladorCadastro = new ControladorCadastro(gerenciadorUsuario);
        this.gerenciadorPartidas = new GerenciadorPartidas();
        this.controladorAssentos = new ControladorAssentos();
        this.controlePagamento = new ControlePagamento();
        this.gerenciadorPedidos = new GerenciadorPedidos();
    }

    public void iniciar() {
        boolean rodando = true;
        while (rodando) {
            System.out.println("\n=======================================================");
            System.out.println("        SISTEMA DE BILHETERIA DIGITAL - CLUBE          ");
            System.out.println("=======================================================");
            if (torcedorLogado != null) {
                System.out.println("Usuário conectado: " + torcedorLogado.getNome() + " (" + torcedorLogado.getEmail() + ")");
                int itensNoCarrinho = (carrinhoAtual != null) ? carrinhoAtual.getIngressos().size() : 0;
                System.out.println("Carrinho atual: " + itensNoCarrinho + " assento(s) reservado(s)");
            } else {
                System.out.println("Modo Visitante (Não autenticado)");
            }
            System.out.println("-------------------------------------------------------");
            System.out.println("1. Consultar Calendário de Jogos (RF02)");
            System.out.println("2. Selecionar Setor e Assentos [Comprar] (RF03, RF04)");
            System.out.println("3. Ver Carrinho e Finalizar Compra (RF05, RF06)");
            System.out.println("4. Meus Ingressos Comprados (RF06)");
            System.out.println("5. Login do Torcedor (RF01)");
            System.out.println("6. Cadastrar Novo Torcedor (RF01, UC01)");
            System.out.println("7. Recuperação de Senha (RF01)");
            System.out.println("8. Área Administrativa - Gerenciar Partidas (RF07)");
            System.out.println("9. Desconectar Usuário");
            System.out.println("0. Sair do Sistema");
            System.out.print("Escolha uma opção: ");

            String opcao = scanner.nextLine().trim();
            switch (opcao) {
                case "1":
                    consultarCalendarioJogos();
                    break;
                case "2":
                    fluxoSelecaoAssentos();
                    break;
                case "3":
                    fluxoCheckoutPagamento();
                    break;
                case "4":
                    visualizarMeusIngressos();
                    break;
                case "5":
                    fazerLogin();
                    break;
                case "6":
                    cadastrarTorcedor();
                    break;
                case "7":
                    recuperarSenha();
                    break;
                case "8":
                    areaAdministrativa();
                    break;
                case "9":
                    desconectar();
                    break;
                case "0":
                    limparReservasNaoPagas();
                    System.out.println("Encerrando o sistema. Até logo!");
                    rodando = false;
                    break;
                default:
                    System.out.println("Opção inválida! Tente novamente.");
            }
        }
    }

    private void consultarCalendarioJogos() {
        System.out.println("\n--- CALENDÁRIO DE JOGOS FUTUROS (RF02) ---");
        long inicio = System.currentTimeMillis();
        List<Partida> futuras = gerenciadorPartidas.listarPartidasFuturas();
        long tempoTotal = System.currentTimeMillis() - inicio;

        if (futuras.isEmpty()) {
            System.out.println("Nenhuma partida futura agendada no momento.");
        } else {
            for (Partida p : futuras) {
                System.out.println(p.toString());
            }
        }
        System.out.println("(Consulta realizada em " + tempoTotal + "ms - Atende RNF 2.1 < 2000ms)");
    }

    private void fluxoSelecaoAssentos() {
        if (torcedorLogado == null) {
            System.out.println("\n[Atenção] É necessário estar logado para selecionar assentos e adicionar ao carrinho.");
            System.out.print("Deseja fazer login agora? (S/N): ");
            String res = scanner.nextLine().trim();
            if (res.equalsIgnoreCase("S")) {
                if (!fazerLogin()) return;
            } else {
                return;
            }
        }

        List<Partida> partidas = gerenciadorPartidas.listarPartidasFuturas();
        if (partidas.isEmpty()) {
            System.out.println("Não há partidas futuras disponíveis para compra de ingressos.");
            return;
        }

        System.out.println("\n--- ESCOLHA A PARTIDA ---");
        for (Partida p : partidas) {
            System.out.println(p.toString());
        }
        System.out.print("Informe o ID da partida desejada: ");
        int idPartida;
        try {
            idPartida = Integer.parseInt(scanner.nextLine().trim());
        } catch (NumberFormatException e) {
            System.out.println("ID inválido.");
            return;
        }

        Partida partidaEscolhida = gerenciadorPartidas.buscarPorId(idPartida);
        if (partidaEscolhida == null) {
            System.out.println("Partida não encontrada.");
            return;
        }

        // Exibir setores (RNF 3.2 - Mapa do Estádio)
        System.out.println("\n================ PLANTA DO ESTÁDIO ================");
        System.out.println("             [      CAMPO DE JOGO      ]            ");
        System.out.println("----------------------------------------------------");
        System.out.println(" Setor 1: Arquibancada Norte (R$ 50,00)");
        System.out.println(" Setor 2: Arquibancada Sul   (R$ 50,00)");
        System.out.println(" Setor 3: Cadeiras Centrais Cobertas (R$ 120,00)");
        System.out.println("====================================================");

        System.out.print("Informe o número do setor desejado: ");
        int idSetor;
        try {
            idSetor = Integer.parseInt(scanner.nextLine().trim());
        } catch (NumberFormatException e) {
            System.out.println("Setor inválido.");
            return;
        }

        Setor setorEscolhido = partidaEscolhida.buscarSetorPorId(idSetor);
        if (setorEscolhido == null) {
            System.out.println("Setor não encontrado nesta partida.");
            return;
        }

        exibirMapaAssentos(setorEscolhido);

        System.out.print("Informe o ID do assento que deseja reservar (ex: 101, 102...): ");
        int idAssento;
        try {
            idAssento = Integer.parseInt(scanner.nextLine().trim());
        } catch (NumberFormatException e) {
            System.out.println("ID de assento inválido.");
            return;
        }

        Assento assentoEscolhido = null;
        for (Assento a : setorEscolhido.getAssentos()) {
            if (a.getIdAssento() == idAssento) {
                assentoEscolhido = a;
                break;
            }
        }

        if (assentoEscolhido == null) {
            System.out.println("Assento não encontrado no setor!");
            return;
        }

        // Tentar reservar (RNF 3.1 e RNF 3.3)
        boolean reservado = controladorAssentos.selecionarAssento(assentoEscolhido);
        if (!reservado) {
            System.out.println("[Erro] O assento " + assentoEscolhido.getFila() + assentoEscolhido.getNumero() +
                    " já está ocupado ou reservado por outro usuário (RNF 3.1)!");
            return;
        }

        // Tratamento de Meia-Entrada (RF04)
        System.out.print("O ingresso é destinado a Meia-Entrada (estudante/idoso/benefício)? (S/N): ");
        String respostaMeia = scanner.nextLine().trim();
        int tipoCategoria = 1;
        double valorIngresso = setorEscolhido.getPrecoBase();
        String beneficiario = torcedorLogado.getNome();
        String docBeneficiario = torcedorLogado.getCpf();

        if (respostaMeia.equalsIgnoreCase("S")) {
            tipoCategoria = 2;
            valorIngresso = setorEscolhido.getPrecoBase() / 2.0; // RNF 4.2
            System.out.println(">> Regra de Meia-Entrada aplicada: 50% de desconto! (Valor: R$ " + String.format("%.2f", valorIngresso) + ")");
            // RNF 4.1: Dados do beneficiário para comprovação de elegibilidade
            System.out.print("Informe o Nome Completo do portador da meia-entrada: ");
            beneficiario = scanner.nextLine().trim();
            System.out.print("Informe o Documento de Comprovação (RG / Carteira de Estudante / CPF): ");
            docBeneficiario = scanner.nextLine().trim();
        }

        if (carrinhoAtual == null) {
            carrinhoAtual = gerenciadorPedidos.criarPedido(torcedorLogado);
        }

        Ingresso ingresso = new Ingresso(nextIngressoId++, tipoCategoria, valorIngresso, assentoEscolhido, beneficiario, docBeneficiario);
        carrinhoAtual.adicionarIngresso(ingresso);

        System.out.println("\n[Sucesso] Assento " + assentoEscolhido.getFila() + assentoEscolhido.getNumero() +
                " bloqueado com sucesso e adicionado ao seu carrinho!");
        System.out.println("Total atual do carrinho: R$ " + String.format("%.2f", carrinhoAtual.calcularTotal()));
    }

    private void exibirMapaAssentos(Setor setor) {
        System.out.println("\n--- MAPA DE ASSENTOS: " + setor.getNomeSetor() + " ---");
        System.out.println("Legenda: [ID:FilaNum - Status] (Ex: [100:A1 - LIVRE])");
        Map<String, List<Assento>> porFileira = new TreeMap<>();
        for (Assento a : setor.getAssentos()) {
            porFileira.computeIfAbsent(a.getFila(), k -> new ArrayList<>()).add(a);
        }

        for (Map.Entry<String, List<Assento>> entry : porFileira.entrySet()) {
            System.out.print("Fileira " + entry.getKey() + ": ");
            for (Assento a : entry.getValue()) {
                String st = a.getStatus().equals("DISPONIVEL") ? "LIVRE" : a.getStatus();
                System.out.print("[" + a.getIdAssento() + ":" + a.getFila() + a.getNumero() + " (" + st + ")] ");
            }
            System.out.println();
        }
        System.out.println("----------------------------------------------------------");
    }

    private void fluxoCheckoutPagamento() {
        if (torcedorLogado == null) {
            System.out.println("Faça login para acessar o carrinho e realizar pagamentos.");
            return;
        }

        if (carrinhoAtual == null || carrinhoAtual.getIngressos().isEmpty()) {
            System.out.println("\nSeu carrinho está vazio! Selecione assentos primeiro.");
            return;
        }

        System.out.println("\n================ RESUMO DO PEDIDO ================");
        System.out.println(carrinhoAtual.obterResumo());
        System.out.println("==================================================");

        System.out.println("Opções:");
        System.out.println("1. Confirmar e Efetuar Pagamento (RF05)");
        System.out.println("2. Cancelar Pedido e Liberar Assentos (RNF 5.2)");
        System.out.println("3. Voltar");
        System.out.print("Escolha: ");
        String op = scanner.nextLine().trim();

        if (op.equals("1")) {
            System.out.println("\nSelecione o método de pagamento:");
            System.out.println("1. Cartão de Crédito");
            System.out.println("2. Cartão de Débito");
            System.out.print("Opção: ");
            String metOp = scanner.nextLine().trim();
            String metodo = metOp.equals("2") ? "CARTAO_DEBITO" : "CARTAO_CREDITO";

            System.out.println("(Simulação segura de gateway externo - RNF 5.1)");
            System.out.print("Digite o número do cartão (13 a 19 dígitos, ou 'falhar' para simular recusa): ");
            String numCartao = scanner.nextLine().trim();

            Pagamento pag = new Pagamento(carrinhoAtual.getIdPedido(), metodo, carrinhoAtual.getValorTotal());
            carrinhoAtual.setPagamento(pag);

            if (numCartao.equalsIgnoreCase("falhar") || !controlePagamento.validarDados(numCartao)) {
                System.out.println("\n[ERRO 2a: PAGAMENTO RECUSADO] Dados de cartão inválidos ou recusados pela operadora!");
                System.out.println("Deseja tentar novamente com outro cartão? (S/N): ");
                String tentar = scanner.nextLine().trim();
                if (tentar.equalsIgnoreCase("S")) {
                    fluxoCheckoutPagamento();
                } else {
                    // Liberação de assentos conforme regra de negócio
                    System.out.println("Deseja cancelar o pedido agora e liberar os assentos? (S/N): ");
                    String lib = scanner.nextLine().trim();
                    if (lib.equalsIgnoreCase("S")) {
                        liberarAssentosDoCarrinho();
                    }
                }
                return;
            }

            // Pagamento validado e processado
            boolean sucesso = controlePagamento.efetuarPagamentoPedido(carrinhoAtual, metodo, numCartao);
            if (sucesso) {
                // Marcar assentos como vendidos definitivamente
                for (Ingresso ing : carrinhoAtual.getIngressos()) {
                    controladorAssentos.confirmarVendaAssento(ing.getAssento());
                }

                System.out.println("\n=======================================================");
                System.out.println(" [PAGAMENTO APROVADO] Ingresso(s) emitido(s) com sucesso!");
                System.out.println("=======================================================");
                for (Ingresso ing : carrinhoAtual.getIngressos()) {
                    System.out.println(">>> TICKET GERADO: " + ing.toString());
                }

                // RNF 6.1: Enviar ingresso com QR Code por email
                ServicoNotificacaoEmail.enviarIngressoEmail(torcedorLogado.getEmail(), carrinhoAtual);

                // Limpa carrinho ativo após sucesso da compra
                carrinhoAtual = null;
            }
        } else if (op.equals("2")) {
            liberarAssentosDoCarrinho();
            System.out.println("Carrinho cancelado e assentos liberados com sucesso.");
        }
    }

    private void liberarAssentosDoCarrinho() {
        if (carrinhoAtual != null) {
            for (Ingresso ing : carrinhoAtual.getIngressos()) {
                controladorAssentos.desmarcarAssento(ing.getAssento());
            }
            carrinhoAtual.limparCarrinho();
            carrinhoAtual = null;
        }
    }

    private void visualizarMeusIngressos() {
        if (torcedorLogado == null) {
            System.out.println("Faça login para ver seus ingressos emitidos.");
            return;
        }

        List<Pedido> pedidos = gerenciadorPedidos.listarPedidosPagosPorTorcedor(torcedorLogado);
        System.out.println("\n================ SEUS INGRESSOS DIGITAIS ================");
        if (pedidos.isEmpty()) {
            System.out.println("Você ainda não possui ingressos comprados.");
        } else {
            for (Pedido p : pedidos) {
                System.out.println("Pedido #" + p.getIdPedido() + " - Pago em: " + p.getDataHora());
                for (Ingresso ing : p.getIngressos()) {
                    System.out.println("  * " + ing.toString());
                    System.out.println("    [QR Code para Leitura na Catraca]: " + ing.getQrCode());
                }
                System.out.println("---------------------------------------------------------");
            }
        }
    }

    private boolean fazerLogin() {
        System.out.println("\n--- LOGIN DO TORCEDOR (RF01) ---");
        System.out.print("E-mail: ");
        String email = scanner.nextLine().trim();
        System.out.print("Senha: ");
        String senha = scanner.nextLine().trim();

        Torcedor t = gerenciadorUsuario.autenticar(email, senha);
        if (t != null) {
            torcedorLogado = t;
            System.out.println("Login efetuado com sucesso! Bem-vindo(a), " + t.getNome() + ".");
            return true;
        } else {
            System.out.println("[Erro] E-mail ou senha inválidos!");
            return false;
        }
    }

    private void cadastrarTorcedor() {
        System.out.println("\n--- CADASTRO DE NOVO TORCEDOR (RF01, UC01) ---");
        System.out.print("Informe seu CPF (somente números ou formatado): ");
        String cpf = scanner.nextLine().trim();

        // Diagrama UC01: Verificar Existência Documentos
        if (controladorCadastro.verificarExistenciaDocumento(cpf)) {
            System.out.println("[Exceção 1a] Já existe uma conta cadastrada para este documento!");
            System.out.print("Deseja ir para a recuperação de senha? (S/N): ");
            String irRecup = scanner.nextLine().trim();
            if (irRecup.equalsIgnoreCase("S")) {
                recuperarSenha();
            }
            return;
        }

        System.out.print("Nome completo: ");
        String nome = scanner.nextLine().trim();
        System.out.print("E-mail: ");
        String email = scanner.nextLine().trim();

        if (gerenciadorUsuario.verificarExistenciaEmail(email)) {
            System.out.println("[Erro] Este e-mail já está em uso por outro cadastro.");
            return;
        }

        System.out.print("Cadastre uma senha de acesso: ");
        String senha = scanner.nextLine().trim();

        if (senha.length() < 4) {
            System.out.println("A senha deve ter no mínimo 4 caracteres.");
            return;
        }

        Torcedor novo = controladorCadastro.efetivarCriacaoConta(nome, cpf, email, senha);
        System.out.println("[Sucesso] Torcedor cadastrado com segurança (senha criptografada via SHA-256)!");
        torcedorLogado = novo;
    }

    private void recuperarSenha() {
        System.out.println("\n--- RECUPERAÇÃO DE ACESSO (UC01 - Variante 1a.2) ---");
        System.out.print("Informe seu documento CPF: ");
        String cpf = scanner.nextLine().trim();

        if (!gerenciadorUsuario.verificarExistenciaDocumento(cpf)) {
            System.out.println("Documento não encontrado na base de dados.");
            return;
        }

        System.out.print("Informe a NOVA senha: ");
        String novaSenha = scanner.nextLine().trim();
        gerenciadorUsuario.redefinirSenha(cpf, novaSenha);
        System.out.println("[Sucesso] Senha redefinida com sucesso! Você já pode efetuar login.");
    }

    private void areaAdministrativa() {
        System.out.println("\n--- ACESSO RESTRITO: ADMINISTRADOR (RF07) ---");
        System.out.print("Digite a senha de administrador (padrão: admin123): ");
        String senhaAdmin = scanner.nextLine().trim();

        if (!gerenciadorPartidas.autenticarAdmin(senhaAdmin)) {
            System.out.println("[Acesso Negado] Senha de administrador incorreta.");
            return;
        }

        boolean noAdmin = true;
        while (noAdmin) {
            System.out.println("\n--- PAINEL DE GERENCIAMENTO DE PARTIDAS ---");
            System.out.println("1. Listar todas as partidas cadastradas");
            System.out.println("2. Adicionar nova partida ao calendário");
            System.out.println("3. Remover partida do calendário");
            System.out.println("4. Alterar senha de administrador");
            System.out.println("0. Voltar ao menu principal");
            System.out.print("Escolha: ");
            String op = scanner.nextLine().trim();

            switch (op) {
                case "1":
                    List<Partida> lista = gerenciadorPartidas.listarTodasPartidas();
                    for (Partida p : lista) {
                        System.out.println(p.toString());
                    }
                    break;
                case "2":
                    try {
                        System.out.print("Time adversário: ");
                        String adv = scanner.nextLine().trim();
                        System.out.print("Nome do campeonato (ex: Copa do Brasil): ");
                        String camp = scanner.nextLine().trim();
                        System.out.print("Data e Hora (formato dd/MM/yyyy HH:mm, ex: 10/12/2026 16:00): ");
                        String dataStr = scanner.nextLine().trim();
                        DateTimeFormatter dtf = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");
                        LocalDateTime dth = LocalDateTime.parse(dataStr, dtf);

                        Partida nova = gerenciadorPartidas.criarNovaPartida(dth, adv, camp);
                        System.out.println("[Sucesso] Partida criada com setores e assentos gerados: " + nova);
                    } catch (Exception e) {
                        System.out.println("[Erro] Formato de data inválido. Use dd/MM/yyyy HH:mm");
                    }
                    break;
                case "3":
                    System.out.print("ID da partida a remover: ");
                    try {
                        int idRem = Integer.parseInt(scanner.nextLine().trim());
                        if (gerenciadorPartidas.removerPartida(idRem)) {
                            System.out.println("Partida removida com sucesso!");
                        } else {
                            System.out.println("Partida não encontrada.");
                        }
                    } catch (Exception e) {
                        System.out.println("ID inválido.");
                    }
                    break;
                case "4":
                    System.out.print("Nova senha de administrador: ");
                    String ns = scanner.nextLine().trim();
                    gerenciadorPartidas.alterarSenhaAdmin(ns);
                    System.out.println("Senha de administrador alterada com sucesso!");
                    break;
                case "0":
                    noAdmin = false;
                    break;
                default:
                    System.out.println("Opção inválida.");
            }
        }
    }

    private void desconectar() {
        if (torcedorLogado != null) {
            liberarAssentosDoCarrinho();
            System.out.println("Torcedor " + torcedorLogado.getNome() + " desconectado.");
            torcedorLogado = null;
        } else {
            System.out.println("Nenhum usuário conectado.");
        }
    }

    private void limparReservasNaoPagas() {
        if (carrinhoAtual != null) {
            liberarAssentosDoCarrinho();
        }
    }
}
