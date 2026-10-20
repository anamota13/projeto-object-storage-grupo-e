
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

            System.out.println("Credenciais carregadas.");
            System.out.println("Tentando enviar a imagem para o bucket...");

            String nomeObjeto = storage.enviarImagem(
                caminhoImagem,
                caminhoImagem.getFileName().toString()
            );

            System.out.println("UPLOAD REALIZADO COM SUCESSO!");
            System.out.println("Nome do objeto: " + nomeObjeto);

            String url = storage.gerarUrlPublica(nomeObjeto);

            System.out.println("URL da imagem: " + url);

            System.out.println("Verificando se a imagem existe...");

            if (storage.imagemExiste(nomeObjeto)) {
                System.out.println(
                    "CONFIRMADO: imagem armazenada no bucket!"
                );
            } else {
                System.out.println(
                    "ATENÇÃO: não foi possível confirmar a imagem."
                );
            }

        } catch (Exception e) {
            System.out.println("ERRO durante o teste:");
            e.printStackTrace();
        }
    }
}
