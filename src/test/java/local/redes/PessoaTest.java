package local.redes;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

import java.io.*;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Testes unitários para a classe Pessoa.
 */
public class PessoaTest {

    @Test
    @DisplayName("Deve criar uma pessoa com construtor padrão")
    public void testConstrutorPadrao() {
        Pessoa pessoa = new Pessoa();
        
        assertNull(pessoa.getNome());
        assertEquals(0, pessoa.getIdade());
    }
    
    @Test
    @DisplayName("Deve criar uma pessoa com construtor com parâmetros")
    public void testConstrutorParametrizado() {
        String nome = "João Silva";
        int idade = 30;
        
        Pessoa pessoa = new Pessoa(nome, idade);
        
        assertEquals(nome, pessoa.getNome());
        assertEquals(idade, pessoa.getIdade());
    }
    
    @Test
    @DisplayName("Deve alterar nome através do setter")
    public void testSetNome() {
        Pessoa pessoa = new Pessoa();
        String nome = "Maria Santos";
        
        pessoa.setNome(nome);
        
        assertEquals(nome, pessoa.getNome());
    }
    
    @Test
    @DisplayName("Deve alterar idade através do setter")
    public void testSetIdade() {
        Pessoa pessoa = new Pessoa();
        int idade = 25;
        
        pessoa.setIdade(idade);
        
        assertEquals(idade, pessoa.getIdade());
    }
    
    @Test
    @DisplayName("Deve verificar igualdade entre pessoas com mesmos valores")
    public void testEquals() {
        Pessoa pessoa1 = new Pessoa("Ana", 28);
        Pessoa pessoa2 = new Pessoa("Ana", 28);
        Pessoa pessoa3 = new Pessoa("Ana", 29);
        Pessoa pessoa4 = new Pessoa("Pedro", 28);
        
        assertTrue(pessoa1.equals(pessoa1)); // Reflexividade
        assertTrue(pessoa1.equals(pessoa2)); // Mesmos valores
        assertTrue(pessoa2.equals(pessoa1)); // Simetria
        assertFalse(pessoa1.equals(pessoa3)); // Idade diferente
        assertFalse(pessoa1.equals(pessoa4)); // Nome diferente
        assertFalse(pessoa1.equals(null)); // Null
        assertFalse(pessoa1.equals("Ana")); // Tipo diferente
    }
    
    @Test
    @DisplayName("Deve gerar código de hash consistente")
    public void testHashCode() {
        Pessoa pessoa1 = new Pessoa("Ana", 28);
        Pessoa pessoa2 = new Pessoa("Ana", 28);
        
        assertEquals(pessoa1.hashCode(), pessoa2.hashCode());
    }
    
    @Test
    @DisplayName("Deve serializar e deserializar uma pessoa corretamente")
    public void testSerializacaoDeserializacao() throws IOException, ClassNotFoundException {
        Pessoa original = new Pessoa("Lucas Oliveira", 42);
        
        // Serializar
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        try (ObjectOutputStream oos = new ObjectOutputStream(baos)) {
            oos.writeObject(original);
        }
        
        // Deserializar
        byte[] bytes = baos.toByteArray();
        ByteArrayInputStream bais = new ByteArrayInputStream(bytes);
        Pessoa deserializada;
        try (ObjectInputStream ois = new ObjectInputStream(bais)) {
            deserializada = (Pessoa) ois.readObject();
        }
        
        // Verificar
        assertEquals(original.getNome(), deserializada.getNome());
        assertEquals(original.getIdade(), deserializada.getIdade());
        assertEquals(original, deserializada);
    }
    
    @Test
    @DisplayName("Deve implementar toString corretamente")
    public void testToString() {
        Pessoa pessoa = new Pessoa("Carlos", 35);
        String esperado = "Pessoa{nome='Carlos', idade=35}";
        
        assertEquals(esperado, pessoa.toString());
    }
    
    @ParameterizedTest
    @CsvSource({
        "João, 20",
        "Maria, 30",
        "Pedro, 25",
        "Ana, 40"
    })
    @DisplayName("Deve criar pessoas com diversos valores usando parâmetros")
    public void testCriacaoPessoasParametrizadas(String nome, int idade) {
        Pessoa pessoa = new Pessoa(nome, idade);
        
        assertEquals(nome, pessoa.getNome());
        assertEquals(idade, pessoa.getIdade());
    }
}
