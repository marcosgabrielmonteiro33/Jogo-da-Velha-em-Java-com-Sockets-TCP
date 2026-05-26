import java.io.*;
import java.net.*;
import java.util.Arrays;

public class Servidor {
    private static final int PORT = 9999;
    private static char[] board = new char[9];

    public static void main(String[] args) {
        // Inicializa o tabuleiro com espaços vazios
        Arrays.fill(board, ' ');

        // Ao usar ServerSocket sem especificar IP, ele automaticamente escuta em 0.0.0.0 (todas as interfaces de rede)
        try (ServerSocket serverSocket = new ServerSocket(PORT)) {
            System.out.println("=== SERVIDOR DE JOGO DA VELHA INICIADO ===");
            System.out.println("Aguardando conexões na porta " + PORT + "...");
            System.out.println("IP DA REDE LOCAL: " + InetAddress.getLocalHost().getHostAddress());
            System.out.println("Informe este IP para os jogadores.\n");

            // Aguarda e configura o Jogador X
            Socket socketX = serverSocket.accept();
            System.out.println("Jogador X conectado do IP: " + socketX.getInetAddress());
            BufferedReader inX = new BufferedReader(new InputStreamReader(socketX.getInputStream()));
            PrintWriter outX = new PrintWriter(socketX.getOutputStream(), true);
            outX.println("AGUARDE|Aguardando o segundo jogador conectar...");

            // Aguarda e configura o Jogador O
            Socket socketO = serverSocket.accept();
            System.out.println("Jogador O conectado do IP: " + socketO.getInetAddress());
            BufferedReader inO = new BufferedReader(new InputStreamReader(socketO.getInputStream()));
            PrintWriter outO = new PrintWriter(socketO.getOutputStream(), true);

            // Inicia a partida
            outX.println("INICIO|X");
            outO.println("INICIO|O");

            char currentTurn = 'X';

            // Loop principal do jogo
            while (true) {
                // 1. Envia o tabuleiro para ambos
                String boardStr = new String(board);
                outX.println("TABULEIRO|" + boardStr);
                outO.println("TABULEIRO|" + boardStr);

                // 2. Verifica vitória ou empate
                String result = checkWin();
                if (result != null) {
                    if (result.equals("EMPATE")) {
                        outX.println("FIM|Deu velha! Jogo empatado.");
                        outO.println("FIM|Deu velha! Jogo empatado.");
                    } else {
                        // Envia mensagem customizada dependendo de quem ganhou
                        outX.println("FIM|" + (result.equals("X") ? "Parabéns, você VENCEU!" : "Que pena, você PERDEU!"));
                        outO.println("FIM|" + (result.equals("O") ? "Parabéns, você VENCEU!" : "Que pena, você PERDEU!"));
                    }
                    break; // Fim do jogo
                }

                // 3. Avisa quem deve jogar
                if (currentTurn == 'X') {
                    outX.println("SUA_VEZ|");
                    outO.println("ESPERE|");
                } else {
                    outO.println("SUA_VEZ|");
                    outX.println("ESPERE|");
                }

                // 4. Recebe a jogada do jogador da vez
                BufferedReader currentIn = (currentTurn == 'X') ? inX : inO;
                boolean validMove = false;

                while (!validMove) {
                    String data = currentIn.readLine();
                    if (data == null) throw new IOException("Um cliente se desconectou.");

                    try {
                        int pos = Integer.parseInt(data.trim());
                        // Valida a regra de negócio (0 a 8 e espaço vazio)
                        if (pos >= 0 && pos <= 8 && board[pos] == ' ') {
                            board[pos] = currentTurn;
                            validMove = true;
                        }
                    } catch (NumberFormatException e) {
                        // Se o cliente enviar algo inválido, ignora e continua esperando
                    }
                }

                // 5. Troca o turno
                currentTurn = (currentTurn == 'X') ? 'O' : 'X';
            }

            System.out.println("\nPartida encerrada. Fechando servidor.");
            socketX.close();
            socketO.close();

        } catch (IOException e) {
            System.err.println("Erro no servidor: " + e.getMessage());
        }
    }

    // Método que implementa e valida as regras de vitória do Jogo da Velha
    private static String checkWin() {
        int[][] winConditions = {
                {0, 1, 2}, {3, 4, 5}, {6, 7, 8}, // Linhas
                {0, 3, 6}, {1, 4, 7}, {2, 5, 8}, // Colunas
                {0, 4, 8}, {2, 4, 6}             // Diagonais
        };

        for (int[] cond : winConditions) {
            if (board[cond[0]] != ' ' && board[cond[0]] == board[cond[1]] && board[cond[0]] == board[cond[2]]) {
                return String.valueOf(board[cond[0]]); // Retorna "X" ou "O"
            }
        }

        for (char c : board) {
            if (c == ' ') return null; // Jogo continua se houver espaço em branco
        }

        return "EMPATE";
    }
}