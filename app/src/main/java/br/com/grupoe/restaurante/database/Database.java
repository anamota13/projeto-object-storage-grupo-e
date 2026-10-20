
package br.com.grupoe.restaurante.database;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;

public class Database {

    private static final String URL = "jdbc:sqlite:restaurante.db";

    public static Connection conectar() throws SQLException {
        Connection conexao = DriverManager.getConnection(URL);

        try (Statement stmt = conexao.createStatement()) {
            stmt.execute("PRAGMA foreign_keys = ON");
        }

        return conexao;
    }

    public static void inicializar() {
        String sqlPrato = """
            CREATE TABLE IF NOT EXISTS prato (
                id INTEGER PRIMARY KEY AUTOINCREMENT,
                nome TEXT NOT NULL,
                descricao TEXT NOT NULL,
                preco NUMERIC NOT NULL
            )
            """;

        String sqlImagem = """
            CREATE TABLE IF NOT EXISTS imagem (
                id INTEGER PRIMARY KEY AUTOINCREMENT,
                prato_id INTEGER NOT NULL,
                object_name TEXT NOT NULL,
                principal INTEGER NOT NULL DEFAULT 0,
                data_cadastro TEXT NOT NULL,
                FOREIGN KEY (prato_id) REFERENCES prato(id)
                    ON DELETE CASCADE
            )
            """;

        try (Connection conexao = conectar();
             Statement stmt = conexao.createStatement()) {

            stmt.execute(sqlPrato);
            stmt.execute(sqlImagem);

            System.out.println("Banco de dados inicializado!");

        } catch (SQLException e) {
            throw new RuntimeException(
                "Erro ao inicializar o banco de dados.", e
            );
        }
    }
}
