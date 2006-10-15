package gsn.wrappers;

import gsn.beans.DataField;
import gsn.beans.DataTypes;
import gsn.beans.StreamElement;
import gsn.storage.StorageManager;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.Serializable;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.TreeMap;

import javax.swing.Timer;

import org.apache.log4j.Logger;

/**
 * This wrapper presents the system current clock.
 * @author Ali Salehi (AliS, ali.salehi-at-epfl.ch)<br>
 */

public class SystemTime extends AbstractStreamProducer implements
	ActionListener {
   private static final Serializable[] EMPTY_DATA_PART = new Serializable[] {};

   private static final Integer[] EMPTY_FIELD_TYPES = new Integer[] {};

    private static final int INITIAL_DELAY = 5 * 1000;

    private static final int CLOCK_PERIODS = 1*1000;

    private String[] EMPTY_FIELD_LIST = new String[] {};

    private Collection<DataField> collection = new ArrayList<DataField>();

    private static int threadCounter = 0;

    private final transient Logger logger = Logger.getLogger(SystemTime.class);

    private Timer timer;

    public boolean initialize(TreeMap context) {
	boolean toReturn = super.initialize(context);
	if (toReturn == false)
	    return false;
	setName("DummyDataProducer-Thread" + (++threadCounter));
	return true;
    }

    public void run() {
	timer = new Timer(CLOCK_PERIODS, this);
	timer.setInitialDelay(INITIAL_DELAY);
	timer.start();
    }


    public Collection<DataField> getProducedStreamStructure() {
	return collection;
    }

    public void actionPerformed(ActionEvent actionEvent) {
	if (listeners.isEmpty())
	    return;
	StreamElement streamElement = new StreamElement(EMPTY_FIELD_LIST,
		EMPTY_FIELD_TYPES, EMPTY_DATA_PART, actionEvent.getWhen());
	publishData(streamElement);
    }

    public void finalize(HashMap context) {
	super.finalize(context);
	timer.stop();
	threadCounter--;

    }

}
