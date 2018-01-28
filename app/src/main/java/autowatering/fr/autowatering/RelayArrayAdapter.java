package autowatering.fr.autowatering;

import android.app.Activity;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageButton;
import android.widget.TextView;

import java.util.List;

import autowatering.fr.autowatering.domain.RelaySchedule;

import static android.view.View.VISIBLE;

/**
 * Created by Nabil on 28/03/2015.
 */
public class RelayArrayAdapter extends ArrayAdapter<RelaySchedule> {

    private final Activity mainActivity;
    private final List<RelaySchedule> relaySchedules;
    private final OnUpdateListener callback;

    public RelayArrayAdapter(Activity context, int resource, List<RelaySchedule> relaySchedules, OnUpdateListener callback) {
        super(context, resource, relaySchedules);
        this.mainActivity = context;
        this.relaySchedules = relaySchedules;
        this.callback = callback;
    }

    @Override
    public View getView(final int position, View convertView, ViewGroup parent) {
        LayoutInflater inflater = (LayoutInflater) mainActivity
                .getSystemService(Context.LAYOUT_INFLATER_SERVICE);

        View rowView = inflater.inflate(R.layout.relay_schedule_item, parent, false);
        TextView descTextView = (TextView) rowView.findViewById(R.id.relay_desc);
        int relayId = relaySchedules.get(position).getRelayId() + 1;
        descTextView.setText(Integer.toString(relayId) + " : " + relaySchedules.get(position).getDescription());

        final TextView weekDaysTextView = (TextView) rowView.findViewById(R.id.relay_weekdays);
        weekDaysTextView.setText(relaySchedules.get(position).getSelectedWeekDays());

        final TextView timeInfoTextView = (TextView) rowView.findViewById(R.id.item_relay_time_info);
        timeInfoTextView.setText(relaySchedules.get(position).getTimeInfo());

        final ImageButton stateButton = (ImageButton) rowView.findViewById(R.id.relay_state_image);
        final ImageButton forcedButton = (ImageButton) rowView.findViewById(R.id.relay_forced_image);
        stateButton.setFocusable(false);
        forcedButton.setFocusable(false);

        if (relaySchedules.get(position).isEnabled()) {
            stateButton.setBackgroundResource(R.drawable.on);
        } else {
            stateButton.setBackgroundResource(R.drawable.off);
        }

        if (relaySchedules.get(position).isForced()) {
            forcedButton.setBackgroundResource(R.drawable.switch_manual);
            weekDaysTextView.setVisibility(View.INVISIBLE);
            timeInfoTextView.setVisibility(View.INVISIBLE);
        } else {
            forcedButton.setBackgroundResource(R.drawable.switch_auto);
            weekDaysTextView.setVisibility(VISIBLE);
            timeInfoTextView.setVisibility(VISIBLE);
        }

        stateButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                relaySchedules.get(position).setEnabled(!relaySchedules.get(position).isEnabled());
                if (relaySchedules.get(position).isEnabled()) {
                    stateButton.setBackgroundResource(R.drawable.on);
                } else {
                    stateButton.setBackgroundResource(R.drawable.off);
                }
                callback.sendBTRequest(Constants.REQUEST_UPDATE + ":" + relaySchedules.get(position).getArduinoFormat());
                //new AsyncBluetoothTask().execute(AsyncBluetoothTask.SEND_REQUEST,request) ;
            }
        });

        forcedButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                relaySchedules.get(position).setForced(!relaySchedules.get(position).isForced());
                if (relaySchedules.get(position).isForced()) {
                    forcedButton.setBackgroundResource(R.drawable.switch_manual);
                    weekDaysTextView.setVisibility(View.INVISIBLE);
                    timeInfoTextView.setVisibility(View.INVISIBLE);
                } else {
                    forcedButton.setBackgroundResource(R.drawable.switch_auto);
                    weekDaysTextView.setVisibility(VISIBLE);
                    timeInfoTextView.setVisibility(VISIBLE);
                }
                callback.sendBTRequest(Constants.REQUEST_UPDATE + ":" + relaySchedules.get(position).getArduinoFormat());
            }
        });
        return rowView;
    }
}
