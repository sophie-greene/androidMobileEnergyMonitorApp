package com.ide.green;

import java.text.FieldPosition;
import java.text.Format;
import java.text.ParsePosition;
import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * @author Somoud
 * 
 */
public class CDateFormat extends Format {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	// create a simple date format that draws on the year portion of 
	// timestamp.
	/**
	 * 
	 */
	private SimpleDateFormat dateFormat = new SimpleDateFormat("h:mma");

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.text.Format#format(java.lang.Object, java.lang.StringBuffer,
	 * java.text.FieldPosition)
	 */
	@Override
	public StringBuffer format(Object obj, StringBuffer toAppendTo,
			FieldPosition pos) {
		long timestamp = ((Number) obj).longValue();
		Date date = new Date(timestamp);
		return this.dateFormat.format(date, toAppendTo, pos);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.text.Format#parseObject(java.lang.String,
	 * java.text.ParsePosition)
	 */
	@Override
	public Object parseObject(String source, ParsePosition pos) {
		return null;

	}

}
