import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.*;
import java.net.*;

public class Cliente {
    private JFrame frame;
    private JLabel lblStatus;
    private JLabel lblInfo;
    private JButton[] botoes;

    private Socket socket;
    private BufferedReader entrada;
    private PrintWriter saida;

    private String simbolo = "?";
    private boolean meuTurno = false;

    public Cliente(String host, int porta) {
        configurarInterface();
        conectarServidor(host, porta);
    }

    private void configurarInterface() {
        frame = new JFrame("Jogo da Velha - TCP");
        frame.setSize(400, 450);
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setResizable(false);
        frame.setLayout(new BorderLayout());

        lblStatus = new JLabel("Conectando...", SwingConstants.CENTER);
        lblStatus.setFont(new Font("Arial", Font.BOLD, 16));
        lblStatus.setBorder(BorderFactory.createEmptyBorder(20, 0, 20, 0));
        frame.add(lblStatus, BorderLayout.NORTH);

        JPanel panelTabuleiro = new JPanel(new GridLayout(3, 3, 5, 5));
        panelTabuleiro.setBorder(BorderFactory.createEmptyBorder(10, 40, 10, 40));
        botoes = new JButton[9];

        for (int i = 0; i < 9; i++) {
            final int index = i;
            botoes[i] = new JButton("");
            botoes[i].setFont(new Font("Arial", Font.BOLD, 40));
            botoes[i].setFocusPainted(false);
            botoes[i].setBackground(Color.WHITE);

            botoes[i].addActionListener(new ActionListener() {
                @Override
                public void actionPerformed(ActionEvent e) {
                    enviarJogada(index);
                }
            });
            panelTabuleiro.add(botoes[i]);
        }
        frame.add(panelTabuleiro, BorderLayout.CENTER);

        lblInfo = new JLabel("Aguardando oponente...", SwingConstants.CENTER);
        lblInfo.setForeground(Color.GRAY);
        lblInfo.setBorder(BorderFactory.createEmptyBorder(10, 0, 20, 0));
        frame.add(lblInfo, BorderLayout.SOUTH);

        frame.setLocationRelativeTo(null);
        frame.setVisible(true);
    }

    private void conectarServidor(String host, int porta) {
        try {
            socket = new Socket(host, porta);
            entrada = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            saida = new PrintWriter(socket.getOutputStream(), true);

            Thread threadEscuta = new Thread(new EscutadorServidor());
            threadEscuta.setDaemon(true);
            threadEscuta.start();

        } catch (IOException e) {
            JOptionPane.showMessageDialog(frame, "Erro ao conectar no servidor: " + host + ":" + porta +
                            "\nVerifique se o IP está correto e o Servidor está rodando.",
                    "Erro de Conexão", JOptionPane.ERROR_MESSAGE);
            System.exit(0);
        }
    }

    private void enviarJogada(int idx) {
        if (meuTurno && botoes[idx].getText().isEmpty()) {
            saida.println(idx);
            meuTurno = false;
            atualizarStatus("Enviando jogada...", Color.ORANGE);
            ativarBotoes(false);
        }
    }

    private class EscutadorServidor implements Runnable {
        @Override
        public void run() {
            try {
                String mensagem;
                while ((mensagem = entrada.readLine()) != null) {
                    final String msgFinal = mensagem;
                    SwingUtilities.invokeLater(() -> processarComando(msgFinal));
                }
            } catch (IOException e) {
                System.out.println("Conexão encerrada.");
            }
        }
    }

    private void processarComando(String msg) {
        try {
            String[] partes = msg.split("\\|");
            String cmd = partes[0];
            String payload = partes.length > 1 ? partes[1] : "";

            switch (cmd) {
                case "INICIO":
                    simbolo = payload;
                    frame.setTitle("Jogador " + simbolo);
                    lblInfo.setText("Você está jogando com o: " + simbolo);
                    break;
                case "TABULEIRO":
                    for (int i = 0; i < 9; i++) {
                        char c = payload.charAt(i);
                        botoes[i].setText(c == ' ' ? "" : String.valueOf(c));
                        if (c == 'X') botoes[i].setForeground(Color.BLUE);
                        else if (c == 'O') botoes[i].setForeground(Color.RED);
                    }
                    break;
                case "SUA_VEZ":
                    meuTurno = true;
                    atualizarStatus("SUA VEZ! Jogue.", new Color(0, 150, 0));
                    ativarBotoes(true);
                    break;
                case "ESPERE":
                    meuTurno = false;
                    atualizarStatus("Vez do Oponente...", Color.RED);
                    ativarBotoes(false);
                    break;
                case "FIM":
                    JOptionPane.showMessageDialog(frame, payload, "Fim de Jogo", JOptionPane.INFORMATION_MESSAGE);
                    fecharConexao();
                    break;
                case "AGUARDE":
                    atualizarStatus(payload, Color.GRAY);
                    break;
            }
        } catch (Exception e) {
            System.err.println("Erro: " + msg);
        }
    }

    private void atualizarStatus(String texto, Color cor) {
        lblStatus.setText(texto);
        lblStatus.setForeground(cor);
    }

    private void ativarBotoes(boolean estado) {
        for (JButton btn : botoes) {
            if (btn.getText().isEmpty()) {
                btn.setEnabled(estado);
            }
        }
    }

    private void fecharConexao() {
        try {
            socket.close();
            System.exit(0);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    // ========= PONTO DE ENTRADA AJUSTADO =========
    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            // Pergunta o IP do servidor antes de abrir a tela
            String ip = JOptionPane.showInputDialog(null,
                    "Digite o IP do Servidor (ex: 192.168.1.15):",
                    "127.0.0.1"); // 127.0.0.1 é o valor padrão que vem preenchido

            // Se o jogador clicar em OK e digitou um IP
            if (ip != null && !ip.trim().isEmpty()) {
                new Cliente(ip.trim(), 9999);
            } else {
                // Se cancelar ou deixar em branco, fecha o programa
                System.exit(0);
            }
        });
    }
}