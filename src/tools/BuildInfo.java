package tools;

public class BuildInfo {
    private static final String BUILD_NUMBER = "3359";
    private static final String BUILD_DATE = "07/22/2022 02:24:35 AM";

    public static final String buildInfo = "JForth, Build: " + BUILD_NUMBER + ", " + BUILD_DATE
            + " -- " + System.getProperty("java.version");

}
