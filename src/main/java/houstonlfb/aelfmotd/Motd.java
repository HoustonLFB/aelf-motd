package houstonlfb.aelfmotd;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents;

import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.time.LocalDate;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;


public class Motd {

    public static final Logger LOGGER = LoggerFactory.getLogger("aelf-motd");

    // Variable pour stocker la dernière date d'exécution
    private static String lastExecutionDate = null;
    private static String currentMotd = null;

    public static void motd() {
        // Register an event handler that runs at the end of each server tick
        ServerTickEvents.END_SERVER_TICK.register(server -> {
            // Vérifier si le code s'exécute sur le serveur (pas côté client)

            // Récupérer la date actuelle
            String currentDate = currentDate();

            if (currentMotd == null) {
                currentMotd = server.getServerMotd();
            }

            // Vérifier si la date a changé depuis la dernière exécution
            if (lastExecutionDate == null || !lastExecutionDate.equals(currentDate)) {
                // Mettre à jour la date de la dernière exécution
                lastExecutionDate = currentDate;

                // Exécuter le code souhaité
                try {
                    StringBuilder content = apiCall();

                    // Convertir le contenu en chaîne
                    String jsonResponse = content.toString();

                    // Extraction manuelle des données JSON
                    String couleur = extractValue(jsonResponse, "couleur");
                    String jourLiturgiqueNom = extractValue(jsonResponse, "jour_liturgique_nom");
                    String fete = extractValue(jsonResponse, "fete");

                    assert jourLiturgiqueNom != null;
                    assert couleur != null;
                    assert fete != null;

                    jourLiturgiqueNom = decodeUnicodeEscapes(jourLiturgiqueNom);
                    fete = decodeUnicodeEscapes(fete);

                    // Afficher les informations récupérées
                    LOGGER.info("Couleur liturgique : " + couleur);
                    LOGGER.info("Nom liturgique du jour : " + jourLiturgiqueNom);
                    LOGGER.info("Fete : " + fete);

                    String motd = currentMotd + "\n" + couleur(couleur);

                    if (jourLiturgiqueNom.equals("de la férie")) {
                        motd += fete;
                    } else {
                        motd += jourLiturgiqueNom;
                    }

                    server.setMotd(motd);

                } catch (Exception e) {
                    LOGGER.error(e.getMessage());
                }
            }
        });
    }

    private static String couleur(String couleur) {
        return switch (couleur) {
            case "rouge" -> "§4";
            case "violet" -> "§5";
            case "rose" -> "§d";
            case "bleu" -> "§9";
            case "vert" -> "§2";
            default -> "§f";
        };
    }

    private static String extractValue(String json, String key) {
        // Trouver la position de la clé
        int startIndex = json.indexOf("\"" + key + "\":");
        if (startIndex == -1) {
            return null; // Clé non trouvée
        }

        // Avancer jusqu'à la valeur
        startIndex = json.indexOf(":", startIndex) + 1;

        // Sauter les espaces blancs
        while (startIndex < json.length() && json.charAt(startIndex) == ' ') {
            startIndex++;
        }

        // Vérifier si la valeur est une chaîne (commence par un guillemet)
        if (json.charAt(startIndex) == '"') {
            startIndex++; // Sauter le guillemet d'ouverture
            int endIndex = json.indexOf('"', startIndex); // Trouver le guillemet de fermeture

            // Retourner la chaîne de caractères extraite
            return json.substring(startIndex, endIndex);
        } else {
            // Si ce n'est pas une chaîne, trouver la fin de la valeur (délimitée par une virgule ou la fin de l'objet)
            int endIndex = startIndex;
            while (endIndex < json.length() && json.charAt(endIndex) != ',' && json.charAt(endIndex) != '}') {
                endIndex++;
            }

            // Retourner la valeur extraite
            return json.substring(startIndex, endIndex).trim();
        }
    }

    public static String decodeUnicodeEscapes(String input) {
        StringBuilder output = new StringBuilder();
        int length = input.length();
        for (int i = 0; i < length; i++) {
            char ch = input.charAt(i);
            if (ch == '\\' && i + 1 < length && input.charAt(i + 1) == 'u') {
                // Find the Unicode escape sequence
                if (i + 5 < length) {
                    String unicode = input.substring(i + 2, i + 6);
                    try {
                        // Convert it to the actual character
                        char unicodeChar = (char) Integer.parseInt(unicode, 16);
                        output.append(unicodeChar);
                        i += 5; // Skip past the Unicode escape sequence
                    } catch (NumberFormatException e) {
                        output.append(ch); // Append back the original characters if parsing fails
                    }
                } else {
                    output.append(ch); // Append back the original characters if it's not a full sequence
                }
            } else {
                output.append(ch);
            }
        }
        return output.toString();
    }

    private static String currentDate() {

        LocalDate currentDate = LocalDate.now();
        return currentDate.toString();
    }

    private static StringBuilder apiCall() throws IOException {

        String currentDate = currentDate();
        // URL de l'API
        String url = "https://api.aelf.org/v1/informations/" + currentDate + "/romain";

        // Créer une connexion HTTP
        HttpURLConnection httpClient = (HttpURLConnection) new URL(url).openConnection();

        // Définir la méthode de requête
        httpClient.setRequestMethod("GET");
        httpClient.setRequestProperty("Accept", "*/*");

        // Lire la réponse
        BufferedReader in = new BufferedReader(new InputStreamReader(httpClient.getInputStream(), StandardCharsets.UTF_8));
        String inputLine;
        StringBuilder content = new StringBuilder();
        while ((inputLine = in.readLine()) != null) {
            content.append(inputLine);
        }

        // Fermer les flux
        in.close();
        httpClient.disconnect();

        return content;
    }
}
