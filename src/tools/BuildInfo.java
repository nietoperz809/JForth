package tools;

public class BuildInfo {
    private static final String BUILD_NUMBER = "2717";
    private static final String BUILD_DATE = "09/28/2021 07:47:04 AM";

    public static final String buildInfo = "JForth, Build: " + BUILD_NUMBER + ", " + BUILD_DATE
            + " -- " + System.getProperty("java.version");

}
