package miroshka.holograms.listener;

import lombok.RequiredArgsConstructor;
import miroshka.holograms.Holograms;
import org.allaymc.api.entity.interfaces.EntityPlayer;
import org.allaymc.api.eventbus.EventHandler;
import org.allaymc.api.eventbus.event.entity.EntityTeleportEvent;
import org.allaymc.api.eventbus.event.server.PlayerJoinEvent;
import org.allaymc.api.eventbus.event.server.PlayerQuitEvent;
import org.allaymc.api.player.Player;
import org.allaymc.api.server.Server;
import org.allaymc.api.world.Dimension;

@RequiredArgsConstructor
public class PlayerListener {

    private final Holograms plugin;

    @EventHandler
    public void onPlayerJoin(PlayerJoinEvent event) {
        Player player = event.getPlayer();
        Server.getInstance().getScheduler().scheduleDelayed(plugin, () -> {
            plugin.getHologramManager().onPlayerJoin(player);
            return true;
        }, 20);
    }

    @EventHandler
    public void onPlayerQuit(PlayerQuitEvent event) {
        plugin.getHologramManager().onPlayerQuit(event.getPlayer());
    }

    @EventHandler
    public void onEntityTeleport(EntityTeleportEvent event) {
        if (!(event.getEntity() instanceof EntityPlayer entityPlayer)) {
            return;
        }

        Player player = entityPlayer.getController();
        if (player == null) {
            return;
        }

        Dimension fromDimension = event.getFrom().dimension();
        Dimension toDimension = event.getTo().dimension();

        if (fromDimension != toDimension) {
            Server.getInstance().getScheduler().scheduleDelayed(plugin, () -> {
                plugin.getHologramManager().onPlayerChangeDimension(player, fromDimension, toDimension);
                return true;
            }, 5);
        }
    }
}
