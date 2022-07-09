package tools;

public class BuildInfo {
    private static final String BUILD_NUMBER = "3102";
    private static final String BUILD_DATE = "07/09/2022 12:31:39 PM";

    public static final String buildInfo = "JForth, Build: " + BUILD_NUMBER + ", " + BUILD_DATE
            + " -- " + System.getProperty("java.version");

}
