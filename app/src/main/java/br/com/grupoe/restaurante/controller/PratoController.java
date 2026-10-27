
package br.com.grupoe.restaurante.controller;

import br.com.grupoe.restaurante.model.Prato;
import br.com.grupoe.restaurante.model.Imagem;
import br.com.grupoe.restaurante.repository.PratoRepository;
import br.com.grupoe.restaurante.repository.ImagemRepository;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;

import java.io.IOException;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;
import java.util.List;

public class PratoController implements HttpHandler {

    private final PratoRepository pratoRepository;
    private final ImagemRepository imagemRepository;

    public PratoController() {
        this.pratoRepository = new PratoRepository();
        this.imagemRepository = new ImagemRepository();
    }

    @Override
    public void handle(HttpExchange exchange) throws IOException {
        String metodo = exchange.getRequestMethod();
        String caminho = exchange.getRequestURI().getPath();

        if (!metodo.equals("GET")) {
            exchange.getResponseHeaders().set("Allow", "GET");
            responder(exchange, 405, "Metodo nao permitido.");
            return;
        }

        try {
            if (caminho.equals("/pratos") ||
                caminho.equals("/pratos/")) {
                listar(exchange);
            } else if (caminho.matches("/pratos/[0-9]+")) {
                String idTexto = caminho.substring("/pratos/".length());
                int id = Integer.parseInt(idTexto);
                detalhar(exchange, id);
            } else {
                responder(exchange, 404, "Pagina nao encontrada.");
            }
        } catch (NumberFormatException e) {
            responder(exchange, 400, "ID invalido.");
        } catch (Exception e) {
            e.printStackTrace();
            responder(exchange, 500, "Erro interno do servidor.");
        }
    }

    private void listar(HttpExchange exchange) throws IOException {
        List<Prato> pratos = pratoRepository.listar();

        StringBuilder html = new StringBuilder();
        html.append("<html><head><meta charset='UTF-8'>");
        html.append("<title>Cardapio - Grupo E</title></head><body>");
        html.append("<h1>Cardapio do Restaurante</h1>");

        for (Prato prato : pratos) {
            html.append("<div style='margin-bottom:25px'>");

            List<Imagem> imagens =
                imagemRepository.listarPorPrato(prato.getId());

            for (Imagem imagem : imagens) {
                if (imagem.isPrincipal()) {
                    html.append("<img width='180' src='")
                        .append(urlImagem(imagem))
                        .append("' alt='Imagem do prato'>");
                    break;
                }
            }

            html.append("<h2>").append(escapar(prato.getNome()))
                .append("</h2>");
            html.append("<p>").append(escapar(prato.getDescricao()))
                .append("</p>");
            html.append("<p>R$ ").append(prato.getPreco())
                .append("</p>");
            html.append("<a href='/pratos/")
                .append(prato.getId())
                .append("'>Ver detalhes</a>");
            html.append("</div>");
        }

        html.append("</body></html>");
        responder(exchange, 200, html.toString());
    }

    private void detalhar(HttpExchange exchange, int id)
            throws IOException {

        Prato prato = pratoRepository.buscarPorId(id);

        if (prato == null) {
            responder(exchange, 404, "Prato nao encontrado.");
            return;
        }

        List<Imagem> imagens = imagemRepository.listarPorPrato(id);

        StringBuilder html = new StringBuilder();
        html.append("<html><head><meta charset='UTF-8'>");
        html.append("<title>Detalhes do Prato</title></head><body>");

        html.append("<h1>").append(escapar(prato.getNome()))
            .append("</h1>");
        html.append("<p>").append(escapar(prato.getDescricao()))
            .append("</p>");
        html.append("<p>Preco: R$ ").append(prato.getPreco())
            .append("</p>");

        html.append("<h2>Imagem principal</h2>");

        for (Imagem imagem : imagens) {
            if (imagem.isPrincipal()) {
                html.append("<img width='300' src='")
                    .append(urlImagem(imagem))
                    .append("' alt='Imagem principal'>");
                break;
            }
        }

        html.append("<h2>Galeria de imagens</h2>");

        for (Imagem imagem : imagens) {
            html.append("<img width='150' style='margin:10px' src='")
                .append(urlImagem(imagem))
                .append("' alt='Foto do prato'>");
        }

        html.append("<p><a href='/pratos'>Voltar ao cardapio</a></p>");
        html.append("</body></html>");

        responder(exchange, 200, html.toString());
    }

    private String urlImagem(Imagem imagem) {
        String objectName = imagem.getObjectName();
        StringBuilder caminho = new StringBuilder();

        for (byte b : objectName.getBytes(StandardCharsets.UTF_8)) {
            int c = b & 0xff;
            if ((c >= 'A' && c <= 'Z') ||
                (c >= 'a' && c <= 'z') ||
                (c >= '0' && c <= '9') ||
                c == '-' || c == '_' || c == '.' || c == '~' || c == '/') {
                caminho.append((char) c);
            } else {
                caminho.append(String.format("%%%02X", c));
            }
        }

        return "https://storage.googleapis.com/fatec-2026-grupo-e/"
            + caminho;
    }

    private String escapar(String texto) {
        if (texto == null) return "";
        return texto.replace("&", "&amp;")
            .replace("<", "&lt;")
            .replace(">", "&gt;")
            .replace("\"", "&quot;")
            .replace("'", "&#39;");
    }

    private void responder(HttpExchange exchange, int status,
                          String conteudo) throws IOException {

        exchange.getResponseHeaders().set(
            "Content-Type", "text/html; charset=UTF-8"
        );

        byte[] bytes = conteudo.getBytes(StandardCharsets.UTF_8);
        exchange.sendResponseHeaders(status, bytes.length);

        try (OutputStream os = exchange.getResponseBody()) {
            os.write(bytes);
        }
    }
}
