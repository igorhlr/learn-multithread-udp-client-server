package local.redes.multicast;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

/**
 * Provedor de tempo para o sistema.
 * Esta classe abstrai a obtenção de tempo, permitindo sua substituição em testes.
 */
public class TimeProvider {
    
    private static final DateTimeFormatter FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
    private static TimeProvider instance = new TimeProvider();
    
    // Variável para armazenar um tempo fixo em testes
    private LocalDateTime fixedTime;
    
    /**
     * Construtor protegido para permitir criação em testes.
     */
    protected TimeProvider() {
        this.fixedTime = null;
    }
    
    /**
     * Obtém a instância do provedor de tempo.
     * 
     * @return Instância do TimeProvider
     */
    public static TimeProvider getInstance() {
        return instance;
    }
    
    /**
     * Define uma instância específica do TimeProvider para testes.
     * 
     * @param provider Provedor de tempo a ser usado
     */
    public static void setInstance(TimeProvider provider) {
        instance = provider;
    }
    
    /**
     * Cria uma instância com tempo fixo para testes.
     * 
     * @param fixedTime Tempo fixo a ser usado
     * @return Nova instância do TimeProvider com tempo fixo
     */
    public static TimeProvider createWithFixedTime(LocalDateTime fixedTime) {
        TimeProvider provider = new TimeProvider();
        provider.setFixedTime(fixedTime);
        return provider;
    }
    
    /**
     * Define um tempo fixo para testes.
     * 
     * @param time Tempo a ser retornado em chamadas subsequentes
     */
    public void setFixedTime(LocalDateTime time) {
        this.fixedTime = time;
    }
    
    /**
     * Obtém o timestamp atual como string formatada.
     * 
     * @return String com timestamp atual ou fixo
     */
    public String getCurrentTimestamp() {
        LocalDateTime time = (fixedTime != null) ? fixedTime : LocalDateTime.now();
        return time.format(FORMATTER);
    }
    
    /**
     * Método estático para facilitar o uso.
     * 
     * @return String com timestamp atual ou fixo
     */
    public static String getTimestamp() {
        return getInstance().getCurrentTimestamp();
    }
    
    /**
     * Reseta o provedor para o comportamento padrão.
     */
    public static void reset() {
        instance = new TimeProvider();
    }
}
