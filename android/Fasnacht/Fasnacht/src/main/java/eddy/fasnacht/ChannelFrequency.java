package eddy.fasnacht;

import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.SeekBar;
import android.widget.TextView;

import java.util.EnumMap;
import java.util.Map;

import static android.widget.SeekBar.OnSeekBarChangeListener;

/**
 * Channel frequency ui. Displays one seekbar for each channel_frequency_item. Allows the user to
 * set a frequency per channel_frequency_item.
 *
 * TODO: set correct vaulues for min and max frequency.
 * NICE: save frequencies when closing application.
 *
 * @author adrian
 */
public class ChannelFrequency extends Activity {

    /**
     * Maximum frequency allowed.
     */
    private static final int MAX_FREQUENCY = 1000;

    /**
     * Minimum frequency allowed.
     */
    private static final int MIN_FREQUENCY = 100;

    public static enum Channel {

        ONE(1, "pref_channel_1_frequency", R.id.channel_frequency_item_1),
        TWO(2, "pref_channel_2_frequency", R.id.channel_frequency_item_2),
        THREE(3, "pref_channel_3_frequency", R.id.channel_frequency_item_3),
        FOUR(4, "pref_channel_4_frequency", R.id.channel_frequency_item_4);

        private int channelId;
        private String channelFrequencyPreferences;
        private int viewId;

        Channel(int channelId, String channelFrequencyPreferences, int viewId) {
            this.channelId = channelId;
            this.channelFrequencyPreferences = channelFrequencyPreferences;
            this.viewId = viewId;
        }



        public int getChannelId() {
            return channelId;
        }

        public String getChannelFrequencyPreferences() {
            return channelFrequencyPreferences;
        }

        public int getViewId() {
            return viewId;
        }
    }
    public int getFrequency(Channel channel) {
        return channelItems.get(channel).getFrequency();
    }

    public int getChannelFrequency(Channel channel) {
        SharedPreferences sharedPref = PreferenceManager.getDefaultSharedPreferences(this);
        return Integer.parseInt(sharedPref.getString(channel.getChannelFrequencyPreferences(), "0"));
    }

    /**
     * Code representation of one channel_frequency_item in ui.
     */
    private static class ChannelFrequencyItem {

        private Activity activity;
        private TextView textTitle;
        private TextView textDetail;
        private SeekBar seekBar;

        private int frequency = MIN_FREQUENCY;

        public ChannelFrequencyItem(Activity activity, View view, int channelId) {
            this.activity = activity;
            textTitle = (TextView) view.findViewById(R.id.textTitle);
            textDetail = (TextView) view.findViewById(R.id.textDetail);
            seekBar = (SeekBar) view.findViewById(R.id.seekBar);

            textTitle.setText(activity.getText(R.string.channel) + " " + channelId);
            seekBar.setMax(MAX_FREQUENCY + MIN_FREQUENCY);
            seekBar.setOnSeekBarChangeListener(new OnSeekBarChangeListener() {
                @Override
                public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                    frequency = progress + MIN_FREQUENCY;
                    updateDetailText();
                }

                @Override
                public void onStartTrackingTouch(SeekBar seekBar) {
                }

                @Override
                public void onStopTrackingTouch(SeekBar seekBar) {
                }
            });

            updateDetailText();
        }

        private void updateDetailText() {
            textDetail.setText(frequency + " " + activity.getText(R.string.hz));
        }

        public int getFrequency() {
            return frequency;
        }
    }

    private FrequencyGenerator generator = new FrequencyGenerator(this);

    /**
     * Array of channel items.
     */
    private Map<Channel, ChannelFrequencyItem> channelItems =
            new EnumMap<Channel, ChannelFrequencyItem>(Channel.class);

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.channel_frequency);
        createChannelItem(Channel.ONE);
        createChannelItem(Channel.TWO);
        createChannelItem(Channel.THREE);
        createChannelItem(Channel.FOUR);
    }

    private void createChannelItem(Channel channel) {
        channelItems.put(channel, new ChannelFrequencyItem(this, findViewById(channel.getViewId()),
                channel.getChannelId()));
    }

    @Override
    protected void onResume() {
        super.onResume();
        generator.start();
    }

    @Override
    protected void onPause() {
        super.onPause();
        generator.stop();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.channel_frequency, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();
        if (id == R.id.action_settings) {
            Intent intent = new Intent(this, SettingsActivity.class);
            startActivity(intent);
            return true;
        }
        return super.onOptionsItemSelected(item);
    }
}
