package houstonlfb.aelfmotd;

import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.time.LocalDate;

public class AELFMOTD implements ModInitializer {
	// This logger is used to write text to the console and the log file.
	// It is considered best practice to use your mod id as the logger's name.
	// That way, it's clear which mod wrote info, warnings, and errors.
	public static final Logger LOGGER = LoggerFactory.getLogger("aelf-motd");

	@Override
	public void onInitialize() {
		// This code runs as soon as Minecraft is in a mod-load-ready state.
		// However, some things (like resources) may still be uninitialized.
		// Proceed with mild caution.

		LOGGER.info("Hello AELF !");

		// Register an event handler that runs at the end of each server tick
		ServerTickEvents.START_SERVER_TICK.register(server -> {
			if (server.isRemote()) {
				// Execute the following code on the server side only
				try {

					LocalDate currentDate = LocalDate.now();

					// Convertir la date en chaîne de caractères au format ISO 8601
					String isoDate = currentDate.toString();

					// URL de l'API
					String url = "https://api.aelf.org/v1/informations/" + isoDate + "/romain";

					// Créer une connexion HTTP
					HttpURLConnection httpClient = (HttpURLConnection) new URL(url).openConnection();

					// Définir la méthode de requête
					httpClient.setRequestMethod("GET");
					httpClient.setRequestProperty("Accept", "*/*");

					// Lire la réponse
					BufferedReader in = new BufferedReader(new InputStreamReader(httpClient.getInputStream()));
					String inputLine;
					StringBuilder content = new StringBuilder();
					while ((inputLine = in.readLine()) != null) {
						content.append(inputLine);
					}

					// Fermer les flux
					in.close();
					httpClient.disconnect();

					// Convertir le contenu en chaîne
					String jsonResponse = content.toString();

					// Extraction manuelle des données JSON
					String couleur = extractValue(jsonResponse, "couleur");
					String jourLiturgiqueNom = extractValue(jsonResponse, "jour_liturgique_nom");

					// Afficher les informations récupérées
					LOGGER.info("Couleur liturgique : " + couleur);
					LOGGER.info("Nom liturgique du jour : " + jourLiturgiqueNom);

					String motd = server.getServerMotd() + "\n" + couleur(couleur) + jourLiturgiqueNom;

					// Vous pouvez aussi mettre à jour le motd ou d'autres fonctionnalités ici
					server.setMotd(motd);

				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		});
	}

	private static String couleur(String couleur) {
		switch (couleur) {
			case "rouge":
				return "§4";
			case "violet":
				return "§5";
			case "rose":
				return "§d";
			case "bleu":
				return "§9";
			default:
				return "§f";
		}
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

}
