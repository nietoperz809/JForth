package tools;

public class BuildInfo {
    private static final String BUILD_NUMBER = "2538";
    private static final String BUILD_DATE = "04/18/2021 05:53:24 AM";

    public static final String buildInfo = "JForth, Build: " + BUILD_NUMBER + ", " + BUILD_DATE
            + " -- " + System.getProperty("java.version");

}
