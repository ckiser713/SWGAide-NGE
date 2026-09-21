package swg.infinity.integration;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Multi-signal structural fingerprint for binding a SWGAide navigation
 * schematic to an Infinity source schematic.
 *
 * <p>The fingerprint combines several independent structural signals so
 * that exact (family, subcategory, name) alone is not the binding key —
 * if any signal disagrees, the fingerprint differs.</p>
 *
 * <p>Primary signals, in canonical order:</p>
 * <ol>
 *   <li>resource slot count</li>
 *   <li>resource slot kinds, sorted lexicographically</li>
 *   <li>experiment group titles, sorted lexicographically</li>
 *   <li>normalized target template (when supplied)</li>
 * </ol>
 *
 * <p>The fingerprint is a SHA-256 hex digest of the canonicalized
 * signals joined by {@code \n}. The signals themselves are preserved
 * verbatim so callers can inspect <em>why</em> two fingerprints did
 * or did not match.</p>
 *
 * <p>Fingerprints are deterministic: only stable, sorted lists, no
 * time/locale input. Two structurally equivalent schematics always
 * produce the same fingerprint.</p>
 */
public final class BindingFingerprint {

    private final String hash;
    private final int resourceSlotCount;
    private final List<String> resourceSlotKinds;
    private final List<String> experimentGroupTitles;
    private final String targetTemplate;

    private BindingFingerprint(
            String hash,
            int resourceSlotCount,
            List<String> resourceSlotKinds,
            List<String> experimentGroupTitles,
            String targetTemplate) {
        this.hash = hash;
        this.resourceSlotCount = resourceSlotCount;
        this.resourceSlotKinds = Collections.unmodifiableList(
                new ArrayList<String>(resourceSlotKinds));
        this.experimentGroupTitles = Collections.unmodifiableList(
                new ArrayList<String>(experimentGroupTitles));
        this.targetTemplate = targetTemplate == null ? "" : targetTemplate;
    }

    /**
     * Computes a fingerprint from the supplied signals. Returns a stable
     * structural hash regardless of input ordering of {@code kinds} or
     * {@code groupTitles}.
     */
    public static BindingFingerprint compute(
            int resourceSlotCount,
            List<String> resourceSlotKinds,
            List<String> experimentGroupTitles,
            String targetTemplate) {
        List<String> sortedKinds = new ArrayList<String>(
                resourceSlotKinds == null ? Collections.<String>emptyList()
                        : resourceSlotKinds);
        Collections.sort(sortedKinds);
        List<String> sortedGroups = new ArrayList<String>(
                experimentGroupTitles == null
                        ? Collections.<String>emptyList()
                        : experimentGroupTitles);
        Collections.sort(sortedGroups);
        String tt = targetTemplate == null ? "" : targetTemplate.trim();

        StringBuilder canon = new StringBuilder();
        canon.append("slots=").append(resourceSlotCount).append('\n');
        canon.append("kinds=");
        for (int i = 0; i < sortedKinds.size(); ++i) {
            if (i > 0) canon.append(',');
            canon.append(sortedKinds.get(i));
        }
        canon.append('\n');
        canon.append("groups=");
        for (int i = 0; i < sortedGroups.size(); ++i) {
            if (i > 0) canon.append(',');
            canon.append(sortedGroups.get(i));
        }
        canon.append('\n');
        canon.append("target=").append(tt).append('\n');
        String hash = sha256Hex(canon.toString());

        return new BindingFingerprint(
                hash, resourceSlotCount, sortedKinds, sortedGroups, tt);
    }

    public String getHash() { return hash; }
    public int getResourceSlotCount() { return resourceSlotCount; }
    public List<String> getResourceSlotKinds() { return resourceSlotKinds; }
    public List<String> getExperimentGroupTitles() { return experimentGroupTitles; }
    public String getTargetTemplate() { return targetTemplate; }

    /**
     * True iff both fingerprints match on every primary signal. Empty
     * target-template signals on one side are ignored so callers can
     * fingerprint partial SWGAide-side inputs without losing the rest
     * of the alignment.
     */
    public boolean alignsWith(BindingFingerprint other) {
        if (other == null) return false;
        if (resourceSlotCount != other.resourceSlotCount) return false;
        if (!resourceSlotKinds.equals(other.resourceSlotKinds)) return false;
        if (!experimentGroupTitles.equals(other.experimentGroupTitles)) {
            return false;
        }
        if (!targetTemplate.isEmpty() && !other.targetTemplate.isEmpty()
                && !targetTemplate.equals(other.targetTemplate)) {
            return false;
        }
        return true;
    }

    private static String sha256Hex(String text) {
        try {
            MessageDigest md = MessageDigest.getInstance("SHA-256");
            byte[] digest = md.digest(text.getBytes(StandardCharsets.UTF_8));
            StringBuilder sb = new StringBuilder(digest.length * 2);
            for (byte b : digest) {
                sb.append(String.format("%02x", b & 0xff));
            }
            return sb.toString();
        } catch (NoSuchAlgorithmException e) {
            throw new IllegalStateException("SHA-256 unavailable", e);
        }
    }
}
