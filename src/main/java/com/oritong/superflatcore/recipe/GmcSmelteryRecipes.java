package com.oritong.superflatcore.recipe;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.gregtechceu.gtceu.api.GTCEuAPI;
import com.gregtechceu.gtceu.api.GTValues;
import com.gregtechceu.gtceu.api.data.chemical.ChemicalHelper;
import com.gregtechceu.gtceu.api.data.chemical.material.Material;
import com.gregtechceu.gtceu.api.data.chemical.material.properties.PropertyKey;
import com.gregtechceu.gtceu.api.data.tag.TagPrefix;
import com.oritong.superflatcore.SuperFlatCore;
import com.oritong.superflatcore.mixin.GmcMaterialMeltingRecipeAccessor;
import com.oritong.superflatcore.mixin.GmcMeltingRecipeAccessor;
import com.oritong.superflatcore.mixin.GmcMaterialFluidRecipeAccessor;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.item.crafting.Recipe;
import net.minecraft.world.item.crafting.RecipeType;
import net.minecraft.world.level.material.Fluid;
import net.minecraft.world.level.material.Fluids;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import slimeknights.mantle.recipe.helper.FluidOutput;
import slimeknights.mantle.recipe.helper.ItemOutput;
import slimeknights.mantle.recipe.ingredient.FluidIngredient;
import slimeknights.tconstruct.library.recipe.TinkerRecipeTypes;
import slimeknights.tconstruct.library.recipe.casting.ICastingRecipe;
import slimeknights.tconstruct.library.recipe.casting.ItemCastingRecipe;
import slimeknights.tconstruct.library.recipe.casting.material.MaterialFluidRecipe;
import slimeknights.tconstruct.library.recipe.melting.IMeltingRecipe;
import slimeknights.tconstruct.library.recipe.melting.MeltingRecipe;
import slimeknights.tconstruct.library.recipe.melting.MaterialMeltingRecipe;
import slimeknights.tconstruct.smeltery.TinkerSmeltery;

import java.util.Collections;
import java.util.ArrayList;
import java.util.IdentityHashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/** Completes GMC's material conversion without changing GregTech fluid temperatures. */
public final class GmcSmelteryRecipes {
    private static final Logger LOGGER = LogManager.getLogger();
    private static final String GENERATED = "gmc_smeltery/";
    private static Map<Fluid, Material> materialsByFluid;
    private static final List<Form> FORMS = List.of(
            new Form("ingot", TagPrefix.ingot, "ingots", true),
            new Form("nugget", TagPrefix.nugget, "nuggets", true),
            new Form("block", TagPrefix.block, "storage_blocks", true),
            new Form("dust", TagPrefix.dust, "dusts", false),
            new Form("rod", TagPrefix.rod, "rods", true),
            new Form("plate", TagPrefix.plate, "plates", true),
            new Form("gear", TagPrefix.gear, "gears", true),
            new Form("wire", TagPrefix.wireGtSingle, "wires", true));

    private GmcSmelteryRecipes() {}

    private static Map<Fluid, Material> materials() {
        if (materialsByFluid == null) {
            Map<Fluid, Material> result = new IdentityHashMap<>();
            for (Material material : GTCEuAPI.materialManager.getRegisteredMaterials()) {
                if (material.hasProperty(PropertyKey.INGOT) && material.hasProperty(PropertyKey.FLUID)) {
                    Fluid fluid = material.getFluid();
                    if (fluid != Fluids.EMPTY) result.putIfAbsent(fluid, material);
                }
            }
            materialsByFluid = Collections.unmodifiableMap(result);
        }
        return materialsByFluid;
    }

    public static void adjustTemperature(JsonObject json) {
        GmcSmelteryRules.adjustTemperature(json, GmcSmelteryRecipes::fluidTemperature);
    }

    private static int fluidTemperature(JsonElement json) {
        if (!json.isJsonObject()) return -1;
        JsonObject object = json.getAsJsonObject();
        String fluidId = GmcSmelteryRules.string(object, "fluid");
        if (!fluidId.isEmpty()) {
            ResourceLocation id = ResourceLocation.tryParse(fluidId);
            Material material = id == null ? null : materials().get(BuiltInRegistries.FLUID.get(id));
            return material == null ? -1 : temperature(material);
        }
        // Material-fluid recipes use tags, which are not bound yet during an initial reload.
        String tag = GmcSmelteryRules.string(object, "tag");
        if (!tag.isEmpty()) {
            for (Material material : materials().values()) {
                if (material.getFluidTag().location().toString().equals(tag)) return temperature(material);
            }
        }
        return -1;
    }

    private static int temperature(Material material) {
        return GmcSmelteryRules.temperature(material.getName(), material.hasProperty(PropertyKey.BLAST));
    }

    public static int meltingTemperature(Fluid fluid, int original) {
        Material material = materials().get(fluid);
        return material == null ? original : temperature(material);
    }

    /** KubeJS and network deserialization can bypass RecipeManager.fromJson entirely. */
    private static boolean normalizeLoadedTemperature(Recipe<?> recipe) {
        if (recipe instanceof MeltingRecipe source) {
            int temperature = meltingTemperature(source.getOutput().getFluid(), source.getTemperature());
            if (temperature != source.getTemperature()) {
                ((GmcMeltingRecipeAccessor) source).superflatcore$setTemperature(temperature);
                return true;
            }
        } else if (recipe instanceof MaterialMeltingRecipe) {
            GmcMaterialMeltingRecipeAccessor source = (GmcMaterialMeltingRecipeAccessor) recipe;
            int temperature = meltingTemperature(source.superflatcore$getResult().get().getFluid(), source.superflatcore$getTemperature());
            if (temperature != source.superflatcore$getTemperature()) {
                source.superflatcore$setTemperature(temperature);
                // Expanded EMI/JEI part recipes contain their own temperature and time snapshots.
                source.superflatcore$setMultiRecipes(null);
                return true;
            }
        } else if (recipe instanceof MaterialFluidRecipe source) {
            GmcMaterialFluidRecipeAccessor accessor = (GmcMaterialFluidRecipeAccessor) source;
            int temperature = fluidTemperature(accessor.superflatcore$getFluid().serialize());
            if (temperature >= 0 && temperature != source.getTemperature()) {
                accessor.superflatcore$setTemperature(temperature);
                return true;
            }
        }
        return false;
    }

    private static boolean canMelt(Material material) {
        boolean blast = material.hasProperty(PropertyKey.BLAST);
        return GmcSmelteryRules.canMelt(blast, blast ? material.getProperty(PropertyKey.BLAST).getEUtOverride() : 0);
    }

    public static boolean isBlockedFluid(Fluid fluid) {
        Material material = materials().get(fluid);
        return material != null && !canMelt(material);
    }

    public static boolean isBlockedMelting(JsonObject json) {
        return GmcSmelteryRules.isBlockedMelting(json, GmcSmelteryRecipes::isBlockedFluidJson);
    }

    private static boolean isBlockedFluidJson(JsonElement json) {
        if (!json.isJsonObject()) return false;
        JsonObject object = json.getAsJsonObject();
        String fluid = GmcSmelteryRules.string(object, "fluid");
        ResourceLocation id = ResourceLocation.tryParse(fluid);
        if (!fluid.isEmpty() && id != null) return isBlockedFluid(BuiltInRegistries.FLUID.get(id));
        String tag = GmcSmelteryRules.string(object, "tag");
        return !tag.isEmpty() && materials().values().stream().anyMatch(material ->
                !canMelt(material) && material.getFluidTag().location().toString().equals(tag));
    }

    private static boolean isBlockedMelting(Recipe<?> recipe) {
        if (recipe instanceof MeltingRecipe source) {
            return source.getOutputWithByproducts().stream().flatMap(List::stream)
                    .anyMatch(fluid -> isBlockedFluid(fluid.getFluid()));
        }
        if (recipe instanceof MaterialMeltingRecipe) {
            GmcMaterialMeltingRecipeAccessor source = (GmcMaterialMeltingRecipeAccessor) recipe;
            return isBlockedFluid(source.superflatcore$getResult().get().getFluid())
                    || source.superflatcore$getByproducts().stream().anyMatch(fluid -> isBlockedFluid(fluid.get().getFluid()));
        }
        return false;
    }

    public static void populate(Map<RecipeType<?>, Map<ResourceLocation, Recipe<?>>> recipes,
                                Map<ResourceLocation, Recipe<?>> byName) {
        RecipeType<?> meltingType = TinkerRecipeTypes.MELTING.get();
        Map<ResourceLocation, Recipe<?>> melting = copyOriginals(recipes, meltingType);
        Map<ResourceLocation, Recipe<?>> table = copyOriginals(recipes, TinkerRecipeTypes.CASTING_TABLE.get());
        Map<ResourceLocation, Recipe<?>> basin = copyOriginals(recipes, TinkerRecipeTypes.CASTING_BASIN.get());
        byName.keySet().removeIf(GmcSmelteryRecipes::isGenerated);
        int blocked = melting.size();
        melting.entrySet().removeIf(entry -> {
            if (!isBlockedMelting(entry.getValue())) return false;
            byName.remove(entry.getKey());
            return true;
        });
        blocked -= melting.size();
        int normalized = 0;
        for (Recipe<?> recipe : melting.values()) {
            if (normalizeLoadedTemperature(recipe)) normalized++;
        }
        for (Recipe<?> recipe : recipes.getOrDefault(TinkerRecipeTypes.DATA.get(), Map.of()).values()) {
            if (normalizeLoadedTemperature(recipe)) normalized++;
        }
        Map<Fluid, List<MeltingRecipe>> meltingIndex = new IdentityHashMap<>();
        for (Recipe<?> recipe : melting.values()) {
            if (recipe instanceof MeltingRecipe source) {
                meltingIndex.computeIfAbsent(source.getOutput().getFluid(), unused -> new ArrayList<>()).add(source);
            }
        }

        // These materials no longer depend on GMC's tool/armor whitelist.
        int melted = 0;
        for (Map.Entry<Fluid, Material> entry : materials().entrySet()) {
            Material material = entry.getValue();
            if (!canMelt(material)) continue;
            for (Form form : FORMS) {
                ItemStack output = ChemicalHelper.get(form.prefix(), material);
                if (output.isEmpty() || hasMelting(meltingIndex.getOrDefault(entry.getKey(), List.of()), material, form, output)) continue;
                int amount = form.amount(material);
                if (amount <= 0) continue;
                ResourceLocation id = id(material, "melting/" + form.name());
                MeltingRecipe recipe = new MeltingRecipe(id, "", Ingredient.of(output),
                        FluidOutput.fromFluid(entry.getKey(), amount), temperature(material),
                        IMeltingRecipe.calcTimeForAmount(temperature(material), amount), List.of());
                melting.put(id, recipe);
                meltingIndex.computeIfAbsent(entry.getKey(), unused -> new ArrayList<>()).add(recipe);
                byName.put(id, recipe);
                melted++;
            }
        }

        // Only fluids with a real item melting recipe can receive supplementary casts.
        Map<Fluid, Integer> meltable = new IdentityHashMap<>();
        for (Recipe<?> recipe : melting.values()) {
            if (recipe instanceof MeltingRecipe source && materials().containsKey(source.getOutput().getFluid())) {
                meltable.merge(source.getOutput().getFluid(), source.getTemperature(), Math::min);
            }
        }
        int cast = 0;
        Map<Fluid, List<ItemCastingRecipe>> castingIndex = new IdentityHashMap<>();
        indexCasting(table.values(), castingIndex);
        indexCasting(basin.values(), castingIndex);
        for (Map.Entry<Fluid, Integer> entry : meltable.entrySet()) {
            Material material = materials().get(entry.getKey());
            for (Form form : FORMS) {
                if (!form.castable()) continue;
                ItemStack output = ChemicalHelper.get(form.prefix(), material);
                int amount = form.amount(material);
                if (output.isEmpty() || amount <= 0) continue;
                Map<ResourceLocation, Recipe<?>> target = form.name().equals("block") ? basin : table;
                for (boolean singleUse : form.name().equals("block") ? new boolean[]{false} : new boolean[]{false, true}) {
                    String castTag = "tconstruct:casts/" + (singleUse ? "single_use/" : "multi_use/") + form.name();
                    Ingredient mold = form.name().equals("block") ? Ingredient.EMPTY
                            : Ingredient.of(net.minecraft.tags.ItemTags.create(new ResourceLocation(castTag)));
                    if (hasCasting(castingIndex.getOrDefault(entry.getKey(), List.of()), material, form, output, castTag, singleUse)) continue;
                    ResourceLocation id = id(material, "casting/" + form.name() + (singleUse ? "_sand_cast" : "_gold_cast"));
                    ItemCastingRecipe recipe = new ItemCastingRecipe(form.name().equals("block")
                            ? TinkerSmeltery.basinRecipeSerializer.get() : TinkerSmeltery.tableRecipeSerializer.get(),
                            id, "", mold, FluidIngredient.of(entry.getKey(), amount), ItemOutput.fromStack(output),
                            ICastingRecipe.calcCoolingTime(entry.getValue(), amount), singleUse, false);
                    target.put(id, recipe);
                    byName.put(id, recipe);
                    cast++;
                }
            }
        }
        recipes.put(meltingType, Collections.unmodifiableMap(melting));
        recipes.put(TinkerRecipeTypes.CASTING_TABLE.get(), Collections.unmodifiableMap(table));
        recipes.put(TinkerRecipeTypes.CASTING_BASIN.get(), Collections.unmodifiableMap(basin));
        LOGGER.info("Completed GMC smeltery recipes: {} additional melting recipes, {} additional casting recipes, {} EV+ melting recipes removed, {} loaded temperatures corrected",
                melted, cast, blocked, normalized);
    }

    private static boolean hasMelting(Iterable<MeltingRecipe> recipes, Material material, Form form, ItemStack output) {
        for (MeltingRecipe source : recipes) {
            if (GmcSmelteryRules.containsIngredient(source.getInput().toJson(), form.tag(material), itemId(output))) return true;
        }
        return false;
    }

    private static boolean hasCasting(Iterable<ItemCastingRecipe> recipes, Material material, Form form,
                                       ItemStack output, String castTag, boolean consumed) {
        for (ItemCastingRecipe source : recipes) {
            boolean basin = source.getType() == TinkerRecipeTypes.CASTING_BASIN.get();
            if (basin != form.name().equals("block") || source.isConsumed() != consumed || source.switchSlots()) continue;
            if (!GmcSmelteryRules.containsIngredient(source.getResult().serialize(false), form.tag(material), itemId(output))) continue;
            if (form.name().equals("block") ? !source.hasCast()
                    : GmcSmelteryRules.containsIngredient(source.getCast().toJson(), castTag,
                    "tconstruct:" + form.name() + (consumed ? "_sand_cast" : "_cast"))) return true;
        }
        return false;
    }

    private static void indexCasting(Iterable<Recipe<?>> recipes, Map<Fluid, List<ItemCastingRecipe>> index) {
        for (Recipe<?> recipe : recipes) {
            if (recipe instanceof ItemCastingRecipe casting) indexCastingFluid(casting.getFluid().serialize(), casting, index);
        }
    }

    private static void indexCastingFluid(JsonElement json, ItemCastingRecipe recipe, Map<Fluid, List<ItemCastingRecipe>> index) {
        if (json.isJsonArray()) {
            for (JsonElement child : json.getAsJsonArray()) indexCastingFluid(child, recipe, index);
        } else if (json.isJsonObject()) {
            JsonObject object = json.getAsJsonObject();
            String fluid = GmcSmelteryRules.string(object, "fluid");
            ResourceLocation fluidId = ResourceLocation.tryParse(fluid);
            if (!fluid.isEmpty() && fluidId != null) {
                Fluid value = BuiltInRegistries.FLUID.get(fluidId);
                if (materials().containsKey(value)) index.computeIfAbsent(value, unused -> new ArrayList<>()).add(recipe);
            } else {
                String tag = GmcSmelteryRules.string(object, "tag");
                for (Map.Entry<Fluid, Material> entry : materials().entrySet()) {
                    if (entry.getValue().getFluidTag().location().toString().equals(tag)) {
                        index.computeIfAbsent(entry.getKey(), unused -> new ArrayList<>()).add(recipe);
                    }
                }
            }
        }
    }

    private static Map<ResourceLocation, Recipe<?>> copyOriginals(Map<RecipeType<?>, Map<ResourceLocation, Recipe<?>>> recipes,
                                                                  RecipeType<?> type) {
        Map<ResourceLocation, Recipe<?>> result = new LinkedHashMap<>(recipes.getOrDefault(type, Map.of()));
        result.keySet().removeIf(GmcSmelteryRecipes::isGenerated);
        return result;
    }

    private static String itemId(ItemStack stack) {
        return BuiltInRegistries.ITEM.getKey(stack.getItem()).toString();
    }

    private static ResourceLocation id(Material material, String suffix) {
        ResourceLocation materialId = material.getResourceLocation();
        return SuperFlatCore.id(GENERATED + materialId.getNamespace() + "/" + materialId.getPath() + "/" + suffix);
    }

    private static boolean isGenerated(ResourceLocation id) {
        return id.getNamespace().equals(SuperFlatCore.MODID) && id.getPath().startsWith(GENERATED);
    }

    private record Form(String name, TagPrefix prefix, String tagPrefix, boolean castable) {
        int amount(Material material) {
            return (int) (prefix.getMaterialAmount(material) * 144 / GTValues.M);
        }

        String tag(Material material) {
            return "forge:" + tagPrefix + "/" + material.getName();
        }
    }
}
