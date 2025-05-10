package local.redes.multicast;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import javax.swing.*;
import java.awt.*;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.net.InetAddress;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

/**
 * Testes unitários para a classe ChatClient.
 * Usa reflexão para acessar componentes e métodos privados.
 */
@ExtendWith(MockitoExtension.class)
public class ChatClientTest {

    @Mock
    private MulticastManager mockManager;
    
    private ChatClient client;
    private JTextArea chatArea;
    private JTextField messageField;

    /**
     * Configuração comum para cada teste.
     */
    @BeforeEach
    public void setUp() throws Exception {
        // Injetar o mock em uma nova instância do ChatClient
        client = createChatClientWithMockManager();
        
        // Acessar componentes de UI
        chatArea = getComponent(client, "chatArea", JTextArea.class);
        messageField = getComponent(client, "messageField", JTextField.class);
        
        // Limpar o conteúdo do chatArea para isolar os testes
        chatArea.setText("");
    }
    
    /**
     * Limpar recursos após cada teste.
     */
    @AfterEach
    public void tearDown() {
        if (client != null) {
            client.dispose();
        }
    }

    /**
     * Helper para criar instância do ChatClient com manager mockado.
     */
    private ChatClient createChatClientWithMockManager() throws Exception {
        // Criar instância com argumentos fictícios
        ChatClient chatClient = new ChatClient(
                InetAddress.getByName("239.0.0.1"),
                9000,
                "TestUser"
        );
        
        // Injetar mock no campo multicastManager usando reflection
        Field managerField = ChatClient.class.getDeclaredField("multicastManager");
        managerField.setAccessible(true);
        
        // Salvar o manager original para fechá-lo
        MulticastManager originalManager = (MulticastManager) managerField.get(chatClient);
        if (originalManager != null) {
            originalManager.stop();
        }
        
        // Substituir pelo mock (sem configuração desnecessária)
        managerField.set(chatClient, mockManager);
        
        return chatClient;
    }
    
    /**
     * Obter componente de interface pelo nome do campo usando reflexão.
     */
    private <T extends Component> T getComponent(ChatClient client, String fieldName, Class<T> type) 
            throws Exception {
        Field field = ChatClient.class.getDeclaredField(fieldName);
        field.setAccessible(true);
        return type.cast(field.get(client));
    }
    
    /**
     * Chamar método privado usando reflexão.
     */
    private Object callPrivateMethod(Object instance, String methodName, Class<?>[] paramTypes, 
            Object[] params) throws Exception {
        Method method = instance.getClass().getDeclaredMethod(methodName, paramTypes);
        method.setAccessible(true);
        return method.invoke(instance, params);
    }

    @Test
    @DisplayName("Deve configurar componentes da interface corretamente")
    public void testInterfaceComponentsInitialization() throws Exception {
        // Verificar componentes principais
        assertNotNull(chatArea);
        assertFalse(chatArea.isEditable());
        assertTrue(chatArea.getLineWrap());
        
        assertNotNull(messageField);
        assertNotNull(getComponent(client, "sendButton", JButton.class));
        assertNotNull(getComponent(client, "statusLabel", JLabel.class));
        
        // Verificar o título da janela
        assertTrue(client.getTitle().contains("TestUser"));
    }
    
    @Test
    @DisplayName("Deve enviar mensagem quando solicitado")
    public void testSendMessage() throws Exception {
        // Configurar comportamento do mock apenas para o teste que realmente verifica isso
        when(mockManager.isRunning()).thenReturn(true);
        
        // Limpar campo de texto e definir nova mensagem
        messageField.setText("Mensagem de teste");
        
        // Chamar método sendMessage
        callPrivateMethod(client, "sendMessage", new Class<?>[]{}, new Object[]{});
        
        // Verificar que o manager foi chamado para enviar a mensagem
        verify(mockManager).sendMessage("Mensagem de teste");
        
        // Verificar que o campo de texto foi limpo
        assertEquals("", messageField.getText());
    }
    
    @Test
    @DisplayName("Não deve enviar mensagem vazia")
    public void testSendEmptyMessage() throws Exception {
        // Acessar o método sendMessage diretamente por reflexão
        // Vamos verificar se o código dentro do método vai verificar a mensagem vazia
        // sem configurar stubs desnecessários
        
        // Configurar campo de mensagem vazio
        messageField.setText("");
        
        // Primeiro, vamos verificar que o campo está realmente vazio
        assertEquals("", messageField.getText(), "O campo de mensagem deve estar vazio");
        
        // Chamar método sendMessage diretamente
        // Mesmo com isRunning() não configurado, se a mensagem estiver vazia,
        // o método não deve chamar sendMessage() no mockManager
        Method sendMessageMethod = ChatClient.class.getDeclaredMethod("sendMessage");
        sendMessageMethod.setAccessible(true);
        sendMessageMethod.invoke(client);
        
        // Verificar que o manager NÃO foi chamado para enviar mensagem
        verify(mockManager, never()).sendMessage(anyString());
    }
    
    @Test
    @DisplayName("Deve exibir mensagem do sistema corretamente")
    public void testAppendSystemMessage() throws Exception {
        // Chamar método appendSystemMessage
        String message = "Sistema iniciado";
        callPrivateMethod(client, "appendSystemMessage", new Class<?>[]{String.class}, new Object[]{message});
        
        // Verificar texto exibido
        String chatText = chatArea.getText();
        assertTrue(chatText.contains(message), "O texto no chat deve conter a mensagem");
        assertTrue(chatText.contains("["), "O texto no chat deve conter um timestamp entre colchetes");
    }
    
    @Test
    @DisplayName("Deve exibir mensagem do próprio usuário corretamente")
    public void testAppendMyMessage() throws Exception {
        // Chamar método appendMyMessage
        String message = "Minha mensagem";
        callPrivateMethod(client, "appendMyMessage", new Class<?>[]{String.class}, new Object[]{message});
        
        // Verificar texto exibido
        String chatText = chatArea.getText();
        assertTrue(chatText.contains(message), "O texto no chat deve conter a mensagem");
        assertTrue(chatText.contains("Você diz:"), "O texto no chat deve conter 'Você diz:'");
    }
    
    @Test
    @DisplayName("Deve processar mensagem recebida corretamente")
    public void testOnMessageReceived() throws Exception {
        // Simular recebimento de mensagem
        String message = "User1 diz: Olá!";
        
        // Primeiro vamos verificar que o chatArea está vazio
        assertEquals("", chatArea.getText(), "chatArea deve estar vazio antes do teste");
        
        // Chamar o método diretamente em vez de usar EventDispatchThread
        Method appendReceivedMethod = ChatClient.class.getDeclaredMethod(
                "appendReceivedMessage", String.class, String.class);
        appendReceivedMethod.setAccessible(true);
        appendReceivedMethod.invoke(client, "User1", message);
        
        // Verificar texto exibido após chamar o método diretamente
        String chatText = chatArea.getText();
        assertTrue(chatText.contains(message), "O texto no chat deve conter a mensagem");
    }
}
