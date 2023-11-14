package tools;

public class BuildInfo {
    private static final String BUILD_NUMBER = "3983";
    private static final String BUILD_DATE = "11/14/2023 01:29:09 PM";

    public static final String buildInfo = "JForth, Build: " + BUILD_NUMBER + ", " + BUILD_DATE
            + " -- " + System.getProperty("java.version");

}
