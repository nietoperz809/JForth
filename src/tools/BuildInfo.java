package tools;

public class BuildInfo {
    private static final String BUILD_NUMBER = "4019";
    private static final String BUILD_DATE = "02/09/2025 12:15:15 AM";

    public static final String buildInfo = "JForth, Build: " + BUILD_NUMBER + ", " + BUILD_DATE
            + " -- " + System.getProperty("java.version");

}
