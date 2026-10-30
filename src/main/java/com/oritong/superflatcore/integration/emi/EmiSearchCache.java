package com.oritong.superflatcore.integration.emi;

import dev.emi.emi.EmiPort;
import dev.emi.emi.EmiUtil;
import dev.emi.emi.api.stack.EmiIngredient;
import dev.emi.emi.api.stack.EmiStack;
import dev.emi.emi.data.EmiAlias;
import dev.emi.emi.data.EmiData;
import dev.emi.emi.registry.EmiStackList;
import dev.emi.emi.runtime.EmiLog;
import dev.emi.emi.runtime.EmiReloadLog;
import dev.emi.emi.search.EmiSearch;
import dev.emi.emi.search.SearchStack;
import net.minecraft.client.Minecraft;
import net.minecraft.client.resources.language.I18n;
import net.minecraft.client.searchtree.SuffixArray;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import net.minecraftforge.fml.ModList;
import net.minecraftforge.fml.loading.FMLPaths;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.lang.reflect.Field;
import java.nio.file.AtomicMoveNotSupportedException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.IdentityHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Supplier;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * Persists only EMI's text search index inputs. Runtime recipe and ingredient
 * objects remain in memory and are always rebuilt, so a cache cannot leak data
 * from a previous server into a new connection.
 */
public final class EmiSearchCache {
    private static final String MAGIC = "superflatcore-emi-search-cache";
    private static final int FORMAT = 1;
    private static final Path CACHE_FILE = FMLPaths.CONFIGDIR.get()
            .resolve("superflatcore")
            .resolve("emi-search-cache.bin");
    private static final AtomicBoolean WRITING = new AtomicBoolean();

    private EmiSearchCache() {
    }

    public static synchronized boolean load() {
        List<EmiStack> current = List.copyOf(EmiStackList.stacks);
        if (current.isEmpty() || !Files.isRegularFile(CACHE_FILE)) {
            return false;
        }
        try (DataInputStream input = new DataInputStream(
                new BufferedInputStream(Files.newInputStream(CACHE_FILE)))) {
            if (!MAGIC.equals(input.readUTF()) || FORMAT != input.readInt()) {
                return false;
            }
            if (!cacheScope().equals(input.readUTF())) {
                return false;
            }
            int count = input.readInt();
            if (count < 0 || count > 2_000_000) {
                return false;
            }
            Map<String, Entry> entries = new HashMap<>(Math.max(16, count * 2));
            for (int i = 0; i < count; i++) {
                String key = input.readUTF();
                if (entries.put(key, new Entry(readStrings(input), readStrings(input), readStrings(input))) != null) {
                    return false;
                }
            }

            SuffixArray<SearchStack> names = new SuffixArray<>();
            SuffixArray<SearchStack> tooltips = new SuffixArray<>();
            SuffixArray<SearchStack> mods = new SuffixArray<>();
            Set<EmiStack> bakedStacks = Collections.newSetFromMap(new IdentityHashMap<>());
            for (EmiStack stack : current) {
                Entry entry = entries.get(stackKey(stack));
                if (entry == null) {
                    return false;
                }
                SearchStack searchStack = new SearchStack(stack);
                addAll(names, searchStack, entry.names());
                addAll(tooltips, searchStack, entry.tooltips());
                addAll(mods, searchStack, entry.mods());
                bakedStacks.add(stack);
            }
            names.generate();
            tooltips.generate();
            mods.generate();
            EmiSearch.names = names;
            EmiSearch.tooltips = tooltips;
            EmiSearch.mods = mods;
            EmiSearch.aliases = buildAliases();
            EmiSearch.bakedStacks = bakedStacks;
            EmiLog.info("Loaded SuperFlatCore EMI search cache for " + current.size() + " stacks");
            return true;
        } catch (Throwable ignored) {
            return false;
        }
    }

    public static void saveAsync() {
        if (!WRITING.compareAndSet(false, true)) {
            return;
        }
        List<EmiStack> snapshot = List.copyOf(EmiStackList.stacks);
        String scope = cacheScope();
        Thread writer = new Thread(() -> {
            try {
                save(snapshot, scope);
            } finally {
                WRITING.set(false);
            }
        }, "SuperFlatCore-EMI-search-cache");
        writer.setDaemon(true);
        writer.start();
    }

    private static void save(List<EmiStack> stacks, String scope) {
        if (stacks.isEmpty()) {
            return;
        }
        Map<String, Entry> entries = new HashMap<>(stacks.size() * 2);
        try {
            for (EmiStack stack : stacks) {
                entries.put(stackKey(stack), capture(stack));
            }
            Files.createDirectories(CACHE_FILE.getParent());
            Path temporary = CACHE_FILE.resolveSibling(CACHE_FILE.getFileName() + ".tmp");
            try (DataOutputStream output = new DataOutputStream(
                    new BufferedOutputStream(Files.newOutputStream(temporary)))) {
                output.writeUTF(MAGIC);
                output.writeInt(FORMAT);
                output.writeUTF(scope);
                output.writeInt(entries.size());
                for (Map.Entry<String, Entry> entry : entries.entrySet()) {
                    output.writeUTF(entry.getKey());
                    writeStrings(output, entry.getValue().names());
                    writeStrings(output, entry.getValue().tooltips());
                    writeStrings(output, entry.getValue().mods());
                }
            }
            try {
                Files.move(temporary, CACHE_FILE, StandardCopyOption.REPLACE_EXISTING,
                        StandardCopyOption.ATOMIC_MOVE);
            } catch (AtomicMoveNotSupportedException e) {
                Files.move(temporary, CACHE_FILE, StandardCopyOption.REPLACE_EXISTING);
            }
            EmiLog.info("Saved SuperFlatCore EMI search cache for " + stacks.size() + " stacks");
        } catch (Throwable ignored) {
            // A stale or unwritable cache must never prevent EMI from loading.
        }
    }

    private static Entry capture(EmiStack stack) {
        List<String> names = new ArrayList<>(1);
        if (stack.getName() != null) {
            names.add(stack.getName().getString().toLowerCase());
        }
        List<String> tooltips = new ArrayList<>();
        try {
            List<net.minecraft.network.chat.Component> tooltip = stack.getTooltipText();
            for (int i = 1; i < tooltip.size(); i++) {
                var line = tooltip.get(i);
                if (line != null) {
                    tooltips.add(line.getString().toLowerCase());
                }
            }
        } catch (Throwable ignored) {
        }
        List<String> mods = new ArrayList<>(3);
        if (stack.getId() != null) {
            String namespace = stack.getId().getNamespace().toLowerCase();
            mods.add(EmiUtil.getModName(namespace).toLowerCase());
            mods.add(namespace);
            mods.add(stack.getId().getPath().toLowerCase());
            if (stack.getItemStack().getItem() == Items.ENCHANTED_BOOK) {
                for (Enchantment enchantment : EnchantmentHelper.getEnchantments(stack.getItemStack()).keySet()) {
                    var enchantmentId = EmiPort.getEnchantmentRegistry().getKey(enchantment);
                    if (enchantmentId != null && !"minecraft".equals(enchantmentId.getNamespace())) {
                        mods.add(EmiUtil.getModName(enchantmentId.getNamespace()).toLowerCase());
                    }
                }
            }
        }
        return new Entry(List.copyOf(names), List.copyOf(tooltips), List.copyOf(mods));
    }

    private static EmiStack stackCopy(EmiIngredient ingredient) {
        List<EmiStack> stacks = ingredient.getEmiStacks();
        return stacks.isEmpty() ? EmiStack.EMPTY : stacks.get(0);
    }

    private static SuffixArray<EmiStack> buildAliases() {
        SuffixArray<EmiStack> aliases = new SuffixArray<>();
        for (Supplier<EmiAlias> supplier : EmiData.aliases) {
            EmiAlias alias = supplier.get();
            for (String key : alias.keys()) {
                if (!I18n.exists(key)) {
                    EmiReloadLog.warn("Untranslated alias " + key);
                }
                String text = I18n.get(key).toLowerCase();
                for (EmiIngredient ingredient : alias.stacks()) {
                    for (EmiStack stack : ingredient.getEmiStacks()) {
                        aliases.add(stack.copy().comparison(EmiPort.compareStrict()), text);
                    }
                }
            }
        }
        for (EmiAlias.Baked alias : EmiStackList.registryAliases) {
            for (var text : alias.text()) {
                for (EmiIngredient ingredient : alias.stacks()) {
                    for (EmiStack stack : ingredient.getEmiStacks()) {
                        aliases.add(stack.copy().comparison(EmiPort.compareStrict()), text.getString().toLowerCase());
                    }
                }
            }
        }
        aliases.generate();
        return aliases;
    }

    private static void addAll(SuffixArray<SearchStack> index, SearchStack stack, List<String> values) {
        for (String value : values) {
            if (!value.isEmpty()) {
                index.add(stack, value);
            }
        }
    }

    private static List<String> readStrings(DataInputStream input) throws IOException {
        int count = input.readInt();
        if (count < 0 || count > 100_000) {
            throw new IOException("Invalid EMI cache string count");
        }
        List<String> values = new ArrayList<>(count);
        for (int i = 0; i < count; i++) {
            values.add(input.readUTF());
        }
        return values;
    }

    private static void writeStrings(DataOutputStream output, List<String> values) throws IOException {
        output.writeInt(values.size());
        for (String value : values) {
            output.writeUTF(value);
        }
    }

    private static String stackKey(EmiStack stack) {
        return String.valueOf(stack.getId()) + "|" + String.valueOf(stack.getNbt());
    }

    private static String cacheScope() {
        List<String> mods = new ArrayList<>();
        try {
            for (var mod : ModList.get().getMods()) {
                mods.add(mod.getModId() + "=" + mod.getVersion());
            }
        } catch (Throwable ignored) {
        }
        mods.sort(Comparator.naturalOrder());
        String packs = "";
        try {
            Object minecraft = Minecraft.getInstance();
            Field optionsField = findField(minecraft.getClass(), "options");
            if (optionsField != null) {
                optionsField.setAccessible(true);
                Object options = optionsField.get(minecraft);
                Field packsField = findField(options.getClass(), "resourcePacks");
                if (packsField != null) {
                    packsField.setAccessible(true);
                    packs = String.valueOf(packsField.get(options));
                }
            }
        } catch (Throwable ignored) {
        }
        return MAGIC + "|" + String.join(",", mods) + "|lang=" + languageCode() + "|packs=" + packs;
    }

    private static String languageCode() {
        try {
            Object minecraft = Minecraft.getInstance();
            Field optionsField = findField(minecraft.getClass(), "options");
            if (optionsField != null) {
                optionsField.setAccessible(true);
                Object options = optionsField.get(minecraft);
                Field languageField = findField(options.getClass(), "languageCode");
                if (languageField != null) {
                    languageField.setAccessible(true);
                    return String.valueOf(languageField.get(options));
                }
            }
        } catch (Throwable ignored) {
        }
        return "unknown";
    }

    private static Field findField(Class<?> type, String name) {
        for (Class<?> current = type; current != null; current = current.getSuperclass()) {
            try {
                return current.getDeclaredField(name);
            } catch (NoSuchFieldException ignored) {
            }
        }
        return null;
    }

    private record Entry(List<String> names, List<String> tooltips, List<String> mods) {
    }
}
