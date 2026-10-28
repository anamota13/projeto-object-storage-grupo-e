
package br.com.grupoe.restaurante;

import br.com.grupoe.restaurante.database.Database;
import br.com.grupoe.restaurante.model.Imagem;
import br.com.grupoe.restaurante.model.Prato;
import br.com.grupoe.restaurante.repository.ImagemRepository;
import br.com.grupoe.restaurante.repository.PratoRepository;
import br.com.grupoe.restaurante.service.GoogleCloudStorageService;
import br.com.grupoe.restaurante.service.ImagemService;

import java.math.BigDecimal;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

public class PopularBanco {

    private static final String[][] DADOS = {
        {"Pizza de Calabresa", "Pizza com calabresa, queijo e molho de tomate.", "45.90"},
        {"Hambúrguer Artesanal", "Hambúrguer com carne artesanal, queijo, alface e tomate.", "38.90"},
        {"Lasanha à Bolonhesa", "Lasanha com molho bolonhesa e queijo gratinado.", "42.90"},
        {"Strogonoff de Frango", "Frango ao molho cremoso com arroz e batata palha.", "34.90"},
        {"Parmegiana de Frango", "Filé de frango empanado com molho, queijo e acompanhamentos.", "39.90"},
        {"Feijoada Completa", "Feijoada tradicional com arroz, couve e farofa.", "49.90"},
        {"Espaguete à Carbonara", "Massa com molho de ovos, queijo e bacon.", "41.90"},
        {"Filé com Fritas", "Filé grelhado acompanhado de batatas fritas.", "54.90"},
        {"Risoto de Camarão", "Risoto cremoso preparado com camarões e temperos.", "59.90"},
        {"Salmão Grelhado", "Salmão grelhado acompanhado de legumes.", "62.90"}
    };

    public static void main(String[] args) {

        try {
            
            for (int i = 1; i <= DADOS.length; i++) {
                String pasta = String.format("prato-%02d", i);

                for (int j = 1; j <= 3; j++) {
                    Path arquivo = Path.of(
                        "imagens", pasta, j + ".jpg"
                    );

                    if (!Files.isRegularFile(arquivo)) {
                        throw new IllegalStateException(
                            "Imagem nao encontrada: " + arquivo
                        );
                    }
                }
            }

           System.out.println("Iniciando preparacao dos uploads...");
            Database.inicializar();

            PratoRepository pratoRepo = new PratoRepository();
            ImagemRepository imagemRepo = new ImagemRepository();
            ImagemService imagemService = new ImagemService();

            System.out.println("Iniciando cadastro dos pratos...");

            for (int i = 0; i < DADOS.length; i++) {

                String nome = DADOS[i][0];
                String descricao = DADOS[i][1];
                BigDecimal preco = new BigDecimal(DADOS[i][2]);

            
                Prato prato = pratoRepo.listar()
                    .stream()
                    .filter(p -> p.getNome().equalsIgnoreCase(nome))
                    .findFirst()
                    .orElse(null);

                if (prato == null) {
                    pratoRepo.cadastrar(
                        new Prato(nome, descricao, preco)
                    );

                    prato = pratoRepo.listar()
                        .stream()
                        .filter(p -> p.getNome().equalsIgnoreCase(nome))
                        .findFirst()
                        .orElseThrow();

                    System.out.println("Prato cadastrado: " + nome);
                } else {
                    System.out.println("Prato existente: " + nome);
                }

                int pratoId = prato.getId();

                List<Imagem> imagensExistentes =
                    imagemRepo.listarPorPrato(pratoId);

                int quantidade = imagensExistentes.size();

                if (quantidade > 3) {
                    System.out.println(
                        "Aviso: prato ja possui mais de 3 imagens."
                    );
                    continue;
                }

                for (int j = quantidade + 1; j <= 3; j++) {

                    String pasta = String.format(
                        "prato-%02d", i + 1
                    );

                    Path arquivo = Path.of(
                        "imagens", pasta, j + ".jpg"
                    );

                    boolean principal = (j == 1);

                    imagemService.enviarImagem(
                        pratoId, arquivo, principal
                    );

                    System.out.println(
                        "Imagem " + j + " enviada: " + nome
                    );
                }
                List<Imagem> imagens =
                    imagemRepo.listarPorPrato(pratoId);

                boolean temPrincipal = imagens.stream()
                    .anyMatch(Imagem::isPrincipal);

                if (!temPrincipal && !imagens.isEmpty()) {
                    imagemRepo.definirPrincipal(
                        pratoId, imagens.get(0).getId()
                    );
                }

                System.out.println(
                    "Finalizado: " + nome + " | Imagens: "
                    + imagemRepo.listarPorPrato(pratoId).size()
                );
            }

            System.out.println();
            System.out.println("POPULACAO FINALIZADA!");

            for (Prato prato : pratoRepo.listar()) {
                int totalImagens =
                    imagemRepo.listarPorPrato(prato.getId()).size();

                System.out.println(
                    prato.getNome() + ": "
                    + totalImagens + " imagens"
                );
            }

        } catch (Exception e) {
            System.err.println("ERRO NA POPULACAO:");
            e.printStackTrace();
        }
    }
}
