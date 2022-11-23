package tools;

public class BuildInfo {
    private static final String BUILD_NUMBER = "3424";
    private static final String BUILD_DATE = "11/23/2022 08:42:53 AM";

    public static final String buildInfo = "JForth, Build: " + BUILD_NUMBER + ", " + BUILD_DATE
            + " -- " + System.getProperty("java.version");

}
