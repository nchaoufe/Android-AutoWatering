/*
 * Copyright (C) 2014 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package autowatering.fr.autowatering;

/**
 * Defines several constants used between {@link BluetoothService} and the UI.
 */
public interface Constants {

    // Intent request codes
    public static final int REQUEST_CONNECT_DEVICE = 1;


    // Arduino Requests
    public static final String REQUEST_RELAYS_COUNT = "RC";
    public static final String REQUEST_RELAY_INFO = "RI";
    public static final String REQUEST_SET_TIME = "ST";
    public static final String REQUEST_UPDATE = "UR";

}
