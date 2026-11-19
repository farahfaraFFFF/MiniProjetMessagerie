
package com.messagerie;

import java.io.File;
import java.time.LocalDateTime;

public class Message {

    public enum TypeMessage {
        TEXTE,
        IMAGE,
        AUDIO,
        VIDEO,
        FICHIER
    }

    private String expediteur;
    private String contenu;
    private TypeMessage type;
    private File fichier;
    private LocalDateTime date;

    public Message(String expediteur, String contenu,
                   TypeMessage type, File fichier) {

        this.expediteur = expediteur;
        this.contenu = contenu;
        this.type = type;
        this.fichier = fichier;
        this.date = LocalDateTime.now();
    }

    public String getExpediteur() {
        return expediteur;
    }

    public String getContenu() {
        return contenu;
    }

    public TypeMessage getType() {
        return type;
    }

    public File getFichier() {
        return fichier;
    }

    public LocalDateTime getDate() {
        return date;
    }
}
