package swg.infinity.contracts;

/**
 * Resource properties observed by the Infinity generic crafting source.
 *
 * <p>ER is valid SWG resource metadata but is intentionally absent because the
 * inspected Infinity generic crafting value lookup does not map it. BK is
 * source-recognized but remains coverage-gated until a real fixture proves its
 * generic semantics.</p>
 */
public enum ResourceStat {
    CR, CD, DR, HR, FL, MA, PE, OQ, SR, UT, BK
}
