package tools;

public class BuildInfo {
    private static final String BUILD_NUMBER = "3416";
    private static final String BUILD_DATE = "11/23/2022 05:53:03 AM";

    public static final String buildInfo = "JForth, Build: " + BUILD_NUMBER + ", " + BUILD_DATE
            + " -- " + System.getProperty("java.version");

}
