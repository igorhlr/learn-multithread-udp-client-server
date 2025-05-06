package local.redes;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectOutputStream;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.JOptionPane;
import javax.swing.SwingUtilities;
import javax.swing.Timer;
import javax.swing.UIManager;
import javax.swing.UnsupportedLookAndFeelException;

/**
 * Interface de cliente para envio de dados ao servidor.
 * 
 * @author Igor Rozalem
 */
public class Cliente extends javax.swing.JFrame {
    
    private static final Logger LOGGER = Logger.getLogger(Cliente.class.getName());
    private static final String SERVER_ADDRESS = "127.0.0.1";
    private static final int SERVER_PORT = 50000;
    private static final int BUFFER_SIZE = 1024;
    private static final int CLOSE_DELAY = 3000; // 3 segundos
    
    /**
     * Construtor da interface do cliente.
     */
    public Cliente() {
        initComponents();
        customizarInterface();
    }
    
    /**
     * Personaliza a interface após inicialização.
     */
    private void customizarInterface() {
        setTitle("Cliente UDP - Envio de Dados");
        setLocationRelativeTo(null); // Centraliza a janela na tela
        campoResposta.setLineWrap(true);
        campoResposta.setWrapStyleWord(true);
    }

    /**
     * Envia os dados do formulário para o servidor.
     */
    private void enviarDados() {
        try {
            // Desabilitar o botão para evitar múltiplos envios
            botaoEnviar.setEnabled(false);
            
            // Validar entradas do usuário
            String nome = campoNome.getText().trim();
            int idade;
            
            if (nome.isEmpty()) {
                exibirErro("Por favor, digite um nome válido!");
                return;
            }
            
            try {
                idade = Integer.parseInt(campoIdade.getText().trim());
                if (idade < 0 || idade > 150) {
                    exibirErro("Por favor, digite uma idade válida (entre 0 e 150)!");
                    return;
                }
            } catch (NumberFormatException e) {
                exibirErro("Por favor, digite uma idade válida!");
                return;
            }
            
            // Criar e enviar pacote UDP com os dados
            enviarPacoteUDP(nome, idade);
            
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Erro ao enviar dados", e);
            campoResposta.append("Erro: " + e.getMessage() + "\n");
            botaoEnviar.setEnabled(true);
        }
    }
    
    /**
     * Exibe uma mensagem de erro.
     * 
     * @param mensagem Mensagem de erro a ser exibida
     */
    private void exibirErro(String mensagem) {
        JOptionPane.showMessageDialog(
            this, 
            mensagem, 
            "Erro", 
            JOptionPane.ERROR_MESSAGE
        );
        botaoEnviar.setEnabled(true);
    }
    
    /**
     * Cria e envia um pacote UDP com os dados do cliente.
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
            campoResposta.append("Enviando dados para o servidor...\n");
            
            // Receber resposta
            receberResposta(socket);
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
     * Recebe a resposta do servidor.
     * 
     * @param socket Socket UDP para receber a resposta
     * @throws IOException Se ocorrer um erro de I/O
     */
    private void receberResposta(DatagramSocket socket) throws IOException {
        // Preparar buffer para receber resposta
        byte[] bufferResposta = new byte[BUFFER_SIZE];
        DatagramPacket pacoteResposta = new DatagramPacket(bufferResposta, bufferResposta.length);
        
        // Receber resposta (bloqueante até timeout)
        socket.receive(pacoteResposta);
        
        // Processar resposta
        String mensagemResposta = new String(pacoteResposta.getData(), 0, pacoteResposta.getLength());
        campoResposta.append("\n--- Resposta do Servidor ---\n" + mensagemResposta + "\n");
        campoResposta.append("\nAplicação será fechada em 3 segundos...\n");
        
        // Configurar timer para fechar a aplicação após 3 segundos
        iniciarTimerFechamento();
    }
    
    /**
     * Inicia um timer para fechar a aplicação após um delay.
     */
    private void iniciarTimerFechamento() {
        Timer timer = new Timer(CLOSE_DELAY, new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                dispose();
                System.exit(0);
            }
        });
        timer.setRepeats(false);
        timer.start();
    }

    /** 
     * Código gerado pelo NetBeans para inicializar os componentes da interface.
     * 
     * WARNING: NÃO modificar manualmente este método.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        jLabel1 = new javax.swing.JLabel();
        campoNome = new javax.swing.JTextField();
        jLabel2 = new javax.swing.JLabel();
        campoIdade = new javax.swing.JTextField();
        jLabel3 = new javax.swing.JLabel();
        jScrollPane1 = new javax.swing.JScrollPane();
        campoResposta = new javax.swing.JTextArea();
        botaoEnviar = new javax.swing.JButton();

        setDefaultCloseOperation(javax.swing.WindowConstants.EXIT_ON_CLOSE);

        jLabel1.setText("Nome");

        campoNome.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                campoNomeActionPerformed(evt);
            }
        });

        jLabel2.setText("Idade");

        jLabel3.setText("Resposta do Servidor");

        campoResposta.setEditable(false);
        campoResposta.setColumns(20);
        campoResposta.setRows(5);
        jScrollPane1.setViewportView(campoResposta);

        botaoEnviar.setText("Enviar");
        botaoEnviar.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                botaoEnviarActionPerformed(evt);
            }
        });

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(getContentPane());
        getContentPane().setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addGap(15, 15, 15)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, layout.createSequentialGroup()
                        .addGap(0, 0, Short.MAX_VALUE)
                        .addComponent(botaoEnviar)
                        .addGap(16, 16, 16))
                    .addGroup(layout.createSequentialGroup()
                        .addGap(12, 12, 12)
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(jScrollPane1, javax.swing.GroupLayout.DEFAULT_SIZE, 351, Short.MAX_VALUE)
                            .addGroup(layout.createSequentialGroup()
                                .addComponent(jLabel3)
                                .addGap(0, 0, Short.MAX_VALUE)))
                        .addGap(15, 15, 15))))
            .addGroup(layout.createSequentialGroup()
                .addGap(23, 23, 23)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(layout.createSequentialGroup()
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(jLabel1)
                            .addGroup(layout.createSequentialGroup()
                                .addGap(6, 6, 6)
                                .addComponent(jLabel2)))
                        .addGap(0, 0, Short.MAX_VALUE))
                    .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, layout.createSequentialGroup()
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                            .addComponent(campoNome, javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(campoIdade))
                        .addGap(15, 15, 15))))
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addGap(15, 15, 15)
                .addComponent(jLabel1)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(campoNome)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jLabel2)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(campoIdade)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jLabel3)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jScrollPane1, javax.swing.GroupLayout.DEFAULT_SIZE, 164, Short.MAX_VALUE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(botaoEnviar, javax.swing.GroupLayout.PREFERRED_SIZE, 19, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(15, 15, 15))
        );

        pack();
    }// </editor-fold>//GEN-END:initComponents

    private void campoNomeActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_campoNomeActionPerformed
        // Ação não necessária aqui
    }//GEN-LAST:event_campoNomeActionPerformed

    private void botaoEnviarActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_botaoEnviarActionPerformed
        enviarDados();
    }//GEN-LAST:event_botaoEnviarActionPerformed

    /**
     * Método principal para iniciar a aplicação cliente.
     *
     * @param args Argumentos da linha de comando (não utilizados)
     */
    public static void main(String args[]) {
        // Configurar o Look and Feel
        configurarLookAndFeel();

        // Iniciar a interface gráfica no EDT (Event Dispatch Thread)
        SwingUtilities.invokeLater(new Runnable() {
            @Override
            public void run() {
                new Cliente().setVisible(true);
            }
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

    // Renomeação de variáveis para maior clareza
    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton botaoEnviar;
    private javax.swing.JTextField campoIdade;
    private javax.swing.JTextField campoNome;
    private javax.swing.JTextArea campoResposta;
    private javax.swing.JLabel jLabel1;
    private javax.swing.JLabel jLabel2;
    private javax.swing.JLabel jLabel3;
    private javax.swing.JScrollPane jScrollPane1;
    // End of variables declaration//GEN-END:variables
}
