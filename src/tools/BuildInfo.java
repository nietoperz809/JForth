package tools;

public class BuildInfo {
    private static final String BUILD_NUMBER = "2596";
    private static final String BUILD_DATE = "05/15/2021 05:30:38 AM";

    public static final String buildInfo = "JForth, Build: " + BUILD_NUMBER + ", " + BUILD_DATE
            + " -- " + System.getProperty("java.version");

}
