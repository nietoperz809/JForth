package tools;

public class BuildInfo {
    private static final String BUILD_NUMBER = "2973";
    private static final String BUILD_DATE = "10/16/2021 09:28:56 AM";

    public static final String buildInfo = "JForth, Build: " + BUILD_NUMBER + ", " + BUILD_DATE
            + " -- " + System.getProperty("java.version");

}
