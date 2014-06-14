package com.mamo_dev.backupR;

import com.mamo_dev.backupR.gui.Gui;
import java.awt.EventQueue;
import java.awt.Frame;
import java.awt.event.ComponentEvent;
import java.awt.event.ComponentListener;
import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.HashMap;
import javax.imageio.ImageIO;
import javax.swing.JFrame;
import javax.swing.JOptionPane;
import javax.swing.JProgressBar;
import javax.swing.UIManager;
import javax.swing.UnsupportedLookAndFeelException;

public class Main {
	
	public static final String VERSION = "1.0";
	
	private static Properties properties;

	public static void main(String[] args) throws IOException {
		properties = new Properties(new File(new File(Main.class.getProtectionDomain().getCodeSource().getLocation().getPath()).getParentFile(), "settings.properties"));

		HashMap<String, Object> defaultValues = new HashMap<>();
		for (PropertyEnum preference : PropertyEnum.values()) {
			defaultValues.put(preference.toString(), preference.defaultValue());
		}
		properties.setDefaultValues(defaultValues);
		properties.setAutoSaveEnabled(true);

		EventQueue.invokeLater(new Runnable() {
			@Override
			public void run() {
				try {
					UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
				} catch (ClassNotFoundException | InstantiationException | IllegalAccessException | UnsupportedLookAndFeelException ex) {
				}

				final JFrame window = new JFrame("BackupR v. " + VERSION);
				try {
					window.setIconImage(ImageIO.read(Main.class.getResource("icon.png")));
				} catch (IOException ex) {
				}
				window.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
				window.setContentPane(new Gui());
				window.pack();
				window.setMinimumSize(window.getSize());
				window.setSize(properties.getInt(PropertyEnum.WINDOW_WIDTH.toString()), properties.getInt(PropertyEnum.WINDOW_HEIGHT.toString()));
				window.setVisible(true);
				window.setLocation(properties.getInt(PropertyEnum.WINDOW_X.toString()), properties.getInt(PropertyEnum.WINDOW_Y.toString()));
				if (properties.getBoolean(PropertyEnum.WINDOW_MAXIMIZED.toString())) {
					window.setExtendedState(Frame.MAXIMIZED_BOTH);
				}
				window.addComponentListener(new ComponentListener() {
					@Override
					public void componentResized(ComponentEvent e) {
						try {
							if ((((JFrame) e.getComponent()).getExtendedState() & Frame.MAXIMIZED_BOTH) != Frame.MAXIMIZED_BOTH) {
								properties.set(PropertyEnum.WINDOW_WIDTH.toString(), (int) e.getComponent().getSize().getWidth());
								properties.set(PropertyEnum.WINDOW_HEIGHT.toString(), (int) e.getComponent().getSize().getHeight());
								properties.set(PropertyEnum.WINDOW_MAXIMIZED.toString(), false);
							} else {
								properties.set(PropertyEnum.WINDOW_MAXIMIZED.toString(), true);
							}
						} catch (IOException ex) {
						}
					}

					@Override
					public void componentMoved(ComponentEvent e) {
						try {
							if ((((JFrame) e.getComponent()).getExtendedState() & Frame.MAXIMIZED_BOTH) != Frame.MAXIMIZED_BOTH) {
								properties.set(PropertyEnum.WINDOW_X.toString(), (int) e.getComponent().getLocation().getX());
								properties.set(PropertyEnum.WINDOW_Y.toString(), (int) e.getComponent().getLocation().getY());
								properties.set(PropertyEnum.WINDOW_MAXIMIZED.toString(), false);
							} else {
								properties.set(PropertyEnum.WINDOW_MAXIMIZED.toString(), true);
							}
						} catch (IOException ex) {
						}
					}

					@Override
					public void componentShown(ComponentEvent e) {
					}

					@Override
					public void componentHidden(ComponentEvent e) {
					}
				});

				new Thread(new Runnable() {
					@Override
					public void run() {
						try {
							Updater updater = new Updater(new URL("http://mamo-dev.com/programs/backupr/versions.xml"), 201406140902L);
							updater.checkForUpdates();
							JProgressBar statusProgressBar = ((Gui) window.getContentPane()).getStatusProgressBar();
							statusProgressBar.setIndeterminate(false);
							if (updater.areUpdatesAvaiable()) {
								if (JOptionPane.showConfirmDialog(window, "An update has been found.\nWould you like to update?", "BackupR", JOptionPane.YES_NO_OPTION) == 0) {
									statusProgressBar.setIndeterminate(true);
									updater.update();
									statusProgressBar.setIndeterminate(false);
									String jrePath = System.getProperty("java.home") + File.separator + "bin" + File.separator;
									if (System.getProperty("os.name").toLowerCase().contains("windows")) {
										jrePath += "java.exe";
									} else {
										jrePath += "java";
									}
									Runtime.getRuntime().exec(new String[]{jrePath, "-jar", new File(Main.class.getProtectionDomain().getCodeSource().getLocation().getPath()).getPath()});
									System.exit(0);
								}
							}
						} catch (MalformedURLException ex) {
						} catch (IOException ex) {
						}
					}
				}).start();
			}
		});
	}

	public static Properties getProperties() {
		return properties;
	}
}
