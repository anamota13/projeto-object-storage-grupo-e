
package br.com.grupoe.restaurante;

import br.com.grupoe.restaurante.database.Database;
import br.com.grupoe.restaurante.model.Imagem;
import br.com.grupoe.restaurante.model.Prato;
import br.com.grupoe.restaurante.repository.PratoRepository;
import br.com.grupoe.restaurante.service.ImagemService;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

public class TesteIntegracaoImagem {

    public static void main(String[] args) {
        try {
            // Inicializa o banco de dados
            Database.inicializar();

            PratoRepository pratoRepository = new PratoRepository();
            List<Prato> pratos = pratoRepository.listar();

            // Garante que exista um prato para associar a imagem
            if (pratos.isEmpty()) {
                System.out.println(
                    "Nenhum prato cadastrado. Execute primeiro a classe App."
                );
                return;
            }

            Prato prato = pratos.get(0);

            Path caminhoImagem = Path.of(
                "C:/Users/jcmot/OneDrive/Área de Trabalho/pizza.png"
            );

            if (!Files.isRegularFile(caminhoImagem)) {
                System.out.println("ERRO: imagem de teste não encontrada.");
                return;
            }

            System.out.println("Prato selecionado: " + prato.getNome());
            System.out.println("ID do prato: " + prato.getId());

            ImagemService imagemService = new ImagemService();

            // Envia a imagem e registra os metadados no SQLite
            Imagem imagem = imagemService.enviarImagem(
                prato.getId(),
                caminhoImagem,
                true
            );

            System.out.println("\nIMAGEM CADASTRADA COM SUCESSO!");
            System.out.println("ID da imagem: " + imagem.getId());
            System.out.println("ID do prato: " + imagem.getPratoId());
            System.out.println("Objeto no bucket: " + imagem.getObjectName());
            System.out.println("Imagem principal: " + imagem.isPrincipal());
            System.out.println("Data de cadastro: " + imagem.getDataCadastro());
            System.out.println(
                "URL pública: " + imagemService.obterUrlPublica(imagem)
            );

            // Consulta novamente as imagens do prato no banco
            List<Imagem> imagens =
                imagemService.listarImagensDoPrato(prato.getId());

            System.out.println("\nImagens registradas para o prato: "
                + imagens.size());

            System.out.println(
                "TESTE DE INTEGRAÇÃO CONCLUÍDO!"
            );

        } catch (Exception e) {
            System.out.println("ERRO NO TESTE DE INTEGRAÇÃO:");
            e.printStackTrace();
        }
    }
}
