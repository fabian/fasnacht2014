package eddy.fasnacht;

import android.content.SharedPreferences;
import android.media.AudioFormat;
import android.media.AudioManager;
import android.media.AudioRecord;
import android.media.AudioTrack;
import android.preference.PreferenceManager;
import android.util.Log;

/**
 * Static class which generates the frequencies and send the to the audio output.
 *
 * @author adrian
 */
public final class FrequencyGenerator {

    private final String TAG = FrequencyGenerator.class.getSimpleName();

    private final double duration; // seconds
    private final int sampleRate = 44100;
    private final int numSamples;
    private final double sampleLeft[];
    private final double sampleRight[];

    private int currentI = 0;

    private final byte generatedSnd[];

    private Thread thread;
    private final Runnable runnable = new Runnable() {
        public void run() {
            audioTrack.play();
            while (running) {
                playSound();
            }
        }
    };

    private final ChannelFrequencyActivity activity;

    private final AudioTrack audioTrack;

    private boolean running = false;

    private int audioTrackBufferSize;

    public FrequencyGenerator(ChannelFrequencyActivity activity) {
        this.activity = activity;

        SharedPreferences sharedPref = PreferenceManager.getDefaultSharedPreferences(activity);
        duration = 1.0 / Integer.parseInt(sharedPref.getString("pref_loops_per_second", "2"));

        int minBufferSize = AudioRecord.getMinBufferSize(sampleRate,
                AudioFormat.CHANNEL_OUT_STEREO,
                AudioFormat.ENCODING_PCM_16BIT);

        long durationInMillis = 10;

        numSamples = (int) (1.0 * sampleRate / (1000 / durationInMillis));
        audioTrackBufferSize = numSamples - (numSamples % 4) + 4;

        Log.i(TAG, "minBufferSize=" + minBufferSize + ", numSamples=" + numSamples + ", bufferSize=" + audioTrackBufferSize);

        audioTrack = new AudioTrack(AudioManager.STREAM_MUSIC,
                sampleRate, AudioFormat.CHANNEL_OUT_STEREO,
                AudioFormat.ENCODING_PCM_16BIT, audioTrackBufferSize,
                AudioTrack.MODE_STREAM);

        sampleLeft = new double[numSamples];
        sampleRight = new double[numSamples];
        generatedSnd = new byte[4 * numSamples];
    }

    public void start() {
        if (!running) {
            running = true;
            thread = new Thread(runnable);
            thread.start();
        }
    }

    public void stop() {
        if (running) {
            running = false;
            try {
                thread.join();
            } catch (InterruptedException e) {
                Log.w(TAG, "Exception while waiting for thread to terminate.", e);
            }
        }
    }

    private double generateSample(int index, int frequency) {
        if (frequency > 0) {
            int tmp = sampleRate/frequency;
            if (tmp > 0) {
                return Math.sin(2 * Math.PI * index / tmp);
            }
        }
        return 0;

    }

    private void generateTone(int freqOfToneLeft, int freqOfToneRight) {

        // fill out the array
        for (int i = 0; i < numSamples; ++i) {
            sampleLeft[i] = generateSample(currentI, freqOfToneLeft);
            sampleRight[i] = generateSample(currentI, freqOfToneRight);
            currentI++;
        }

        // convert to 16 bit pcm sound array
        // assumes the sample buffer is normalised.
        int idx = 0;
        for (int i = 0; i < numSamples; i++) {
            // scale to maximum amplitude
            final short valLeft = (short) ((sampleLeft[i] * 32767));
            final short valRight = (short) ((sampleRight[i] * 32767));

            // in 16 bit wav PCM, first byte is the low order byte
            generatedSnd[idx++] = (byte) (valLeft & 0x00ff);
            generatedSnd[idx++] = (byte) ((valLeft & 0xff00) >>> 8);
            generatedSnd[idx++] = (byte) (valRight & 0x00ff);
            generatedSnd[idx++] = (byte) ((valRight & 0xff00) >>> 8);

        }
    }

    private void playSound(){
        generateTone(activity.getFrequencyLeft(), activity.getFrequencyRight());
        audioTrack.write(generatedSnd, 0, generatedSnd.length);

        long currentTime = System.currentTimeMillis();
        Log.i(TAG, "Tone sent, diff is: " + (currentTime - lastTime) + ", numSamples=" + numSamples + ", bufferSize=" + audioTrackBufferSize);
        lastTime = currentTime;
    }

    private long lastTime = 0;

    private int generateFrequency(boolean channelOne, boolean channelTwo) {

        if (channelOne) {
            if (channelTwo) {
                return 450;
            } else {
                return 350;
            }
        } else {
            if (channelTwo) {
                return 250;
            } else {
                return 150;
            }
        }
    }
    private void generateTone(boolean channelOne, boolean channelTwo, boolean channelThree, boolean channelFour) {
        generateTone(generateFrequency(channelOne, channelTwo), generateFrequency(channelThree, channelFour));
    }

    public boolean isRunning() {
        return running;
    }
}
