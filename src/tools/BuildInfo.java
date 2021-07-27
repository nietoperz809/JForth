package tools;

public class BuildInfo {
    private static final String BUILD_NUMBER = "2651";
    private static final String BUILD_DATE = "07/27/2021 03:44:31 PM";

    public static final String buildInfo = "JForth, Build: " + BUILD_NUMBER + ", " + BUILD_DATE
            + " -- " + System.getProperty("java.version");

}
