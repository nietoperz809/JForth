package tools;

public class BuildInfo {
    private static final String BUILD_NUMBER = "3550";
    private static final String BUILD_DATE = "05/25/2023 04:56:14 AM";

    public static final String buildInfo = "JForth, Build: " + BUILD_NUMBER + ", " + BUILD_DATE
            + " -- " + System.getProperty("java.version");

}
