package tools;

public class BuildInfo {
    private static final String BUILD_NUMBER = "3123";
    private static final String BUILD_DATE = "07/18/2022 04:42:48 AM";

    public static final String buildInfo = "JForth, Build: " + BUILD_NUMBER + ", " + BUILD_DATE
            + " -- " + System.getProperty("java.version");

}
