
package com.messagerie.client;

import java.io.File;

public class FichierRecu {

    private String expediteur;
    private String nomFichier;
    private String typeFichier;
    private File fichier;

    public FichierRecu(String expediteur, String nomFichier,
                       String typeFichier, File fichier) {
        this.expediteur = expediteur;
        this.nomFichier = nomFichier;
        this.typeFichier = typeFichier;
        this.fichier = fichier;
    }

    public String getExpediteur() {
        return expediteur;
    }

    public String getNomFichier() {
        return nomFichier;
    }

    public String getTypeFichier() {
        return typeFichier;
    }

    public File getFichier() {
        return fichier;
    }
}
