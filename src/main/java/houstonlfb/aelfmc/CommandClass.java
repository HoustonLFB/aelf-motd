package houstonlfb.aelfmc;

import net.fabricmc.fabric.api.command.v2.CommandRegistrationCallback;
import net.minecraft.server.command.CommandManager;
import net.minecraft.text.Text;

import java.io.IOException;

public class CommandClass {

    public static void aelfCommand() {
        try { // TRY CATCH DE CON QUI REND LA COMMANDE PAS JOLI
            CommandRegistrationCallback.EVENT.register((dispatcher, registryAccess, environment) -> {
                dispatcher.register(CommandManager.literal("aelf").executes(context -> {
                    context.getSource().sendFeedback(() -> {
                        try {
                            return Text.literal(chatFormat());
                        } catch (IOException e) {
                            throw new RuntimeException(e);
                        }
                    }, false);
                    return 1;
                }));
            });
        } catch (Exception e) {
            throw new RuntimeException(e);
        };
    }

    public static String chatFormat() throws IOException {
        String data = Motd.apiCall().toString();

        String semaine = Motd.decodeUnicodeEscapes(Motd.extractValue(data, "semaine"));
        String couleur = Motd.decodeUnicodeEscapes(Motd.extractValue(data, "couleur"));
        String jour = Motd.decodeUnicodeEscapes(Motd.extractValue(data, "jour"));
        String fete = Motd.decodeUnicodeEscapes(Motd.extractValue(data, "fete"));
        String jour_liturgique_nom = Motd.decodeUnicodeEscapes(Motd.extractValue(data, "jour_liturgique_nom"));

        String couleurCode = Motd.couleur(couleur);

        String chat;
        if(jour_liturgique_nom.equals("de la férie")) {
            chat = couleurCode + semaine + "\n" + jour + " " + jour_liturgique_nom + "\n" + fete;
        } else {
            if(fete.equals("")) {
            chat = couleurCode + jour_liturgique_nom + "\n" + fete;
        } else {
                chat = couleurCode + jour_liturgique_nom;
            }
        }

        return chat;
    }
}
