import java.awt.image.BufferedImage;

public class T05HistogramChecks {
    public static void main(String[] args) {
        for (int width : new int[] {1024, 512, 320, 255, 2, 1}) {
            for (int green : new int[] {0, 255}) {
                BufferedImage source = new BufferedImage(width, 24, BufferedImage.TYPE_INT_RGB);
                int color = (80 << 16) | (green << 8) | 120;
                for (int y = 0; y < 24; y++) {
                    for (int x = 0; x < width; x++) source.setRGB(x, y, color);
                }
                BufferedImage result = T05.process(source);
                int chartWidth = width / 2;
                if (chartWidth > 0) {
                    int barX = green == 0 ? 0 : chartWidth - 1;
                    for (int y = 0; y < 12; y++) {
                        if ((result.getRGB(barX, y) & 0xffffff) != 0x00ff00) {
                            throw new AssertionError("Histogram does not reach quarter boundary: width=" + width + ", green=" + green + ", x=" + barX);
                        }
                    }
                }
                for (int y = 0; y < 24; y++) {
                    for (int x = 0; x < width; x++) {
                        if ((x >= chartWidth || y >= 12) && result.getRGB(x, y) != source.getRGB(x, y)) {
                            throw new AssertionError("Histogram escapes top-left quarter: width=" + width + ", x=" + x + ", y=" + y);
                        }
                    }
                }
            }
        }
        System.out.println("PASS: histogram fits top-left quarter at 6 widths, including both extreme green levels.");
    }
}
