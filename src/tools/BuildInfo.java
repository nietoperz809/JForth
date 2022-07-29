package tools;

public class BuildInfo {
    private static final String BUILD_NUMBER = "3393";
    private static final String BUILD_DATE = "07/29/2022 06:34:04 PM";

    public static final String buildInfo = "JForth, Build: " + BUILD_NUMBER + ", " + BUILD_DATE
            + " -- " + System.getProperty("java.version");

}
