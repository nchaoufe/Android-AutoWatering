package autowatering.fr.autowatering;

import java.io.Serializable;

/**
 * Created by Nabil on 29/03/2015.
 */
public interface OnUpdateListener extends Serializable {

    public void sendBTRequest(String requests);
}
