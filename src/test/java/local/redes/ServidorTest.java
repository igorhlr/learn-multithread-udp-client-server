package local.redes;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.io.ByteArrayOutputStream;
import java.io.ObjectOutputStream;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.lang.reflect.Field;
import java.lang.reflect.Method;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

/**
 * Testes unitários para a classe Servidor.
 * Usa reflexão para acessar métodos privados para teste.
 */
@ExtendWith(MockitoExtension.class)
public class ServidorTest {

    @Mock
    private DatagramSocket mockSocket;

    /**
     * Teste da classe interna TratadorRequisicao do Servidor.
     */
    @Test
    @DisplayName("Deve processar uma requisição corretamente")
    public void testProcessarRequisicao() throws Exception {
        // Criar objeto para serializar
        Pessoa pessoa = new Pessoa("Teste Unitário", 25);
        
        // Serializar a pessoa
        ByteArrayOutputStream byteStream = new ByteArrayOutputStream();
        ObjectOutputStream objectStream = new ObjectOutputStream(byteStream);
        objectStream.writeObject(pessoa);
        objectStream.flush();
        byte[] dadosSerializados = byteStream.toByteArray();
        
        // Criar endereço de teste
        InetAddress enderecoCliente = InetAddress.getLocalHost();
        int portaCliente = 12345;
        
        // Criar pacote simulando recebimento do cliente
        DatagramPacket pacoteRecebido = new DatagramPacket(
                dadosSerializados, 
                dadosSerializados.length,
                enderecoCliente,
                portaCliente
        );
        
        // Capturador para verificar o pacote enviado como resposta
        ArgumentCaptor<DatagramPacket> pacoteCaptor = ArgumentCaptor.forClass(DatagramPacket.class);
        
        // Instanciar a classe interna TratadorRequisicao por reflexão
        Class<?> servidorClass = Servidor.class;
        Class<?>[] innerClasses = servidorClass.getDeclaredClasses();
        Class<?> tratadorClass = null;
        
        for (Class<?> innerClass : innerClasses) {
            if (innerClass.getSimpleName().equals("TratadorRequisicao")) {
                tratadorClass = innerClass;
                break;
            }
        }
        
        assertNotNull(tratadorClass, "A classe TratadorRequisicao deve existir");
        
        // Construtor da classe interna
        Object tratador = tratadorClass.getDeclaredConstructor(
                DatagramPacket.class, DatagramSocket.class)
                .newInstance(pacoteRecebido, mockSocket);
        
        // Acessar o método "processarRequisicao" por reflexão
        Method processarRequisicao = tratadorClass.getDeclaredMethod("processarRequisicao");
        processarRequisicao.setAccessible(true);
        
        // Executar o método
        processarRequisicao.invoke(tratador);
        
        // Verificar que o socket.send foi chamado
        verify(mockSocket).send(pacoteCaptor.capture());
        
        // Verificar o pacote enviado
        DatagramPacket pacoteEnviado = pacoteCaptor.getValue();
        assertNotNull(pacoteEnviado);
        assertEquals(enderecoCliente, pacoteEnviado.getAddress());
        assertEquals(portaCliente, pacoteEnviado.getPort());
        
        // Verificar o conteúdo da resposta
        String resposta = new String(pacoteEnviado.getData(), 0, pacoteEnviado.getLength());
        assertTrue(resposta.contains("Olá Teste Unitário"));
        assertTrue(resposta.contains("seus dados foram recebidos com sucesso"));
        assertTrue(resposta.contains("Você tem 25 anos"));
        assertTrue(resposta.contains("Timestamp:"));
    }
    
    @Test
    @DisplayName("Deve montar resposta correta para o cliente")
    public void testMontarResposta() throws Exception {
        // Criar objeto Pessoa para teste
        Pessoa pessoa = new Pessoa("Cliente Teste", 30);
        
        // Instanciar a classe interna TratadorRequisicao por reflexão
        Class<?> servidorClass = Servidor.class;
        Class<?>[] innerClasses = servidorClass.getDeclaredClasses();
        Class<?> tratadorClass = null;
        
        for (Class<?> innerClass : innerClasses) {
            if (innerClass.getSimpleName().equals("TratadorRequisicao")) {
                tratadorClass = innerClass;
                break;
            }
        }
        
        assertNotNull(tratadorClass, "A classe TratadorRequisicao deve existir");
        
        // Criar pacote simulado para construir o objeto
        DatagramPacket pacoteMock = mock(DatagramPacket.class);
        
        // Construtor da classe interna
        Object tratador = tratadorClass.getDeclaredConstructor(
                DatagramPacket.class, DatagramSocket.class)
                .newInstance(pacoteMock, mockSocket);
        
        // Acessar o método "montarResposta" por reflexão
        Method montarResposta = tratadorClass.getDeclaredMethod("montarResposta", Pessoa.class);
        montarResposta.setAccessible(true);
        
        // Executar o método
        String resposta = (String) montarResposta.invoke(tratador, pessoa);
        
        // Verificar o conteúdo da resposta
        assertNotNull(resposta);
        assertTrue(resposta.contains("Olá Cliente Teste"));
        assertTrue(resposta.contains("seus dados foram recebidos com sucesso"));
        assertTrue(resposta.contains("Você tem 30 anos"));
        assertTrue(resposta.contains("Timestamp:"));
    }
}
