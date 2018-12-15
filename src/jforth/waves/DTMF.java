package jforth.waves;

import java.util.HashMap;

import static jforth.waves.WaveForms.curveSine;


/**
 * Creates DTMF samples
 */
public class DTMF
{
    /**
     * Sampling rate of all samples
     */
    private final int samplingRate;
    /**
     * delay between numbers
     */
    private final int delay;

    /**
     * Map of all samples
     */
    private final HashMap<Character, Wave16> wavemap = new HashMap<>();
    

    /**
     * Main Constructor: Builds all DTMF samples
     * @param rate         Sampling rate
     * @param samples      Samples per number
     * @param delaysamples Number of delay samples
     */
    private DTMF (int rate, int samples, int delaysamples)
    {
        samplingRate = rate;
        Wave16 s1 = curveSine(samplingRate, samples, new double[]{1209, 697},0);
        Wave16 s2 = curveSine(samplingRate, samples, new double[]{1336, 697},0);
        Wave16 s3 = curveSine(samplingRate, samples, new double[]{1477, 697},0);
        Wave16 SA = curveSine(samplingRate, samples, new double[]{1633, 697},0);

        Wave16 s4 = curveSine(samplingRate, samples, new double[]{1209, 770},0);
        Wave16 s5 = curveSine(samplingRate, samples, new double[]{1336, 770},0);
        Wave16 s6 = curveSine(samplingRate, samples, new double[]{1477, 770},0);
        Wave16 SB = curveSine(samplingRate, samples, new double[]{1633, 770},0);

        Wave16 s7 = curveSine(samplingRate, samples, new double[]{1209, 770},0);
        Wave16 s8 = curveSine(samplingRate, samples, new double[]{1336, 770},0);
        Wave16 s9 = curveSine(samplingRate, samples, new double[]{1477, 770},0);
        Wave16 SC = curveSine(samplingRate, samples, new double[]{1633, 770},0);

        Wave16 sstar = curveSine(samplingRate, samples, new double[]{1209, 941},0);
        Wave16 s0 = curveSine(samplingRate, samples, new double[]{1336, 941},0);
        Wave16 ssharp = curveSine(samplingRate, samples, new double[]{1477, 941},0);
        Wave16 SD = curveSine(samplingRate, samples, new double[]{1633, 941},0);

        wavemap.put('1', s1);
        wavemap.put('2', s2);
        wavemap.put('3', s3);
        wavemap.put('A', SA);
        wavemap.put('4', s4);
        wavemap.put('5', s5);
        wavemap.put('6', s6);
        wavemap.put('B', SB);
        wavemap.put('7', s7);
        wavemap.put('8', s8);
        wavemap.put('9', s9);
        wavemap.put('C', SC);
        wavemap.put('*', sstar);
        wavemap.put('0', s0);
        wavemap.put('#', ssharp);
        wavemap.put('D', SD);
        
        delay = delaysamples;
    }

    /**
     * Secondary constructor: Calls main constructor but without delay value
     * @param rate    Sampling rate
     * @param samples Number of samples per DTMF tone
     */
    public DTMF (int rate, int samples)
    {
        this(rate, samples, 0);
    }

    /**
     * Creates sampling object from string that contains valid key IDs
     * @param in Input string
     * @return Sampling object
     */
    public Wave16 dtmfFromString(String in)
    {
        Wave16[] inWave;
        if (delay == 0)
        {
            inWave = new Wave16[in.length()];
            for (int s = 0; s < in.length(); s++)
            {
                inWave[s] = wavemap.get(in.charAt(s));
            }
        }
        else
        {
            inWave = new Wave16[in.length() * 2];
            Wave16 delWave = new Wave16(delay, samplingRate);
            for (int s = 0; s < in.length(); s++)
            {
                inWave[s * 2] = wavemap.get(in.charAt(s));
                inWave[s * 2 + 1] = delWave;
            }
        }
        return Wave16.combineAppend(inWave);
    }
}
