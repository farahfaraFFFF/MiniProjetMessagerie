
package com.messagerie;

import com.messagerie.client.ConnexionClient;

import javafx.animation.FadeTransition;
import javafx.animation.TranslateTransition;
import javafx.application.Application;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.effect.DropShadow;
import javafx.scene.layout.HBox;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;
import javafx.stage.Stage;
import javafx.util.Duration;

public class Main extends Application {

    @Override
    //public void start(Stage stage) est la méthode appelée automatiquement par JavaFX au démarrage de l'application.
    public void start(Stage stage) {

        // Partie gauche : présentation de la plateforme
        VBox panneauGauche = new VBox(25); //25 représente l'espacement vertical entre les composants.
        panneauGauche.setAlignment(Pos.CENTER_LEFT);
        panneauGauche.setPadding(new Insets(60));
        panneauGauche.setPrefWidth(530);
        panneauGauche.setStyle(
            "-fx-background-color: linear-gradient(to bottom right, #3023AE, #7B2FF7, #B14AED);"
        );

        //Crée un texte contenant une étoile et le nom MESSENGER.
        Label logo = new Label("✦  farah a");
        logo.setStyle(
            "-fx-text-fill: white;" +
            "-fx-font-size: 22px;" +
            "-fx-font-weight: bold;"
        );

        Label grandTitre = new Label(
            "Connectez-vous.\nPartagez. Communiquez."
        );
        grandTitre.setStyle(
            "-fx-text-fill: white;" +
            "-fx-font-size: 37px;" +
            "-fx-font-weight: bold;"
        );

        Label description = new Label(
            "Une nouvelle façon de communiquer,\n" +
            "simple, rapide et intelligente."
        );
        description.setStyle(
            "-fx-text-fill: #E9DDFF;" +
            "-fx-font-size: 16px;" +
            "-fx-line-spacing: 8px;"
        );

        Label fonctionnalites = new Label(
            "✉  Messagerie instantanée\n\n" +
            "◈  Partage de fichiers et de médias\n\n" +
            "✦  Assistant intelligent intégré"
        );
        fonctionnalites.setStyle(
            "-fx-text-fill: white;" +
            "-fx-font-size: 15px;"
        );

        panneauGauche.getChildren().addAll(
            logo, grandTitre, description, fonctionnalites
        );

        // Cercles décoratifs
        Circle cercle1 = new Circle(120, Color.rgb(255, 255, 255, 0.08));
        Circle cercle2 = new Circle(75, Color.rgb(255, 255, 255, 0.07));

        StackPane decoration = new StackPane(cercle1, cercle2);
        StackPane.setAlignment(cercle1, Pos.TOP_RIGHT);
        StackPane.setAlignment(cercle2, Pos.BOTTOM_LEFT);
        decoration.setMouseTransparent(true);

        StackPane gaucheAvecDecoration = new StackPane(
            panneauGauche, decoration
        );

        // Partie droite : formulaire de connexion
        VBox formulaire = new VBox(20);
        formulaire.setAlignment(Pos.CENTER_LEFT);
        formulaire.setMaxWidth(370);

        Label petitTitre = new Label("BIENVENUE");
        petitTitre.setStyle(
            "-fx-text-fill: #8054E9;" +
            "-fx-font-size: 13px;" +
            "-fx-font-weight: bold;"
        );

        Label titreConnexion = new Label("Content de vous revoir !");
        titreConnexion.setStyle(
            "-fx-text-fill: #24213A;" +
            "-fx-font-size: 28px;" +
            "-fx-font-weight: bold;"
        );

        Label sousTitre = new Label(
            "Entrez votre pseudo pour accéder à votre espace."
        );
        sousTitre.setStyle(
            "-fx-text-fill: #858398;" +
            "-fx-font-size: 13px;"
        );

        Label labelPseudo = new Label("Votre pseudo");
        labelPseudo.setStyle(
            "-fx-text-fill: #343047;" +
            "-fx-font-size: 13px;" +
            "-fx-font-weight: bold;"
        );

        TextField champPseudo = new TextField();
        champPseudo.setPromptText("Exemple : Farah");
        champPseudo.setPrefHeight(48);
        champPseudo.setStyle(
            "-fx-background-color: #F5F3FA;" +
            "-fx-background-radius: 12;" +
            "-fx-border-color: #E8E3F2;" +
            "-fx-border-radius: 12;" +
            "-fx-padding: 12;" +
            "-fx-font-size: 14px;"
        );

        Button boutonConnexion = new Button("Se connecter  →");
        boutonConnexion.setMaxWidth(Double.MAX_VALUE);
        boutonConnexion.setPrefHeight(50);
        boutonConnexion.setStyle(
            "-fx-background-color: linear-gradient(to right, #7545E8, #A34DEB);" +
            "-fx-text-fill: white;" +
            "-fx-font-size: 15px;" +
            "-fx-font-weight: bold;" +
            "-fx-background-radius: 12;" +
            "-fx-cursor: hand;"
        );

        boutonConnexion.setEffect(new DropShadow(
            15, Color.rgb(117, 69, 232, 0.22)
        ));

        Label message = new Label();
        message.setWrapText(true);
        message.setStyle(
            "-fx-text-fill: #E05252;" +
            "-fx-font-size: 12px;"
        );

        Label piedPage = new Label(
            "Votre espace de communication vous attend."
        );
        piedPage.setStyle(
            "-fx-text-fill: #A09BAD;" +
            "-fx-font-size: 11px;"
        );

        formulaire.getChildren().addAll(
            petitTitre,
            titreConnexion,
            sousTitre,
            new VBox(8, labelPseudo, champPseudo),
            boutonConnexion,
            message,
            piedPage
        );

        StackPane panneauDroit = new StackPane(formulaire);
        panneauDroit.setPadding(new Insets(45));
        panneauDroit.setStyle("-fx-background-color: #FFFFFF;");

        HBox root = new HBox(
            gaucheAvecDecoration,
            panneauDroit
        );

        HBox.setHgrow(panneauDroit, javafx.scene.layout.Priority.ALWAYS);

        // Connexion au serveur
        ConnexionClient connexion = new ConnexionClient();

        boutonConnexion.setOnAction(e -> {

            String pseudo = champPseudo.getText().trim();

            if (pseudo.isEmpty()) {
                message.setText("Veuillez saisir votre pseudo !");
                champPseudo.requestFocus();
                return;
            }

            try {
                boutonConnexion.setDisable(true);
                boutonConnexion.setText("Connexion en cours...");
                message.setText("");

                connexion.connecter(pseudo);

                FenetreMessagerie fenetre = new FenetreMessagerie();
                fenetre.afficher(stage, pseudo, connexion);

                stage.setOnCloseRequest(event -> {
                    try {
                        connexion.fermerConnexion();
                    } catch (Exception ex) {
                        System.out.println(
                            "Erreur lors de la fermeture."
                        );
                    }
                });

            } catch (Exception ex) {
                message.setText(
                    "Impossible de contacter le serveur. Vérifiez qu'il est démarré."
                );

                boutonConnexion.setDisable(false);
                boutonConnexion.setText("Se connecter  →");
            }
        });

        // Permet de se connecter avec la touche Entrée
        champPseudo.setOnAction(e -> boutonConnexion.fire());

        // Animation d'apparition
        formulaire.setOpacity(0);
        formulaire.setTranslateY(25);

        FadeTransition apparition = new FadeTransition(
            Duration.millis(900), formulaire
        );
        apparition.setFromValue(0);
        apparition.setToValue(1);
        apparition.play();

        TranslateTransition mouvement = new TranslateTransition(
            Duration.millis(900), formulaire
        );
        mouvement.setFromY(25);
        mouvement.setToY(0);
        mouvement.play();

        // Fenêtre
        Scene scene = new Scene(root, 1150, 700);

        stage.setTitle("Messenger | Connexion");
        stage.setScene(scene);
        stage.setMinWidth(950);
        stage.setMinHeight(620);
        stage.centerOnScreen();
        stage.show();
    }

    public static void main(String[] args) {
        launch(args);
    }
}
