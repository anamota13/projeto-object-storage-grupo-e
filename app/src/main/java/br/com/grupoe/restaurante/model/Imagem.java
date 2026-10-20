
package br.com.grupoe.restaurante.model;

public class Imagem {

    private int id;
    private int pratoId;
    private String objectName;
    private boolean principal;
    private String dataCadastro;

    public Imagem(int id, int pratoId, String objectName,
                  boolean principal, String dataCadastro) {
        this.id = id;
        this.pratoId = pratoId;
        this.objectName = objectName;
        this.principal = principal;
        this.dataCadastro = dataCadastro;
    }

    public int getId() {
        return id;
    }

    public int getPratoId() {
        return pratoId;
    }

    public String getObjectName() {
        return objectName;
    }

    public boolean isPrincipal() {
        return principal;
    }

    public String getDataCadastro() {
        return dataCadastro;
    }
}
