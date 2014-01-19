package eddy.fasnacht;

import android.app.Activity;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.SeekBar;
import android.widget.TextView;

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
    }

    /**
     * Array of channel items.
     */
    private ChannelFrequencyItem[] channelItems = new ChannelFrequencyItem[4];

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_channel_frequency);
        channelItems[0] =
                new ChannelFrequencyItem(this, findViewById(R.id.channel_frequency_item_1), 1);
        channelItems[1] =
                new ChannelFrequencyItem(this, findViewById(R.id.channel_frequency_item_2), 2);
        channelItems[2] =
                new ChannelFrequencyItem(this, findViewById(R.id.channel_frequency_item_3), 3);
        channelItems[3] =
                new ChannelFrequencyItem(this, findViewById(R.id.channel_frequency_item_4), 4);
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
            return true;
        }
        return super.onOptionsItemSelected(item);
    }
}
