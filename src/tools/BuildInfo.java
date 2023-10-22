package tools;

public class BuildInfo {
    private static final String BUILD_NUMBER = "3741";
    private static final String BUILD_DATE = "10/22/2023 08:55:03 PM";

    public static final String buildInfo = "JForth, Build: " + BUILD_NUMBER + ", " + BUILD_DATE
            + " -- " + System.getProperty("java.version");

}
