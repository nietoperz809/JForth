package tools;

public class BuildInfo {
    private static final String BUILD_NUMBER = "2815";
    private static final String BUILD_DATE = "10/08/2021 08:48:18 AM";

    public static final String buildInfo = "JForth, Build: " + BUILD_NUMBER + ", " + BUILD_DATE
            + " -- " + System.getProperty("java.version");

}
