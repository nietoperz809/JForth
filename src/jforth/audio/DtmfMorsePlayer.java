package jforth.audio;

import jforth.forthwords.PredefinedWords;

import javax.sound.sampled.SourceDataLine;
import java.io.ByteArrayOutputStream;
import java.util.Base64;
import java.util.HashMap;

public class DtmfMorsePlayer extends SynthToneBase {
    private static final byte[] sin = new byte[SLEN];

    private static final HashMap<Character, int[]> dtmfMap = new HashMap<>();

    static {
        makeSingleWave(1000d, sin);

        dtmfMap.put('1', new int[]{1209, 697});
        dtmfMap.put('2', new int[]{1336, 697});
        dtmfMap.put('3', new int[]{1477, 697});
        dtmfMap.put('A', new int[]{1633, 697});

        dtmfMap.put('4', new int[]{1209, 770});
        dtmfMap.put('5', new int[]{1336, 770});
        dtmfMap.put('6', new int[]{1477, 770});
        dtmfMap.put('B', new int[]{1633, 770});

        dtmfMap.put('7', new int[]{1209, 852});
        dtmfMap.put('8', new int[]{1336, 852});
        dtmfMap.put('9', new int[]{1477, 852});
        dtmfMap.put('C', new int[]{1633, 852});

        dtmfMap.put('*', new int[]{1209, 941});
        dtmfMap.put('0', new int[]{1336, 941});
        dtmfMap.put('#', new int[]{1477, 941});
        dtmfMap.put('D', new int[]{1633, 941});
    }

    private static byte[] makeDtmfWave(char key) {
        byte[] buff = new byte[SLEN];
        int[] val = dtmfMap.get(key);
        if (val != null) {
            for (int i = 0; i < SLEN; i++) {
                double mu = 2d * Math.PI * i;
                double angle1 = mu / ((double) SAMPLE_RATE / val[0]);
                double angle2 = mu / ((double) SAMPLE_RATE / val[1]);
                double tmp = Math.sin(angle1) * 127f + Math.sin(angle2) * 127f;
                buff[i] = (byte) (tmp / 2d);
            }
        }
        return buff;
    }

    private static void playOneDtmf(char c, SourceDataLine line) {
        c = Character.toUpperCase(c);
        if (c == ' ') {
            play(line, pause, 50);
            return;
        }
        byte[] tone = makeDtmfWave(c);
        play(line, tone, 150);
    }

    public static void playDtmfString(String in) {
        playString(in, DtmfMorsePlayer::playOneDtmf);
    }

    public static void sendDtmftoBrowser(String in, PredefinedWords predefinedWords) throws Exception {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        for (char c : in.toCharArray()) {
            baos.write(makeDtmfWave(c));
        }
        byte[] complete = WaveTools.withWAVHeader(baos.toByteArray(), af);
        String encoded = Base64.getEncoder().encodeToString(complete);
        predefinedWords._jforth._out.print("audBytes" + encoded);
    }

    public static void playMorseString(String in) {
        playString(in, DtmfMorsePlayer::playOneMorse);
    }

    private static void playShort(SourceDataLine line) {
        play(line, sin, 50);
        play(line, pause, 100);
    }

    private static void playLong(SourceDataLine line) {
        play(line, sin, 200);
        play(line, pause, 100);
    }

    private static void playOneMorse(char c, SourceDataLine line) {
        if (c == '·')
            playShort(line);
        else if (c == '-')
            playLong(line);
        else if (c == ' ')
            play(line, pause, 200);
    }
}
