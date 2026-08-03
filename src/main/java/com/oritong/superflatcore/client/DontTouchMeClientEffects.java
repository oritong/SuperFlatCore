package com.oritong.superflatcore.client;

import com.oritong.superflatcore.SuperFlatCore;
import com.oritong.superflatcore.item.DontTouchMeItem;

import net.minecraft.CrashReport;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.resources.ResourceLocation;

import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.RenderGuiOverlayEvent;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

@Mod.EventBusSubscriber(modid = SuperFlatCore.MODID, value = Dist.CLIENT)
public final class DontTouchMeClientEffects {
    private static final Logger LOGGER = LogManager.getLogger();
    private static final String CRASH_UNIT = "Don't touch me.";
    private static final String CRASH_SPAM = CRASH_UNIT.repeat(256);
    private static final Logger CRASH_LOGGER = LogManager.getLogger(CRASH_UNIT);
    private static final int CRASH_LOG_LINES = 4096;
    private static final ResourceLocation GRAYSCALE_SHADER =
            new ResourceLocation("minecraft", "shaders/post/superflatcore_dont_touch_me_grayscale.json");
    private static long grayscaleEndTick;
    private static long overlayEndTick;
    private static boolean grayscaleActive;
    private static boolean crashPending;

    private DontTouchMeClientEffects() {}

    public static void applyStage(int stage) {
        if (stage > DontTouchMeItem.MAX_STAGE) {
            crashPending = true;
            return;
        }

        Minecraft minecraft = Minecraft.getInstance();
        long now = minecraft.level == null ? 0L : minecraft.level.getGameTime();
        if (stage == 0) {
            clearEffects();
        } else if (stage == 3 || stage == 4) {
            overlayEndTick = 0L;
            grayscaleEndTick = now + DontTouchMeItem.RESET_TICKS;
            startGrayscale();
        } else if (stage == 5) {
            stopGrayscale();
            overlayEndTick = now + DontTouchMeItem.RESET_TICKS;
        }
    }

    @SubscribeEvent
    public static void onClientTick(TickEvent.ClientTickEvent event) {
        if (event.phase != TickEvent.Phase.END) {
            return;
        }

        Minecraft minecraft = Minecraft.getInstance();
        if (crashPending) {
            crashPending = false;
            crashClient();
            return;
        }
        if (minecraft.level == null) {
            clearEffects();
            return;
        }

        long now = minecraft.level.getGameTime();
        if (grayscaleActive && grayscaleEndTick > 0L && now >= grayscaleEndTick) {
            stopGrayscale();
        }
        if (overlayEndTick > 0L && now >= overlayEndTick) {
            overlayEndTick = 0L;
        }
    }

    @SubscribeEvent
    public static void onRenderGuiOverlay(RenderGuiOverlayEvent.Post event) {
        Minecraft minecraft = Minecraft.getInstance();
        if (minecraft.level == null || overlayEndTick <= 0L || minecraft.level.getGameTime() >= overlayEndTick) {
            return;
        }

        GuiGraphics graphics = event.getGuiGraphics();
        int width = event.getWindow().getGuiScaledWidth();
        int height = event.getWindow().getGuiScaledHeight();
        drawMissingTextureOverlay(graphics, width, height);
    }

    private static void startGrayscale() {
        Minecraft minecraft = Minecraft.getInstance();
        if (minecraft.level == null || grayscaleActive) {
            return;
        }

        try {
            minecraft.gameRenderer.loadEffect(GRAYSCALE_SHADER);
            grayscaleActive = true;
        } catch (RuntimeException exception) {
            LOGGER.warn("Failed to load Don't Touch Me grayscale shader", exception);
            grayscaleEndTick = 0L;
            grayscaleActive = false;
        }
    }

    private static void stopGrayscale() {
        if (grayscaleActive) {
            Minecraft.getInstance().gameRenderer.shutdownEffect();
        }
        grayscaleActive = false;
        grayscaleEndTick = 0L;
    }

    private static void clearEffects() {
        stopGrayscale();
        overlayEndTick = 0L;
    }

    public static void crashClient() {
        for (int i = 0; i < CRASH_LOG_LINES; i++) {
            CRASH_LOGGER.error(CRASH_SPAM);
        }
        Minecraft.crash(CrashReport.forThrowable(new DontTouchMeCrash(), CRASH_SPAM));
    }

    private static final class DontTouchMeCrash extends RuntimeException {
        private DontTouchMeCrash() {
            super(CRASH_SPAM, null, false, false);
        }

        @Override
        public String toString() {
            return CRASH_SPAM;
        }
    }

    private static void drawMissingTextureOverlay(GuiGraphics graphics, int width, int height) {
        int tileSize = 16;
        int magenta = 0xFFF800F8;
        int black = 0xFF000000;
        for (int y = 0; y < height; y += tileSize) {
            for (int x = 0; x < width; x += tileSize) {
                int color = ((x / tileSize) + (y / tileSize)) % 2 == 0 ? magenta : black;
                graphics.fill(x, y, Math.min(x + tileSize, width), Math.min(y + tileSize, height), color);
            }
        }
    }
}
