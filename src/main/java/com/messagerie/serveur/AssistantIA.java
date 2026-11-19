
package com.messagerie.serveur;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.util.List;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

public class AssistantIA {

    private static final String URL_API =
        "https://api.openai.com/v1/responses";

    //Cette constante contient le nom du modèle IA utilisé.
    private static final String MODELE = "gpt-6-luna";

    //Elle commence la création d'un client HTTP.
    private final HttpClient client = HttpClient.newBuilder()
        .connectTimeout(Duration.ofSeconds(30))//Elle définit le délai maximal de connexion à 30 secondes.
        .build();//Elle termine la construction du client HTTP et crée l'objet HttpClient.

    public String repondre(
            String pseudo,
            String question,
            List<JsonObject> historique)
            throws IOException, InterruptedException {
    	//IOException : problème d'entrée/sortie.
    	//InterruptedException : interruption pendant l'attente de la réponse HTTP
        
    	//System.getenv() permet de récupérer une variable d'environnement de Windows
    	String cleAPI = System.getenv("OPENAI_API_KEY");

        if (cleAPI == null || cleAPI.trim().isEmpty()) {
            throw new IOException(
                "La clé API OPENAI_API_KEY n'est pas configurée."
            );
        }

        //Cette instruction crée un tableau JSON vide.
        JsonArray input = new JsonArray();

        // Ajouter les anciennes questions et réponses
        for (JsonObject element : historique) {
            input.add(element);
        }

        // Ajouter la nouvelle question
        JsonObject messageUtilisateur = new JsonObject();
        messageUtilisateur.addProperty("role", "user");
        messageUtilisateur.addProperty("content", question);

        //Tu ajoutes la nouvelle question au tableau JSON.
        input.add(messageUtilisateur);

        JsonObject corps = new JsonObject();

        corps.addProperty("model", MODELE);

        corps.addProperty(
            "instructions",
            "Tu es un assistant IA utile, intelligent et amical. " +
            "Tu réponds aux questions sur tous les sujets. " +
            "Adapte ta langue à celle de l'utilisateur. " +
            "Réponds clairement, naturellement et avec des explications " +
            "adaptées à son niveau. L'utilisateur s'appelle " + pseudo + "."
        );

        corps.add("input", input);

        //Cette propriété limite le nombre maximal de tokens que le modèle peut générer pour sa réponse.
        corps.addProperty("max_output_tokens", 1200);

        HttpRequest requete = HttpRequest.newBuilder()
            .uri(URI.create(URL_API))
            .timeout(Duration.ofSeconds(90))
            .header("Content-Type", "application/json")
            .header("Authorization", "Bearer " + cleAPI)
            .POST(
                HttpRequest.BodyPublishers.ofString(corps.toString())
            )
            .build();

        HttpResponse<String> reponse = client.send(
            requete,
            HttpResponse.BodyHandlers.ofString()
        );

        if (reponse.statusCode() < 200
                || reponse.statusCode() >= 300) {

            System.out.println(
                "Erreur API OpenAI : " + reponse.statusCode()
            );

            throw new IOException(
                "L'API IA a retourné une erreur : "
                + reponse.statusCode()
            );
        }

        JsonObject resultat = JsonParser
            .parseString(reponse.body())
            .getAsJsonObject();

        JsonArray sorties = resultat.getAsJsonArray("output");

        StringBuilder texteReponse = new StringBuilder();

        for (int i = 0; i < sorties.size(); i++) {

            JsonObject element = sorties.get(i).getAsJsonObject();

            if (!element.has("content")) {
                continue;
            }

            JsonArray contenus = element.getAsJsonArray("content");

            for (int j = 0; j < contenus.size(); j++) {

                JsonObject contenu = contenus.get(j).getAsJsonObject();

                if (contenu.has("type")
                        && contenu.get("type").getAsString()
                            .equals("output_text")) {

                    texteReponse.append(
                        contenu.get("text").getAsString()
                    );
                }
            }
        }

        String texte = texteReponse.toString().trim();

        if (texte.isEmpty()) {
            throw new IOException(
                "L'assistant IA n'a retourné aucune réponse."
            );
        }

        // Conserver l'historique pour cette connexion
        historique.add(messageUtilisateur);

        JsonObject messageAssistant = new JsonObject();
        messageAssistant.addProperty("role", "assistant");
        messageAssistant.addProperty("content", texte);

        historique.add(messageAssistant);

        return texte;
    }
}
