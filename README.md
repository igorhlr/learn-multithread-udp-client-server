# Projeto Cliente-Servidor Multithreaded com UDP

Este projeto demonstra a implementação de uma aplicação cliente-servidor utilizando o protocolo UDP e threads em Java. O servidor recebe objetos `Pessoa` de múltiplos clientes simultaneamente, processando cada requisição em uma thread separada.

## Requisitos

- Java JDK 8 ou superior (testado com JDK 1.8.0_40)
- Apache Maven 3.6.0 ou superior
- NetBeans IDE (opcional, mas recomendado para execução fácil)

## Estrutura do Projeto

```
src/main/java/local/redes/
├── Cliente.java      # Cliente com interface gráfica Swing
├── Cliente.form      # Arquivo de layout da interface gráfica
├── Pessoa.java       # Objeto serializado transferido entre cliente e servidor
└── Servidor.java     # Servidor multithreaded com classe interna para processamento
```

## Funcionalidades

### Servidor
- Execução em modo console (terminal)
- Escuta na porta 50000 via UDP (DatagramSocket)
- Permanece em estado bloqueante aguardando conexões
- Cria uma nova thread para cada cliente conectado
- Exibe informações sobre os clientes e dados recebidos
- Envia confirmação de recebimento para o cliente

### Cliente
- Interface gráfica Swing centralizada na tela
- Campos para inserção de nome e idade
- Envio do objeto Pessoa serializado para o servidor
- Recebimento e exibição da confirmação do servidor
- Fechamento automático 3 segundos após receber resposta

## Como Executar

### Compilando o Projeto
```bash
cd /caminho/para/redes-atividade4
mvn clean compile
```

### Executando o Servidor
```bash
mvn exec:java -Dexec.mainClass="local.redes.Servidor"
```

### Executando o Cliente
Em um novo terminal:
```bash
mvn exec:java -Dexec.mainClass="local.redes.Cliente"
```

Você pode executar vários clientes simultaneamente para testar o processamento paralelo.

### Usando o NetBeans
1. Abra o projeto no NetBeans
2. Clique com o botão direito no projeto e selecione "Clean and Build"
3. Clique com o botão direito em `Servidor.java` e selecione "Run File"
4. Clique com o botão direito em `Cliente.java` e selecione "Run File" (pode repetir este passo várias vezes)

## Funcionamento Técnico

### Comunicação UDP
O projeto utiliza o protocolo UDP para comunicação, oferecendo:
- Comunicação leve sem estabelecimento de conexão persistente
- Serialização de objetos Java para transmissão pela rede
- O servidor usa a porta fixa 50000, enquanto os clientes usam portas efêmeras

### Modelo de Threading
O servidor implementa um modelo de concorrência onde:
- A thread principal permanece bloqueada aguardando novas conexões
- Cada requisição é processada em uma thread separada
- A classe interna `TratadorRequisicao` implementa `Runnable` para processamento paralelo
- As threads compartilham o mesmo socket UDP, mas processam dados independentemente

### Interface Gráfica
O cliente utiliza Java Swing para:
- Apresentar uma interface gráfica centralizada
- Validar entrada de dados (idade deve ser numérica)
- Exibir respostas do servidor
- Fechar automaticamente após completar a tarefa

## Problemas Comuns

- **Porta já em uso**: Se encontrar erro "Address already in use", execute `lsof -i :50000` e `kill -9 [PID]`
- **Firewall bloqueando**: Certifique-se que a porta 50000 UDP não está bloqueada pelo firewall
- **Execução do cliente antes do servidor**: Certifique-se de iniciar o servidor antes dos clientes

## Licença

Este projeto é de código aberto e pode ser utilizado para fins educacionais e de aprendizado.

## Autor

Igor Rozalem
