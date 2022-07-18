package tools;

public class BuildInfo {
    private static final String BUILD_NUMBER = "3133";
    private static final String BUILD_DATE = "07/18/2022 05:17:20 PM";

    public static final String buildInfo = "JForth, Build: " + BUILD_NUMBER + ", " + BUILD_DATE
            + " -- " + System.getProperty("java.version");

}
