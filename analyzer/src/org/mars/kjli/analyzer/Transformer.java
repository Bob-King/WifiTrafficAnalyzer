package org.mars.kjli.analyzer;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Path;
import java.util.Comparator;
import java.util.SortedSet;
import java.util.TreeSet;

public class Transformer {
	
	public void transform(File file) throws IOException {
		if (!file.isFile()) {
			throw new IllegalArgumentException("File must be specified!");
		}

		Path path = file.toPath();
		Path sorted = path.getFileSystem().getPath(path.getParent().toString(), path.getFileName().toString() + ".sorted.log");
		Path tsfFormated = path.getFileSystem().getPath(path.getParent().toString(), path.getFileName().toString() + ".tsf.txt");
		

		SortedSet<ProbeRequestRecord> set = new TreeSet<>(PROBE_REQUEST_RECORD_COMPARATOR);
		
		try (ProbeRequestReader reader = new ProbeRequestReader(new FileInputStream(file));
				BufferedWriter sortedWriter = new BufferedWriter(new FileWriter(sorted.toFile()));
				BufferedWriter tsfFormatedWriter = new BufferedWriter(new FileWriter(tsfFormated.toFile()))) {
			
			ProbeRequestRecord record = reader.readProbeRequestRecord();
			while (record != null) {
				set.add(record);
				record = reader.readProbeRequestRecord();
			}
			
			for (ProbeRequestRecord r : set) {
				sortedWriter.append(r.toString());
				sortedWriter.newLine();
				tsfFormatedWriter.append(r.formatTsf());
				tsfFormatedWriter.newLine();
			}
			
		}

	}
	
	private static final Comparator<ProbeRequestRecord> PROBE_REQUEST_RECORD_COMPARATOR = new Comparator<ProbeRequestRecord>() {

		@Override
		public int compare(ProbeRequestRecord o1, ProbeRequestRecord o2) {
			if (o1.tsf != o2.tsf) {
				return (int) (o1.tsf - o2.tsf);
			}
			
			if (o1.ra != o2.ra) {
				return (int) (o1.ra - o2.ra);
			}
			
			if (o1.seq != o2.seq) {
				return o1.seq - o2.seq;
			}
			
			if (o1.ta != o2.ta) {
				return (int) (o1.ta - o2.ta);
			}
			
			return o1.rssi - o2.rssi;
		}
		
	};
	

}
