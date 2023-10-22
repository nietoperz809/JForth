package tools;

public class BuildInfo {
    private static final String BUILD_NUMBER = "3725";
    private static final String BUILD_DATE = "10/22/2023 03:35:50 AM";

    public static final String buildInfo = "JForth, Build: " + BUILD_NUMBER + ", " + BUILD_DATE
            + " -- " + System.getProperty("java.version");

}
