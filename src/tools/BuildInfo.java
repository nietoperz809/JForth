package tools;

public class BuildInfo {
    private static final String BUILD_NUMBER = "2920";
    private static final String BUILD_DATE = "10/12/2021 01:07:53 PM";

    public static final String buildInfo = "JForth, Build: " + BUILD_NUMBER + ", " + BUILD_DATE
            + " -- " + System.getProperty("java.version");

}
