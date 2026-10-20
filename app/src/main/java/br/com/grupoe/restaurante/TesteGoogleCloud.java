
package br.com.grupoe.restaurante;

import br.com.grupoe.restaurante.service.GoogleCloudStorageService;

import java.nio.file.Files;
import java.nio.file.Path;

public class TesteGoogleCloud {

    public static void main(String[] args) {
        try {
            Path caminhoImagem = Path.of(
                "C:/Users/jcmot/OneDrive/Área de Trabalho/pizza.png"
            );

            if (!Files.isRegularFile(caminhoImagem)) {
                System.out.println("ERRO: imagem não encontrada!");
                System.out.println(caminhoImagem);
                return;
            }

            GoogleCloudStorageService storage =
                new GoogleCloudStorageService();

            System.out.println("Testando conexão com o bucket...");

            if (!storage.testarConexao()) {
                System.out.println("ERRO: bucket não encontrado.");
                return;
            }

            System.out.println("Conexão estabelecida!");

            System.out.println("Enviando imagem...");

            String nomeObjeto = storage.enviarImagem(
                caminhoImagem,
                caminhoImagem.getFileName().toString()
            );

            System.out.println("UPLOAD REALIZADO COM SUCESSO!");
            System.out.println("Nome do objeto: " + nomeObjeto);

            String url = storage.gerarUrlPublica(nomeObjeto);

            System.out.println("URL da imagem: " + url);

            if (storage.imagemExiste(nomeObjeto)) {
                System.out.println("CONFIRMADO: imagem armazenada no bucket!");
            } else {
                System.out.println("ATENÇÃO: não foi possível confirmar a imagem.");
            }

        } catch (Exception e) {
            System.out.println("ERRO durante o teste:");
            e.printStackTrace();
        }
    }
}
