package jforth.audio;

import jforth.forthwords.PredefinedWords;

import javax.sound.sampled.SourceDataLine;
import java.io.ByteArrayOutputStream;
import java.util.ArrayList;

public class MusicTones extends SynthToneBase {
    /**
     * Array of frequencies
     */
    private static final float[][] frequencies = new float[][]
            {
                    {16.35f, 17.32f, 18.35f, 19.45f, 20.60f, 21.83f, 23.12f, 24.50f, 25.96f, 27.50f, 29.14f, 30.87f},
                    {32.70f, 34.65f, 36.71f, 38.89f, 41.20f, 43.65f, 46.25f, 49.00f, 51.91f, 55.00f, 58.27f, 61.74f},
                    {65.41f, 69.30f, 73.42f, 77.78f, 82.41f, 87.31f, 92.50f, 98.00f, 103.8f, 110.0f, 116.5f, 123.5f},
                    {130.8f, 138.6f, 146.8f, 155.6f, 164.8f, 174.6f, 185.0f, 196.0f, 207.7f, 220.0f, 233.1f, 246.9f},
                    {261.6f, 277.2f, 293.7f, 311.1f, 329.6f, 349.2f, 370.0f, 392.0f, 415.3f, 440.0f, 466.2f, 493.9f},
                    {523.3f, 554.4f, 587.3f, 622.3f, 659.3f, 698.5f, 740.0f, 784.0f, 830.6f, 880.0f, 932.3f, 987.8f},
                    {1047.0f, 1109.0f, 1175.0f, 1245.0f, 1319.0f, 1397.0f, 1480.0f, 1568.0f, 1661.0f, 1760.0f, 1865.0f, 1976.0f},
                    {2093.0f, 2217.0f, 2349.0f, 2489.0f, 2637.0f, 2794.0f, 2960.0f, 3136.0f, 3322.0f, 3520.0f, 3729.0f, 3951.0f},
                    {4186.0f, 4435.0f, 4699.0f, 4978.0f, 5274.0f, 5588.0f, 5920.0f, 6272.0f, 6645.0f, 7040.0f, 7459.0f, 7902.0f},
            };

    /**
     * Musical notes
     */
    private static final String[] notes = {"C", "C#", "D", "Eb", "E", "F", "F#", "G", "G#", "A", "Bb", "B"};

    /**
     * Tone length
     */
    private static int multiplier;  // Tone length

    private static float getFrequency(String note, int octave) throws ArrayIndexOutOfBoundsException {
        int x = -1;
        for (int s = 0; s <= notes.length; s++) {
            if (notes[s].equals(note)) {
                x = s;
                break;
            }
        }
        return frequencies[octave][x];
    }

    private static void makeToneAndPlay(SourceDataLine line, String code) {
        float freq;
        if (code.charAt(0) == 'L') {
            multiplier = code.charAt(1) - '0';
            return;
        }
        if (code.length() == 2) {
            freq = getFrequency(code.substring(0, 1), code.charAt(1) - '0');
        } else // length == 3
        {
            freq = getFrequency(code.substring(0, 2), code.charAt(2) - '0');
        }
        int ms = 100 * multiplier;
        byte[] out = new byte[SAMPLE_RATE * ms / 1000];
        makeSingleWave(freq, out);
        play(line, out, ms);
        //System.out.println("tonelength = "+ms);
    }

    private static void makeToneAndStore(ByteArrayOutputStream baos, String code) {
        float freq;
        if (code.charAt(0) == 'L') {
            multiplier = code.charAt(1) - '0';
            return;
        }
        if (code.length() == 2) {
            freq = getFrequency(code.substring(0, 1), code.charAt(1) - '0');
        } else // length == 3
        {
            freq = getFrequency(code.substring(0, 2), code.charAt(2) - '0');
        }
        int ms = 100 * multiplier;
        byte[] out = new byte[SAMPLE_RATE * ms / 1000];
        makeSingleWave(freq, out);
        baos.write(out, 0, out.length);
    }

    /**
     * Find Notes in string, also length code 'L'
     *
     * @param in input string
     * @return List of found notes
     */
    private static ArrayList<String> parseTones(String in) {
        in = in.toUpperCase();
        ArrayList<String> toks = new ArrayList<>();
        StringBuilder sb = new StringBuilder();
        for (int s = 0; s < in.length(); s++) {
            boolean found = false;
            char c = in.charAt(s);
            if (sb.length() == 0)  // 1st char
            {
                if (c == 'L' || c == 'C' || c == 'D' || c == 'E' || c == 'F' || c == 'G' || c == 'A' || c == 'B') {
                    found = true;
                    sb.append(c);
                }
            } else if (sb.length() == 1) // 2nd char
            {
                char c2 = sb.charAt(0);
                if (c2 == 'L')   // first is 'L'
                {
                    if (c >= '1' && c <= '9') {
                        sb.append(c);
                        toks.add(sb.toString());
                        sb.setLength(0);
                        continue;
                    }
                } else if (c == '#' && (c2 == 'C' || c2 == 'G' || c2 == 'F')) {
                    found = true;
                    sb.append('#');
                } else if (c == 'B' && (c2 == 'E' || c2 == 'B')) {
                    found = true;
                    sb.append('b');
                }
            }
            if (sb.length() != 0) {
                int oct = c - '0';
                if (oct >= 0 && oct < 9) {
                    sb.append(c);
                    toks.add(sb.toString());
                    sb.setLength(0);
                }
            }
            if (!found)
                sb.setLength(0);
        }
        //System.out.println(toks);
        return toks;
    }

    /**
     * Make song from string
     *
     * @param input Input String  (eg. c4d4l3c4d4 means
     *              play c4,d4 length=1,
     *              then c4,d4 again with length=3)
     */
    public static void playSong(String input) {
        multiplier = 1;
        ArrayList<String> list = parseTones(input);

        try {
            SourceDataLine line = openLine();

            for (String value : list) {
                makeToneAndPlay(line, value);
            }
            closeLine(line);
        } catch (Exception ignored) {
            //System.out.println (ignored);
        }
    }

    public static byte[] putSongIntoMemory(String input) throws Exception {
        multiplier = 1;
        ArrayList<String> list = parseTones(input);

        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        for (String value : list) {
            makeToneAndStore(baos, value);
        }
        return  WaveTools.withWAVHeader(baos.toByteArray(), af);
    }

    public static void sendSongtoBrowser(String in, PredefinedWords predefinedWords) throws Exception {
        toBrowser (putSongIntoMemory(in), predefinedWords);
    }


} // End class

