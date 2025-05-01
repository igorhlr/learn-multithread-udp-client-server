package local.redes;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketException;

/**
 *
 * @author Igor Rozalem
 */
public class Servidor {

    private static final int PORTA = 50000;
    private static final int TAMANHO_BUFFER = 8192; // Buffer maior para objetos serializados

    /**
     * Classe interna que implementa o processamento de cada requisição de
     * cliente em uma thread separada
     */
    private static class TratadorRequisicao implements Runnable {

        private DatagramPacket pacoteRecebido;
        private DatagramSocket socketServidor;

        /**
         * Construtor que recebe o pacote e o socket do servidor
         *
         * @param pacote O pacote recebido do cliente
         * @param socket O socket do servidor para enviar resposta
         */
        public TratadorRequisicao(DatagramPacket pacote, DatagramSocket socket) {
            this.pacoteRecebido = pacote;
            this.socketServidor = socket;
        }

        @Override
        public void run() {
            try {
                // Obter dados do pacote
                byte[] dadosRecebidos = pacoteRecebido.getData();

                // Obter endereço e porta do cliente para resposta
                InetAddress enderecoCliente = pacoteRecebido.getAddress();
                int portaCliente = pacoteRecebido.getPort();

                // Deserializar o objeto Pessoa
                ByteArrayInputStream byteStream = new ByteArrayInputStream(dadosRecebidos);
                ObjectInputStream objectStream = new ObjectInputStream(byteStream);
                Pessoa pessoa = (Pessoa) objectStream.readObject();

                // Mostrar dados no console
                System.out.println("======================================");
                System.out.println("Thread ID: " + Thread.currentThread().getId());
                System.out.println("Cliente: " + enderecoCliente + ":" + portaCliente);
                System.out.println("Objeto Pessoa recebido do cliente:");
                System.out.println("Nome: " + pessoa.getNome());
                System.out.println("Idade: " + pessoa.getIdade());
                System.out.println("======================================");

                // Preparar resposta para o cliente
                String mensagemResposta = "Dados recebidos corretamente!";
                byte[] dadosResposta = mensagemResposta.getBytes();

                // Criar pacote de resposta
                DatagramPacket pacoteResposta = new DatagramPacket(
                        dadosResposta,
                        dadosResposta.length,
                        enderecoCliente,
                        portaCliente
                );

                // Enviar resposta
                socketServidor.send(pacoteResposta);

                System.out.println("Ultima resposta enviada para " + enderecoCliente + ":" + portaCliente);
                System.out.println("Servidor continua ativo na porta 50000");
                System.out.println("Aguardando conexões...");
                System.out.println("======================================");

            } catch (Exception e) {
                System.err.println("Erro na thread do servidor: " + e.getMessage());
                e.printStackTrace();
            }
        }
    }

    public static void main(String[] args) {
        DatagramSocket socketServidor = null;

        try {
            // Criar socket UDP na porta 50000
            socketServidor = new DatagramSocket(PORTA);
            System.out.println("Servidor iniciado na porta " + PORTA);
            System.out.println("Aguardando conexões...");
            System.out.println("======================================");

            // Loop infinito para aceitar conexões
            while (true) {
                // Preparar buffer para receber dados
                byte[] buffer = new byte[TAMANHO_BUFFER];
                DatagramPacket pacoteRecebido = new DatagramPacket(
                        buffer, 
                        buffer.length);

                // Aguardar recebimento de pacote (bloqueante)
                socketServidor.receive(pacoteRecebido);

                System.out.println("Conexão recebida de " + pacoteRecebido.getAddress() + ":" + pacoteRecebido.getPort());

                // Criar nova thread para processar a requisição usando a classe interna
                Thread threadCliente = new Thread(new TratadorRequisicao(pacoteRecebido, socketServidor));
                threadCliente.start();

                System.out.println("Thread " + threadCliente.getId() + " iniciada para atender o cliente");
            }

        } catch (SocketException e) {
            System.err.println("Erro ao criar socket: " + e.getMessage());
            e.printStackTrace();
        } catch (IOException e) {
            System.err.println("Erro de I/O: " + e.getMessage());
            e.printStackTrace();
        } finally {
            // Este bloco nunca deve ser executado no uso normal, pois o servidor deve permanecer em execução
            if (socketServidor != null && !socketServidor.isClosed()) {
                System.out.println("Encerrando servidor...");
                socketServidor.close();
            }
        }
    }
}
