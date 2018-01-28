package autowatering.fr.autowatering.domain;

import java.io.Serializable;
import java.util.Arrays;
import java.util.SortedSet;
import java.util.TreeSet;

import static autowatering.fr.autowatering.domain.WeekDay.FRIDAY;
import static autowatering.fr.autowatering.domain.WeekDay.MONDAY;

/**
 * Created by Nabil on 23/03/2015.
 */
public class RelaySchedule implements Serializable {

    private Integer relayId;

    private SortedSet<WeekDay> days = new TreeSet<>(Arrays.asList(WeekDay.values()));

    private boolean forced;

    private boolean enabled;

    private String description;

    private Integer startHour = 10;

    private Integer endHour = 11;

    private Integer startMinutes = 00;

    private Integer endMinutes = 00;

    public RelaySchedule(Integer relayId) {
        this.relayId = relayId;
    }

    public void copyValuesTo(RelaySchedule toRelaySchedule) {
        toRelaySchedule.setRelayId(new Integer(this.getRelayId()));
        toRelaySchedule.setStartHour(new Integer(this.getStartHour()));
        toRelaySchedule.setStartMinutes(new Integer(this.getStartMinutes()));
        toRelaySchedule.setEndHour(new Integer(this.getEndHour()));
        toRelaySchedule.setEndMinutes(new Integer(this.getEndMinutes()));
        toRelaySchedule.setDays(new TreeSet<WeekDay>(this.getDays()));
        toRelaySchedule.setEnabled(this.isEnabled());
        toRelaySchedule.setDescription(this.description);
    }

    public Integer getRelayId() {
        return relayId;
    }

    public void setRelayId(Integer relayId) {
        this.relayId = relayId;
    }

    public boolean isEnabled() {
        return enabled;
    }

    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }


    public boolean isForced() {
        return forced;
    }

    public void setForced(boolean forced) {
        this.forced = forced;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public SortedSet<WeekDay> getDays() {
        return days;
    }

    public void setDays(SortedSet<WeekDay> days) {
        this.days = days;
    }

    public Integer getStartHour() {
        return startHour;
    }

    public void setStartHour(Integer startHour) {
        this.startHour = startHour;
    }

    public Integer getEndHour() {
        return endHour;
    }

    public void setEndHour(Integer endHour) {
        this.endHour = endHour;
    }

    public Integer getStartMinutes() {
        return startMinutes;
    }

    public void setStartMinutes(Integer startMinutes) {
        this.startMinutes = startMinutes;
    }

    public Integer getEndMinutes() {
        return endMinutes;
    }

    public void setEndMinutes(Integer endMinutes) {
        this.endMinutes = endMinutes;
    }

    public String getSelectedWeekDays() {
        if (days.isEmpty())
            return "relai désactivé";

        StringBuffer sb = new StringBuffer();
        for (WeekDay weekDay : days) {
            sb.append(weekDay.getTruncatedValue(2)).append(" ");
        }
        return sb.toString();
    }

    public void checkStartTime() {
        if (startHour > endHour) {
            int value = endHour - 1;
            startHour = value < 0 ? 0 : value;
        }
        if (startHour == endHour) {
            if (startMinutes >= endMinutes) {
                int value = endMinutes - 15;
                startHour = value < 0 ? (startHour == 0 ? 23 : startHour - 1) : startHour;
                startMinutes = value < 0 ? 0 : value;
            }
        }
    }

    public void checkEndTime() {
        if (endHour == 0 && endMinutes == 0) {
            endHour = 23;
            endMinutes = 59;
        }
        if (startHour > endHour) {
            int value = startHour + 1;
            endHour = value > 23 ? 0 : value;
        }
        if (startHour == endHour) {
            if (startMinutes >= endMinutes) {
                int value = startMinutes + 15;
                endHour = value > 59 ? (startHour == 23 ? 0 : startHour + 1) : startHour;
                endMinutes = value > 59 ? 0 : value;
            }
        }
    }

    public String getStartTime() {
        return convertToTimeFormat(startHour) + ":" + convertToTimeFormat(startMinutes);
    }

    public String getEndTime() {
        return convertToTimeFormat(endHour) + ":" + convertToTimeFormat(endMinutes);
    }

    public String getTimeInfo() {
        return getStartTime() + " - " + getEndTime();
    }

    private String convertToTimeFormat(Integer val) {
        if (String.valueOf(val).length() == 1) {
            return "0" + val;
        }
        return String.valueOf(val);
    }

    public void setWeekDaysFromArduino(String arduinoWeekdays) {
        days.clear();
        Byte b = Byte.parseByte(arduinoWeekdays, 2);
        for (int i = 0; i < 7; i++) {
            if (getBit(b, i) == 1) {
                switch (i) {
                    case 0:
                        days.add(WeekDay.SUNDAY);
                        break;
                    case 1:
                        days.add(MONDAY);
                        break;
                    case 2:
                        days.add(WeekDay.TUESDAY);
                        break;
                    case 3:
                        days.add(WeekDay.WEDNESDAY);
                        break;
                    case 4:
                        days.add(WeekDay.THURSDAY);
                        break;
                    case 5:
                        days.add(FRIDAY);
                        break;
                    case 6:
                        days.add(WeekDay.SATURDAY);
                        break;
                }
            }
        }

    }

    private int getBit(byte b, int position) {
        return (b >>> (position)) & 1;
    }

    private byte setBit(byte x, int position) {
        return (byte) (x | (1 << position));
    }

    private String getWeekDaysToArduino() {

        StringBuilder result = new StringBuilder("00000000");

        for (WeekDay weekDay : days) {
            switch (weekDay) {
                case SUNDAY:
                    result.setCharAt(7, '1');
                    break;
                case MONDAY:
                    result.setCharAt(6, '1');
                    break;
                case TUESDAY:
                    result.setCharAt(5, '1');
                    break;
                case WEDNESDAY:
                    result.setCharAt(4, '1');
                    break;
                case THURSDAY:
                    result.setCharAt(3, '1');
                    break;
                case FRIDAY:
                    result.setCharAt(2, '1');
                    break;
                case SATURDAY:
                    result.setCharAt(1, '1');
                    break;
            }
        }
        return result.toString();
    }

    public String getArduinoFormat() {
        String result = "";
        result += relayId;
        result += "|";
        result += forced ? "1" : "0";
        result += "|";
        result += enabled ? "1" : "0";
        result += "|";
        result += description;
        result += "|";
        result += getWeekDaysToArduino();
        result += "|";
        result += startHour;
        result += "|";
        result += startMinutes;
        result += "|";
        result += endHour;
        result += "|";
        result += endMinutes;

        return result;
    }
}
