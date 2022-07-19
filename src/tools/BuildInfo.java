package tools;

public class BuildInfo {
    private static final String BUILD_NUMBER = "3259";
    private static final String BUILD_DATE = "07/20/2022 01:31:04 AM";

    public static final String buildInfo = "JForth, Build: " + BUILD_NUMBER + ", " + BUILD_DATE
            + " -- " + System.getProperty("java.version");

}
