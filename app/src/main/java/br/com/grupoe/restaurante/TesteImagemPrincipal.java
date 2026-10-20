
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

public class TesteImagemPrincipal {

    public static void main(String[] args) {
        try {
            Database.inicializar();

            PratoRepository pratoRepository = new PratoRepository();
            ImagemRepository imagemRepository = new ImagemRepository();
            ImagemService imagemService = new ImagemService();

            List<Prato> pratos = pratoRepository.listar();

            if (pratos.isEmpty()) {
                System.out.println("ERRO: nenhum prato cadastrado.");
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

            // Localiza a imagem principal atual
            List<Imagem> imagensAntes =
                imagemService.listarImagensDoPrato(prato.getId());

            Imagem principalAtual = imagensAntes.stream()
                .filter(Imagem::isPrincipal)
                .findFirst()
                .orElse(null);

            if (principalAtual == null) {
                System.out.println(
                    "ERRO: não existe imagem principal para esse prato."
                );
                return;
            }

            System.out.println(
                "Imagem principal atual: " + principalAtual.getId()
            );

            // Cadastra outra imagem sem torná-la principal
            Imagem novaImagem = imagemService.enviarImagem(
                prato.getId(),
                caminhoImagem,
                false
            );

            System.out.println(
                "Nova imagem cadastrada: " + novaImagem.getId()
            );

            // Transfere a marcação de principal para a nova imagem
            imagemService.definirImagemPrincipal(
                prato.getId(),
                novaImagem.getId()
            );

            // Confere o resultado no SQLite
            List<Imagem> imagensDepois =
                imagemService.listarImagensDoPrato(prato.getId());

            long quantidadePrincipal = imagensDepois.stream()
                .filter(Imagem::isPrincipal)
                .count();

            boolean novaEhPrincipal = imagemRepository
                .buscarPorId(novaImagem.getId())
                .isPrincipal();

            System.out.println(
                "Quantidade de imagens principais: "
                + quantidadePrincipal
            );

            System.out.println(
                "Nova imagem é principal: " + novaEhPrincipal
            );

            if (quantidadePrincipal == 1 && novaEhPrincipal) {
                System.out.println(
                    "SUCESSO: a imagem principal foi trocada corretamente!"
                );
            } else {
                System.out.println(
                    "ATENÇÃO: a troca da imagem principal precisa ser revisada."
                );
            }

        } catch (Exception e) {
            System.out.println("ERRO NO TESTE:");
            e.printStackTrace();
        }
    }
}
