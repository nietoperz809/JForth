package tools;

public class BuildInfo {
    private static final String BUILD_NUMBER = "3596";
    private static final String BUILD_DATE = "06/14/2023 10:58:13 PM";

    public static final String buildInfo = "JForth, Build: " + BUILD_NUMBER + ", " + BUILD_DATE
            + " -- " + System.getProperty("java.version");

}
