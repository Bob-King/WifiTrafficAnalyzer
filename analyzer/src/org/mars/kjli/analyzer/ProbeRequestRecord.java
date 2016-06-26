package org.mars.kjli.analyzer;

class ProbeRequestRecord {

	public long ra;
	public long ta;
	public long tsf;
	public int seq;
	public byte rssi;

	@Override
	public String toString() {
		StringBuilder sb = new StringBuilder(80);
		sb.append("ra=");
		sb.append(Utils.long2MacAddress(ra));
		sb.append(" ta=");
		sb.append(Utils.long2MacAddress(ta));
		sb.append(" tsf=");
		sb.append(tsf);
		sb.append(" seq=");
		sb.append(seq);
		sb.append(" rssi=");
		sb.append(rssi);

		return sb.toString();
	}
	
	public String formatTsf() {
		StringBuilder sb = new StringBuilder(80);
		sb.append("ra=");
		sb.append(Utils.long2MacAddress(ra));
		sb.append(" ta=");
		sb.append(Utils.long2MacAddress(ta));
		sb.append(" tsf=\"");
		sb.append(Utils.formatTsf(tsf));
		sb.append("\" seq=");
		sb.append(seq);
		sb.append(" rssi=");
		sb.append(rssi);

		return sb.toString();
	}

}
