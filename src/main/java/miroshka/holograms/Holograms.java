package miroshka.holograms;

import lombok.Getter;
import miroshka.holograms.command.HologramsCommand;
import miroshka.holograms.listener.PlayerListener;
import miroshka.holograms.manager.Config;
import miroshka.holograms.manager.HologramManager;
import miroshka.holograms.service.PlaceholderService;
import org.allaymc.api.entity.interfaces.EntityPlayer;
import org.allaymc.api.plugin.Plugin;
import org.allaymc.api.registry.Registries;
import org.allaymc.api.server.Server;

@Getter
public class Holograms extends Plugin {

    @Getter
    private static Holograms instance;

    private Config config;
    private HologramManager hologramManager;
    private PlaceholderService placeholderService;

    @Override
    public void onLoad() {
        instance = this;
    }

    @Override
    public void onEnable() {
        config = new Config(getPluginContainer().dataFolder().toFile(), getPluginLogger());
        placeholderService = new PlaceholderService(getPluginLogger());
        hologramManager = new HologramManager(this);

        hologramManager.loadHolograms();

        Registries.COMMANDS.register(new HologramsCommand(this));
        Server.getInstance().getEventBus().registerListener(new PlayerListener(this));

        if (config.getUpdateInterval() > 0) {
            Server.getInstance().getScheduler().scheduleRepeating(this, () -> {
                hologramManager.updateAllHolograms();
                return true;
            }, config.getUpdateInterval() * 20);
        }
    }

    @Override
    public void onDisable() {
        if (hologramManager != null) {
            hologramManager.saveHolograms();
            hologramManager.removeAllHolograms();
        }
    }

    public String parsePlaceholders(EntityPlayer player, String text) {
        return placeholderService.parse(player, text);
    }
}
