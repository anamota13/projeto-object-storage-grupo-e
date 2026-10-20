
package br.com.grupoe.restaurante;

import br.com.grupoe.restaurante.database.Database;
import br.com.grupoe.restaurante.model.Prato;
import br.com.grupoe.restaurante.repository.PratoRepository;

import java.math.BigDecimal;

public class App {

    public static void main(String[] args) {
        Database.inicializar();

        PratoRepository repositorio = new PratoRepository();

        if (repositorio.listar().isEmpty()) {
            repositorio.cadastrar(new Prato(
                "Pizza de Calabresa",
                "Pizza com calabresa, queijo e molho de tomate.",
                new BigDecimal("45.90")
            ));
        }

        System.out.println("\n=== CARDÁPIO ===");

        for (Prato prato : repositorio.listar()) {
            System.out.println("ID: " + prato.getId());
            System.out.println("Nome: " + prato.getNome());
            System.out.println("Descrição: " + prato.getDescricao());
            System.out.println("Preço: R$ " + prato.getPreco());
            System.out.println("-------------------------");
        }
    }
}
