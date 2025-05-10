package local.redes.multicast;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.MulticastSocket;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

/**
 * Testes unitários para a classe MulticastManager.
 * 
 * Nota: Alguns testes requerem suporte a multicast na rede local.
 */
@ExtendWith(MockitoExtension.class)
public class MulticastManagerTest {

    @Mock
    private MulticastManager.MessageListener mockListener;
    
    private MulticastManager manager;
    private final String username = "TestUser";
    private InetAddress groupAddress;
    private final int port = 50501; // Porta de teste diferente da padrão
    
    @BeforeEach
    public void setUp() throws Exception {
        // Usar um endereço multicast válido para testes
        groupAddress = InetAddress.getByName("239.255.255.250");
    }
    
    @AfterEach
    public void tearDown() {
        if (manager != null && manager.isRunning()) {
            manager.stop();
        }
    }
    
    @Test
    @DisplayName("Deve criar MulticastManager com valores corretos")
    public void testConstrutor() throws IOException {
        manager = new MulticastManager(groupAddress, port, username);
        
        assertEquals(groupAddress, manager.getGroupAddress());
        assertEquals(port, manager.getPort());
        assertEquals(username, manager.getUsername());
        assertFalse(manager.isRunning());
    }
    
    @Test
    @DisplayName("Deve iniciar e parar corretamente o recebimento de mensagens")
    public void testStartStopReceiving() throws IOException {
        manager = new MulticastManager(groupAddress, port, username);
        
        assertFalse(manager.isRunning());
        
        manager.startReceiving();
        assertTrue(manager.isRunning());
        
        manager.stop();
        assertFalse(manager.isRunning());
    }
    
    @Test
    @DisplayName("Deve definir o listener de mensagens corretamente")
    public void testSetMessageListener() throws IOException {
        manager = new MulticastManager(groupAddress, port, username);
        
        // Verificar que o listener não foi definido inicialmente
        manager.setMessageListener(mockListener);
        
        // Como não temos acesso direto ao listener, precisamos testar indiretamente
        // enviando uma mensagem e verificando se o mockListener foi chamado
        // Este será testado em outros métodos
    }
    
    @Test
    @DisplayName("Deve formatar timestamp corretamente")
    public void testGetCurrentTimestamp() {
        String timestamp = MulticastManager.getCurrentTimestamp();
        
        assertNotNull(timestamp);
        assertTrue(timestamp.matches("\\d{4}-\\d{2}-\\d{2} \\d{2}:\\d{2}:\\d{2}"));
    }
    
    /**
     * Testes de integração que exigem suporte a multicast na rede.
     * Esses testes simulam a comunicação real entre instâncias.
     */
    
    @Test
    @DisplayName("Deve enviar e receber mensagens entre instâncias (teste de integração)")
    public void testEnvioRecebimentoMensagens() throws Exception {
        // Este teste requer duas instâncias do MulticastManager
        
        // Primeiro, criamos o listener que receberá a mensagem
        final CountDownLatch messageReceived = new CountDownLatch(1);
        final String[] receivedMessage = new String[1];
        
        // Criar primeiro manager (receptor)
        MulticastManager receiver = new MulticastManager(groupAddress, port, "Receiver");
        receiver.setMessageListener((message, sender) -> {
            receivedMessage[0] = message;
            messageReceived.countDown();
        });
        receiver.startReceiving();
        
        // Dar tempo para que o receiver se junte ao grupo
        Thread.sleep(500);
        
        // Criar segundo manager (emissor)
        MulticastManager sender = new MulticastManager(groupAddress, port, "Sender");
        
        try {
            // Enviar mensagem
            final String testMessage = "Mensagem de teste";
            sender.sendMessage(testMessage);
            
            // Aguardar recebimento da mensagem (com timeout)
            boolean received = messageReceived.await(5, TimeUnit.SECONDS);
            
            // Verificar
            assertTrue(received, "A mensagem deve ser recebida em 5 segundos");
            assertNotNull(receivedMessage[0], "A mensagem recebida não deve ser nula");
            assertTrue(receivedMessage[0].contains("Sender diz: " + testMessage), 
                    "A mensagem recebida deve conter o conteúdo enviado");
            
        } finally {
            // Limpar
            receiver.stop();
            sender.stop();
        }
    }
}
