package com.oritong.superflatcore.mixin.client;

import net.minecraft.client.gui.components.events.ContainerEventHandler;
import net.minecraft.client.gui.components.events.GuiEventListener;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.NoSuchElementException;

/**
 * GUI customization mods can rebuild a screen's children while a click is
 * being dispatched. Stop iterating if the live child list changes.
 */
@Mixin(ContainerEventHandler.class)
public interface ContainerEventHandlerMouseClickedMixin {
    /**
     * Preserve vanilla click handling while exiting safely when a child
     * rebuilds the screen's listener list during dispatch.
     *
     * @author SuperFlatCore
     * @reason Avoid iterating the live listener list during click callbacks.
     */
    @Overwrite
    default boolean mouseClicked(double mouseX, double mouseY, int button) {
        List<? extends GuiEventListener> children = ((ContainerEventHandler) this).children();
        List<? extends GuiEventListener> snapshot = new ArrayList<>(children);
        Iterator<? extends GuiEventListener> iterator = new Iterator<>() {
            private int index;

            @Override
            public boolean hasNext() {
                if (children.size() != snapshot.size()) {
                    return false;
                }
                for (int i = 0; i < snapshot.size(); i++) {
                    if (children.get(i) != snapshot.get(i)) {
                        return false;
                    }
                }
                return index < snapshot.size();
            }

            @Override
            public GuiEventListener next() {
                if (!hasNext()) {
                    throw new NoSuchElementException();
                }
                return snapshot.get(index++);
            }
        };

        while (iterator.hasNext()) {
            GuiEventListener child = iterator.next();
            if (child.mouseClicked(mouseX, mouseY, button)) {
                ((ContainerEventHandler) this).setFocused(child);
                if (button == 0) {
                    ((ContainerEventHandler) this).setDragging(true);
                }
                return true;
            }
        }
        return false;
    }
}
