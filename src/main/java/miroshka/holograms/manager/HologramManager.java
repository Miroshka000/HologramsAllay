package miroshka.holograms.manager;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;
import lombok.Getter;
import miroshka.holograms.Holograms;
import miroshka.holograms.data.HologramData;
import org.allaymc.api.debugshape.DebugText;
import org.allaymc.api.entity.interfaces.EntityPlayer;
import org.allaymc.api.math.location.Location3d;
import org.allaymc.api.player.Player;
import org.allaymc.api.server.Server;
import org.allaymc.api.world.Dimension;
import org.joml.Vector3f;

import java.awt.Color;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.lang.reflect.Type;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

@Getter
public class HologramManager {

    private static final Gson GSON = new GsonBuilder()
            .setPrettyPrinting()
            .disableHtmlEscaping()
            .create();
    private static final Type HOLOGRAM_MAP_TYPE = new TypeToken<Map<String, HologramData>>() {
    }.getType();

    private final Holograms plugin;
    private final File hologramsFile;
    private final Map<String, HologramData> holograms = new ConcurrentHashMap<>();
    private final Map<UUID, Map<String, List<DebugText>>> playerHolograms = new ConcurrentHashMap<>();

    public HologramManager(Holograms plugin) {
        this.plugin = plugin;
        this.hologramsFile = new File(plugin.getPluginContainer().dataFolder().toFile(), "holograms.json");
    }

    public void loadHolograms() {
        if (!hologramsFile.exists()) {
            return;
        }

        try (FileReader reader = new FileReader(hologramsFile, StandardCharsets.UTF_8)) {
            Map<String, HologramData> loaded = GSON.fromJson(reader, HOLOGRAM_MAP_TYPE);
            if (loaded != null) {
                holograms.putAll(loaded);
            }
        } catch (IOException e) {
            plugin.getPluginLogger().error("Failed to load holograms", e);
        }
    }

    public void saveHolograms() {
        hologramsFile.getParentFile().mkdirs();
        try (FileWriter writer = new FileWriter(hologramsFile, StandardCharsets.UTF_8)) {
            GSON.toJson(holograms, writer);
        } catch (IOException e) {
            plugin.getPluginLogger().error("Failed to save holograms", e);
        }
    }

    public boolean createHologram(HologramData data) {
        if (holograms.containsKey(data.getId())) {
            return false;
        }

        holograms.put(data.getId(), data);
        saveHolograms();
        spawnHologramForAllPlayers(data);
        return true;
    }

    public boolean deleteHologram(String id) {
        HologramData data = holograms.remove(id);
        if (data == null) {
            return false;
        }

        despawnHologramForAllPlayers(id);
        saveHolograms();
        return true;
    }

    public boolean updateHologram(HologramData data) {
        if (!holograms.containsKey(data.getId())) {
            return false;
        }

        HologramData oldData = holograms.get(data.getId());
        holograms.put(data.getId(), data);

        boolean linesCountChanged = oldData.getLines().size() != data.getLines().size();
        boolean multiLineChanged = oldData.isMultiLine() != data.isMultiLine();
        boolean lineSpacingChanged = oldData.getLineSpacing() != data.getLineSpacing();

        if (linesCountChanged || multiLineChanged || lineSpacingChanged) {
            despawnHologramForAllPlayers(data.getId());
            spawnHologramForAllPlayers(data);
        } else {
            updateHologramTextsForAllPlayers(data);
        }

        saveHolograms();
        return true;
    }

    private void updateHologramTextsForAllPlayers(HologramData data) {
        for (Map.Entry<UUID, Map<String, List<DebugText>>> entry : playerHolograms.entrySet()) {
            UUID playerId = entry.getKey();
            Player player = findPlayerByUUID(playerId);
            if (player == null) {
                continue;
            }

            EntityPlayer entityPlayer = player.getControlledEntity();
            if (entityPlayer == null) {
                continue;
            }

            Map<String, List<DebugText>> hologramMap = entry.getValue();
            List<DebugText> texts = hologramMap.get(data.getId());
            if (texts == null || texts.isEmpty()) {
                continue;
            }

            String locale = player.getLoginData().getLangCode().toString();
            List<String> lines = data.getLinesForLocale(locale);

            if (data.isMultiLine() && lines.size() > 1) {
                float yOffset = 0;
                for (int i = 0; i < texts.size() && i < lines.size(); i++) {
                    int lineIndex = lines.size() - 1 - i;
                    String lineText = processPlaceholders(entityPlayer, lines.get(lineIndex));
                    DebugText debugText = texts.get(i);
                    debugText.setText(lineText);
                    debugText.setPosition(
                            new Vector3f((float) data.getX(), (float) (data.getY() + yOffset), (float) data.getZ()));
                    yOffset += (float) data.getLineSpacing();
                }
            } else {
                String combinedText = String.join("\n", lines);
                combinedText = processPlaceholders(entityPlayer, combinedText);
                DebugText debugText = texts.get(0);
                debugText.setText(combinedText);
                debugText.setPosition(new Vector3f((float) data.getX(), (float) data.getY(), (float) data.getZ()));
            }
        }
    }

    public Optional<HologramData> getHologram(String id) {
        return Optional.ofNullable(holograms.get(id));
    }

    public Collection<HologramData> getAllHolograms() {
        return holograms.values();
    }

    public Optional<HologramData> getNearestHologram(EntityPlayer player, double maxDistance) {
        Location3d playerLoc = new Location3d(player.getLocation());
        HologramData nearest = null;
        double nearestDistance = maxDistance;

        for (HologramData data : holograms.values()) {
            if (!data.getWorld().equals(player.getDimension().getWorld().getName())) {
                continue;
            }

            double distance = calculateDistance(
                    playerLoc.x(), playerLoc.y(), playerLoc.z(),
                    data.getX(), data.getY(), data.getZ());

            if (distance < nearestDistance) {
                nearestDistance = distance;
                nearest = data;
            }
        }

        return Optional.ofNullable(nearest);
    }

    private double calculateDistance(double x1, double y1, double z1, double x2, double y2, double z2) {
        double dx = x1 - x2;
        double dy = y1 - y2;
        double dz = z1 - z2;
        return Math.sqrt(dx * dx + dy * dy + dz * dz);
    }

    public void onPlayerJoin(Player player) {
        UUID playerId = player.getLoginData().getUuid();
        EntityPlayer entityPlayer = player.getControlledEntity();
        if (entityPlayer == null) {
            return;
        }

        String locale = player.getLoginData().getLangCode().toString();
        Map<String, List<DebugText>> hologramMap = new ConcurrentHashMap<>();

        for (HologramData data : holograms.values()) {
            if (!data.getWorld().equals(entityPlayer.getDimension().getWorld().getName())) {
                continue;
            }

            List<DebugText> texts = createHologramTextsForPlayer(player, data, locale);
            hologramMap.put(data.getId(), texts);
        }

        playerHolograms.put(playerId, hologramMap);
    }

    public void onPlayerQuit(Player player) {
        UUID playerId = player.getLoginData().getUuid();
        Map<String, List<DebugText>> hologramMap = playerHolograms.remove(playerId);
        if (hologramMap == null) {
            return;
        }

        for (List<DebugText> texts : hologramMap.values()) {
            for (DebugText text : texts) {
                text.removeViewer(player);
            }
        }
    }

    public void onPlayerChangeDimension(Player player, Dimension oldDimension, Dimension newDimension) {
        UUID playerId = player.getLoginData().getUuid();
        Map<String, List<DebugText>> hologramMap = playerHolograms.get(playerId);
        if (hologramMap == null) {
            return;
        }

        for (List<DebugText> texts : hologramMap.values()) {
            for (DebugText text : texts) {
                text.removeViewer(player);
            }
        }
        hologramMap.clear();

        String locale = player.getLoginData().getLangCode().toString();
        String newWorldName = newDimension.getWorld().getName();

        for (HologramData data : holograms.values()) {
            if (!data.getWorld().equals(newWorldName)) {
                continue;
            }

            List<DebugText> texts = createHologramTextsForPlayer(player, data, locale);
            hologramMap.put(data.getId(), texts);
        }
    }

    private List<DebugText> createHologramTextsForPlayer(Player player, HologramData data, String locale) {
        List<DebugText> texts = new ArrayList<>();
        List<String> lines = data.getLinesForLocale(locale);
        EntityPlayer entityPlayer = player.getControlledEntity();
        if (entityPlayer == null) {
            return texts;
        }

        if (data.isMultiLine() && lines.size() > 1) {
            float yOffset = 0;
            for (int i = lines.size() - 1; i >= 0; i--) {
                String lineText = processPlaceholders(entityPlayer, lines.get(i));
                DebugText debugText = createDebugText(data.getX(), data.getY() + yOffset, data.getZ(), lineText);
                debugText.addViewer(player);
                texts.add(debugText);
                yOffset += (float) data.getLineSpacing();
            }
        } else {
            String combinedText = String.join("\n", lines);
            combinedText = processPlaceholders(entityPlayer, combinedText);
            DebugText debugText = createDebugText(data.getX(), data.getY(), data.getZ(), combinedText);
            debugText.addViewer(player);
            texts.add(debugText);
        }

        return texts;
    }

    private DebugText createDebugText(double x, double y, double z, String text) {
        return new DebugText(
                new Vector3f((float) x, (float) y, (float) z),
                Color.WHITE,
                text,
                1.0f);
    }

    private String processPlaceholders(EntityPlayer player, String text) {
        return plugin.parsePlaceholders(player, text);
    }

    private void spawnHologramForAllPlayers(HologramData data) {
        for (Player player : Server.getInstance().getPlayerManager().getPlayers().values()) {
            EntityPlayer entityPlayer = player.getControlledEntity();
            if (entityPlayer == null) {
                continue;
            }

            if (!data.getWorld().equals(entityPlayer.getDimension().getWorld().getName())) {
                continue;
            }

            UUID playerId = player.getLoginData().getUuid();

            String locale = player.getLoginData().getLangCode().toString();

            Map<String, List<DebugText>> hologramMap = playerHolograms.computeIfAbsent(playerId,
                    k -> new ConcurrentHashMap<>());
            List<DebugText> texts = createHologramTextsForPlayer(player, data, locale);
            hologramMap.put(data.getId(), texts);
        }
    }

    private void despawnHologramForAllPlayers(String hologramId) {
        for (Map.Entry<UUID, Map<String, List<DebugText>>> entry : playerHolograms.entrySet()) {
            Map<String, List<DebugText>> hologramMap = entry.getValue();
            List<DebugText> texts = hologramMap.remove(hologramId);
            if (texts == null) {
                continue;
            }

            UUID playerId = entry.getKey();
            Player player = findPlayerByUUID(playerId);

            for (DebugText text : texts) {
                if (player != null) {
                    text.removeViewer(player);
                }
                for (var viewer : new java.util.ArrayList<>(text.getViewers())) {
                    text.removeViewer(viewer);
                }
            }
        }
    }

    private Player findPlayerByUUID(UUID uuid) {
        return Server.getInstance().getPlayerManager().getPlayers().get(uuid);
    }

    public void updateAllHolograms() {
        for (Player player : Server.getInstance().getPlayerManager().getPlayers().values()) {
            updateHologramsForPlayer(player);
        }
    }

    public void updateHologramsForPlayer(Player player) {
        UUID playerId = player.getLoginData().getUuid();
        Map<String, List<DebugText>> hologramMap = playerHolograms.get(playerId);
        if (hologramMap == null) {
            return;
        }

        EntityPlayer entityPlayer = player.getControlledEntity();
        if (entityPlayer == null) {
            return;
        }

        String locale = player.getLoginData().getLangCode().toString();

        for (Map.Entry<String, HologramData> entry : holograms.entrySet()) {
            HologramData data = entry.getValue();

            if (data.getUpdateInterval() <= 0) {
                continue;
            }

            if (!data.getWorld().equals(entityPlayer.getDimension().getWorld().getName())) {
                continue;
            }

            List<DebugText> texts = hologramMap.get(data.getId());
            if (texts == null || texts.isEmpty()) {
                continue;
            }

            List<String> lines = data.getLinesForLocale(locale);
            updateDebugTexts(entityPlayer, texts, lines, data.isMultiLine());
        }
    }

    private void updateDebugTexts(EntityPlayer player, List<DebugText> texts, List<String> lines, boolean multiLine) {
        if (multiLine && lines.size() > 1) {
            for (int i = 0; i < texts.size() && i < lines.size(); i++) {
                String text = processPlaceholders(player, lines.get(lines.size() - 1 - i));
                texts.get(i).setText(text);
            }
        } else {
            String combinedText = String.join("\n", lines);
            combinedText = processPlaceholders(player, combinedText);
            if (!texts.isEmpty()) {
                texts.get(0).setText(combinedText);
            }
        }
    }

    public void removeAllHolograms() {
        for (Map.Entry<UUID, Map<String, List<DebugText>>> entry : playerHolograms.entrySet()) {
            UUID playerId = entry.getKey();
            Player player = findPlayerByUUID(playerId);

            for (List<DebugText> texts : entry.getValue().values()) {
                for (DebugText text : texts) {
                    if (player != null) {
                        text.removeViewer(player);
                    }
                    for (var viewer : new java.util.ArrayList<>(text.getViewers())) {
                        text.removeViewer(viewer);
                    }
                }
            }
        }
        playerHolograms.clear();
    }

    public void reloadHolograms() {
        removeAllHolograms();
        holograms.clear();
        loadHolograms();

        for (Player player : Server.getInstance().getPlayerManager().getPlayers().values()) {
            onPlayerJoin(player);
        }
    }
}
