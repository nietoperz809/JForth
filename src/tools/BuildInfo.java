package tools;

public class BuildInfo {
    private static final String BUILD_NUMBER = "3333";
    private static final String BUILD_DATE = "07/21/2022 07:50:09 AM";

    public static final String buildInfo = "JForth, Build: " + BUILD_NUMBER + ", " + BUILD_DATE
            + " -- " + System.getProperty("java.version");

}
