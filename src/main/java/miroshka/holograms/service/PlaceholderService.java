package miroshka.holograms.service;

import lombok.Getter;
import org.allaymc.api.entity.interfaces.EntityPlayer;
import org.allaymc.api.server.Server;
import org.slf4j.Logger;

import java.lang.reflect.Method;

public class PlaceholderService {

    @Getter
    private final boolean enabled;
    private Object apiInstance;
    private Method setPlaceholdersMethod;

    public PlaceholderService(Logger logger) {
        boolean available = Server.getInstance().getPluginManager().getPlugin("PlaceholderAPI") != null;

        if (available) {
            try {
                Class<?> papiClass = Class.forName("org.allaymc.papi.PlaceholderAPI");
                apiInstance = papiClass.getMethod("getAPI").invoke(null);
                setPlaceholdersMethod = apiInstance.getClass().getMethod("setPlaceholders", EntityPlayer.class,
                        String.class);
                logger.info("PlaceholderAPI found, placeholders are enabled");
            } catch (Exception e) {
                available = false;
                logger.warn("Failed to initialize PlaceholderAPI integration");
            }
        }

        this.enabled = available;
    }

    public String parse(EntityPlayer player, String text) {
        if (!enabled || player == null || text == null) {
            return text;
        }

        try {
            return (String) setPlaceholdersMethod.invoke(apiInstance, player, text);
        } catch (Exception e) {
            return text;
        }
    }
}
