
package br.com.grupoe.restaurante;

import br.com.grupoe.restaurante.controller.PratoController;
import br.com.grupoe.restaurante.database.Database;

import com.sun.net.httpserver.HttpServer;

import java.net.InetSocketAddress;

public class ServidorWeb {

    public static void main(String[] args) throws Exception {

        Database.inicializar();

        HttpServer servidor = HttpServer.create(
            new InetSocketAddress(8080), 0
        );

        servidor.createContext("/pratos", new PratoController());

        servidor.start();

        System.out.println("Servidor iniciado com sucesso!");
        System.out.println("Acesse: http://localhost:8080/pratos");
    }
}
