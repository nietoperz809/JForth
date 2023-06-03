package tools;

public class BuildInfo {
    private static final String BUILD_NUMBER = "3553";
    private static final String BUILD_DATE = "06/03/2023 08:42:49 AM";

    public static final String buildInfo = "JForth, Build: " + BUILD_NUMBER + ", " + BUILD_DATE
            + " -- " + System.getProperty("java.version");

}
