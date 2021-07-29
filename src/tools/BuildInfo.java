package tools;

public class BuildInfo {
    private static final String BUILD_NUMBER = "2666";
    private static final String BUILD_DATE = "07/29/2021 06:20:11 PM";

    public static final String buildInfo = "JForth, Build: " + BUILD_NUMBER + ", " + BUILD_DATE
            + " -- " + System.getProperty("java.version");

}
