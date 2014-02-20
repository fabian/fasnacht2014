package eddy.fasnacht;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.CompoundButton;
import android.widget.Switch;
import android.widget.TextView;

import java.util.EnumMap;
import java.util.Map;


/**
 * Channel frequency ui. Displays one seekbar for each channel_frequency_item. Allows the user to
 * set a frequency per channel_frequency_item.
 *
 * TODO: set correct vaulues for min and max frequency.
 * NICE: save frequencies when closing application.
 *
 * @author adrian
 */
public class ChannelFrequencyActivity extends Activity {

    public static enum Channel {

        ONE(1, R.string.channel_1_title, "pref_channel_1_frequency", R.id.channel_frequency_item_1),
        TWO(2, R.string.channel_2_title, "pref_channel_2_frequency", R.id.channel_frequency_item_2),
        THREE(3, R.string.channel_3_title, "pref_channel_3_frequency", R.id.channel_frequency_item_3),
        FOUR(4, R.string.channel_4_title, "pref_channel_4_frequency", R.id.channel_frequency_item_4);

        private int channelId;
        private int channelName;
        private String channelFrequencyPreferences;
        private int viewId;

        Channel(int channelId, int channelName, String channelFrequencyPreferences, int viewId) {
            this.channelId = channelId;
            this.channelName = channelName;
            this.channelFrequencyPreferences = channelFrequencyPreferences;
            this.viewId = viewId;
        }

        public int getChannelId() {
            return channelId;
        }

        public int getChannelName() {
            return channelName;
        }

        public String getChannelFrequencyPreferences() {
            return channelFrequencyPreferences;
        }

        public int getViewId() {
            return viewId;
        }
    }

    public boolean getFrequency(Channel channel) {
        return channelItems.get(channel).isChecked();
    }

    /**
     * Code representation of one channel_frequency_item in ui.
     */
    private static class ChannelFrequencyItem {

        private TextView textTitle;
        private Switch switchWidget;

        private boolean checked;

        public ChannelFrequencyItem(Activity activity, View view, Channel channel) {
            textTitle = (TextView) view.findViewById(R.id.textTitle);
            switchWidget = (Switch) view.findViewById(R.id.switchWidget);

            textTitle.setText(activity.getText(channel.getChannelName()));
            switchWidget.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
                @Override
                public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                    ChannelFrequencyItem.this.checked = isChecked;
                }
            });
        }

        public boolean isChecked() {
            return checked;
        }
    }

    private FrequencyGenerator frequencyGenerator;

    /**
     * Array of channel items.
     */
    private Map<Channel, ChannelFrequencyItem> channelItems =
            new EnumMap<Channel, ChannelFrequencyItem>(Channel.class);

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        frequencyGenerator = new FrequencyGenerator(this);

        setContentView(R.layout.channel_frequency);
        createChannelItem(Channel.ONE);
        createChannelItem(Channel.TWO);
        createChannelItem(Channel.THREE);
        createChannelItem(Channel.FOUR);

        startFrequencyGenerator(true);
    }

    private void createChannelItem(Channel channel) {
        channelItems.put(channel, new ChannelFrequencyItem(this, findViewById(channel.getViewId()),
                channel));
    }

    @Override
    protected void onDestroy() {
        startFrequencyGenerator(false);
        super.onDestroy();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.channel_frequency, menu);
        this.menu = menu;
        updateMenu(frequencyGenerator.isRunning());
        return true;
    }

    private Menu menu;

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
        } else if (id == R.id.action_start) {
            startFrequencyGenerator(true);
            return true;
        } else if (id == R.id.action_stop) {
            startFrequencyGenerator(false);
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    private void startFrequencyGenerator(boolean start) {
        updateMenu(start);
        if (start) {
            frequencyGenerator.start();
        } else {
            frequencyGenerator.stop();
        }
    }

    private void updateMenu(boolean running) {
        if (menu != null) {
            menu.findItem(R.id.action_start).setVisible(!running);
            menu.findItem(R.id.action_stop).setVisible(running);
        }
    }


}
