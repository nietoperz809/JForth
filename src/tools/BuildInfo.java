package tools;

public class BuildInfo {
    private static final String BUILD_NUMBER = "2636";
    private static final String BUILD_DATE = "07/26/2021 02:43:48 PM";

    public static final String buildInfo = "JForth, Build: " + BUILD_NUMBER + ", " + BUILD_DATE
            + " -- " + System.getProperty("java.version");

}
