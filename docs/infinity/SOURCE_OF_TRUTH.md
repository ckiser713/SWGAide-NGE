# Source of Truth

## Product architecture

The application feature is a **generic SWGAide Crafting Simulator with server rules
modules**.

The selected SWGAide server determines both:

1. resource data source;
2. eligible rules provider.

Do not mix servers in normal mode.

## SWGAide baseline

`ckiser713/SWGAide-NGE` was forked from:

`twistedatrocity/SWGAide-NGE@7f520ee508221cce3ac3e8871919e74de9fcb0c2`

The feature branch remains additive relative to this baseline.

SWGAide is authority for:

- selected galaxy/server;
- current resource feed;
- loaded/recent resource state;
- resource inventory and quantities;
- schematic navigation/display;
- server-specific custom schematic/override presentation;
- existing Laboratory/Test Bench behavior.

## Rules-provider authority

Crafting Simulator selects rules through `ServerRulesRegistry`.

Precedence:

1. exact server;
2. explicitly verified family;
3. no provider/resource-only.

`precu` or `nge` labels alone are not crafting-rule evidence.

## SWG Infinity first server module

Initial exact rules authority:

`swginfinity/public@6b6ac3726aaa3c293fca83911850d3a02e2adb4f`

Target SWGAide server ID:

`154`

High-value Infinity source paths include:

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

## Native resource APIs

For selected galaxy:

- current: `SWGResourceManager.getSpawning(galaxy)`
- loaded current/recent: `SWGResourceManager.getSet(galaxy)`
- inventory: `SWGResController.inventory(galaxy)`

Do not create a second resource database or bypass the private resource cache.

## Precedence within a server module

1. pinned server source;
2. accepted golden fixtures;
3. normalized server ruleset;
4. server implementation;
5. generic simulator services;
6. SWGAide convenience calculations;
7. external guides/community assumptions.

A disagreement is evidence to investigate, not permission to average behaviors.
