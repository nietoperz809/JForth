package tools;

public class BuildInfo {
    private static final String BUILD_NUMBER = "3318";
    private static final String BUILD_DATE = "07/20/2022 09:55:53 PM";

    public static final String buildInfo = "JForth, Build: " + BUILD_NUMBER + ", " + BUILD_DATE
            + " -- " + System.getProperty("java.version");

}
