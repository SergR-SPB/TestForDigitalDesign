package com.png.LineFinder;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.awt.image.DataBufferByte;
import java.io.File;
import java.io.IOException;
import java.util.LinkedList;
import java.util.List;

public class LineFinder {
    private static final int WHITE = 16777215;

    public static void main(String[] args) throws IOException {
        if (args == null || args.length < 1) {
            throw new IllegalArgumentException("Path to file not found");
        }

        BufferedImage image = ImageIO.read(new File(args[0]));
        int[][] pixels = convertTo2DWithoutUsingGetRGBAndAlphaChannel(image);

        System.out.println("Total lines length = " + totalLinesLength(pixels));
        List<Integer> list = findLines(pixels);
        int count = 0;
        System.out.println(String.format(" № :  Length"));
        for (int i = 0; i < list.size(); i++) {
            System.out.println(String.format("%3d:    %d", i + 1, list.get(i)));
            count += list.get(i);
        }
        System.out.println("Total lines length (when lines were found) = " + count);
    }

    private static int[][] convertTo2DWithoutUsingGetRGBAndAlphaChannel(BufferedImage image) {

        final byte[] pixels = ((DataBufferByte) image.getRaster().getDataBuffer()).getData();
        final int width = image.getWidth();
        final int height = image.getHeight();
        final boolean hasAlphaChannel = image.getAlphaRaster() != null;

        int[][] result = new int[height][width];
        final int pixelLength;
        if (hasAlphaChannel) {
            pixelLength = 4;
        } else {
            pixelLength = 3;
        }

        for (int pixel = 0, row = 0, col = 0; pixel < pixels.length; pixel += pixelLength) {
            int argb = 0;

            argb += ((int) pixels[pixel + 1] & 0xff); // blue
            argb += (((int) pixels[pixel + 2] & 0xff) << 8); // green
            argb += (((int) pixels[pixel + 3] & 0xff) << 16); // red
            result[row][col] = argb;
            col++;
            if (col == width) {
                col = 0;
                row++;
            }
        }

        return result;
    }

    private static int totalLinesLength(int[][] pixels) {
        int count = 0;
        for (int[] row : pixels) {
            for (int pixel : row) {
                if (pixel != WHITE) {
                    count++;
                }
            }
        }
        return count;
    }

    private static List<Integer> findLines(int[][] pixels) {
        List<Integer> list = new LinkedList<>();

        for (int y = 0; y < pixels.length; y++) {
            for (int x = 0; x < pixels[y].length; x++) {
                if (pixels[y][x] != WHITE) {
                    list.add(lineSize(pixels, y, x));
                }
            }
        }
        return list;
    }

    private static boolean inRange(int[][] pixels, int y, int x) {
        return 0 <= y && y < pixels.length
                && 0 <= x && x < pixels[y].length;
    }

    private static int lineSize(int[][] pixels, int y, int x) {
        int color = pixels[y][x];
        int count = 0;
        while (inRange(pixels, y, x) && pixels[y][x] == color) {
            count++;
            pixels[y][x] = WHITE;

            outerFor:
            for (int i = 0; i <= 1; i++) {
                for (int j = -1; j <= 1; j++) {
                    if (inRange(pixels, y + i, x + j) && pixels[y + i][x + j] == color) {
                        y += i;
                        x += j;
                        break outerFor;
                    }
                }
            }
        }
        return count;
    }
}
