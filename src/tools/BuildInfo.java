package tools;

public class BuildInfo {
    private static final String BUILD_NUMBER = "3614";
    private static final String BUILD_DATE = "06/15/2023 03:46:07 AM";

    public static final String buildInfo = "JForth, Build: " + BUILD_NUMBER + ", " + BUILD_DATE
            + " -- " + System.getProperty("java.version");

}
