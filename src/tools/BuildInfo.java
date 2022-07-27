package tools;

public class BuildInfo {
    private static final String BUILD_NUMBER = "3388";
    private static final String BUILD_DATE = "07/27/2022 04:20:30 AM";

    public static final String buildInfo = "JForth, Build: " + BUILD_NUMBER + ", " + BUILD_DATE
            + " -- " + System.getProperty("java.version");

}
