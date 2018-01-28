package autowatering.fr.autowatering;


import android.app.Dialog;
import android.app.DialogFragment;
import android.app.Fragment;
import android.graphics.Color;
import android.os.Bundle;
import android.text.InputType;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.TimePicker;
import android.widget.ToggleButton;

import autowatering.fr.autowatering.domain.RelaySchedule;
import autowatering.fr.autowatering.domain.WeekDay;


/**
 * A simple {@link Fragment} subclass.
 * create an instance of this fragment.
 */
public class NewScheduleDialogFragment extends DialogFragment {
    // TODO: Rename parameter arguments, choose names that match
    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    private static final String ARG_RELAYSCHEDULE = "relaySchedule";
    private static final String ARG_CALLBACK = "callback";
    private static final int START_TIME_DIALOG = 0;

    private static final int END_TIME_DIALOG = 1;
    private static BluetoothService bluetoothService;

    private RelaySchedule relaySchedule;

    private RelaySchedule tmpRelaySchedule;

    private OnUpdateListener callback;

    private EditText descEditText;


    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @param relaySchedule Parameter 1.
     * @return A new instance of fragment NewScheduleDialogFragment.
     */
    // TODO: Rename and change types and number of parameters
    public static NewScheduleDialogFragment newInstance(RelaySchedule relaySchedule, OnUpdateListener callback, BluetoothService bs) {
        NewScheduleDialogFragment fragment = new NewScheduleDialogFragment();
        bluetoothService = bs;
        Bundle args = new Bundle();
        args.putSerializable(ARG_RELAYSCHEDULE, relaySchedule);
        args.putSerializable(ARG_CALLBACK, callback);
        fragment.setArguments(args);
        return fragment;
    }

    public NewScheduleDialogFragment() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            relaySchedule = (RelaySchedule) getArguments().getSerializable(ARG_RELAYSCHEDULE);
            tmpRelaySchedule = new RelaySchedule(relaySchedule.getRelayId());
            relaySchedule.copyValuesTo(tmpRelaySchedule);
            callback = (OnUpdateListener) getArguments().getSerializable(ARG_CALLBACK);
        }
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        descEditText = (EditText) getView().findViewById(R.id.descValue);
        descEditText.setInputType(InputType.TYPE_TEXT_FLAG_NO_SUGGESTIONS);
        updateStartTimeView();
        updateEndTimeView();
        updateDescription();
        setCancelable(true);
        createActionButtons();
        createWeekDaysButton();
        createTimePickerDialog(R.id.startValue, "Entrez l'heure de démarrage", START_TIME_DIALOG);
        createTimePickerDialog(R.id.endValue, "Entrez l'heure d'arrêt", END_TIME_DIALOG);

    }

    private void createActionButtons() {

        Button saveButton = (Button) getView().findViewById(R.id.save_relay_settings);
        saveButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                tmpRelaySchedule.setDescription(descEditText.getText().toString());
                tmpRelaySchedule.copyValuesTo(relaySchedule);
                //Send Bluetooth update
                callback.sendBTRequest(Constants.REQUEST_UPDATE + ":" + relaySchedule.getArduinoFormat());
                dismiss();
            }
        });

        Button cancelButton = (Button) getView().findViewById(R.id.cancel_relay_settings);
        cancelButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dismiss();
            }
        });
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        getDialog().setTitle("Configuration du relai " + (tmpRelaySchedule.getRelayId() + 1));

        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_new_schedule_dialog, container, false);
    }

    private void createWeekDaysButton() {
        LinearLayout weekDaysToggleBtnLayout = (LinearLayout) getView().findViewById(R.id.weekDaysToggleBtnLayout);
        for (final WeekDay weekDay : WeekDay.values()) {
            final ToggleButton toggleButton = new ToggleButton(getActivity());
            toggleButton.setTextOff(weekDay.getTruncatedValue(2));

            toggleButton.setTextOn(weekDay.getTruncatedValue(2));
            toggleButton.setMinimumWidth(0);
            toggleButton.setTextSize(10);
            int width = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 42, getResources().getDisplayMetrics());
            toggleButton.setLayoutParams(new ViewGroup.LayoutParams(width, ViewGroup.LayoutParams.WRAP_CONTENT));
            toggleButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (toggleButton.isChecked()) {
                        tmpRelaySchedule.getDays().add(weekDay);
                        toggleButton.setTextColor(Color.BLACK);
                    } else {
                        tmpRelaySchedule.getDays().remove(weekDay);
                        toggleButton.setTextColor(Color.GRAY);
                    }
                }
            });
            for (WeekDay relayDay : tmpRelaySchedule.getDays()) {
                if (relayDay.getValue().equals(weekDay.getValue())) {
                    toggleButton.setChecked(true);
                    break;
                }
            }
            if (toggleButton.isChecked())
                toggleButton.setTextColor(Color.BLACK);
            else
                toggleButton.setTextColor(Color.GRAY);
            weekDaysToggleBtnLayout.addView(toggleButton);
        }
    }

    private void createTimePickerDialog(int viewId, final String title, final int dialogType) {
        TextView textView = (TextView) getView().findViewById(viewId);
        textView.setOnClickListener(new View.OnClickListener() {
            TimePicker timePicker;

            @Override
            public void onClick(View arg0) {

                // custom dialog
                final Dialog dialog = new Dialog(getActivity());
                dialog.setContentView(R.layout.time_picker_dialog);
                dialog.setTitle(title);
                timePicker = (TimePicker) dialog.findViewById(R.id.dialogTimePicker);
                timePicker.setIs24HourView(true);

                switch (dialogType) {
                    case START_TIME_DIALOG:
                        timePicker.setCurrentHour(tmpRelaySchedule.getStartHour());
                        timePicker.setCurrentMinute(tmpRelaySchedule.getStartMinutes());
                        break;
                    case END_TIME_DIALOG:
                        timePicker.setCurrentHour(tmpRelaySchedule.getEndHour());
                        timePicker.setCurrentMinute(tmpRelaySchedule.getEndMinutes());
                        break;
                }

                Button dialogButton = (Button) dialog.findViewById(R.id.dialogButtonOK);
                // if button is clicked, close the custom dialog
                dialogButton.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        switch (dialogType) {
                            case START_TIME_DIALOG:
                                tmpRelaySchedule.setStartHour(timePicker.getCurrentHour());
                                tmpRelaySchedule.setStartMinutes(timePicker.getCurrentMinute());
                                updateStartTimeView();
                                break;
                            case END_TIME_DIALOG:
                                if (timePicker.getCurrentHour() == 0 && timePicker.getCurrentMinute() == 0) {
                                    tmpRelaySchedule.setEndHour(23);
                                    tmpRelaySchedule.setEndMinutes(59);
                                } else {
                                    tmpRelaySchedule.setEndHour(timePicker.getCurrentHour());
                                    tmpRelaySchedule.setEndMinutes(timePicker.getCurrentMinute());
                                }
                                updateEndTimeView();
                                break;
                        }
                        dialog.dismiss();
                    }
                });
                dialog.show();
            }
        });
    }

    public void updateTimeView() {

        TextView startTextView = (TextView) getView().findViewById(R.id.startValue);
        startTextView.setTextColor(Color.BLACK);
        startTextView.setText(tmpRelaySchedule.getStartTime());
        TextView endTextView = (TextView) getView().findViewById(R.id.endValue);
        endTextView.setTextColor(Color.BLACK);
        endTextView.setText(tmpRelaySchedule.getEndTime());
    }

    public void updateStartTimeView() {
        TextView startTextView = (TextView) getView().findViewById(R.id.startValue);
        startTextView.setTextColor(Color.BLACK);
        startTextView.setText(tmpRelaySchedule.getStartTime());
        tmpRelaySchedule.checkEndTime();
        TextView endTextView = (TextView) getView().findViewById(R.id.endValue);
        endTextView.setText(tmpRelaySchedule.getEndTime());
    }

    public void updateEndTimeView() {
        TextView endTextView = (TextView) getView().findViewById(R.id.endValue);
        endTextView.setTextColor(Color.BLACK);
        endTextView.setText(tmpRelaySchedule.getEndTime());
        tmpRelaySchedule.checkStartTime();
        TextView startTextView = (TextView) getView().findViewById(R.id.startValue);
        startTextView.setText(tmpRelaySchedule.getStartTime());
    }

    private void updateDescription() {
        descEditText.setText(tmpRelaySchedule.getDescription());
    }

}
