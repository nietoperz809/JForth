package tools;

public class BuildInfo {
    private static final String BUILD_NUMBER = "3574";
    private static final String BUILD_DATE = "06/13/2023 02:08:59 PM";

    public static final String buildInfo = "JForth, Build: " + BUILD_NUMBER + ", " + BUILD_DATE
            + " -- " + System.getProperty("java.version");

}
