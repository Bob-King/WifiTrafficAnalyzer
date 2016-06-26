package org.mars.kjli.analyzer;

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

		fileMenu.add("Transform wta log file").addActionListener(
				new ActionListener() {

					@Override
					public void actionPerformed(ActionEvent event) {
						JFileChooser chooser = new JFileChooser();
						chooser.setFileSelectionMode(JFileChooser.FILES_AND_DIRECTORIES);
						chooser.setFileFilter(new FileNameExtensionFilter(
								"Xml files", "log"));
						int ret = chooser.showOpenDialog(MainApplet.this);
						if (ret == JFileChooser.APPROVE_OPTION) {
							File file = chooser.getSelectedFile();
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
				int ret = chooser.showOpenDialog(MainApplet.this);
				if (ret == JFileChooser.APPROVE_OPTION) {
					File file = chooser.getSelectedFile();
					MyLogger.info("Try to load directory: "
							+ file.getAbsolutePath());
					try {
						mTrainer.train(file);
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
				int ret = chooser.showOpenDialog(MainApplet.this);
				if (ret == JFileChooser.APPROVE_OPTION) {
					File file = chooser.getSelectedFile();
					MyLogger.info("Try to load database "
							+ file.getAbsolutePath());
					try {
						Database.storeToXmlFile(mDatabase, file);
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
				int ret = chooser.showSaveDialog(MainApplet.this);
				if (ret == JFileChooser.APPROVE_OPTION) {
					File file = chooser.getSelectedFile();
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
				// TODO Auto-generated method stub

			}

		});

		menubar.add(fileMenu);
		setJMenuBar(menubar);

	}

	private void doInitPanel() {
		mTextArea = new JTextArea();
		mTextArea.setEnabled(false);
		add(mTextArea);
	}
	
	private Trainer mTrainer = Trainer.instantiate();
	private Database mDatabase;
	
	private JTextArea mTextArea;

	/**
	 * 
	 */
	private static final long serialVersionUID = 6900150921469924288L;

}
