package tools;

public class BuildInfo {
    private static final String BUILD_NUMBER = "2676";
    private static final String BUILD_DATE = "07/30/2021 11:03:08 AM";

    public static final String buildInfo = "JForth, Build: " + BUILD_NUMBER + ", " + BUILD_DATE
            + " -- " + System.getProperty("java.version");

}
