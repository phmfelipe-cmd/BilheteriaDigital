package com.bilheteria.ui;

import com.bilheteria.model.*;
import com.bilheteria.service.*;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.JTableHeader;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.List;

public class JanelaPrincipal extends JFrame {
    private GerenciadorUsuario gerenciadorUsuario;
    private ControladorCadastro controladorCadastro;
    private GerenciadorPartidas gerenciadorPartidas;
    private ControladorAssentos controladorAssentos;
    private ControlePagamento controlePagamento;
    private GerenciadorPedidos gerenciadorPedidos;

    private Torcedor torcedorLogado = null;
    private Pedido carrinhoAtual = null;
    private int nextIngressoId = 1;

    // ── Paleta Premium Dark ──
    static final Color BG_DEEP       = new Color(10, 14, 23);
    static final Color BG_BASE       = new Color(15, 19, 32);
    static final Color BG_RAISED     = new Color(22, 28, 45);
    static final Color BG_SURFACE    = new Color(28, 35, 54);
    static final Color BORDER_SUBTLE = new Color(40, 50, 72);
    static final Color BORDER_HOVER  = new Color(60, 75, 110);

    static final Color TEXT_PRIMARY   = new Color(237, 242, 247);
    static final Color TEXT_SECONDARY = new Color(140, 155, 180);
    static final Color TEXT_TERTIARY  = new Color(90, 105, 130);

    static final Color ACCENT_GREEN  = new Color(52, 211, 153);
    static final Color ACCENT_BLUE   = new Color(96, 165, 250);
    static final Color ACCENT_CYAN   = new Color(34, 211, 238);
    static final Color ACCENT_RED    = new Color(248, 113, 113);
    static final Color ACCENT_AMBER  = new Color(251, 191, 36);
    static final Color ACCENT_PURPLE = new Color(167, 139, 250);

    static final Color SEAT_FREE     = new Color(96, 165, 250);
    static final Color SEAT_TAKEN    = new Color(45, 55, 75);
    static final Color SEAT_SELECTED = new Color(52, 211, 153);

    // ── Navegação ──
    private CardLayout cardLayout;
    private JPanel painelConteudo;
    private JLabel lblStatusUsuario, lblStatusCarrinho;
    private JButton btnLoginMenu, btnLogoutMenu;
    private String navAtiva = "JOGOS";
    private final List<JButton> botoesSidebar = new ArrayList<>();

    public JanelaPrincipal() {
        gerenciadorUsuario  = new GerenciadorUsuario();
        controladorCadastro = new ControladorCadastro(gerenciadorUsuario);
        gerenciadorPartidas = new GerenciadorPartidas();
        controladorAssentos = new ControladorAssentos();
        controlePagamento   = new ControlePagamento();
        gerenciadorPedidos  = new GerenciadorPedidos();

        configurarJanela();
        montarLayout();
    }

    private void configurarJanela() {
        setTitle("Bilheteria Digital \u2026 Plataforma de Ingressos Esportivos");
        setSize(1280, 860);
        setMinimumSize(new Dimension(1100, 740));
        setLocationRelativeTo(null);
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        getContentPane().setBackground(BG_DEEP);
    }

    private void montarLayout() {
        setLayout(new BorderLayout());
        add(criarHeader(), BorderLayout.NORTH);
        add(criarSidebar(), BorderLayout.WEST);

        cardLayout = new CardLayout();
        painelConteudo = new JPanel(cardLayout);
        painelConteudo.setBackground(BG_BASE);

        painelConteudo.add(criarPainelJogos(),       "JOGOS");
        painelConteudo.add(criarPainelEstadio(),      "ESTADIO");
        painelConteudo.add(criarPainelCarrinho(),     "CARRINHO");
        painelConteudo.add(criarPainelMeusIngressos(),"MEUS_INGRESSOS");
        painelConteudo.add(criarPainelAdmin(),        "ADMIN");

        add(painelConteudo, BorderLayout.CENTER);
        navegarPara("JOGOS");
    }

    // ================================================================
    //  COMPONENTES REUTILIZÁVEIS
    // ================================================================

    /** Painel com cantos arredondados e borda sutil. */
    static class PainelArredondado extends JPanel {
        private final int raio;
        private Color corFundo;
        private Color corBorda;

        PainelArredondado(int raio, Color fundo, Color borda) {
            this.raio = raio;
            this.corFundo = fundo;
            this.corBorda = borda;
            setOpaque(false);
        }
        PainelArredondado(int raio) { this(raio, BG_SURFACE, BORDER_SUBTLE); }
        PainelArredondado() { this(16); }

        public void setCorFundo(Color c) { corFundo = c; repaint(); }
        public void setCorBorda(Color c) { corBorda = c; repaint(); }

        @Override protected void paintComponent(Graphics g) {
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            g2.setColor(corFundo);
            g2.fillRoundRect(0, 0, getWidth(), getHeight(), raio, raio);
            if (corBorda != null) {
                g2.setColor(corBorda);
                g2.setStroke(new BasicStroke(1f));
                g2.drawRoundRect(0, 0, getWidth()-1, getHeight()-1, raio, raio);
            }
            g2.dispose();
            super.paintComponent(g);
        }
    }

    /** Botão com cantos arredondados, hover suave. */
    private JButton criarBotao(String texto, Color bg, Color fg) {
        JButton btn = new JButton(texto) {
            boolean hover = false;
            {
                addMouseListener(new MouseAdapter() {
                    public void mouseEntered(MouseEvent e) { hover = true; repaint(); }
                    public void mouseExited(MouseEvent e)  { hover = false; repaint(); }
                });
            }
            @Override protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                Color base = hover ? bg.brighter() : bg;
                g2.setColor(base);
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), 12, 12);
                g2.dispose();
                super.paintComponent(g);
            }
        };
        btn.setFont(new Font("Segoe UI", Font.BOLD, 13));
        btn.setForeground(fg);
        btn.setContentAreaFilled(false);
        btn.setBorderPainted(false);
        btn.setFocusPainted(false);
        btn.setBorder(new EmptyBorder(10, 20, 10, 20));
        btn.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        return btn;
    }

    /** Campo de texto estilizado para o tema dark. */
    private JTextField criarCampo(String placeholder) {
        JTextField field = new JTextField(placeholder);
        field.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        field.setBackground(BG_RAISED);
        field.setForeground(TEXT_PRIMARY);
        field.setCaretColor(ACCENT_CYAN);
        field.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(BORDER_SUBTLE, 1, true),
            new EmptyBorder(10, 14, 10, 14)
        ));
        return field;
    }

    private JPasswordField criarCampoSenha(String txt) {
        JPasswordField f = new JPasswordField(txt);
        f.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        f.setBackground(BG_RAISED);
        f.setForeground(TEXT_PRIMARY);
        f.setCaretColor(ACCENT_CYAN);
        f.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(BORDER_SUBTLE, 1, true),
            new EmptyBorder(10, 14, 10, 14)
        ));
        return f;
    }

    private JLabel criarLabel(String txt, int size, int style, Color cor) {
        JLabel l = new JLabel(txt);
        l.setFont(new Font("Segoe UI", style, size));
        l.setForeground(cor);
        return l;
    }

    private void navegarPara(String destino) {
        navAtiva = destino;
        if ("CARRINHO".equals(destino)) atualizarPainelCarrinho();
        else if ("MEUS_INGRESSOS".equals(destino)) atualizarPainelMeusIngressos();
        else if ("JOGOS".equals(destino)) atualizarTabelaJogos();
        cardLayout.show(painelConteudo, destino);
        atualizarEstadoSidebar();
    }

    private void atualizarEstadoSidebar() {
        for (JButton b : botoesSidebar) {
            String tag = (String) b.getClientProperty("destino");
            boolean ativo = navAtiva.equals(tag);
            b.setForeground(ativo ? ACCENT_CYAN : TEXT_SECONDARY);
            b.setBackground(ativo ? new Color(22, 36, 56) : BG_DEEP);
        }
    }

    // ================================================================
    //  HEADER
    // ================================================================
    private JPanel criarHeader() {
        JPanel header = new JPanel(new BorderLayout()) {
            @Override protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                GradientPaint gp = new GradientPaint(0, 0, new Color(14, 20, 38), getWidth(), 0, new Color(20, 30, 55));
                g2.setPaint(gp);
                g2.fillRect(0, 0, getWidth(), getHeight());
                // Linha inferior com brilho sutil
                g2.setColor(new Color(ACCENT_CYAN.getRed(), ACCENT_CYAN.getGreen(), ACCENT_CYAN.getBlue(), 40));
                g2.fillRect(0, getHeight()-2, getWidth(), 2);
                g2.dispose();
            }
        };
        header.setPreferredSize(new Dimension(0, 68));
        header.setBorder(new EmptyBorder(0, 24, 0, 24));

        // Marca
        JPanel marca = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 14));
        marca.setOpaque(false);

        JLabel icone = criarLabel("\ud83c\udfab", 22, Font.PLAIN, ACCENT_CYAN);
        JLabel titulo = criarLabel("BILHETERIA DIGITAL", 17, Font.BOLD, TEXT_PRIMARY);
        JLabel sub = criarLabel("Plataforma de Ingressos", 12, Font.PLAIN, TEXT_TERTIARY);

        marca.add(icone); marca.add(titulo); marca.add(sub);

        // Direita
        JPanel dir = new JPanel(new FlowLayout(FlowLayout.RIGHT, 14, 12));
        dir.setOpaque(false);

        // Ícone de carrinho estilizado sem emojis problemáticos
        lblStatusCarrinho = new JLabel("0 itens") {
            @Override protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                Font emojiFont = new Font("Segoe UI Emoji", Font.PLAIN, 15);
                g2.setFont(emojiFont);
                g2.setColor(ACCENT_GREEN);
                g2.drawString("🛒", 0, getHeight() / 2 + 5);
                g2.setFont(new Font("Segoe UI", Font.BOLD, 13));
                g2.drawString(getText(), 24, getHeight() / 2 + 5);
                g2.dispose();
            }
        };
        lblStatusCarrinho.setPreferredSize(new Dimension(100, 30));
        lblStatusCarrinho.setForeground(ACCENT_GREEN);
        lblStatusCarrinho.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        lblStatusCarrinho.addMouseListener(new MouseAdapter() {
            public void mouseClicked(MouseEvent e) { navegarPara("CARRINHO"); }
        });

        lblStatusUsuario = criarLabel("Visitante", 13, Font.PLAIN, TEXT_SECONDARY);

        btnLoginMenu = criarBotao("Entrar", ACCENT_GREEN, BG_DEEP);
        btnLoginMenu.addActionListener(e -> abrirDialogLogin());

        btnLogoutMenu = criarBotao("Sair", new Color(60, 30, 30), ACCENT_RED);
        btnLogoutMenu.setVisible(false);
        btnLogoutMenu.addActionListener(e -> realizarLogout());

        dir.add(lblStatusCarrinho);
        dir.add(criarSeparadorVertical());
        dir.add(lblStatusUsuario);
        dir.add(btnLoginMenu);
        dir.add(btnLogoutMenu);

        header.add(marca, BorderLayout.WEST);
        header.add(dir, BorderLayout.EAST);
        return header;
    }

    private JPanel criarSeparadorVertical() {
        JPanel sep = new JPanel() {
            @Override protected void paintComponent(Graphics g) {
                g.setColor(BORDER_SUBTLE);
                g.fillRect(0, 4, 1, getHeight()-8);
            }
        };
        sep.setPreferredSize(new Dimension(1, 30));
        sep.setOpaque(false);
        return sep;
    }

    // ================================================================
    //  SIDEBAR
    // ================================================================
    private JPanel criarSidebar() {
        JPanel sb = new JPanel();
        sb.setLayout(new BoxLayout(sb, BoxLayout.Y_AXIS));
        sb.setBackground(BG_DEEP);
        sb.setPreferredSize(new Dimension(240, 0));
        sb.setBorder(BorderFactory.createMatteBorder(0, 0, 0, 1, BORDER_SUBTLE));

        sb.add(Box.createVerticalStrut(28));
        sb.add(criarLabelSecao("NAVEGAÇÃO"));
        sb.add(Box.createVerticalStrut(10));
        sb.add(criarItemSidebar("📅", "Calendário de Jogos",   "JOGOS"));
        sb.add(criarItemSidebar("🏟", "Mapa do Estádio",         "ESTADIO"));
        sb.add(criarItemSidebar("🛒", "Carrinho & Checkout",   "CARRINHO"));
        sb.add(criarItemSidebar("🎟", "Meus Ingressos",       "MEUS_INGRESSOS"));

        sb.add(Box.createVerticalStrut(28));
        sb.add(criarLabelSecao("SISTEMA"));
        sb.add(Box.createVerticalStrut(10));
        sb.add(criarItemSidebar("⚙", "Administração",         "ADMIN"));

        sb.add(Box.createVerticalGlue());

        // Versão no rodapé
        JLabel ver = criarLabel("  v1.0 \u2022 Java Swing", 11, Font.PLAIN, TEXT_TERTIARY);
        ver.setBorder(new EmptyBorder(0, 20, 16, 0));
        ver.setAlignmentX(Component.LEFT_ALIGNMENT);
        sb.add(ver);

        return sb;
    }

    private JLabel criarLabelSecao(String txt) {
        JLabel l = criarLabel("    " + txt, 11, Font.BOLD, TEXT_TERTIARY);
        l.setAlignmentX(Component.LEFT_ALIGNMENT);
        return l;
    }

    private JButton criarItemSidebar(String iconeEmoji, String texto, String destino) {
        JButton btn = new JButton() {
            @Override protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                boolean ativo = navAtiva.equals(destino);
                g2.setColor(getBackground());
                g2.fillRoundRect(6, 2, getWidth()-12, getHeight()-4, 10, 10);
                if (ativo) {
                    g2.setColor(ACCENT_CYAN);
                    g2.fillRoundRect(10, 6, 3, getHeight()-12, 3, 3);
                }

                // Renderiza ícone com fonte Emoji dedicada e texto com Segoe UI
                Font emojiFont = new Font("Segoe UI Emoji", Font.PLAIN, 15);
                Font textFont = new Font("Segoe UI", ativo ? Font.BOLD : Font.PLAIN, 13);

                g2.setFont(emojiFont);
                g2.setColor(ativo ? ACCENT_CYAN : TEXT_SECONDARY);
                g2.drawString(iconeEmoji, 22, getHeight() / 2 + 5);

                g2.setFont(textFont);
                g2.setColor(ativo ? TEXT_PRIMARY : TEXT_SECONDARY);
                g2.drawString(texto, 48, getHeight() / 2 + 5);

                g2.dispose();
            }
        };
        btn.putClientProperty("destino", destino);
        btn.setBackground(BG_DEEP);
        btn.setBorder(new EmptyBorder(12, 10, 12, 10));
        btn.setHorizontalAlignment(SwingConstants.LEFT);
        btn.setMaximumSize(new Dimension(240, 44));
        btn.setFocusPainted(false);
        btn.setContentAreaFilled(false);
        btn.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));

        btn.addMouseListener(new MouseAdapter() {
            public void mouseEntered(MouseEvent e) {
                if (!navAtiva.equals(destino)) {
                    btn.setBackground(BG_RAISED);
                    btn.repaint();
                }
            }
            public void mouseExited(MouseEvent e) {
                if (!navAtiva.equals(destino)) {
                    btn.setBackground(BG_DEEP);
                    btn.repaint();
                }
            }
        });

        btn.addActionListener(e -> navegarPara(destino));
        botoesSidebar.add(btn);
        return btn;
    }

    // ================================================================
    //  1. CALENDÁRIO DE JOGOS (RF02)
    // ================================================================
    private DefaultTableModel modeloJogos;
    private JTable tabelaJogos;
    private JLabel lblTempo;

    private JPanel criarPainelJogos() {
        JPanel p = new JPanel(new BorderLayout(0, 18));
        p.setBackground(BG_BASE);
        p.setBorder(new EmptyBorder(28, 32, 24, 32));

        // Cabeçalho
        JPanel cab = new JPanel(new BorderLayout(0, 4));
        cab.setOpaque(false);
        cab.add(criarLabel("Calendário de Partidas", 24, Font.BOLD, TEXT_PRIMARY), BorderLayout.NORTH);
        lblTempo = criarLabel("", 12, Font.ITALIC, TEXT_TERTIARY);
        cab.add(lblTempo, BorderLayout.SOUTH);
        p.add(cab, BorderLayout.NORTH);

        // Tabela
        String[] cols = {"ID", "Competição", "Confronto", "Data & Horário", "Local", "Status"};
        modeloJogos = new DefaultTableModel(cols, 0) {
            public boolean isCellEditable(int r, int c) { return false; }
        };
        tabelaJogos = estilizarTabela(new JTable(modeloJogos));

        PainelArredondado wrap = new PainelArredondado(14, BG_RAISED, BORDER_SUBTLE);
        wrap.setLayout(new BorderLayout());
        JScrollPane sc = new JScrollPane(tabelaJogos);
        sc.getViewport().setBackground(BG_RAISED);
        sc.setBorder(null);
        wrap.add(sc);
        p.add(wrap, BorderLayout.CENTER);

        // CTA
        JPanel rodape = new JPanel(new FlowLayout(FlowLayout.RIGHT, 0, 8));
        rodape.setOpaque(false);
        JButton btn = criarBotao("Selecionar Partida e Ver Assentos  \u279C", ACCENT_GREEN, BG_DEEP);
        btn.setFont(new Font("Segoe UI", Font.BOLD, 14));
        btn.setPreferredSize(new Dimension(340, 46));
        btn.addActionListener(e -> {
            int row = tabelaJogos.getSelectedRow();
            if (row == -1) { mostrarAviso("Selecione uma partida na tabela."); return; }
            abrirEstadio((int) modeloJogos.getValueAt(row, 0));
        });
        rodape.add(btn);
        p.add(rodape, BorderLayout.SOUTH);

        atualizarTabelaJogos();
        return p;
    }

    private void atualizarTabelaJogos() {
        long t0 = System.currentTimeMillis();
        List<Partida> fut = gerenciadorPartidas.listarPartidasFuturas();
        long dt = System.currentTimeMillis() - t0;
        modeloJogos.setRowCount(0);
        DateTimeFormatter dtf = DateTimeFormatter.ofPattern("dd/MM/yyyy '\u00e0s' HH:mm");
        for (Partida p : fut)
            modeloJogos.addRow(new Object[]{ p.getIdPartida(), p.getCampeonato(),
                p.getNomeConfronto(), p.getDataHora().format(dtf),
                p.getEstadio(), "\u2705 Aberto" });
        if (lblTempo != null) lblTempo.setText("Consulta em " + dt + "ms  \u2022  Apenas jogos futuros (RNF 2.1 / 2.2)");
    }

    // ================================================================
    //  2. MAPA DO ESTÁDIO E ASSENTOS (RF03 / RF04)
    // ================================================================
    private Partida partidaSelecionada;
    private Setor setorSelecionado;
    private Assento assentoSel;
    private JLabel lblConfronto, lblSubConfronto, lblSetorNome;
    private JPanel gradeAssentos;
    private JLabel lblResumoTitulo, lblResumoSub, lblResumoPreco;
    private JButton btnProsseguir;

    private JPanel criarPainelEstadio() {
        JPanel p = new JPanel(new BorderLayout(0, 14));
        p.setBackground(BG_BASE);
        p.setBorder(new EmptyBorder(18, 28, 16, 28));

        // ── Topo: informação do confronto ──
        PainelArredondado topo = new PainelArredondado(14, BG_SURFACE, BORDER_SUBTLE);
        topo.setLayout(new BorderLayout(12, 0));
        topo.setBorder(new EmptyBorder(14, 20, 14, 20));

        JButton btnVoltar = criarBotao("\u2190  Jogos", BG_RAISED, TEXT_SECONDARY);
        btnVoltar.addActionListener(e -> navegarPara("JOGOS"));

        JPanel info = new JPanel(new GridLayout(2, 1, 0, 2));
        info.setOpaque(false);
        lblConfronto = criarLabel("Selecione uma partida", 16, Font.BOLD, TEXT_PRIMARY);
        lblConfronto.setHorizontalAlignment(SwingConstants.CENTER);
        lblSubConfronto = criarLabel("", 12, Font.PLAIN, TEXT_TERTIARY);
        lblSubConfronto.setHorizontalAlignment(SwingConstants.CENTER);
        info.add(lblConfronto); info.add(lblSubConfronto);

        lblSetorNome = criarLabel("", 13, Font.BOLD, ACCENT_CYAN);
        lblSetorNome.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(BORDER_SUBTLE, 1, true),
            new EmptyBorder(6, 16, 6, 16)));

        topo.add(btnVoltar, BorderLayout.WEST);
        topo.add(info, BorderLayout.CENTER);
        topo.add(lblSetorNome, BorderLayout.EAST);
        p.add(topo, BorderLayout.NORTH);

        // ── Centro: Estádio + Grade ──
        JPanel centro = new JPanel(new GridLayout(1, 2, 18, 0));
        centro.setOpaque(false);

        // Esquerda: planta do estádio + seletor de setores
        PainelArredondado colE = new PainelArredondado(14, BG_SURFACE, BORDER_SUBTLE);
        colE.setLayout(new BorderLayout(0, 10));
        colE.setBorder(new EmptyBorder(14, 14, 14, 14));
        colE.add(criarLabel("Clique no setor do mapa ou nos botões abaixo:", 12, Font.PLAIN, TEXT_TERTIARY), BorderLayout.NORTH);
        colE.add(criarDesenhoEstadio(), BorderLayout.CENTER);
        colE.add(criarBotoesRapidosSetores(), BorderLayout.SOUTH);

        // Direita: grade de assentos
        PainelArredondado colD = new PainelArredondado(14, BG_SURFACE, BORDER_SUBTLE);
        colD.setLayout(new BorderLayout(0, 10));
        colD.setBorder(new EmptyBorder(14, 14, 14, 14));
        colD.add(criarLabel("Mapa de Assentos", 14, Font.BOLD, TEXT_PRIMARY), BorderLayout.NORTH);

        gradeAssentos = new JPanel();
        gradeAssentos.setOpaque(false);
        JScrollPane scGrade = new JScrollPane(gradeAssentos);
        scGrade.getViewport().setOpaque(false);
        scGrade.setOpaque(false);
        scGrade.setBorder(null);
        colD.add(scGrade, BorderLayout.CENTER);

        // Legenda
        JPanel leg = new JPanel(new FlowLayout(FlowLayout.CENTER, 24, 4));
        leg.setOpaque(false);
        leg.add(criarLegenda("Ocupados",    SEAT_TAKEN));
        leg.add(criarLegenda("Dispon\u00edveis", SEAT_FREE));
        leg.add(criarLegenda("Selecionado", SEAT_SELECTED));
        colD.add(leg, BorderLayout.SOUTH);

        centro.add(colE); centro.add(colD);
        p.add(centro, BorderLayout.CENTER);

        // ── Rodapé: resumo de compra ──
        PainelArredondado rodape = new PainelArredondado(14, BG_SURFACE, BORDER_SUBTLE);
        rodape.setLayout(new BorderLayout(20, 0));
        rodape.setBorder(new EmptyBorder(14, 24, 14, 24));
        rodape.setPreferredSize(new Dimension(0, 72));

        JPanel esq = new JPanel(new GridLayout(2, 1));
        esq.setOpaque(false);
        lblResumoTitulo = criarLabel("Nenhum assento selecionado", 14, Font.BOLD, TEXT_PRIMARY);
        lblResumoSub = criarLabel("Clique em um assento azul", 12, Font.PLAIN, TEXT_TERTIARY);
        esq.add(lblResumoTitulo); esq.add(lblResumoSub);

        JPanel dirR = new JPanel(new FlowLayout(FlowLayout.RIGHT, 20, 6));
        dirR.setOpaque(false);
        lblResumoPreco = criarLabel("R$ 0,00", 20, Font.BOLD, TEXT_PRIMARY);
        btnProsseguir = criarBotao("PROSSEGUIR PARA O PAGAMENTO", ACCENT_GREEN, BG_DEEP);
        btnProsseguir.setFont(new Font("Segoe UI", Font.BOLD, 13));
        btnProsseguir.setPreferredSize(new Dimension(280, 42));
        btnProsseguir.setEnabled(false);
        btnProsseguir.addActionListener(e -> confirmarAssentoECarrinho());
        dirR.add(lblResumoPreco); dirR.add(btnProsseguir);

        rodape.add(esq, BorderLayout.WEST);
        rodape.add(dirR, BorderLayout.EAST);
        p.add(rodape, BorderLayout.SOUTH);

        return p;
    }

    private JPanel painelBotoesSetores;

    private JPanel criarBotoesRapidosSetores() {
        painelBotoesSetores = new JPanel(new FlowLayout(FlowLayout.CENTER, 6, 4));
        painelBotoesSetores.setOpaque(false);
        atualizarBotoesRapidosSetores();
        return painelBotoesSetores;
    }

    private void atualizarBotoesRapidosSetores() {
        if (painelBotoesSetores == null) return;
        painelBotoesSetores.removeAll();
        if (partidaSelecionada != null && !partidaSelecionada.getSetores().isEmpty()) {
            for (Setor s : partidaSelecionada.getSetores()) {
                boolean ativo = (setorSelecionado != null && setorSelecionado.getIdSetor() == s.getIdSetor());
                JButton b = new JButton(s.getNomeSetor()) {
                    @Override protected void paintComponent(Graphics g) {
                        Graphics2D g2 = (Graphics2D) g.create();
                        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                        g2.setColor(ativo ? ACCENT_CYAN : BG_RAISED);
                        g2.fillRoundRect(0, 0, getWidth(), getHeight(), 8, 8);
                        g2.setColor(ativo ? BG_DEEP : TEXT_SECONDARY);
                        g2.setFont(new Font("Segoe UI", ativo ? Font.BOLD : Font.PLAIN, 11));
                        FontMetrics fm = g2.getFontMetrics();
                        int x = (getWidth() - fm.stringWidth(getText())) / 2;
                        int y = (getHeight() + fm.getAscent() - fm.getDescent()) / 2;
                        g2.drawString(getText(), x, y);
                        g2.dispose();
                    }
                };
                b.setPreferredSize(new Dimension(100, 28));
                b.setContentAreaFilled(false);
                b.setBorderPainted(false);
                b.setFocusPainted(false);
                b.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
                b.setToolTipText("R$ " + String.format("%.2f", s.getPrecoBase()));
                b.addActionListener(e -> trocarSetor(s));
                painelBotoesSetores.add(b);
            }
        }
        painelBotoesSetores.revalidate();
        painelBotoesSetores.repaint();
    }

    private JPanel criarDesenhoEstadio() {
        JPanel stadium = new JPanel() {
            @Override protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

                int w = getWidth(), h = getHeight();
                int cx = w/2, cy = h/2 - 8;
                int ew = Math.min(w-50, 340), eh = Math.min(h-70, 210);

                // Glow atrás do estádio
                g2.setColor(new Color(ACCENT_CYAN.getRed(), ACCENT_CYAN.getGreen(), ACCENT_CYAN.getBlue(), 12));
                g2.fillOval(cx-ew/2-15, cy-eh/2-15, ew+30, eh+30);

                // Setores como arcos
                int[][] setores = {{45,90}, {225,90}, {135,90}, {315,90}};
                Color[] cores = {new Color(65,135,210), new Color(180,195,220), new Color(230,110,60), new Color(55,185,120)};
                for (int i = 0; i < 4; i++) {
                    g2.setColor(cores[i]);
                    g2.fillArc(cx-ew/2, cy-eh/2, ew, eh, setores[i][0], setores[i][1]);
                }

                // Separadores
                g2.setColor(BG_SURFACE);
                g2.setStroke(new BasicStroke(3.5f));
                g2.drawOval(cx-ew/2, cy-eh/2, ew, eh);

                // Gramado
                int gw = ew/2-16, gh = eh/2-16;
                g2.setColor(new Color(34,130,70));
                g2.fillRoundRect(cx-gw/2, cy-gh/2, gw, gh, 12, 12);
                g2.setColor(new Color(255,255,255,120));
                g2.setStroke(new BasicStroke(1.2f));
                g2.drawRoundRect(cx-gw/2+5, cy-gh/2+5, gw-10, gh-10, 8, 8);
                g2.drawLine(cx, cy-gh/2+5, cx, cy+gh/2-5);
                g2.drawOval(cx-16, cy-16, 32, 32);

                // VIP
                g2.setColor(ACCENT_AMBER);
                g2.fillRoundRect(cx-gw/2+14, cy+gh/2+10, gw-28, 20, 10, 10);
                g2.setColor(BG_DEEP);
                g2.setFont(new Font("Segoe UI", Font.BOLD, 10));
                FontMetrics fm = g2.getFontMetrics();
                g2.drawString("VIP", cx-fm.stringWidth("VIP")/2, cy+gh/2+24);

                // Labels dos setores
                g2.setColor(Color.WHITE);
                g2.setFont(new Font("Segoe UI", Font.BOLD, 11));
                fm = g2.getFontMetrics();
                g2.drawString("Leste (Sup)", cx-fm.stringWidth("Leste (Sup)")/2, cy-eh/2+22);
                g2.drawString("Leste (Inf)", cx-fm.stringWidth("Leste (Inf)")/2, cy+eh/2-10);
                g2.drawString("Oeste", cx-ew/2+10, cy+4);
                g2.drawString("Norte", cx+ew/2-fm.stringWidth("Norte")-10, cy+4);

                g2.dispose();
            }
        };
        stadium.setOpaque(false);
        stadium.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        stadium.addMouseListener(new MouseAdapter() {
            @Override public void mouseClicked(MouseEvent e) {
                if (partidaSelecionada == null || partidaSelecionada.getSetores().isEmpty()) return;
                int mx = e.getX();
                int my = e.getY();
                int cx = stadium.getWidth() / 2;
                int cy = stadium.getHeight() / 2 - 8;

                // Identifica se clicou no quadrante correspondente a cada setor
                int dx = mx - cx;
                int dy = my - cy;

                // Zona VIP (abaixo do campo perto do centro)
                if (Math.abs(dx) <= 60 && dy >= 20 && dy <= 60) {
                    selecionarSetorPorNome("VIP");
                    return;
                }
                // Setor Norte (Lado direito)
                if (dx > 40 && Math.abs(dy) <= 50) {
                    selecionarSetorPorNome("Norte");
                    return;
                }
                // Setor Oeste (Lado esquerdo)
                if (dx < -40 && Math.abs(dy) <= 50) {
                    selecionarSetorPorNome("Oeste");
                    return;
                }
                // Setor Leste Superior (Topo)
                if (dy < -20) {
                    selecionarSetorPorNome("Superior");
                    return;
                }
                // Setor Leste Inferior (Base)
                if (dy > 20) {
                    selecionarSetorPorNome("Inferior");
                    return;
                }

                // Fallback de rotação se clicou no centro
                List<Setor> ss = partidaSelecionada.getSetores();
                int idx = ss.indexOf(setorSelecionado);
                trocarSetor(ss.get((idx+1)%ss.size()));
            }
        });
        return stadium;
    }

    private void selecionarSetorPorNome(String termo) {
        if (partidaSelecionada == null) return;
        for (Setor s : partidaSelecionada.getSetores()) {
            if (s.getNomeSetor().toLowerCase().contains(termo.toLowerCase())) {
                trocarSetor(s);
                return;
            }
        }
    }

    private JPanel criarLegenda(String txt, Color cor) {
        JPanel p = new JPanel(new FlowLayout(FlowLayout.LEFT, 6, 0));
        p.setOpaque(false);
        JPanel dot = new JPanel() {
            @Override protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(cor); g2.fillOval(0, 0, 11, 11);
                g2.dispose();
            }
        };
        dot.setPreferredSize(new Dimension(11, 11));
        dot.setOpaque(false);
        p.add(dot); p.add(criarLabel(txt, 12, Font.PLAIN, TEXT_TERTIARY));
        return p;
    }

    private void abrirEstadio(int id) {
        partidaSelecionada = gerenciadorPartidas.buscarPorId(id);
        if (partidaSelecionada == null) return;
        DateTimeFormatter dtf = DateTimeFormatter.ofPattern("EEE, dd MMM - HH:mm", Locale.of("pt", "BR"));
        lblConfronto.setText(partidaSelecionada.getCampeonato().toUpperCase() + "  \u2022  " + partidaSelecionada.getNomeConfronto().toUpperCase());
        lblSubConfronto.setText(partidaSelecionada.getEstadio().toUpperCase() + "  \u2022  " + partidaSelecionada.getDataHora().format(dtf).toUpperCase());
        if (!partidaSelecionada.getSetores().isEmpty()) trocarSetor(partidaSelecionada.getSetores().get(0));
        navegarPara("ESTADIO");
    }

    private void trocarSetor(Setor s) {
        setorSelecionado = s;
        assentoSel = null;
        lblSetorNome.setText(s.getNomeSetor() + "  \u2022  R$ " + String.format("%.2f", s.getPrecoBase()));
        atualizarBotoesRapidosSetores();
        renderizarAssentos();
        atualizarResumo();
    }

    private void renderizarAssentos() {
        gradeAssentos.removeAll();
        if (setorSelecionado == null) return;

        Map<String, List<Assento>> filas = new TreeMap<>();
        for (Assento a : setorSelecionado.getAssentos())
            filas.computeIfAbsent(a.getFila(), k -> new ArrayList<>()).add(a);

        gradeAssentos.setLayout(new GridLayout(filas.size(), 1, 0, 8));

        for (Map.Entry<String, List<Assento>> entry : filas.entrySet()) {
            JPanel linha = new JPanel(new FlowLayout(FlowLayout.CENTER, 9, 0));
            linha.setOpaque(false);

            JLabel lf = criarLabel(entry.getKey(), 12, Font.BOLD, TEXT_TERTIARY);
            lf.setPreferredSize(new Dimension(18, 20));
            linha.add(lf);

            for (Assento a : entry.getValue()) {
                JButton dot = new JButton() {
                    @Override protected void paintComponent(Graphics g) {
                        Graphics2D g2 = (Graphics2D) g.create();
                        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                        boolean sel = assentoSel != null && assentoSel.getIdAssento() == a.getIdAssento();
                        int d = Math.min(getWidth(), getHeight()) - 4;
                        int x = (getWidth()-d)/2, y = (getHeight()-d)/2;
                        if (sel) {
                            g2.setColor(new Color(SEAT_SELECTED.getRed(), SEAT_SELECTED.getGreen(), SEAT_SELECTED.getBlue(), 50));
                            g2.fillOval(x-3, y-3, d+6, d+6);
                            g2.setColor(SEAT_SELECTED);
                            g2.fillOval(x, y, d, d);
                        } else if ("DISPONIVEL".equalsIgnoreCase(a.getStatus())) {
                            g2.setColor(SEAT_FREE);
                            g2.fillOval(x, y, d, d);
                        } else {
                            g2.setColor(SEAT_TAKEN);
                            g2.fillOval(x, y, d, d);
                        }
                        g2.dispose();
                    }
                };
                dot.setPreferredSize(new Dimension(22, 22));
                dot.setContentAreaFilled(false);
                dot.setBorderPainted(false);
                dot.setFocusPainted(false);
                if ("DISPONIVEL".equalsIgnoreCase(a.getStatus())) {
                    dot.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
                    dot.setToolTipText(a.getFila()+a.getNumero()+" \u2022 Dispon\u00edvel");
                    dot.addActionListener(e -> { assentoSel = a; renderizarAssentos(); atualizarResumo(); });
                } else {
                    dot.setToolTipText(a.getFila()+a.getNumero()+" \u2022 Ocupado");
                }
                linha.add(dot);
            }
            gradeAssentos.add(linha);
        }
        gradeAssentos.revalidate();
        gradeAssentos.repaint();
    }

    private void atualizarResumo() {
        if (assentoSel == null) {
            lblResumoTitulo.setText("Nenhum assento selecionado");
            lblResumoSub.setText("Clique em um assento azul para escolher");
            lblResumoPreco.setText("R$ 0,00");
            btnProsseguir.setEnabled(false);
        } else {
            lblResumoTitulo.setText("1 Ingresso Selecionado");
            lblResumoSub.setText(setorSelecionado.getNomeSetor() + "  \u2022  " + assentoSel.getFila()+"-"+assentoSel.getNumero());
            lblResumoPreco.setText("R$ " + String.format("%.2f", setorSelecionado.getPrecoBase()));
            btnProsseguir.setEnabled(true);
        }
    }

    private void confirmarAssentoECarrinho() {
        if (assentoSel == null) return;
        if (torcedorLogado == null) {
            if (JOptionPane.showConfirmDialog(this, "Fa\u00e7a login para continuar.\nDeseja entrar agora?", "Login Necess\u00e1rio", JOptionPane.YES_NO_OPTION) == JOptionPane.YES_OPTION)
                abrirDialogLogin();
            return;
        }

        String[] opcoes = {"Inteira (R$ "+String.format("%.2f", setorSelecionado.getPrecoBase())+")", "Meia-Entrada 50% (R$ "+String.format("%.2f", setorSelecionado.getPrecoBase()/2.0)+")"};
        int escolha = JOptionPane.showOptionDialog(this, "Categoria do assento "+assentoSel.getFila()+assentoSel.getNumero()+":", "Tipo de Ingresso (RF04)", JOptionPane.DEFAULT_OPTION, JOptionPane.PLAIN_MESSAGE, null, opcoes, opcoes[0]);

        int tipo = 1; double valor = setorSelecionado.getPrecoBase();
        String nomeBen = torcedorLogado.getNome(), docBen = torcedorLogado.getCpf();

        if (escolha == 1) {
            tipo = 2; valor /= 2.0;
            JPanel pd = new JPanel(new GridLayout(4,1,4,4));
            JTextField tn = criarCampo(torcedorLogado.getNome());
            JTextField td = criarCampo("");
            pd.setOpaque(false);
            pd.add(criarLabel("Nome do benefici\u00e1rio:", 13, Font.PLAIN, TEXT_SECONDARY));
            pd.add(tn);
            pd.add(criarLabel("Documento de comprova\u00e7\u00e3o:", 13, Font.PLAIN, TEXT_SECONDARY));
            pd.add(td);
            if (JOptionPane.showConfirmDialog(this, pd, "Elegibilidade (RNF 4.1)", JOptionPane.OK_CANCEL_OPTION) != JOptionPane.OK_OPTION || td.getText().trim().isEmpty()) {
                mostrarAviso("Documento obrigat\u00f3rio para meia-entrada."); return;
            }
            nomeBen = tn.getText().trim(); docBen = td.getText().trim();
        }

        if (!controladorAssentos.selecionarAssento(assentoSel)) {
            mostrarAviso("Este assento j\u00e1 foi reservado (RNF 3.1)."); renderizarAssentos(); return;
        }

        if (carrinhoAtual == null) carrinhoAtual = gerenciadorPedidos.criarPedido(torcedorLogado);
        carrinhoAtual.adicionarIngresso(new Ingresso(nextIngressoId++, tipo, valor, assentoSel, nomeBen, docBen));
        atualizarTopBar(); renderizarAssentos();

        if (JOptionPane.showConfirmDialog(this, "Assento adicionado ao carrinho!\nIr para o Checkout?", "Sucesso", JOptionPane.YES_NO_OPTION) == JOptionPane.YES_OPTION) {
            navegarPara("CARRINHO");
        }
        assentoSel = null; atualizarResumo();
    }

    // ================================================================
    //  3. CARRINHO & CHECKOUT (RF05)
    // ================================================================
    private DefaultTableModel modeloCarrinho;
    private JLabel lblTotal;

    private JPanel criarPainelCarrinho() {
        JPanel p = new JPanel(new BorderLayout(0, 18));
        p.setBackground(BG_BASE);
        p.setBorder(new EmptyBorder(28, 32, 24, 32));

        p.add(criarLabel("Carrinho de Compras", 24, Font.BOLD, TEXT_PRIMARY), BorderLayout.NORTH);

        String[] cols = {"#", "Assento", "Categoria", "Benefici\u00e1rio", "Documento", "Valor"};
        modeloCarrinho = new DefaultTableModel(cols, 0) {
            public boolean isCellEditable(int r, int c) { return false; }
        };
        JTable t = estilizarTabela(new JTable(modeloCarrinho));
        PainelArredondado wrap = new PainelArredondado(14, BG_RAISED, BORDER_SUBTLE);
        wrap.setLayout(new BorderLayout());
        JScrollPane sc = new JScrollPane(t);
        sc.getViewport().setBackground(BG_RAISED);
        sc.setBorder(null);
        wrap.add(sc);
        p.add(wrap, BorderLayout.CENTER);

        // Rodapé
        PainelArredondado rodape = new PainelArredondado(14, BG_SURFACE, BORDER_SUBTLE);
        rodape.setLayout(new BorderLayout(15, 0));
        rodape.setBorder(new EmptyBorder(16, 24, 16, 24));

        lblTotal = criarLabel("Total: R$ 0,00", 20, Font.BOLD, TEXT_PRIMARY);
        rodape.add(lblTotal, BorderLayout.WEST);

        JPanel bts = new JPanel(new FlowLayout(FlowLayout.RIGHT, 14, 0));
        bts.setOpaque(false);
        JButton btnCanc = criarBotao("Cancelar e Liberar Assentos", BG_RAISED, ACCENT_RED);
        btnCanc.addActionListener(e -> cancelarCarrinho());
        JButton btnPag = new JButton("Pagar com Cartão") {
            @Override protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(ACCENT_GREEN);
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), 12, 12);
                g2.setFont(new Font("Segoe UI Emoji", Font.PLAIN, 15));
                g2.setColor(BG_DEEP);
                g2.drawString("💳", 20, getHeight() / 2 + 5);
                g2.setFont(new Font("Segoe UI", Font.BOLD, 14));
                g2.drawString("Pagar com Cartão", 46, getHeight() / 2 + 5);
                g2.dispose();
            }
        };
        btnPag.setContentAreaFilled(false);
        btnPag.setBorderPainted(false);
        btnPag.setFocusPainted(false);
        btnPag.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        btnPag.setPreferredSize(new Dimension(230, 44));
        btnPag.addActionListener(e -> abrirDialogPagamento());
        bts.add(btnCanc); bts.add(btnPag);
        rodape.add(bts, BorderLayout.EAST);

        p.add(rodape, BorderLayout.SOUTH);
        return p;
    }

    private void atualizarPainelCarrinho() {
        modeloCarrinho.setRowCount(0);
        if (carrinhoAtual == null || carrinhoAtual.getIngressos().isEmpty()) {
            lblTotal.setText("Carrinho vazio"); return;
        }
        for (Ingresso ing : carrinhoAtual.getIngressos()) {
            String cat = ing.getTipoCategoria() == 2 ? "Meia-Entrada" : "Inteira";
            modeloCarrinho.addRow(new Object[]{"#"+ing.getIdIngresso(), ing.getAssento().getFila()+ing.getAssento().getNumero(), cat, ing.getNomeBeneficiario(), ing.getDocumentoBeneficiario(), "R$ "+String.format("%.2f", ing.getValorPago())});
        }
        lblTotal.setText("Total: R$ " + String.format("%.2f", carrinhoAtual.calcularTotal()));
    }

    private void cancelarCarrinho() {
        if (carrinhoAtual == null || carrinhoAtual.getIngressos().isEmpty()) return;
        if (JOptionPane.showConfirmDialog(this, "Cancelar pedido e liberar assentos?", "Confirmar", JOptionPane.YES_NO_OPTION) == JOptionPane.YES_OPTION) {
            for (Ingresso ing : carrinhoAtual.getIngressos()) controladorAssentos.desmarcarAssento(ing.getAssento());
            carrinhoAtual.limparCarrinho(); carrinhoAtual = null;
            atualizarTopBar(); atualizarPainelCarrinho();
        }
    }

    private void abrirDialogPagamento() {
        if (carrinhoAtual == null || carrinhoAtual.getIngressos().isEmpty()) { mostrarAviso("Carrinho vazio."); return; }

        JDialog d = criarDialog("Checkout Seguro", 460, 380);
        d.setLayout(new BorderLayout());

        JPanel form = new JPanel();
        form.setLayout(new BoxLayout(form, BoxLayout.Y_AXIS));
        form.setOpaque(false);
        form.setBorder(new EmptyBorder(24, 28, 16, 28));

        JLabel lv = criarLabel("R$ " + String.format("%.2f", carrinhoAtual.calcularTotal()), 26, Font.BOLD, ACCENT_GREEN);
        lv.setAlignmentX(Component.LEFT_ALIGNMENT);
        form.add(lv); form.add(Box.createVerticalStrut(18));

        JComboBox<String> combo = new JComboBox<>(new String[]{"Cart\u00e3o de Cr\u00e9dito", "Cart\u00e3o de D\u00e9bito"});
        combo.setMaximumSize(new Dimension(Integer.MAX_VALUE, 38));
        combo.setAlignmentX(Component.LEFT_ALIGNMENT);

        JTextField txtNum = criarCampo("4111 2222 3333 4444");
        txtNum.setMaximumSize(new Dimension(Integer.MAX_VALUE, 42));
        txtNum.setAlignmentX(Component.LEFT_ALIGNMENT);

        JLabel l1 = criarLabel("M\u00e9todo de pagamento", 12, Font.PLAIN, TEXT_TERTIARY);
        l1.setAlignmentX(Component.LEFT_ALIGNMENT);
        JLabel l2 = criarLabel("N\u00famero do cart\u00e3o (13\u201319 d\u00edgitos)", 12, Font.PLAIN, TEXT_TERTIARY);
        l2.setAlignmentX(Component.LEFT_ALIGNMENT);

        form.add(l1); form.add(Box.createVerticalStrut(6)); form.add(combo);
        form.add(Box.createVerticalStrut(14));
        form.add(l2); form.add(Box.createVerticalStrut(6)); form.add(txtNum);

        JPanel pBt = new JPanel(new FlowLayout(FlowLayout.RIGHT, 0, 12));
        pBt.setOpaque(false);
        JButton btn = criarBotao("Confirmar Pagamento", ACCENT_GREEN, BG_DEEP);
        btn.addActionListener(e -> {
            String num = txtNum.getText().trim();
            Pagamento pag = new Pagamento(carrinhoAtual.getIdPedido(), (String)combo.getSelectedItem(), carrinhoAtual.getValorTotal());
            carrinhoAtual.setPagamento(pag);
            if (!controlePagamento.validarDados(num)) { JOptionPane.showMessageDialog(d, "Pagamento Recusado! Verifique os dados.", "Erro", JOptionPane.ERROR_MESSAGE); return; }
            controlePagamento.efetuarPagamentoPedido(carrinhoAtual, (String)combo.getSelectedItem(), num);
            for (Ingresso ing : carrinhoAtual.getIngressos()) controladorAssentos.confirmarVendaAssento(ing.getAssento());
            
            // Dispara envio de e-mail com QR Code e abre cópia no navegador
            Pedido pedidoPago = carrinhoAtual;
            boolean enviadoWeb = ServicoNotificacaoEmail.enviarIngressoEmail(torcedorLogado.getEmail(), pedidoPago);
            ServicoNotificacaoEmail.abrirComprovanteNavegador(pedidoPago);

            String msgSucesso = "<html><body><h3>🎉 Pagamento Aprovado com Sucesso!</h3>"
                    + "<p>Seu ticket digital com <b>imagem de QR Code</b> foi gerado para: <b>" + torcedorLogado.getEmail() + "</b></p>"
                    + "<p style='color:#38bdf8;'>Uma cópia do e-mail com a imagem do QR Code foi aberta no seu navegador.</p>"
                    + (enviadoWeb ? "<p style='color:#4ade80;'>✔ O e-mail também foi despachado para sua caixa de entrada!</p>" : "<p style='color:#94a3b8;'><i>(Arquivo HTML completo disponível na pasta do projeto)</i></p>")
                    + "</body></html>";

            JOptionPane.showMessageDialog(d, msgSucesso, "Compra Concluída", JOptionPane.INFORMATION_MESSAGE);
            carrinhoAtual = null; atualizarTopBar(); atualizarPainelCarrinho(); d.dispose();
            navegarPara("MEUS_INGRESSOS");
        });
        pBt.add(btn);

        d.add(form, BorderLayout.CENTER);
        d.add(pBt, BorderLayout.SOUTH);
        d.setVisible(true);
    }

    // ================================================================
    //  4. MEUS INGRESSOS / QR CODE (RF06)
    // ================================================================
    private JPanel painelCards;

    private JPanel criarPainelMeusIngressos() {
        JPanel p = new JPanel(new BorderLayout(0, 18));
        p.setBackground(BG_BASE);
        p.setBorder(new EmptyBorder(28, 32, 24, 32));
        p.add(criarLabel("Meus Ingressos Digitais", 24, Font.BOLD, TEXT_PRIMARY), BorderLayout.NORTH);

        painelCards = new JPanel();
        painelCards.setLayout(new BoxLayout(painelCards, BoxLayout.Y_AXIS));
        painelCards.setOpaque(false);
        JScrollPane sc = new JScrollPane(painelCards);
        sc.getViewport().setBackground(BG_BASE);
        sc.setBorder(null);
        p.add(sc, BorderLayout.CENTER);
        return p;
    }

    private void atualizarPainelMeusIngressos() {
        painelCards.removeAll();
        if (torcedorLogado == null) {
            painelCards.add(criarLabel("  Fa\u00e7a login para ver seus ingressos.", 15, Font.PLAIN, TEXT_TERTIARY));
        } else {
            List<Pedido> pedidos = gerenciadorPedidos.listarPedidosPagosPorTorcedor(torcedorLogado);
            if (pedidos.isEmpty()) {
                painelCards.add(criarLabel("  Nenhum ingresso comprado ainda.", 15, Font.PLAIN, TEXT_TERTIARY));
            } else {
                for (Pedido ped : pedidos)
                    for (Ingresso ing : ped.getIngressos()) {
                        painelCards.add(criarCardIngresso(ing, ped));
                        painelCards.add(Box.createVerticalStrut(14));
                    }
            }
        }
        painelCards.revalidate(); painelCards.repaint();
    }

    private JPanel criarCardIngresso(Ingresso ing, Pedido ped) {
        PainelArredondado card = new PainelArredondado(14, BG_SURFACE, BORDER_SUBTLE);
        card.setLayout(new BorderLayout(20, 0));
        card.setBorder(new EmptyBorder(18, 22, 18, 22));
        card.setMaximumSize(new Dimension(1000, 120));

        JPanel dados = new JPanel(new GridLayout(3,1,2,2));
        dados.setOpaque(false);
        String cat = ing.getTipoCategoria()==2 ? "MEIA-ENTRADA" : "INTEIRA";
        dados.add(criarLabel("Assento "+ing.getAssento().getFila()+ing.getAssento().getNumero()+"  \u2022  "+cat+"  \u2022  R$ "+String.format("%.2f", ing.getValorPago()), 15, Font.BOLD, TEXT_PRIMARY));
        String sub = ing.getTipoCategoria()==2 ? "Benefici\u00e1rio: "+ing.getNomeBeneficiario()+" ("+ing.getDocumentoBeneficiario()+")" : "Titular: "+ped.getTorcedor().getNome();
        dados.add(criarLabel(sub, 13, Font.PLAIN, TEXT_SECONDARY));
        dados.add(criarLabel("\u2705 Aprovado  \u2022  Enviado para "+ped.getTorcedor().getEmail(), 12, Font.PLAIN, ACCENT_GREEN));

        // Bloco QR
        PainelArredondado qr = new PainelArredondado(10, BG_RAISED, BORDER_SUBTLE);
        qr.setLayout(new BorderLayout(4, 4));
        qr.setBorder(new EmptyBorder(10, 16, 10, 16));
        qr.setPreferredSize(new Dimension(260, 85));

        // Mini grid QR visual
        JPanel miniQr = new JPanel() {
            @Override protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                Random rng = new Random(ing.getQrCode().hashCode());
                int sz = 4, gap = 1;
                int cols = getWidth()/(sz+gap), rows = getHeight()/(sz+gap);
                for (int r = 0; r < rows; r++)
                    for (int c = 0; c < cols; c++) {
                        g2.setColor(rng.nextBoolean() ? ACCENT_CYAN : BG_SURFACE);
                        g2.fillRect(c*(sz+gap), r*(sz+gap), sz, sz);
                    }
                g2.dispose();
            }
        };
        miniQr.setOpaque(false);
        miniQr.setPreferredSize(new Dimension(55, 55));

        JPanel qrTextos = new JPanel(new GridLayout(2,1));
        qrTextos.setOpaque(false);
        qrTextos.add(criarLabel("QR Code V\u00e1lido", 11, Font.BOLD, ACCENT_CYAN));
        JLabel code = criarLabel(ing.getQrCode(), 10, Font.BOLD, TEXT_SECONDARY);
        code.setFont(new Font("Monospaced", Font.BOLD, 10));
        qrTextos.add(code);

        qr.add(miniQr, BorderLayout.WEST);
        qr.add(qrTextos, BorderLayout.CENTER);

        card.add(dados, BorderLayout.CENTER);
        card.add(qr, BorderLayout.EAST);
        return card;
    }

    // ================================================================
    //  5. PAINEL ADMINISTRATIVO (RF07)
    // ================================================================
    private DefaultTableModel modeloAdmin;
    private boolean adminOk = false;

    private JPanel criarPainelAdmin() {
        JPanel p = new JPanel(new BorderLayout(0, 18));
        p.setBackground(BG_BASE);
        p.setBorder(new EmptyBorder(28, 32, 24, 32));
        p.add(criarLabel("Painel Administrativo", 24, Font.BOLD, TEXT_PRIMARY), BorderLayout.NORTH);

        String[] cols = {"ID", "Competi\u00e7\u00e3o", "Advers\u00e1rio", "Data e Hor\u00e1rio", "Setores"};
        modeloAdmin = new DefaultTableModel(cols, 0) {
            public boolean isCellEditable(int r, int c) { return false; }
        };
        JTable t = estilizarTabela(new JTable(modeloAdmin));
        PainelArredondado wrap = new PainelArredondado(14, BG_RAISED, BORDER_SUBTLE);
        wrap.setLayout(new BorderLayout());
        JScrollPane sc = new JScrollPane(t);
        sc.getViewport().setBackground(BG_RAISED);
        sc.setBorder(null);
        wrap.add(sc);
        p.add(wrap, BorderLayout.CENTER);

        JPanel bts = new JPanel(new FlowLayout(FlowLayout.RIGHT, 14, 8));
        bts.setOpaque(false);
        JButton btnRem = criarBotao("Remover Partida", BG_RAISED, ACCENT_RED);
        btnRem.addActionListener(e -> {
            if (!checarAdmin()) return;
            int r = t.getSelectedRow();
            if (r == -1) { mostrarAviso("Selecione uma partida."); return; }
            gerenciadorPartidas.removerPartida((int) modeloAdmin.getValueAt(r, 0));
            atualizarAdmin(); atualizarTabelaJogos();
        });
        JButton btnAdd = criarBotao("+ Nova Partida", ACCENT_GREEN, BG_DEEP);
        btnAdd.addActionListener(e -> { if (checarAdmin()) dialogNovaPartida(); });
        bts.add(btnRem); bts.add(btnAdd);
        p.add(bts, BorderLayout.SOUTH);

        atualizarAdmin();
        return p;
    }

    private boolean checarAdmin() {
        if (adminOk) return true;
        JPasswordField pw = criarCampoSenha("");
        if (JOptionPane.showConfirmDialog(this, pw, "Senha de administrador:", JOptionPane.OK_CANCEL_OPTION) == JOptionPane.OK_OPTION
            && gerenciadorPartidas.autenticarAdmin(new String(pw.getPassword()))) { adminOk = true; return true; }
        JOptionPane.showMessageDialog(this, "Senha incorreta.", "Erro", JOptionPane.ERROR_MESSAGE);
        return false;
    }

    private void dialogNovaPartida() {
        JDialog d = criarDialog("Cadastrar Nova Partida", 480, 480);
        d.setLayout(new BorderLayout());
        JPanel f = new JPanel();
        f.setLayout(new BoxLayout(f, BoxLayout.Y_AXIS));
        f.setOpaque(false);
        f.setBorder(new EmptyBorder(20, 26, 12, 26));

        JTextField tm = criarCampo("Corinthians"); tm.setMaximumSize(new Dimension(Integer.MAX_VALUE, 40)); tm.setAlignmentX(0);
        JTextField tv = criarCampo("Flamengo");    tv.setMaximumSize(new Dimension(Integer.MAX_VALUE, 40)); tv.setAlignmentX(0);
        JTextField tc = criarCampo("Campeonato Brasileiro"); tc.setMaximumSize(new Dimension(Integer.MAX_VALUE, 40)); tc.setAlignmentX(0);
        JTextField te = criarCampo("Neo Química Arena");     te.setMaximumSize(new Dimension(Integer.MAX_VALUE, 40)); te.setAlignmentX(0);
        JTextField td = criarCampo("15/12/2026 16:00");      td.setMaximumSize(new Dimension(Integer.MAX_VALUE, 40)); td.setAlignmentX(0);

        JLabel lMandante = criarLabel("Time Mandante (Casa)", 12, Font.BOLD, TEXT_SECONDARY); lMandante.setAlignmentX(0);
        JLabel lVisitante = criarLabel("Time Visitante (Adversário)", 12, Font.BOLD, TEXT_SECONDARY); lVisitante.setAlignmentX(0);
        JLabel lCampeonato = criarLabel("Campeonato / Competição", 12, Font.BOLD, TEXT_SECONDARY); lCampeonato.setAlignmentX(0);
        JLabel lEstadio = criarLabel("Estádio / Arena", 12, Font.BOLD, TEXT_SECONDARY); lEstadio.setAlignmentX(0);
        JLabel lData = criarLabel("Data e Horário (dd/MM/yyyy HH:mm)", 12, Font.BOLD, TEXT_SECONDARY); lData.setAlignmentX(0);

        f.add(lMandante); f.add(Box.createVerticalStrut(4)); f.add(tm);
        f.add(Box.createVerticalStrut(10));
        f.add(lVisitante); f.add(Box.createVerticalStrut(4)); f.add(tv);
        f.add(Box.createVerticalStrut(10));
        f.add(lCampeonato); f.add(Box.createVerticalStrut(4)); f.add(tc);
        f.add(Box.createVerticalStrut(10));
        f.add(lEstadio); f.add(Box.createVerticalStrut(4)); f.add(te);
        f.add(Box.createVerticalStrut(10));
        f.add(lData); f.add(Box.createVerticalStrut(4)); f.add(td);

        JPanel pb = new JPanel(new FlowLayout(FlowLayout.RIGHT, 0, 12));
        pb.setOpaque(false);
        pb.setBorder(new EmptyBorder(0, 26, 16, 26));

        JButton bs = criarBotao("Salvar Partida (Assentos 100% Livres)", ACCENT_GREEN, BG_DEEP);
        bs.addActionListener(e -> {
            try {
                LocalDateTime dt = LocalDateTime.parse(td.getText().trim(), DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm"));
                gerenciadorPartidas.criarNovaPartida(dt, tm.getText().trim(), tv.getText().trim(), tc.getText().trim(), te.getText().trim());
                atualizarAdmin();
                atualizarTabelaJogos();
                d.dispose();
                JOptionPane.showMessageDialog(this, "Partida cadastrada com sucesso!\nTodos os assentos foram inicializados como LIVRES.", "Sucesso", JOptionPane.INFORMATION_MESSAGE);
            } catch (Exception ex) {
                mostrarAviso("Formato de data inválido! Use o formato: dd/MM/yyyy HH:mm");
            }
        });
        pb.add(bs);
        d.add(f, BorderLayout.CENTER);
        d.add(pb, BorderLayout.SOUTH);
        d.setVisible(true);
    }

    private void atualizarAdmin() {
        modeloAdmin.setRowCount(0);
        DateTimeFormatter dtf = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");
        for (Partida p : gerenciadorPartidas.listarTodasPartidas())
            modeloAdmin.addRow(new Object[]{p.getIdPartida(), p.getCampeonato(), p.getNomeConfronto(), p.getDataHora().format(dtf), p.getSetores().size()+" setores"});
    }

    // ================================================================
    //  6. LOGIN & CADASTRO (RF01)
    // ================================================================
    private void abrirDialogLogin() {
        JDialog d = criarDialog("Acesso do Torcedor", 440, 440);
        d.setLayout(new BorderLayout());

        JPanel f = new JPanel();
        f.setLayout(new BoxLayout(f, BoxLayout.Y_AXIS));
        f.setOpaque(false);
        f.setBorder(new EmptyBorder(22, 28, 10, 28));

        JLabel t = criarLabel("Bem-vindo de volta", 20, Font.BOLD, TEXT_PRIMARY);
        t.setAlignmentX(0);
        JLabel s = criarLabel("Entre com suas credenciais para continuar", 13, Font.PLAIN, TEXT_TERTIARY);
        s.setAlignmentX(0);

        JTextField te = criarCampo("torcedor@teste.com");
        te.setMaximumSize(new Dimension(Integer.MAX_VALUE, 40));
        te.setPreferredSize(new Dimension(380, 40));
        te.setAlignmentX(0);

        JPasswordField ts = criarCampoSenha("123456");
        ts.setMaximumSize(new Dimension(Integer.MAX_VALUE, 40));
        ts.setPreferredSize(new Dimension(380, 40));
        ts.setAlignmentX(0);

        JLabel le = criarLabel("E-mail de acesso", 12, Font.BOLD, TEXT_SECONDARY); le.setAlignmentX(0);
        JLabel ls = criarLabel("Senha", 12, Font.BOLD, TEXT_SECONDARY); ls.setAlignmentX(0);

        f.add(t); f.add(Box.createVerticalStrut(4)); f.add(s);
        f.add(Box.createVerticalStrut(18));
        f.add(le); f.add(Box.createVerticalStrut(6)); f.add(te);
        f.add(Box.createVerticalStrut(14));
        f.add(ls); f.add(Box.createVerticalStrut(6)); f.add(ts);

        JPanel pb = new JPanel(new GridLayout(2,1,0,8));
        pb.setOpaque(false);
        pb.setBorder(new EmptyBorder(12, 28, 24, 28));

        JButton be = criarBotao("Entrar na Conta", ACCENT_GREEN, BG_DEEP);
        be.setPreferredSize(new Dimension(380, 42));
        be.addActionListener(e -> {
            Torcedor tor = gerenciadorUsuario.autenticar(te.getText().trim(), new String(ts.getPassword()));
            if (tor != null) { torcedorLogado = tor; atualizarTopBar(); d.dispose(); }
            else JOptionPane.showMessageDialog(d, "E-mail ou senha inválidos.", "Erro de Autenticação", JOptionPane.ERROR_MESSAGE);
        });

        JButton bc = criarBotao("Não tenho conta (Cadastrar)", BG_RAISED, TEXT_SECONDARY);
        bc.setPreferredSize(new Dimension(380, 40));
        bc.addActionListener(e -> { d.dispose(); abrirDialogCadastro(); });

        pb.add(be); pb.add(bc);
        d.add(f, BorderLayout.CENTER); d.add(pb, BorderLayout.SOUTH);
        d.setVisible(true);
    }

    private void abrirDialogCadastro() {
        JDialog d = criarDialog("Cadastro de Torcedor", 440, 430);
        d.setLayout(new BorderLayout());

        JPanel f = new JPanel();
        f.setLayout(new BoxLayout(f, BoxLayout.Y_AXIS));
        f.setOpaque(false);
        f.setBorder(new EmptyBorder(24, 28, 10, 28));

        JTextField tc = criarCampo(""); tc.setMaximumSize(new Dimension(Integer.MAX_VALUE, 42)); tc.setAlignmentX(0);
        JTextField tn = criarCampo(""); tn.setMaximumSize(new Dimension(Integer.MAX_VALUE, 42)); tn.setAlignmentX(0);
        JTextField tm = criarCampo(""); tm.setMaximumSize(new Dimension(Integer.MAX_VALUE, 42)); tm.setAlignmentX(0);
        JPasswordField tp = criarCampoSenha(""); tp.setMaximumSize(new Dimension(Integer.MAX_VALUE, 42)); tp.setAlignmentX(0);

        JLabel l1=criarLabel("CPF",12,Font.PLAIN,TEXT_TERTIARY); l1.setAlignmentX(0);
        JLabel l2=criarLabel("Nome completo",12,Font.PLAIN,TEXT_TERTIARY); l2.setAlignmentX(0);
        JLabel l3=criarLabel("E-mail",12,Font.PLAIN,TEXT_TERTIARY); l3.setAlignmentX(0);
        JLabel l4=criarLabel("Senha",12,Font.PLAIN,TEXT_TERTIARY); l4.setAlignmentX(0);

        f.add(l1); f.add(Box.createVerticalStrut(4)); f.add(tc);
        f.add(Box.createVerticalStrut(10)); f.add(l2); f.add(Box.createVerticalStrut(4)); f.add(tn);
        f.add(Box.createVerticalStrut(10)); f.add(l3); f.add(Box.createVerticalStrut(4)); f.add(tm);
        f.add(Box.createVerticalStrut(10)); f.add(l4); f.add(Box.createVerticalStrut(4)); f.add(tp);

        JPanel pb = new JPanel(new FlowLayout(FlowLayout.RIGHT, 0, 10));
        pb.setOpaque(false); pb.setBorder(new EmptyBorder(0, 28, 16, 28));
        JButton bs = criarBotao("Concluir Cadastro", ACCENT_GREEN, BG_DEEP);
        bs.addActionListener(e -> {
            if (controladorCadastro.verificarExistenciaDocumento(tc.getText().trim())) { mostrarAviso("CPF j\u00e1 cadastrado."); return; }
            torcedorLogado = controladorCadastro.efetivarCriacaoConta(tn.getText().trim(), tc.getText().trim(), tm.getText().trim(), new String(tp.getPassword()));
            atualizarTopBar(); d.dispose();
        });
        pb.add(bs);
        d.add(f, BorderLayout.CENTER); d.add(pb, BorderLayout.SOUTH);
        d.setVisible(true);
    }

    private void realizarLogout() {
        if (carrinhoAtual != null) cancelarCarrinho();
        torcedorLogado = null; atualizarTopBar(); atualizarPainelMeusIngressos();
    }

    private void atualizarTopBar() {
        if (torcedorLogado != null) {
            lblStatusUsuario.setText("Ol\u00e1, " + torcedorLogado.getNome());
            btnLoginMenu.setVisible(false); btnLogoutMenu.setVisible(true);
        } else {
            lblStatusUsuario.setText("Visitante");
            btnLoginMenu.setVisible(true); btnLogoutMenu.setVisible(false);
        }
        int q = carrinhoAtual != null ? carrinhoAtual.getIngressos().size() : 0;
        lblStatusCarrinho.setText(q + " ite" + (q == 1 ? "m" : "ns"));
    }

    // ================================================================
    //  UTILITÁRIOS DE UI
    // ================================================================
    private JTable estilizarTabela(JTable t) {
        t.setRowHeight(46);
        t.setBackground(BG_RAISED);
        t.setForeground(TEXT_PRIMARY);
        t.setGridColor(new Color(35, 44, 65));
        t.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        t.setSelectionBackground(new Color(35, 55, 90));
        t.setSelectionForeground(ACCENT_CYAN);
        t.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        t.setShowHorizontalLines(true);
        t.setShowVerticalLines(false);
        t.setIntercellSpacing(new Dimension(0, 1));

        JTableHeader h = t.getTableHeader();
        h.setBackground(BG_SURFACE);
        h.setForeground(TEXT_TERTIARY);
        h.setFont(new Font("Segoe UI", Font.BOLD, 12));
        h.setPreferredSize(new Dimension(0, 42));
        h.setBorder(BorderFactory.createMatteBorder(0, 0, 1, 0, BORDER_SUBTLE));
        h.setReorderingAllowed(false);

        DefaultTableCellRenderer renderer = new DefaultTableCellRenderer() {
            @Override public Component getTableCellRendererComponent(JTable table, Object value, boolean isSel, boolean hasFocus, int row, int col) {
                super.getTableCellRendererComponent(table, value, isSel, hasFocus, row, col);
                setBorder(new EmptyBorder(0, 14, 0, 14));
                setBackground(isSel ? new Color(35, 55, 90) : (row%2==0 ? BG_RAISED : new Color(25, 32, 50)));
                setForeground(isSel ? ACCENT_CYAN : TEXT_PRIMARY);
                return this;
            }
        };
        for (int i = 0; i < t.getColumnCount(); i++) t.getColumnModel().getColumn(i).setCellRenderer(renderer);

        return t;
    }

    private JDialog criarDialog(String titulo, int w, int h) {
        JDialog d = new JDialog(this, titulo, true);
        d.setSize(w, h);
        d.setLocationRelativeTo(this);
        d.getContentPane().setBackground(BG_SURFACE);
        d.setUndecorated(false);
        return d;
    }

    private void mostrarAviso(String msg) {
        JOptionPane.showMessageDialog(this, msg, "Aviso", JOptionPane.INFORMATION_MESSAGE);
    }
}
