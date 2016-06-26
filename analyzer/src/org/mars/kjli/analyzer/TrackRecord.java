package org.mars.kjli.analyzer;

public class TrackRecord {

	public Header hdr;

	public Body body;

	public static class Header {
		public long tsf;
		public int seq;

		@Override
		public boolean equals(Object obj) {
			if (this == obj) {
				return true;
			}
			if (obj instanceof Header) {
				Header hdr = (Header) obj;
				return tsf == hdr.tsf && seq == hdr.seq;
			}

			return false;
		}

		@Override
		public int hashCode() {
			int r = (int) (tsf >> 32);
			r ^= (int) tsf;
			r ^= ((int) seq) << 16;

			return r;
		}

	}

	public static class Body {
		public RssiRecord[] rrs = new RssiRecord[N];

		public boolean add(long ra, byte rssi) {
			if (rssi == 0) {
				return false;
			}

			for (int i = 0; i != N; ++i) {
				if (rrs[i].ra == 0) {
					rrs[i].ra = ra;
					rrs[i].rssi = rssi;
					return true;
				}
			}

			return false;
		}

	}

	private static final int N = 4;

}
