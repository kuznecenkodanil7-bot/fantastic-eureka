package ru.helena.chatreminder;

import net.fabricmc.api.ClientModInitializer;
import net.minecraft.client.MinecraftClient;
import net.minecraft.text.Text;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public final class ChatReminderClient implements ClientModInitializer {
    public static final String MOD_ID = "chatreminder";
    public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);

    private static boolean wasInGame = false;
    private static int ticksUntilNextMessage = -1;
    private static int configCheckTicks = 0;

    @Override
    public void onInitializeClient() {
        ChatReminderConfig.load();
        LOGGER.info("Chat Reminder initialized");
    }

    public static void onClientTick(MinecraftClient client) {
        if (client == null || client.player == null || client.world == null) {
            resetTimerState();
            return;
        }

        checkConfigOncePerSecond();

        ChatReminderConfig.Config config = ChatReminderConfig.get();
        if (!config.enabled) {
            ticksUntilNextMessage = Math.max(1, config.firstDelaySeconds) * 20;
            return;
        }

        if (!wasInGame) {
            wasInGame = true;
            ticksUntilNextMessage = Math.max(1, config.firstDelaySeconds) * 20;
        }

        ticksUntilNextMessage--;

        if (ticksUntilNextMessage <= 0) {
            sendReminder(client, config.message);
            ticksUntilNextMessage = Math.max(1, config.intervalSeconds) * 20;
        }
    }

    private static void checkConfigOncePerSecond() {
        configCheckTicks++;
        if (configCheckTicks >= 20) {
            configCheckTicks = 0;
            ChatReminderConfig.reloadIfChanged();
        }
    }

    private static void sendReminder(MinecraftClient client, String message) {
        if (client.player == null || message == null || message.isBlank()) {
            return;
        }

        Text text = ChatReminderText.fromLegacyAmpersand(message);
        client.player.sendMessage(text, false);
    }

    private static void resetTimerState() {
        wasInGame = false;
        ticksUntilNextMessage = -1;
        configCheckTicks = 0;
    }
}
