package tools;

public class BuildInfo {
    private static final String BUILD_NUMBER = "3678";
    private static final String BUILD_DATE = "10/21/2023 04:24:38 AM";

    public static final String buildInfo = "JForth, Build: " + BUILD_NUMBER + ", " + BUILD_DATE
            + " -- " + System.getProperty("java.version");

}
