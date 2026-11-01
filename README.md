# SuperFlatCore

Minecraft Forge 1.20.1 core mod for the pack **Diamond Continent Jiahao Version**.

Current version: `1.4.5-hotfix1`

## Features

- **Steel Component Assembly Line:** An 11 x 3 x 3, fully rotatable multiblock with a fixed parallel limit of 32 and standard non-perfect electric overclocking. Its `superflatcore:component_assembly_line` recipe type accepts up to nine unordered item inputs, one fluid input and one item output. On recipe reload or synchronization, assembler recipes producing GT motors, pistons, pumps, conveyors, robot arms, field generators, emitters or sensors are copied here with their original assembler duration and EU/t by 0.9, rounded to integer units. The machine applies a 2x runtime penalty, while the recipe viewer continues to show the original assembler time. Original assembler recipes, ingredients, quantities, conditions and output NBT are preserved. Copies reflect the final scripted recipes and are rebuilt without accumulating multipliers. The structure requires exactly nine ULV input buses, one single-fluid input hatch, one item output bus, one energy hatch, one muffler and one maintenance hatch. Muffler tier limits the output component tier: MV components require an MV or higher muffler even when their recipe consumes LV power. Recipe voltage, overclocking and combined parallel EU demand are governed by the energy hatch. A full or obstructed muffler pauses work without consuming tick inputs or discarding batch progress. No machine tooltip is supplied.

- **Tool Welding Assembler:** LV-and-up single-block machines with the assembler front overlay, nine item input slots, one item output and two fluid input tanks. GT's generated manual tools (including flint tools, mallets and plungers), electric tools and netherite upgrades are redirected here. Original material quantities and output NBT are retained; crafting-tool catalysts are removed. Vanilla stick ingredients are replaced with `gtceu:treated_wood_rod`. Manual recipes use ULV, electric recipes use their tool tier, at 80% of nominal tier voltage (rounded to integer EU/t) for 100 ticks without overclocking. Diamond harvest level and higher produces two alternatives: 576 mB tin or 288 mB soldering alloy. Every electric tool recipe additionally consumes 288 mB polyethylene. Configured circuits distinguish manual tool shapes with identical remaining materials. Electric assembly transfers the input power unit's capacity and charge; netherite upgrades retain the base tool's NBT. Machine crafting uses `ABC/DED/FGF`: conveyor, robot arm, pump, two emitters, hull, two single cables, and a double steel plate. Components follow the machine tier; the double steel plate is fixed. Recipe-viewer titles are localized and voltage prefixes use GT's tier colors.

- **GTNN Component Assembly Line:** Corrects the component tier lookup after GT's missing-tier fallbacks are expanded, so LV components retain LV recipe voltage and casing requirements instead of being promoted to MAX.

### Machines and parts

- **Combustion Generator fuel:** GregTech combustion generators accept 4 mB of `gtceu:creosote` for 12 ticks (0.6 seconds) at 32 EU/t, generating 384 EU per batch.
- **Large Coke Oven:** An 11x11x11 multiblock using existing GT coke oven recipes with up to 32 parallel operations, without adding EU consumption or changing recipe duration. Coke bricks in the lower B positions accept item/fluid input and output parts; the top E position requires exactly one muffler hatch. The controller and formed hatches use the GT coke-brick casing appearance.
- **Wind Generator:** A GregTech-style Mekanism wind generator that outputs EU. It requires open sky and Y >= 0. Height efficiency rises from 35% at Y=0 to 100% at Y=128, then remains at 100%. Output is `base output × height efficiency × rotor wind output`. The rotor slot accepts SFC wind rotors. Defaults are 60 EU/t base output, 6 durability lost per second while generating, and 2 per second when wind is available but the buffer is full; all three values are configurable.
- **Mana Oppressor:** A GT multiblock with Botania mana infusion and mana oppression modes. It consumes `technicalflowers:mana` at 1 mB per mana. Its mana input hatch has a single spark slot in the left GUI sidebar. Installing a spark enables input from mana bursts and nearby mana pools with sparks in the selected color network (white by default), using Botania's 12-block scan range and up to 1,000 mana per pool per tick. Isolated sparks are excluded. The hatch creates no spark entity and never sends mana back; removing the spark or filling the hatch stops network intake. Infusion uses recipe pedestals without consuming them, runs for 40 ticks, supports item-count and mixed-recipe parallel processing, and cannot use a parallel hatch. Oppression mode uses its own recipe type and parallel hatches. The machine has no EU input or output. Both modes display mana requirements in the machine GUI and JEI.
- **Heat Exchanger:** Adds a GT multiblock recipe type for heat exchange recipes.
- **IV Supercritical Steam Turbine:** Includes a turbine casing and a built-in recipe that consumes 1,280 mB of supercritical steam per 30 ticks, generates 128 EU/t, and returns 8 mB of distilled water.
- **Radioactive fluid handling:** Adds Create-connected radioactive tanks (16,000 mB per block) and GT radioactive fluid input/output hatches (256,000 mB). Radiation is released when stored radioactive fluid is spilled or relevant storage is dismantled; AE2 storage and AtomicEmbersDecayed reactor integrations are also supported.
- **GT parts:** Adds configurable automatic maintenance hatches, LV-and-up dual input/output hatches, high-amperage and wireless energy/laser hatches, mana and radioactive fluid hatches, and GT cover attachments for infinite water and air intake.

### World resources

- Adds small, medium, large, and full clusters for earth, wood, water, fire, and metal corundum. Breaking a full cluster always drops 2-3 matching GT gems, independent of tool and Fortune.
- Create spouts can grow clusters by pouring at least 325 mB of mana. The first growth requires the matching Botania metamorphic stone above its corresponding base block; later stages can also grow on blackstone. Stage growth chances are 75%, 65%, and 55%. Each successful pour has a 22% chance to turn the metamorphic stone into blackstone.

| Element | Base block |
| --- | --- |
| Water | `botania:manasteel_block` |
| Wood | `botania:mana_diamond_block` |
| Fire | `tconstruct:pig_iron_block` |
| Earth | `embers:dawnstone_block` |
| Metal | `ars_nouveau:source_gem_block` |
- Replaces Botania's Terrestrial Agglomeration Plate structure. When formed, a mana-glass Menger sponge appears above the plate, tilted 45 degrees, rotating and changing color, with its size cycling between 1.75x and 2.5x.

### Mod integrations

- **Pack recipe restrictions:** Removes all three Iron Furnaces generator categories (`ironfurnaces:generator_regular`, `generator_blasting`, and `generator_smoking`), including dynamically generated fuel/food entries; IFEU dragon/dragon-star generator, infuser, and structure-information categories; Industrial Foregoing biofuel recipes; selected Mekanism processing categories; and Tinkers' Construct modifiers, tool building, and modifier worktable recipes from EMI. Corresponding registered processing recipes are removed on reload and synchronization. Industrial Foregoing machine-output displays retain only meat, pink slime, and dry rubber. Mekanism fission reactors no longer check or form their multiblock structure, and nutritional liquifiers no longer generate food recipes.
- **Botania and Ars Nouveau:** Sets Source Jar capacity to 128,000 Source, adds Botania wand interactions to the Dominion Wand, launches a fireball when Fire Essence is used, and connects Botania infusion recipes to the Mana Oppressor.
- **Technical Flowers:** Functional flowers in Botania's `botania:floating_flowers` block tag can also draw mana from fluid pipes adjacent to their sides, using Technical Flowers' own pipe checks and mana conversion.
- **Occultism:** Its dimensional mineshaft uses GTNN's dimension ore pool instead of its original drops; raw bauxite and raw coal are excluded. Otherstone trades require blackstone, Otherworld Tree trades require a Pure Daisy, and Manasteel Shears are needed to obtain leaves from Otherworld Leaves.
- Occultism's outdated miner-result recipes are hidden from EMI because the Dimensional Mineshaft now uses GTNN's dimension-specific ore pool.
- **Mekanism and AtomicEmbersDecayed:** Prevents Mekanism Energized Smelters from generating vanilla smelting recipes. Adds a standard reactor structure and Mekanism radiation handling for radioactive fluids and reactor accidents; EzUnclear can gate reactor accidents behind its confirmation flow.
- **MoreBurners:** The Ember Burner attempts to consume `100 ×` its configured Ember Cost every five seconds, then runs for five seconds. A running burner can heat an Embers Mini Boiler below it.
- **Create, GregTech, and Industrial Foregoing:** Adds wind rotor material recipes and corundum growth; updates the Infinity Nuke ignition item and Latex Processing Unit fluid; hides selected automatically generated Industrial Foregoing recipes from JEI.
- Industrial Foregoing speed, efficiency, processing, and range addons work in upgrade slots even when obtained without NBT. Their original upgrade values are initialized when needed, preserving existing item data and the machines' restrictions on duplicate or unsupported upgrades.
- This also applies to Industrial Foregoing: Extra Upgrades (`ifeu`) tier 3-6 speed, efficiency, and processing addons, with their original upgrade values.
- **Other integrations:** RainRot drink effects and a 64-item Data Pearl stack limit; GTNN mining output; AE2 radioactive-fluid restrictions; Super Factory Manager catalyst display; GT multiblock preview panning; spring bounce behavior; and TIAB/Jade machine effect displays.
- **Merged Diamond Core fixes:** AE2 pattern amount limits and ExtendedAE Infinity Cell handling; GT parallel hatch initialization; Pipez energy transfer into GT machines; GTMThings wireless FE input; BetterGTAE pattern hatch/assembler behavior; EMI recipe filling for TConstruct crafting stations and Botania fluid outputs; and fixes for ME Requester fields, GTMQOL data-stick rendering, and Industrial Foregoing JEI subtype lookup.

### Player features and recipe tools

- **Chat flight:** Send `中国人能飞` in chat to enable and immediately enter creative-style flight, or `曼巴out` to stop it. No operator permission is required, and the player's game mode stays unchanged. Flight permission persists through reconnects, respawns, and dimension changes until disabled. Normal flight controls still apply, and disabling the feature preserves Creative/Spectator mode flight permission.
- **`/spawn`:** Teleports a player to a safe standing position near the Overworld world spawn. It can be used without an operator permission level.
- **Structure Detect:** Reports GT multiblock formation errors and highlights the problem position. Sneak-right-clicking air toggles Cable Facades disguise rendering.
- **Employee Companion:** Summons and manages Minopp employee projections.
- **Machine Soul effects:** Adds machine speed effects triggered by the corresponding potion effects; Jade can show the multiplier and remaining time.
- **Don't Touch Me:** A staged client-side prank item with visual effects and a final local-client crash.
- Exposes `GTRecipeModifiers.MULTIPLERECIPE` for multi-recipe batch processing and `GTRecipeModifiers.PROPORTIONAL_INPUT` for integer-multiple input batches.

## Configuration

Common configuration file: `config/superflatcore-common.toml`.

| Setting | Default | Description |
| --- | ---: | --- |
| `windGeneratorBaseProduction` | 60 EU/t | Wind Generator base output |
| `windGeneratorActiveRotorWear` | 6 per second | Rotor durability loss while generating |
| `windGeneratorFullRotorWear` | 2 per second | Rotor durability loss while the energy buffer is full and wind conditions are met |

## Build

```bat
.\gradlew.bat clean build
```

The jar is generated in `build/libs`.

## Required dependencies

SFC is a modpack core mod and is not intended to run without its companion mods. Required mod IDs and version ranges are declared in `src/main/resources/META-INF/mods.toml`.

- **Platform:** Minecraft `1.20.1`, Forge `47+`
- **GregTech:** GTCEu (`gtceu`) `7.5.3+`, GTMThings (`gtmthings`), GTNN (`gtnn`) `1.3.9+`, GTM QOL (`gtmqol`)
- **Technology:** Create `6.0.8+`, Mekanism and Mekanism Generators `10.4.16+`, AE2 `15.4.10+`, ExtendedAE (`expatternprovider`), Industrial Foregoing `3.5.21+`, IFEU, Pipez, BetterGTAE
- **Magic and resources:** Botania, Ars Nouveau, Occultism `1.158.0+`, Embers `1.4.7+`, Technical Flowers `1.3.1+`, MoreBurners `1.20.1-0.3.5+`, AtomicEmbersDecayed, EzUnclear, RainRot
- **Other integrations:** Minopp, ME Requester, GTMQOL, Time in a Bottle `4.0.4+`, Curios `5.14.1+`, Jade `11.13.1+`, Beyond Dimensions `0.4.17+`, Super Factory Manager
- **Client-side required mods:** Cable Facades `2.1.3+`, JEI, EMI `1.1.24+`, Tinkers' Construct and Mantle
- **Recipe restrictions:** Iron Furnaces `4.1.8+`; Tinkers' Construct is also required on the server.
