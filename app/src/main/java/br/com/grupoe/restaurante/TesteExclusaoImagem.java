
package br.com.grupoe.restaurante;

import br.com.grupoe.restaurante.database.Database;
import br.com.grupoe.restaurante.model.Imagem;
import br.com.grupoe.restaurante.model.Prato;
import br.com.grupoe.restaurante.repository.ImagemRepository;
import br.com.grupoe.restaurante.repository.PratoRepository;
import br.com.grupoe.restaurante.service.ImagemService;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

public class TesteExclusaoImagem {

    public static void main(String[] args) {
        try {
            Database.inicializar();

            Path caminhoImagem = Path.of(
                "C:/Users/jcmot/OneDrive/Área de Trabalho/pizza.png"
            );

            if (!Files.isRegularFile(caminhoImagem)) {
                System.out.println("ERRO: imagem de teste não encontrada.");
                return;
            }

            PratoRepository pratoRepository = new PratoRepository();
            List<Prato> pratos = pratoRepository.listar();

            if (pratos.isEmpty()) {
                System.out.println("ERRO: não há pratos cadastrados.");
                return;
            }

            int pratoId = pratos.get(0).getId();

            ImagemService imagemService = new ImagemService();
            ImagemRepository imagemRepository = new ImagemRepository();

            // Cadastra uma imagem temporária sem torná-la principal
            Imagem imagem = imagemService.enviarImagem(
                pratoId,
                caminhoImagem,
                false
            );

            int imagemId = imagem.getId();
            String nomeObjeto = imagem.getObjectName();

            System.out.println("Imagem temporária cadastrada.");
            System.out.println("ID da imagem: " + imagemId);
            System.out.println("Objeto no bucket: " + nomeObjeto);

            // Confirma que o objeto existe antes da exclusão
            if (!new br.com.grupoe.restaurante.service.GoogleCloudStorageService()
                    .imagemExiste(nomeObjeto)) {
                System.out.println("ERRO: objeto não encontrado no bucket.");
                return;
            }

            System.out.println("Objeto confirmado no bucket.");
            System.out.println("Excluindo imagem de teste...");

            imagemService.excluirImagem(imagemId);

            // Confirma que o registro foi removido do SQLite
            Imagem registroRestante = imagemRepository.buscarPorId(imagemId);

            // Confirma que o objeto foi removido do bucket
            boolean objetoRestante =
                new br.com.grupoe.restaurante.service.GoogleCloudStorageService()
                    .imagemExiste(nomeObjeto);

            if (registroRestante == null && !objetoRestante) {
                System.out.println(
                    "SUCESSO: imagem removida do bucket e do SQLite!"
                );
            } else {
                System.out.println(
                    "ATENÇÃO: a exclusão não foi confirmada nos dois locais."
                );
                System.out.println(
                    "Registro ainda existe no SQLite: "
                    + (registroRestante != null)
                );
                System.out.println(
                    "Objeto ainda existe no bucket: " + objetoRestante
                );
            }

        } catch (Exception e) {
            System.out.println("ERRO NO TESTE DE EXCLUSÃO:");
            e.printStackTrace();
        }
    }
}
