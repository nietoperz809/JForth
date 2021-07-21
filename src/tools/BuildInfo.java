package tools;

public class BuildInfo {
    private static final String BUILD_NUMBER = "2615";
    private static final String BUILD_DATE = "07/21/2021 06:59:00 PM";

    public static final String buildInfo = "JForth, Build: " + BUILD_NUMBER + ", " + BUILD_DATE
            + " -- " + System.getProperty("java.version");

}
