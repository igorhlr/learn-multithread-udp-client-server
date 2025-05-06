package local.redes;

import java.io.Serializable;
import java.util.Objects;

/**
 * Modelo que representa uma pessoa no sistema.
 * 
 * @author Igor Rozalem
 */
public class Pessoa implements Serializable {
    
    private static final long serialVersionUID = 1L;
    
    private String nome;
    private int idade;

    /**
     * Construtor padrão.
     */
    public Pessoa() {
        // Construtor vazio necessário para serialização
    }

    /**
     * Construtor com parâmetros.
     * 
     * @param nome Nome da pessoa
     * @param idade Idade da pessoa
     */
    public Pessoa(String nome, int idade) {
        this.nome = nome;
        this.idade = idade;
    }

    /**
     * Obtém o nome da pessoa.
     * 
     * @return O nome da pessoa
     */
    public String getNome() {
        return nome;
    }

    /**
     * Define o nome da pessoa.
     * 
     * @param nome O novo nome da pessoa
     */
    public void setNome(String nome) {
        this.nome = nome;
    }

    /**
     * Obtém a idade da pessoa.
     * 
     * @return A idade da pessoa
     */
    public int getIdade() {
        return idade;
    }

    /**
     * Define a idade da pessoa.
     * 
     * @param idade A nova idade da pessoa
     */
    public void setIdade(int idade) {
        this.idade = idade;
    }
    
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Pessoa pessoa = (Pessoa) o;
        return idade == pessoa.idade && 
               Objects.equals(nome, pessoa.nome);
    }

    @Override
    public int hashCode() {
        return Objects.hash(nome, idade);
    }

    @Override
    public String toString() {
        return "Pessoa{" +
                "nome='" + nome + '\'' +
                ", idade=" + idade +
                '}';
    }
}
