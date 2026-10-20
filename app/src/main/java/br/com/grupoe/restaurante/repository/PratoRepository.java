
package br.com.grupoe.restaurante.repository;

import br.com.grupoe.restaurante.database.Database;
import br.com.grupoe.restaurante.model.Prato;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class PratoRepository {

    public void cadastrar(Prato prato) {
        String sql = """
            INSERT INTO prato (nome, descricao, preco)
            VALUES (?, ?, ?)
            """;

        try (Connection conexao = Database.conectar();
             PreparedStatement stmt = conexao.prepareStatement(sql)) {

            stmt.setString(1, prato.getNome());
            stmt.setString(2, prato.getDescricao());
            stmt.setBigDecimal(3, prato.getPreco());

            stmt.executeUpdate();

            System.out.println("Prato cadastrado com sucesso!");

        } catch (SQLException e) {
            throw new RuntimeException("Erro ao cadastrar prato.", e);
        }
    }

    public List<Prato> listar() {
        List<Prato> pratos = new ArrayList<>();

        String sql = """
            SELECT id, nome, descricao, preco
            FROM prato
            ORDER BY id
            """;

        try (Connection conexao = Database.conectar();
             PreparedStatement stmt = conexao.prepareStatement(sql);
             ResultSet resultado = stmt.executeQuery()) {

            while (resultado.next()) {
                Prato prato = new Prato(
                    resultado.getInt("id"),
                    resultado.getString("nome"),
                    resultado.getString("descricao"),
                    resultado.getBigDecimal("preco")
                );

                pratos.add(prato);
            }

        } catch (SQLException e) {
            throw new RuntimeException("Erro ao listar pratos.", e);
        }

        return pratos;
    }

    public Prato buscarPorId(int id) {
        String sql = """
            SELECT id, nome, descricao, preco
            FROM prato
            WHERE id = ?
            """;

        try (Connection conexao = Database.conectar();
             PreparedStatement stmt = conexao.prepareStatement(sql)) {

            stmt.setInt(1, id);

            try (ResultSet resultado = stmt.executeQuery()) {
                if (resultado.next()) {
                    return new Prato(
                        resultado.getInt("id"),
                        resultado.getString("nome"),
                        resultado.getString("descricao"),
                        resultado.getBigDecimal("preco")
                    );
                }
            }

        } catch (SQLException e) {
            throw new RuntimeException("Erro ao buscar prato.", e);
        }

        return null;
    }

    // Atualizar os dados de um prato
    public void atualizar(Prato prato) {
        String sql = """
            UPDATE prato
            SET nome = ?, descricao = ?, preco = ?
            WHERE id = ?
            """;

        try (Connection conexao = Database.conectar();
             PreparedStatement stmt = conexao.prepareStatement(sql)) {

            stmt.setString(1, prato.getNome());
            stmt.setString(2, prato.getDescricao());
            stmt.setBigDecimal(3, prato.getPreco());
            stmt.setInt(4, prato.getId());

            int linhas = stmt.executeUpdate();

            if (linhas == 0) {
                System.out.println("Prato não encontrado.");
            } else {
                System.out.println("Prato atualizado com sucesso!");
            }

        } catch (SQLException e) {
            throw new RuntimeException("Erro ao atualizar prato.", e);
        }
    }

    // Excluir um prato pelo ID
    public void excluir(int id) {
        String sql = "DELETE FROM prato WHERE id = ?";

        try (Connection conexao = Database.conectar();
             PreparedStatement stmt = conexao.prepareStatement(sql)) {

            stmt.setInt(1, id);

            int linhas = stmt.executeUpdate();

            if (linhas == 0) {
                System.out.println("Prato não encontrado.");
            } else {
                System.out.println("Prato excluído com sucesso!");
            }

        } catch (SQLException e) {
            throw new RuntimeException("Erro ao excluir prato.", e);
        }
    }
}
