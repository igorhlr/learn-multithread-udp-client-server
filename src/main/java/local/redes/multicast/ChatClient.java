package local.redes.multicast;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.IOException;
import java.net.InetAddress;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.SwingUtilities;
import javax.swing.UIManager;
import javax.swing.UnsupportedLookAndFeelException;
import javax.swing.border.EmptyBorder;

/**
 * Cliente para chat multicast com interface gráfica.
 * 
 * @author Igor Rozalem
 */
public class ChatClient extends JFrame implements MulticastManager.MessageListener {
    private static final Logger LOGGER = Logger.getLogger(ChatClient.class.getName());
    private static final long serialVersionUID = 1L;
    
    // Componentes da interface
    private JTextArea chatArea;
    private JTextField messageField;
    private JButton sendButton;
    private JLabel statusLabel;
    
    // Gerenciador de comunicação multicast
    private MulticastManager multicastManager;
    
    /**
     * Construtor padrão.
     * 
     * @param groupAddress Endereço do grupo multicast
     * @param port Porta do grupo multicast
     * @param username Nome do usuário
     */
    public ChatClient(InetAddress groupAddress, int port, String username) {
        // Configuração da janela
        super("Chat Multicast - " + username);
        setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);
        setSize(600, 500);
        setLocationRelativeTo(null);
        
        // Inicializar componentes da interface
        initializeInterface();
        
        // Configurar manipulador de fechamento de janela
        addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent e) {
                closeChat();
            }
        });
        
        try {
            // Inicializar gerenciador multicast
            multicastManager = new MulticastManager(groupAddress, port, username);
            multicastManager.setMessageListener(this);
            
            // Iniciar recebimento de mensagens
            multicastManager.startReceiving();
            
            // Atualizar status
            statusLabel.setText("Conectado ao grupo " + groupAddress.getHostAddress() + ":" + port);
            appendSystemMessage("Você entrou na sala de chat.");
            
        } catch (IOException e) {
            LOGGER.log(Level.SEVERE, "Erro ao inicializar gerenciador multicast", e);
            showError("Erro ao conectar ao grupo multicast: " + e.getMessage());
            dispose();
        }
    }
    
    /**
     * Inicializa os componentes da interface gráfica.
     */
    private void initializeInterface() {
        // Painel principal
        JPanel mainPanel = new JPanel(new BorderLayout(5, 5));
        mainPanel.setBorder(new EmptyBorder(10, 10, 10, 10));
        setContentPane(mainPanel);
        
        // Área de chat
        chatArea = new JTextArea();
        chatArea.setEditable(false);
        chatArea.setLineWrap(true);
        chatArea.setWrapStyleWord(true);
        chatArea.setFont(new Font("SansSerif", Font.PLAIN, 14));
        chatArea.setBackground(new Color(248, 248, 248));
        
        JScrollPane scrollPane = new JScrollPane(chatArea);
        scrollPane.setBorder(BorderFactory.createLineBorder(new Color(200, 200, 200)));
        mainPanel.add(scrollPane, BorderLayout.CENTER);
        
        // Painel de entrada
        JPanel inputPanel = new JPanel(new BorderLayout(5, 0));
        
        messageField = new JTextField();
        messageField.setFont(new Font("SansSerif", Font.PLAIN, 14));
        messageField.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                sendMessage();
            }
        });
        
        sendButton = new JButton("Enviar");
        sendButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                sendMessage();
            }
        });
        
        inputPanel.add(messageField, BorderLayout.CENTER);
        inputPanel.add(sendButton, BorderLayout.EAST);
        
        // Painel de status
        JPanel statusPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        statusLabel = new JLabel("Não conectado");
        statusLabel.setFont(new Font("SansSerif", Font.ITALIC, 12));
        statusPanel.add(statusLabel);
        
        // Painel inferior
        JPanel bottomPanel = new JPanel(new BorderLayout());
        bottomPanel.add(inputPanel, BorderLayout.CENTER);
        bottomPanel.add(statusPanel, BorderLayout.SOUTH);
        
        mainPanel.add(bottomPanel, BorderLayout.SOUTH);
        
        // Focar no campo de mensagem
        addWindowListener(new WindowAdapter() {
            @Override
            public void windowOpened(WindowEvent e) {
                messageField.requestFocus();
            }
        });
    }
    
    /**
     * Envia a mensagem atual para o grupo multicast.
     */
    private void sendMessage() {
        String message = messageField.getText().trim();
        
        if (message.isEmpty()) {
            return;
        }
        
        if (multicastManager != null && multicastManager.isRunning()) {
            try {
                // Enviar mensagem
                multicastManager.sendMessage(message);
                
                // Adicionar mensagem local (sem duplicação)
                appendMyMessage(message);
                
                // Limpar campo de texto
                messageField.setText("");
                
            } catch (IOException e) {
                LOGGER.log(Level.WARNING, "Erro ao enviar mensagem", e);
                appendSystemMessage("Erro ao enviar mensagem: " + e.getMessage());
            }
        } else {
            showError("Não conectado ao grupo multicast.");
        }
    }
    
    /**
     * Fecha o chat e libera recursos.
     */
    private void closeChat() {
        int option = JOptionPane.showConfirmDialog(
                this,
                "Deseja realmente sair do chat?",
                "Sair do Chat",
                JOptionPane.YES_NO_OPTION
        );
        
        if (option == JOptionPane.YES_OPTION) {
            if (multicastManager != null) {
                multicastManager.stop();
            }
            
            dispose();
            System.exit(0);
        }
    }
    
    /**
     * Adiciona uma mensagem de sistema à área de chat.
     * 
     * @param message Mensagem de sistema
     */
    private void appendSystemMessage(String message) {
        chatArea.append("[" + MulticastManager.getCurrentTimestamp() + "] " + 
                message + "\n");
        chatArea.setCaretPosition(chatArea.getDocument().getLength());
    }
    
    /**
     * Adiciona uma mensagem própria à área de chat.
     * 
     * @param message Mensagem enviada pelo usuário
     */
    private void appendMyMessage(String message) {
        chatArea.append("[" + MulticastManager.getCurrentTimestamp() + "] " + 
                "Você diz: " + message + "\n");
        chatArea.setCaretPosition(chatArea.getDocument().getLength());
    }
    
    /**
     * Adiciona uma mensagem recebida à área de chat.
     * 
     * @param sender Remetente da mensagem
     * @param message Mensagem recebida
     */
    private void appendReceivedMessage(String sender, String message) {
        chatArea.append("[" + MulticastManager.getCurrentTimestamp() + "] " + 
                message + "\n");
        chatArea.setCaretPosition(chatArea.getDocument().getLength());
    }
    
    /**
     * Exibe uma mensagem de erro.
     * 
     * @param message Mensagem de erro
     */
    private void showError(String message) {
        JOptionPane.showMessageDialog(
                this,
                message,
                "Erro",
                JOptionPane.ERROR_MESSAGE
        );
    }
    
    /**
     * Método chamado quando uma mensagem é recebida.
     * Implementação da interface MessageListener.
     * 
     * @param message Mensagem recebida
     * @param sender Remetente da mensagem
     */
    @Override
    public void onMessageReceived(String message, String sender) {
        SwingUtilities.invokeLater(() -> {
            appendReceivedMessage(sender, message);
        });
    }
    
    /**
     * Configura o Look and Feel da aplicação.
     */
    private static void configurarLookAndFeel() {
        try {
            // Tentar usar Nimbus Look and Feel
            for (UIManager.LookAndFeelInfo info : UIManager.getInstalledLookAndFeels()) {
                if ("Nimbus".equals(info.getName())) {
                    UIManager.setLookAndFeel(info.getClassName());
                    break;
                }
            }
        } catch (ClassNotFoundException | InstantiationException |
                 IllegalAccessException | UnsupportedLookAndFeelException ex) {
            LOGGER.log(Level.WARNING, "Falha ao configurar Look and Feel", ex);
        }
    }
    
    /**
     * Método principal para iniciar a aplicação.
     * 
     * @param args Argumentos da linha de comando
     */
    public static void main(String[] args) {
        // Verificar argumentos
        if (args.length != 2) {
            System.out.println("Uso: java ChatClient <endereco_multicast> <porta>");
            System.out.println("Exemplo: java ChatClient 224.0.0.1 9000");
            System.exit(1);
        }
        
        try {
            // Obter parâmetros
            final InetAddress groupAddress = InetAddress.getByName(args[0]);
            final int port = Integer.parseInt(args[1]);
            
            // Verificar se é um endereço multicast válido
            if (!groupAddress.isMulticastAddress()) {
                System.err.println("Endereço inválido: " + args[0]);
                System.err.println("O endereço deve estar no intervalo 224.0.0.0 a 239.255.255.255");
                System.exit(1);
            }
            
            // Solicitar nome de usuário
            final String username = JOptionPane.showInputDialog(
                    null,
                    "Digite seu nome de usuário:",
                    "Chat Multicast",
                    JOptionPane.QUESTION_MESSAGE
            );
            
            // Verificar nome de usuário
            if (username == null || username.trim().isEmpty()) {
                System.out.println("Nome de usuário é obrigatório.");
                System.exit(0);
            }
            
            // Configurar Look and Feel
            configurarLookAndFeel();
            
            // Iniciar interface
            SwingUtilities.invokeLater(() -> {
                new ChatClient(groupAddress, port, username.trim()).setVisible(true);
            });
            
        } catch (NumberFormatException e) {
            System.err.println("Porta inválida: " + args[1]);
            System.exit(1);
        } catch (IOException e) {
            System.err.println("Erro ao conectar: " + e.getMessage());
            System.exit(1);
        }
    }
}