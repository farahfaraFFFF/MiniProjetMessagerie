
package com.messagerie.client;

import com.messagerie.Protocole;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;

import java.net.Socket;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

import java.util.function.Consumer;

public class ConnexionClient {

    private Socket socket;
    private DataOutputStream sortie;
    private DataInputStream entree;

    public void connecter(String pseudo) throws IOException {

        socket = new Socket("localhost", 5000);

        sortie = new DataOutputStream(
            new BufferedOutputStream(socket.getOutputStream())
        );

        entree = new DataInputStream(
            new BufferedInputStream(socket.getInputStream())
        );

        sortie.writeUTF(pseudo);
        sortie.flush();
    }

    // Envoyer un message privé
    public synchronized void envoyerMessage(String message) {

        try {

            sortie.writeByte(Protocole.TYPE_TEXTE);
            sortie.writeUTF(message);
            sortie.flush();

        } catch (IOException e) {

            System.out.println(
                "Erreur lors de l'envoi du message."
            );
        }
    }

    // Envoyer une question à l'agent IA
    public synchronized void envoyerQuestionIA(String question)
            throws IOException {

        sortie.writeByte(Protocole.TYPE_IA);
        sortie.writeUTF(question);
        sortie.flush();
    }

    // Envoyer un fichier
    public synchronized void envoyerFichier(
            String destinataire,
            File fichier,
            String type) {

        try (FileInputStream fis = new FileInputStream(fichier)) {

            sortie.writeByte(Protocole.TYPE_FICHIER);
            sortie.writeUTF(destinataire);
            sortie.writeUTF(fichier.getName());
            sortie.writeUTF(type);
            sortie.writeLong(fichier.length());

            byte[] buffer = new byte[4096];

            int taille;

            while ((taille = fis.read(buffer)) != -1) {

                sortie.write(buffer, 0, taille);
            }

            sortie.flush();

            System.out.println(
                "Fichier envoyé : " + fichier.getName()
            );

        } catch (IOException e) {

            System.out.println(
                "Erreur lors de l'envoi du fichier : "
                + e.getMessage()
            );
        }
    }

    // Réception des messages et des fichiers
    public void demarrerReception(
            Consumer<String> traitementMessage,
            Consumer<FichierRecu> traitementFichier) {

        Thread thread = new Thread(() -> {

            try {

                while (!socket.isClosed()) {

                    byte type = entree.readByte();

                    // Réception texte
                    if (type == Protocole.TYPE_TEXTE) {

                        String message = entree.readUTF();

                        traitementMessage.accept(message);
                    }

                    // Réception fichier
                    else if (type == Protocole.TYPE_FICHIER) {

                        String expediteur = entree.readUTF();
                        String nomFichier = entree.readUTF();
                        String typeFichier = entree.readUTF();

                        long tailleFichier = entree.readLong();

                        Path dossier = Paths.get(
                            System.getProperty("user.home"),
                            "Downloads",
                            "Messenger"
                        );

                        Files.createDirectories(dossier);

                        String nomSecurise = Paths.get(nomFichier)
                            .getFileName().toString();

                        File fichier = dossier.resolve(
                            System.currentTimeMillis()
                            + "_" + nomSecurise
                        ).toFile();

                        try (FileOutputStream fos =
                                new FileOutputStream(fichier)) {

                            byte[] buffer = new byte[4096];

                            long restant = tailleFichier;

                            while (restant > 0) {

                                int taille = entree.read(
                                    buffer,
                                    0,
                                    (int) Math.min(
                                        buffer.length,
                                        restant
                                    )
                                );

                                if (taille == -1) {

                                    throw new IOException(
                                        "Fichier reçu incomplet."
                                    );
                                }

                                fos.write(buffer, 0, taille);
                                restant -= taille;
                            }
                        }

                        FichierRecu fichierRecu = new FichierRecu(
                            expediteur,
                            nomFichier,
                            typeFichier,
                            fichier
                        );

                        traitementFichier.accept(fichierRecu);
                    }
                }

            } catch (IOException e) {

                System.out.println(
                    "Connexion avec le serveur fermée."
                );
            }
        });

        thread.setDaemon(true);
        thread.start();
    }

    public void fermerConnexion() throws IOException {

        if (socket != null && !socket.isClosed()) {
            socket.close();
        }
    }
}
