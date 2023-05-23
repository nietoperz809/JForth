package tools;

public class BuildInfo {
    private static final String BUILD_NUMBER = "3525";
    private static final String BUILD_DATE = "05/23/2023 08:38:28 PM";

    public static final String buildInfo = "JForth, Build: " + BUILD_NUMBER + ", " + BUILD_DATE
            + " -- " + System.getProperty("java.version");

}
