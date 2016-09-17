package org.mars.kjli.analyzer;

import java.awt.BorderLayout;
import java.awt.EventQueue;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import javax.swing.JApplet;
import javax.swing.JFileChooser;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JTextArea;
import javax.swing.filechooser.FileNameExtensionFilter;

public class MainApplet extends JApplet {

	@Override
	public void init() {
		EventQueue.invokeLater(new Runnable() {

			@Override
			public void run() {
				doInit();
			}

		});
	}

	private void doInit() {
		doInitMenubar();
		doInitPanel();
	}

	private void doInitMenubar() {
		JMenuBar menubar = new JMenuBar();
		JMenu fileMenu = new JMenu("File");
		
		fileMenu.add("Reset").addActionListener(
				new ActionListener() {

					@Override
					public void actionPerformed(ActionEvent arg0) {
						mTrainer = Trainer.instantiate();						
					}
					
				});

		fileMenu.add("Transform wta log file").addActionListener(
				new ActionListener() {

					@Override
					public void actionPerformed(ActionEvent event) {
						JFileChooser chooser = new JFileChooser();
						chooser.setFileSelectionMode(JFileChooser.FILES_AND_DIRECTORIES);
						chooser.setFileFilter(new FileNameExtensionFilter(
								"Xml files", "log"));
						if (mCurrentDirectory != null) {
							chooser.setCurrentDirectory(mCurrentDirectory);
						}
						int ret = chooser.showOpenDialog(MainApplet.this);
						if (ret == JFileChooser.APPROVE_OPTION) {
							File file = chooser.getSelectedFile();
							setCurrentDirectory(file);
							MyLogger.info("Try to transform log file "
									+ file.getAbsolutePath());
							try {
								new Transformer().transform(file);
							} catch (Exception e) {
								MyLogger.loge(
										"Failed to transform wta log file!", e);
							}
						}
					}

				});

		fileMenu.add("Train").addActionListener(new ActionListener() {

			@Override
			public void actionPerformed(ActionEvent event) {
				JFileChooser chooser = new JFileChooser();
				chooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
				if (mCurrentDirectory != null) {
					chooser.setCurrentDirectory(mCurrentDirectory);
				}
				int ret = chooser.showOpenDialog(MainApplet.this);
				if (ret == JFileChooser.APPROVE_OPTION) {
					File file = chooser.getSelectedFile();
					setCurrentDirectory(file);
					MyLogger.info("Try to load directory: "
							+ file.getAbsolutePath());
					try {
						mDatabase = mTrainer.train(file);
					} catch (Exception e) {
						MyLogger.loge("Failed in trainning data!", e);
					}
				}
			}

		});

		fileMenu.add("Load database").addActionListener(new ActionListener() {

			@Override
			public void actionPerformed(ActionEvent event) {
				JFileChooser chooser = new JFileChooser();
				chooser.setFileSelectionMode(JFileChooser.FILES_AND_DIRECTORIES);
				chooser.setFileFilter(new FileNameExtensionFilter("Xml files",
						"xml"));
				if (mCurrentDirectory != null) {
					chooser.setCurrentDirectory(mCurrentDirectory);
				}
				int ret = chooser.showOpenDialog(MainApplet.this);
				if (ret == JFileChooser.APPROVE_OPTION) {
					File file = chooser.getSelectedFile();
					setCurrentDirectory(file);
					MyLogger.info("Try to load database "
							+ file.getAbsolutePath());
					try {
						mDatabase = Database.loadFromXmlFile(file);
					} catch (Exception e) {
						MyLogger.loge("Failed to load database!", e);
					}
				}
			}

		});

		fileMenu.add("Save database").addActionListener(new ActionListener() {

			@Override
			public void actionPerformed(ActionEvent event) {

				JFileChooser chooser = new JFileChooser();
				chooser.setFileSelectionMode(JFileChooser.FILES_AND_DIRECTORIES);
				chooser.setFileFilter(new FileNameExtensionFilter("Xml files",
						"xml"));
				if (mCurrentDirectory != null) {
					chooser.setCurrentDirectory(mCurrentDirectory);
				}
				int ret = chooser.showSaveDialog(MainApplet.this);
				if (ret == JFileChooser.APPROVE_OPTION) {
					File file = chooser.getSelectedFile();
					setCurrentDirectory(file);
					MyLogger.info("Try to save database "
							+ file.getAbsolutePath());
					try {
						Database.storeToXmlFile(mDatabase, file);
					} catch (Exception e) {
						MyLogger.loge("Failed to save database!", e);
					}
				}

			}

		});
		
		fileMenu.add("Validate").addActionListener(new ActionListener() {

			@Override
			public void actionPerformed(ActionEvent arg0) {
				mTextArea.append("\nValidate");				
			}
			
		});

		fileMenu.add("Analyze").addActionListener(new ActionListener() {

			@Override
			public void actionPerformed(ActionEvent event) {
				JFileChooser chooser = new JFileChooser();
				chooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
				if (mCurrentDirectory != null) {
					chooser.setCurrentDirectory(mCurrentDirectory);
				}
				int ret = chooser.showOpenDialog(MainApplet.this);
				if (ret == JFileChooser.APPROVE_OPTION) {
					File file = chooser.getSelectedFile();
					setCurrentDirectory(file);
					MyLogger.info("Try to load directory: "
							+ file.getAbsolutePath());
					try {
						mAnalyzer = new Analyzer(mDatabase);
						mAnalyzer.load(file);
						mAnalyzer.analyze();
					} catch (Exception e) {
						MyLogger.loge("Failed in analyze data!", e);
					}
				}

			}

		});

		menubar.add(fileMenu);
		setJMenuBar(menubar);

	}
	
	private void setCurrentDirectory(File dir) {
		if (dir.isDirectory()) {
			mCurrentDirectory = dir;
		}
	}

	private void doInitPanel() {
		mTextArea = new JTextArea();
		mTextArea.setText("ABC");
		mTextArea.setEnabled(false);
		add(mTextArea, BorderLayout.SOUTH);
	}
	
	private Trainer mTrainer = Trainer.instantiate();
	private Database mDatabase;
	private Analyzer mAnalyzer;
	private File mCurrentDirectory;
	
	private JTextArea mTextArea;

	/**
	 * 
	 */
	private static final long serialVersionUID = 6900150921469924288L;

}
