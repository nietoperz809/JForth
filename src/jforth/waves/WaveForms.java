package jforth.waves;

/**
 * Created by Administrator on 1/18/2016.
 */
public class WaveForms
{
    private static final double pow1 = Math.pow(2.0, 3.0 / 12.0);
    private static final double pow2 = Math.pow(2.0, 7.0 / 12.0);
    private static final double pow3 = Math.pow(2.0, 4.0 / 12.0);

    static public Wave16 sineMoll(int samplingrate, int samples, double startfreq, int startval)
    {
        double[] freq = {startfreq, startfreq*pow1, startfreq*pow2};
        return curveSine(samplingrate, samples, freq, startval);
    }

    static public Wave16 curveSine(int samplingrate, int samples, double[] freq, int startval)
    {
        Wave16 out = new Wave16(samples, samplingrate);

        for (int x = 0; x < samples; x++)
        {
            double f = 0;
            for (double aFreq : freq)
            {
                double d1 = Wave16.PI2 / samplingrate * aFreq;
                f = f + (Wave16.MAX_VALUE * Math.sin(startval * d1));
            }
            out.data[x] = f; // / freq.length;
            startval++;
        }
        out.data = Wave16.fitValues(out.data);
        return out;
    }

    static public Wave16 sineDur(int samplingrate, int samples, double startfreq, int startval)
    {
        double[] freq = {startfreq, startfreq*pow3, startfreq*pow2};
        return curveSine(samplingrate, samples, freq, startval);
    }

    static public Wave16 curvePulse(int samplingrate, int samples, double freq, int startval)
    {
        return curveRect(samplingrate, samples, freq, startval).deriveAndFitValues();
    }

    static public Wave16 curveRect(int samplingrate, int samples, double freq, int startval)
    {
        Wave16 out = new Wave16(samples, samplingrate);

        double d1 = Wave16.PI2 / samplingrate * freq;
        for (int x = 0; x < samples; x++)
        {
            out.data[x] = Wave16.MAX_VALUE * Math.signum(Math.sin(startval * d1));
            startval++;
        }
        out.data = Wave16.fitValues(out.data);
        return out;
    }

    static public Wave16 curveSawTooth(int samplingrate, int samples, double freq, int startval)
    {
        Wave16 out = new Wave16(samples, samplingrate);

        double d1 = Wave16.PI / samplingrate * freq;
        //double d2 = samplingrate/freq;
        for (int x = 0; x < samples; x++)
        {
            out.data[x] = Wave16.MAX_VALUE * Math.asin(Math.sin(startval * d1)) / Wave16.ASIN1 * Math.pow(-1,
                    Math.floor(0.5 + startval / ((double) samplingrate / freq)));
            startval++;
        }
        out.data = Wave16.fitValues(out.data);
        return out;
    }

    static public Wave16 curveSine(int samplingrate, int samples, double freq, int startval)
    {
        Wave16 out = new Wave16(samples, samplingrate);

        double d1 = Wave16.PI2 / samplingrate * freq;
        for (int x = 0; x < samples; x++)
        {
            out.data[x] = Wave16.MAX_VALUE * Math.sin(startval * d1);
            startval++;
        }
        out.data = Wave16.fitValues(out.data);
        return out;
    }

    static public Wave16 sweepSine(int samplingrate, int fstart, int fend, double seconds)
    {
        double time = seconds * samplingrate;
        return sweepSine(samplingrate, fstart, fend, (int) time);
    }

    static public Wave16 sweepSine(int samplingrate, int fstart, int fend, int samples)
    {
        Wave16 out = new Wave16(samples, samplingrate, WaveFormType.SweepSIN);
        double step = (((double) fend - (double) fstart) / samples / Wave16.PI);
        double fact = fstart < fend ? fstart : fend;
        for (int x = 0; x < samples; x++)
        {
            out.data[x] =
                    Wave16.MAX_VALUE * Math.sin(2 * Wave16.PI * fact * ((double) x / samplingrate));
            fact += step;
        }
        return out;
    }

    static public Wave16 sweepTriangle(int samplingrate, int fstart, int fend, double seconds)
    {
        double time = seconds * samplingrate;
        return sweepTriangle(samplingrate, fstart, fend, (int) time);
    }

    static public Wave16 sweepTriangle(int samplingrate, int fstart, int fend, int samples)
    {
        Wave16 out = new Wave16(samples, samplingrate, WaveFormType.SweepTRI);
        double step = ((double) fend - (double) fstart) / samples / Wave16.PI;
        double fact = fstart < fend ? fstart : fend;
        for (int x = 0; x < samples; x++)
        {
            double c1 = Math.sin(2 * Wave16.PI * fact * ((double) x / samplingrate));
            out.data[x] = Wave16.MAX_VALUE * Math.asin(c1) / Math.asin(1);
            fact += step;
        }
        return out;
    }

    static public Wave16 sweepSquare(int samplingrate, int fstart, int fend, double seconds)
    {
        double time = seconds * samplingrate;
        return sweepSquare(samplingrate, fstart, fend, (int) time);
    }

    static public Wave16 sweepSquare(int samplingrate, int fstart, int fend, int samples)
    {
        Wave16 out = new Wave16(samples, samplingrate, WaveFormType.SweepSQR);
        double step = (((double) fend - (double) fstart) / samples / Wave16.PI);
        double fact = fstart < fend ? fstart : fend;
        for (int x = 0; x < samples; x++)
        {
            out.data[x] = Wave16.MAX_VALUE * Math.signum(
                    Math.sin(2 * Wave16.PI * fact * ((double) x / samplingrate)));
            fact += step;
        }
        return out;
    }

    static public Wave16 sweepPulse(int samplingrate, int fstart, int fend, double seconds)
    {
        double time = seconds * samplingrate;
        return sweepPulse(samplingrate, fstart, fend, (int) time);
    }

    static public Wave16 sweepPulse(int samplingrate, int fstart, int fend, int samples)
    {
        Wave16 wv = sweepSquare(samplingrate, fstart, fend, samples).deriveAndFitValues();
        wv.waveType = WaveFormType.SweepPUL;
        return wv;
    }

    static public Wave16 curveTriangle(int samplingrate, int samples, double freq, int startval)
    {
        Wave16 out = new Wave16(samples, samplingrate);

        double d1 = Wave16.PI2 / samplingrate * freq;
        for (int x = 0; x < samples; x++)
        {
            out.data[x] = Wave16.MAX_VALUE * Math.asin(Math.sin(startval * d1)) / Wave16.ASIN1;
            startval++;
        }
        out.data = Wave16.fitValues(out.data);
        return out;
    }
}
