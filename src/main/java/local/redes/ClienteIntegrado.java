package local.redes;

import java.awt.BorderLayout;
import java.awt.CardLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectOutputStream;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.BorderFactory;
import javax.swing.ButtonGroup;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.SwingUtilities;
import javax.swing.UIManager;
import javax.swing.UnsupportedLookAndFeelException;
import javax.swing.border.EmptyBorder;
import javax.swing.border.TitledBorder;

import local.redes.multicast.MulticastManager;

/**
 * Cliente integrado que suporta tanto comunicação UDP ponto-a-ponto
 * quanto comunicação multicast.
 * 
 * @author Igor Rozalem
 */
public class ClienteIntegrado extends JFrame {
    private static final Logger LOGGER = Logger.getLogger(ClienteIntegrado.class.getName());
    private static final long serialVersionUID = 1L;
    
    // Constantes para configuração da rede
    private static final String SERVER_ADDRESS = "127.0.0.1";
    private static final int SERVER_PORT = 50000;
    private static final int BUFFER_SIZE = 1024;
    private static final String DEFAULT_MULTICAST_ADDRESS = "224.0.0.1";
    private static final int DEFAULT_MULTICAST_PORT = 9000;
    
    // Constantes para layout
    private static final String CARD_HOME = "HOME";
    private static final String CARD_PESSOA = "PESSOA";
    private static final String CARD_CHAT = "CHAT";
    
    // Componentes da interface
    private CardLayout cardLayout;
    private JPanel cardPanel;
    
    // Componentes da tela inicial
    private JTextField nomeUsuarioField;
    
    // Componentes da tela de Pessoa
    private JTextField campoNome;
    private JTextField campoIdade;
    private JTextArea campoRespostaPessoa;
    
    // Componentes da tela de Chat
    private JTextArea chatArea;
    private JTextField messageField;
    private JLabel statusLabel;
    
    // Gerenciadores de comunicação
    private String nomeUsuario;
    private MulticastManager multicastManager;
    
    /**
     * Construtor do cliente integrado.
     */
    public ClienteIntegrado() {
        super("Sistema de Comunicação UDP");
        setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);
        setSize(600, 500);
        setLocationRelativeTo(null);
        
        // Configurar manipulador de fechamento de janela
        addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent e) {
                fecharAplicacao();
            }
        });
        
        // Inicializar componentes da interface
        initializeInterface();
    }
    
    /**
     * Inicializa os componentes da interface gráfica.
     */
    private void initializeInterface() {
        // Painel principal com CardLayout
        cardLayout = new CardLayout();
        cardPanel = new JPanel(cardLayout);
        setContentPane(cardPanel);
        
        // Criar painéis para cada "cartão"
        cardPanel.add(createHomePanel(), CARD_HOME);
        cardPanel.add(createPessoaPanel(), CARD_PESSOA);
        cardPanel.add(createChatPanel(), CARD_CHAT);
        
        // Mostrar painel inicial
        cardLayout.show(cardPanel, CARD_HOME);
    }
    
    /**
     * Cria o painel inicial para seleção de modo.
     * 
     * @return JPanel com a interface inicial
     */
    private JPanel createHomePanel() {
        JPanel panel = new JPanel(new GridBagLayout());
        panel.setBorder(new EmptyBorder(20, 20, 20, 20));
        
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(5, 5, 5, 5);
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.weightx = 1.0;
        
        // Título
        JLabel titleLabel = new JLabel("Sistema de Comunicação UDP");
        titleLabel.setFont(new Font("SansSerif", Font.BOLD, 18));
        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.gridwidth = 2;
        panel.add(titleLabel, gbc);
        
        // Subtítulo
        JLabel subtitleLabel = new JLabel("Escolha seu nome de usuário e modo de operação");
        subtitleLabel.setFont(new Font("SansSerif", Font.ITALIC, 14));
        gbc.gridy = 1;
        gbc.insets = new Insets(5, 5, 15, 5);
        panel.add(subtitleLabel, gbc);
        
        // Nome do usuário
        JLabel userLabel = new JLabel("Nome de usuário:");
        gbc.gridwidth = 1;
        gbc.gridy = 2;
        gbc.insets = new Insets(5, 5, 5, 5);
        panel.add(userLabel, gbc);
        
        nomeUsuarioField = new JTextField(20);
        gbc.gridx = 1;
        panel.add(nomeUsuarioField, gbc);
        
        // Painel para modos
        JPanel modesPanel = new JPanel(new GridBagLayout());
        modesPanel.setBorder(BorderFactory.createTitledBorder(
                BorderFactory.createLineBorder(Color.GRAY),
                "Modos de Operação",
                TitledBorder.CENTER,
                TitledBorder.TOP
        ));
        
        GridBagConstraints gbcModes = new GridBagConstraints();
        gbcModes.insets = new Insets(10, 10, 10, 10);
        gbcModes.fill = GridBagConstraints.HORIZONTAL;
        gbcModes.weightx = 1.0;
        gbcModes.gridwidth = 2;
        
        // Opção de Pessoa
        JButton pessoaButton = new JButton("Enviar Pessoa (Ponto-a-Ponto)");
        pessoaButton.setToolTipText("Envia um objeto Pessoa para o servidor via UDP");
        gbcModes.gridx = 0;
        gbcModes.gridy = 0;
        pessoaButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                iniciarModoPessoa();
            }
        });
        modesPanel.add(pessoaButton, gbcModes);
        
        // Opção de Chat
        JButton chatButton = new JButton("Entrar no Chat (Multicast)");
        chatButton.setToolTipText("Participa de um chat em grupo usando multicast UDP");
        gbcModes.gridy = 1;
        chatButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                iniciarModoChat();
            }
        });
        modesPanel.add(chatButton, gbcModes);
        
        // Adicionar painel de modos
        gbc.gridx = 0;
        gbc.gridy = 3;
        gbc.gridwidth = 2;
        gbc.insets = new Insets(15, 5, 5, 5);
        panel.add(modesPanel, gbc);
        
        return panel;
    }
    
    /**
     * Cria o painel para envio de objeto Pessoa.
     * 
     * @return JPanel com interface para envio de Pessoa
     */
    private JPanel createPessoaPanel() {
        JPanel panel = new JPanel(new BorderLayout(10, 10));
        panel.setBorder(new EmptyBorder(20, 20, 20, 20));
        
        // Painel de formulário
        JPanel formPanel = new JPanel(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(5, 5, 5, 5);
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.weightx = 1.0;
        
        // Título
        JLabel titleLabel = new JLabel("Envio de Pessoa (UDP Ponto-a-Ponto)");
        titleLabel.setFont(new Font("SansSerif", Font.BOLD, 16));
        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.gridwidth = 2;
        formPanel.add(titleLabel, gbc);
        
        // Campo Nome
        JLabel nomeLabel = new JLabel("Nome:");
        gbc.gridwidth = 1;
        gbc.gridy = 1;
        formPanel.add(nomeLabel, gbc);
        
        campoNome = new JTextField(20);
        gbc.gridx = 1;
        formPanel.add(campoNome, gbc);
        
        // Campo Idade
        JLabel idadeLabel = new JLabel("Idade:");
        gbc.gridx = 0;
        gbc.gridy = 2;
        formPanel.add(idadeLabel, gbc);
        
        campoIdade = new JTextField(20);
        gbc.gridx = 1;
        formPanel.add(campoIdade, gbc);
        
        // Botões
        JPanel buttonPanel = new JPanel(new GridBagLayout());
        GridBagConstraints gbcBtn = new GridBagConstraints();
        gbcBtn.insets = new Insets(5, 5, 5, 5);
        
        JButton enviarButton = new JButton("Enviar");
        gbcBtn.gridx = 0;
        gbcBtn.gridy = 0;
        enviarButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                enviarPessoa();
            }
        });
        buttonPanel.add(enviarButton, gbcBtn);
        
        JButton voltarButtonPessoa = new JButton("Voltar");
        gbcBtn.gridx = 1;
        voltarButtonPessoa.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                cardLayout.show(cardPanel, CARD_HOME);
            }
        });
        buttonPanel.add(voltarButtonPessoa, gbcBtn);
        
        gbc.gridx = 0;
        gbc.gridy = 3;
        gbc.gridwidth = 2;
        formPanel.add(buttonPanel, gbc);
        
        // Adicionar painel de formulário
        panel.add(formPanel, BorderLayout.NORTH);
        
        // Área de resposta
        JLabel respostaLabel = new JLabel("Resposta do Servidor:");
        panel.add(respostaLabel, BorderLayout.CENTER);
        
        campoRespostaPessoa = new JTextArea();
        campoRespostaPessoa.setEditable(false);
        campoRespostaPessoa.setLineWrap(true);
        campoRespostaPessoa.setWrapStyleWord(true);
        
        JScrollPane scrollPane = new JScrollPane(campoRespostaPessoa);
        scrollPane.setPreferredSize(new Dimension(400, 200));
        panel.add(scrollPane, BorderLayout.SOUTH);
        
        return panel;
    }
    
    /**
     * Cria o painel para chat multicast.
     * 
     * @return JPanel com interface para chat
     */
    private JPanel createChatPanel() {
        JPanel panel = new JPanel(new BorderLayout(5, 5));
        panel.setBorder(new EmptyBorder(10, 10, 10, 10));
        
        // Título
        JLabel titleLabel = new JLabel("Chat Multicast UDP");
        titleLabel.setFont(new Font("SansSerif", Font.BOLD, 16));
        panel.add(titleLabel, BorderLayout.NORTH);
        
        // Área de chat
        chatArea = new JTextArea();
        chatArea.setEditable(false);
        chatArea.setLineWrap(true);
        chatArea.setWrapStyleWord(true);
        chatArea.setFont(new Font("SansSerif", Font.PLAIN, 14));
        chatArea.setBackground(new Color(248, 248, 248));
        
        JScrollPane scrollPane = new JScrollPane(chatArea);
        scrollPane.setBorder(BorderFactory.createLineBorder(new Color(200, 200, 200)));
        panel.add(scrollPane, BorderLayout.CENTER);
        
        // Painel inferior
        JPanel bottomPanel = new JPanel(new BorderLayout(5, 5));
        
        // Painel de entrada
        JPanel inputPanel = new JPanel(new BorderLayout(5, 0));
        
        messageField = new JTextField();
        messageField.setFont(new Font("SansSerif", Font.PLAIN, 14));
        messageField.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                enviarMensagemChat();
            }
        });
        
        JButton sendButton = new JButton("Enviar");
        sendButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                enviarMensagemChat();
            }
        });
        
        inputPanel.add(messageField, BorderLayout.CENTER);
        inputPanel.add(sendButton, BorderLayout.EAST);
        
        // Painel de status
        JPanel statusPanel = new JPanel(new BorderLayout(5, 0));
        
        statusLabel = new JLabel("Não conectado");
        statusLabel.setFont(new Font("SansSerif", Font.ITALIC, 12));
        
        JButton voltarButtonChat = new JButton("Voltar");
        voltarButtonChat.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                sairDoChat();
            }
        });
        
        statusPanel.add(statusLabel, BorderLayout.CENTER);
        statusPanel.add(voltarButtonChat, BorderLayout.EAST);
        
        // Montar painel inferior
        bottomPanel.add(inputPanel, BorderLayout.CENTER);
        bottomPanel.add(statusPanel, BorderLayout.SOUTH);
        
        panel.add(bottomPanel, BorderLayout.SOUTH);
        
        return panel;
    }
    
    /**
     * Inicia o modo de envio de Pessoa.
     */
    private void iniciarModoPessoa() {
        nomeUsuario = nomeUsuarioField.getText().trim();
        
        if (nomeUsuario.isEmpty()) {
            JOptionPane.showMessageDialog(
                    this,
                    "Por favor, digite um nome de usuário.",
                    "Nome Obrigatório",
                    JOptionPane.WARNING_MESSAGE
            );
            return;
        }
        
        // Preencher campo nome com nome do usuário
        campoNome.setText(nomeUsuario);
        
        // Limpar campo de resposta
        campoRespostaPessoa.setText("");
        
        // Mostrar painel de Pessoa
        cardLayout.show(cardPanel, CARD_PESSOA);
    }
    
    /**
     * Inicia o modo de chat multicast.
     */
    private void iniciarModoChat() {
        nomeUsuario = nomeUsuarioField.getText().trim();
        
        if (nomeUsuario.isEmpty()) {
            JOptionPane.showMessageDialog(
                    this,
                    "Por favor, digite um nome de usuário.",
                    "Nome Obrigatório",
                    JOptionPane.WARNING_MESSAGE
            );
            return;
        }
        
        try {
            // Solicitar configuração de multicast
            String multicastAddress = JOptionPane.showInputDialog(
                    this,
                    "Endereço do grupo multicast:",
                    DEFAULT_MULTICAST_ADDRESS
            );
            
            if (multicastAddress == null) {
                return; // Cancelado pelo usuário
            }
            
            String portStr = JOptionPane.showInputDialog(
                    this,
                    "Porta do grupo multicast:",
                    String.valueOf(DEFAULT_MULTICAST_PORT)
            );
            
            if (portStr == null) {
                return; // Cancelado pelo usuário
            }
            
            int port = Integer.parseInt(portStr);
            InetAddress groupAddress = InetAddress.getByName(multicastAddress);
            
            // Verificar se é um endereço multicast válido
            if (!groupAddress.isMulticastAddress()) {
                JOptionPane.showMessageDialog(
                        this,
                        "Endereço inválido: " + multicastAddress + "\n" +
                        "O endereço deve estar no intervalo 224.0.0.0 a 239.255.255.255",
                        "Endereço Inválido",
                        JOptionPane.ERROR_MESSAGE
                );
                return;
            }
            
            // Inicializar gerenciador multicast
            multicastManager = new MulticastManager(groupAddress, port, nomeUsuario);
            multicastManager.setMessageListener(new MulticastManager.MessageListener() {
                @Override
                public void onMessageReceived(String message, String sender) {
                    SwingUtilities.invokeLater(() -> {
                        adicionarMensagemChat(message);
                    });
                }
            });
            
            // Iniciar recebimento de mensagens
            multicastManager.startReceiving();
            
            // Limpar área de chat
            chatArea.setText("");
            
            // Atualizar status
            statusLabel.setText("Conectado ao grupo " + groupAddress.getHostAddress() + ":" + port);
            adicionarMensagemSistema("Você entrou na sala de chat.");
            
            // Mostrar painel de chat
            cardLayout.show(cardPanel, CARD_CHAT);
            
            // Focar no campo de mensagem
            messageField.requestFocus();
            
        } catch (NumberFormatException e) {
            JOptionPane.showMessageDialog(
                    this,
                    "Porta inválida. Por favor, digite um número.",
                    "Erro",
                    JOptionPane.ERROR_MESSAGE
            );
        } catch (UnknownHostException e) {
            JOptionPane.showMessageDialog(
                    this,
                    "Endereço inválido: " + e.getMessage(),
                    "Erro",
                    JOptionPane.ERROR_MESSAGE
            );
        } catch (IOException e) {
            LOGGER.log(Level.SEVERE, "Erro ao inicializar multicast", e);
            JOptionPane.showMessageDialog(
                    this,
                    "Erro ao conectar ao grupo multicast: " + e.getMessage(),
                    "Erro",
                    JOptionPane.ERROR_MESSAGE
            );
        }
    }
    
    /**
     * Envia um objeto Pessoa para o servidor.
     */
    private void enviarPessoa() {
        try {
            // Validar entradas
            String nome = campoNome.getText().trim();
            String idadeStr = campoIdade.getText().trim();
            
            if (nome.isEmpty()) {
                JOptionPane.showMessageDialog(
                        this,
                        "Por favor, digite um nome válido!",
                        "Erro",
                        JOptionPane.ERROR_MESSAGE
                );
                return;
            }
            
            int idade;
            try {
                idade = Integer.parseInt(idadeStr);
                if (idade < 0 || idade > 150) {
                    JOptionPane.showMessageDialog(
                            this,
                            "Por favor, digite uma idade válida (entre 0 e 150)!",
                            "Erro",
                            JOptionPane.ERROR_MESSAGE
                    );
                    return;
                }
            } catch (NumberFormatException e) {
                JOptionPane.showMessageDialog(
                        this,
                        "Por favor, digite uma idade válida!",
                        "Erro",
                        JOptionPane.ERROR_MESSAGE
                );
                return;
            }
            
            // Criar e enviar pacote UDP
            campoRespostaPessoa.append("Enviando dados para o servidor...\n");
            enviarPacoteUDP(nome, idade);
            
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Erro ao enviar dados", e);
            campoRespostaPessoa.append("Erro: " + e.getMessage() + "\n");
        }
    }
    
    /**
     * Cria e envia um pacote UDP com os dados da pessoa.
     * 
     * @param nome Nome da pessoa
     * @param idade Idade da pessoa
     * @throws IOException Se ocorrer um erro de I/O
     */
    private void enviarPacoteUDP(String nome, int idade) throws IOException {
        try (DatagramSocket socket = new DatagramSocket()) {
            // Configurar timeout para o socket
            socket.setSoTimeout(5000); // 5 segundos de timeout
            
            // Criar objeto Pessoa
            Pessoa pessoa = new Pessoa(nome, idade);
            
            // Serializar o objeto Pessoa
            byte[] dados = serializarObjeto(pessoa);
            
            // Configurar endereço do servidor
            InetAddress endereco = InetAddress.getByName(SERVER_ADDRESS);
            
            // Criar pacote para envio
            DatagramPacket pacoteEnvio = new DatagramPacket(
                dados, 
                dados.length, 
                endereco, 
                SERVER_PORT
            );
            
            // Enviar pacote
            socket.send(pacoteEnvio);
            
            // Receber resposta
            receberRespostaPessoa(socket);
        }
    }
    
    /**
     * Serializa um objeto para um array de bytes.
     * 
     * @param objeto Objeto a ser serializado
     * @return Array de bytes do objeto serializado
     * @throws IOException Se ocorrer um erro de I/O
     */
    private byte[] serializarObjeto(Object objeto) throws IOException {
        try (ByteArrayOutputStream byteStream = new ByteArrayOutputStream();
             ObjectOutputStream objectStream = new ObjectOutputStream(byteStream)) {
            objectStream.writeObject(objeto);
            objectStream.flush();
            return byteStream.toByteArray();
        }
    }
    
    /**
     * Recebe a resposta do servidor após envio de Pessoa.
     * 
     * @param socket Socket UDP para receber a resposta
     * @throws IOException Se ocorrer um erro de I/O
     */
    private void receberRespostaPessoa(DatagramSocket socket) throws IOException {
        // Preparar buffer para receber resposta
        byte[] bufferResposta = new byte[BUFFER_SIZE];
        DatagramPacket pacoteResposta = new DatagramPacket(bufferResposta, bufferResposta.length);
        
        // Receber resposta (bloqueante até timeout)
        socket.receive(pacoteResposta);
        
        // Processar resposta
        String mensagemResposta = new String(pacoteResposta.getData(), 0, pacoteResposta.getLength());
        campoRespostaPessoa.append("\n--- Resposta do Servidor ---\n" + mensagemResposta + "\n");
    }
    
    /**
     * Envia uma mensagem para o chat multicast.
     */
    private void enviarMensagemChat() {
        String message = messageField.getText().trim();
        
        if (message.isEmpty()) {
            return;
        }
        
        if (multicastManager != null && multicastManager.isRunning()) {
            try {
                // Enviar mensagem
                multicastManager.sendMessage(message);
                
                // Adicionar mensagem local (sem duplicação no listener)
                adicionarMensagemPropria(message);
                
                // Limpar campo de texto
                messageField.setText("");
                
            } catch (IOException e) {
                LOGGER.log(Level.WARNING, "Erro ao enviar mensagem", e);
                adicionarMensagemSistema("Erro ao enviar mensagem: " + e.getMessage());
            }
        } else {
            JOptionPane.showMessageDialog(
                    this,
                    "Não conectado ao grupo multicast.",
                    "Erro",
                    JOptionPane.ERROR_MESSAGE
            );
        }
    }
    
    /**
     * Adiciona uma mensagem de sistema à área de chat.
     * 
     * @param message Mensagem de sistema
     */
    private void adicionarMensagemSistema(String message) {
        chatArea.append("[" + MulticastManager.getCurrentTimestamp() + "] " + 
                message + "\n");
        chatArea.setCaretPosition(chatArea.getDocument().getLength());
    }
    
    /**
     * Adiciona uma mensagem própria à área de chat.
     * 
     * @param message Mensagem enviada pelo usuário
     */
    private void adicionarMensagemPropria(String message) {
        chatArea.append("[" + MulticastManager.getCurrentTimestamp() + "] " + 
                "Você diz: " + message + "\n");
        chatArea.setCaretPosition(chatArea.getDocument().getLength());
    }
    
    /**
     * Adiciona uma mensagem recebida à área de chat.
     * 
     * @param message Mensagem recebida
     */
    private void adicionarMensagemChat(String message) {
        chatArea.append("[" + MulticastManager.getCurrentTimestamp() + "] " + 
                message + "\n");
        chatArea.setCaretPosition(chatArea.getDocument().getLength());
    }
    
    /**
     * Sai do chat e retorna à tela inicial.
     */
    private void sairDoChat() {
        if (multicastManager != null) {
            multicastManager.stop();
            multicastManager = null;
        }
        
        cardLayout.show(cardPanel, CARD_HOME);
    }
    
    /**
     * Fecha a aplicação após confirmar com o usuário.
     */
    private void fecharAplicacao() {
        int option = JOptionPane.showConfirmDialog(
                this,
                "Deseja realmente sair da aplicação?",
                "Sair",
                JOptionPane.YES_NO_OPTION
        );
        
        if (option == JOptionPane.YES_OPTION) {
            // Fechar recursos
            if (multicastManager != null) {
                multicastManager.stop();
            }
            
            dispose();
            System.exit(0);
        }
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
     * @param args Argumentos da linha de comando (não utilizados)
     */
    public static void main(String[] args) {
        // Configurar Look and Feel
        configurarLookAndFeel();
        
        // Iniciar interface
        SwingUtilities.invokeLater(() -> {
            new ClienteIntegrado().setVisible(true);
        });
    }
}