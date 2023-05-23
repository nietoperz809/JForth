package tools;

public class BuildInfo {
    private static final String BUILD_NUMBER = "3513";
    private static final String BUILD_DATE = "05/23/2023 02:29:58 AM";

    public static final String buildInfo = "JForth, Build: " + BUILD_NUMBER + ", " + BUILD_DATE
            + " -- " + System.getProperty("java.version");

}
