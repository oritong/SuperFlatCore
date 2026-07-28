# SuperFlatCore

Minecraft Forge 1.20.1 core mod for the pack **Diamond Continent Jiahao Version**.

Current version: `1.0.20-hotfix3`

## Features

- Adds `superflatcore:super_infinite_water_cover`, a GTCEu cover that fills attached fluid containers with up to `Integer.MAX_VALUE` mB of water once per second.
- Adds five air intake covers that supply GTCEu air to attached fluid containers once per second in 20-tick batches: basic `30 mB/t` (`600 mB/s` actual), advanced `150 mB/t` (`3,000 mB/s` actual), elite `500 mB/t` (`10,000 mB/s` actual), ultimate `1,000 mB/t` (`20,000 mB/s` actual), and creative `Integer.MAX_VALUE mB/t` capped at `Integer.MAX_VALUE` per batch.
- Adds configurable automatic maintenance hatches: base, cleanroom, sterile cleanroom, and creative sterile cleanroom. Base/cleanroom/sterile range is `0.5x` to `1.5x`; creative sterile range is `0.001x` to `100x`. Normal click changes `0.01x`, Shift-click changes `0.10x`, Ctrl-click changes `1.00x`, and Ctrl+Shift jumps to the min/max.
- Expands GTCEu dual input/output hatch registration tiers from LuV+ to LV+ without changing recipe generation, and fixes low-tier dual hatch slot/capacity scaling.
- Adds SuperFlatCore 64A energy/dynamo hatches from HV onward, plus 262,144A, 1,048,576A, and 4,194,304A laser target/source hatches from IV onward.
- Adds GTMThings-compatible wireless 64A energy/dynamo hatches from HV onward and wireless 262,144A, 1,048,576A, and 4,194,304A laser target/source hatches from IV onward. These use GTMThings' wireless energy pool.
- Changes Industrial Foregoing's Infinity Nuke ignition item from flint and steel to `ifeu:dragon_star`, consuming one dragon star when ignition succeeds.
- Adds `superflatcore:employee_companion`, which summons GregTech employee holographic projections using Minopp auto players, cycles their focus mode while held, and removes them with sneak-right-click.
- Adds the `superflatcore:machine_soul_joy` effect. While active, jumping on GTCEu machines with recipe logic has a 35% chance to spawn a short Time in a Bottle acceleration burst if the machine is not already accelerated.
- Changes Industrial Foregoing's Latex Processing Unit latex tank to accept `oritong:latex`.
- Adds a Jade tooltip overlay for Time in a Bottle accelerators, with a config toggle and block/entity support. It shows acceleration multiplier and remaining time.
- Adds `GTRecipeModifiers.MULTIPLERECIPE` for GTCEu KubeJS multiblock registrations. Add it to `.recipeModifiers([...])` to let a workable multiblock run multiple matching recipes in one combined runtime batch.
- Adds Botania `twig_wand` behavior to Ars Nouveau's `ars_nouveau:dominion_wand`. The dominion wand can use Botania wand interactions, and sneak-right-clicking air toggles the Botania wand mode while refreshing the displayed item name. The displayed name keeps the normal dominion wand base name and appends the Botania mode.
- Sneak-right-clicking while holding `beyonddimensions:shattered_space_time_crystallization` in the main hand consumes one crystal and adds 15 minutes to the first Time in a Bottle found in the player's inventory or Curios slots.
- Adds Embers `tinker_hammer` behavior to items tagged `#gtceu:tools/crafting_mallets`, including recipe tag compatibility, Embers pipe toggling, and ember emitter/receptor linking.
- Adds `oritong:latex`, a custom latex fluid with source/flowing forms, a placeable fluid block, bucket, and fluid block render resources for recipe viewers. It has no water or lava fluid tags and flows slightly faster than lava.
- Directly changes RainRot's three Five Pebbsi drinks to always be drinkable at full hunger. Drinking one grants Speed I for 1 minute and Machine Soul Joy for 3 minutes, then returns `gtceu:magnalium_foil` and `gtceu:aluminium_ring`.
- Adds the SuperFlatCore logo to the Forge mod metadata.

## Build

```bat
.\gradlew.bat clean build
```

The built jar will be generated under `build/libs`.

## Dependencies

- Forge `1.20.1-47.2.0`
- Minecraft `1.20.1`
- GTM / GregTech Modern (`gtceu`) `7.5.3+`
- GTMThings
- Industrial Foregoing `3.5.21+`
- IFEU (`ifeu`)
- Minopp
- Time In A Bottle (`tiab`) `4.0.4+`
- Curios `5.14.1+`
- Jade `11.13.1+` (optional, for the TIAB tooltip overlay)
- Botania `1.20.1-448+`
- Ars Nouveau `4.12.7+`
- Embers Rekindled `1.4.7+`
- RainRot `1.0.0+`
