package tools;

public class BuildInfo {
    private static final String BUILD_NUMBER = "3786";
    private static final String BUILD_DATE = "10/24/2023 06:12:30 PM";

    public static final String buildInfo = "JForth, Build: " + BUILD_NUMBER + ", " + BUILD_DATE
            + " -- " + System.getProperty("java.version");

}
