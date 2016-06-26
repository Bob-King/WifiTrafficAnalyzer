package org.mars.kjli.analyzer;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.nio.file.DirectoryStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.HashMap;
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
								new HashMap<TrackRecord.Header, TrackRecord.Body>());
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
		
	}

	private static void addProbeRequestRecord(
			HashMap<TrackRecord.Header, TrackRecord.Body> map,
			ProbeRequestRecord record) {
		if (record.rssi < Utils.MIN_VALID_RSSI
				|| record.rssi > Utils.MAX_VALID_RSSI) {
			return;
		}

		TrackRecord.Header hdr = new TrackRecord.Header();
		hdr.tsf = record.tsf;
		hdr.seq = record.seq;

		if (!map.containsKey(hdr)) {
			map.put(hdr, new TrackRecord.Body());
		}

		map.get(hdr).add(record.ra, record.rssi);
	}

	private Map<Long, HashMap<TrackRecord.Header, TrackRecord.Body>> mTracks = new TreeMap<>();
	private SortedMap<Long, ArrayList<Short>> mTimestamp2SequenceMap = new TreeMap<>();

}
