# SuperFlatCore

Minecraft Forge 1.20.1 core mod for the pack **Diamond Continent Jiahao Version**.

Current version: `1.3.38-hotfix4`

## Features

- **Steel Component Assembly Line:** An 11 x 3 x 3, fully rotatable multiblock with a fixed parallel limit of 32 and standard non-perfect electric overclocking. Its `superflatcore:component_assembly_line` recipe type accepts up to nine unordered item inputs, one fluid input and one item output. On recipe reload or synchronization, assembler recipes producing GT motors, pistons, pumps, conveyors, robot arms, field generators, emitters or sensors are copied here with their original assembler duration and EU/t by 0.9, rounded to integer units. The machine applies a 2x runtime penalty, while the recipe viewer continues to show the original assembler time. Original assembler recipes, ingredients, quantities, conditions and output NBT are preserved. Copies reflect the final scripted recipes and are rebuilt without accumulating multipliers. The structure requires exactly nine ULV input buses, one single-fluid input hatch, one item output bus, one energy hatch, one muffler and one maintenance hatch. Muffler tier limits the output component tier: MV components require an MV or higher muffler even when their recipe consumes LV power. Recipe voltage, overclocking and combined parallel EU demand are governed by the energy hatch. A full or obstructed muffler pauses work without consuming tick inputs or discarding batch progress. No machine tooltip is supplied.

- **Tool Welding Assembler:** LV-and-up single-block machines with the assembler front overlay, nine item input slots, one item output and two fluid input tanks. GT's generated manual tools (including flint tools, mallets and plungers), electric tools and netherite upgrades are redirected here. Original material quantities and output NBT are retained; crafting-tool catalysts are removed. Vanilla stick ingredients are replaced with `gtceu:treated_wood_rod`. Manual recipes use ULV, electric recipes use their tool tier, at 80% of nominal tier voltage (rounded to integer EU/t) for 100 ticks without overclocking. Diamond harvest level and higher produces two alternatives: 576 mB tin or 288 mB soldering alloy. Every electric tool recipe additionally consumes 288 mB polyethylene. Configured circuits distinguish manual tool shapes with identical remaining materials. Electric assembly transfers the input power unit's capacity and charge; netherite upgrades retain the base tool's NBT. Machine crafting uses `ABC/DED/FGF`: conveyor, robot arm, pump, two emitters, hull, two single cables, and a double steel plate. Components follow the machine tier; the double steel plate is fixed. Recipe-viewer titles are localized and voltage prefixes use GT's tier colors.

- **GTNN Component Assembly Line:** Corrects the component tier lookup after GT's missing-tier fallbacks are expanded, so LV components retain LV recipe voltage and casing requirements instead of being promoted to MAX.

- **Pack recipe restrictions:** Removes all three Iron Furnaces generator categories (`ironfurnaces:generator_regular`, `generator_blasting`, and `generator_smoking`), including dynamically generated fuel/food entries; IFEU dragon/dragon-star generator, infuser, and structure-information categories; Industrial Foregoing biofuel recipes; selected Mekanism processing categories; and Tinkers' Construct modifiers, tool building, and modifier worktable recipes from EMI. Corresponding registered processing recipes are removed on reload and synchronization. Industrial Foregoing machine-output displays retain only meat, pink slime, and dry rubber. Mekanism fission reactors no longer check or form their multiblock structure, and nutritional liquifiers no longer generate food recipes.
- **Large Coke Oven:** An 11x11x11 multiblock using existing GT coke oven recipes with up to 32 parallel operations, without adding EU consumption or changing recipe duration. Coke bricks in the lower B positions accept item/fluid input and output parts; the top E position requires exactly one muffler hatch. The controller and formed hatches use the GT coke-brick casing appearance.
- **Combustion Generator fuel:** GregTech combustion generators accept 4 mB of `gtceu:creosote` for 12 ticks (0.6 seconds) at 32 EU/t, generating 384 EU per batch.
- Adds `superflatcore:super_infinite_water_cover`, a GTCEu cover that fills attached fluid containers with up to `Integer.MAX_VALUE` mB of water once per second.
- Adds five air intake covers that supply GTCEu air to attached fluid containers once per second in 20-tick batches: basic `30 mB/t` (`600 mB/s` actual), advanced `150 mB/t` (`3,000 mB/s` actual), elite `500 mB/t` (`10,000 mB/s` actual), ultimate `1,000 mB/t` (`20,000 mB/s` actual), and creative `Integer.MAX_VALUE mB/t` capped at `Integer.MAX_VALUE` per batch.
- Adds configurable automatic maintenance hatches: base, cleanroom, sterile cleanroom, and creative sterile cleanroom. Base/cleanroom/sterile range is `0.5x` to `1.5x`; creative sterile range is `0.001x` to `100x`. Normal click changes `0.01x`, Shift-click changes `0.10x`, Ctrl-click changes `1.00x`, and Ctrl+Shift jumps to the min/max.
- Expands GTCEu dual input/output hatch registration tiers from LuV+ to LV+ without changing recipe generation, and fixes low-tier dual hatch slot/capacity scaling.
- Adds SuperFlatCore 262,144A, 1,048,576A, and 4,194,304A laser target/source hatches from IV onward.
- Adds GTMThings-compatible wireless 64A energy/dynamo hatches from HV onward and wireless 262,144A, 1,048,576A, and 4,194,304A laser target/source hatches from IV onward. These use GTMThings' wireless energy pool.
- Changes Industrial Foregoing's Infinity Nuke ignition item from flint and steel to `ifeu:dragon_star`, consuming one dragon star when ignition succeeds.
- Adds `superflatcore:employee_companion`, which summons GregTech employee holographic projections using Minopp auto players, cycles their focus mode while held, and removes them with sneak-right-click. Summoned projections receive localized random codes like `Employee Projection#A67H`, with a 0.5% chance to be named `ChocoFox`.
- Adds the `superflatcore:machine_soul_joy` effect. While active, jumping on GTCEu machines with recipe logic has a 35% chance to spawn a 2-4 second Time in a Bottle acceleration burst if the machine is not already accelerated. Generator-type machines instead receive a short x0.5 (75%) or x0.25 (25%) slowdown, replacing a Machine Soul Displeasure acceleration if present.
- Adds `superflatcore:machine_soul_displeasure`. Every 5 seconds, it applies a 30-second x0.5 (75%) or x0.25 (25%) recipe-logic slowdown to eligible GTCEu machines in the surrounding 3x3 chunk area, excluding machines already affected by Time in a Bottle. Generator-type machines instead receive a 30-second x2 (75%) or x4 (25%) Time in a Bottle acceleration. Slowdowns use a red Time in a Bottle-style block-surface multiplier display.
- Changes Industrial Foregoing's Latex Processing Unit latex tank to accept `oritong:latex`.
- Adds a Jade tooltip overlay for Time in a Bottle accelerators and Machine Soul Displeasure slowdowns, with a config toggle and block/entity support. It shows the multiplier and remaining time.
- Adds `GTRecipeModifiers.MULTIPLERECIPE` for GTCEu KubeJS multiblock registrations. Add it to `.recipeModifiers([...])` to let a workable multiblock run multiple matching recipes in one combined runtime batch.
- Adds `GTRecipeModifiers.PROPORTIONAL_INPUT`. It requires all consumable item and fluid inputs to be supplied as the same whole-number multiple of the recipe quantities; a mismatched batch is consumed but produces no outputs.
- Adds `dont_touch_me:dont_touch_me`. It resets to stage 0 when a player enters the world, advances one stage per right-click, changes texture per stage, plays glass break sounds, applies grayscale/nausea/purple-black screen effects, resets after 10 seconds of inactivity, and crashes only the local client if pressed again at stage 5.
- Adds Botania `twig_wand` behavior to Ars Nouveau's `ars_nouveau:dominion_wand`. The dominion wand can use Botania wand interactions, and sneak-right-clicking air toggles the Botania wand mode while refreshing the displayed item name. The displayed name keeps the normal dominion wand base name and appends the Botania mode.
- Right-clicking with `ars_nouveau:fire_essence` consumes one essence and launches a vanilla small fireball in the player's facing direction.
- Sneak-right-clicking while holding `beyonddimensions:shattered_space_time_crystallization` in the main hand consumes one crystal and adds 15 minutes to the first Time in a Bottle found in the player's inventory or Curios slots.
- Adds Embers `tinker_hammer` behavior to items tagged `#gtceu:tools/crafting_mallets`, including recipe tag compatibility, Embers pipe toggling, ember emitter/receptor linking, and AtomicEmbersDecayed centrifuge/reactor operation.
- Integrates AtomicEmbersDecayed reactor accidents with EzUnclear: Atomic reactor meltdowns enter EzUnclear's confirmation queue and execute only after its normal chat confirmation.
- Adds `oritong:latex`, a custom latex fluid with source/flowing forms, a placeable fluid block, bucket, and fluid block render resources for recipe viewers. It has no water or lava fluid tags and flows slightly faster than lava.
- Directly changes RainRot's three Five Pebbsi drinks to always be drinkable at full hunger. Drinking one grants Speed I for 1 minute and Machine Soul Joy for 3 minutes, then returns `gtceu:magnalium_foil` and `gtceu:aluminium_ring`.
- Adds the SuperFlatCore logo to the Forge mod metadata.
- Changes Super Factory Manager's Falling Anvil form JEI display so the consumed catalyst block is shown as `gtceu:steel_block`.

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
- Super Factory Manager
- JEI `15.20.0.134+` (client, for the SFM Falling Anvil form display patch)

- **Recipe restrictions:** Iron Furnaces 4.1.8+, Mekanism and Mekanism Generators 10.4.16+, Tinkers' Construct and Mantle are required on both sides; EMI 1.1.24+ is required on the client.
