package tinygsn.model.vsensor;

import java.io.Serializable;
import java.util.ArrayList;

import tinygsn.beans.DataField;
import tinygsn.beans.DataTypes;
import tinygsn.beans.StreamElement;

public class ExposureVirtualSensor extends AbstractVirtualSensor {


	private static final long serialVersionUID = -3819903462891465980L;
	
	private ArrayList<StreamElement> buffer = new ArrayList<StreamElement>();
	
	private DataField[] outputStructure = new DataField[]{new DataField("exposure",DataTypes.DOUBLE),new DataField("start",DataTypes.BIGINT),new DataField("end",DataTypes.BIGINT)};

	@Override
	public boolean initialize() {
		
		return true;
	}

	@Override
	public void dispose() {
	}

	@Override
	public void dataAvailable(String inputStreamName,
			StreamElement streamElement) {
		if(inputStreamName.equalsIgnoreCase("met")){
			double va = (Double)streamElement.getData("VA"); 
			long s = (Long)streamElement.getData("start");
			long e = (Long)streamElement.getData("end");
			
			double sum = 0.0;
			for (StreamElement se:buffer){
				sum += ((Double)se.getData("o3"))/1000.0;
			}
			sum /= 1.0*buffer.size();
			double exposure = va * sum * (e-s)/60000.0;
			dataProduced(new StreamElement(outputStructure, new Serializable[]{exposure,s,e}, streamElement.getTimeStamp()));
			buffer.clear();
		}else{
			buffer.add(streamElement); 
		}

	}

}
