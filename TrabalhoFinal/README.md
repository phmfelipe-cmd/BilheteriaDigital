# Sistema de Bilheteria para Eventos Esportivos (Bilheteria Digital)

Implementação completa em **Java puro** baseada no documento de Especificação de Requisitos de Software `Bilheteria aps.pdf`.

---

## 📌 Alinhamento com o Documento de Requisitos

| Requisito / Diagrama | Descrição | Onde está implementado |
|---|---|---|
| **RF01 / RNF 1.1 a 1.3** | Acesso e cadastro de torcedores com criptografia de dados (SHA-256) | [`Torcedor.java`](file:///c:/Users/User/Documents/Faculdade/4_Periodo/APS/TrabalhoFinal/src/com/bilheteria/model/Torcedor.java), [`ControladorCadastro.java`](file:///c:/Users/User/Documents/Faculdade/4_Periodo/APS/TrabalhoFinal/src/com/bilheteria/service/ControladorCadastro.java), [`GerenciadorUsuario.java`](file:///c:/Users/User/Documents/Faculdade/4_Periodo/APS/TrabalhoFinal/src/com/bilheteria/service/GerenciadorUsuario.java) |
| **RF02 / RNF 2.1 e 2.2** | Calendário de jogos futuros exibidos em até 2 segundos; jogos passados filtrados | [`GerenciadorPartidas.java`](file:///c:/Users/User/Documents/Faculdade/4_Periodo/APS/TrabalhoFinal/src/com/bilheteria/service/GerenciadorPartidas.java) |
| **RF03 / RNF 3.1 a 3.4** | Mapa do estádio, setores, assentos numerados com ID único e bloqueio temporário | [`Assento.java`](file:///c:/Users/User/Documents/Faculdade/4_Periodo/APS/TrabalhoFinal/src/com/bilheteria/model/Assento.java), [`Setor.java`](file:///c:/Users/User/Documents/Faculdade/4_Periodo/APS/TrabalhoFinal/src/com/bilheteria/model/Setor.java), [`ControladorAssentos.java`](file:///c:/Users/User/Documents/Faculdade/4_Periodo/APS/TrabalhoFinal/src/com/bilheteria/service/ControladorAssentos.java) |
| **RF04 / RNF 4.1 e 4.2** | Meia-entrada com 50% de desconto e solicitação de dados comprobatórios do beneficiário | [`Ingresso.java`](file:///c:/Users/User/Documents/Faculdade/4_Periodo/APS/TrabalhoFinal/src/com/bilheteria/model/Ingresso.java), [`TerminalInterface.java`](file:///c:/Users/User/Documents/Faculdade/4_Periodo/APS/TrabalhoFinal/src/com/bilheteria/ui/TerminalInterface.java) |
| **RF05 / RNF 5.1 e 5.2** | Checkout e pagamento (simulação segura externa) com liberação de assentos se cancelado/recusado | [`ControlePagamento.java`](file:///c:/Users/User/Documents/Faculdade/4_Periodo/APS/TrabalhoFinal/src/com/bilheteria/service/ControlePagamento.java), [`Pagamento.java`](file:///c:/Users/User/Documents/Faculdade/4_Periodo/APS/TrabalhoFinal/src/com/bilheteria/model/Pagamento.java), [`Pedido.java`](file:///c:/Users/User/Documents/Faculdade/4_Periodo/APS/TrabalhoFinal/src/com/bilheteria/model/Pedido.java) |
| **RF06 / RNF 6.1** | Emissão imediata de ticket digital com QR Code e envio via e-mail | [`Ingresso.java`](file:///c:/Users/User/Documents/Faculdade/4_Periodo/APS/TrabalhoFinal/src/com/bilheteria/model/Ingresso.java), [`ServicoNotificacaoEmail.java`](file:///c:/Users/User/Documents/Faculdade/4_Periodo/APS/TrabalhoFinal/src/com/bilheteria/service/ServicoNotificacaoEmail.java) |
| **RF07 / RNF 7.1** | Painel administrativo com autenticação segura por senha para gerenciamento de partidas | [`GerenciadorPartidas.java`](file:///c:/Users/User/Documents/Faculdade/4_Periodo/APS/TrabalhoFinal/src/com/bilheteria/service/GerenciadorPartidas.java), Menu Admin na interface |
| **Diagrama de Classes & Interação** | Classes `Torcedor`, `Partida`, `Setor`, `Assento`, `Pedido`, `Ingresso`, `Pagamento`, `ControladorAssentos`, `GerenciadorAssentos`, `ControladorCadastro`, `GerenciadorUsuario`, `ControlePagamento` | Totalmente modeladas conforme as páginas 10, 11 e 12 da especificação |

---

## 🚀 Como Executar

### Pré-requisitos
- JDK instalado (versão 11 ou superior recomendada; testado com JDK 26)

### Compilação e Execução pelo Terminal

No diretório do projeto:

```powershell
# Compilar todas as classes para o diretório bin
javac -d bin -sourcepath src (Get-ChildItem -Path src -Filter *.java -Recurse | Select-Object -ExpandProperty FullName)

# Executar a aplicação
java -cp bin com.bilheteria.Main
```

### Credenciais Padrão Pré-cadastradas para Teste
- **Torcedor:**
  - E-mail: `torcedor@teste.com`
  - Senha: `123456`
- **Administrador:**
  - Senha de acesso ao Painel Admin: `admin123`
