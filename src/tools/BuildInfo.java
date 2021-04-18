package tools;

public class BuildInfo {
    private static final String BUILD_NUMBER = "2550";
    private static final String BUILD_DATE = "04/18/2021 10:06:50 AM";

    public static final String buildInfo = "JForth, Build: " + BUILD_NUMBER + ", " + BUILD_DATE
            + " -- " + System.getProperty("java.version");

}
