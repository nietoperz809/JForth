package tools;

public class BuildInfo {
    private static final String BUILD_NUMBER = "2849";
    private static final String BUILD_DATE = "10/10/2021 02:25:57 PM";

    public static final String buildInfo = "JForth, Build: " + BUILD_NUMBER + ", " + BUILD_DATE
            + " -- " + System.getProperty("java.version");

}
