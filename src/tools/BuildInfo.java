package tools;

public class BuildInfo {
    private static final String BUILD_NUMBER = "2680";
    private static final String BUILD_DATE = "07/30/2021 11:38:37 AM";

    public static final String buildInfo = "JForth, Build: " + BUILD_NUMBER + ", " + BUILD_DATE
            + " -- " + System.getProperty("java.version");

}
