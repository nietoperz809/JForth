package tools;

public class BuildInfo {
    private static final String BUILD_NUMBER = "3802";
    private static final String BUILD_DATE = "10/25/2023 01:54:51 AM";

    public static final String buildInfo = "JForth, Build: " + BUILD_NUMBER + ", " + BUILD_DATE
            + " -- " + System.getProperty("java.version");

}
