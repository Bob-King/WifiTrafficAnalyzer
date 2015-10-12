package org.mars.kjli.analyzer;

import java.applet.Applet;
import java.awt.EventQueue;

import javax.swing.JLabel;

public class MainApplet extends Applet {

	@Override
	public void init() {
		EventQueue.invokeLater(new Runnable() {

			@Override
			public void run() {
				JLabel label = new JLabel("Hello, world!");
				add(label);
			}
			
		});
	}

	/**
	 * 
	 */
	private static final long serialVersionUID = 6900150921469924288L;

}
