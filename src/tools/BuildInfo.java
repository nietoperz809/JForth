package tools;

public class BuildInfo {
    private static final String BUILD_NUMBER = "2671";
    private static final String BUILD_DATE = "07/30/2021 09:50:08 AM";

    public static final String buildInfo = "JForth, Build: " + BUILD_NUMBER + ", " + BUILD_DATE
            + " -- " + System.getProperty("java.version");

}
