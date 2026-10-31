package com.oritong.superflatcore.client;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.oritong.superflatcore.SuperFlatCore;

import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.network.chat.ClickEvent;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.Style;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.ClientPlayerNetworkEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URI;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.concurrent.atomic.AtomicBoolean;

/** Checks the pack update manifest without blocking the client or exposing player data. */
@Mod.EventBusSubscriber(modid = SuperFlatCore.MODID, value = Dist.CLIENT)
public final class SFUpdateChecker {
    private static final String CURRENT_PACK_VERSION = "0.2.2";
    private static final String MANIFEST_URL = "https://oritong.github.io/updates/superflatcore.json";
    private static final String FALLBACK_DOWNLOAD_URL =
            "https://www.xyebbs.com/res-id/diamond_continent_plus_and_plus_edition/downloads";
    private static final AtomicBoolean CHECKED_THIS_SESSION = new AtomicBoolean();

    private SFUpdateChecker() {}

    @SubscribeEvent
    public static void onLoggingIn(ClientPlayerNetworkEvent.LoggingIn event) {
        if (!CHECKED_THIS_SESSION.compareAndSet(false, true)) {
            return;
        }

        Thread checker = new Thread(SFUpdateChecker::checkInBackground, "SuperFlatCore update checker");
        checker.setDaemon(true);
        checker.start();
    }

    @SubscribeEvent
    public static void onLoggingOut(ClientPlayerNetworkEvent.LoggingOut event) {
        CHECKED_THIS_SESSION.set(false);
    }

    private static void checkInBackground() {
        try {
            Manifest manifest = loadManifest();
            if (manifest == null || compareVersions(CURRENT_PACK_VERSION, manifest.latestVersion()) >= 0) {
                return;
            }

            Minecraft.getInstance().execute(() -> announce(manifest));
        } catch (Exception ignored) {
            // A failed update check must never interfere with entering or playing a world.
        }
    }

    private static Manifest loadManifest() throws IOException {
        URI uri = URI.create(MANIFEST_URL);
        if (!"https".equalsIgnoreCase(uri.getScheme())) {
            return null;
        }

        URL url = uri.toURL();
        HttpURLConnection connection = (HttpURLConnection) url.openConnection();
        connection.setRequestMethod("GET");
        connection.setConnectTimeout(3000);
        connection.setReadTimeout(3000);
        connection.setUseCaches(false);
        connection.setRequestProperty("Accept", "application/json");
        connection.setRequestProperty("User-Agent", "SuperFlatCore-update-check");
        try {
            if (connection.getResponseCode() != HttpURLConnection.HTTP_OK) {
                return null;
            }
            try (InputStream input = connection.getInputStream();
                 InputStreamReader reader = new InputStreamReader(input, StandardCharsets.UTF_8)) {
                JsonObject json = JsonParser.parseReader(reader).getAsJsonObject();
                if (!json.has("latestVersion") || !json.get("latestVersion").isJsonPrimitive()) {
                    return null;
                }

                String latestVersion = json.get("latestVersion").getAsString().trim();
                if (latestVersion.isEmpty()) {
                    return null;
                }
                String downloadUrl = json.has("downloadUrl") && json.get("downloadUrl").isJsonPrimitive()
                        ? json.get("downloadUrl").getAsString().trim()
                        : FALLBACK_DOWNLOAD_URL;
                if (!isHttpsUrl(downloadUrl)) {
                    downloadUrl = FALLBACK_DOWNLOAD_URL;
                }
                return new Manifest(latestVersion, downloadUrl);
            }
        } finally {
            connection.disconnect();
        }
    }

    private static void announce(Manifest manifest) {
        LocalPlayer player = Minecraft.getInstance().player;
        if (player == null) {
            return;
        }

        Component link = Component.literal("链接").withStyle(Style.EMPTY
                .withColor(ChatFormatting.AQUA)
                .withUnderlined(true)
                .withClickEvent(new ClickEvent(ClickEvent.Action.OPEN_URL, manifest.downloadUrl())));
        player.sendSystemMessage(Component.literal("——————————————"));
        player.sendSystemMessage(Component.literal("当前最新版本为：" + manifest.latestVersion()));
        player.sendSystemMessage(Component.literal("点击该").append(link).append("以下载最新版本！"));
        player.sendSystemMessage(Component.literal("——————————————"));
    }

    private static boolean isHttpsUrl(String value) {
        try {
            return "https".equalsIgnoreCase(URI.create(value).getScheme());
        } catch (IllegalArgumentException ignored) {
            return false;
        }
    }

    private static int compareVersions(String left, String right) {
        String[] leftParts = left.split("\\.");
        String[] rightParts = right.split("\\.");
        int count = Math.max(leftParts.length, rightParts.length);
        for (int index = 0; index < count; index++) {
            int leftPart = index < leftParts.length ? parseVersionPart(leftParts[index]) : 0;
            int rightPart = index < rightParts.length ? parseVersionPart(rightParts[index]) : 0;
            if (leftPart != rightPart) {
                return Integer.compare(leftPart, rightPart);
            }
        }
        return 0;
    }

    private static int parseVersionPart(String value) {
        int end = 0;
        while (end < value.length() && Character.isDigit(value.charAt(end))) {
            end++;
        }
        if (end == 0) {
            return 0;
        }
        try {
            return Integer.parseInt(value.substring(0, end));
        } catch (NumberFormatException ignored) {
            return Integer.MAX_VALUE;
        }
    }

    private record Manifest(String latestVersion, String downloadUrl) {}
}
