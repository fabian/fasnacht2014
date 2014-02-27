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
 * - Check chain configuration
 * - Make "loopsPerSecond" configurable
 * - cleanup the mess ;)
 *
 * NICE: save frequencies when closing application.
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

        ONE  (1, R.string.channel_1_title, R.string.channel_1_detail, "pref_channel_1_frequency", R.id.channel_frequency_item_1),
        TWO  (2, R.string.channel_2_title, R.string.channel_2_detail, "pref_channel_2_frequency", R.id.channel_frequency_item_2),
        THREE(3, R.string.channel_3_title, R.string.channel_3_detail, "pref_channel_3_frequency", R.id.channel_frequency_item_3),
        FOUR (4, R.string.channel_4_title, R.string.channel_4_detail, "pref_channel_4_frequency", R.id.channel_frequency_item_4);

        private int channelId;
        private int channelName;
        private int channelDetail;
        private String channelFrequencyPreferences;
        private int viewId;

        Channel(int channelId, int channelName, int channelDetail, String channelFrequencyPreferences, int viewId) {
            this.channelId = channelId;
            this.channelName = channelName;
            this.channelDetail = channelDetail;
            this.channelFrequencyPreferences = channelFrequencyPreferences;
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

    private static final int FREQUENCY_ONE = 150;
    private static final int FREQUENCY_TWO = 250;
    private static final int FREQUENCY_THREE = 350;
    private static final int FREQUENCY_FOUR = 450;

    public int getFrequencyRight() {
        return getFrequency(Channel.THREE, Channel.FOUR);
    }

    public int getFrequencyLeft() {
        return getFrequency(Channel.ONE, Channel.TWO);
    }

    private int getFrequency(Channel one, Channel two) {
        return getFrequency(channelItems.get(one).isChecked(), channelItems.get(two).isChecked());
    }

    long lastTime = System.currentTimeMillis();

    private int getFrequency(boolean one, boolean two) {
        if (System.currentTimeMillis() - lastTime > 500) {
            updateChannels();
            lastTime = System.currentTimeMillis();
        }
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

    private FrequencyGenerator frequencyGenerator;

    private int chainPosition;

    private int totalChainMembers;


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

        final Spinner spinner = (Spinner) findViewById(R.id.modes_spinner);

        Mode.updateTexts(this);
        final ArrayAdapter<Mode> adapter = new ArrayAdapter<Mode>(this, android.R.layout.simple_spinner_dropdown_item, Mode.values());

        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinner.setAdapter(adapter);
        spinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                updateMode(adapter.getItem(position));
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
            }
        });

        loadSettings();

        final Button syncButton = (Button) findViewById(R.id.sync_button);
        syncButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                lastTime = 0;
                reset();
            }
        });

        frequencyGenerator = new FrequencyGenerator(this);
        startFrequencyGenerator(true);
    }

    private void loadSettings() {
        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(this);
        // TODO save keys in a resource file
        chainPosition = Integer.valueOf(preferences.getString("chain_position", "1"));
        currentChainPos = 1;
        totalChainMembers = Integer.valueOf(preferences.getString("total_chain_members", "4"));
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
            loadSettings();
            return true;
        } else if (id == R.id.action_start) {
            reset();
            startFrequencyGenerator(true);
            startThread();
            return true;
        } else if (id == R.id.action_stop) {
            stopThread();
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

    private void updateMode(Mode mode) {
        switch (mode) {
            case MANUAL:
                stopThread();
                break;
            default:
                currentMode = mode;
                reset();
                startThread();
        }
    }

    private boolean running = false;

    private long loop = 500;

    private int currentChainPos;

    private Mode currentMode = Mode.MANUAL;

    private Runnable runnable = new Runnable() {
        @Override
        public void run() {
            while(running) {
                //updateChannels();
                try {
                    Thread.sleep((int) (loop));
                } catch (InterruptedException e) {
                }
            }
        }
    };
    private Thread thread;

    private void startThread() {
        if (!running) {
            running = true;
            thread = new Thread(runnable);
            thread.start();
        }
    }

    private void stopThread() {
        if (running) {
            running = false;
            try {
                thread.join();
            } catch (InterruptedException e) {
            }
            thread = null;
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
