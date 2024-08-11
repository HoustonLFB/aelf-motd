package houstonlfb.aelfmotd;

import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.time.LocalDate;

import org.json.JSONObject;

public class AELFMOTD implements ModInitializer {
	// This logger is used to write text to the console and the log file.
	// It is considered best practice to use your mod id as the logger's name.
	// That way, it's clear which mod wrote info, warnings, and errors.
	public static final Logger LOGGER = LoggerFactory.getLogger("aelf-motd");

	// Variable pour stocker la dernière date d'exécution
	private String lastExecutionDate = null;

	@Override
	public void onInitialize() {
		// This code runs as soon as Minecraft is in a mod-load-ready state.
		// However, some things (like resources) may still be uninitialized.
		// Proceed with mild caution.

		LOGGER.info("AELF MOTD starting...");

		// Register an event handler that runs at the end of each server tick
		ServerTickEvents.END_SERVER_TICK.register(server -> {
			// Vérifier si le code s'exécute sur le serveur (pas côté client)
			if (server.isRemote()) {
				// Récupérer la date actuelle
				String currentDate = currentDate();

				// Vérifier si la date a changé depuis la dernière exécution
				if (lastExecutionDate == null || !lastExecutionDate.equals(currentDate)) {
					// Mettre à jour la date de la dernière exécution
					lastExecutionDate = currentDate;

					// Exécuter le code souhaité
					try {
						StringBuilder content = apiCall();


						// Parser le contenu JSON
						JSONObject responseJson = new JSONObject(content.toString());
						JSONObject informations = responseJson.getJSONObject("informations");

						// Récupérer les données souhaitées
						String couleur = informations.getString("couleur");
						String jourLiturgiqueNom = informations.getString("jour_liturgique_nom");

						// Afficher les informations récupérées
						LOGGER.info("Couleur liturgique : " + couleur);
						LOGGER.info("Nom liturgique du jour : " + jourLiturgiqueNom);

						String motd = server.getServerMotd() + "\n" + couleur(couleur) + jourLiturgiqueNom;

						// Vous pouvez aussi mettre à jour le motd ou d'autres fonctionnalités ici
						server.setMotd(motd);

						LOGGER.info("Motd mis à jour");

					} catch (Exception e) {
						LOGGER.error(e.getMessage());
					}
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
