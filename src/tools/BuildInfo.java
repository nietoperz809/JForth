package tools;

public class BuildInfo {
    private static final String BUILD_NUMBER = "3061";
    private static final String BUILD_DATE = "03/17/2022 12:52:45 AM";

    public static final String buildInfo = "JForth, Build: " + BUILD_NUMBER + ", " + BUILD_DATE
            + " -- " + System.getProperty("java.version");

}
