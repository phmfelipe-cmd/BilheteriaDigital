package com.bilheteria.service;

import java.io.*;
import java.net.Socket;
import java.nio.charset.StandardCharsets;
import java.util.Base64;
import javax.net.ssl.SSLSocket;
import javax.net.ssl.SSLSocketFactory;

public class SmtpClient {
    private String host;
    private int port;
    private String username;
    private String password;
    private boolean useTls;

    public SmtpClient(String host, int port, String username, String password, boolean useTls) {
        this.host = host;
        this.port = port;
        this.username = username;
        this.password = password;
        this.useTls = useTls;
    }

    public boolean enviarEmail(String de, String para, String assunto, String htmlCorpo) {
        try (Socket socket = new Socket(host, port);
             BufferedReader reader = new BufferedReader(new InputStreamReader(socket.getInputStream()));
             BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(socket.getOutputStream(), StandardCharsets.UTF_8))) {

            lerResposta(reader); // 220 banner

            enviarComando(writer, "EHLO " + host);
            lerResposta(reader);

            if (useTls) {
                enviarComando(writer, "STARTTLS");
                String respTls = lerResposta(reader);
                if (!respTls.startsWith("220")) {
                    return false;
                }

                SSLSocketFactory sslFactory = (SSLSocketFactory) SSLSocketFactory.getDefault();
                try (SSLSocket sslSocket = (SSLSocket) sslFactory.createSocket(socket, host, port, true);
                     BufferedReader sslReader = new BufferedReader(new InputStreamReader(sslSocket.getInputStream()));
                     BufferedWriter sslWriter = new BufferedWriter(new OutputStreamWriter(sslSocket.getOutputStream(), StandardCharsets.UTF_8))) {

                    sslSocket.startHandshake();

                    enviarComando(sslWriter, "EHLO " + host);
                    lerResposta(sslReader);

                    if (username != null && !username.isEmpty() && password != null && !password.isEmpty()) {
                        enviarComando(sslWriter, "AUTH LOGIN");
                        lerResposta(sslReader);

                        enviarComando(sslWriter, Base64.getEncoder().encodeToString(username.getBytes(StandardCharsets.UTF_8)));
                        lerResposta(sslReader);

                        enviarComando(sslWriter, Base64.getEncoder().encodeToString(password.getBytes(StandardCharsets.UTF_8)));
                        String respAuth = lerResposta(sslReader);
                        if (!respAuth.startsWith("235")) {
                            return false;
                        }
                    }

                    return transmitirMensagem(sslWriter, sslReader, de, para, assunto, htmlCorpo);
                }
            } else {
                return transmitirMensagem(writer, reader, de, para, assunto, htmlCorpo);
            }
        } catch (Exception e) {
            return false;
        }
    }

    private boolean transmitirMensagem(BufferedWriter writer, BufferedReader reader, String de, String para, String assunto, String htmlCorpo) throws IOException {
        enviarComando(writer, "MAIL FROM:<" + de + ">");
        if (!lerResposta(reader).startsWith("250")) return false;

        enviarComando(writer, "RCPT TO:<" + para + ">");
        if (!lerResposta(reader).startsWith("250")) return false;

        enviarComando(writer, "DATA");
        if (!lerResposta(reader).startsWith("354")) return false;

        writer.write("From: " + de + "\r\n");
        writer.write("To: " + para + "\r\n");
        writer.write("Subject: =?UTF-8?B?" + Base64.getEncoder().encodeToString(assunto.getBytes(StandardCharsets.UTF_8)) + "?=\r\n");
        writer.write("MIME-Version: 1.0\r\n");
        writer.write("Content-Type: text/html; charset=UTF-8\r\n");
        writer.write("\r\n");
        writer.write(htmlCorpo);
        writer.write("\r\n.\r\n");
        writer.flush();

        String respData = lerResposta(reader);
        enviarComando(writer, "QUIT");
        return respData.startsWith("250");
    }

    private void enviarComando(BufferedWriter writer, String cmd) throws IOException {
        writer.write(cmd + "\r\n");
        writer.flush();
    }

    private String lerResposta(BufferedReader reader) throws IOException {
        String linha = reader.readLine();
        String ultima = linha;
        while (linha != null && linha.length() >= 4 && linha.charAt(3) == '-') {
            linha = reader.readLine();
            if (linha != null) ultima = linha;
        }
        return ultima != null ? ultima : "";
    }
}
