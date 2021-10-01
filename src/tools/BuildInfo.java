package tools;

public class BuildInfo {
    private static final String BUILD_NUMBER = "2734";
    private static final String BUILD_DATE = "10/01/2021 09:37:07 AM";

    public static final String buildInfo = "JForth, Build: " + BUILD_NUMBER + ", " + BUILD_DATE
            + " -- " + System.getProperty("java.version");

}
