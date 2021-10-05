package tools;

public class BuildInfo {
    private static final String BUILD_NUMBER = "2766";
    private static final String BUILD_DATE = "10/05/2021 05:32:40 AM";

    public static final String buildInfo = "JForth, Build: " + BUILD_NUMBER + ", " + BUILD_DATE
            + " -- " + System.getProperty("java.version");

}
