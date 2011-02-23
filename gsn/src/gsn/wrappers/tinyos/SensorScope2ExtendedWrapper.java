package gsn.wrappers.tinyos;

import gsn.wrappers.AbstractWrapper;
import gsn.beans.DataField;
import gsn.beans.AddressBean;
import org.apache.log4j.Logger;
import net.tinyos.packet.BuildSource;
import net.tinyos.packet.PacketSource;
import net.tinyos.util.PrintStreamMessenger;

import java.io.IOException;
import java.io.Serializable;
import java.text.DecimalFormat;


public class SensorScope2ExtendedWrapper extends AbstractWrapper {

    private transient Logger logger = Logger.getLogger(this.getClass());

    private static final int THREAD_RATE = 1000; //default THREAD_RATE, every 1 second.
    private int selected_station_id;

    private static final String INITPARAM_SOURCE = "source";
    private static final String INITPARAM_STATION_ID = "station_id";

    private static final int MAX_DUPN = 4; // maximum id for extended sensors supported (max number = max_dpun++1)

    private static final int INDEX_AIR_TEMP = (MAX_DUPN+1)*5;
    private static final int INDEX_AIR_HUMID = (MAX_DUPN+1)*2;
    private static final int INDEX_SOLAR_RAD = (MAX_DUPN+1)*3;
    private static final int INDEX_RAIN_METER = (MAX_DUPN+1)*4;
    private static final int INDEX_GROUND_TEMP_TNX = (MAX_DUPN+1);
    private static final int INDEX_AIR_TEMP_TNX = (MAX_DUPN+1)*6;
    private static final int INDEX_SOIL_TEMP_ECTM = (MAX_DUPN+1)*7;
    private static final int INDEX_SOIL_MOISTURE_ECTM = (MAX_DUPN+1)*8;
    private static final int INDEX_SOIL_WATER_POTENTIAL = (MAX_DUPN+1)*9;
    private static final int INDEX_SOIL_TEMP_DECAGON = (MAX_DUPN+1)*10;
    private static final int INDEX_SOIL_MOISTURE_DECAGON = (MAX_DUPN+1)*11;
    private static final int INDEX_SOIL_CONDUCT_DECAGON = (MAX_DUPN+1)*12;
    private static final int INDEX_WIND_DIRECTION = (MAX_DUPN+1)*13;
    private static final int INDEX_WIND_SPEED = (MAX_DUPN+1)*14;

    private int threadCounter = 0;

    private String source;

    DecimalFormat measure = new DecimalFormat("0.00");

    private DataField[] outputStructureCache = new DataField[]{
            new DataField("station_id", "int", "Station ID"),

            new DataField("int_batt_volt", "double", "Battery - Internal"),
            new DataField("ext_batt_volt", "double", "Battery - External"),
            new DataField("cpu_volt", "double", "CPU - Voltage"),
            new DataField("cpu_temp", "double", "CPU - Temperature"),

            new DataField("air_temp", "double", "SHT75 Temperature"),
            new DataField("air_temp_2", "double", "SHT75 Temperature (2)"),
            new DataField("air_temp_3", "double", "SHT75 Temperature (3)"),
            new DataField("air_temp_4", "double", "SHT75 Temperature (4)"),
            new DataField("air_temp_5", "double", "SHT75 Temperature (5)"),

            new DataField("air_humid", "double", "SHT75 Humidity"),
            new DataField("air_humid_2", "double", "SHT75 Humidity (2)"),
            new DataField("air_humid_3", "double", "SHT75 Humidity (3)"),
            new DataField("air_humid_4", "double", "SHT75 Humidity (4)"),
            new DataField("air_humid_5", "double", "SHT75 Humidity (5)"),

            new DataField("solar_rad", "double", "Davis Solar radiation"),
            new DataField("solar_rad_2", "double", "Davis Solar radiation (2)"),
            new DataField("solar_rad_3", "double", "Davis Solar radiation (3)"),
            new DataField("solar_rad_4", "double", "Davis Solar radiation (4)"),
            new DataField("solar_rad_5", "double", "Davis Solar radiation (5)"),

            new DataField("rain_meter", "double", "Davis Rain Meter"),
            new DataField("rain_meter_2", "double", "Davis Rain Meter (2)"),
            new DataField("rain_meter_3", "double", "Davis Rain Meter (3)"),
            new DataField("rain_meter_4", "double", "Davis Rain Meter (4)"),
            new DataField("rain_meter_5", "double", "Davis Rain Meter (5)"),

            new DataField("ground_temp_tnx", "double", "TNX Ground Temperature"),
            new DataField("ground_temp_tnx_2", "double", "TNX Ground Temperature (2)"),
            new DataField("ground_temp_tnx_3", "double", "TNX Ground Temperature (3)"),
            new DataField("ground_temp_tnx_4", "double", "TNX Ground Temperature (4)"),
            new DataField("ground_temp_tnx_5", "double", "TNX Ground Temperature (5)"),

            new DataField("air_temp_tnx", "double", "TNX Air Temperature"),
            new DataField("air_temp_tnx_2", "double", "TNX Air Temperature (2)"),
            new DataField("air_temp_tnx_3", "double", "TNX Air Temperature (3)"),
            new DataField("air_temp_tnx_4", "double", "TNX Air Temperature (4)"),
            new DataField("air_temp_tnx_5", "double", "TNX Air Temperature (5)"),

            new DataField("soil_temp_ectm", "double", "EC-TM Temperature"),
            new DataField("soil_temp_ectm_2", "double", "EC-TM Temperature (2)"),
            new DataField("soil_temp_ectm_3", "double", "EC-TM Temperature (3)"),
            new DataField("soil_temp_ectm_4", "double", "EC-TM Temperature (4)"),
            new DataField("soil_temp_ectm_5", "double", "EC-TM Temperature (5)"),

            new DataField("soil_moisture_ectm", "double", "EC-TM Moisture"),
            new DataField("soil_moisture_ectm_2", "double", "EC-TM Moisture (2)"),
            new DataField("soil_moisture_ectm_3", "double", "EC-TM Moisture (3)"),
            new DataField("soil_moisture_ectm_4", "double", "EC-TM Moisture (4)"),
            new DataField("soil_moisture_ectm_5", "double", "EC-TM Moisture (5)"),

            new DataField("soil_water_potential", "double", "Decagon MPS-1 Potential"),
            new DataField("soil_water_potential_2", "double", "Decagon MPS-1 Potential (2)"),
            new DataField("soil_water_potential_3", "double", "Decagon MPS-1 Potential (3)"),
            new DataField("soil_water_potential_4", "double", "Decagon MPS-1 Potential (4)"),
            new DataField("soil_water_potential_5", "double", "Decagon MPS-1 Potential (5)"),

            new DataField("soil_temp_decagon", "double", "Decagon Temperature"),
            new DataField("soil_temp_decagon_2", "double", "Decagon Temperature (2)"),
            new DataField("soil_temp_decagon_3", "double", "Decagon Temperature (3)"),
            new DataField("soil_temp_decagon_4", "double", "Decagon Temperature (4)"),
            new DataField("soil_temp_decagon_5", "double", "Decagon Temperature (5)"),

            new DataField("soil_moisture_decagon", "double", "Decagon Moisture"),
            new DataField("soil_moisture_decagon_2", "double", "Decagon Moisture (2)"),
            new DataField("soil_moisture_decagon_3", "double", "Decagon Moisture (3)"),
            new DataField("soil_moisture_decagon_4", "double", "Decagon Moisture (4)"),
            new DataField("soil_moisture_decagon_5", "double", "Decagon Moisture (5)"),

            new DataField("soil_conduct_decagon", "double", "Decagon Conductivity"),
            new DataField("soil_conduct_decagon_2", "double", "Decagon Conductivity (2)"),
            new DataField("soil_conduct_decagon_3", "double", "Decagon Conductivity (3)"),
            new DataField("soil_conduct_decagon_4", "double", "Decagon Conductivity (4)"),
            new DataField("soil_conduct_decagon_5", "double", "Decagon Conductivity (5)"),

            new DataField("wind_direction", "double", "Davis Anemometer Direction"),
            new DataField("wind_direction_2", "double", "Davis Anemometer Direction_2"),
            new DataField("wind_direction_3", "double", "Davis Anemometer Direction_3"),
            new DataField("wind_direction_4", "double", "Davis Anemometer Direction_4"),
            new DataField("wind_direction_5", "double", "Davis Anemometer Direction_5"),

            new DataField("wind_speed", "double", "Davis Anemometer Speed"),
            new DataField("wind_speed_2", "double", "Davis Anemometer Speed (2)"),
            new DataField("wind_speed_3", "double", "Davis Anemometer Speed (3)"),
            new DataField("wind_speed_4", "double", "Davis Anemometer Speed (4)"),
            new DataField("wind_speed_5", "double", "Davis Anemometer Speed (5)"),

            new DataField("timestamp", "bigint", "Timestamp")
    };

    private final int OUTPUT_STRUCTURE_SIZE = 76;

    private Serializable[] buffer = new Serializable[OUTPUT_STRUCTURE_SIZE];

    private long last_timestamp = -1;
    private long previous_timestamp = -1;

    private double[] buf = new double[OUTPUT_STRUCTURE_SIZE];
    private int[] count = new int[OUTPUT_STRUCTURE_SIZE];

    private boolean doPostStreamElement;

    PacketSource reader;

    public boolean initialize() {
        setName("SensorScope2-Thread" + (++threadCounter));
        AddressBean addressBean = getActiveAddressBean();

        // source
        if (addressBean.getPredicateValue(INITPARAM_SOURCE) != null) {
            source = addressBean.getPredicateValue(INITPARAM_SOURCE);
            logger.warn("Sensorscope source: " + source);
        } else {
            logger.warn("The specified parameter >" + INITPARAM_SOURCE + "< for >SensorScopeWrapper< is missing.");
            logger.warn("Initialization failed.");
            return false;
        }

        // station_id
        String station_id_str;
        if (addressBean.getPredicateValue(INITPARAM_STATION_ID) != null) {
            station_id_str = addressBean.getPredicateValue(INITPARAM_STATION_ID);
            logger.warn("Selected station-id: " + station_id_str);
        } else {
            logger.warn("The specified parameter >" + INITPARAM_STATION_ID + "< for >SensorScopeWrapper< is missing.");
            logger.warn("Initialization failed.");
            return false;
        }


        selected_station_id = Integer.parseInt(station_id_str);

        reader = BuildSource.makePacketSource(source);

        if (reader == null) {
            logger.warn("Invalid packet source: " + source);
            return false;
        }

        boolean to_return = true;

        try {
            reader.open(PrintStreamMessenger.err);
        }
        catch (IOException e) {
            logger.warn("Error on " + reader.getName() + ": " + e);
            to_return = false;
        }

        //reset buffers
        for (int i = 0; i < OUTPUT_STRUCTURE_SIZE; i++) {
            buf[i] = 0.0;
            count[i] = 0;
        }

        return to_return;
    }

    public void run() {
        while (isActive()) {
            try {
                // delay
                Thread.sleep(THREAD_RATE);
            } catch (InterruptedException e) {
                logger.error(e.getMessage(), e);
            }

            int[] packet = null;

            try {

                byte[] original_packet = reader.readPacket();

                packet = new int[original_packet.length];

                for (int i = 0; i < packet.length; i++)
                    packet[i] = byte2uint(original_packet[i]);

                /// TinyOS header
                int destination;
                int source;
                int length;
                int group;
                int type;

                destination = packet[1] * 256 + packet[2];
                source = packet[3] * 256 + packet[4];
                length = packet[5];
                group = packet[6];
                type = packet[7];

                if (type == 1) { //if (type != 1) continue; // ignore packets other than sensing data

                    if (logger.isDebugEnabled()) {
                        logger.debug("\nPacket (" + packet.length + ")");
                        logger.debug(list_array(packet));
                    }

                    if (logger.isDebugEnabled())
                        logger.debug("dst:" + destination + " src:" + source + " len:" + length + " grp:" + group + " typ:" + type);

                    /// TinyOS header

                    /// preambule

                    int hopCount = packet[8];
                    int stationID = packet[9] * 256 + packet[10];
                    int dataPayLoadSize = length - 3;

                    if (logger.isDebugEnabled())
                        logger.debug("hopCount:" + hopCount + " stationID:" + stationID + " dataPayLoad.size:" + dataPayLoadSize);


                    if (stationID == selected_station_id) {

                        /// preambule

                        // first data chunk

                        int currentChunk = 0;
                        boolean stillOtherChunks = true;

                        int currentChunk_begin = 11;

                        long reference_timestamp = -1;
                        int timestamp_offset = -1;

                        while (stillOtherChunks) {

                            long timestamp = -1;

                            if (currentChunk == 0) { // first chunk contains reference_timestamp

                                reference_timestamp = packet[currentChunk_begin] * 16777216 + packet[currentChunk_begin + 1] * 65536 + packet[currentChunk_begin + 2] * 256 + packet[currentChunk_begin + 3];
                                timestamp = reference_timestamp;
                                if (logger.isDebugEnabled())
                                    logger.debug("reference_timestamp => " + reference_timestamp + " :: " + list_array(packet, currentChunk_begin, currentChunk_begin + 3));
                                timestamp_offset = 5;

                            } else {

                                timestamp_offset = 2; // one byte only for other timestamps, relative to first
                                if (logger.isDebugEnabled())
                                    logger.debug("delta ts " + packet[currentChunk_begin]);
                                timestamp = reference_timestamp + packet[currentChunk_begin]; // time elapsed since previous timestamp
                                reference_timestamp = timestamp;

                            }

                            if (logger.isDebugEnabled()) {
                                logger.debug("Chunk : " + currentChunk);
                                logger.debug("timestamp => " + timestamp);
                            }

                            int dataLength = packet[currentChunk_begin + timestamp_offset - 1]; // just before data
                            int currentChunk_end = currentChunk_begin + timestamp_offset + dataLength - 1;

                            // debug only
                            if (logger.isDebugEnabled())
                                logger.debug("data length (current chunk) => " + dataLength + " (total=" + dataPayLoadSize + ")");
                            //int pos = currentChunk_begin + timestamp_offset-1;
                            //logger.debug("@"+pos+ " = "+packet[pos]);
                            //logger.debug("currentChunk_begin: " + currentChunk_begin + " currentChunk_end:" + currentChunk_end);
                            // debug only

                            //reading sensorData

                            int[] data = new int[dataLength];

                            for (int i = 0; i < dataLength; i++) {
                                data[i] = packet[currentChunk_begin + timestamp_offset + i];
                            }

                            // debug only
                            if (logger.isDebugEnabled())
                                logger.debug("data (" + currentChunk_begin + "," + currentChunk_end + ")= [ " + list_array(data) + "]");
                            //logger.debug("data ("+currentChunk_begin+","+currentChunk_end+")= [ " + list_array(packet,currentChunk_begin+timestamp_offset, currentChunk_end)+" ]"+" <-- DEBUG");
                            //logger.debug("chunk = [ "+ list_array(packet,currentChunk_begin, currentChunk_end)+" ]"+" <-- DEBUG");
                            // debug only

                            // int processReading(int[] _data): returns index of last data reading

                            boolean stillOtherReadingsInChunk = true;
                            int last_data_reading = -1; // index of last data reading, needed for processing possible further readings within a chunk
                            int readingShift = 0; // shift within readings, for multiple readings within a chunk

                            while (stillOtherReadingsInChunk) {

                                int ext = data[0 + readingShift] / 128;
                                int sid1 = data[0 + readingShift] % 128;
                                int sid = -1;
                                int dupn = 0;

                                if (logger.isDebugEnabled())
                                    logger.debug("ext:" + ext + " sid1:" + sid1);

                                int reading[];

                                if ((ext == 0) && (sid1 < 108)) { // no extension, no sid2

                                    reading = new int[2];
                                    reading[0] = data[1 + readingShift];
                                    reading[1] = data[2 + readingShift];
                                    last_data_reading = 2 + readingShift;
                                    sid = sid1;
                                    //logger.debug("SID=" + sid + " Reading=" + list_array(reading));

                                } else if ((ext == 1) && (sid1 < 108)) { // extension, but no sid2

                                    int data_dupn = data[1 + readingShift] / 16;
                                    int data_length = data[1 + readingShift] % 16;

                                    dupn = data_dupn;

                                    if (logger.isDebugEnabled())
                                        logger.debug("data_dupn=" + data_dupn + " data_length=" + data_length);

                                    reading = new int[data_length + 1];
                                    for (int i = 0; i < reading.length; i++)
                                        reading[i] = data[2 + i + readingShift]; // skip sid + dat_length
                                    last_data_reading = 1 + reading.length + readingShift;
                                    sid = sid1;
                                    //logger.debug("SID=" + sid + " Reading=" + list_array(reading));

                                } else if ((ext == 0) && (sid1 >= 108)) { // no extension, with sid2

                                    int sid2 = data[1];
                                    sid = (sid1 - 108) * 256 + sid2;
                                    reading = new int[2];
                                    reading[0] = data[2 + readingShift]; // shifted by 1, because of sid2
                                    reading[1] = data[3 + readingShift]; // shifted by 1, because of sid2
                                    last_data_reading = 3 + readingShift;

                                    //logger.debug("SID=" + sid + " Reading=" + list_array(reading));

                                } else {// (ext==1) && /sid1 >=108)

                                    int sid2 = data[1];
                                    sid = (sid1 - 108) * 256 + sid2;

                                    int data_dupn = data[2] / 16;
                                    int data_length = data[2] % 16;

                                    dupn = data_dupn;

                                    if (logger.isDebugEnabled())
                                        logger.debug("data_dupn=" + data_dupn + " data_length=" + data_length);

                                    // shift by 3
                                    reading = new int[data_length + 1];
                                    for (int i = 0; i < reading.length; i++)
                                        reading[i] = data[3 + i + readingShift];
                                    last_data_reading = 2 + reading.length + readingShift;

                                    //logger.debug("SID=" + sid + " Reading=" + list_array(reading));
                                }

                                // interpreting raw readings

                                double sid1_int_batt_volt;
                                double sid1_ext_batt_volt;
                                double sid1_cpu_volt;
                                double sid1_cpu_temp;
                                double sid2_air_temp;
                                double sid2_air_humid;
                                double sid4_solar_rad;
                                double sid5_rain_meter;
                                double sid6_ground_temp;
                                double sid6_air_temp;
                                double sid7_soil_temp;
                                double sid7_soil_moisture;
                                double sid8_soil_water_potential;
                                double sid9_soil_temp;
                                double sid9_soil_moisture;
                                double sid9_soil_conduct;
                                double sid10_wind_direction;
                                double sid10_wind_speed;

                                if (logger.isDebugEnabled()) {
                                    logger.debug("SensorID:" + sid + " Dupn:" + dupn + " Reading:" + list_array(reading));
                                    logger.debug("TS:" + timestamp + " StationID:" + stationID + " SensorID:" + sid + " Dupn:" + dupn);
                                }

                                last_timestamp = timestamp * 1000;
                                buffer[0] = new Integer(stationID);
                                buf[0] = stationID;

                                for (int i = 1; i <= OUTPUT_STRUCTURE_SIZE-2; i++)
                                    buffer[i] = null;
                                buffer[OUTPUT_STRUCTURE_SIZE-1] = new Long(last_timestamp);
                                doPostStreamElement = true;

                                // extended sensors (when other sensors share the same bus)
                                // are supported up to dupn=MAX_DUPN (MAX_DUPN+1 sensors in total)
                                if (dupn > MAX_DUPN)
                                    doPostStreamElement = false;

                                if (dupn <= MAX_DUPN)

                                    switch (sid) {

                                        case 1:
                                            long raw_int_batt_volt = reading[0] * 16 + reading[1] / 16;
                                            long raw_ext_batt_volt = (reading[1] % 16) * 256 + reading[2];
                                            long raw_cpu_volt = reading[3] * 16 + reading[4] / 16;
                                            long raw_cpu_temp = (reading[4] % 16) * 256 + reading[5];
                                            sid1_int_batt_volt = raw_int_batt_volt * 2.4 * 2.5 / 4095;
                                            sid1_ext_batt_volt = raw_ext_batt_volt * 6.12 * 2.5 / 4095 + 0.242;
                                            sid1_cpu_volt = raw_cpu_volt * 3.0 / 4095;
                                            sid1_cpu_temp = (raw_cpu_temp * 1.5 / 4095 - 0.986) / 0.00355;
                                            logger.info("sid1_int_batt_volt: " + measure.format(sid1_int_batt_volt) +
                                                    " sid1_ext_batt_volt: " + measure.format(sid1_ext_batt_volt) +
                                                    " sid1_cpu_volt: " + measure.format(sid1_cpu_volt) +
                                                    " sid1_cpu_temp: " + measure.format(sid1_cpu_temp));
                                            buffer[1] = new Double(sid1_int_batt_volt);
                                            buf[1] = sid1_int_batt_volt;
                                            count[1]++;
                                            buffer[2] = new Double(sid1_ext_batt_volt);
                                            buf[2] = sid1_ext_batt_volt;
                                            count[2]++;
                                            buffer[3] = new Double(sid1_cpu_volt);
                                            buf[3] = sid1_cpu_volt;
                                            count[3]++;
                                            buffer[4] = new Double(sid1_cpu_temp);
                                            buf[4] = sid1_cpu_temp;
                                            count[4]++;
                                            break;

                                        case 2:
                                            long raw_airtemp = reading[0] * 64 + reading[1] / 4;
                                            long raw_airhumidity = reading[3] / 64 + reading[2] * 4 + (reading[1] % 4) * 1024;

                                            sid2_air_temp = raw_airtemp * 1.0 / 100 - 39.6;
                                            sid2_air_humid = (raw_airhumidity * 1.0 * 0.0405) - 4 - (raw_airhumidity * raw_airhumidity * 0.0000028) + ((raw_airhumidity * 0.00008) + 0.01) * (sid2_air_temp - 25);
                                            logger.info("sid2_air_temp: " + measure.format(sid2_air_temp) +
                                                    " sid2_air_humid: " + measure.format(sid2_air_humid));
                                            buffer[INDEX_AIR_TEMP + dupn] = new Double(sid2_air_temp);
                                            buf[INDEX_AIR_TEMP + dupn] = sid2_air_temp;
                                            count[INDEX_AIR_TEMP + dupn]++;
                                            buffer[INDEX_AIR_HUMID + dupn] = new Double(sid2_air_humid);
                                            buf[INDEX_AIR_HUMID + dupn] = sid2_air_humid;
                                            count[INDEX_AIR_HUMID + dupn]++;
                                            break;

                                        case 4:
                                            long raw_solar_rad = reading[0] * 256 + reading[1];
                                            sid4_solar_rad = raw_solar_rad * 2.5 * 1000 * 6 / (4095 * 1.67 * 5);
                                            logger.info("sid4_solar_rad: " + measure.format(sid4_solar_rad));
                                            buffer[INDEX_SOLAR_RAD + dupn] = new Double(sid4_solar_rad);
                                            buf[INDEX_SOLAR_RAD + dupn] = sid4_solar_rad;
                                            count[INDEX_SOLAR_RAD + dupn]++;
                                            break;

                                        case 5:
                                            long raw_rain_meter = reading[0] * 256 + reading[1];
                                            sid5_rain_meter = raw_rain_meter * 0.254;
                                            logger.info("sid5_rain_meter: " + measure.format(sid5_rain_meter));
                                            buffer[INDEX_RAIN_METER + dupn] = new Double(sid5_rain_meter);
                                            buf[INDEX_RAIN_METER + dupn] = sid5_rain_meter;
                                            count[INDEX_RAIN_METER + dupn]++;
                                            break;

                                        case 6:
                                            long raw_ground_temp = reading[0] * 256 + reading[1];
                                            long raw_air_temp = reading[2] * 256 + reading[3];
                                            sid6_ground_temp = raw_ground_temp / 16.0 - 273.15;
                                            sid6_air_temp = raw_air_temp / 16.0 - 273.15;
                                            buffer[INDEX_GROUND_TEMP_TNX + dupn] = new Double(sid6_ground_temp);
                                            buf[INDEX_GROUND_TEMP_TNX + dupn] = sid6_ground_temp;
                                            count[INDEX_GROUND_TEMP_TNX + dupn]++;
                                            buffer[INDEX_AIR_TEMP_TNX + dupn] = new Double(sid6_air_temp);
                                            buf[INDEX_AIR_TEMP_TNX + dupn] = sid6_air_temp;
                                            count[INDEX_AIR_TEMP_TNX + dupn]++;
                                            logger.info("sid6_ground_temp: " + measure.format(sid6_ground_temp) +
                                                    " sid6_air_temp: " + measure.format(sid6_air_temp));
                                            break;

                                        case 7:
                                            long raw_soil_temp = reading[0] * 256 + reading[1];
                                            long raw_soil_moisture = reading[2] * 256 + reading[3];
                                            sid7_soil_temp = (raw_soil_temp - 400.0) / 10.0;
                                            sid7_soil_moisture = (raw_soil_moisture * 0.00104 - 0.5) * 100;
                                            buffer[INDEX_SOIL_TEMP_ECTM + dupn] = new Double(sid7_soil_temp);
                                            buf[INDEX_SOIL_TEMP_ECTM + dupn] = sid7_soil_temp;
                                            count[INDEX_SOIL_TEMP_ECTM + dupn]++;
                                            buffer[INDEX_SOIL_MOISTURE_ECTM + dupn] = new Double(sid7_soil_moisture);
                                            buf[INDEX_SOIL_MOISTURE_ECTM + dupn] = sid7_soil_moisture;
                                            count[INDEX_SOIL_MOISTURE_ECTM + dupn]++;
                                            logger.info("sid7_soil_temp: " + measure.format(sid7_soil_temp) +
                                                    " sid7_soil_moisture: " + measure.format(sid7_soil_moisture));
                                            break;

                                        case 8:
                                            long raw_soil_water_potential = reading[0] * 256 + reading[1];
                                            sid8_soil_water_potential = raw_soil_water_potential;
                                            buffer[INDEX_SOIL_WATER_POTENTIAL + dupn] = new Double(sid8_soil_water_potential);
                                            buf[INDEX_SOIL_WATER_POTENTIAL + dupn] = sid8_soil_water_potential;
                                            count[INDEX_SOIL_WATER_POTENTIAL + dupn]++;
                                            logger.info("sid8_soil_water_potential:" + measure.format(sid8_soil_water_potential));
                                            break;

                                        case 9:
                                            long raw_sid9_soil_temp = reading[0] * 256 + reading[1];
                                            long raw_sid9_soil_moisture = reading[2] * 256 + reading[3];
                                            long raw_sid9_soil_conduct = reading[4] * 256 + reading[5];
                                            if (raw_sid9_soil_temp <= 900)
                                                sid9_soil_temp = (raw_sid9_soil_temp - 400.0) / 10.0;
                                            else
                                                sid9_soil_temp = (900 + 5 * (raw_sid9_soil_temp - 900.0) - 400) / 10.0;
                                            sid9_soil_moisture = raw_sid9_soil_moisture / 50.0;
                                            if (raw_sid9_soil_conduct <= 700)
                                                sid9_soil_conduct = raw_sid9_soil_conduct / 100.0;
                                            else
                                                sid9_soil_conduct = (700 + 5.0 * (raw_sid9_soil_conduct - 700)) / 100.0;
                                            buffer[INDEX_SOIL_TEMP_DECAGON + dupn] = new Double(sid9_soil_temp);
                                            buf[INDEX_SOIL_TEMP_DECAGON + dupn] = sid9_soil_temp;
                                            count[INDEX_SOIL_TEMP_DECAGON + dupn]++;
                                            buffer[INDEX_SOIL_MOISTURE_DECAGON + dupn] = new Double(sid9_soil_moisture);
                                            buf[INDEX_SOIL_MOISTURE_DECAGON + dupn] = sid9_soil_moisture;
                                            count[INDEX_SOIL_MOISTURE_DECAGON + dupn]++;
                                            buffer[INDEX_SOIL_CONDUCT_DECAGON + dupn] = new Double(sid9_soil_conduct);
                                            buf[INDEX_SOIL_CONDUCT_DECAGON + dupn] = sid9_soil_conduct;
                                            count[INDEX_SOIL_CONDUCT_DECAGON + dupn]++;
                                            logger.info("sid9_soil_temp: " + measure.format(sid9_soil_temp) +
                                                    " sid9_soil_moisture: " + measure.format(sid9_soil_moisture) +
                                                    " sid9_soil_conduct: " + measure.format(sid9_soil_conduct));
                                            break;

                                        case 10:
                                            int sign = reading[0] / 128;
                                            long raw_sid10_wind_direction = (reading[0] % 16) * 256 + reading[1];
                                            long raw_sid10_wind_speed = reading[2] * 256 + reading[3];
                                            if (sign == 0)
                                                sid10_wind_direction = java.lang.Math.acos(((raw_sid10_wind_direction * 2.0) / 4095.0) - 1) * 360.0 / (2 * java.lang.Math.PI);
                                            else
                                                sid10_wind_direction = 360 - java.lang.Math.acos((raw_sid10_wind_direction * 2.0) / 4095.0 - 1) * 360.0 / (2 * java.lang.Math.PI);
                                            sid10_wind_speed = raw_sid10_wind_speed * 3600.0 * 1.6093 / (600 * 1600 * 3.6);
                                            buffer[INDEX_WIND_DIRECTION + dupn] = new Double(sid10_wind_direction);
                                            buf[INDEX_WIND_DIRECTION + dupn] = sid10_wind_direction;
                                            count[INDEX_WIND_DIRECTION + dupn]++;
                                            buffer[INDEX_WIND_SPEED + dupn] = new Double(sid10_wind_speed);
                                            buf[INDEX_WIND_SPEED + dupn] = sid10_wind_speed;
                                            count[INDEX_WIND_SPEED + dupn]++;
                                            logger.info("sid10_wind_direction: " + measure.format(sid10_wind_direction) +
                                                    " sid10_wind_speed: " + measure.format(sid10_wind_speed));
                                            break;

                                        default:
                                            logger.debug("Unknown SID:" + sid);
                                            doPostStreamElement = false;
                                            break;

                                    } // switch

                                if (doPostStreamElement) {
                                    /*
                                    logger.debug("\t\t\t" + previous_timestamp + " => " + last_timestamp);


                                    StringBuilder sb2 = new StringBuilder();
                                    NumberFormat nf2 = NumberFormat.getInstance();

                                    for (int i2 = 0; i2 < buf.length - 1; i2++)
                                        if (count[i2] == 0)
                                            sb2.append("......\t");
                                        else
                                            sb2.append(nf2.format(buf[i2])).append("\t");
                                    logger.debug("-- " + last_timestamp + " " + sb2.toString());
                                    */
                                    if (last_timestamp != previous_timestamp) {

                                        //logger.debug("");

                                        for (int i = 1; i < OUTPUT_STRUCTURE_SIZE-1; i++) {// accumulated values
                                            if (count[i] > 0)
                                                buffer[i] = new Double(buf[i]);
                                        }
                                        previous_timestamp = last_timestamp;

                                        postStreamElement(last_timestamp, buffer);

                                        /*
                                        StringBuilder sb = new StringBuilder();

                                        NumberFormat nf = NumberFormat.getInstance();

                                        for (int i = 0; i < buf.length - 1; i++)
                                            if (count[i] == 0)
                                                sb.append("------\t");
                                            else
                                                sb.append(nf.format(buf[i])).append("\t");

                                        logger.debug(">> " + last_timestamp + " " + sb.toString());
                                        logger.debug("***************************************");
                                        */


                                        // reset
                                        for (int i = 1; i < OUTPUT_STRUCTURE_SIZE; i++) { // i=1 => don't reset SID
                                            buffer[i] = null;
                                            buf[i] = -1;
                                            count[i] = 0;
                                        }
                                    }


                                }

                                if (logger.isDebugEnabled())
                                    logger.debug("last_data_reading => " + last_data_reading + " [" + data[last_data_reading] + "]");

                                if (last_data_reading < dataLength - 1) { // still other readings within chunk
                                    stillOtherReadingsInChunk = true;
                                    readingShift = last_data_reading + 1;
                                } else
                                    stillOtherReadingsInChunk = false;

                            } // while (stillOtherReadingsInChunk)

                            if (currentChunk_end < dataPayLoadSize + 10) { // still other chunks to process
                                stillOtherChunks = true;
                                currentChunk++;
                                currentChunk_begin = currentChunk_end + 1;
                            } else stillOtherChunks = false;

                        }
                    } // if (stationID == selected_station_id)
                } // if (type == 1)

            }
            catch (IOException e) {
                logger.warn("Error on " + reader.getName() + ": " + e);
            }
            catch (IndexOutOfBoundsException e) {
                logger.warn("Error while parsing SensorScope packet ("+selected_station_id+"):" + list_array(packet));
                logger.warn(e);
            }


        }

    }

    public void dispose() {
        threadCounter--;
    }

    public final DataField[] getOutputFormat() {
        return outputStructureCache;
    }

    public String getWrapperName() {
        return "Sensorscope2 packet wrapper";
    }

    private static int byte2uint(byte b) {
        int u = (b + 256) % 256;
        return u;
    }

    private static String list_array(int[] a) {
        StringBuilder sb = new StringBuilder();
        if (a != null)
            for (int i = 0; i < a.length; i++)
                sb.append(a[i]).append(" ");
        return sb.toString();
    }

    private static String list_array(int[] a, int begin, int end) {
        StringBuilder sb = new StringBuilder();
        if (a != null)
            for (int i = begin; (i < a.length) && (i <= end); i++)
                sb.append(a[i]).append(" ");
        return sb.toString();
    }


}