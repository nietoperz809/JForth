package tools;

public class BuildInfo {
    private static final String BUILD_NUMBER = "2990";
    private static final String BUILD_DATE = "10/16/2021 09:12:00 PM";

    public static final String buildInfo = "JForth, Build: " + BUILD_NUMBER + ", " + BUILD_DATE
            + " -- " + System.getProperty("java.version");

}
