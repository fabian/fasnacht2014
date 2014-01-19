package eddy.fasnacht;

import android.media.AudioFormat;
import android.media.AudioManager;
import android.media.AudioTrack;
import android.util.Log;

/**
 * Static class which generates the frequencies and send the to the audio output.
 *
 * @author adrian
 */
public final class FrequencyGenerator {

    private final String TAG = FrequencyGenerator.class.getSimpleName();

    private final double duration = 0.5; // seconds
    private final int sampleRate = 8000;
    private final int numSamples = (int) (duration * sampleRate);
    private final double sampleLeft[] = new double[numSamples];
    private final double sampleRight[] = new double[numSamples];

    private final byte generatedSnd[] = new byte[4 * numSamples];

    private Thread thread;
    private final Runnable runnable = new Runnable() {
        public void run() {
            audioTrack.play();
            while (running) {
                playSound();
            }
        }
    };

    private final ChannelFrequency model;

    private boolean running = false;

    public FrequencyGenerator(ChannelFrequency model) {
        this.model = model;
    }

    public void start() {
        running = true;
        thread = new Thread(runnable);
        thread.start();
    }

    public void stop() {
        running = false;
        try {
            thread.join();
        } catch (InterruptedException e) {
            Log.w(TAG, "Exception while waiting for thread to terminate.", e);
        }
    }

    private void generateTone(ChannelFrequency.Channel channel){
        generateTone(model.getChannelFrequency(channel), model.getFrequency(channel));
    }

    private void generateTone(int freqOfToneLeft, int freqOfToneRight) {

        // fill out the array
        for (int i = 0; i < numSamples; ++i) {
            sampleLeft[i] = Math.sin(2 * Math.PI * i / (sampleRate/freqOfToneLeft));
            sampleRight[i] = Math.sin(2 * Math.PI * i / (sampleRate/freqOfToneRight));
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

    private final AudioTrack audioTrack = new AudioTrack(AudioManager.STREAM_MUSIC,
            sampleRate, AudioFormat.CHANNEL_OUT_STEREO,
            AudioFormat.ENCODING_PCM_16BIT, numSamples,
            AudioTrack.MODE_STREAM);

    private void playSound(){
        for (ChannelFrequency.Channel channel : ChannelFrequency.Channel.values()) {
            generateTone(channel);
            audioTrack.write(generatedSnd, 0, generatedSnd.length);
        }
    }
}
