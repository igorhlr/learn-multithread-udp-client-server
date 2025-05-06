package local.redes.multicast;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.InetAddress;
import java.net.MulticastSocket;
import java.net.SocketException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Gerenciador para comunicação multicast.
 * Esta classe fornece métodos para enviar e receber mensagens em um grupo multicast.
 * 
 * @author Igor Rozalem
 */
public class MulticastManager {
    private static final Logger LOGGER = Logger.getLogger(MulticastManager.class.getName());
    private static final DateTimeFormatter FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
    private static final int BUFFER_SIZE = 1024;
    
    private final InetAddress groupAddress;
    private final int port;
    private final String username;
    private MulticastSocket receiveSocket;
    private MulticastSocket sendSocket;
    private boolean running;
    private MessageListener listener;
    private final ExecutorService executorService;
    
    /**
     * Interface para recebimento de mensagens multicast.
     */
    public interface MessageListener {
        /**
         * Chamado quando uma mensagem é recebida.
         * 
         * @param message Mensagem recebida
         * @param sender Remetente da mensagem (pode ser null)
         */
        void onMessageReceived(String message, String sender);
    }
    
    /**
     * Cria um novo gerenciador multicast.
     * 
     * @param groupAddress Endereço do grupo multicast (224.0.0.0 a 239.255.255.255)
     * @param port Porta do grupo multicast
     * @param username Nome do usuário para identificação
     * @throws IOException Se ocorrer erro ao inicializar os sockets
     */
    public MulticastManager(InetAddress groupAddress, int port, String username) throws IOException {
        this.groupAddress = groupAddress;
        this.port = port;
        this.username = username;
        this.executorService = Executors.newCachedThreadPool();
        this.running = false;
        
        initializeSockets();
    }
    
    /**
     * Inicializa os sockets para comunicação multicast.
     * 
     * @throws IOException Se ocorrer erro ao criar os sockets
     */
    private void initializeSockets() throws IOException {
        // Socket para receber mensagens
        receiveSocket = new MulticastSocket(port);
        receiveSocket.joinGroup(groupAddress);
        
        // Socket para enviar mensagens
        sendSocket = new MulticastSocket();
        
        LOGGER.log(Level.INFO, "Inicializado gerenciador multicast para grupo {0}:{1}", 
                new Object[]{groupAddress.getHostAddress(), port});
    }
    
    /**
     * Define o ouvinte de mensagens.
     * 
     * @param listener Ouvinte para receber mensagens
     */
    public void setMessageListener(MessageListener listener) {
        this.listener = listener;
    }
    
    /**
     * Inicia o recebimento de mensagens em uma thread separada.
     */
    public void startReceiving() {
        if (running) {
            return;
        }
        
        running = true;
        
        executorService.submit(() -> {
            try {
                receiveMessages();
            } catch (IOException e) {
                LOGGER.log(Level.SEVERE, "Erro ao receber mensagens multicast", e);
            }
        });
        
        // Enviar mensagem de entrada no grupo
        try {
            sendSystemMessage(username + " entrou no chat.");
        } catch (IOException e) {
            LOGGER.log(Level.WARNING, "Não foi possível enviar mensagem de entrada", e);
        }
    }
    
    /**
     * Método executado em thread separada para receber mensagens.
     * 
     * @throws IOException Se ocorrer erro ao receber mensagens
     */
    private void receiveMessages() throws IOException {
        LOGGER.log(Level.INFO, "Iniciando recebimento de mensagens multicast");
        
        byte[] buffer = new byte[BUFFER_SIZE];
        
        while (running) {
            try {
                // Preparar pacote para receber dados
                DatagramPacket packet = new DatagramPacket(buffer, buffer.length);
                
                // Receber pacote (bloqueante)
                receiveSocket.receive(packet);
                
                // Converter dados para string
                String message = new String(packet.getData(), 0, packet.getLength());
                
                // Processar apenas mensagens que não tenham sido enviadas por este usuário
                if (!message.startsWith(username + " diz:") && !message.startsWith(username + " ")) {
                    // Extrair nome do remetente, se presente
                    String sender = null;
                    if (message.contains(" diz: ")) {
                        sender = message.substring(0, message.indexOf(" diz:"));
                    }
                    
                    // Notificar ouvinte, se registrado
                    if (listener != null) {
                        final String finalSender = sender;
                        listener.onMessageReceived(message, finalSender);
                    }
                    
                    LOGGER.log(Level.FINE, "Mensagem recebida: {0}", message);
                }
                
                // Limpar buffer para próxima mensagem
                buffer = new byte[BUFFER_SIZE];
                
            } catch (SocketException e) {
                if (!running) {
                    // Socket fechado porque o gerenciador foi parado
                    break;
                }
                LOGGER.log(Level.SEVERE, "Erro no socket ao receber mensagem", e);
            } catch (IOException e) {
                LOGGER.log(Level.SEVERE, "Erro ao receber mensagem", e);
            }
        }
    }
    
    /**
     * Envia uma mensagem para o grupo multicast.
     * 
     * @param message Mensagem a ser enviada
     * @throws IOException Se ocorrer erro ao enviar a mensagem
     */
    public void sendMessage(String message) throws IOException {
        if (message == null || message.trim().isEmpty()) {
            return;
        }
        
        // Formatar mensagem com nome do usuário
        String formattedMessage = username + " diz: " + message;
        byte[] data = formattedMessage.getBytes();
        
        // Criar pacote para envio
        DatagramPacket packet = new DatagramPacket(
                data,
                data.length,
                groupAddress,
                port
        );
        
        // Enviar pacote
        sendSocket.send(packet);
        
        LOGGER.log(Level.FINE, "Mensagem enviada: {0}", formattedMessage);
    }
    
    /**
     * Envia uma mensagem de sistema sem nome de usuário.
     * 
     * @param message Mensagem de sistema a ser enviada
     * @throws IOException Se ocorrer erro ao enviar a mensagem
     */
    private void sendSystemMessage(String message) throws IOException {
        if (message == null || message.trim().isEmpty()) {
            return;
        }
        
        // Formatar mensagem como notificação de sistema
        String formattedMessage = username + " " + message;
        byte[] data = formattedMessage.getBytes();
        
        // Criar pacote para envio
        DatagramPacket packet = new DatagramPacket(
                data,
                data.length,
                groupAddress,
                port
        );
        
        // Enviar pacote
        sendSocket.send(packet);
        
        LOGGER.log(Level.FINE, "Mensagem de sistema enviada: {0}", formattedMessage);
    }
    
    /**
     * Para o recebimento de mensagens e libera recursos.
     */
    public void stop() {
        if (!running) {
            return;
        }
        
        running = false;
        
        try {
            // Enviar mensagem de saída do grupo
            sendSystemMessage("saiu do chat.");
        } catch (IOException e) {
            LOGGER.log(Level.WARNING, "Não foi possível enviar mensagem de saída", e);
        }
        
        try {
            if (receiveSocket != null) {
                receiveSocket.leaveGroup(groupAddress);
                receiveSocket.close();
            }
            
            if (sendSocket != null) {
                sendSocket.close();
            }
        } catch (IOException e) {
            LOGGER.log(Level.WARNING, "Erro ao fechar recursos", e);
        }
        
        executorService.shutdown();
        
        LOGGER.log(Level.INFO, "Gerenciador multicast parado");
    }
    
    /**
     * Retorna o nome do usuário atual.
     * 
     * @return Nome do usuário
     */
    public String getUsername() {
        return username;
    }
    
    /**
     * Retorna o endereço do grupo multicast.
     * 
     * @return Endereço do grupo
     */
    public InetAddress getGroupAddress() {
        return groupAddress;
    }
    
    /**
     * Retorna a porta do grupo multicast.
     * 
     * @return Porta do grupo
     */
    public int getPort() {
        return port;
    }
    
    /**
     * Verifica se o gerenciador está em execução.
     * 
     * @return true se estiver recebendo mensagens, false caso contrário
     */
    public boolean isRunning() {
        return running;
    }
    
    /**
     * Retorna timestamp formatado do momento atual.
     * 
     * @return String com timestamp atual
     */
    public static String getCurrentTimestamp() {
        return LocalDateTime.now().format(FORMATTER);
    }
}