
package com.messagerie;

import java.awt.Desktop;
import java.io.File;
import java.time.format.DateTimeFormatter;

import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.Clip;

import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ListCell;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;

public class CelluleMessage extends ListCell<Message> {

    private static final String VERT = "#DCF8C6";
    private static final String BLANC = "#FFFFFF";
    private static final String TEXTE = "#263238";
    private static final String GRIS = "#78909C";

    @Override
    protected void updateItem(Message message, boolean empty) {

        super.updateItem(message, empty);

        if (empty || message == null) {
            setGraphic(null);
            setText(null);
            setStyle("-fx-background-color: transparent;");
            return;
        }

        boolean estMoi = "Moi".equals(message.getExpediteur());

        // Bulle du message
        VBox bulle = new VBox(7);
        bulle.setPadding(new Insets(10, 13, 8, 13));
        bulle.setMaxWidth(330);

        bulle.setStyle(
            "-fx-background-color: "
            + (estMoi ? VERT : BLANC) + ";" +
            "-fx-background-radius: 15;" +
            "-fx-effect: dropshadow(gaussian, rgba(0,0,0,0.07), 5, 0, 0, 2);"
        );

        // Nom de l'expéditeur
        if (!estMoi && message.getType() != Message.TypeMessage.TEXTE) {
            Label expediteur = new Label(message.getExpediteur());
            expediteur.setStyle(
                "-fx-text-fill: #128C7E;" +
                "-fx-font-weight: bold;" +
                "-fx-font-size: 11px;"
            );
            bulle.getChildren().add(expediteur);
        }

        // Contenu selon le type de message
        switch (message.getType()) {

            case TEXTE:

                Label contenu = new Label(message.getContenu());
                contenu.setWrapText(true);
                contenu.setMaxWidth(290);

                contenu.setStyle(
                    "-fx-text-fill: " + TEXTE + ";" +
                    "-fx-font-size: 14px;"
                );

                bulle.getChildren().add(contenu);
                break;

            case IMAGE:

                File imageFile = message.getFichier();

                if (imageFile != null && imageFile.exists()) {

                    try {
                        ImageView imageView = new ImageView(
                            new Image(imageFile.toURI().toString())
                        );

                        imageView.setFitWidth(270);
                        imageView.setFitHeight(220);
                        imageView.setPreserveRatio(true);
                        imageView.setSmooth(true);

                        bulle.getChildren().add(imageView);

                    } catch (Exception e) {
                        bulle.getChildren().add(
                            new Label("Impossible d'afficher l'image")
                        );
                    }
                }
                break;

            case AUDIO:

                HBox audioBox = new HBox(10);
                audioBox.setAlignment(Pos.CENTER_LEFT);

                Button boutonAudio = new Button("▶");

                boutonAudio.setStyle(
                    "-fx-background-color: #128C7E;" +
                    "-fx-text-fill: white;" +
                    "-fx-font-size: 16px;" +
                    "-fx-background-radius: 50%;" +
                    "-fx-min-width: 38px;" +
                    "-fx-min-height: 38px;" +
                    "-fx-cursor: hand;"
                );

                Label texteAudio = new Label("Message vocal");
                texteAudio.setStyle(
                    "-fx-text-fill: " + TEXTE + ";" +
                    "-fx-font-size: 13px;" +
                    "-fx-font-weight: bold;"
                );

                boutonAudio.setOnAction(
                    e -> lireAudio(message.getFichier())
                );

                audioBox.getChildren().addAll(
                    boutonAudio,
                    texteAudio
                );

                bulle.getChildren().add(audioBox);
                break;

            case VIDEO:
            case FICHIER:

                HBox fichierBox = new HBox(10);
                fichierBox.setAlignment(Pos.CENTER_LEFT);

                Label icone = new Label(
                    message.getType() == Message.TypeMessage.VIDEO
                    ? "▶" : "📄"
                );

                icone.setStyle(
                    "-fx-font-size: 22px;"
                );

                VBox informations = new VBox(3);

                Label nomFichier = new Label(message.getContenu());
                nomFichier.setWrapText(true);
                nomFichier.setMaxWidth(190);

                nomFichier.setStyle(
                    "-fx-text-fill: " + TEXTE + ";" +
                    "-fx-font-weight: bold;" +
                    "-fx-font-size: 12px;"
                );

                Label typeFichier = new Label("Fichier joint");
                typeFichier.setStyle(
                    "-fx-text-fill: " + GRIS + ";" +
                    "-fx-font-size: 11px;"
                );

                informations.getChildren().addAll(
                    nomFichier,
                    typeFichier
                );

                Button ouvrir = new Button("Ouvrir");

                ouvrir.setStyle(
                    "-fx-background-color: #128C7E;" +
                    "-fx-text-fill: white;" +
                    "-fx-background-radius: 8;" +
                    "-fx-cursor: hand;"
                );

                ouvrir.setOnAction(
                    e -> ouvrirFichier(message.getFichier())
                );

                fichierBox.getChildren().addAll(
                    icone,
                    informations,
                    ouvrir
                );

                bulle.getChildren().add(fichierBox);
                break;
        }

        // Heure du message
        Label heure = new Label(
            message.getDate().format(
                DateTimeFormatter.ofPattern("HH:mm")
            )
        );

        heure.setStyle(
            "-fx-text-fill: " + GRIS + ";" +
            "-fx-font-size: 10px;"
        );

        HBox ligneHeure = new HBox(heure);
        ligneHeure.setAlignment(Pos.CENTER_RIGHT);

        bulle.getChildren().add(ligneHeure);

        // Alignement du message
        HBox ligne = new HBox();

        ligne.setPadding(new Insets(5, 15, 5, 15));
        ligne.setAlignment(
            estMoi ? Pos.CENTER_RIGHT : Pos.CENTER_LEFT
        );

        ligne.setMaxWidth(Double.MAX_VALUE);
        ligne.getChildren().add(bulle);

        setGraphic(ligne);
        setText(null);

        setStyle(
            "-fx-background-color: transparent;" +
            "-fx-padding: 0;"
        );
    }

    private void ouvrirFichier(File fichier) {

        try {
            if (fichier != null && fichier.exists()) {
                Desktop.getDesktop().open(fichier);
            }
        } catch (Exception e) {
            System.out.println(
                "Impossible d'ouvrir le fichier : " + e.getMessage()
            );
        }
    }

    private void lireAudio(File fichier) {

        try {
            if (fichier != null && fichier.exists()) {

                Clip clip = AudioSystem.getClip();

                clip.open(
                    AudioSystem.getAudioInputStream(fichier)
                );

                clip.start();
            }

        } catch (Exception e) {
            System.out.println(
                "Erreur de lecture audio : " + e.getMessage()
            );
        }
    }
}
