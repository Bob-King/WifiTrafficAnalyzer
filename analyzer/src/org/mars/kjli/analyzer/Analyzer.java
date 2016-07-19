package org.mars.kjli.analyzer;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.nio.file.DirectoryStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Map;
import java.util.SortedMap;
import java.util.TreeMap;
import java.util.logging.Level;

public class Analyzer {

	public void load(File file) throws IOException {

		if (!file.isDirectory()) {
			throw new IllegalArgumentException("Directory must be specified!");
		}

		try (DirectoryStream<Path> entries = Files.newDirectoryStream(
				file.toPath(), "*.log")) {

			for (Path entry : entries) {
				if (Files.isDirectory(entry)) {
					continue;
				}

				try (ProbeRequestReader reader = new ProbeRequestReader(
						new FileInputStream(entry.toFile()))) {

					ProbeRequestRecord record = reader.readProbeRequestRecord();
					if (!mTracks.containsKey(record.ta)) {
						mTracks.put(
								record.ta,
								new TreeMap<Long, TrackRecords>());
					}

					addProbeRequestRecord(mTracks.get(record.ta), record);

				} catch (IOException e) {
					MyLogger.get().log(Level.WARNING,
							"Reading file from " + entry.toString(), e);
				}
			}

		}

	}
	
	public void analyze() {
		for (long ta : mTracks.keySet()) {
			LocationRecords lrs = mLocations.get(ta);
			if (lrs == null) {
				lrs = mLocations.put(ta, new LocationRecords());
			}
			
			SortedMap<Long, TrackRecords> map = mTracks.get(ta);
			
			for (long tsf : map.keySet()) {
				TrackRecords rrs = map.get(tsf);
			}
		}
	}

	private static void addProbeRequestRecord(
			SortedMap<Long, TrackRecords> map,
			ProbeRequestRecord record) {
		if (record.rssi < Utils.MIN_VALID_RSSI
				|| record.rssi > Utils.MAX_VALID_RSSI) {
			return;
		}
		
		TrackRecords trs = map.get(record.tsf); 
		
		if (trs == null) {
			trs = map.put(record.tsf, new TrackRecords());
		}
		
		trs.add(record.ra, record.seq, record.rssi);
	}

	private Map<Long, SortedMap<Long, TrackRecords>> mTracks = new TreeMap<>();
	private Map<Long, LocationRecords> mLocations = new TreeMap<>();

}
