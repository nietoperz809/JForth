package tools;

public class BuildInfo {
    private static final String BUILD_NUMBER = "3401";
    private static final String BUILD_DATE = "07/29/2022 10:26:19 PM";

    public static final String buildInfo = "JForth, Build: " + BUILD_NUMBER + ", " + BUILD_DATE
            + " -- " + System.getProperty("java.version");

}
