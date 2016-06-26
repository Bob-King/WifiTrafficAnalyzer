package org.mars.kjli.analyzer;

import java.io.BufferedReader;
import java.io.Closeable;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

class ProbeRequestReader implements Closeable {

	public ProbeRequestReader(InputStream is) {
		mBufferedReader = new BufferedReader(new InputStreamReader(is));
	}

	public ProbeRequestRecord readProbeRequestRecord() throws IOException {
		final String line = mBufferedReader.readLine();

		if (line == null) {
			return null;
		}

		Matcher matcher = PROBE_REQUEST_RECORD_LINE_PATTERN.matcher(line);
		if (!matcher.find()) {
			return null;
		}

		ProbeRequestRecord record = new ProbeRequestRecord();
		record.ra = Long.valueOf(Utils.macAddress2Long(matcher.group(1)));
		record.ta = Long.valueOf(Utils.macAddress2Long(matcher.group(2)));
		record.tsf = Long.valueOf(matcher.group(3));
		record.seq = Integer.valueOf(matcher.group(4));
		record.rssi = Byte.valueOf(matcher.group(5));

		return record;
	}

	private final BufferedReader mBufferedReader;

	private static final Pattern PROBE_REQUEST_RECORD_LINE_PATTERN = Pattern
			.compile("ra=((?:[0-9a-fA-F]{2}:){5}[0-9a-fA-F]{2})"
					+ " ta=((?:[0-9a-fA-F]{2}:){5}[0-9a-fA-F]{2})"
					+ " tsf=([1-9][0-9]*|0)" + " seq=([1-9][0-9]*|0)"
					+ " rssi=(-[1-9][0-9]*)");

	@Override
	public void close() throws IOException {
		mBufferedReader.close();
	}

}
