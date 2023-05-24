package tools;

public class BuildInfo {
    private static final String BUILD_NUMBER = "3535";
    private static final String BUILD_DATE = "05/24/2023 11:44:43 AM";

    public static final String buildInfo = "JForth, Build: " + BUILD_NUMBER + ", " + BUILD_DATE
            + " -- " + System.getProperty("java.version");

}
