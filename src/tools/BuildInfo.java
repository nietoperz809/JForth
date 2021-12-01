package tools;

public class BuildInfo {
    private static final String BUILD_NUMBER = "3038";
    private static final String BUILD_DATE = "11/30/2021 06:12:45 AM";

    public static final String buildInfo = "JForth, Build: " + BUILD_NUMBER + ", " + BUILD_DATE
            + " -- " + System.getProperty("java.version");

}
