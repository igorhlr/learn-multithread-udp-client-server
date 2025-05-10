# Documentação de Implementação de Testes

Este documento descreve a estratégia e implementação dos testes unitários e de integração para o projeto de comunicação UDP Cliente-Servidor com chat multicast.

## Visão Geral

A implementação de testes segue princípios de testes unitários e de integração, com foco em:

1. **Isolamento de componentes**: Testes que verificam o comportamento de classes individuais
2. **Integração de componentes**: Testes que verificam a comunicação entre componentes
3. **Cobertura de código**: Cobrindo o máximo possível de código e cenários de uso
4. **Testes parametrizados**: Para validar vários cenários com uma única implementação de teste
5. **Mocks e simulação**: Para isolar componentes e testar comportamentos específicos

## Ferramentas e Frameworks

- **JUnit 5**: Framework principal de testes
- **Mockito**: Para criação de mocks e simulação de comportamentos
- **Maven Surefire**: Para execução de testes no processo de build

## Estrutura de Testes

### 1. Testes da Classe `Pessoa`

**Arquivo**: `PessoaTest.java`

Testes implementados:
- Construtor padrão
- Construtor com parâmetros
- Getters e setters
- Serialização e deserialização
- Métodos `equals` e `hashCode`
- Método `toString`
- Testes parametrizados para diversos valores

### 2. Testes do Servidor UDP

**Arquivo**: `ServidorTest.java`

Testes implementados:
- Processamento de requisições
- Montagem de resposta para o cliente
- Manipulação correta de objetos Pessoa recebidos

Técnicas utilizadas:
- Reflexão para acesso a métodos privados
- Mockito para simular envio de pacotes
- ArgumentCaptor para verificar pacotes enviados

### 3. Testes do Cliente UDP

**Incorporados nos testes de integração**

Os testes do cliente estão principalmente focados em sua interação com o servidor, implementados nos testes de integração.

### 4. Testes do MulticastManager

**Arquivo**: `MulticastManagerTest.java`

Testes implementados:
- Criação do gerenciador multicast
- Iniciar e parar recebimento de mensagens
- Definição de listener de mensagens
- Formatação de timestamp
- Envio e recebimento de mensagens (teste de integração)

### 5. Testes do ChatClient

**Arquivo**: `ChatClientTest.java`

Testes implementados:
- Inicialização de componentes da interface
- Envio de mensagens
- Tratamento de mensagens vazias
- Exibição de mensagens de sistema
- Exibição de mensagens do próprio usuário
- Processamento de mensagens recebidas

Técnicas utilizadas:
- Reflexão para acesso a componentes e métodos privados
- Mock do MulticastManager para testar isoladamente a interface

### 6. Testes de Integração

**Arquivo**: `IntegrationTest.java`

Testes implementados:
- Comunicação completa entre cliente e servidor
- Envio de objetos Pessoa serializados
- Recebimento e validação de respostas

Técnicas utilizadas:
- Threads separadas para simular servidor e cliente
- CountDownLatch para sincronização entre threads
- Timeout para evitar bloqueios indefinidos

### 7. Utilitários para Testes

**Arquivo**: `NetworkTestUtils.java`

Funcionalidades implementadas:
- Envio e recebimento de objetos serializados
- Serialização e deserialização de objetos
- Servidor UDP mock para testes
- Utilitários para espera com timeout
- Execução de tarefas com timeout controlado

## Estratégias de Teste

### Mocking de Recursos Externos

Utilizamos o Mockito para simular componentes externos, como sockets e interfaces de rede, permitindo testes mais previsíveis e isolados.

### Reflexão para Acesso a Membros Privados

Utilizamos reflexão para acessar métodos e campos privados durante os testes, permitindo testar componentes internos sem expor a API publicamente.

### Threads e Sincronização

Para testes de comunicação entre componentes, utilizamos threads separadas com mecanismos de sincronização (CountDownLatch) para garantir a ordem correta de execução e evitar condições de corrida.

### Parametrização

Utilizamos testes parametrizados para testar múltiplos cenários sem duplicação de código.

## Execução dos Testes

Os testes podem ser executados através do Maven:

```bash
mvn test
```

## Considerações sobre Testes de Rede

Os testes que envolvem comunicação de rede real (especialmente multicast) podem apresentar comportamentos diferentes dependendo da configuração de rede do ambiente. É importante considerar:

1. **Suporte a Multicast**: Alguns ambientes podem não suportar tráfego multicast.
2. **Firewall**: Restrições de firewall podem bloquear tráfego UDP.
3. **Portas disponíveis**: Os testes utilizam portas diferentes das usadas pela aplicação, mas ainda podem haver conflitos.

## Melhorias Futuras

1. **Cobertura de Código**: Implementar relatórios de cobertura com JaCoCo.
2. **Testes de Performance**: Adicionar testes para avaliar o desempenho sob carga.
3. **Testes de Falha**: Melhorar os testes para cenários de falha, como perda de pacotes e timeouts.
4. **Testes de UI Automatizados**: Implementar testes automatizados da interface gráfica.
