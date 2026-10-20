
package br.com.grupoe.restaurante.model;

import java.math.BigDecimal;

public class Prato {

    private int id;
    private String nome;
    private String descricao;
    private BigDecimal preco;

    public Prato(int id, String nome, String descricao, BigDecimal preco) {
        this.id = id;
        this.nome = nome;
        this.descricao = descricao;
        this.preco = preco;
    }

    public Prato(String nome, String descricao, BigDecimal preco) {
        this(0, nome, descricao, preco);
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getNome() {
        return nome;
    }

    public String getDescricao() {
        return descricao;
    }

    public BigDecimal getPreco() {
        return preco;
    }
}
