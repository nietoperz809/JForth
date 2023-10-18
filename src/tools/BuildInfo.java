package tools;

public class BuildInfo {
    private static final String BUILD_NUMBER = "3629";
    private static final String BUILD_DATE = "10/18/2023 04:44:11 PM";

    public static final String buildInfo = "JForth, Build: " + BUILD_NUMBER + ", " + BUILD_DATE
            + " -- " + System.getProperty("java.version");

}
