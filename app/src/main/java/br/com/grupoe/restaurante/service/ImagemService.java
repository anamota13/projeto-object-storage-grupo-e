
package br.com.grupoe.restaurante.service;

import br.com.grupoe.restaurante.model.Imagem;
import br.com.grupoe.restaurante.repository.ImagemRepository;
import br.com.grupoe.restaurante.repository.PratoRepository;

import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDateTime;
import java.util.List;

public class ImagemService {

    private final GoogleCloudStorageService storageService;
    private final ImagemRepository imagemRepository;
    private final PratoRepository pratoRepository;

    public ImagemService() {
        this.storageService = new GoogleCloudStorageService();
        this.imagemRepository = new ImagemRepository();
        this.pratoRepository = new PratoRepository();
    }

    // Enviar imagem para o bucket e registrar seus metadados
    public Imagem enviarImagem(
            int pratoId,
            Path caminhoArquivo,
            boolean principal) {

        if (pratoRepository.buscarPorId(pratoId) == null) {
            throw new IllegalArgumentException(
                "O prato informado não existe.");
        }

        if (!Files.isRegularFile(caminhoArquivo)) {
            throw new IllegalArgumentException(
                "O arquivo informado não existe.");
        }

        String nomeArquivo = caminhoArquivo.getFileName().toString();

        // Envia o arquivo para o Google Cloud Storage
        String nomeObjeto = storageService.enviarImagem(
            caminhoArquivo,
            nomeArquivo
        );

        try {
            // Registra os metadados no SQLite
            Imagem imagem = new Imagem(
                0,
                pratoId,
                nomeObjeto,
                false,
                LocalDateTime.now().toString()
            );

            imagemRepository.cadastrar(imagem);

            // Define a imagem como principal, se solicitado
            if (principal) {
                List<Imagem> imagens =
                    imagemRepository.listarPorPrato(pratoId);

                Imagem novaImagem = imagens.stream()
                    .filter(item ->
                        item.getObjectName().equals(nomeObjeto))
                    .findFirst()
                    .orElseThrow(() ->
                        new RuntimeException(
                            "Imagem cadastrada, mas não localizada no banco."));

                imagemRepository.definirPrincipal(
                    pratoId,
                    novaImagem.getId()
                );

                // Retorna o registro atualizado do banco
                return imagemRepository.buscarPorId(
                    novaImagem.getId()
                );
            }

            return imagemRepository.buscarPorId(
                imagensId(nomeObjeto, pratoId)
            );

        } catch (RuntimeException e) {
            // Se o banco falhar, tenta remover o arquivo enviado
            try {
                storageService.excluirImagem(nomeObjeto);
            } catch (RuntimeException erroExclusao) {
                e.addSuppressed(erroExclusao);
            }

            throw e;
        }
    }

    // Localizar o ID do registro da imagem recém-cadastrada
    private int imagensId(String nomeObjeto, int pratoId) {
        return imagemRepository.listarPorPrato(pratoId)
            .stream()
            .filter(imagem ->
                imagem.getObjectName().equals(nomeObjeto))
            .mapToInt(Imagem::getId)
            .findFirst()
            .orElseThrow(() ->
                new RuntimeException(
                    "Imagem cadastrada, mas não localizada no banco."));
    }

    // Listar as imagens associadas a um prato
    public List<Imagem> listarImagensDoPrato(int pratoId) {
        if (pratoRepository.buscarPorId(pratoId) == null) {
            throw new IllegalArgumentException(
                "O prato informado não existe.");
        }

        return imagemRepository.listarPorPrato(pratoId);
    }

    // Gerar a URL pública da imagem
    public String obterUrlPublica(Imagem imagem) {
        return storageService.gerarUrlPublica(
            imagem.getObjectName()
        );
    }

    // Definir uma imagem existente como principal
    public void definirImagemPrincipal(int pratoId, int imagemId) {
        if (pratoRepository.buscarPorId(pratoId) == null) {
            throw new IllegalArgumentException(
                "O prato informado não existe.");
        }

        imagemRepository.definirPrincipal(pratoId, imagemId);
    }

    // Excluir o arquivo do bucket e seu registro no SQLite
    public void excluirImagem(int imagemId) {
        Imagem imagem = imagemRepository.buscarPorId(imagemId);

        if (imagem == null) {
            throw new IllegalArgumentException(
                "Imagem não encontrada.");
        }

        boolean excluida = storageService.excluirImagem(
            imagem.getObjectName()
        );

        if (!excluida) {
            throw new RuntimeException(
                "Não foi possível excluir o arquivo do bucket. "
                + "O registro do banco foi mantido.");
        }

        imagemRepository.excluirPorId(imagemId);
    }
}
