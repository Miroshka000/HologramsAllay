package miroshka.holograms.ui;

import miroshka.holograms.Holograms;
import miroshka.holograms.data.HologramData;
import miroshka.holograms.util.MessageUtils;
import org.allaymc.api.entity.interfaces.EntityPlayer;
import org.allaymc.api.form.Forms;
import org.allaymc.api.math.location.Location3dc;
import org.allaymc.api.player.Player;
import org.allaymc.api.utils.TextFormat;

import java.util.ArrayList;
import java.util.List;

public final class HologramForms {

    private HologramForms() {
    }

    public static void openCreateForm(Holograms plugin, EntityPlayer entityPlayer) {
        Player player = entityPlayer.getController();
        if (player == null) {
            return;
        }

        Location3dc loc = entityPlayer.getLocation();

        Forms.custom()
                .title(MessageUtils.tr(player, "form.create.title"))
                .input(MessageUtils.tr(player, "form.create.name"), MessageUtils.tr(player, "form.create.name_placeholder"))
                .input(MessageUtils.tr(player, "form.create.lines"), MessageUtils.tr(player, "form.create.lines_placeholder"))
                .toggle(MessageUtils.tr(player, "form.create.multiline"), true)
                .slider(MessageUtils.tr(player, "form.create.update_interval"), -1, 60, 1, -1)
                .slider(MessageUtils.tr(player, "form.create.line_spacing"), 0.1f, 1.0f, 1, 0.25f)
                .toggle(MessageUtils.tr(player, "form.create.use_current_pos"), true)
                .input(MessageUtils.tr(player, "form.create.x"), "0", String.format("%.2f", loc.x()))
                .input(MessageUtils.tr(player, "form.create.y"), "0", String.format("%.2f", loc.y()))
                .input(MessageUtils.tr(player, "form.create.z"), "0", String.format("%.2f", loc.z()))
                .onResponse(responses -> handleCreateFormResponse(plugin, entityPlayer, player, loc, responses))
                .sendTo(player);
    }

    private static void handleCreateFormResponse(Holograms plugin, EntityPlayer entityPlayer, Player player,
                                                   Location3dc loc, List<String> responses) {
        String name = responses.get(0);
        String linesRaw = responses.get(1);
        boolean multiLine = "true".equalsIgnoreCase(responses.get(2));
        int updateInterval = parseIntSafe(responses.get(3), -1);
        double lineSpacing = parseDoubleSafe(responses.get(4), 0.25);
        boolean useCurrentPos = "true".equalsIgnoreCase(responses.get(5));

        double x, y, z;
        if (useCurrentPos) {
            x = loc.x();
            y = loc.y();
            z = loc.z();
        } else {
            x = parseDoubleSafe(responses.get(6), loc.x());
            y = parseDoubleSafe(responses.get(7), loc.y());
            z = parseDoubleSafe(responses.get(8), loc.z());
        }

        if (name == null || name.trim().isEmpty()) {
            MessageUtils.send(player, "form.error.empty_name");
            return;
        }

        List<String> lines = parseLines(linesRaw);

        HologramData data = HologramData.builder()
                .id(name.trim())
                .world(entityPlayer.getDimension().getWorld().getName())
                .x(x)
                .y(y)
                .z(z)
                .lines(lines)
                .multiLine(multiLine)
                .updateInterval(updateInterval)
                .lineSpacing(lineSpacing)
                .build();

        if (plugin.getHologramManager().createHologram(data)) {
            MessageUtils.send(player, "hologram.created", name);
        } else {
            MessageUtils.send(player, "hologram.already_exists", name);
        }
    }

    public static void openEditForm(Holograms plugin, EntityPlayer entityPlayer, HologramData data) {
        Player player = entityPlayer.getController();
        if (player == null) {
            return;
        }

        String currentLines = String.join("\\n", data.getLines());

        Forms.custom()
                .title(MessageUtils.tr(player, "form.edit.title") + " - " + data.getId())
                .input(MessageUtils.tr(player, "form.create.lines"), MessageUtils.tr(player, "form.create.lines_placeholder"), currentLines)
                .toggle(MessageUtils.tr(player, "form.create.multiline"), data.isMultiLine())
                .slider(MessageUtils.tr(player, "form.create.update_interval"), -1, 60, 1, data.getUpdateInterval())
                .slider(MessageUtils.tr(player, "form.create.line_spacing"), 0.1f, 1.0f, 1, (float) data.getLineSpacing())
                .input(MessageUtils.tr(player, "form.create.x"), "0", String.format("%.2f", data.getX()))
                .input(MessageUtils.tr(player, "form.create.y"), "0", String.format("%.2f", data.getY()))
                .input(MessageUtils.tr(player, "form.create.z"), "0", String.format("%.2f", data.getZ()))
                .onResponse(responses -> handleEditFormResponse(plugin, entityPlayer, player, data, responses))
                .sendTo(player);
    }

    private static void handleEditFormResponse(Holograms plugin, EntityPlayer entityPlayer, Player player,
                                                 HologramData data, List<String> responses) {
        String linesRaw = responses.get(0);
        boolean multiLine = "true".equalsIgnoreCase(responses.get(1));
        int updateInterval = parseIntSafe(responses.get(2), -1);
        double lineSpacing = parseDoubleSafe(responses.get(3), 0.25);
        double x = parseDoubleSafe(responses.get(4), data.getX());
        double y = parseDoubleSafe(responses.get(5), data.getY());
        double z = parseDoubleSafe(responses.get(6), data.getZ());

        List<String> lines = parseLines(linesRaw);

        HologramData updatedData = HologramData.builder()
                .id(data.getId())
                .world(data.getWorld())
                .x(x)
                .y(y)
                .z(z)
                .lines(lines)
                .localizedLines(data.getLocalizedLines())
                .multiLine(multiLine)
                .updateInterval(updateInterval)
                .lineSpacing(lineSpacing)
                .build();

        if (plugin.getHologramManager().updateHologram(updatedData)) {
            MessageUtils.send(player, "hologram.updated", data.getId());
        }
    }

    public static void openEditSelectForm(Holograms plugin, EntityPlayer entityPlayer) {
        Player player = entityPlayer.getController();
        if (player == null) {
            return;
        }

        var holograms = plugin.getHologramManager().getAllHolograms();

        if (holograms.isEmpty()) {
            MessageUtils.send(player, "list.empty");
            return;
        }

        var form = Forms.simple()
                .title(MessageUtils.tr(player, "form.edit.select_title"))
                .content(MessageUtils.tr(player, "form.edit.select_content"));

        for (HologramData hologramData : holograms) {
            String buttonText = TextFormat.YELLOW + hologramData.getId() + TextFormat.GRAY +
                    String.format(" (%.1f, %.1f, %.1f)", hologramData.getX(), hologramData.getY(), hologramData.getZ());
            form.button(buttonText).onClick(btn -> openEditForm(plugin, entityPlayer, hologramData));
        }

        form.sendTo(player);
    }

    public static void openDeleteSelectForm(Holograms plugin, EntityPlayer entityPlayer) {
        Player player = entityPlayer.getController();
        if (player == null) {
            return;
        }

        var holograms = plugin.getHologramManager().getAllHolograms();

        if (holograms.isEmpty()) {
            MessageUtils.send(player, "list.empty");
            return;
        }

        var form = Forms.simple()
                .title(MessageUtils.tr(player, "form.delete.select_title"))
                .content(MessageUtils.tr(player, "form.delete.select_content"));

        for (HologramData hologramData : holograms) {
            String buttonText = TextFormat.RED + hologramData.getId() + TextFormat.GRAY +
                    String.format(" (%.1f, %.1f, %.1f)", hologramData.getX(), hologramData.getY(), hologramData.getZ());
            form.button(buttonText).onClick(btn -> openDeleteConfirmForm(plugin, entityPlayer, hologramData));
        }

        form.sendTo(player);
    }

    public static void openDeleteConfirmForm(Holograms plugin, EntityPlayer entityPlayer, HologramData data) {
        Player player = entityPlayer.getController();
        if (player == null) {
            return;
        }

        Forms.modal()
                .title(MessageUtils.tr(player, "form.delete.confirm_title"))
                .content(MessageUtils.tr(player, "form.delete.confirm_content", data.getId()))
                .trueButton(MessageUtils.tr(player, "form.delete.confirm_yes"), () -> {
                    if (plugin.getHologramManager().deleteHologram(data.getId())) {
                        MessageUtils.send(player, "hologram.deleted", data.getId());
                    }
                })
                .falseButton(MessageUtils.tr(player, "form.delete.confirm_no"), () -> {})
                .sendTo(player);
    }

    public static void openLocaleManageForm(Holograms plugin, EntityPlayer entityPlayer, HologramData data) {
        Player player = entityPlayer.getController();
        if (player == null) {
            return;
        }

        List<String> availableLocales = data.getAvailableLocales();
        String localesText = availableLocales.isEmpty()
                ? MessageUtils.tr(player, "form.locale.default_text")
                : String.join(", ", availableLocales);

        var form = Forms.simple()
                .title(MessageUtils.tr(player, "form.locale.title") + " - " + data.getId())
                .content(MessageUtils.tr(player, "form.locale.available", localesText));

        form.button(TextFormat.GREEN + MessageUtils.tr(player, "form.locale.add"))
                .onClick(btn -> openAddLocaleForm(plugin, entityPlayer, data));

        for (String locale : availableLocales) {
            String buttonText = TextFormat.YELLOW + locale + " " + TextFormat.GRAY +
                    MessageUtils.tr(player, "form.locale.edit_button");
            form.button(buttonText).onClick(btn -> openEditLocaleForm(plugin, entityPlayer, data, locale));
        }

        form.button(MessageUtils.tr(player, "form.button.back"))
                .onClick(btn -> openEditForm(plugin, entityPlayer, data));

        form.sendTo(player);
    }

    public static void openAddLocaleForm(Holograms plugin, EntityPlayer entityPlayer, HologramData data) {
        Player player = entityPlayer.getController();
        if (player == null) {
            return;
        }

        List<String> supportedLocales = plugin.getConfig().getSupportedLocales();
        List<String> availableToAdd = new ArrayList<>();
        for (String locale : supportedLocales) {
            if (!data.hasLocalizedLines(locale)) {
                availableToAdd.add(locale);
            }
        }

        if (availableToAdd.isEmpty()) {
            MessageUtils.send(player, "form.locale.all_added");
            return;
        }

        Forms.custom()
                .title(MessageUtils.tr(player, "form.locale.add_title"))
                .dropdown(MessageUtils.tr(player, "form.locale.select_locale"), availableToAdd)
                .input(MessageUtils.tr(player, "form.create.lines"), MessageUtils.tr(player, "form.create.lines_placeholder"),
                        String.join("\\n", data.getLines()))
                .onResponse(responses -> {
                    int localeIndex = parseIntSafe(responses.get(0), 0);
                    String locale = availableToAdd.get(localeIndex);
                    String linesRaw = responses.get(1);
                    List<String> lines = parseLines(linesRaw);

                    data.setLinesForLocale(locale, lines);
                    plugin.getHologramManager().updateHologram(data);
                    MessageUtils.send(player, "hologram.updated", data.getId());
                    openLocaleManageForm(plugin, entityPlayer, data);
                })
                .sendTo(player);
    }

    public static void openEditLocaleForm(Holograms plugin, EntityPlayer entityPlayer, HologramData data,
                                           String locale) {
        Player player = entityPlayer.getController();
        if (player == null) {
            return;
        }

        List<String> currentLines = data.getLinesForLocale(locale);

        Forms.custom()
                .title(MessageUtils.tr(player, "form.locale.edit") + " - " + locale)
                .input(MessageUtils.tr(player, "form.create.lines"), MessageUtils.tr(player, "form.create.lines_placeholder"),
                        String.join("\\n", currentLines))
                .toggle(TextFormat.RED + MessageUtils.tr(player, "form.locale.remove"), false)
                .onResponse(responses -> {
                    boolean remove = "true".equalsIgnoreCase(responses.get(1));

                    if (remove) {
                        data.removeLinesForLocale(locale);
                    } else {
                        String linesRaw = responses.get(0);
                        List<String> lines = parseLines(linesRaw);
                        data.setLinesForLocale(locale, lines);
                    }

                    plugin.getHologramManager().updateHologram(data);
                    MessageUtils.send(player, "hologram.updated", data.getId());
                    openLocaleManageForm(plugin, entityPlayer, data);
                })
                .sendTo(player);
    }

    private static List<String> parseLines(String raw) {
        if (raw == null || raw.isEmpty()) {
            return new ArrayList<>();
        }
        String[] split = raw.split("\\\\n");
        List<String> lines = new ArrayList<>();
        for (String line : split) {
            lines.add(line.replace("&", "§"));
        }
        return lines;
    }

    private static int parseIntSafe(String value, int defaultValue) {
        try {
            return (int) Float.parseFloat(value);
        } catch (NumberFormatException e) {
            return defaultValue;
        }
    }

    private static double parseDoubleSafe(String value, double defaultValue) {
        try {
            return Double.parseDouble(value);
        } catch (NumberFormatException e) {
            return defaultValue;
        }
    }
}
