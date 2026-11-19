
package com.messagerie.client;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;
import java.util.Scanner;

public class Client {

    public static void main(String[] args) {

        try {

            // Connecter ce client au serveur qui fonctionne sur le même ordinateur et écoute sur le port 5000.
            Socket socket = new Socket("localhost", 5000);

            System.out.println("Connecte au serveur !");

            Scanner clavier = new Scanner(System.in);

            // Envoyer les informations au serveur
            PrintWriter sortie = new PrintWriter(
                socket.getOutputStream(), true
            );

            // Saisir le pseudo
            System.out.print("Entrez votre pseudo : ");
            String pseudo = clavier.nextLine();

            sortie.println(pseudo);
         // Thread pour recevoir les messages du serveur
            Thread reception = new Thread(() -> {

                try {

                    BufferedReader entree = new BufferedReader(
                        new InputStreamReader(socket.getInputStream())
                    );

                    String message;

                    while ((message = entree.readLine()) != null) {

                        System.out.println("\n" + message);
                    }

                } catch (Exception e) {

                    System.out.println("Connexion fermee.");
                }

            });

            reception.start();

            // Envoyer des messages privés
            while (true) {

                System.out.print("Destinataire : ");
                String destinataire = clavier.nextLine();

                if (destinataire.equalsIgnoreCase("quit")) {
                    break;
                }

                System.out.print("Votre message : ");
                String message = clavier.nextLine();

                sortie.println(destinataire + "|" + message);
            }

            socket.close();

        } catch (Exception e) {

            System.out.println("Erreur client : " + e.getMessage());
        }
    }
}
