
package com.messagerie.serveur;

import java.util.ArrayList;
import java.util.List;

public class GestionClients {

    private static List<ClientHandler> clients = new ArrayList<>();

    public static synchronized void ajouterClient(ClientHandler client) {
        clients.add(client);
        System.out.println("Nombre de clients connectes : " + clients.size());
    }

    public static synchronized void supprimerClient(ClientHandler client) {
        clients.remove(client);
        System.out.println("Nombre de clients connectes : " + clients.size());
    }

    public static synchronized List<ClientHandler> getClients() {
        return new ArrayList<>(clients);
    }

    public static synchronized ClientHandler chercherClientParPseudo(String pseudo) {
        for (ClientHandler client : clients) {
            if (client.getPseudo().equalsIgnoreCase(pseudo)) {
                return client;
            }
        }
        return null;
    }

    // Envoyer la liste des utilisateurs connectes a tous les clients
    public static synchronized void diffuserListeUtilisateurs() {

        StringBuilder liste = new StringBuilder("USERS|");

        for (ClientHandler client : clients) {
            liste.append(client.getPseudo()).append(",");
        }

        for (ClientHandler client : clients) {
            client.envoyerMessage(liste.toString());
        }
    }

    // Informer tous les clients qu'un utilisateur est connecte
    public static synchronized void diffuserConnexion(String pseudo) {

        for (ClientHandler client : clients) {
            client.envoyerMessage("CONNEXION|" + pseudo);
        }
    }
}

/*Pourquoi utiliser new ArrayList<>(clients) dans getClients() ?

Pour retourner une copie de la liste. Ainsi, les autres classes ne peuvent 
pas modifier directement la liste originale.*/