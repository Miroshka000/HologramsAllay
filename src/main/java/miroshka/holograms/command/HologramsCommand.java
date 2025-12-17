package miroshka.holograms.command;

import miroshka.holograms.Holograms;
import miroshka.holograms.data.HologramData;
import miroshka.holograms.ui.HologramForms;
import miroshka.holograms.util.MessageUtils;
import org.allaymc.api.command.Command;
import org.allaymc.api.command.SenderType;
import org.allaymc.api.command.tree.CommandTree;
import org.allaymc.api.math.location.Location3d;
import org.allaymc.api.player.Player;

public class HologramsCommand extends Command {

    private final Holograms plugin;

    public HologramsCommand(Holograms plugin) {
        super("holograms", "holograms:command.description", "holograms.admin");
        this.plugin = plugin;
        this.aliases.add("holo");
        this.aliases.add("hd");
    }

    @Override
    public void prepareCommandTree(CommandTree tree) {
        tree.getRoot()
                .key("create")
                .exec((context, entityPlayer) -> {
                    HologramForms.openCreateForm(plugin, entityPlayer);
                    return context.success();
                }, SenderType.PLAYER)
                .root()
                .key("edit")
                .str("name").optional()
                .exec((context, entityPlayer) -> {
                    String name = context.getResult(1);
                    Player player = entityPlayer.getController();
                    if (name == null || name.isEmpty()) {
                        var nearest = plugin.getHologramManager().getNearestHologram(entityPlayer,
                                plugin.getConfig().getMaxHologramDistance());
                        if (nearest.isEmpty()) {
                            MessageUtils.send(player, "hologram.no_nearby");
                            return context.fail();
                        }
                        HologramForms.openEditForm(plugin, entityPlayer, nearest.get());
                    } else {
                        var hologram = plugin.getHologramManager().getHologram(name);
                        if (hologram.isEmpty()) {
                            MessageUtils.send(player, "hologram.not_found", name);
                            return context.fail();
                        }
                        HologramForms.openEditForm(plugin, entityPlayer, hologram.get());
                    }
                    return context.success();
                }, SenderType.PLAYER)
                .root()
                .key("delete")
                .str("name").optional()
                .exec((context, entityPlayer) -> {
                    String name = context.getResult(1);
                    Player player = entityPlayer.getController();
                    if (name == null || name.isEmpty()) {
                        HologramForms.openDeleteSelectForm(plugin, entityPlayer);
                    } else {
                        var hologram = plugin.getHologramManager().getHologram(name);
                        if (hologram.isEmpty()) {
                            MessageUtils.send(player, "hologram.not_found", name);
                            return context.fail();
                        }
                        HologramForms.openDeleteConfirmForm(plugin, entityPlayer, hologram.get());
                    }
                    return context.success();
                }, SenderType.PLAYER)
                .root()
                .key("list")
                .exec((context, entityPlayer) -> {
                    Player player = entityPlayer.getController();
                    var holograms = plugin.getHologramManager().getAllHolograms();
                    if (holograms.isEmpty()) {
                        MessageUtils.send(player, "list.empty");
                        return context.success();
                    }

                    MessageUtils.send(player, "list.header");
                    for (HologramData data : holograms) {
                        MessageUtils.send(player, "list.entry",
                                data.getId(),
                                String.format("%.1f", data.getX()),
                                String.format("%.1f", data.getY()),
                                String.format("%.1f", data.getZ()));
                    }
                    return context.success();
                }, SenderType.PLAYER)
                .root()
                .key("reload")
                .exec((context, entityPlayer) -> {
                    Player player = entityPlayer.getController();
                    plugin.getHologramManager().reloadHolograms();
                    MessageUtils.send(player, "reload.success");
                    return context.success();
                }, SenderType.PLAYER)
                .root()
                .key("locale")
                .str("name")
                .exec((context, entityPlayer) -> {
                    String name = context.getResult(1);
                    Player player = entityPlayer.getController();

                    var hologram = plugin.getHologramManager().getHologram(name);
                    if (hologram.isEmpty()) {
                        MessageUtils.send(player, "hologram.not_found", name);
                        return context.fail();
                    }
                    HologramForms.openLocaleManageForm(plugin, entityPlayer, hologram.get());
                    return context.success();
                }, SenderType.PLAYER)
                .root()
                .key("tp")
                .str("name")
                .exec((context, entityPlayer) -> {
                    String name = context.getResult(1);
                    Player player = entityPlayer.getController();
                    var hologram = plugin.getHologramManager().getHologram(name);
                    if (hologram.isEmpty()) {
                        MessageUtils.send(player, "hologram.not_found", name);
                        return context.fail();
                    }
                    HologramData data = hologram.get();
                    entityPlayer.teleport(new Location3d(
                            data.getX(), data.getY(), data.getZ(),
                            0, 0,
                            entityPlayer.getDimension()));
                    MessageUtils.send(player, "hologram.teleported", name);
                    return context.success();
                }, SenderType.PLAYER);
    }
}
