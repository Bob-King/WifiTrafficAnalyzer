package org.mars.kjli.analyzer;

public class RssiRecord {
	public long ra;
	public byte rssi;

	public RssiRecord(long ra, byte rssi) {
		this.ra = ra;
		this.rssi = rssi;
	}
}