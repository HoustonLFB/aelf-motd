package houstonlfb.aelfmotd;

import net.fabricmc.fabric.api.command.v2.CommandRegistrationCallback;
import net.minecraft.server.command.CommandManager;
import net.minecraft.text.Text;

public class CommandClass {

    public static void aelfCommand() {
        CommandRegistrationCallback.EVENT.register((dispatcher, registryAccess, environment) -> {
            dispatcher.register(CommandManager.literal("aelf").executes(context -> {
                context.getSource().sendFeedback(() -> Text.literal(""), false);
                return 1;
            }));
        });
    }
}
