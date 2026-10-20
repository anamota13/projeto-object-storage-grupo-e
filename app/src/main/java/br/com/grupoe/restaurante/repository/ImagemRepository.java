
package br.com.grupoe.restaurante.repository;

import br.com.grupoe.restaurante.database.Database;
import br.com.grupoe.restaurante.model.Imagem;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class ImagemRepository {

    // Cadastrar uma imagem no banco de dados
    public void cadastrar(Imagem imagem) {
        String sql = """
            INSERT INTO imagem
                (prato_id, object_name, principal, data_cadastro)
            VALUES (?, ?, ?, ?)
            """;

        try (Connection conexao = Database.conectar();
             PreparedStatement stmt = conexao.prepareStatement(sql)) {

            stmt.setInt(1, imagem.getPratoId());
            stmt.setString(2, imagem.getObjectName());
            stmt.setInt(3, imagem.isPrincipal() ? 1 : 0);
            stmt.setString(4, imagem.getDataCadastro());

            stmt.executeUpdate();

            System.out.println("Imagem registrada no banco!");

        } catch (SQLException e) {
            throw new RuntimeException(
                "Erro ao registrar imagem no banco.", e);
        }
    }

    // Listar todas as imagens de um prato
    public List<Imagem> listarPorPrato(int pratoId) {
        List<Imagem> imagens = new ArrayList<>();

        String sql = """
            SELECT id, prato_id, object_name, principal, data_cadastro
            FROM imagem
            WHERE prato_id = ?
            ORDER BY principal DESC, id
            """;

        try (Connection conexao = Database.conectar();
             PreparedStatement stmt = conexao.prepareStatement(sql)) {

            stmt.setInt(1, pratoId);

            try (ResultSet resultado = stmt.executeQuery()) {
                while (resultado.next()) {
                    Imagem imagem = new Imagem(
                        resultado.getInt("id"),
                        resultado.getInt("prato_id"),
                        resultado.getString("object_name"),
                        resultado.getInt("principal") == 1,
                        resultado.getString("data_cadastro")
                    );

                    imagens.add(imagem);
                }
            }

        } catch (SQLException e) {
            throw new RuntimeException(
                "Erro ao listar imagens do prato.", e);
        }

        return imagens;
    }

    // Buscar uma imagem pelo ID
    public Imagem buscarPorId(int id) {
        String sql = """
            SELECT id, prato_id, object_name, principal, data_cadastro
            FROM imagem
            WHERE id = ?
            """;

        try (Connection conexao = Database.conectar();
             PreparedStatement stmt = conexao.prepareStatement(sql)) {

            stmt.setInt(1, id);

            try (ResultSet resultado = stmt.executeQuery()) {
                if (resultado.next()) {
                    return new Imagem(
                        resultado.getInt("id"),
                        resultado.getInt("prato_id"),
                        resultado.getString("object_name"),
                        resultado.getInt("principal") == 1,
                        resultado.getString("data_cadastro")
                    );
                }
            }

        } catch (SQLException e) {
            throw new RuntimeException(
                "Erro ao buscar imagem.", e);
        }

        return null;
    }

    // Excluir o registro de uma imagem do banco
    public void excluirPorId(int id) {
        String sql = "DELETE FROM imagem WHERE id = ?";

        try (Connection conexao = Database.conectar();
             PreparedStatement stmt = conexao.prepareStatement(sql)) {

            stmt.setInt(1, id);

            int linhas = stmt.executeUpdate();

            if (linhas == 0) {
                System.out.println("Imagem não encontrada.");
            } else {
                System.out.println(
                    "Registro da imagem excluído.");
            }

        } catch (SQLException e) {
            throw new RuntimeException(
                "Erro ao excluir registro da imagem.", e);
        }
    }

    // Definir uma imagem como principal para determinado prato
    public void definirPrincipal(int pratoId, int imagemId) {
        String verificar = """
            SELECT id
            FROM imagem
            WHERE id = ? AND prato_id = ?
            """;

        String desmarcar = """
            UPDATE imagem
            SET principal = 0
            WHERE prato_id = ?
            """;

        String marcar = """
            UPDATE imagem
            SET principal = 1
            WHERE id = ? AND prato_id = ?
            """;

        try (Connection conexao = Database.conectar()) {
            conexao.setAutoCommit(false);

            try {
                // Verifica se a imagem pertence ao prato
                try (PreparedStatement stmt =
                         conexao.prepareStatement(verificar)) {

                    stmt.setInt(1, imagemId);
                    stmt.setInt(2, pratoId);

                    try (ResultSet resultado = stmt.executeQuery()) {
                        if (!resultado.next()) {
                            throw new IllegalArgumentException(
                                "A imagem não pertence ao prato informado.");
                        }
                    }
                }

                // Remove a marcação principal das imagens anteriores
                try (PreparedStatement stmt =
                         conexao.prepareStatement(desmarcar)) {

                    stmt.setInt(1, pratoId);
                    stmt.executeUpdate();
                }

                // Marca a imagem selecionada como principal
                try (PreparedStatement stmt =
                         conexao.prepareStatement(marcar)) {

                    stmt.setInt(1, imagemId);
                    stmt.setInt(2, pratoId);

                    int linhas = stmt.executeUpdate();

                    if (linhas == 0) {
                        throw new SQLException(
                            "Não foi possível definir a imagem principal.");
                    }
                }

                conexao.commit();

                System.out.println(
                    "Imagem principal definida com sucesso!");

            } catch (Exception e) {
                try {
                    conexao.rollback();
                } catch (SQLException erroRollback) {
                    e.addSuppressed(erroRollback);
                }

                if (e instanceof RuntimeException) {
                    throw (RuntimeException) e;
                }

                throw new RuntimeException(
                    "Erro ao definir a imagem principal.", e);

            } finally {
                try {
                    conexao.setAutoCommit(true);
                } catch (SQLException e) {
                    System.err.println(
                        "Aviso: não foi possível restaurar o modo de transação.");
                }
            }

        } catch (SQLException e) {
            throw new RuntimeException(
                "Erro ao definir imagem principal.", e);
        }
    }
}
