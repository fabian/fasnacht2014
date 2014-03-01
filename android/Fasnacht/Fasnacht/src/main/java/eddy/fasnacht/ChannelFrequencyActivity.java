package eddy.fasnacht;

import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.Spinner;
import android.widget.Switch;
import android.widget.TextView;

import java.util.Collection;
import java.util.EnumMap;
import java.util.Map;


/**
 * Channel frequency ui. Displays one seekbar for each channel_frequency_item. Allows the user to
 * set a frequency per channel_frequency_item.
 *
 * TODO:
 * - Save configuration keys in a resource file
 * - cleanup the mess ;)
 *
 * @author adrian
 */
public class ChannelFrequencyActivity extends Activity {

    private enum Mode {
        MANUAL(R.string.manual_mode),
        BLINK(R.string.blink_mode),
        SWITCH(R.string.switch_mode),
        RANDOM(R.string.random_mode),
        CHAIN(R.string.chain_mode);

        private int textId;
        private CharSequence text;

        private Mode(int textId) {
            this.textId = textId;
        }

        public static void updateTexts(Activity activity) {
            for (Mode mode : values()) {
                mode.text = activity.getText(mode.textId);
            }
        }

        @Override
        public String toString() {
            return text != null ? text.toString() : super.toString();
        }
    }

    public static enum Channel {

        ONE  (R.string.channel_1_title, R.string.channel_1_detail, R.id.channel_frequency_item_1),
        TWO  (R.string.channel_2_title, R.string.channel_2_detail, R.id.channel_frequency_item_2),
        THREE(R.string.channel_3_title, R.string.channel_3_detail, R.id.channel_frequency_item_3),
        FOUR (R.string.channel_4_title, R.string.channel_4_detail, R.id.channel_frequency_item_4);

        private int channelName;
        private int channelDetail;
        private int viewId;

        Channel(int channelName, int channelDetail, int viewId) {
            this.channelName = channelName;
            this.channelDetail = channelDetail;
            this.viewId = viewId;
        }

        public int getChannelName() {
            return channelName;
        }

        public int getChannelDetail() {
            return channelDetail;
        }

        public int getViewId() {
            return viewId;
        }
    }

    public static class FrequencyHolder {
        private int left;
        private int right;

        private FrequencyHolder(int left, int right) {
            this.left = left;
            this.right = right;
        }

        public int getLeft() {
            return left;
        }

        public int getRight() {
            return right;
        }
    }

    /**
     * Code representation of one channel_frequency_item in ui.
     */
    private static class ChannelFrequencyItem {

        private Activity activity;

        private Switch switchWidget;

        private boolean checked;

        public ChannelFrequencyItem(Activity activity, View view, Channel channel) {
            this.activity = activity;
            TextView textTitle = (TextView) view.findViewById(R.id.textTitle);
            TextView textDetail = (TextView) view.findViewById(R.id.textDetail);
            switchWidget = (Switch) view.findViewById(R.id.switchWidget);

            textTitle.setText(activity.getText(channel.getChannelName()));
            textDetail.setText(activity.getText(channel.getChannelDetail()));
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

        public void setChecked(final boolean checked) {
            activity.runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    switchWidget.setChecked(checked);
                }
            });
            this.checked = checked;
        }
    }

    private static final int FREQUENCY_ONE = 150;
    private static final int FREQUENCY_TWO = 250;
    private static final int FREQUENCY_THREE = 350;
    private static final int FREQUENCY_FOUR = 450;

    private FrequencyGenerator frequencyGenerator;

    // settings
    private int interval;
    private int chainPosition;
    private int totalChainMembers;

    // loop variables
    private long lastTime;
    private Mode currentMode = Mode.MANUAL;
    private int currentChainPos;

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

        final Spinner spinner = (Spinner) findViewById(R.id.modes_spinner);

        Mode.updateTexts(this);
        final ArrayAdapter<Mode> adapter = new ArrayAdapter<Mode>(this, android.R.layout.simple_spinner_dropdown_item, Mode.values());

        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinner.setAdapter(adapter);
        spinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                currentMode = adapter.getItem(position);
                reset();
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
            }
        });

        final Button syncButton = (Button) findViewById(R.id.sync_button);
        syncButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                lastTime = 0;
                reset();
            }
        });

        loadSettings();
        frequencyGenerator = new FrequencyGenerator(this);
        startFrequencyGenerator(true);
    }

    private void loadSettings() {
        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(this);
        interval = Integer.valueOf(preferences.getString("pref_interval", "1"));
        chainPosition = Integer.valueOf(preferences.getString("pref_chain_position", "1"));
        totalChainMembers = Integer.valueOf(preferences.getString("pref_total_chain_members", "6"));
        reset();
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
            startActivityForResult(intent, 0);
            return true;
        } else if (id == R.id.action_start) {
            reset();
            startFrequencyGenerator(true);
            return true;
        } else if (id == R.id.action_stop) {
            startFrequencyGenerator(false);
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        loadSettings();
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

    private void updateMode(Mode mode) {
        switch (mode) {
            case MANUAL:
                break;
            default:
                currentMode = mode;
                reset();
        }
    }

    public boolean updateNeeded() {
        return System.currentTimeMillis() - lastTime >= interval;
    }

    public FrequencyHolder getFrequencies() {
        updateChannels();
        lastTime = System.currentTimeMillis();
        return new FrequencyHolder(getFrequency(Channel.ONE, Channel.TWO), getFrequency(Channel.THREE, Channel.FOUR));
    }

    private int getFrequency(Channel one, Channel two) {
        return getFrequency(channelItems.get(one).isChecked(), channelItems.get(two).isChecked());
    }

    private int getFrequency(boolean one, boolean two) {
        if (!one && !two) {
            return FREQUENCY_TWO;
        } else if (one && !two) {
            return FREQUENCY_ONE;
        } else if (!one && two) {
            return FREQUENCY_FOUR;
        } else {
            return FREQUENCY_THREE;
        }
    }

    private void updateChannels() {
        switch(currentMode) {
            case BLINK:
                switchChannel(channelItems.get(Channel.ONE));
                switchChannel(channelItems.get(Channel.TWO));
                switchChannel(channelItems.get(Channel.THREE));
                switchChannel(channelItems.get(Channel.FOUR));
                break;
            case CHAIN:
                setChannels(currentChainPos == chainPosition, channelItems.values());
                currentChainPos += 1;
                if (currentChainPos > totalChainMembers) {
                    currentChainPos = 1;
                }
                break;
            case SWITCH:
                switchChannel(channelItems.get(Channel.ONE), channelItems.get(Channel.THREE));
                switchChannel(channelItems.get(Channel.TWO), channelItems.get(Channel.FOUR));
                break;
            case RANDOM:
                channelItems.get(Channel.ONE).setChecked(Math.random() < 0.45);
                channelItems.get(Channel.TWO).setChecked(Math.random() < 0.45);
                channelItems.get(Channel.THREE).setChecked(Math.random() < 0.45);
                channelItems.get(Channel.FOUR).setChecked(Math.random() < 0.45);
                break;
        }
    }

    private void reset() {
        currentChainPos = 1;
        setChannels(false, channelItems.values());
        switch (currentMode) {
            case SWITCH:
                channelItems.get(Channel.ONE).setChecked(true);
                channelItems.get(Channel.TWO).setChecked(true);
                break;
            case CHAIN:
                if (currentChainPos == chainPosition) {
                    setChannels(true, channelItems.values());
                }
        }
    }

    private static void switchChannel(ChannelFrequencyItem item) {
        item.setChecked(!item.isChecked());
    }

    private static void switchChannel(ChannelFrequencyItem item1, ChannelFrequencyItem item2) {
        boolean checked1 = item1.isChecked();
        item1.setChecked(item2.isChecked());
        item2.setChecked(checked1);
    }

    private static void setChannels(boolean check, Collection<ChannelFrequencyItem> channelItems) {
        for (ChannelFrequencyItem item : channelItems) {
            item.setChecked(check);
        }
    }
}
