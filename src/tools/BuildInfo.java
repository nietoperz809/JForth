package tools;

public class BuildInfo {
    private static final String BUILD_NUMBER = "3449";
    private static final String BUILD_DATE = "11/24/2022 07:09:10 AM";

    public static final String buildInfo = "JForth, Build: " + BUILD_NUMBER + ", " + BUILD_DATE
            + " -- " + System.getProperty("java.version");

}
