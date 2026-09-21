package swg.infinity.extract;

/** One structured source-extraction issue. */
public final class ExtractionIssue {
    private final ExtractionSeverity severity;
    private final String code;
    private final String sourcePath;
    private final String message;

    public ExtractionIssue(
            ExtractionSeverity severity,
            String code,
            String sourcePath,
            String message) {
        if (severity == null) throw new NullPointerException("severity");
        this.code = requireText(code, "code");
        this.sourcePath = requireText(sourcePath, "sourcePath");
        this.message = requireText(message, "message");
        this.severity = severity;
    }

    public ExtractionSeverity getSeverity() { return severity; }
    public String getCode() { return code; }
    public String getSourcePath() { return sourcePath; }
    public String getMessage() { return message; }

    public boolean blocksAdmission() {
        return severity == ExtractionSeverity.ERROR
                || severity == ExtractionSeverity.BLOCKER;
    }

    private static String requireText(String value, String name) {
        if (value == null || value.trim().isEmpty()) {
            throw new IllegalArgumentException(name + " must not be blank");
        }
        return value;
    }
}
