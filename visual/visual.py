#!/usr/bin/env python3

import re
import numpy as np
import matplotlib.pyplot as plt
import unittest

class Parser:
	'''Wta log parser'''

	def __init__(self):
		'''Open log file'''
		self._pattern = re.compile(
			r'^ra=((?:[0-9a-fA-F]{2}:){5}(?:[0-9a-fA-F]{2}))[ \t]+' +
			r'ta=((?:[0-9a-fA-F]{2}:){5}(?:[0-9a-fA-F]{2}))[ \t]+' +
			r'tsf=([0-9]+)[ \t]+' + 
			r'seq=([0-9]+)[ \t]+' +
			r'rssi=(-[0-9]+)$')

	def _match(self, line, ra, ta):
		match = self._pattern.match(line)
		if not match:
			return None
		if ra == match.group(1) and ta == match.group(2):
			return (match.group(1), match.group(2), int(match.group(3)),
				int(match.group(5)))

	def getRecords(self, path, ra, ta):
		f = open(path)
		records = []
		for line in self.f.lines():
			r = _match(line, ra, ta)
			if r:
				records.append(r)
		return records

class ParserTest(unittest.TestCase):
	'''Parser's unit test class'''

	def test_match(self):
		line = "ra=00:4b:69:6e:73:30 ta=c8:93:46:a3:8e:74 tsf=1473507516 seq=28769 rssi=-60"
		ra = "00:4b:69:6e:73:30"
		ta = "c8:93:46:a3:8e:74"
		tsf = 1473507516
		rssi = -60
		p = Parser()
		r = p._match(line, ra, ta)
		self.assertTrue(r is not None)
		self.assertEqual(r[0], ra)
		self.assertEqual(r[1], ta)
		self.assertEqual(r[2], tsf)
		self.assertEqual(r[3], rssi)

def main():
	pass

if __name__ == "__main__":
	main()
