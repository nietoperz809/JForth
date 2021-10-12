package tools;

public class BuildInfo {
    private static final String BUILD_NUMBER = "2898";
    private static final String BUILD_DATE = "10/12/2021 02:31:09 AM";

    public static final String buildInfo = "JForth, Build: " + BUILD_NUMBER + ", " + BUILD_DATE
            + " -- " + System.getProperty("java.version");

}
