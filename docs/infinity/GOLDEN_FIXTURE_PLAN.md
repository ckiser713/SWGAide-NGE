# Golden Fixture Plan

The main-based foundation self-tests are arithmetic/contract smoke tests. They are not a substitute for real source-backed fixtures.

## Weapon acceptance corpus

Before declaring weapon support Exact, add evidence for:

1. one raw-resource melee weapon;
2. one ranged weapon with mixed OQ/CD-style weights;
3. UT-only Projectile Feed Mechanism;
4. an advanced crafted weapon component;
5. a normal optional enhancement;
6. an exact owned rare Gorax-style enhancement;
7. a multi-use exotic component;
8. identical component slot;
9. mixed component slot;
10. partially filled optional slot;
11. custom resource-like ingredient;
12. reversed attack-speed range;
13. raw resource missing a weighted stat;
14. inherited resource-class match;
15. current vs inventory resource candidate;
16. LINEAR combine;
17. PERCENTAGE combine;
18. BITSET combine;
19. OVERRIDE combine;
20. LIMITED combine;
21. complete component -> parent weapon recursion.

## Fixture evidence

Each fixture must record:

- pinned Infinity SHA;
- source paths;
- normalized schematic identity;
- all input resource/component values;
- intermediate weighted stats;
- property weighted scores;
- assembly/current/max percentages;
- effective min/max after components;
- final CraftingValues;
- final functional item fields.

A fixture is not accepted because a guide or calculator reports a similar number. The expected result must be traceable to pinned server behavior or an independently reproducible server-side test.
