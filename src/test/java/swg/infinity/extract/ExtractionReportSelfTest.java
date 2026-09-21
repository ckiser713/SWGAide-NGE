package swg.infinity.extract;

import java.util.Collections;

/** Dependency-free fail-closed extraction receipt tests. */
public final class ExtractionReportSelfTest {
    private ExtractionReportSelfTest() {
    }

    public static void main(String[] args) {
        ExtractionReport ready = new ExtractionReport(
                "swginfinity/public",
                "6b6ac3726aaa3c293fca83911850d3a02e2adb4f",
                100, 100, Collections.<ExtractionIssue>emptyList());
        if (!ready.isAdmissionReady()) {
            throw new AssertionError("complete clean extraction not admission-ready");
        }

        ExtractionReport incomplete = new ExtractionReport(
                "swginfinity/public",
                "6b6ac3726aaa3c293fca83911850d3a02e2adb4f",
                100, 99, Collections.<ExtractionIssue>emptyList());
        if (incomplete.isAdmissionReady()) {
            throw new AssertionError("incomplete extraction admitted");
        }

        ExtractionReport blocked = new ExtractionReport(
                "swginfinity/public",
                "6b6ac3726aaa3c293fca83911850d3a02e2adb4f",
                100, 100,
                Collections.singletonList(new ExtractionIssue(
                        ExtractionSeverity.ERROR,
                        "TARGET_TEMPLATE_MISSING",
                        "fixture.lua",
                        "fixture")));
        if (blocked.isAdmissionReady()) {
            throw new AssertionError("error extraction admitted");
        }
        System.out.println("ExtractionReportSelfTest PASS");
    }
}
