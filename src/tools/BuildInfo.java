package tools;

public class BuildInfo {
    private static final String BUILD_NUMBER = "3206";
    private static final String BUILD_DATE = "07/19/2022 02:21:23 PM";

    public static final String buildInfo = "JForth, Build: " + BUILD_NUMBER + ", " + BUILD_DATE
            + " -- " + System.getProperty("java.version");

}
