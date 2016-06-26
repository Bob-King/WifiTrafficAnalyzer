package org.mars.kjli.analyzer;

import java.io.File;
import java.io.FileInputStream;
import java.nio.file.DirectoryStream;
import java.nio.file.Files;
import java.nio.file.Path;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

import org.mars.kjli.analyzer.Rules.Rule;
import org.w3c.dom.Document;

public class Trainer {	

	private interface MatchedRecordHandler {
		void newMatchedRecord(ProbeRequestRecord record, Rule rule);
	}

	private Trainer() {
	}


	public static Trainer instantiate() {
		return new Trainer();
	}

	public void train(File file) throws Exception {

		if (!file.isDirectory()) {
			throw new IllegalArgumentException("Directory must be specified!");
		}

		Path path = file.toPath();
		path = path.getFileSystem().getPath(path.toString(), "rules.xml");

		DocumentBuilder builder = DocumentBuilderFactory.newInstance()
				.newDocumentBuilder();
		Document doc = builder.parse(path.toFile());
		mRules.clear();
		mRules.load(doc);

		try (DirectoryStream<Path> entries = Files.newDirectoryStream(
				file.toPath(), "*.log")) {

			for (Path entry : entries) {
				if (Files.isDirectory(entry)) {
					continue;
				}
				try (ProbeRequestReader reader = new ProbeRequestReader(
						new FileInputStream(entry.toFile()))) {
					while (true) {
						try {
							ProbeRequestRecord record = reader
									.readProbeRequestRecord();

							if (record == null) {
								break;
							}

							Rule rule = mRules.matchRule(record.ta, record.tsf);
							if (rule != null) {
								mHandler.newMatchedRecord(record, rule);
							} else {
								MyLogger.fine("No matched rule for probe request {"
										+ record.toString() + ")");
							}
						} catch (Exception e) {
							MyLogger.loge("Failed to read probe request", e);
						}
					}
				}
			}

		}

	}
	
	private class RssiDatabaseHandler implements MatchedRecordHandler {
		
		public RssiDatabaseHandler() {
			mDatabase = new Database.RssiDatabase();
		}

		@Override
		public void newMatchedRecord(ProbeRequestRecord record, Rule rule) {
			mDatabase.addRssiRecord(rule.loc, new RssiRecord[] {
					new RssiRecord(record.ra, record.rssi)
			}, rule.tp);			
		}
		
	}

	private Rules mRules = new Rules();
	private Database mDatabase;
	private MatchedRecordHandler mHandler = new RssiDatabaseHandler();

}
