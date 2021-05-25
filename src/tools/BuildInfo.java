package tools;

public class BuildInfo {
    private static final String BUILD_NUMBER = "2602";
    private static final String BUILD_DATE = "05/25/2021 01:50:10 PM";

    public static final String buildInfo = "JForth, Build: " + BUILD_NUMBER + ", " + BUILD_DATE
            + " -- " + System.getProperty("java.version");

}
