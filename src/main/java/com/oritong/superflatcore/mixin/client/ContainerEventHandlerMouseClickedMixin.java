package com.oritong.superflatcore.mixin.client;

import net.minecraft.client.gui.components.events.ContainerEventHandler;
import net.minecraft.client.gui.components.events.GuiEventListener;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.NoSuchElementException;

/**
 * GUI customization mods can rebuild a screen's children while a click is
 * being dispatched. Iterate over a stable snapshot so that rebuild does not
 * invalidate the event loop's ArrayList iterator.
 */
@Mixin(ContainerEventHandler.class)
public abstract class ContainerEventHandlerMouseClickedMixin {
    @Redirect(
            method = "mouseClicked",
            at = @At(value = "INVOKE", target = "Ljava/util/List;iterator()Ljava/util/Iterator;")
    )
    private static Iterator<? extends GuiEventListener> superflatcore$iterateClickedChildren(
            List<? extends GuiEventListener> children) {
        List<? extends GuiEventListener> snapshot = new ArrayList<>(children);
        return new Iterator<>() {
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
    }
}
