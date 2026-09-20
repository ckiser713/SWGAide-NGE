# Source of Truth

## SWGAide baseline

`ckiser713/SWGAide-NGE` was forked from:

`twistedatrocity/SWGAide-NGE@7f520ee508221cce3ac3e8871919e74de9fcb0c2`

The foundation branch must remain additive relative to this baseline.

## SWG Infinity crafting authority

Initial rules authority:

`swginfinity/public@6b6ac3726aaa3c293fca83911850d3a02e2adb4f`

High-value source paths include:

- `MMOCoreORB/src/server/zone/managers/crafting/CraftingManagerImplementation.cpp`
- `.../crafting/labratories/SharedLabratory.cpp`
- `.../crafting/labratories/ResourceLabratory.cpp`
- `.../crafting/labratories/GeneticLabratory.cpp`
- `.../crafting/labratories/DroidLabratory.cpp`
- `MMOCoreORB/src/server/zone/objects/player/sessions/crafting/CraftingSessionImplementation.cpp`
- `MMOCoreORB/src/server/zone/objects/manufactureschematic/craftingvalues/CraftingValues.cpp`
- `MMOCoreORB/src/templates/crafting/AttributesMap.h`
- `MMOCoreORB/src/templates/crafting/resourceweight/ResourceWeight.h`
- `MMOCoreORB/src/templates/intangible/DraftSchematicObjectTemplate.cpp`
- `MMOCoreORB/src/templates/crafting/draftslot/DraftSlot.h`
- `MMOCoreORB/src/server/zone/objects/manufactureschematic/ingredientslots/ResourceSlot.h`
- `MMOCoreORB/src/server/zone/objects/manufactureschematic/ingredientslots/ComponentSlot.h`
- `MMOCoreORB/src/server/zone/objects/resource/ResourceSpawnImplementation.cpp`
- `MMOCoreORB/src/server/zone/objects/tangible/component/ComponentImplementation.cpp`
- `MMOCoreORB/src/server/zone/managers/loot/LootManagerImplementation.cpp`

## Precedence

1. pinned Infinity source
2. accepted golden fixtures derived from that source
3. normalized Infinity ruleset
4. InfinityCraftEngine
5. SWGAide adapters
6. existing SWGAide convenience calculations
7. external guides/community assumptions

A disagreement is evidence to investigate; it is not permission to average two behaviors.
