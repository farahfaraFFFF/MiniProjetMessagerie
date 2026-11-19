
package com.messagerie.serveur;

import com.messagerie.Protocole;

import com.google.gson.JsonObject;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;

import java.net.Socket;

import java.util.ArrayList;
import java.util.List;

public class ClientHandler implements Runnable {

    private Socket socket;
    private String pseudo;
    private DataOutputStream sortie;

    private AssistantIA assistantIA = new AssistantIA();

    private List<JsonObject> historiqueIA = new ArrayList<>();

    public ClientHandler(Socket socket) {
        this.socket = socket;
    }

    public String getPseudo() {
        return pseudo;
    }

    // Envoyer un message texte
    public synchronized void envoyerMessage(String message) {

        try {

            sortie.writeByte(Protocole.TYPE_TEXTE);
            sortie.writeUTF(message);
            sortie.flush();

        } catch (IOException e) {

            System.out.println("Erreur d'envoi vers " + pseudo);
        }
    }

    // Transférer un fichier vers un client
    public synchronized void recevoirFichier(
            DataInputStream entree,
            String expediteur,
            String nomFichier,
            String typeFichier,
            long taille) throws IOException {

        sortie.writeByte(Protocole.TYPE_FICHIER);
        sortie.writeUTF(expediteur);
        sortie.writeUTF(nomFichier);
        sortie.writeUTF(typeFichier);
        sortie.writeLong(taille);

        byte[] buffer = new byte[4096];

        long restant = taille;

        while (restant > 0) {

            int lu = entree.read(
                buffer,
                0,
                (int) Math.min(buffer.length, restant)
            );

            if (lu == -1) {
                throw new IOException("Fichier incomplet.");
            }

            sortie.write(buffer, 0, lu);
            restant -= lu;
        }

        sortie.flush();
    }

    // Ignorer un fichier si le destinataire est introuvable
    private void ignorerFichier(
            DataInputStream entree,
            long taille) throws IOException {

        byte[] buffer = new byte[4096];

        long restant = taille;

        while (restant > 0) {

            int lu = entree.read(
                buffer,
                0,
                (int) Math.min(buffer.length, restant)
            );

            if (lu == -1) {
                throw new IOException("Fichier incomplet.");
            }

            restant -= lu;
        }
    }

    // Traiter une question destinée à l'IA
    private void traiterQuestionIA(String question) {

        try {

            String reponse = assistantIA.repondre(
                pseudo,
                question,
                historiqueIA
            );

            envoyerMessage("IA|" + reponse);

            System.out.println(
                "Réponse IA envoyée à " + pseudo
            );

        } catch (InterruptedException e) {

            Thread.currentThread().interrupt();

            envoyerMessage(
                "IA|La génération de la réponse a été interrompue."
            );

        } catch (Exception e) {

            System.out.println(
                "Erreur IA : " + e.getMessage()
            );

            envoyerMessage(
                "IA|Désolé, je ne peux pas répondre pour le moment. "
                + "Vérifiez la configuration de l'API."
            );
        }
    }

    @Override
    public void run() {

        boolean clientAjoute = false;

        try {

            DataInputStream entree = new DataInputStream(
                new BufferedInputStream(socket.getInputStream())
            );

            sortie = new DataOutputStream(
                new BufferedOutputStream(socket.getOutputStream())
            );

            // Recevoir le pseudo
            pseudo = entree.readUTF();

            if (pseudo == null || pseudo.trim().isEmpty()) {
                return;
            }

            System.out.println(pseudo + " est connecté.");

            GestionClients.ajouterClient(this);
            clientAjoute = true;

            GestionClients.diffuserConnexion(pseudo);
            GestionClients.diffuserListeUtilisateurs();

            while (true) {

                byte type = entree.readByte();

                // Message texte privé
                if (type == Protocole.TYPE_TEXTE) {

                    String message = entree.readUTF();

                    int position = message.indexOf("|");

                    if (position != -1) {

                        String destinataire =
                            message.substring(0, position);

                        String contenu =
                            message.substring(position + 1);

                        ClientHandler clientDestinataire =
                            GestionClients.chercherClientParPseudo(
                                destinataire
                            );

                        if (clientDestinataire != null) {

                            clientDestinataire.envoyerMessage(
                                "De " + pseudo + " : " + contenu
                            );

                            System.out.println(
                                pseudo + " -> " + destinataire
                                + " : " + contenu
                            );

                        } else {

                            envoyerMessage(
                                "Utilisateur introuvable : " + destinataire
                            );
                        }

                    } else {

                        envoyerMessage("Format incorrect.");
                    }
                }

                // Transfert de fichier
                else if (type == Protocole.TYPE_FICHIER) {

                    String destinataire = entree.readUTF();
                    String nomFichier = entree.readUTF();
                    String typeFichier = entree.readUTF();
                    long taille = entree.readLong();

                    ClientHandler clientDestinataire =
                        GestionClients.chercherClientParPseudo(
                            destinataire
                        );

                    if (clientDestinataire != null) {

                        clientDestinataire.recevoirFichier(
                            entree,
                            pseudo,
                            nomFichier,
                            typeFichier,
                            taille
                        );

                        System.out.println(
                            "Fichier " + nomFichier
                            + " envoyé de " + pseudo
                            + " vers " + destinataire
                        );

                    } else {

                        ignorerFichier(entree, taille);

                        envoyerMessage(
                            "Utilisateur introuvable : " + destinataire
                        );
                    }
                }

                // Question pour l'agent IA
                else if (type == Protocole.TYPE_IA) {

                    String question = entree.readUTF();

                    if (!question.trim().isEmpty()) {
                        traiterQuestionIA(question);
                    }
                }

                else {

                    throw new IOException(
                        "Type de message inconnu."
                    );
                }
            }

        } catch (IOException e) {

            System.out.println(pseudo + " est déconnecté.");

        } finally {

            if (clientAjoute) {

                GestionClients.supprimerClient(this);
                GestionClients.diffuserListeUtilisateurs();
            }

            try {

                socket.close();

            } catch (IOException e) {

                System.out.println(
                    "Erreur lors de la fermeture du socket."
                );
            }
        }
    }
}
