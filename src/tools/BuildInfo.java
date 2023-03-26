package tools;

public class BuildInfo {
    private static final String BUILD_NUMBER = "3495";
    private static final String BUILD_DATE = "03/26/2023 08:40:20 PM";

    public static final String buildInfo = "JForth, Build: " + BUILD_NUMBER + ", " + BUILD_DATE
            + " -- " + System.getProperty("java.version");

}
