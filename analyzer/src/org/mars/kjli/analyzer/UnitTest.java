package org.mars.kjli.analyzer;

import static org.junit.Assert.*;

import org.junit.Test;

public class UnitTest {
	
	@Test
	public void testMacLongTransform() {
		final String MAC = "08:00:2A:3B:4C:5E";
		final long L = 0x00468A0E1357L;
		
		for (char ch = '0'; ch <= '9'; ++ch) {
			assertEquals(ch, Utils.toHex((byte)(ch - '0')));
		}
		for (char ch = 'A'; ch <= 'F'; ++ch) {
			assertEquals(ch, Utils.toHex((byte)(ch - 'A' + 10)));
		}
		for (char ch = 'a'; ch <= 'f'; ++ch) {
			assertEquals(ch - 'a' + 'A', Utils.toHex((byte)(ch - 'a' + 10)));
		}
		assertEquals(MAC, Utils.long2MacAddress(Utils.macAddress2Long(MAC)).toUpperCase());
		assertEquals(L, Utils.macAddress2Long(Utils.long2MacAddress(L)));
	}

}
