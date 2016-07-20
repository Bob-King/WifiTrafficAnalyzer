package org.mars.kjli.analyzer;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.Map;
import java.util.SortedSet;
import java.util.TreeMap;
import java.util.TreeSet;

public class TrackRecords {

	public static class Record {
		public long ra;
		public int seq;
		public byte rssi;

		public Record(long ra, int seq, byte rssi) {
			this.ra = ra;
			this.seq = seq;
			this.rssi = rssi;
		}

		@Override
		public boolean equals(Object obj) {
			if (this == obj) {
				return true;
			}

			if (!(obj instanceof Record)) {
				return false;
			}

			Record r = (Record) obj;
			return seq == r.seq && ra == r.ra && rssi == r.rssi;
		}

		@Override
		public int hashCode() {
			int r = (int) (ra >> 32);
			r ^= ra;
			r ^= (seq << 16) | rssi;
			return r;
		}

	}
	
	private static class RssiTable {
		byte[] table = new byte[Utils.MAX_VALID_RSSI - Utils.MIN_VALID_RSSI + 1];
		
		public static int rssi2Index(byte rssi) {
			return rssi - Utils.MIN_VALID_RSSI;
		}
		
		public static byte index2Rssi(int index) {
			return (byte)(index + Utils.MIN_VALID_RSSI);
		}
		
		public void increaseRssiCount(byte rssi, byte cnt) {
			table[rssi2Index(rssi)] += cnt;
		}
		
		public byte populerRssi() {
			int popular = 0;
			for (int i = 1; i != table.length; ++i) {
				if (table[i] > table[popular]) {
					popular = i;
				}
			}
			
			return index2Rssi(popular);
		}
	}
	
	public TrackRecords reduce() {
		Map<Long, RssiTable> map = new TreeMap<>();
		for (Record r : mRecords) {
			if (!map.containsKey(r.ra)) {
				map.put(r.ra, new RssiTable());
			}
			
			RssiTable table = map.get(r.ra);
			table.increaseRssiCount(r.rssi, (byte)1);
			if (r.rssi - (byte)2 >= Utils.MIN_VALID_RSSI) {
				table.increaseRssiCount((byte)(r.rssi - 2), (byte)1);
			}
			if (r.rssi - (byte)1 >= Utils.MIN_VALID_RSSI) {
				table.increaseRssiCount((byte)(r.rssi - 1), (byte)1);
			}
			if (r.rssi + (byte)1 <= Utils.MAX_VALID_RSSI) {
				table.increaseRssiCount((byte)(r.rssi + 1), (byte)1);
			}
			if (r.rssi + (byte)2 <= Utils.MAX_VALID_RSSI) {
				table.increaseRssiCount((byte)(r.rssi + 2), (byte)1);
			}
		}
		
		TrackRecords ret = new TrackRecords();
		
		for (long ra: map.keySet()) {
			ret.add(ra,  0, map.get(ra).populerRssi());
		}		
		
		return ret;
	}

	// FIX ME: bad performance, need re-implementation
	public ArrayList<Record> packetsWithSeq(int seq) {
		if (seq < mRecords.first().seq || seq > mRecords.last().seq) {
			return new ArrayList<Record>();
		}

		ArrayList<Record> ret = new ArrayList<>();
		for (Record r : mRecords) {
			if (seq == r.seq) {
				ret.add(r);
			} else if (seq < r.seq) {
				break;
			}
		}

		return ret;
	}

	public SortedSet<Record> Records() {
		return mRecords;
	}

	public void add(long ra, int seq, byte rssi) {
		mRecords.add(new Record(ra, seq, rssi));
	}

	private SortedSet<Record> mRecords = new TreeSet<Record>(
			new Comparator<Record>() {

				@Override
				public int compare(Record o1, Record o2) {
					if (o1.seq < o2.seq) {
						return -2;
					} else if (o1.seq > o2.seq) {
						return 2;
					} else {
						if (o1.ra < o2.ra) {
							return -1;
						} else if (o1.ra > o2.ra) {
							return 1;
						} else {
							return 0;
						}
					}
				}

			});

}
