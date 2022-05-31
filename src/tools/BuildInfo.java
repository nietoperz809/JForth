package tools;

public class BuildInfo {
    private static final String BUILD_NUMBER = "3075";
    private static final String BUILD_DATE = "05/31/2022 12:15:22 PM";

    public static final String buildInfo = "JForth, Build: " + BUILD_NUMBER + ", " + BUILD_DATE
            + " -- " + System.getProperty("java.version");

}
