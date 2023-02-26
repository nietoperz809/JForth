package tools;

public class BuildInfo {
    private static final String BUILD_NUMBER = "3457";
    private static final String BUILD_DATE = "02/26/2023 05:18:57 PM";

    public static final String buildInfo = "JForth, Build: " + BUILD_NUMBER + ", " + BUILD_DATE
            + " -- " + System.getProperty("java.version");

}
