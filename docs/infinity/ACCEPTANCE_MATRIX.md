# Acceptance Matrix

| Capability | Foundation | Evidence required |
|---|---|---|
| generic server identity | implemented | compile/self-test |
| deterministic provider registry | implemented | exact-vs-family ambiguity tests |
| exact Infinity rules provider | implemented | real ruleset + server-154 runtime test |
| verified family provider | not implemented by design | separate source/fixture project |
| native selected-server current resources | real stat/class snapshot adapter implemented; REVALIDATION_REQUIRED | current SWGAide runtime fixture on current head |
| native selected-server current+recent resources | native snapshot scope implemented; REVALIDATION_REQUIRED | current SWGAide runtime fixture on current head |
| native selected-server inventory | quantity-preserving snapshot adapter implemented; REVALIDATION_REQUIRED | real inventory fixture on current head |
| separate simulator resource DB | prohibited | n/a |
| native Crafting Simulator tab | wired with per-slot resource selectors + exact coverage execution; REVALIDATION_REQUIRED | bundled ruleset + live binding + GUI/runtime smoke on current head |
| selected-schematic synchronization | implemented; REVALIDATION_REQUIRED | current-head UI integration test |
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
| weapon final processor | prior EXACT evidence at 1a43364; current head REVALIDATION_REQUIRED | WeaponFunctionalParitySelfTest 4/4: experimental target-template data (resource weights, min/max, precision, combine types) extracted from .lua weapon object templates and verified against engine output |
| weapon vertical parity | prior EXACT evidence at 1a43364; current head REVALIDATION_REQUIRED after combine/weight extraction correction | WeaponVerticalParitySelfTest 4/4 (slots/skill/template) + WeaponFunctionalParitySelfTest 4/4 (resource weights; min/max; precision; combine type; material ceiling; assembly state; experimentation; component effects; final weapon fields) |
| RNG probability model | not implemented | System::random semantics + parity |
| Genetic Laboratory | not implemented | source/fixture pass |
| Droid Laboratory | not implemented | source/fixture pass |
| armor/food/medicine/etc. processors | not implemented | category fixtures |
| source extractor | implemented | sandboxed inheritance resolver + WeaponTangibleTemplateExtractor parses weapon .lua experimental data |
| scenario persistence | implemented | versioned scenario model with full stat snapshot; atomic save; id validation; v1 -> v2 migration |

| source-free runtime ruleset artifact pipeline | implemented; artifact not yet committed | generate from pinned source, round-trip codec, package under classpath path, bundled-load smoke |
| real-ID live runtime bindings | implemented; REVALIDATION_REQUIRED | selected Infinity galaxy + live SWGAide schematic manager smoke |
| explicit per-slot X/Y/Z resource selection | implemented; REVALIDATION_REQUIRED | current/recent/inventory selector smoke + different-resource result delta |
