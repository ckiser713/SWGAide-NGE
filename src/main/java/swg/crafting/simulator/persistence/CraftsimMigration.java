package swg.crafting.simulator.persistence;

import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Forward-only migration framework for {@link CraftsimScenario}. Each
 * migration bumps the version and returns a scenario at the new version.
 * No version is ever downgraded.
 *
 * <p>New migrations are registered with {@link #register(int, Migration)};
 * unknown older versions raise {@link IllegalArgumentException} from
 * {@link #migrate(CraftsimScenario, int)}.</p>
 */
public final class CraftsimMigration {

    /** A migration that takes a scenario and returns it at a higher version. */
    public interface Migration {
        CraftsimScenario apply(CraftsimScenario scenario);
    }

    private static final Map<Integer, Migration> REGISTRY =
            new LinkedHashMap<Integer, Migration>();

    static {
        // Version 1 is the initial format; no migrations are needed yet.
    }

    private CraftsimMigration() {
        throw new AssertionError("Do not instantiate");
    }

    public static synchronized void register(int targetVersion, Migration m) {
        if (m == null) throw new NullPointerException("m");
        if (targetVersion < CraftsimScenario.CURRENT_VERSION) {
            throw new IllegalArgumentException(
                    "Migration target version must be >= current version");
        }
        REGISTRY.put(Integer.valueOf(targetVersion), m);
    }

    public static CraftsimScenario migrate(CraftsimScenario s, int target) {
        if (s == null) throw new NullPointerException("s");
        if (target < 1) {
            throw new IllegalArgumentException("target must be >= 1");
        }
        if (target < s.getVersion()) {
            throw new IllegalArgumentException(
                    "Cannot downgrade from version " + s.getVersion()
                    + " to " + target);
        }
        CraftsimScenario current = s;
        for (int v = current.getVersion(); v < target; ++v) {
            Migration m = REGISTRY.get(Integer.valueOf(v + 1));
            if (m == null) {
                throw new IllegalStateException(
                        "No migration registered to version " + (v + 1));
            }
            current = m.apply(current);
        }
        return current;
    }
}
