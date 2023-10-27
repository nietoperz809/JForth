package tools;

public class BuildInfo {
    private static final String BUILD_NUMBER = "3894";
    private static final String BUILD_DATE = "10/27/2023 05:26:55 AM";

    public static final String buildInfo = "JForth, Build: " + BUILD_NUMBER + ", " + BUILD_DATE
            + " -- " + System.getProperty("java.version");

}
