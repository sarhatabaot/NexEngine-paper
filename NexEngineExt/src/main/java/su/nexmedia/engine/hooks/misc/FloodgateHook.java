package su.nexmedia.engine.hooks.misc;

import org.geysermc.floodgate.api.FloodgateApi;

import java.util.UUID;

public class FloodgateHook {

    public static boolean isFloodgatePlayer(UUID uuid) {
        return FloodgateApi.getInstance().isFloodgatePlayer(uuid);
    }

}
