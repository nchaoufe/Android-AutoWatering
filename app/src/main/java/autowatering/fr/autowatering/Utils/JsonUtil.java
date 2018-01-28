package autowatering.fr.autowatering.Utils;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

import autowatering.fr.autowatering.domain.RelaySchedule;

/**
 * Created by Nabil on 26/03/2015.
 */
public class JsonUtil {


    public static int parseRelayCountResponse(String json) {
        long count = 0;
        try {
            JSONObject mainObject = new JSONObject(json);
            count = mainObject.getLong("rc");
        } catch (JSONException e) {
            e.printStackTrace();
        }

        return Integer.parseInt(Long.toString(count));
    }

    public static RelaySchedule parseRelayInfoResponse(String json) {
        RelaySchedule rs = null;
        try {
            JSONObject mainObject = new JSONObject(json);
            String[] content = mainObject.getString("ri").split("\\|");
            if (content.length == 9) {
                rs = new RelaySchedule(Integer.parseInt(content[0]));
                rs.setForced("1".equals(content[1]));
                rs.setEnabled("1".equals(content[2]));
                rs.setDescription(content[3]);
                rs.setWeekDaysFromArduino(content[4]);
                rs.setStartHour(Integer.parseInt(content[5]));
                rs.setStartMinutes(Integer.parseInt(content[6]));
                rs.setEndHour(Integer.parseInt(content[7]));
                rs.setEndMinutes(Integer.parseInt(content[8]));
            }

        } catch (JSONException e) {
            e.printStackTrace();
        }

        return rs;
    }

}
