
package com.messagerie.serveur;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;

public class Serveur {

    public static void main(String[] args) {

        try {

            ServerSocket serveur = new ServerSocket(5000); //Crée le serveur sur le port 5000.

            System.out.println("Serveur demarre sur le port 5000...");
            
            while (true) {//	Permet d'accepter continuellement de nouveaux clients.

            	//socket Représente la connexion avec un client.
                Socket socket = serveur.accept();

                System.out.println("Un nouveau client est connecte !");

                //ClientHandler:	Gère la communication avec un client.
                ClientHandler client = new ClientHandler(socket);

                Thread thread = new Thread(client);

                thread.start();
            }

        } catch (IOException e) {
            System.out.println("Erreur serveur : " + e.getMessage());
        }
    }
}
