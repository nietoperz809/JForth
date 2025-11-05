package tools;

public class BuildInfo {
    private static final String BUILD_NUMBER = "4121";
    private static final String BUILD_DATE = "07/12/2025 06:33:24 PM";

    public static final String buildInfo = "JForth, Build: " + BUILD_NUMBER + ", " + BUILD_DATE
            + " -- " + System.getProperty("java.version");

}
