package com.ide.green;

import java.text.DecimalFormat;
import java.text.FieldPosition;
import java.text.Format;
import java.text.ParsePosition;


/**
 * @author Somoud
 *
 */
public class KwFormat extends Format {

	/**
	 * 
	 */
	private String frmt;
	private static final long serialVersionUID = 1L;
	
	public KwFormat(String frmt){
		super();
		this.frmt=frmt;
		
	}
	/* (non-Javadoc)
	 * @see java.text.Format#format(java.lang.Object, java.lang.StringBuffer, java.text.FieldPosition)
	 */
	@Override
	public StringBuffer format(Object arg0, StringBuffer arg1,
			FieldPosition arg2) {
		DecimalFormat  df= new DecimalFormat(this.frmt);
		double kw = ((Number) arg0).doubleValue();
		arg1.append(df.format(kw/1000));
		
		return arg1;
	}

	/* (non-Javadoc)
	 * @see java.text.Format#parseObject(java.lang.String, java.text.ParsePosition)
	 */
	@Override
	public Object parseObject(String arg0, ParsePosition arg1) {
		
		return null;
	}

}
