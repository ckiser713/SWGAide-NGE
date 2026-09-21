package swg.crafting.simulator.contracts;

import java.util.Objects;

/** Immutable source provenance attached to rules and calculations. */
public final class Provenance {
    private final String repository;
    private final String commit;
    private final String sourcePath;

    public Provenance(String repository, String commit, String sourcePath) {
        this.repository = requireText(repository, "repository");
        this.commit = requireText(commit, "commit");
        this.sourcePath = requireText(sourcePath, "sourcePath");
    }

    public String getRepository() { return repository; }
    public String getCommit() { return commit; }
    public String getSourcePath() { return sourcePath; }

    private static String requireText(String value, String name) {
        if (value == null || value.trim().isEmpty()) {
            throw new IllegalArgumentException(name + " must not be blank");
        }
        return value;
    }

    @Override
    public boolean equals(Object other) {
        if (this == other) return true;
        if (!(other instanceof Provenance)) return false;
        Provenance that = (Provenance) other;
        return repository.equals(that.repository)
                && commit.equals(that.commit)
                && sourcePath.equals(that.sourcePath);
    }

    @Override
    public int hashCode() {
        return Objects.hash(repository, commit, sourcePath);
    }

    @Override
    public String toString() {
        return repository + "@" + commit + ":" + sourcePath;
    }
}
