package tools;

public class BuildInfo {
    private static final String BUILD_NUMBER = "3940";
    private static final String BUILD_DATE = "11/04/2023 05:54:50 AM";

    public static final String buildInfo = "JForth, Build: " + BUILD_NUMBER + ", " + BUILD_DATE
            + " -- " + System.getProperty("java.version");

}
