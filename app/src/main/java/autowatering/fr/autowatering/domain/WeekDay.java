package autowatering.fr.autowatering.domain;

/**
 * Created by Nabil on 25/03/2015.
 */
public enum WeekDay {
    MONDAY("Lundi", "dowMonday"),
    TUESDAY("Mardi", "dowTuesday"),
    WEDNESDAY("Mercredi", "dowWednesday"),
    THURSDAY("Jeudi", "dowThursday"),
    FRIDAY("Vendredi", "dowFriday"),
    SATURDAY("Samedi", "dowSaturday"),
    SUNDAY("Dimanche", "dowSunday"),;


    private String value;

    private String arduinoValue;

    WeekDay(String value, String arduinoValue) {
        this.value = value;
        this.arduinoValue = arduinoValue;
    }

    public String getValue() {
        return value;
    }

    public void setValue(String value) {
        this.value = value;
    }

    public String getArduinoValue() {
        return arduinoValue;
    }

    public void setArduinoValue(String arduinoValue) {
        this.arduinoValue = arduinoValue;
    }

    public static WeekDay enumOf(String value) {
        for (WeekDay weekDay : values()) {
            if (weekDay.getValue().equals(value))
                return weekDay;
        }
        return null;
    }

    public String getTruncatedValue(int arg) {
        return getValue().substring(0, arg);
    }
}
