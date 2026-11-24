package com.bilheteria.service;

import com.bilheteria.model.Assento;
import com.bilheteria.model.Ingresso;
import com.bilheteria.model.Pedido;

import java.awt.Desktop;
import java.io.File;
import java.io.FileOutputStream;
import java.io.OutputStreamWriter;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.text.SimpleDateFormat;

public class ServicoNotificacaoEmail {

    // Configurações SMTP opcionais (podem ser configuradas pelo usuário ou mantidas padrão)
    private static String smtpHost = "smtp.gmail.com";
    private static int smtpPort = 587;
    private static String smtpUser = ""; // Preencher caso deseje usar envio direto com App Password
    private static String smtpPass = "";

    public static void configurarSmtp(String host, int port, String user, String pass) {
        smtpHost = host;
        smtpPort = port;
        smtpUser = user;
        smtpPass = pass;
    }

    public static String getSmtpHost() { return smtpHost; }
    public static int getSmtpPort() { return smtpPort; }
    public static String getSmtpUser() { return smtpUser; }
    public static String getSmtpPass() { return smtpPass; }

    public static boolean enviarIngressoEmail(String emailDestinatario, Pedido pedido) {
        String assunto = "🎟️ Seus Ingressos Confirmados - Pedido #" + pedido.getIdPedido();
        String htmlCorpo = gerarHtmlIngresso(emailDestinatario, pedido);

        // 1. Sempre gera cópia HTML local com QRCode no diretório do projeto e abre para conferência visual
        File arquivoLocal = salvarComprovanteHtmlLocal(pedido, htmlCorpo);

        // 2. Tenta envio por SMTP real se credenciais estiverem preenchidas
        boolean enviadoReal = false;
        if (smtpUser != null && !smtpUser.isEmpty() && smtpPass != null && !smtpPass.isEmpty()) {
            SmtpClient client = new SmtpClient(smtpHost, smtpPort, smtpUser, smtpPass, true);
            enviadoReal = client.enviarEmail(smtpUser, emailDestinatario, assunto, htmlCorpo);
        }

        // 3. Se não houver SMTP configurado, tenta envio via serviço HTTP
        if (!enviadoReal) {
            enviadoReal = EmailHttpSender.enviarEmailComQRCode(emailDestinatario, assunto, htmlCorpo);
        }

        // 4. Imprime no console a confirmação
        System.out.println("\n------------------------------------------------------------");
        System.out.println(">>> [SISTEMA DE E-MAIL REAL - NOTIFICAÇÃO DE INGRESSOS]");
        System.out.println("Destinatário: " + emailDestinatario);
        System.out.println("Assunto: " + assunto);
        System.out.println("Status Envio Web/SMTP: " + (enviadoReal ? "ENVIADO COM SUCESSO!" : "REGISTRADO / COPIA LOCAL PRONTA"));
        if (arquivoLocal != null) {
            System.out.println("Comprovante visual gerado em: " + arquivoLocal.getAbsolutePath());
        }
        System.out.println("------------------------------------------------------------\n");

        return enviadoReal;
    }

    public static void abrirComprovanteNavegador(Pedido pedido) {
        try {
            File file = new File("ingressos_pedido_" + pedido.getIdPedido() + ".html");
            if (file.exists() && Desktop.isDesktopSupported()) {
                Desktop.getDesktop().open(file);
            }
        } catch (Exception ignored) {
        }
    }

    private static File salvarComprovanteHtmlLocal(Pedido pedido, String html) {
        try {
            File file = new File("ingressos_pedido_" + pedido.getIdPedido() + ".html");
            try (OutputStreamWriter writer = new OutputStreamWriter(new FileOutputStream(file), StandardCharsets.UTF_8)) {
                writer.write(html);
            }
            return file;
        } catch (Exception e) {
            return null;
        }
    }

    public static String gerarHtmlIngresso(String emailDestinatario, Pedido pedido) {
        StringBuilder sb = new StringBuilder();
        String nomeTorcedor = (pedido.getTorcedor() != null) ? pedido.getTorcedor().getNome() : "Torcedor";
        SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy HH:mm");

        sb.append("<!DOCTYPE html>");
        sb.append("<html><head><meta charset='UTF-8'>");
        sb.append("<style>");
        sb.append("body { font-family: 'Segoe UI', Arial, sans-serif; background-color: #0f172a; color: #f8fafc; margin: 0; padding: 20px; }");
        sb.append(".container { max-width: 620px; margin: 0 auto; background-color: #1e293b; border-radius: 16px; border: 1px solid #334155; overflow: hidden; }");
        sb.append(".header { background: linear-gradient(135deg, #1e3a8a, #0284c7); padding: 25px; text-align: center; }");
        sb.append(".header h1 { margin: 0; font-size: 24px; color: #ffffff; }");
        sb.append(".header p { margin: 5px 0 0; color: #bae6fd; font-size: 14px; }");
        sb.append(".content { padding: 25px; }");
        sb.append(".ticket-card { background-color: #0f172a; border-radius: 12px; border: 1px solid #334155; margin-bottom: 20px; padding: 18px; display: flex; align-items: center; justify-content: space-between; }");
        sb.append(".ticket-info { flex: 1; }");
        sb.append(".ticket-info h3 { margin: 0 0 6px; color: #38bdf8; font-size: 18px; }");
        sb.append(".ticket-info p { margin: 3px 0; color: #94a3b8; font-size: 13px; }");
        sb.append(".qr-container { text-align: center; margin-left: 15px; padding: 10px; background: #ffffff; border-radius: 10px; }");
        sb.append(".qr-container img { display: block; width: 130px; height: 130px; }");
        sb.append(".qr-text { font-family: monospace; font-size: 10px; color: #334155; margin-top: 5px; font-weight: bold; }");
        sb.append(".footer { text-align: center; padding: 20px; font-size: 12px; color: #64748b; border-top: 1px solid #334155; }");
        sb.append("</style></head><body>");

        sb.append("<div class='container'>");
        sb.append("<div class='header'>");
        sb.append("<h1>⚽ BILHETERIA DIGITAL OFICIAL</h1>");
        sb.append("<p>Confirmação de Pagamento & Emissão de Ingressos</p>");
        sb.append("</div>");

        sb.append("<div class='content'>");
        sb.append("<p style='font-size: 16px;'>Olá, <b>").append(nomeTorcedor).append("</b>!</p>");
        sb.append("<p style='color: #94a3b8;'>Seu pedido <b>#").append(pedido.getIdPedido()).append("</b> foi processado com sucesso em ").append(sdf.format(pedido.getDataHora())).append(". Apresente o QR Code abaixo diretamente na catraca do estádio:</p>");

        for (Ingresso ing : pedido.getIngressos()) {
            Assento assento = ing.getAssento();
            String filaNum = (assento != null) ? assento.getFila() + assento.getNumero() : "N/A";
            String cat = (ing.getTipoCategoria() == 2) ? "MEIA-ENTRADA (50%)" : "INTEIRA";

            // URL pública de geração de imagem de QR Code real da API qrserver
            String encodedQr = URLEncoder.encode(ing.getQrCode(), StandardCharsets.UTF_8);
            String qrUrl = "https://api.qrserver.com/v1/create-qr-code/?size=150x150&data=" + encodedQr;

            sb.append("<div class='ticket-card'>");
            sb.append("<div class='ticket-info'>");
            sb.append("<h3>Assento: ").append(filaNum).append("</h3>");
            sb.append("<p><b>Categoria:</b> ").append(cat).append("</p>");
            sb.append("<p><b>Valor Pago:</b> R$ ").append(String.format("%.2f", ing.getValorPago())).append("</p>");
            if (ing.getTipoCategoria() == 2) {
                sb.append("<p><b>Beneficiário:</b> ").append(ing.getNomeBeneficiario()).append(" (Doc: ").append(ing.getDocumentoBeneficiario()).append(")</p>");
            }
            sb.append("<p style='color: #4ade80; margin-top: 8px;'>● Ingresso Ativo e Válido</p>");
            sb.append("</div>");

            sb.append("<div class='qr-container'>");
            sb.append("<img src='").append(qrUrl).append("' alt='QR Code do Ingresso' />");
            sb.append("<div class='qr-text'>").append(ing.getQrCode()).append("</div>");
            sb.append("</div>");
            sb.append("</div>");
        }

        sb.append("<p style='margin-top: 25px; color: #94a3b8; font-size: 13px;'>* Este ingresso é digital e nominal. Tenha em mãos um documento oficial com foto na entrada do estádio.</p>");
        sb.append("</div>");

        sb.append("<div class='footer'>");
        sb.append("© 2026 Bilheteria Digital Esportiva • Todos os direitos reservados.");
        sb.append("</div>");
        sb.append("</div></body></html>");

        return sb.toString();
    }
}
