# Acceptance Matrix

| Capability | Foundation | Evidence required |
|---|---|---|
| generic server identity | implemented | compile/self-test |
| deterministic provider registry | implemented | exact-vs-family ambiguity tests |
| exact Infinity rules provider | implemented | real ruleset + server-154 runtime test |
| verified family provider | not implemented by design | separate source/fixture project |
| native selected-server current resources | terminal/unit integration PASS; live GUI PENDING | live selected-server resource selector smoke |
| native selected-server current+recent resources | terminal/unit integration PASS; live GUI PENDING | live current+recent resource selector smoke |
| native selected-server inventory | terminal/schema integration PASS; live GUI PENDING | live inventory selector + quantity smoke |
| separate simulator resource DB | prohibited | n/a |
| native Crafting Simulator tab | headless/runtime PASS; live GUI PENDING | LIVE_GUI_SMOKE.md |
| selected-schematic synchronization | headless integration PASS; live GUI PENDING | select schematic in Draft/Laboratory and observe simulator sync |
| immutable source contracts | implemented | compile + self-tests |
| ruleset structural validation | implemented | invalid/valid fixtures |
| raw resource weighted value | implemented in Infinity module | source-derived numeric fixtures |
| zero-stat denominator behavior | implemented | golden fixture |
| custom resource ingredient weighting | implemented | real Infinity fixture |
| material experimentation ceiling | implemented | golden fixture |
| forced assembly outcome | implemented | golden fixture |
| forced experimentation outcome | implemented | golden fixture |
| inverted min/max interpolation | implemented | golden fixture |
| component use accounting | implemented | golden fixture |
| identical serial behavior | implemented | golden fixture |
| mixed-slot first prototype | implemented | golden fixture |
| optional partial component behavior | implemented | golden fixture |
| generic component combine path | implemented | fixture per combine type |
| recursive crafted component conversion | implemented | nested craft fixture |
| generic compare/explain/planning | implemented | compile/self-tests |
| weapon final processor | EXACT for accepted 4-weapon corpus; terminal evidence PASS | WeaponFunctionalParitySelfTest 4/4: experimental target-template data (resource weights, min/max, precision, combine types) extracted from .lua weapon object templates and verified against engine output |
| weapon vertical parity | EXACT for accepted 4-weapon corpus; terminal evidence PASS | WeaponVerticalParitySelfTest 4/4 (slots/skill/template) + WeaponFunctionalParitySelfTest 4/4 (resource weights; min/max; precision; combine type; material ceiling; assembly state; experimentation; component effects; final weapon fields) |
| RNG probability model | not implemented | System::random semantics + parity |
| Genetic Laboratory | not implemented | source/fixture pass |
| Droid Laboratory | not implemented | source/fixture pass |
| armor/food/medicine/etc. processors | not implemented | category fixtures |
| source extractor | implemented | sandboxed inheritance resolver + WeaponTangibleTemplateExtractor parses weapon .lua experimental data |
| scenario persistence | implemented | versioned scenario model with full stat snapshot; atomic save; id validation; v1 -> v2 migration |

| source-free runtime ruleset artifact pipeline | PASS / artifact committed | 843,794-byte artifact + bundled-load smoke + pinned source/hash receipt |
| real-ID live runtime bindings | headless/runtime PASS; live GUI PENDING | selected Infinity galaxy + actual SWGAide schematic-id smoke |
| explicit per-slot X/Y/Z resource selection | terminal/unit PASS; live GUI PENDING | current/recent/inventory selector smoke + visible result delta |
