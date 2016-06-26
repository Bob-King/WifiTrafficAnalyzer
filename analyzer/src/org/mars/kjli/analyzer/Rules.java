package org.mars.kjli.analyzer;

import java.text.ParseException;
import java.util.ArrayList;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.w3c.dom.Text;

public class Rules {
	
	public void clear() {
		mRules.clear();
	}

	public void load(Document xml) {
		Element root = xml.getDocumentElement();
		NodeList children = root.getChildNodes();
		for (int i = 0; i != children.getLength(); ++i) {
			Node child = children.item(i);
			if (child instanceof Element) {
				loadRule((Element) child);
			}
		}
	}

	public void save(Document xml) {
		assert (xml.getDocumentElement() == null);

		Element root = xml.createElement(TAG_RULES);
		for (Rule rule : mRules) {
			root.appendChild(saveRule(xml, rule));
		}
	}

	public Rule matchRule(long ta, long tsf) {
		for (Rule r : mRules) {
			if (r.ta != ta) {
				continue;
			}

			if (tsf >= r.tsfb && tsf < r.tsfe) {
				return r;
			}
		}

		return null;
	}

	private Node saveRule(Document xml, Rule rule) {
		Element xRule = xml.createElement(TAG_RULE);

		xRule.appendChild(xml.createElement(TAG_TA)).appendChild(
				xml.createTextNode(Utils.long2MacAddress(rule.ta)));

		xRule.appendChild(xml.createElement(TAG_TSFB)).appendChild(
				xml.createTextNode(Utils.formatTsf(rule.tsfb)));

		xRule.appendChild(xml.createElement(TAG_TSFE)).appendChild(
				xml.createTextNode(Utils.formatTsf(rule.tsfe)));

		xRule.appendChild(xml.createElement(TAG_LOCATION)).appendChild(
				xml.createTextNode(rule.loc));

		xRule.appendChild(xml.createElement(TAG_TX_POWER)).appendChild(
				xml.createTextNode(Integer.toString(rule.tp)));

		return xRule;
	}

	private void loadRule(Element rule) {
		try {
			NodeList children = rule.getChildNodes();
			Rule r = new Rule();
			int flag = RF_NONE;
			for (int i = 0; i != children.getLength(); ++i) {
				Node child = children.item(i);
				if (child instanceof Element) {
					Element ele = (Element) child;
					String value = ((Text) ele.getFirstChild()).getData()
							.trim();
					switch (ele.getTagName()) {
					case TAG_TA:
						r.ta = Utils.macAddress2Long(value);
						flag |= RF_TA;
						break;

					case TAG_TSFB:
						r.tsfb = Utils.parseTsf(value);
						flag |= RF_TSFB;
						break;

					case TAG_TSFE:
						r.tsfe = Utils.parseTsf(value);
						flag |= RF_TSFE;
						break;

					case TAG_LOCATION:
						r.loc = value;
						flag |= RF_LOC;
						break;

					case TAG_TX_POWER:
						r.tp = Integer.valueOf(value);
						flag |= RF_TP;
					}
				}
			}
			if (flag == RF_ALL) {
				MyLogger.fine("Add rule: " + r);
				mRules.add(r);
			}
		} catch (ParseException e) {
			MyLogger.loge("Failed to load rule: " + rule.toString(), e);
		}
	}

	/*
	 * private static class Left { long ta; long tsfb; long tsfe;
	 * 
	 * @Override public boolean equals(Object obj) { if (this == obj) { return
	 * true; }
	 * 
	 * if (obj instanceof Left) { Left o = (Left) obj; return ta == o.ta && tsfb
	 * == o.tsfb && tsfe == o.tsfe; }
	 * 
	 * return false; }
	 * 
	 * @Override public int hashCode() { int r = (int) ta; r ^= (int) (ta >>
	 * 32); r ^= (int) (tsfe ^ tsfe);
	 * 
	 * return r; } }
	 * 
	 * private static class Right { String loc; int tp; }
	 */

	public static class Rule {
		public long ta;
		public long tsfb;
		public long tsfe;
		public String loc;
		public int tp;
		@Override
		public String toString() {
			StringBuilder sb = new StringBuilder(80);
			sb.append("tx_sta=");
			sb.append(Utils.long2MacAddress(ta));
			sb.append(" start_time=");
			sb.append(Utils.formatTsf(tsfb));
			sb.append(" end_time=");
			sb.append(Utils.formatTsf(tsfe));
			sb.append(" location=");
			sb.append(loc);
			sb.append(" tx_power=");
			sb.append(tp);
			return sb.toString();
		}
		
		
	}

	ArrayList<Rule> mRules = new ArrayList<>();

	private static final String TAG_RULES = "rules";
	private static final String TAG_RULE = "rule";

	private static final String TAG_TSFB = "start_time";
	private static final String TAG_TSFE = "end_time";
	private static final String TAG_TA = "tx_sta";
	private static final String TAG_LOCATION = "location";
	private static final String TAG_TX_POWER = "tx_power";

	private static final int RF_NONE = 0x0;
	private static final int RF_TA = 0x1;
	private static final int RF_TSFB = 0x2;
	private static final int RF_TSFE = 0x4;
	private static final int RF_LOC = 0x8;
	private static final int RF_TP = 0x10;
	private static final int RF_ALL = RF_TA | RF_TSFB | RF_TSFE | RF_LOC
			| RF_TP;
}
