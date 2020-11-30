package helper;

/**
 * It took me a whole 6 hours to finally get the color conversion right.
 * I have a fairly good understanding of the various color formats (YUV420P, SP, 422, etc etc),
 * and how to access individual Y, U, and V components.
 * I however struggled because of a very simple yet hair-pulling gotcha.
 * All primitives in Java are signed! If you come from a Python-like world where 0xFF prints 255,
 * you see yourself struggle just the same. I am however embarrassed at spending 6 hours on this.
 *
 * @author rish
 */
public class ColorConversions {

    /**
     * Converts yuv420p to rgba 8888
     * The tricky bit here is:
     * Java has signed primitives only. This means 0xFF is -1, and not 0x255, as you'd expect
     * in an unsigned python like world.
     * <p>
     * And-ing with 0xFF (0xFF is an int, not a byte ;)) converts the individual Y, U, V, bits
     * from an signed byte to a signed int, but for our purposes behaves as an unsigned byte.
     * This means, (byte)0xFF=-1, whereas 0xFF & (byte)0xFF = 255.
     **/
    public static void yuv420pToRGBA8888(int[] out, byte[] yuv, int width, int height) {
        if (out.length < width * height) {
            throw new IllegalArgumentException("Size of out must be " + width * height);
        }
        if (yuv.length < width * height * 3.0 / 2) {
            throw new IllegalArgumentException("Size of yuv must be " + width * height * 3.0 / 2);
        }
        int size = width * height;
        for (int j = 0; j < height; j++) {
            for (int i = 0; i < width; i++) {
                /*accessing YUV420P elements*/
                int indexY = j * width + i;
                int indexU = (size + (j / 2) * (width / 2) + i / 2);
                int indexV = (int) (size * 1.25 + (j / 2) * (width / 2) + i / 2);

                // todo; this conversion to int and then later back to int really isn't required. 
                // There's room for better work here.
                int Y = 0xFF & yuv[indexY];
                int U = 0xFF & yuv[indexU];
                int V = 0xFF & yuv[indexV];

                /*constants picked up from http://www.fourcc.org/fccyvrgb.php*/
                int R = (int) (Y + 1.402f * (V - 128));
                int G = (int) (Y - 0.344f * (U - 128) - 0.714f * (V - 128));
                int B = (int) (Y + 1.772f * (U - 128));

                /*clamping values*/
                R = R < 0 ? 0 : R;
                G = G < 0 ? 0 : G;
                B = B < 0 ? 0 : B;
                R = R > 255 ? 255 : R;
                G = G > 255 ? 255 : G;
                B = B > 255 ? 255 : B;

                out[width * j + i] = 0xff000000 + (R << 16) + (G << 8) + B;
            }
        }
    }
}