package org.cerion.stocklist;

import org.jetbrains.annotations.NotNull;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

public class Price implements IPrice
{
	private java.util.Date _date;
	private float _open;
	private float _high;
	private float _low;
	private float _volume;
	private float _close;
	
	//Fields used in list
	public PriceList parent;
	public int pos;

	private static DateFormat mDateFormat = new SimpleDateFormat("yyyy-MM-dd");
	public String getFormattedDate() { return mDateFormat.format(_date); } //When it needs to be formatted properly
	public static String getDecimal(float val)
	{
		return String.format("%.2f",val);
	}

	public Price(IPrice p) {
		this(p.getDate(), p.getOpen(), p.getHigh(), p.getLow(), p.getClose(), p.getVolume());
	}

	public Price(java.util.Date date, float open, float high, float low, float close, float volume) {
		this._date = date;
		this._open = open;
		this._high = high;
		this._low = low;
		this._volume = volume;
		this._close = close;

		//Error checking
		if(open < low || close < low || open > high || close > high)
			throw new RuntimeException("Price range inconsistency " + String.format("%s,%f,%f,%f,%f", getFormattedDate(), open, high, low, close));
	}
	
	public int getDOW() {
		Calendar c = Calendar.getInstance();
		c.setTime(_date);
		return c.get(Calendar.DAY_OF_WEEK);
	}

	public float slope(int period) { return parent.slope(period, pos); } //Slope of closing price
	public float tp() { return parent.tp(pos); } //Typical price
	public float change(Price prev)
	{
		return getPercentDiff(prev);
	}
	public float getPercentDiff(Price old)
	{
		if(!old._date.before(_date))
			throw new RuntimeException("current price is older than input price");
		
		float diff = _close - old._close;
		float percent = (100 * (diff / old._close));
		return percent;
	}

	@Override public float getClose() {
		return _close;
	}
	@Override public float getHigh() { return _high; }
	@Override public float getLow() { return _low; }
	@Override public float getOpen() { return _open; }
	@Override public float getVolume() { return _volume; }
	@NotNull @Override public Date getDate() { return _date; }
}
