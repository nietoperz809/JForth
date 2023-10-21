package tools;

public class BuildInfo {
    private static final String BUILD_NUMBER = "3717";
    private static final String BUILD_DATE = "10/21/2023 05:42:18 PM";

    public static final String buildInfo = "JForth, Build: " + BUILD_NUMBER + ", " + BUILD_DATE
            + " -- " + System.getProperty("java.version");

}
