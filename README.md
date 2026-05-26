Jogo da Velha em Rede com Sockets TCP

Projeto desenvolvido para a disciplina de Redes de Computadores com o objetivo de implementar um jogo da velha distribuído utilizando comunicação cliente-servidor através de Sockets TCP em Java.

Objetivo do Projeto

O projeto tem como finalidade aplicar na prática conceitos de:

Comunicação em redes de computadores;
Modelo Cliente-Servidor;
Sockets TCP;
Protocolos de aplicação;
Troca de mensagens em rede;
Captura e análise de pacotes utilizando Wireshark.

A aplicação permite que dois jogadores disputem uma partida de jogo da velha em máquinas distintas através da rede.

Arquitetura da Aplicação

O sistema segue obrigatoriamente o modelo Cliente-Servidor.

Servidor

O servidor é responsável por:

aceitar conexões TCP;
controlar a lógica do jogo;
validar jogadas;
controlar turnos;
verificar vitória ou empate;
atualizar o estado do tabuleiro;
enviar mensagens aos clientes.
Cliente

Os clientes são responsáveis por:

conectar ao servidor;
exibir a interface gráfica;
permitir interação do jogador;
enviar jogadas;
receber atualizações do servidor.


Tecnologias Utilizadas:
- Java
- Java Swing
- Sockets TCP
- Wireshark

Estrutura do Projeto
src/
  Cliente.java
  Servidor.java


Como Executar:

1. Compilar os arquivos

Abra o terminal na pasta src e execute:

javac Servidor.java
javac Cliente.java

2. Iniciar o servidor
java Servidor

O servidor exibirá o IP da máquina e aguardará conexões na porta 9999.

3. Iniciar os clientes

Em novos terminais execute:

java Cliente

Digite o IP do servidor quando solicitado.

Protocolo de Comunicação

O protocolo desenvolvido utiliza mensagens em texto simples codificadas em UTF-8 no formato:

<COMANDO>|<PAYLOAD>

Cada mensagem é finalizada com \n.

Exemplos de Mensagens
Mensagem	Função
AGUARDE|Esperando jogador 2	Aguarda novo jogador
INICIO|X	Define símbolo do jogador
TABULEIRO|X O X	Atualiza estado do tabuleiro
SUA_VEZ|	Libera jogada
ESPERE|	Aguarda turno do adversário
FIM|Parabéns	Finaliza partida


Comunicação TCP

A aplicação utiliza sockets TCP puros através das classes:

ServerSocket
Socket
PrintWriter
BufferedReader

Toda comunicação ocorre exclusivamente entre clientes e servidor.

Análise com Wireshark

Durante os testes foi possível identificar:

Three-Way Handshake TCP;
troca de mensagens da aplicação;
encapsulamento dos dados em segmentos TCP;
encerramento da conexão através de pacotes FIN/ACK.

Filtro utilizado no Wireshark:

tcp.port == 9999
Exemplos Observados no Wireshark
Estabelecimento da conexão
SYN
SYN, ACK
ACK
Troca de dados
PSH, ACK
Encerramento
FIN, ACK

Integrantes
Marcos Gabriel Monteiro
Guilherme Evangelista dos Anjos


Projeto acadêmico desenvolvido para fins educacionais.
