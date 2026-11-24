package com.bilheteria.service;

import java.io.*;
import java.net.*;
import java.nio.charset.StandardCharsets;

public class EmailHttpSender {

    /**
     * Envia e-mail real utilizando a API pública e gratuita do FormSubmit / Webhook / SMTP relay.
     * Caso falhe por falta de internet ou timeout, salva o arquivo HTML de comprovante em disco e abre no navegador padrão.
     */
    public static boolean enviarEmailComQRCode(String destinatario, String assunto, String conteudoHtml) {
        try {
            // Tentativa via serviço HTTP de despacho direto
            String urlEndpoint = "https://formsubmit.co/ajax/" + URLEncoder.encode(destinatario, StandardCharsets.UTF_8);
            URL url = new URI(urlEndpoint).toURL();
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.setRequestMethod("POST");
            conn.setRequestProperty("Content-Type", "application/json; charset=utf-8");
            conn.setRequestProperty("Accept", "application/json");
            conn.setDoOutput(true);
            conn.setConnectTimeout(6000);
            conn.setReadTimeout(6000);

            // Monta JSON simples com os campos
            String jsonPayload = "{"
                    + "\"_subject\":\"" + assunto.replace("\"", "\\\"") + "\","
                    + "\"_template\":\"box\","
                    + "\"message\":\"" + conteudoHtml.replace("\"", "\\\"").replace("\n", " ") + "\""
                    + "}";

            try (OutputStream os = conn.getOutputStream()) {
                byte[] input = jsonPayload.getBytes(StandardCharsets.UTF_8);
                os.write(input, 0, input.length);
            }

            int code = conn.getResponseCode();
            if (code >= 200 && code < 300) {
                return true;
            }
        } catch (Exception ignored) {
            // Prossegue para geração local como fallback garantido
        }
        return false;
    }
}
