package com.ide.green;

public class GraphData {
	/**
	 * 
	 */
	private long updatedTime;
	/**
	 * 
	 */
	private double temperature;
	/**
	 * 
	 */
	private double power;

	/**
	 * @return
	 */
	public long getUpdatedTime() {
		return this.updatedTime;
	}

	/** 
	 * @return
	 */
	public double getTemperature() {
		return this.temperature;
	}

	/**
	 * @return
	 */
	public double getPower() {
		return this.power;
	}

	/**
	 * @param updatedTime
	 */
	public void setUpdatedTime(long updatedTime) {
		this.updatedTime = updatedTime;
	}

	/**
	 * @param temperature
	 */
	public void setTemperature(double temperature) {
		this.temperature = temperature;
	}

	/**
	 * @param power
	 */
	public void setPower(double power) {
		this.power = power;
	}

}
