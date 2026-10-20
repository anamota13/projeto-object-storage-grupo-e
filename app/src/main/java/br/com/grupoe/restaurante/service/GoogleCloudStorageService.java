
package br.com.grupoe.restaurante.service;

import com.google.auth.oauth2.GoogleCredentials;
import com.google.cloud.storage.Blob;
import com.google.cloud.storage.BlobId;
import com.google.cloud.storage.BlobInfo;
import com.google.cloud.storage.Bucket;
import com.google.cloud.storage.Storage;
import com.google.cloud.storage.StorageOptions;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.UUID;

public class GoogleCloudStorageService {

    private static final String BUCKET_NAME = "fatec-2026-grupo-e";

    private final Storage storage;

    public GoogleCloudStorageService() {
        String caminhoCredenciais =
                "config/sa-grupo-e-key.json";

        try (InputStream credenciais = Files.newInputStream(
                Path.of(caminhoCredenciais))) {

            GoogleCredentials googleCredentials =
                    GoogleCredentials.fromStream(credenciais);

            this.storage = StorageOptions.newBuilder()
                    .setCredentials(googleCredentials)
                    .build()
                    .getService();

        } catch (IOException e) {
            throw new RuntimeException(
                    "Erro ao carregar as credenciais do Google Cloud.", e);
        }
    }

    // Testa se o bucket existe e pode ser acessado.
    public boolean testarConexao() {
        try {
            Bucket bucket = storage.get(BUCKET_NAME);
            return bucket != null;

        } catch (Exception e) {
            throw new RuntimeException(
                    "Não foi possível acessar o bucket do Google Cloud Storage.",
                    e);
        }
    }

    // Envia uma imagem para o Google Cloud Storage.
    public String enviarImagem(Path caminhoArquivo, String nomeOriginal) {
        try {
            if (!Files.isRegularFile(caminhoArquivo)) {
                throw new IllegalArgumentException(
                        "O arquivo de imagem não existe: " + caminhoArquivo);
            }

            String extensao = obterExtensao(nomeOriginal);
            String nomeObjeto =
                    "pratos/" + UUID.randomUUID() + extensao;

            String tipoConteudo = Files.probeContentType(caminhoArquivo);

            if (tipoConteudo == null) {
                tipoConteudo = "application/octet-stream";
            }

            BlobInfo blobInfo = BlobInfo.newBuilder(
                    BlobId.of(BUCKET_NAME, nomeObjeto))
                    .setContentType(tipoConteudo)
                    .build();

            storage.create(
                    blobInfo,
                    Files.readAllBytes(caminhoArquivo));

            return nomeObjeto;

        } catch (IOException e) {
            throw new RuntimeException(
                    "Erro ao enviar imagem para o Google Cloud Storage.", e);
        }
    }

    // Monta a URL pública da imagem.
    public String gerarUrlPublica(String nomeObjeto) {
        return "https://storage.googleapis.com/"
                + BUCKET_NAME + "/" + nomeObjeto;
    }

    // Exclui uma imagem do bucket.
    public boolean excluirImagem(String nomeObjeto) {
        return storage.delete(BlobId.of(BUCKET_NAME, nomeObjeto));
    }

    // Verifica se uma imagem existe no bucket.
    public boolean imagemExiste(String nomeObjeto) {
        Blob blob = storage.get(BlobId.of(BUCKET_NAME, nomeObjeto));
        return blob != null;
    }

    private String obterExtensao(String nomeArquivo) {
        int indice = nomeArquivo.lastIndexOf('.');

        if (indice < 0) {
            return "";
        }

        return nomeArquivo.substring(indice).toLowerCase();
    }
}
