package org.mars.kjli.analyzer;

import java.io.File;
import java.io.FileOutputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

public abstract class Database {

	public static class DatabaseException extends Exception {

		private static final long serialVersionUID = 2534765205460886619L;

	}

	public static class NoRecordException extends DatabaseException {

		private static final long serialVersionUID = 1351551664743588204L;

	}

	private static final Class<?>[] DATABASE_CLASSES = { RssiDatabase.class, RssiVectorDatabase.class };

	public static Database loadFromXmlFile(File file) throws Exception {
		DocumentBuilder builder = DocumentBuilderFactory.newInstance().newDocumentBuilder();
		Document xml = builder.parse(file);
		for (Class<?> cls : DATABASE_CLASSES) {
			try {
				Database db = (Database) cls.newInstance();
				if (db.getXmlLoader().load(xml)) {
					return db;
				}
			} catch (Exception e) {
				MyLogger.loge("Failed to construct database instance!", e);
			}
		}

		return null;
	}

	public static void storeToXmlFile(Database database, File file) throws Exception {
		DocumentBuilder builder = DocumentBuilderFactory.newInstance().newDocumentBuilder();
		Document xml = builder.newDocument();
		database.getXmlStorer().store(xml);

		Transformer t = TransformerFactory.newInstance().newTransformer();
		t.transform(new DOMSource(xml), new StreamResult(new FileOutputStream(file)));
	}

	public interface XmlLoader {
		boolean load(Document xml) throws Exception;
	}

	public interface XmlStorer {
		void store(Document xml) throws Exception;
	}

	public abstract XmlLoader getXmlLoader();

	public abstract XmlStorer getXmlStorer();

	public abstract void addRssiRecord(String loc, RssiRecord[] records, int tp);

	public abstract String queryLocation(RssiRecord[] q);

	public abstract void ellipse();

	public static class RssiDatabase extends Database {

		public RssiDatabase() {
			mData = new TreeMap<>();
		}

		/*
		 * public void load(File file) throws Exception { DocumentBuilder
		 * builder = DocumentBuilderFactory.newInstance() .newDocumentBuilder();
		 * Document xml = builder.parse(file); fromXml(xml); }
		 * 
		 * public void save(File file) throws Exception { DocumentBuilder
		 * builder = DocumentBuilderFactory.newInstance() .newDocumentBuilder();
		 * Document xml = builder.newDocument(); toXml(xml);
		 * 
		 * Transformer t = TransformerFactory.newInstance().newTransformer();
		 * t.transform(new DOMSource(xml), new StreamResult( new
		 * FileOutputStream(file))); }
		 */

		private void toXml(Document doc) {
			Node root = doc.appendChild(doc.createElement(TAG_DATABASE));

			for (String location : mData.keySet()) {

				Map<Long, ArrayList<TpRssiRelation>> raRecord = mData.get(location);

				for (long ra : raRecord.keySet()) {

					for (TpRssiRelation relation : raRecord.get(ra)) {

						Node xRecord = root.appendChild(doc.createElement(TAG_RECORD));

						xRecord.appendChild(doc.createElement(TAG_LOCATION)).appendChild(doc.createTextNode(location));

						xRecord.appendChild(doc.createElement(TAG_RA))
								.appendChild(doc.createTextNode(Utils.long2MacAddress(ra)));

						xRecord.appendChild(doc.createElement(TAG_TX_POWER))
								.appendChild(doc.createTextNode(Integer.toString(relation.txPower)));

						xRecord.appendChild(doc.createElement(TAG_CENTRE_RSSI))
								.appendChild(doc.createTextNode(Byte.toString(relation.centreRssi)));
						
						xRecord.appendChild(doc.createElement(TAG_RSSI_RADIUS))
								.appendChild(doc.createTextNode(Byte.toString(relation.rssiRadius)));

						for (int i = 0; i != relation.rssiSeenTable.length; ++i) {
							if (relation.rssiSeenTable[i] < TpRssiRelation.RSSI_SEEN_TIMES_THRESHOLD) {
								continue;
							}

							Node xRssiItem = xRecord.appendChild(doc.createElement(TAG_RSSI_ITEM));

							xRssiItem.appendChild(doc.createElement(TAG_RSSI))
									.appendChild(doc.createTextNode(Byte.toString(rssiArrayIndex2rssi(i))));

							xRssiItem.appendChild(doc.createElement(TAG_SEEN_TIMES))
									.appendChild(doc.createTextNode(Short.toString(relation.rssiSeenTable[i])));
						}
					}
				}
			}
		}

		private void fromXml(Document doc) {
			Element root = doc.getDocumentElement();

			NodeList xRootChildren = root.getChildNodes();

			for (int i = 0; i != xRootChildren.getLength(); ++i) {
				Node node = xRootChildren.item(i);
				if (node instanceof Element) {
					Element xRecord = (Element) node;

					NodeList xLocationElements = xRecord.getElementsByTagName(TAG_LOCATION);
					if (xLocationElements.getLength() != 1) {
						continue;
					}

					String location = ((Element) xLocationElements.item(0)).getTextContent();

					NodeList xRaElements = xRecord.getElementsByTagName(TAG_RA);
					if (xRaElements.getLength() != 1) {
						continue;
					}

					long ra = Utils.macAddress2Long(((Element) xRaElements.item(0)).getTextContent());

					NodeList xTxPowerElements = xRecord.getElementsByTagName(TAG_TX_POWER);
					if (xTxPowerElements.getLength() != 1) {
						continue;
					}

					int tp = Integer.valueOf(((Element) xTxPowerElements.item(0)).getTextContent());

					if (!mData.containsKey(location)) {
						mData.put(location, new TreeMap<Long, ArrayList<TpRssiRelation>>());
					}

					Map<Long, ArrayList<TpRssiRelation>> m = mData.get(location);

					if (!m.containsKey(ra)) {
						m.put(ra, new ArrayList<TpRssiRelation>(N_AP));
					}

					List<TpRssiRelation> relations = m.get(ra);

					for (TpRssiRelation tr : relations) {
						if (tr.txPower == INVALID_TX_POWER) {
							tr.txPower = tp;
							
							NodeList xCentreRssiElements = xRecord.getElementsByTagName(TAG_CENTRE_RSSI);
							if (xCentreRssiElements.getLength() == 1) {
								tr.centreRssi = Byte.valueOf(((Element) xCentreRssiElements.item(0)).getTextContent());
							}
							
							NodeList xRssiRadiusElements = xRecord.getElementsByTagName(TAG_RSSI_RADIUS);
							if (xRssiRadiusElements.getLength() == 1) {
								tr.rssiRadius = Byte.valueOf(((Element) xRssiRadiusElements.item(0)).getTextContent());
							}

							NodeList xRssiItemElements = xRecord.getElementsByTagName(TAG_RSSI_ITEM);
							for (int j = 0; j != xRssiItemElements.getLength(); ++j) {
								Element xRssiItem = (Element) xRssiItemElements.item(j);
								NodeList xRssiElements = xRssiItem.getElementsByTagName(TAG_RSSI);
								if (xRssiItemElements.getLength() != 1) {
									continue;
								}
								byte rssi = Byte.valueOf(((Element) xRssiElements.item(0)).getTextContent());

								NodeList xSeenTimesElements = xRssiItem.getElementsByTagName(TAG_SEEN_TIMES);
								if (xSeenTimesElements.getLength() != 1) {
									continue;
								}
								byte seenTimes = Byte.valueOf(((Element) xSeenTimesElements.item(0)).getTextContent());

								tr.rssiSeenTable[rssi2RssiArrayIndex(rssi)] = seenTimes;
								tr.totalRssiSeen += seenTimes;

								if (rssi < tr.minRssi) {
									tr.minRssi = rssi;
								} else if (rssi > tr.maxRssi) {
									tr.maxRssi = rssi;
								}
							}

							break;
						}
					}
				}
			}
		}

		@Override
		public void addRssiRecord(String loc, RssiRecord[] records, int tp) {
			if (records == null || records.length == 0) {
				throw new IllegalArgumentException("rssi record can't be null!");
			}

			for (RssiRecord r : records) {
				if (r.rssi < Utils.MIN_VALID_RSSI || r.rssi > Utils.MAX_VALID_RSSI) {
					throw new IllegalArgumentException("rssi " + Byte.toString(r.rssi) + " is out of range!");
				}
			}

			if (!mData.containsKey(loc)) {
				mData.put(loc, new TreeMap<Long, ArrayList<TpRssiRelation>>());
			}

			Map<Long, ArrayList<TpRssiRelation>> m = mData.get(loc);

			for (RssiRecord r : records) {

				if (!m.containsKey(r.ra)) {
					m.put(r.ra, new ArrayList<TpRssiRelation>(N_AP));
				}

				List<TpRssiRelation> relations = m.get(r.ra);

				boolean found = false;
				final int index = rssi2RssiArrayIndex(r.rssi);

				for (TpRssiRelation tr : relations) {
					if (tr.txPower == tp) {
						found = true;
						if (tr.rssiSeenTable[index] < Short.MAX_VALUE) {
							++tr.rssiSeenTable[index];
							++tr.totalRssiSeen;

							if (r.rssi < tr.minRssi) {
								tr.minRssi = r.rssi;
							} else if (r.rssi > tr.maxRssi) {
								tr.maxRssi = r.rssi;
							}
						}
						break;
					}
				}

				if (!found) {
					relations.add(new TpRssiRelation());
					TpRssiRelation tr = relations.get(relations.size() - 1);
					tr.txPower = tp;
					if (tr.rssiSeenTable[index] < Short.MAX_VALUE) {
						++tr.rssiSeenTable[index];
						++tr.totalRssiSeen;

						if (r.rssi < tr.minRssi) {
							tr.minRssi = r.rssi;
						} else if (r.rssi > tr.maxRssi) {
							tr.maxRssi = r.rssi;
						}
					}
				}
			}
		}

		@Override
		public String queryLocation(RssiRecord[] q) {
			if (q.length == 0) {
				return "";
			}

			Map<String, ArrayList<Integer>> poss = new TreeMap<>();

			for (String loc : mData.keySet()) {

				poss.put(loc, new ArrayList<Integer>());
				ArrayList<Integer> factors = poss.get(loc);

				Map<Long, ArrayList<TpRssiRelation>> m = mData.get(loc);
				for (RssiRecord r : q) {
					ArrayList<TpRssiRelation> trs = m.get(r.ra);
					if (trs == null || trs.size() < 1) {
						factors.add(F_ACTOR_MISS);
						continue;
					}

					// FIXME: only check the default txpower
					factors.add(calcPossible(trs.get(0), r.rssi));
				}
			}

			if (poss.isEmpty()) {
				return "";
			}

			String loc = "";
			int pf = 1;

			for (String l : poss.keySet()) {
				int tpf = Utils.multiply(poss.get(l));
				if (loc == "" || tpf > pf) {
					loc = l;
					pf = tpf;
				}
			}

			return loc;
		}

		private static int calcPossible(TpRssiRelation tr, byte rssi) {

			if (rssi < tr.minRssi || rssi > tr.maxRssi) {
				return F_ACTOR_MISS;
			}

			final int index = rssi2RssiArrayIndex(rssi);
			
			int seen = tr.rssiSeenTable[index] * 2;
			if (index >= 1) {
				seen += tr.rssiSeenTable[index - 1];
			}
			if (index < tr.rssiSeenTable.length - 1) {
				seen += tr.rssiSeenTable[index + 1];
			}
			
			return 100 * seen / tr.totalRssiSeen;
		}

		private static class TpRssiRelation {
			public int txPower = INVALID_TX_POWER;
			public byte centreRssi = 0;
			public byte rssiRadius = 0;
			public int totalRssiSeen = 0;
			public byte minRssi = Utils.MAX_VALID_RSSI;
			public byte maxRssi = Utils.MIN_VALID_RSSI;
			public short[] rssiSeenTable = new short[N_RSSI_ARRAY];

			private static final int ELLIPSE_COVER_PERCENTAGE = 75;

			public static final int RSSI_SEEN_TIMES_THRESHOLD = 10;

			public void ellipse() {
				int ci = 0;
				totalRssiSeen = 0;
				for (int i = 0; i != rssiSeenTable.length; ++i) {
					if (rssiSeenTable[i] < RSSI_SEEN_TIMES_THRESHOLD) {
						rssiSeenTable[i] = 0;
						continue;
					}
					if (rssiSeenTable[ci] < rssiSeenTable[i]) {
						ci = i;
					}
					totalRssiSeen += rssiSeenTable[i]; 
				}

				centreRssi = rssiArrayIndex2rssi(ci);
				
				if (rssiSeenTable[ci] < RSSI_SEEN_TIMES_THRESHOLD) {
					rssiRadius = 0;
					return;
				}

				rssiRadius = 0;
				short seen = rssiSeenTable[ci];
				while (seen * 100 < totalRssiSeen * ELLIPSE_COVER_PERCENTAGE) {
					++rssiRadius;
					if (ci - rssiRadius >= 0) {
						seen += rssiSeenTable[ci - rssiRadius];
					}
					if (ci + rssiRadius < rssiSeenTable.length) {
						seen += rssiSeenTable[ci + rssiRadius];
					}
				}
			}
		}

		private static int rssi2RssiArrayIndex(byte rssi) {
			return rssi - Utils.MIN_VALID_RSSI;
		}

		private static byte rssiArrayIndex2rssi(int index) {
			return (byte) (Utils.MIN_VALID_RSSI + index);
		}

		private Map<String, Map<Long, ArrayList<TpRssiRelation>>> mData;

		// the size of the array of different tx power
		private static final int N_AP = 4;

		private static final int N_RSSI_ARRAY = Utils.MAX_VALID_RSSI - Utils.MIN_VALID_RSSI + 1;

		private static final int INVALID_TX_POWER = -1;

		private static final String TAG_DATABASE = "database";

		private static final String TAG_RECORD = "record";

		private static final String TAG_LOCATION = "location";

		private static final String TAG_RA = "ra";

		private static final String TAG_TX_POWER = "tx_power";

		private static final String TAG_RSSI_ITEM = "rssi_item";

		private static final String TAG_RSSI = "rssi";

		private static final String TAG_SEEN_TIMES = "seen_times";

		private static final String TAG_CENTRE_RSSI = "centre_rssi";

		private static final String TAG_RSSI_RADIUS = "rssi_radius";

		private static final int F_ACTOR_MISS = 1;

		@Override
		public XmlLoader getXmlLoader() {
			return new XmlLoader() {

				@Override
				public boolean load(Document xml) throws Exception {
					fromXml(xml);
					return true;
				}

			};
		}

		@Override
		public XmlStorer getXmlStorer() {
			return new XmlStorer() {

				@Override
				public void store(Document xml) throws Exception {
					toXml(xml);
				}

			};
		}

		@Override
		public void ellipse() {
			for (String location : mData.keySet()) {

				Map<Long, ArrayList<TpRssiRelation>> raRecord = mData.get(location);

				for (long ra : raRecord.keySet()) {

					for (TpRssiRelation relation : raRecord.get(ra)) {

						relation.ellipse();
					}
				}
			}
		}

	}

	public static class RssiVectorDatabase extends Database {

		@Override
		public void addRssiRecord(String loc, RssiRecord[] records, int tp) {
			// TODO Auto-generated method stub

		}

		@Override
		public String queryLocation(RssiRecord[] q) {
			// TODO Auto-generated method stub
			return null;
		}

		@Override
		public XmlLoader getXmlLoader() {
			return new XmlLoader() {

				@Override
				public boolean load(Document xml) throws Exception {
					// TODO Auto-generated method stub
					return false;
				}

			};
		}

		@Override
		public XmlStorer getXmlStorer() {
			return new XmlStorer() {

				@Override
				public void store(Document xml) throws Exception {
					// TODO Auto-generated method stub

				}

			};
		}

		@Override
		public void ellipse() {
			// TODO Auto-generated method stub

		}

	}

}
