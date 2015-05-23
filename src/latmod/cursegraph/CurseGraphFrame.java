package latmod.cursegraph;

import java.awt.*;
import java.awt.event.*;
import java.util.*;

import javax.swing.*;

public class CurseGraphFrame extends JFrame
{
	private static final long serialVersionUID = 1L;
	public static final CurseGraphFrame inst = new CurseGraphFrame();
	
	public final JTabbedPane pane = new JTabbedPane(JTabbedPane.TOP, JTabbedPane.WRAP_TAB_LAYOUT);
	
	public CurseGraphFrame()
	{
		setTitle("CurseGraph v" + Main.version);
		setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);
		add(pane);
		this.setSize(700, 500);
		pane.setSize(700, 500);
		setMinimumSize(new Dimension(400, 300));
		setResizable(true);
		refresh();
		pack();
		setLocationRelativeTo(null);
		
		setVisible(!Main.config.startMinimized.booleanValue());
		setIconImage(Main.imageReady);
	}
	
	public void dispose()
	{
		if(Main.config.closeToTray) setVisible(false);
		else System.exit(0);
	}
	
	public void refresh()
	{
		int tabs = Main.config.displayTabs.intValue();
		boolean displayIcons = (tabs == 0 || tabs == 1);
		boolean displayTitles = (tabs == 0 || tabs == 2);
		
		pane.removeAll();
		pane.setTabLayoutPolicy(Main.config.scrollTabs.booleanValue() ? JTabbedPane.SCROLL_TAB_LAYOUT : JTabbedPane.WRAP_TAB_LAYOUT);
		
		ArrayList<String> componentsAdded = new ArrayList<String>();
		componentsAdded.add("Settings");
		
		JPanel settingsPanel = new JPanel(false);
		
		GridLayout layout = new GridLayout();
		layout.setColumns(1);
		layout.setRows(0);
		layout.setVgap(5);
		settingsPanel.setLayout(layout);
		
		if(Main.version < Main.latestVersion)
		{
			JButton b = new JButton("Update available!");
			b.addActionListener(new ActionListener()
			{
				public void actionPerformed(ActionEvent e)
				{ Main.openURL("https://github.com/LatvianModder/CurseGraph/"); }
			});
			
			settingsPanel.add(b);
		}
		
		{
			JButton b = new JButton("Add");
			b.addActionListener(new ActionListener()
			{
				public void actionPerformed(ActionEvent e)
				{
					try
					{
						String types[] = Curse.Type.getAllNames();
						String type0 = (String)JOptionPane.showInputDialog(null, "Select the mod:", "Add new Project", JOptionPane.PLAIN_MESSAGE, null, types, Curse.Type.MOD.name);
						if(type0 == null || type0.isEmpty()) return;
						Curse.Type t = Curse.Type.getFromName(type0);
						
						String input = JOptionPane.showInputDialog("Enter " + t.name + "'s ProjectID here:", "");
						if(input != null && !input.trim().isEmpty())
						{
							Projects.add(t, input.trim(), false);
							Main.refresh();
						}
					}
					catch(Exception ex)
					{ ex.printStackTrace(); }
				}
			});
			
			settingsPanel.add(b);
		}
		
		{
			JButton b = new JButton("Refresh");
			b.addActionListener(new ActionListener()
			{
				public void actionPerformed(ActionEvent e)
				{ try { Main.refresh(); Graph.logData(); } catch(Exception ex)
				{ ex.printStackTrace(); } }
			});
			
			settingsPanel.add(b);
		}
		
		{
			final int times[] = { -1, 1, 24, 24 * 7, 24 * 30 };
			final String types[] = { "None", "Hour", "Day", "Week", "Month" };
			
			String bname = "Custom ( " + Main.config.graphLimit + "h )";
			if(Main.config.graphLimit.intValue() == -1) bname = "None";
			else for(int i = 0; i < times.length; i++)
			{ if(times[i] == Main.config.graphLimit.intValue()) bname = types[i]; }
			
			JButton b = new JButton("Graph limit: " + bname);
			b.addActionListener(new ActionListener()
			{
				public void actionPerformed(ActionEvent e)
				{
					try
					{
						String type0 = (String)JOptionPane.showInputDialog(null, "Select graph limit:", "Graph limit", JOptionPane.PLAIN_MESSAGE, null, types, types[0]);
						if(type0 == null || type0.isEmpty()) return;
						
						for(int i = 0; i < types.length; i++)
						{
							if(type0.equals(types[i]))
							{
								Main.config.graphLimit = times[i];
								Main.config.save();
								Main.refresh();
								return;
							}
						}
					}
					catch(Exception ex)
					{ ex.printStackTrace(); }
				}
			});
			
			settingsPanel.add(b);
		}
		
		{
			JButton b = new JButton("Graph type: " + (Main.config.graphRelative.booleanValue() ? "Relative" : "Default"));
			b.addActionListener(new ActionListener()
			{
				public void actionPerformed(ActionEvent e)
				{
					Main.config.graphRelative = !Main.config.graphRelative.booleanValue();
					Main.config.save();
					refresh();
				}
			});
			
			settingsPanel.add(b);
		}
		
		{
			final boolean scrollTabs = Main.config.scrollTabs.booleanValue();
			final JButton b = new JButton("Scrolling Tabs: " + (scrollTabs ? "ON" : "OFF"));
			b.addActionListener(new ActionListener()
			{
				public void actionPerformed(ActionEvent e)
				{
					Main.config.scrollTabs = !scrollTabs;
					Main.config.save();
					refresh();
				}
			});
			
			settingsPanel.add(b);
		}
		
		{
			final int displayTabs = Main.config.displayTabs.intValue();
			final JButton b = new JButton("Display Tabs: " + ((displayTabs == 0) ? "Icons & Titles" : (displayTabs == 1 ? "Icons" : "Titles")));
			b.addActionListener(new ActionListener()
			{
				public void actionPerformed(ActionEvent e)
				{
					Main.config.displayTabs = (displayTabs + 1) % 3;
					Main.config.save();
					refresh();
				}
			});
			
			settingsPanel.add(b);
		}
		
		{
			JButton b = new JButton("Set refresh interval");
			b.addActionListener(new ActionListener()
			{
				public void actionPerformed(ActionEvent e)
				{
					String input = JOptionPane.showInputDialog("Set refresh minutes:", "" + Main.config.refreshMinutes);
					if(input != null && !input.isEmpty())
					{
						try
						{
							int i = Integer.parseInt(input);
							Main.config.refreshMinutes = Math.max(1, i);
							Main.config.save();
						}
						catch(Exception ex)
						{ Main.error("Invalid number!", false); }
					}
				}
			});
			
			settingsPanel.add(b);
		}
		
		{
			JButton b = new JButton("Open data folder");
			b.addActionListener(new ActionListener()
			{
				public void actionPerformed(ActionEvent e)
				{ try { Desktop.getDesktop().open(Main.folder); }
				catch(Exception ex) { ex.printStackTrace(); } }
			});
			
			settingsPanel.add(b);
		}
		
		/*
		{
			JButton b = new JButton("Select colors");
			b.addActionListener(new ActionListener()
			{
				public void actionPerformed(ActionEvent e)
				{
					//
					CurseGraphFrame.inst.refresh();
					Main.config.save();
				}
			});
			
			settingsPanel.add(b);
		}
		*/
		
		{
			final int times[] = { 1, 24, 24 * 7, 24 * 30 };
			final String types[] = { "Hour", "Day", "Week", "Month" };
			
			JButton b = new JButton("Clear older than...");
			b.addActionListener(new ActionListener()
			{
				public void actionPerformed(ActionEvent e)
				{
					try
					{
						String type0 = (String)JOptionPane.showInputDialog(null, "Time period:", "Clear older than...", JOptionPane.PLAIN_MESSAGE, null, types, types[0]);
						if(type0 == null || type0.isEmpty()) return;
						
						for(int i = 1; i < types.length; i++)
						{
							if(type0.equals(types[i]))
							{
								int c = Graph.clearData(times[i] * 3600000);
								
								Main.info("Removed " + c + " values!", false);
								Graph.logData();
								Main.refresh();
								return;
							}
						}
					}
					catch(Exception ex)
					{ ex.printStackTrace(); }
				}
			});
			
			settingsPanel.add(b);
		}
		
		{
			JButton b = new JButton("Exit");
			b.addActionListener(new ActionListener()
			{
				public void actionPerformed(ActionEvent e)
				{ System.exit(0); }
			});
			
			settingsPanel.add(b);
		}
		
		pane.addTab(displayTitles ? "Settings" : "", displayIcons ? Main.iconSettings : null, settingsPanel, null);
		
		Curse.Project[] projectsList = Projects.list.toArray(new Curse.Project[0]);
		Arrays.sort(projectsList);
		
		for(final Curse.Project p : projectsList)
		{
			JPopupMenu menu = new JPopupMenu();
			
			{
				JMenuItem m1 = new JMenuItem("Open Project");
				m1.addActionListener(new ActionListener()
				{
					public void actionPerformed(ActionEvent e)
					{ Main.openURL(p.url); }
				});
				
				menu.add(m1);
			}
			
			JMenu info = new JMenu("Info");
			
			{
				if(p.authors.length == 1)
				{
					JMenuItem m1 = new JMenuItem("Author: " + p.authors[0]);
					m1.addActionListener(new ActionListener()
					{
						public void actionPerformed(ActionEvent e)
						{ Main.openURL("http://minecraft.curseforge.com/members/" + p.authors[0]); }
					});
					
					info.add(m1);
				}
				else
				{
					PopupMenu authors = new PopupMenu("Authors:");
					for(final String s : p.authors)
					{
						MenuItem m1 = new MenuItem(s);
						m1.addActionListener(new ActionListener()
						{
							public void actionPerformed(ActionEvent e)
							{ Main.openURL("http://minecraft.curseforge.com/members/" + s); }
						});
						authors.add(m1);
					}
					
					info.add(authors);
				}
				
				info.add("Likes: " + p.likes);
				info.add("Favorites: " + p.favorites);
			}
			
			info.add("All downloads: " + p.getTotalDownloads());
			Integer mo = p.downloads.get("monthly");
			if(mo != null && mo.intValue() > 0) info.add("Monthly downloads: " + mo);
			info.add("Last file downloads: " + p.download.downloads);
			
			menu.add(info);
			
			{
				JMenu downloads = new JMenu("Files:");
				
				JMenuItem all = new JMenuItem("All");
				all.addActionListener(new ActionListener()
				{
					public void actionPerformed(ActionEvent e)
					{ Main.openURL("http://minecraft.curseforge.com/" + p.getType().ID + "/" + p.projectID + "/files"); }
				});
				
				downloads.add(all);
				
				JMenuItem latest = new JMenuItem("Latest");
				latest.addActionListener(new ActionListener()
				{
					public void actionPerformed(ActionEvent e)
					{ Main.openURL(p.download.url); }
				});
				
				downloads.add(latest);
				
				for(String s : p.versions.keySet())
				{
					JMenu m1 = new JMenu(s);
					
					final Curse.Version[] v = p.versions.get(s);
					for(int j = 0; j < v.length; j++)
					{
						final int fj = j;
						JMenuItem m2 = new JMenuItem(v[j].name);
						
						m2.addActionListener(new ActionListener()
						{
							public void actionPerformed(ActionEvent e)
							{ Main.openURL(v[fj].url); }
						});
						
						m1.add(m2);
					}
					
					downloads.add(m1);
				}
				
				menu.add(downloads);
			}
			
			{
				JMenuItem remove = new JMenuItem("Remove");
				remove.addActionListener(new ActionListener()
				{
					public void actionPerformed(ActionEvent e)
					{
						if(Main.showYesNo("Confirm", "Remove " + p.title + "?"))
						{
							Projects.list.remove(p);
							Projects.save();
							Main.refresh();
						}
					}
				});
				
				menu.add(remove);
			}
			
			JCurseGraph panel = new JCurseGraph(p);
			panel.setSize(700, 500);
			panel.setComponentPopupMenu(menu);
			ImageIcon icon = null;
			if(displayIcons && p.image != null)
				icon = new ImageIcon(p.image.getScaledInstance(32, 32, Image.SCALE_SMOOTH));
			pane.addTab(displayTitles ? p.title : "", icon, panel, p.title);
			componentsAdded.add(p.title);
		}
		
		JPopupMenu menuAll = new JPopupMenu();
		
		for(int i = 0; i < componentsAdded.size(); i++)
		{
			final JMenuItem item = new JMenuItem(componentsAdded.get(i));
			if(displayIcons) item.setIcon(pane.getIconAt(i));
			
			final int index = i;
			item.addActionListener(new ActionListener()
			{
				public void actionPerformed(ActionEvent e)
				{ pane.setSelectedIndex(index); }
			});
			
			menuAll.add(item);
		}
		
		pane.setComponentPopupMenu(menuAll);
	}
	
	//private static long getH(int i)
	//{ return 1000L * 60L * 60L * i; }
}