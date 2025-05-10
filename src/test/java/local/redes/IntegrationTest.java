package local.redes;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.Timeout;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectOutputStream;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketTimeoutException;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.logging.Level;
import java.util.logging.Logger;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Testes de integração para o sistema Cliente-Servidor UDP.
 * Simula a comunicação entre cliente e servidor em threads separadas.
 */
public class IntegrationTest {

    private static final Logger LOGGER = Logger.getLogger(IntegrationTest.class.getName());
    private static final int TEST_PORT = 50100; // Porta diferente da aplicação real
    private static final String TEST_HOST = "127.0.0.1";
    private static final int TIMEOUT_SECONDS = 10; // Aumentado para evitar falsos negativos
    private static final int CLIENT_TIMEOUT = 1000; // 1 segundo
    
    /**
     * Thread que simula o servidor para testes.
     */
    private static class ServerThread implements Runnable {
        private final int port;
        private final CountDownLatch serverReady;
        private final CountDownLatch serverDone;
        private volatile boolean running = true;
        private DatagramSocket socket;
        
        public ServerThread(int port, CountDownLatch serverReady, CountDownLatch serverDone) {
            this.port = port;
            this.serverReady = serverReady;
            this.serverDone = serverDone;
        }
        
        @Override
        public void run() {
            try {
                socket = new DatagramSocket(port);
                
                // Definir timeout para evitar bloqueio indefinido
                socket.setSoTimeout(1000);
                
                // Notificar que o servidor está pronto
                serverReady.countDown();
                LOGGER.info("Servidor de teste iniciado na porta " + port);
                
                // Loop para receber pacotes
                while (running) {
                    try {
                        // Receber pacote
                        byte[] buffer = new byte[8192];
                        DatagramPacket packet = new DatagramPacket(buffer, buffer.length);
                        
                        socket.receive(packet);
                        LOGGER.info("Servidor recebeu pacote de " + packet.getAddress() + ":" + packet.getPort());
                        
                        // Processar o pacote
                        InetAddress clientAddress = packet.getAddress();
                        int clientPort = packet.getPort();
                        
                        // Preparar resposta
                        String response = "Teste concluído com sucesso!";
                        byte[] responseData = response.getBytes();
                        
                        // Enviar resposta
                        DatagramPacket responsePacket = new DatagramPacket(
                                responseData,
                                responseData.length,
                                clientAddress,
                                clientPort
                        );
                        socket.send(responsePacket);
                        LOGGER.info("Servidor enviou resposta para " + clientAddress + ":" + clientPort);
                        
                    } catch (SocketTimeoutException e) {
                        // Timeout normal, continuar loop
                    } catch (IOException e) {
                        if (!running) {
                            break;
                        }
                        LOGGER.log(Level.SEVERE, "Erro de IO no servidor", e);
                    }
                }
                
            } catch (Exception e) {
                LOGGER.log(Level.SEVERE, "Erro no servidor", e);
            } finally {
                if (socket != null && !socket.isClosed()) {
                    socket.close();
                }
                // Notificar que o servidor terminou
                serverDone.countDown();
                LOGGER.info("Servidor de teste finalizado");
            }
        }
        
        public void stop() {
            running = false;
            if (socket != null) {
                socket.close();
            }
        }
    }
    
    @Test
    @DisplayName("Deve enviar objeto Pessoa e receber resposta do servidor")
    @Timeout(value = TIMEOUT_SECONDS, unit = TimeUnit.SECONDS)
    public void testClientServerCommunication() throws Exception {
        // Preparar latches para sincronização entre threads
        CountDownLatch serverReady = new CountDownLatch(1);
        CountDownLatch serverDone = new CountDownLatch(1);
        CountDownLatch clientDone = new CountDownLatch(1);
        
        // Resultados do teste
        final String[] responseReceived = new String[1];
        final boolean[] success = {false};
        
        // Iniciar servidor em thread separada
        ExecutorService executor = Executors.newCachedThreadPool();
        ServerThread serverThread = new ServerThread(TEST_PORT, serverReady, serverDone);
        executor.submit(serverThread);
        
        // Aguardar servidor ficar pronto
        assertTrue(serverReady.await(5, TimeUnit.SECONDS), "Servidor deve ficar pronto em 5 segundos");
        LOGGER.info("Servidor está pronto. Iniciando cliente...");
        
        // Thread do cliente
        executor.submit(() -> {
            DatagramSocket clientSocket = null;
            try {
                clientSocket = new DatagramSocket();
                // Configurar timeout
                clientSocket.setSoTimeout(CLIENT_TIMEOUT);
                
                // Criar objeto para envio
                Pessoa pessoa = new Pessoa("Teste Integração", 100);
                
                // Serializar objeto
                ByteArrayOutputStream byteStream = new ByteArrayOutputStream();
                ObjectOutputStream objectStream = new ObjectOutputStream(byteStream);
                objectStream.writeObject(pessoa);
                objectStream.flush();
                byte[] sendData = byteStream.toByteArray();
                
                // Preparar pacote
                InetAddress serverAddress = InetAddress.getByName(TEST_HOST);
                DatagramPacket sendPacket = new DatagramPacket(
                        sendData,
                        sendData.length,
                        serverAddress,
                        TEST_PORT
                );
                
                // Enviar pacote
                LOGGER.info("Cliente enviando pacote para " + TEST_HOST + ":" + TEST_PORT);
                clientSocket.send(sendPacket);
                
                // Aguardar resposta
                byte[] receiveData = new byte[1024];
                DatagramPacket receivePacket = new DatagramPacket(receiveData, receiveData.length);
                
                LOGGER.info("Cliente aguardando resposta...");
                clientSocket.receive(receivePacket);
                
                // Processar resposta
                responseReceived[0] = new String(
                        receivePacket.getData(), 
                        0, 
                        receivePacket.getLength()
                );
                
                LOGGER.info("Cliente recebeu resposta: " + responseReceived[0]);
                success[0] = true;
                
            } catch (Exception e) {
                LOGGER.log(Level.SEVERE, "Erro no cliente", e);
            } finally {
                if (clientSocket != null) {
                    clientSocket.close();
                }
                clientDone.countDown();
                LOGGER.info("Cliente finalizado");
            }
        });
        
        // Aguardar conclusão do cliente com tempo maior
        assertTrue(clientDone.await(8, TimeUnit.SECONDS), "Cliente deve terminar em 8 segundos");
        
        // Parar servidor
        LOGGER.info("Parando servidor...");
        serverThread.stop();
        assertTrue(serverDone.await(2, TimeUnit.SECONDS), "Servidor deve terminar em 2 segundos");
        
        // Encerrar executor
        executor.shutdownNow();
        assertTrue(executor.awaitTermination(2, TimeUnit.SECONDS), "Executor deve terminar em 2 segundos");
        
        // Verificar resultados
        assertTrue(success[0], "Comunicação deve ser bem-sucedida");
        assertNotNull(responseReceived[0], "Resposta não deve ser nula");
        assertEquals("Teste concluído com sucesso!", responseReceived[0]);
    }
}
