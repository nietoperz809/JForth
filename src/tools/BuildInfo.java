package tools;

public class BuildInfo {
    private static final String BUILD_NUMBER = "4033";
    private static final String BUILD_DATE = "05/01/2025 11:44:46 AM";

    public static final String buildInfo = "JForth, Build: " + BUILD_NUMBER + ", " + BUILD_DATE
            + " -- " + System.getProperty("java.version");

}
