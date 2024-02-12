public class PrintingColorProvider {
    public static final String BLACK = "\033[0;30m";
    public static final String RED = "\033[0;31m";
    public static final String GREEN = "\033[0;32m";
    public static final String YELLOW = "\033[0;33m";
    public static final String BLUE = "\033[0;34m";
    public static final String PURPLE = "\033[0;35m";
    public static final String CYAN = "\033[0;36m";   
    public static final String WHITE = "\033[0;37m";

    private static final String[] COLORS = {BLACK, RED, GREEN, YELLOW, BLUE, PURPLE, CYAN};
    
    private static int currentIndex = 0;

    public static String provideNextPrintingColor() {
        String currentColor = COLORS[currentIndex];
        currentIndex = (currentIndex + 1) % COLORS.length;
        return currentColor;
    }
}