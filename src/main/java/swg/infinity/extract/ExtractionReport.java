package swg.infinity.extract;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/** Immutable extraction receipt used to gate normalized ruleset admission. */
public final class ExtractionReport {
    private final String repository;
    private final String commit;
    private final int registeredSchematics;
    private final int normalizedSchematics;
    private final List<ExtractionIssue> issues;

    public ExtractionReport(
            String repository,
            String commit,
            int registeredSchematics,
            int normalizedSchematics,
            List<ExtractionIssue> issues) {
        if (repository == null || repository.trim().isEmpty()) {
            throw new IllegalArgumentException("repository must not be blank");
        }
        if (commit == null || !commit.matches("[0-9a-fA-F]{40}")) {
            throw new IllegalArgumentException("commit must be a 40-character SHA");
        }
        if (registeredSchematics < 0 || normalizedSchematics < 0
                || normalizedSchematics > registeredSchematics) {
            throw new IllegalArgumentException("invalid schematic counts");
        }
        if (issues == null) throw new NullPointerException("issues");
        this.repository = repository;
        this.commit = commit.toLowerCase();
        this.registeredSchematics = registeredSchematics;
        this.normalizedSchematics = normalizedSchematics;
        this.issues = Collections.unmodifiableList(
                new ArrayList<ExtractionIssue>(issues));
    }

    public String getRepository() { return repository; }
    public String getCommit() { return commit; }
    public int getRegisteredSchematics() { return registeredSchematics; }
    public int getNormalizedSchematics() { return normalizedSchematics; }
    public List<ExtractionIssue> getIssues() { return issues; }

    public boolean isAdmissionReady() {
        if (normalizedSchematics != registeredSchematics) return false;
        for (ExtractionIssue issue : issues) {
            if (issue.blocksAdmission()) return false;
        }
        return true;
    }
}
