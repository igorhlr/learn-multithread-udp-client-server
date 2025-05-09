package local.redes.multicast;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;
import java.time.Month;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Testes unitários para a classe TimeProvider.
 */
public class TimeProviderTest {
    
    private TimeProvider originalProvider;
    
    @BeforeEach
    public void setUp() {
        // Salvar a instância original
        originalProvider = TimeProvider.getInstance();
    }
    
    @AfterEach
    public void tearDown() {
        // Restaurar o comportamento original
        TimeProvider.reset();
    }
    
    @Test
    @DisplayName("Deve retornar tempo atual no formato correto")
    public void testGetTimestampNormal() {
        String timestamp = TimeProvider.getTimestamp();
        
        assertNotNull(timestamp);
        assertTrue(timestamp.matches("\\d{4}-\\d{2}-\\d{2} \\d{2}:\\d{2}:\\d{2}"));
    }
    
    @Test
    @DisplayName("Deve retornar tempo fixo quando configurado")
    public void testGetTimestampFixedTime() {
        // Definir um tempo fixo específico
        LocalDateTime fixedTime = LocalDateTime.of(2025, Month.MAY, 15, 10, 30, 45);
        
        // Criar uma nova instância com tempo fixo usando o método factory
        TimeProvider mockProvider = TimeProvider.createWithFixedTime(fixedTime);
        
        // Substituir a instância global
        TimeProvider.setInstance(mockProvider);
        
        // Verificar que o timestamp retornado é o esperado
        String expectedTimestamp = "2025-05-15 10:30:45";
        String actualTimestamp = TimeProvider.getTimestamp();
        
        assertEquals(expectedTimestamp, actualTimestamp);
        
        // Verificar que o MulticastManager usa o mesmo tempo fixo
        assertEquals(expectedTimestamp, MulticastManager.getCurrentTimestamp());
    }
    
    @Test
    @DisplayName("Deve redefinir para comportamento normal após reset")
    public void testResetTimeProvider() {
        // Configurar tempo fixo
        LocalDateTime fixedTime = LocalDateTime.of(2000, 1, 1, 0, 0, 0);
        TimeProvider mockProvider = TimeProvider.createWithFixedTime(fixedTime);
        TimeProvider.setInstance(mockProvider);
        
        // Verificar que o tempo fixo está funcionando
        assertEquals("2000-01-01 00:00:00", TimeProvider.getTimestamp());
        
        // Resetar
        TimeProvider.reset();
        
        // Verificar que voltou ao comportamento normal
        String timestamp = TimeProvider.getTimestamp();
        assertNotNull(timestamp);
        assertNotEquals("2000-01-01 00:00:00", timestamp);
        assertTrue(timestamp.matches("\\d{4}-\\d{2}-\\d{2} \\d{2}:\\d{2}:\\d{2}"));
    }
}
