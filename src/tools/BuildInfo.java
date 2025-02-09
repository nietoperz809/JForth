package tools;

public class BuildInfo {
    private static final String BUILD_NUMBER = "4028";
    private static final String BUILD_DATE = "02/09/2025 06:05:14 AM";

    public static final String buildInfo = "JForth, Build: " + BUILD_NUMBER + ", " + BUILD_DATE
            + " -- " + System.getProperty("java.version");

}
