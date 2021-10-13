package tools;

public class BuildInfo {
    private static final String BUILD_NUMBER = "2929";
    private static final String BUILD_DATE = "10/13/2021 02:10:15 PM";

    public static final String buildInfo = "JForth, Build: " + BUILD_NUMBER + ", " + BUILD_DATE
            + " -- " + System.getProperty("java.version");

}
