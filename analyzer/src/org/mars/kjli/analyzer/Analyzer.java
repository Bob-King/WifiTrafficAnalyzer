package org.mars.kjli.analyzer;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.nio.file.DirectoryStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Map;
import java.util.SortedMap;
import java.util.SortedSet;
import java.util.TreeMap;
import java.util.logging.Level;

public class Analyzer {

	public Analyzer(Database database) {
		mDatabase = database;
	}

	public void load(File file) throws IOException {

		if (!file.isDirectory()) {
			throw new IllegalArgumentException("Directory must be specified!");
		}

		try (DirectoryStream<Path> entries = Files.newDirectoryStream(file.toPath(), "*.log")) {

			for (Path entry : entries) {
				if (Files.isDirectory(entry)) {
					continue;
				}

				try (ProbeRequestReader reader = new ProbeRequestReader(new FileInputStream(entry.toFile()))) {

					do {
						ProbeRequestRecord record = reader.readProbeRequestRecord();
						if (record == null) {
							break;
						}
						
						if (!mTracks.containsKey(record.ta)) {
							mTracks.put(record.ta, new TreeMap<Long, TrackRecords>());
						}

						addProbeRequestRecord(mTracks.get(record.ta), record);
					} while (true);

				} catch (IOException e) {
					MyLogger.get().log(Level.WARNING, "Reading file from " + entry.toString(), e);
				}
			}
		}
	}

	public void analyze() {
		for (long ta : mTracks.keySet()) {
			LocationRecords lrs = mLocations.get(ta);
			if (lrs == null) {
				mLocations.put(ta, new LocationRecords());
				lrs = mLocations.get(ta);
			}

			SortedMap<Long, TrackRecords> map = mTracks.get(ta);

			for (long tsf : map.keySet()) {
				String location = queryLocation(map.get(tsf));
				if (!location.equals("")) {
					lrs.add(tsf, location);
				}
			}
		}
	}
	
	public String queryLocation(TrackRecords trackRecords) {
		trackRecords = trackRecords.reduce();
		
		// TODO: need improvement for only one situation that only one AP received packets  
		if (trackRecords.records().size() < 2) {
			return "";
		}

		RssiRecord[] rssiRecords = new RssiRecord[trackRecords.records().size()];

		int i = 0;
		for (TrackRecords.Record tr : trackRecords.records()) {
			rssiRecords[i++] = new RssiRecord(tr.ra, tr.rssi);
		}

		return mDatabase.queryLocation(rssiRecords);
	}

	public String dump() {
		StringBuilder sb = new StringBuilder();
		for (long ta : mLocations.keySet()) {
			sb.append("MAC: " + Utils.long2MacAddress(ta) + "\n");

			SortedSet<LocationRecords.Record> records = mLocations.get(ta).Records();

			for (LocationRecords.Record record : records) {
				sb.append(Utils.formatTsf(record.tsf) + " : " + record.loc + "\n");
			}

		}

		return sb.toString();
	}

	public Map<Long, SortedMap<Long, TrackRecords>> getTracks() {
		return mTracks;
	}
	
	public Map<Long, LocationRecords> getLocations() {
		return mLocations;
	}

	private static void addProbeRequestRecord(SortedMap<Long, TrackRecords> map, ProbeRequestRecord record) {
		if (record.rssi < Utils.MIN_VALID_RSSI || record.rssi > Utils.MAX_VALID_RSSI) {
			return;
		}

		TrackRecords trs = map.get(record.tsf);

		if (trs == null) {
			map.put(record.tsf, new TrackRecords());
			trs = map.get(record.tsf);
		}

		trs.add(record.ra, record.seq, record.rssi);
	}

	private Map<Long, SortedMap<Long, TrackRecords>> mTracks = new TreeMap<>();
	private Map<Long, LocationRecords> mLocations = new TreeMap<>();
	private Database mDatabase;

}
