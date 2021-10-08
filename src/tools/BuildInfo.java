package tools;

public class BuildInfo {
    private static final String BUILD_NUMBER = "2842";
    private static final String BUILD_DATE = "10/08/2021 05:45:31 PM";

    public static final String buildInfo = "JForth, Build: " + BUILD_NUMBER + ", " + BUILD_DATE
            + " -- " + System.getProperty("java.version");

}
