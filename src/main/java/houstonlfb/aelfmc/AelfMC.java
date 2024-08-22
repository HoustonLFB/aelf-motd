package houstonlfb.aelfmc;

import net.fabricmc.api.DedicatedServerModInitializer;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class AelfMC implements DedicatedServerModInitializer {

    public static final Logger LOGGER = LoggerFactory.getLogger("aelfmc");


    @Override
    public void onInitializeServer() {

        LOGGER.info("AELF MOTD starting...");

        CommandClass.aelfCommand();
        Motd.motd();


    }
}
