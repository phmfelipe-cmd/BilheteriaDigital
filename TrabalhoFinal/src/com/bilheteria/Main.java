package com.bilheteria;

import com.bilheteria.ui.JanelaPrincipal;
import com.bilheteria.ui.TerminalInterface;

import javax.swing.*;

public class Main {
    public static void main(String[] args) {
        // Se passado argumento "--terminal", executa via linha de comando
        if (args.length > 0 && "--terminal".equalsIgnoreCase(args[0])) {
            TerminalInterface app = new TerminalInterface();
            app.iniciar();
            return;
        }

        // Caso padrão: Abre a aplicação com Interface Gráfica moderna (Swing)
        SwingUtilities.invokeLater(() -> {
            try {
                // Aplica o tema nativo do sistema operacional (Windows Look and Feel)
                UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
            } catch (Exception ignored) {
            }

            JanelaPrincipal janela = new JanelaPrincipal();
            janela.setVisible(true);
        });
    }
}
