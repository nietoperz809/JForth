package tools;

public class BuildInfo {
    private static final String BUILD_NUMBER = "3971";
    private static final String BUILD_DATE = "11/13/2023 05:46:22 PM";

    public static final String buildInfo = "JForth, Build: " + BUILD_NUMBER + ", " + BUILD_DATE
            + " -- " + System.getProperty("java.version");

}
