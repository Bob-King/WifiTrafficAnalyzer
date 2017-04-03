package org.mars.kjli.analyzer;

import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.SortedMap;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.w3c.dom.Document;

public class Validator {
	
	private static class Result {
		public long ta;
		public long tsf;
		public String tLoc;
		public String vLoc;
		Rules.Rule rule;
		TrackRecords trs;
	}
	
	public void validate(Database database, File dir) {
		try {
			Path path = dir.toPath();
			path = path.getFileSystem().getPath(path.toString(), "rules.xml");

			DocumentBuilder builder = DocumentBuilderFactory.newInstance().newDocumentBuilder();
			Document doc = builder.parse(path.toFile());
			mRules = new Rules();
			mRules.load(doc);

			mAnalyzer = new Analyzer(database);
			mAnalyzer.load(dir);
			
			final Map<Long, SortedMap<Long, TrackRecords>> tracks = mAnalyzer.getTracks();
			
			mResults = new ArrayList<>();
			
			for (long ta : tracks.keySet()) {
				SortedMap<Long, TrackRecords>  sm = tracks.get(ta);
				for (long tsf : sm.keySet()) {
					if (tsf == 1474896189L) {
						MyLogger.info("1474781691\n");
					}
					Rules.Rule rule = mRules.matchRule(ta, tsf);
					if (rule == null) {
						continue;
					}
					
					TrackRecords trs = sm.get(tsf);
					String loc = mAnalyzer.queryLocation(trs);
					Result result = new Result();
					result.ta = ta;
					result.tsf = tsf;
					result.tLoc = loc;
					result.vLoc = rule.loc;
					result.rule = rule;
					result.trs = trs;
					
					mResults.add(result);					
				}
			}			
			
		} catch (Exception e) {
			MyLogger.loge("Failed to load " + dir.toString() + "!", e);
		}
	}
	
	public String dump() {
		StringBuilder sb = new StringBuilder();
		
		if (!mResults.isEmpty()) {
			int correct = 0;
			int undetermined = 0;
			for (Result r : mResults) {
				if (r.vLoc.equals(r.tLoc)) {
					++correct;
					continue;
				} else if (r.tLoc.equals("")) {
					++undetermined;
					continue;
				}
				sb.append(r.tsf);
				sb.append(", ");
				sb.append(Utils.long2MacAddress(r.ta));
				sb.append(", ");
				sb.append(Utils.formatTsf(r.tsf));
				sb.append(", ");
				sb.append(r.vLoc);
				sb.append(", ");
				sb.append(r.tLoc);
				sb.append('\n');
				sb.append(r.rule);
				sb.append('\n');
				sb.append(r.trs);
				sb.append('\n');
			}

			sb.append("Correct rate: ");
			sb.append(correct);
			sb.append('/');
			sb.append(mResults.size());
			sb.append('=');
			sb.append(correct * 100 / mResults.size());
			sb.append('\n');
			
			sb.append("Undetermined: ");
			sb.append(undetermined);
		}
		
		return sb.toString();
	}
	
	// private Database mDatabase;
	private Analyzer mAnalyzer;
	private Rules mRules;
	private List<Result> mResults;

}
