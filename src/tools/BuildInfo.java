package tools;

public class BuildInfo {
    private static final String BUILD_NUMBER = "3045";
    private static final String BUILD_DATE = "12/16/2021 07:46:41 PM";

    public static final String buildInfo = "JForth, Build: " + BUILD_NUMBER + ", " + BUILD_DATE
            + " -- " + System.getProperty("java.version");

}
