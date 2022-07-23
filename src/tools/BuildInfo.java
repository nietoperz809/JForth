package tools;

public class BuildInfo {
    private static final String BUILD_NUMBER = "3365";
    private static final String BUILD_DATE = "07/23/2022 11:52:27 AM";

    public static final String buildInfo = "JForth, Build: " + BUILD_NUMBER + ", " + BUILD_DATE
            + " -- " + System.getProperty("java.version");

}
