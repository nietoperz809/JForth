package tools;

public class BuildInfo {
    private static final String BUILD_NUMBER = "2648";
    private static final String BUILD_DATE = "07/27/2021 02:02:31 AM";

    public static final String buildInfo = "JForth, Build: " + BUILD_NUMBER + ", " + BUILD_DATE
            + " -- " + System.getProperty("java.version");

}
