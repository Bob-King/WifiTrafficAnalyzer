package org.mars.kjli.analyzer;

import java.util.Comparator;
import java.util.SortedSet;
import java.util.TreeSet;

public class LocationRecords {
	
	public static class Record {
		public long tsf;
		public String loc;
		
		public Record(long tsf, String loc) {
			this.tsf = tsf;
			this.loc = loc;
		}
	}
	
	public SortedSet<Record> Records() {
		return mRecords;
	}
	
	public void add(long tsf, String location) {
		mRecords.add(new Record(tsf, location));
	}
	
	private SortedSet<Record> mRecords = new TreeSet<>(new Comparator<Record>() {

		@Override
		public int compare(Record o1, Record o2) {
			return (int)(o1.tsf - o2.tsf);
		}
		
	});

}
