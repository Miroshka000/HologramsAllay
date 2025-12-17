package miroshka.holograms.manager;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import lombok.Getter;
import org.slf4j.Logger;

import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.List;

@Getter
public class Config {

    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();

    private final File configFile;
    private final Logger logger;
    private ConfigData data;

    public Config(File dataFolder, Logger logger) {
        this.configFile = new File(dataFolder, "config.json");
        this.logger = logger;
        load();
    }

    private void load() {
        if (!configFile.exists()) {
            configFile.getParentFile().mkdirs();
            data = new ConfigData();
            save();
            return;
        }

        try (FileReader reader = new FileReader(configFile, StandardCharsets.UTF_8)) {
            data = GSON.fromJson(reader, ConfigData.class);
            if (data == null) {
                data = new ConfigData();
            }
        } catch (IOException e) {
            logger.error("Failed to load config", e);
            data = new ConfigData();
        }
    }

    public void save() {
        try (FileWriter writer = new FileWriter(configFile, StandardCharsets.UTF_8)) {
            GSON.toJson(data, writer);
        } catch (IOException e) {
            logger.error("Failed to save config", e);
        }
    }

    public int getUpdateInterval() {
        return data.updateInterval;
    }

    public String getDefaultLocale() {
        return data.defaultLocale;
    }

    public double getDefaultLineSpacing() {
        return data.defaultLineSpacing;
    }

    public int getMaxHologramDistance() {
        return data.maxHologramDistance;
    }

    public List<String> getSupportedLocales() {
        return data.supportedLocales;
    }

    public static class ConfigData {
        public int updateInterval = 20;
        public String defaultLocale = "en_US";
        public double defaultLineSpacing = 0.25;
        public int maxHologramDistance = 32;
        public List<String> supportedLocales = List.of(
                "en_US", "ru_RU", "zh_CN", "de_DE", "fr_FR",
                "es_ES", "pt_BR", "ja_JP", "ko_KR", "uk_UA");
    }
}
