package local.redes;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketException;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Utilitários para testes de comunicação em rede.
 */
public class NetworkTestUtils {
    
    private static final Logger LOGGER = Logger.getLogger(NetworkTestUtils.class.getName());
    
    /**
     * Envia um objeto serializado para um endereço e porta.
     * 
     * @param object Objeto a ser enviado
     * @param address Endereço de destino
     * @param port Porta de destino
     * @return true se enviado com sucesso, false caso contrário
     */
    public static boolean sendObject(Object object, InetAddress address, int port) {
        try (DatagramSocket socket = new DatagramSocket()) {
            // Serializar objeto
            byte[] data = serializeObject(object);
            
            // Criar pacote
            DatagramPacket packet = new DatagramPacket(data, data.length, address, port);
            
            // Enviar
            socket.send(packet);
            return true;
            
        } catch (IOException e) {
            LOGGER.log(Level.SEVERE, "Erro ao enviar objeto", e);
            return false;
        }
    }
    
    /**
     * Recebe um objeto em uma porta específica com timeout.
     * 
     * @param <T> Tipo do objeto esperado
     * @param port Porta para receber
     * @param timeout Timeout em milissegundos
     * @param expectedType Classe esperada
     * @return Objeto recebido ou null se ocorrer erro ou timeout
     */
    public static <T> T receiveObject(int port, int timeout, Class<T> expectedType) {
        try (DatagramSocket socket = new DatagramSocket(port)) {
            socket.setSoTimeout(timeout);
            
            // Buffer para receber dados
            byte[] buffer = new byte[8192];
            DatagramPacket packet = new DatagramPacket(buffer, buffer.length);
            
            // Receber pacote
            socket.receive(packet);
            
            // Deserializar objeto
            return deserializeObject(packet.getData(), expectedType);
            
        } catch (IOException | ClassNotFoundException e) {
            LOGGER.log(Level.SEVERE, "Erro ao receber objeto", e);
            return null;
        }
    }
    
    /**
     * Envia um objeto e espera por uma resposta string.
     * 
     * @param object Objeto a enviar
     * @param address Endereço de destino
     * @param port Porta de destino
     * @param timeout Timeout em milissegundos
     * @return String de resposta ou null se falhar
     */
    public static String sendObjectAndReceiveString(Object object, InetAddress address, int port, int timeout) {
        try (DatagramSocket socket = new DatagramSocket()) {
            socket.setSoTimeout(timeout);
            
            // Serializar objeto
            byte[] data = serializeObject(object);
            
            // Criar pacote
            DatagramPacket sendPacket = new DatagramPacket(data, data.length, address, port);
            
            // Enviar
            socket.send(sendPacket);
            
            // Preparar para receber resposta
            byte[] buffer = new byte[1024];
            DatagramPacket receivePacket = new DatagramPacket(buffer, buffer.length);
            
            // Receber resposta
            socket.receive(receivePacket);
            
            // Converter para string
            return new String(receivePacket.getData(), 0, receivePacket.getLength());
            
        } catch (IOException e) {
            LOGGER.log(Level.SEVERE, "Erro na comunicação", e);
            return null;
        }
    }
    
    /**
     * Iniciar um servidor UDP em thread separada para testes.
     * 
     * @param port Porta para o servidor
     * @param responseText Texto de resposta para qualquer requisição
     * @param latch Latch para sinalizar término (opcional)
     * @return Thread do servidor
     */
    public static Thread startMockUdpServer(int port, String responseText, CountDownLatch latch) {
        Thread serverThread = new Thread(() -> {
            try (DatagramSocket socket = new DatagramSocket(port)) {
                // Buffer para receber dados
                byte[] buffer = new byte[8192];
                DatagramPacket packet = new DatagramPacket(buffer, buffer.length);
                
                // Receber pacote (bloqueante)
                socket.receive(packet);
                
                // Preparar resposta
                byte[] responseData = responseText.getBytes();
                DatagramPacket responsePacket = new DatagramPacket(
                        responseData,
                        responseData.length,
                        packet.getAddress(),
                        packet.getPort()
                );
                
                // Enviar resposta
                socket.send(responsePacket);
                
            } catch (IOException e) {
                LOGGER.log(Level.SEVERE, "Erro no servidor mock", e);
            } finally {
                if (latch != null) {
                    latch.countDown();
                }
            }
        });
        
        serverThread.setDaemon(true);
        serverThread.start();
        return serverThread;
    }
    
    /**
     * Espera até que um servidor esteja disponível em uma porta específica.
     * 
     * @param address Endereço do servidor
     * @param port Porta do servidor
     * @param timeoutMillis Timeout em milissegundos
     * @return true se o servidor estiver disponível, false caso contrário
     */
    public static boolean waitForServerAvailable(InetAddress address, int port, long timeoutMillis) {
        long startTime = System.currentTimeMillis();
        
        while (System.currentTimeMillis() - startTime < timeoutMillis) {
            try (DatagramSocket socket = new DatagramSocket()) {
                socket.setSoTimeout(200);
                socket.connect(address, port);
                return true;
            } catch (SocketException e) {
                // Continuar tentando
                try {
                    Thread.sleep(100);
                } catch (InterruptedException ie) {
                    Thread.currentThread().interrupt();
                    return false;
                }
            }
        }
        
        return false;
    }
    
    /**
     * Serializa um objeto para um array de bytes.
     * 
     * @param object Objeto a ser serializado
     * @return Array de bytes
     * @throws IOException Se ocorrer erro de serialização
     */
    public static byte[] serializeObject(Object object) throws IOException {
        try (ByteArrayOutputStream baos = new ByteArrayOutputStream();
             ObjectOutputStream oos = new ObjectOutputStream(baos)) {
            oos.writeObject(object);
            oos.flush();
            return baos.toByteArray();
        }
    }
    
    /**
     * Deserializa um array de bytes para um objeto.
     * 
     * @param <T> Tipo do objeto
     * @param data Array de bytes
     * @param expectedType Classe esperada
     * @return Objeto deserializado
     * @throws IOException Se ocorrer erro de deserialização
     * @throws ClassNotFoundException Se a classe não for encontrada
     */
    @SuppressWarnings("unchecked")
    public static <T> T deserializeObject(byte[] data, Class<T> expectedType) 
            throws IOException, ClassNotFoundException {
        try (ByteArrayInputStream bais = new ByteArrayInputStream(data);
             ObjectInputStream ois = new ObjectInputStream(bais)) {
            Object obj = ois.readObject();
            
            if (expectedType.isInstance(obj)) {
                return (T) obj;
            } else {
                throw new ClassCastException("Objeto recebido não é do tipo esperado: " 
                        + obj.getClass().getName());
            }
        }
    }
    
    /**
     * Executa uma função com timeout.
     * 
     * @param <T> Tipo de retorno da função
     * @param runnable Função a executar
     * @param timeoutMillis Timeout em milissegundos
     * @return Resultado da função ou null se timeout
     * @throws Exception Se ocorrer erro na execução
     */
    public static <T> T runWithTimeout(RunnableWithResult<T> runnable, long timeoutMillis) 
            throws Exception {
        final T[] result = (T[]) new Object[1];
        final Exception[] exception = new Exception[1];
        final CountDownLatch latch = new CountDownLatch(1);
        
        Thread thread = new Thread(() -> {
            try {
                result[0] = runnable.run();
            } catch (Exception e) {
                exception[0] = e;
            } finally {
                latch.countDown();
            }
        });
        
        thread.setDaemon(true);
        thread.start();
        
        boolean completed = latch.await(timeoutMillis, TimeUnit.MILLISECONDS);
        
        if (!completed) {
            thread.interrupt();
            return null;
        }
        
        if (exception[0] != null) {
            throw exception[0];
        }
        
        return result[0];
    }
    
    /**
     * Interface funcional para execução com resultado.
     */
    @FunctionalInterface
    public interface RunnableWithResult<T> {
        T run() throws Exception;
    }
}
