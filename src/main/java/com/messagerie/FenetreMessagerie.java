
package com.messagerie;

import com.messagerie.client.ConnexionClient;
import com.messagerie.client.FichierRecu;
import com.messagerie.client.EnregistreurAudio;

import java.io.File;
import java.util.HashMap;
import java.util.Map;
import java.util.List;
import java.util.ArrayList;
import java.util.function.Consumer;

import javafx.animation.FadeTransition;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.collections.transformation.FilteredList;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.geometry.Side;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.ContextMenu;
import javafx.scene.control.Label;
import javafx.scene.control.ListView;
import javafx.scene.control.MenuItem;
import javafx.scene.control.TextField;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.scene.layout.Priority;
import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import javafx.util.Duration;

public class FenetreMessagerie {

    private final String fond = "#F5F6FC";
    private final String violet = "#7654E8";
    private final String texte = "#25243A";
    private final String gris = "#8D8BA0";

    private Button creerBouton(String titre, String couleur) {

        Button bouton = new Button(titre);

        bouton.setStyle(
            "-fx-background-color: " + couleur + ";" +
            "-fx-text-fill: white;" +
            "-fx-font-size: 13px;" +
            "-fx-font-weight: bold;" +
            "-fx-background-radius: 12;" +
            "-fx-padding: 11 16;" +
            "-fx-cursor: hand;"
        );

        return bouton;
    }

    private void afficherVue(StackPane conteneur, Node vue) {

        conteneur.getChildren().setAll(vue);

        vue.setOpacity(0);

        FadeTransition animation = new FadeTransition(
            Duration.millis(300),
            vue
        );

        animation.setFromValue(0);
        animation.setToValue(1);
        animation.play();
    }

    public void afficher(
            Stage stage,
            String pseudo,
            ConnexionClient connexion) {

        // ==========================================
        // 1. GESTION DES CONVERSATIONS
        // ==========================================

        String[] destinataireSelectionne = {null};

        Map<String, List<Message>> conversations =
            new HashMap<>();

        List<Message> historiqueIA = new ArrayList<>();

        // ==========================================
        // 2. BARRE LATERALE
        // ==========================================

        Label logo = new Label("✦ farah a");

        logo.setStyle(
            "-fx-font-size: 25px;" +
            "-fx-font-weight: bold;" +
            "-fx-text-fill: " + violet + ";"
        );

        Label sousTitre = new Label("Votre espace de communication");

        sousTitre.setStyle(
            "-fx-font-size: 11px;" +
            "-fx-text-fill: " + gris + ";"
        );

        Label avatarProfil = new Label(
            pseudo.substring(0, 1).toUpperCase()
        );

        avatarProfil.setMinSize(44, 44);
        avatarProfil.setAlignment(Pos.CENTER);

        avatarProfil.setStyle(
            "-fx-background-color: #EEE9FF;" +
            "-fx-text-fill: " + violet + ";" +
            "-fx-font-size: 18px;" +
            "-fx-font-weight: bold;" +
            "-fx-background-radius: 50%;"
        );

        Label nomProfil = new Label(pseudo);

        nomProfil.setStyle(
            "-fx-font-size: 14px;" +
            "-fx-font-weight: bold;" +
            "-fx-text-fill: " + texte + ";"
        );

        Label statutProfil = new Label("Connecté");

        statutProfil.setStyle(
            "-fx-font-size: 11px;" +
            "-fx-text-fill: #21A67A;"
        );

        VBox infosProfil = new VBox(4, nomProfil, statutProfil);

        HBox profil = new HBox(
            12,
            avatarProfil,
            infosProfil
        );

        profil.setAlignment(Pos.CENTER_LEFT);
        profil.setPadding(new Insets(14));

        profil.setStyle(
            "-fx-background-color: #F5F3FC;" +
            "-fx-background-radius: 15;"
        );

        // Bouton IA
        Button boutonAgentIA = new Button(
            "✦  Parler avec l'agent IA"
        );

        boutonAgentIA.setMaxWidth(Double.MAX_VALUE);
        boutonAgentIA.setPrefHeight(48);

        boutonAgentIA.setStyle(
            "-fx-background-color: linear-gradient(to right, #7047DF, #A05AEF);" +
            "-fx-text-fill: white;" +
            "-fx-font-size: 13px;" +
            "-fx-font-weight: bold;" +
            "-fx-background-radius: 13;" +
            "-fx-cursor: hand;"
        );

        Label titreContacts = new Label("CONVERSATIONS");

        titreContacts.setStyle(
            "-fx-font-size: 11px;" +
            "-fx-font-weight: bold;" +
            "-fx-text-fill: " + gris + ";" +
            "-fx-padding: 12 0 4 3;"
        );

        TextField recherche = new TextField();

        recherche.setPromptText("⌕  Rechercher un contact");

        recherche.setStyle(
            "-fx-background-color: #F3F3F9;" +
            "-fx-background-radius: 12;" +
            "-fx-padding: 12;" +
            "-fx-font-size: 12px;"
        );

        ObservableList<String> tousContacts =
            FXCollections.observableArrayList();

        FilteredList<String> contactsFiltres =
            new FilteredList<>(tousContacts, nom -> true);

        ListView<String> listeContacts = new ListView<>(
            contactsFiltres
        );

        recherche.textProperty().addListener(
            (observable, ancien, nouveau) -> {

                contactsFiltres.setPredicate(nom -> {

                    if (nouveau == null || nouveau.trim().isEmpty()) {
                        return true;
                    }

                    return nom.toLowerCase().contains(
                        nouveau.toLowerCase().trim()
                    );
                });
            }
        );

        listeContacts.setStyle(
            "-fx-background-color: transparent;" +
            "-fx-background-insets: 0;"
        );

        listeContacts.setCellFactory(list ->
            new javafx.scene.control.ListCell<String>() {

                @Override
                protected void updateItem(String nom, boolean empty) {

                    super.updateItem(nom, empty);

                    if (empty || nom == null) {

                        setGraphic(null);
                        setText(null);
                        setStyle("-fx-background-color: transparent;");

                        return;
                    }

                    Label avatar = new Label(
                        nom.substring(0, 1).toUpperCase()
                    );

                    avatar.setMinSize(43, 43);
                    avatar.setAlignment(Pos.CENTER);

                    avatar.setStyle(
                        "-fx-background-color: #EEE9FF;" +
                        "-fx-text-fill: #7654E8;" +
                        "-fx-font-size: 16px;" +
                        "-fx-font-weight: bold;" +
                        "-fx-background-radius: 50%;"
                    );

                    Label nomContact = new Label(nom);

                    nomContact.setStyle(
                        "-fx-font-size: 13px;" +
                        "-fx-font-weight: bold;" +
                        "-fx-text-fill: #25243A;"
                    );

                    Label description = new Label(
                        "Ouvrir la conversation"
                    );

                    description.setStyle(
                        "-fx-font-size: 10px;" +
                        "-fx-text-fill: #9A98AA;"
                    );

                    VBox infos = new VBox(
                        5,
                        nomContact,
                        description
                    );

                    HBox ligne = new HBox(
                        12,
                        avatar,
                        infos
                    );

                    ligne.setAlignment(Pos.CENTER_LEFT);
                    ligne.setPadding(new Insets(10));

                    setGraphic(ligne);
                    setText(null);

                    setStyle(
                        "-fx-background-color: transparent;" +
                        "-fx-padding: 3;"
                    );
                }
            }
        );

        VBox panneauGauche = new VBox(16);

        panneauGauche.setPadding(
            new Insets(25, 18, 20, 18)
        );

        panneauGauche.setPrefWidth(290);
        panneauGauche.setMinWidth(250);

        panneauGauche.setStyle(
            "-fx-background-color: white;" +
            "-fx-border-color: #ECEAF3;" +
            "-fx-border-width: 0 1 0 0;"
        );

        panneauGauche.getChildren().addAll(
            logo,
            sousTitre,
            profil,
            boutonAgentIA,
            titreContacts,
            recherche,
            listeContacts
        );

        VBox.setVgrow(listeContacts, Priority.ALWAYS);

        // ==========================================
        // 3. ACCUEIL
        // ==========================================

        VBox accueil = new VBox(25);

        accueil.setAlignment(Pos.CENTER);
        accueil.setPadding(new Insets(35));

        accueil.setStyle(
            "-fx-background-color: " + fond + ";"
        );

        Circle decoration1 = new Circle(
            100,
            Color.rgb(118, 84, 232, 0.07)
        );

        Circle decoration2 = new Circle(
            65,
            Color.rgb(34, 180, 180, 0.08)
        );

        Label etoile = new Label("✦");

        etoile.setStyle(
            "-fx-font-size: 52px;" +
            "-fx-text-fill: " + violet + ";"
        );

        StackPane iconeAccueil = new StackPane(
            decoration1,
            decoration2,
            etoile
        );

        Label bienvenue = new Label(
            "Bienvenue, " + pseudo + " !"
        );

        bienvenue.setStyle(
            "-fx-font-size: 30px;" +
            "-fx-font-weight: bold;" +
            "-fx-text-fill: " + texte + ";"
        );

        Label descriptionAccueil = new Label(
            "Votre espace de communication commence ici.\n" +
            "Choisissez un contact ou discutez avec votre assistant IA."
        );

        descriptionAccueil.setAlignment(Pos.CENTER);
        descriptionAccueil.setStyle(
            "-fx-font-size: 14px;" +
            "-fx-text-fill: " + gris + ";" +
            "-fx-text-alignment: center;"
        );

        Button boutonAccueilIA = creerBouton(
            "✦  Découvrir l'assistant IA",
            "#7654E8"
        );

        boutonAccueilIA.setPrefHeight(50);

        VBox carteAccueil = new VBox(
            22,
            iconeAccueil,
            bienvenue,
            descriptionAccueil,
            boutonAccueilIA
        );

        carteAccueil.setAlignment(Pos.CENTER);
        carteAccueil.setMaxWidth(570);
        carteAccueil.setPadding(new Insets(45));

        carteAccueil.setStyle(
            "-fx-background-color: white;" +
            "-fx-background-radius: 25;" +
            "-fx-effect: dropshadow(gaussian, #DAD6E8, 25, 0.12, 0, 8);"
        );

        accueil.getChildren().add(carteAccueil);

        // ==========================================
        // 4. ZONE PRINCIPALE
        // ==========================================

        StackPane contenuCentral = new StackPane();

        contenuCentral.setStyle(
            "-fx-background-color: " + fond + ";"
        );

        // ==========================================
        // 5. CONVERSATION PRIVEE
        // ==========================================

        Button boutonRetour = new Button("← Accueil");

        boutonRetour.setStyle(
            "-fx-background-color: #F1EEFA;" +
            "-fx-text-fill: #7654E8;" +
            "-fx-background-radius: 10;" +
            "-fx-padding: 9 13;" +
            "-fx-cursor: hand;"
        );

        Label avatarContact = new Label("?");

        avatarContact.setMinSize(43, 43);
        avatarContact.setAlignment(Pos.CENTER);

        avatarContact.setStyle(
            "-fx-background-color: #EEE9FF;" +
            "-fx-text-fill: #7654E8;" +
            "-fx-font-size: 17px;" +
            "-fx-font-weight: bold;" +
            "-fx-background-radius: 50%;"
        );

        Label titreConversation = new Label();

        titreConversation.setStyle(
            "-fx-font-size: 15px;" +
            "-fx-font-weight: bold;" +
            "-fx-text-fill: " + texte + ";"
        );

        Label statutConversation = new Label(
            "Conversation privée"
        );

        statutConversation.setStyle(
            "-fx-font-size: 11px;" +
            "-fx-text-fill: " + gris + ";"
        );

        VBox infosConversation = new VBox(
            5,
            titreConversation,
            statutConversation
        );

        HBox enteteConversation = new HBox(
            14,
            boutonRetour,
            avatarContact,
            infosConversation
        );

        enteteConversation.setAlignment(Pos.CENTER_LEFT);
        enteteConversation.setPadding(new Insets(15, 22, 15, 22));

        enteteConversation.setStyle(
            "-fx-background-color: white;" +
            "-fx-border-color: #ECEAF3;" +
            "-fx-border-width: 0 0 1 0;"
        );

        ListView<Message> messages = new ListView<>();

        messages.setCellFactory(param -> new CelluleMessage());

        messages.setStyle(
            "-fx-background-color: " + fond + ";" +
            "-fx-control-inner-background: " + fond + ";" +
            "-fx-background-insets: 0;"
        );

        // ==========================================
        // 6. BARRE DE SAISIE PRIVEE
        // ==========================================

        TextField champMessage = new TextField();

        champMessage.setPromptText("Écrire un message...");

        champMessage.setStyle(
            "-fx-background-color: transparent;" +
            "-fx-font-size: 13px;" +
            "-fx-padding: 10;"
        );

        HBox.setHgrow(champMessage, Priority.ALWAYS);

        Button boutonEnvoyer = creerBouton("Envoyer ➤", violet);

        Button boutonImage = new Button("📷 Image");
        Button boutonAudio = new Button("🎙 Vocal");
        Button boutonVideo = new Button("🎬 Vidéo");
        Button boutonFichier = new Button("📎 Fichier");
        Button boutonEmoji = new Button("😊 Emoji");

        String stylePieceJointe =
            "-fx-background-color: #F2EFFB;" +
            "-fx-text-fill: #6750B5;" +
            "-fx-font-size: 11px;" +
            "-fx-background-radius: 10;" +
            "-fx-padding: 10;" +
            "-fx-cursor: hand;";

        boutonImage.setStyle(stylePieceJointe);
        boutonAudio.setStyle(stylePieceJointe);
        boutonVideo.setStyle(stylePieceJointe);
        boutonFichier.setStyle(stylePieceJointe);
        boutonEmoji.setStyle(stylePieceJointe);

        HBox zoneSaisie = new HBox(
            7,
            boutonEmoji,
            boutonImage,
            boutonVideo,
            boutonFichier,
            boutonAudio,
            champMessage,
            boutonEnvoyer
        );

        zoneSaisie.setAlignment(Pos.CENTER);
        zoneSaisie.setPadding(new Insets(12));

        zoneSaisie.setStyle(
            "-fx-background-color: white;" +
            "-fx-background-radius: 16;" +
            "-fx-border-color: #E8E5F0;" +
            "-fx-border-radius: 16;"
        );

        VBox zoneDiscussion = new VBox(
            12,
            zoneSaisie
        );

        zoneDiscussion.setPadding(
            new Insets(10, 20, 18, 20)
        );

        BorderPane panneauConversation = new BorderPane();

        panneauConversation.setTop(enteteConversation);
        panneauConversation.setCenter(messages);
        panneauConversation.setBottom(zoneDiscussion);

        panneauConversation.setStyle(
            "-fx-background-color: " + fond + ";"
        );

        // ==========================================
        // 7. CONVERSATION AVEC L'AGENT IA
        // ==========================================

        Button boutonRetourIA = new Button("← Accueil");

        boutonRetourIA.setStyle(
            "-fx-background-color: #F1EEFA;" +
            "-fx-text-fill: #7654E8;" +
            "-fx-background-radius: 10;" +
            "-fx-padding: 9 13;" +
            "-fx-cursor: hand;"
        );

        Label avatarIA = new Label("✦");

        avatarIA.setMinSize(45, 45);
        avatarIA.setAlignment(Pos.CENTER);

        avatarIA.setStyle(
            "-fx-background-color: #EEE9FF;" +
            "-fx-text-fill: #7654E8;" +
            "-fx-font-size: 22px;" +
            "-fx-background-radius: 50%;"
        );

        Label titreIA = new Label("Assistant IA");

        titreIA.setStyle(
            "-fx-font-size: 15px;" +
            "-fx-font-weight: bold;" +
            "-fx-text-fill: " + texte + ";"
        );

        Label statutIA = new Label("Votre assistant intelligent");

        statutIA.setStyle(
            "-fx-font-size: 11px;" +
            "-fx-text-fill: #21A67A;"
        );

        VBox infosIA = new VBox(5, titreIA, statutIA);

        HBox enteteIA = new HBox(
            14,
            boutonRetourIA,
            avatarIA,
            infosIA
        );

        enteteIA.setAlignment(Pos.CENTER_LEFT);
        enteteIA.setPadding(new Insets(15, 22, 15, 22));

        enteteIA.setStyle(
            "-fx-background-color: white;" +
            "-fx-border-color: #ECEAF3;" +
            "-fx-border-width: 0 0 1 0;"
        );

        ListView<Message> messagesIA = new ListView<>();

        messagesIA.setCellFactory(param -> new CelluleMessage());

        messagesIA.setStyle(
            "-fx-background-color: " + fond + ";" +
            "-fx-control-inner-background: " + fond + ";"
        );

        Label statutGeneration = new Label();

        statutGeneration.setStyle(
            "-fx-text-fill: " + violet + ";" +
            "-fx-font-size: 11px;"
        );

        TextField champIA = new TextField();

        champIA.setPromptText(
            "Posez votre question à l'assistant..."
        );

        champIA.setStyle(
            "-fx-background-color: #F5F3FA;" +
            "-fx-background-radius: 12;" +
            "-fx-padding: 13;" +
            "-fx-font-size: 13px;"
        );

        HBox.setHgrow(champIA, Priority.ALWAYS);

        Button boutonEnvoyerIA = creerBouton(
            "Envoyer ➤",
            "#7654E8"
        );

        HBox zoneSaisieIA = new HBox(
            10,
            champIA,
            boutonEnvoyerIA
        );

        zoneSaisieIA.setAlignment(Pos.CENTER);
        zoneSaisieIA.setPadding(new Insets(13));

        VBox basIA = new VBox(
            7,
            statutGeneration,
            zoneSaisieIA
        );

        basIA.setPadding(new Insets(10, 20, 18, 20));

        basIA.setStyle("-fx-background-color: white;");

        BorderPane panneauIA = new BorderPane();

        panneauIA.setTop(enteteIA);
        panneauIA.setCenter(messagesIA);
        panneauIA.setBottom(basIA);

        panneauIA.setStyle(
            "-fx-background-color: " + fond + ";"
        );

        // ==========================================
        // 8. NAVIGATION ENTRE LES PAGES
        // ==========================================

        Runnable retourAccueil = () -> {

            destinataireSelectionne[0] = null;

            listeContacts.getSelectionModel().clearSelection();

            afficherVue(contenuCentral, accueil);
        };

        boutonRetour.setOnAction(e -> retourAccueil.run());

        boutonRetourIA.setOnAction(e -> retourAccueil.run());

        boutonAgentIA.setOnAction(e -> {

            listeContacts.getSelectionModel().clearSelection();

            destinataireSelectionne[0] = null;

            afficherVue(contenuCentral, panneauIA);
        });

        boutonAccueilIA.setOnAction(e -> {

            listeContacts.getSelectionModel().clearSelection();

            destinataireSelectionne[0] = null;

            afficherVue(contenuCentral, panneauIA);
        });

        listeContacts.getSelectionModel()
            .selectedItemProperty().addListener(
                (observable, ancien, nouveau) -> {

                    if (nouveau == null) {
                        return;
                    }

                    destinataireSelectionne[0] = nouveau;

                    titreConversation.setText(nouveau);

                    avatarContact.setText(
                        nouveau.substring(0, 1).toUpperCase()
                    );

                    conversations.computeIfAbsent(
                        nouveau,
                        nom -> new ArrayList<>()
                    );

                    messages.getItems().setAll(
                        conversations.get(nouveau)
                    );

                    afficherVue(
                        contenuCentral,
                        panneauConversation
                    );
                }
            );

        // ==========================================
        // 9. ENVOI DES FICHIERS
        // ==========================================

        Consumer<String> envoyerFichier = type -> {

            if (destinataireSelectionne[0] == null) {
                return;
            }

            FileChooser choixFichier = new FileChooser();

            choixFichier.setTitle("Choisir un fichier");

            switch (type) {

                case "image":

                    choixFichier.getExtensionFilters().add(
                        new FileChooser.ExtensionFilter(
                            "Images",
                            "*.png", "*.jpg", "*.jpeg", "*.gif"
                        )
                    );
                    break;

                case "audio":

                    choixFichier.getExtensionFilters().add(
                        new FileChooser.ExtensionFilter(
                            "Fichiers audio",
                            "*.mp3", "*.wav", "*.ogg", "*.m4a"
                        )
                    );
                    break;

                case "video":

                    choixFichier.getExtensionFilters().add(
                        new FileChooser.ExtensionFilter(
                            "Vidéos",
                            "*.mp4", "*.avi", "*.mov", "*.mkv"
                        )
                    );
                    break;

                case "file":

                    choixFichier.getExtensionFilters().add(
                        new FileChooser.ExtensionFilter(
                            "Tous les fichiers",
                            "*.*"
                        )
                    );
                    break;
            }

            File fichier = choixFichier.showOpenDialog(stage);

            if (fichier == null) {
                return;
            }

            String destinataire = destinataireSelectionne[0];

            connexion.envoyerFichier(
                destinataire,
                fichier,
                type
            );

            Message.TypeMessage typeMessage;

            switch (type) {

                case "image":
                    typeMessage = Message.TypeMessage.IMAGE;
                    break;

                case "audio":
                    typeMessage = Message.TypeMessage.AUDIO;
                    break;

                case "video":
                    typeMessage = Message.TypeMessage.VIDEO;
                    break;

                default:
                    typeMessage = Message.TypeMessage.FICHIER;
                    break;
            }

            Message message = new Message(
                "Moi",
                fichier.getName(),
                typeMessage,
                fichier
            );

            conversations.computeIfAbsent(
                destinataire,
                nom -> new ArrayList<>()
            ).add(message);

            messages.getItems().add(message);
        };

        boutonImage.setOnAction(e ->
            envoyerFichier.accept("image")
        );

        boutonVideo.setOnAction(e ->
            envoyerFichier.accept("video")
        );

        boutonFichier.setOnAction(e ->
            envoyerFichier.accept("file")
        );

        // ==========================================
        // 10. ENREGISTREMENT VOCAL
        // ==========================================

        EnregistreurAudio enregistreur =
            new EnregistreurAudio();

        boolean[] enregistrementEnCours = {false};

        String[] destinataireEnregistrement = {null};

        boutonAudio.setOnAction(e -> {

            if (destinataireSelectionne[0] == null) {
                return;
            }

            if (!enregistrementEnCours[0]) {

                try {

                    destinataireEnregistrement[0] =
                        destinataireSelectionne[0];

                    enregistreur.demarrer();

                    enregistrementEnCours[0] = true;

                    boutonAudio.setText("⏹ Arrêter");

                    boutonAudio.setStyle(
                        "-fx-background-color: #FCE4E4;" +
                        "-fx-text-fill: #E53935;" +
                        "-fx-font-size: 11px;" +
                        "-fx-background-radius: 10;" +
                        "-fx-padding: 10;"
                    );

                } catch (Exception ex) {

                    System.out.println(
                        "Erreur microphone : " + ex.getMessage()
                    );
                }

            } else {

                File fichierAudio = enregistreur.arreter();

                enregistrementEnCours[0] = false;

                boutonAudio.setText("🎙 Vocal");
                boutonAudio.setStyle(stylePieceJointe);

                if (fichierAudio != null
                        && fichierAudio.exists()
                        && fichierAudio.length() > 0) {

                    String destinataire =
                        destinataireEnregistrement[0];

                    connexion.envoyerFichier(
                        destinataire,
                        fichierAudio,
                        "audio"
                    );

                    Message message = new Message(
                        "Moi",
                        "Message vocal",
                        Message.TypeMessage.AUDIO,
                        fichierAudio
                    );

                    conversations.computeIfAbsent(
                        destinataire,
                        nom -> new ArrayList<>()
                    ).add(message);

                    if (destinataire.equals(
                            destinataireSelectionne[0])) {

                        messages.getItems().add(message);
                    }
                }

                destinataireEnregistrement[0] = null;
            }
        });

        // ==========================================
        // 11. MENU EMOJIS
        // ==========================================

        ContextMenu menuEmoji = new ContextMenu();

        String[] emojis = {
            "😀", "😂", "😍", "🥰",
            "😊", "❤️", "👍", "🎉",
            "😎", "😭", "🔥", "👏",
            "😘", "🙌", "🤔", "💜"
        };

        for (String emoji : emojis) {

            MenuItem item = new MenuItem(emoji);

            item.setOnAction(e ->
                champMessage.appendText(emoji)
            );

            menuEmoji.getItems().add(item);
        }

        boutonEmoji.setOnAction(e ->
            menuEmoji.show(
                boutonEmoji,
                Side.BOTTOM,
                0,
                0
            )
        );

        // ==========================================
        // 12. ENVOI DES MESSAGES PRIVES
        // ==========================================

        boutonEnvoyer.setOnAction(e -> {

            String contenu = champMessage.getText().trim();

            if (destinataireSelectionne[0] == null
                    || contenu.isEmpty()) {
                return;
            }

            String destinataire = destinataireSelectionne[0];

            connexion.envoyerMessage(
                destinataire + "|" + contenu
            );

            Message message = new Message(
                "Moi",
                contenu,
                Message.TypeMessage.TEXTE,
                null
            );

            conversations.computeIfAbsent(
                destinataire,
                nom -> new ArrayList<>()
            ).add(message);

            messages.getItems().add(message);

            champMessage.clear();
        });

        champMessage.setOnAction(
            e -> boutonEnvoyer.fire()
        );

        // ==========================================
        // 13. ENVOI DES QUESTIONS A L'IA
        // ==========================================

        Runnable envoyerQuestionIA = () -> {

            String question = champIA.getText().trim();

            if (question.isEmpty()) {
                return;
            }

            Message questionUtilisateur = new Message(
                "Moi",
                question,
                Message.TypeMessage.TEXTE,
                null
            );

            historiqueIA.add(questionUtilisateur);

            messagesIA.getItems().add(questionUtilisateur);

            champIA.clear();

            statutGeneration.setText(
                "✦ L'assistant IA réfléchit..."
            );

            boutonEnvoyerIA.setDisable(true);
            champIA.setDisable(true);

            try {

                connexion.envoyerQuestionIA(question);

            } catch (Exception ex) {

                statutGeneration.setText(
                    "Erreur lors de l'envoi de la question."
                );

                boutonEnvoyerIA.setDisable(false);
                champIA.setDisable(false);
            }
        };

        boutonEnvoyerIA.setOnAction(
            e -> envoyerQuestionIA.run()
        );

        champIA.setOnAction(
            e -> envoyerQuestionIA.run()
        );

        // ==========================================
        // 14. FENETRE PRINCIPALE
        // ==========================================

        afficherVue(contenuCentral, accueil);

        BorderPane root = new BorderPane();

        root.setLeft(panneauGauche);
        root.setCenter(contenuCentral);

        Scene scene = new Scene(root, 1250, 780);

        stage.setTitle("Messenger | " + pseudo);
        stage.setMinWidth(1000);
        stage.setMinHeight(650);
        stage.setScene(scene);
        stage.centerOnScreen();
        stage.show();

        // ==========================================
        // 15. RECEPTION DES MESSAGES
        // ==========================================

        connexion.demarrerReception(message -> {

            Platform.runLater(() -> {

                // Réponse de l'agent IA
                if (message.startsWith("IA|")) {

                    String reponse = message.substring(3);

                    Message messageIA = new Message(
                        "Assistant IA",
                        reponse,
                        Message.TypeMessage.TEXTE,
                        null
                    );

                    historiqueIA.add(messageIA);

                    messagesIA.getItems().add(messageIA);

                    statutGeneration.setText("");

                    boutonEnvoyerIA.setDisable(false);
                    champIA.setDisable(false);

                    return;
                }

                // Liste des utilisateurs
                if (message.startsWith("USERS|")) {

                    tousContacts.clear();

                    String utilisateurs = message.substring(6);

                    if (!utilisateurs.isEmpty()) {

                        String[] pseudos = utilisateurs.split(",");

                        for (String nom : pseudos) {

                            if (!nom.isEmpty()
                                    && !nom.equalsIgnoreCase(pseudo)
                                    && !tousContacts.contains(nom)) {

                                tousContacts.add(nom);
                            }
                        }
                    }
                }

                // Connexion d'un utilisateur
                else if (message.startsWith("CONNEXION|")) {

                    String nouveauPseudo = message.substring(10);

                    if (destinataireSelectionne[0] != null) {

                        Message notification = new Message(
                            "Système",
                            nouveauPseudo + " vient de se connecter.",
                            Message.TypeMessage.TEXTE,
                            null
                        );

                        messages.getItems().add(notification);
                    }
                }

                // Message privé reçu
                else if (message.startsWith("De ")) {

                    int position = message.indexOf(" : ");

                    if (position != -1) {

                        String expediteur =
                            message.substring(3, position);

                        String contenu =
                            message.substring(position + 3);

                        Message messageRecu = new Message(
                            expediteur,
                            contenu,
                            Message.TypeMessage.TEXTE,
                            null
                        );

                        conversations.computeIfAbsent(
                            expediteur,
                            nom -> new ArrayList<>()
                        ).add(messageRecu);

                        if (expediteur.equals(
                                destinataireSelectionne[0])) {

                            messages.getItems().add(messageRecu);
                        }
                    }
                }

                // Notification système
                else {

                    Message notification = new Message(
                        "Système",
                        message,
                        Message.TypeMessage.TEXTE,
                        null
                    );

                    if (destinataireSelectionne[0] != null) {
                        messages.getItems().add(notification);
                    }
                }
            });

        }, fichierRecu -> {

            Platform.runLater(() -> {

                String expediteur =
                    fichierRecu.getExpediteur();

                String typeFichier =
                    fichierRecu.getTypeFichier();

                Message.TypeMessage typeMessage;

                switch (typeFichier) {

                    case "image":
                        typeMessage = Message.TypeMessage.IMAGE;
                        break;

                    case "audio":
                        typeMessage = Message.TypeMessage.AUDIO;
                        break;

                    case "video":
                        typeMessage = Message.TypeMessage.VIDEO;
                        break;

                    default:
                        typeMessage = Message.TypeMessage.FICHIER;
                        break;
                }

                Message messageFichier = new Message(
                    expediteur,
                    fichierRecu.getNomFichier(),
                    typeMessage,
                    fichierRecu.getFichier()
                );

                conversations.computeIfAbsent(
                    expediteur,
                    nom -> new ArrayList<>()
                ).add(messageFichier);

                if (expediteur.equals(
                        destinataireSelectionne[0])) {

                    messages.getItems().add(messageFichier);
                }

                System.out.println(
                    "Fichier reçu : "
                    + fichierRecu.getFichier().getAbsolutePath()
                );
            });
        });
    }
}
