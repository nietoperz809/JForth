package tools;

public class BuildInfo {
    private static final String BUILD_NUMBER = "2807";
    private static final String BUILD_DATE = "10/07/2021 08:28:29 PM";

    public static final String buildInfo = "JForth, Build: " + BUILD_NUMBER + ", " + BUILD_DATE
            + " -- " + System.getProperty("java.version");

}
