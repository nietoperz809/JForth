package tools;

public class BuildInfo {
    private static final String BUILD_NUMBER = "3546";
    private static final String BUILD_DATE = "05/25/2023 03:28:56 AM";

    public static final String buildInfo = "JForth, Build: " + BUILD_NUMBER + ", " + BUILD_DATE
            + " -- " + System.getProperty("java.version");

}
