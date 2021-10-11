package tools;

public class BuildInfo {
    private static final String BUILD_NUMBER = "2869";
    private static final String BUILD_DATE = "10/11/2021 07:41:23 PM";

    public static final String buildInfo = "JForth, Build: " + BUILD_NUMBER + ", " + BUILD_DATE
            + " -- " + System.getProperty("java.version");

}
