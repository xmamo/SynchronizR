package mamo.backupR;

import mamo.backupR.gui.Gui;
import mamo.backupR.gui.LicenseGui;
import java.awt.EventQueue;
import java.awt.Frame;
import java.awt.event.ComponentEvent;
import java.awt.event.ComponentListener;
import java.awt.event.WindowEvent;
import java.awt.event.WindowListener;
import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.HashMap;
import java.util.Locale;
import javax.swing.JFrame;
import javax.swing.JOptionPane;
import javax.swing.ToolTipManager;
import javax.swing.UIManager;
import javax.swing.UnsupportedLookAndFeelException;

public class BackupR {
	
	public static final String VERSION = "1.7.2";
	public static final long releaseDate = 201408121413L;
	
	private static Settings settings;
	private static final Lang lang = new Lang("lang");
	
	public static void main(String[] args) throws IOException, URISyntaxException {
		for (String arg : args) {
			if (arg.toLowerCase().startsWith("locale:")) {
				getLang().changeLocale(new Locale(arg.substring("locale:".length())));
			}
		}
		
		settings = new Settings(new File(new File(BackupR.class.getProtectionDomain().getCodeSource().getLocation().toURI()).getParentFile(), "settings.properties"));
		
		HashMap<String, Object> defaultValues = new HashMap<>();
		for (SettingsEnum preference : SettingsEnum.values()) {
			defaultValues.put(preference.toString(), preference.defaultValue());
		}
		getSettings().setDefaultValues(defaultValues);
		
		EventQueue.invokeLater(new Runnable() {
			@Override
			public void run() {
				try {
					UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
				} catch (ClassNotFoundException | InstantiationException | IllegalAccessException | UnsupportedLookAndFeelException ex) {
				}
				ToolTipManager.sharedInstance().setDismissDelay(Integer.MAX_VALUE);
				
				final JFrame window = new JFrame("BackupR v. " + VERSION);
				window.setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);
				final Gui gui = new Gui();
				gui.setEverythingEnabled(false);
				gui.getProgressBar().setIndeterminate(true);
				window.setContentPane(gui);
				window.pack();
				window.setMinimumSize(window.getSize());
				window.setSize(getSettings().getInt(SettingsEnum.WINDOW_WIDTH.toString()), getSettings().getInt(SettingsEnum.WINDOW_HEIGHT.toString()));
				window.setVisible(true);
				if (getSettings().getInt(SettingsEnum.WINDOW_X.toString()) == Integer.MIN_VALUE || getSettings().getInt(SettingsEnum.WINDOW_Y.toString()) == Integer.MIN_VALUE) {
					window.setLocationRelativeTo(null);
				} else {
					window.setLocation(getSettings().getInt(SettingsEnum.WINDOW_X.toString()), getSettings().getInt(SettingsEnum.WINDOW_Y.toString()));
				}
				if (getSettings().getBoolean(SettingsEnum.WINDOW_MAXIMIZED.toString())) {
					window.setExtendedState(Frame.MAXIMIZED_BOTH);
				}
				
				if (!getSettings().getBoolean("acceptedLicense")) {
					gui.getProgressBar().setIndeterminate(false);
					LicenseGui licenseGui = new LicenseGui(window, true);
					licenseGui.setTitle("BackupR");
					licenseGui.setMinimumSize(licenseGui.getMinimumSize());
					licenseGui.setSize(640, 480);
					licenseGui.setLocationRelativeTo(null);
					licenseGui.setVisible(true);
					if (licenseGui.getReturnStatus() == 1) {
						try {
							getSettings().set(SettingsEnum.ACCEPTED_LICENSE.toString(), true);
						} catch (IOException ex) {
						}
					} else {
						System.exit(0);
					}
					gui.getProgressBar().setIndeterminate(true);
				}
				
				try {
					getSettings().save();
				} catch (IOException ex) {
				}
				getSettings().setAutoSaveEnabled(true);
				
				window.addComponentListener(new ComponentListener() {
					@Override
					public void componentResized(ComponentEvent e) {
						try {
							if ((((JFrame) e.getComponent()).getExtendedState() & Frame.MAXIMIZED_BOTH) != Frame.MAXIMIZED_BOTH) {
								getSettings().set(SettingsEnum.WINDOW_WIDTH.toString(), (int) e.getComponent().getSize().getWidth());
								getSettings().set(SettingsEnum.WINDOW_HEIGHT.toString(), (int) e.getComponent().getSize().getHeight());
								getSettings().set(SettingsEnum.WINDOW_MAXIMIZED.toString(), false);
							} else {
								getSettings().set(SettingsEnum.WINDOW_MAXIMIZED.toString(), true);
							}
						} catch (IOException ex) {
						}
					}
					
					@Override
					public void componentMoved(ComponentEvent e) {
						try {
							if ((((JFrame) e.getComponent()).getExtendedState() & Frame.MAXIMIZED_BOTH) != Frame.MAXIMIZED_BOTH) {
								getSettings().set(SettingsEnum.WINDOW_X.toString(), (int) e.getComponent().getLocation().getX());
								getSettings().set(SettingsEnum.WINDOW_Y.toString(), (int) e.getComponent().getLocation().getY());
								getSettings().set(SettingsEnum.WINDOW_MAXIMIZED.toString(), false);
							} else {
								getSettings().set(SettingsEnum.WINDOW_MAXIMIZED.toString(), true);
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
				
				window.addWindowListener(new WindowListener() {
					
					@Override
					public void windowOpened(WindowEvent e) {
					}
					
					@Override
					public void windowClosing(WindowEvent e) {
						if (!gui.getBackupGui().isBackingUp()) {
							window.dispose();
						} else {
							JOptionPane.showOptionDialog(window, getLang().get("cantCloseWhileBackingUp"), "BackupR", JOptionPane.OK_OPTION, JOptionPane.WARNING_MESSAGE, null, new String[] {getLang().get("ok2")}, null);
						}
					}
					
					@Override
					public void windowClosed(WindowEvent e) {
					}
					
					@Override
					public void windowIconified(WindowEvent e) {
					}
					
					@Override
					public void windowDeiconified(WindowEvent e) {
					}
					
					@Override
					public void windowActivated(WindowEvent e) {
					}
					
					@Override
					public void windowDeactivated(WindowEvent e) {
					}
				});
				
				if (getSettings().getBoolean(SettingsEnum.AUTOMATIC_UPDATE_CHECK.toString())) {
					new Thread(new Runnable() {
						@Override
						public void run() {
							try {
								Updater updater = new Updater(new URL("https://sites.google.com/site/mamoswebsite/backupr/versions.xml"), releaseDate);
								updater.checkForUpdates();
								gui.getProgressBar().setIndeterminate(false);
								if (updater.areUpdatesAvaiable() && (getSettings().getBoolean(SettingsEnum.AUTOMATIC_UPDATE_INSTALLATION.toString()) || JOptionPane.showOptionDialog(window, getLang().get("updateFound"), "BackupR", JOptionPane.YES_NO_OPTION, JOptionPane.QUESTION_MESSAGE, null, new String[]{getLang().get("yes"), getLang().get("no")}, null) == 0)) {
									gui.getProgressBar().setIndeterminate(true);
									updater.update();
									gui.getProgressBar().setIndeterminate(false);
									String jrePath = System.getProperty("java.home") + File.separator + "bin" + File.separator;
									if (System.getProperty("os.name").toLowerCase().contains("windows")) {
										jrePath += "java.exe";
									} else {
										jrePath += "java";
									}
									Runtime.getRuntime().exec(new String[]{jrePath, "-jar", new File(BackupR.class.getProtectionDomain().getCodeSource().getLocation().getPath()).getPath()});
									System.exit(0);
								}
								gui.setEverythingEnabled(true);
							} catch (MalformedURLException ex) {
							} catch (IOException ex) {
							}
						}
					}).start();
				} else {
					gui.setEverythingEnabled(true);
					gui.getProgressBar().setIndeterminate(false);
				}
			}
		});
	}
	
	public static Settings getSettings() {
		return settings;
	}
	
	public static Lang getLang() {
		return lang;
	}
}