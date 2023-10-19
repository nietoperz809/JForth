package tools;

public class BuildInfo {
    private static final String BUILD_NUMBER = "3652";
    private static final String BUILD_DATE = "10/19/2023 05:27:03 AM";

    public static final String buildInfo = "JForth, Build: " + BUILD_NUMBER + ", " + BUILD_DATE
            + " -- " + System.getProperty("java.version");

}
