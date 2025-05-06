package local.redes;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Servidor UDP para processar requisições de clientes usando threads.
 * 
 * @author Igor Rozalem
 */
public class Servidor {

    private static final int PORTA = 50000;
    private static final int TAMANHO_BUFFER = 8192; // Buffer maior para objetos serializados
    private static final Logger LOGGER = Logger.getLogger(Servidor.class.getName());
    private static final DateTimeFormatter FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
    
    // Pool de threads para gerenciar as conexões de clientes
    private static final ExecutorService THREAD_POOL = Executors.newCachedThreadPool();

    /**
     * Classe interna que implementa o processamento de cada requisição de
     * cliente em uma thread separada.
     */
    private static class TratadorRequisicao implements Runnable {
        private final DatagramPacket pacoteRecebido;
        private final DatagramSocket socketServidor;

        /**
         * Construtor que recebe o pacote e o socket do servidor.
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
                processarRequisicao();
            } catch (Exception e) {
                LOGGER.log(Level.SEVERE, "Erro na thread do servidor", e);
            }
        }
        
        /**
         * Processa a requisição do cliente.
         * 
         * @throws Exception Se ocorrer um erro ao processar a requisição
         */
        private void processarRequisicao() throws Exception {
            // Obter dados do pacote
            byte[] dadosRecebidos = pacoteRecebido.getData();

            // Obter endereço e porta do cliente para resposta
            InetAddress enderecoCliente = pacoteRecebido.getAddress();
            int portaCliente = pacoteRecebido.getPort();
            String enderecoClienteCompleto = enderecoCliente + ":" + portaCliente;

            // Deserializar o objeto Pessoa
            Pessoa pessoa;
            try (ByteArrayInputStream byteStream = new ByteArrayInputStream(dadosRecebidos);
                 ObjectInputStream objectStream = new ObjectInputStream(byteStream)) {
                pessoa = (Pessoa) objectStream.readObject();
            }

            // Mostrar dados no console
            String threadId = String.valueOf(Thread.currentThread().getId());
            logRequisicao(threadId, enderecoClienteCompleto, pessoa);

            // Preparar resposta para o cliente
            String mensagemResposta = montarResposta(pessoa);
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

            logRespostaEnviada(enderecoClienteCompleto);
        }
        
        /**
         * Monta a mensagem de resposta para o cliente.
         * 
         * @param pessoa A pessoa recebida do cliente
         * @return A mensagem de resposta
         */
        private String montarResposta(Pessoa pessoa) {
            return String.format(
                "Olá %s, seus dados foram recebidos com sucesso!\n" +
                "Você tem %d anos.\n" +
                "Timestamp: %s",
                pessoa.getNome(),
                pessoa.getIdade(),
                LocalDateTime.now().format(FORMATTER)
            );
        }
        
        /**
         * Registra informações da requisição recebida.
         * 
         * @param threadId ID da thread atual
         * @param enderecoCliente Endereço do cliente
         * @param pessoa Dados da pessoa recebida
         */
        private void logRequisicao(String threadId, String enderecoCliente, Pessoa pessoa) {
            System.out.println("======================================");
            System.out.println("Thread ID: " + threadId);
            System.out.println("Cliente: " + enderecoCliente);
            System.out.println("Objeto Pessoa recebido do cliente:");
            System.out.println("Nome: " + pessoa.getNome());
            System.out.println("Idade: " + pessoa.getIdade());
            System.out.println("Horário: " + LocalDateTime.now().format(FORMATTER));
            System.out.println("======================================");
        }
        
        /**
         * Registra informações sobre a resposta enviada.
         * 
         * @param enderecoCliente Endereço do cliente
         */
        private void logRespostaEnviada(String enderecoCliente) {
            System.out.println("Resposta enviada para " + enderecoCliente);
            System.out.println("Servidor continua ativo na porta " + PORTA);
            System.out.println("Aguardando conexões...");
            System.out.println("======================================");
        }
    }

    /**
     * Método principal que inicia o servidor.
     * 
     * @param args Argumentos da linha de comando (não utilizados)
     */
    public static void main(String[] args) {
        try (DatagramSocket socketServidor = new DatagramSocket(PORTA)) {
            iniciarServidor(socketServidor);
        } catch (SocketException e) {
            LOGGER.log(Level.SEVERE, "Erro ao criar socket", e);
        } catch (IOException e) {
            LOGGER.log(Level.SEVERE, "Erro de I/O", e);
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Erro inesperado", e);
        }
    }
    
    /**
     * Inicia o servidor e aguarda conexões de clientes.
     * 
     * @param socketServidor O socket UDP do servidor
     * @throws IOException Se ocorrer um erro de I/O
     */
    private static void iniciarServidor(DatagramSocket socketServidor) throws IOException {
        System.out.println("=================================================");
        System.out.println("Servidor iniciado na porta " + PORTA);
        System.out.println("Horário de início: " + LocalDateTime.now().format(FORMATTER));
        System.out.println("Aguardando conexões...");
        System.out.println("=================================================");

        // Loop infinito para aceitar conexões
        while (true) {
            // Preparar buffer para receber dados
            byte[] buffer = new byte[TAMANHO_BUFFER];
            DatagramPacket pacoteRecebido = new DatagramPacket(buffer, buffer.length);

            // Aguardar recebimento de pacote (bloqueante)
            socketServidor.receive(pacoteRecebido);
            
            String clienteInfo = pacoteRecebido.getAddress() + ":" + pacoteRecebido.getPort();
            System.out.println("Conexão recebida de " + clienteInfo);

            // Submeter tarefa ao pool de threads para processamento assíncrono
            THREAD_POOL.submit(new TratadorRequisicao(pacoteRecebido, socketServidor));
            
            System.out.println("Nova thread iniciada para atender o cliente " + clienteInfo);
        }
    }
}
