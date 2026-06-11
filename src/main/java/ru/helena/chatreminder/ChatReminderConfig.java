package ru.helena.chatreminder;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonSyntaxException;
import net.fabricmc.loader.api.FabricLoader;

import java.io.IOException;
import java.io.Reader;
import java.io.Writer;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;

public final class ChatReminderConfig {
    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();
    private static final Path CONFIG_PATH = FabricLoader.getInstance()
            .getConfigDir()
            .resolve("chat_reminder.json");

    private static Config config = Config.defaults();
    private static long lastModifiedTime = -1L;

    private ChatReminderConfig() {
    }

    public static Config get() {
        return config;
    }

    public static void load() {
        try {
            Files.createDirectories(CONFIG_PATH.getParent());

            if (!Files.exists(CONFIG_PATH)) {
                config = Config.defaults();
                save();
                lastModifiedTime = readLastModifiedTime();
                return;
            }

            try (Reader reader = Files.newBufferedReader(CONFIG_PATH, StandardCharsets.UTF_8)) {
                Config loadedConfig = GSON.fromJson(reader, Config.class);
                config = sanitize(loadedConfig);
            }

            lastModifiedTime = readLastModifiedTime();
        } catch (IOException | JsonSyntaxException exception) {
            ChatReminderClient.LOGGER.warn("Failed to load Chat Reminder config. Default values will be used.", exception);
            config = Config.defaults();
        }
    }

    public static void reloadIfChanged() {
        long currentModifiedTime = readLastModifiedTime();
        if (currentModifiedTime != -1L && currentModifiedTime != lastModifiedTime) {
            load();
            ChatReminderClient.LOGGER.info("Chat Reminder config reloaded");
        }
    }

    public static void save() {
        try {
            Files.createDirectories(CONFIG_PATH.getParent());
            try (Writer writer = Files.newBufferedWriter(CONFIG_PATH, StandardCharsets.UTF_8)) {
                GSON.toJson(config, writer);
            }
            lastModifiedTime = readLastModifiedTime();
        } catch (IOException exception) {
            ChatReminderClient.LOGGER.warn("Failed to save Chat Reminder config.", exception);
        }
    }

    public static Path getConfigPath() {
        return CONFIG_PATH;
    }

    private static Config sanitize(Config loadedConfig) {
        Config defaults = Config.defaults();

        if (loadedConfig == null) {
            return defaults;
        }

        if (loadedConfig.message == null || loadedConfig.message.isBlank()) {
            loadedConfig.message = defaults.message;
        }

        if (loadedConfig.intervalSeconds < 1) {
            loadedConfig.intervalSeconds = defaults.intervalSeconds;
        }

        if (loadedConfig.firstDelaySeconds < 1) {
            loadedConfig.firstDelaySeconds = defaults.firstDelaySeconds;
        }

        return loadedConfig;
    }

    private static long readLastModifiedTime() {
        try {
            if (!Files.exists(CONFIG_PATH)) {
                return -1L;
            }
            return Files.getLastModifiedTime(CONFIG_PATH).toMillis();
        } catch (IOException exception) {
            return -1L;
        }
    }

    public static final class Config {
        public boolean enabled = true;
        public String message = "&a[Авто-сообщение] &fЭто сообщение отправляется каждые 4 минуты.";
        public int intervalSeconds = 240;
        public int firstDelaySeconds = 10;

        public static Config defaults() {
            return new Config();
        }
    }
}
