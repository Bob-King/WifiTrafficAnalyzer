package org.mars.kjli.analyzer;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

class Utils {

	public static long macAddress2Long(String mac) {
		Matcher matcher = MAC_ADDRESS_PATTERN.matcher(mac);
		if (!matcher.matches()) {
			throw new IllegalArgumentException();
		}

		long l = 0;

		l |= Long.valueOf(matcher.group(6), 16);
		l |= (Long.valueOf(matcher.group(5), 16)) << 8;
		l |= (Long.valueOf(matcher.group(4), 16)) << 16;
		l |= (Long.valueOf(matcher.group(3), 16)) << 24;
		l |= (Long.valueOf(matcher.group(2), 16)) << 32;
		l |= (Long.valueOf(matcher.group(1), 16)) << 40;

		return l;
	}

	public static String long2MacAddress(long l) {
		StringBuilder sb = new StringBuilder(17);
		sb.append(toHex((byte)((l >> 44) & 0xf)));
		sb.append(toHex((byte)((l >> 40) & 0xf)));
		sb.append(":");
		sb.append(toHex((byte)((l >> 36) & 0xf)));
		sb.append(toHex((byte)((l >> 32) & 0xf)));
		sb.append(":");
		sb.append(toHex((byte)((l >> 38) & 0xf)));
		sb.append(toHex((byte)((l >> 24) & 0xf)));
		sb.append(":");
		sb.append(toHex((byte)((l >> 20) & 0xf)));
		sb.append(toHex((byte)((l >> 16) & 0xf)));
		sb.append(":");
		sb.append(toHex((byte)((l >> 12) & 0xf)));
		sb.append(toHex((byte)((l >> 8) & 0xf)));
		sb.append(":");
		sb.append(toHex((byte)((l >> 4) & 0xf)));
		sb.append(toHex((byte)(l & 0xf)));
		return sb.toString();
	}
	
	public static int multiply(ArrayList<Integer> factors) {
		int r = 1;
		for (Integer i : factors) {
			r *= i;
		}
		
		return r;
	}
	
	public static char toHex(byte lowByte) {
		assert(lowByte < 16);
		if (lowByte < 10) {
			return (char)('0' + lowByte);
		} else if (lowByte < 16) {
			return (char)('A' + lowByte - 10);
		} else {
			return '?';
		}
	}

	public static String formatTsf(long tsf) {
		return SDF.format(new Date(tsf * 1000));
	}

	public static long parseTsf(String s) throws ParseException {
		return SDF.parse(s).getTime() / 1000;
	}
	
	public static final byte MAX_VALID_RSSI = -30;
	public static final byte MIN_VALID_RSSI = -99;	

	private static final SimpleDateFormat SDF = new SimpleDateFormat(
			"yyyy-MM-dd HH:mm:ss Z");

	private static final Pattern MAC_ADDRESS_PATTERN = Pattern
			.compile("([0-9a-fA-F]{2}):([0-9a-fA-F]{2}):([0-9a-fA-F]{2}):([0-9a-fA-F]{2}):([0-9a-fA-F]{2}):([0-9a-fA-F]{2})");

}
