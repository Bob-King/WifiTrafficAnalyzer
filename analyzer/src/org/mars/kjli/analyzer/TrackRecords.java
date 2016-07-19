package org.mars.kjli.analyzer;

import java.util.Comparator;
import java.util.SortedSet;
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
