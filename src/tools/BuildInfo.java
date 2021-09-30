package tools;

public class BuildInfo {
    private static final String BUILD_NUMBER = "2731";
    private static final String BUILD_DATE = "09/30/2021 03:12:48 PM";

    public static final String buildInfo = "JForth, Build: " + BUILD_NUMBER + ", " + BUILD_DATE
            + " -- " + System.getProperty("java.version");

}
