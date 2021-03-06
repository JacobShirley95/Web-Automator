package tech.conexus.webautomator.gui.swing.script.selector;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.Frame;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.LinkedList;

import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTree;
import javax.swing.border.EmptyBorder;
import javax.swing.event.TreeSelectionEvent;
import javax.swing.event.TreeSelectionListener;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeModel;

import tech.conexus.webautomator.Bot;
import tech.conexus.webautomator.internal.executor.ScriptExecutor;
import tech.conexus.webautomator.internal.loader.ScriptLoader;
import tech.conexus.webautomator.script.Script;
import tech.conexus.webautomator.shared.data.library.ScriptLibrary;
import tech.conexus.webautomator.shared.data.library.ScriptManifest;

public class JScriptSelectorFrame extends JFrame implements ActionListener, TreeSelectionListener{
	private JTree tree;
	private LinkedList<JScriptSelectorListener> scriptSelListeners = new LinkedList<JScriptSelectorListener>();

	private Bot bot;
	private Object lastSelected = null;
	
	private ScriptLibrary library;
	
	public JScriptSelectorFrame(Frame parent, Bot bot) {
		super("Scripts");
		
		this.bot = bot;
		this.library = bot.getScriptLibrary();
		
		JPanel content = new JPanel();
		content.setLayout(new BorderLayout());
		content.setBorder(new EmptyBorder(10, 10, 10, 10));

		DefaultMutableTreeNode top = new DefaultMutableTreeNode(this.library.getName());
		
		DefaultTreeModel model = new DefaultTreeModel(top);

		tree = new JTree(model);
        tree.setMinimumSize(new Dimension(400, 500));
        
        for (ScriptManifest script : bot.getScriptLibrary().getScripts()) {
        	top.add(new ScriptTreeNode(script));
        }
        
        for (int i = 0; i < tree.getRowCount(); i++) {
            tree.expandRow(i);
        }
        
        tree.addTreeSelectionListener(this);

		JScrollPane listScrollPane = new JScrollPane(tree);
		
		content.add(listScrollPane, BorderLayout.CENTER);
		
		JPanel buttonPanel = new JPanel();
		
		JButton runScript = new JButton("Run");
		JButton importScript = new JButton("Import");
		
		runScript.addActionListener(this);
		
		buttonPanel.add(runScript);
		buttonPanel.add(importScript);
		
		content.add(buttonPanel, BorderLayout.SOUTH);
		
		setContentPane(content);
		
		setMinimumSize(new Dimension(300, 100));
		setResizable(false);
		setLocationRelativeTo(parent);
		setVisible(true);
		pack();
	}
	
	@Override
	public void actionPerformed(ActionEvent arg0) {
		this.dispose();
		
		ScriptLoader loader = bot.getScriptLoader();

		try {
			if (lastSelected instanceof ScriptTreeNode) {
				Script script = loader.loadScript(((ScriptTreeNode)lastSelected).manifest);
				ScriptExecutor executor = bot.getScriptExecutor();
				executor.runScript(script);
				
				for (JScriptSelectorListener listener : scriptSelListeners) {
					listener.onScriptSelected(script);
				}
			}
		} catch (ClassNotFoundException e) {
			e.printStackTrace();
		}
	}
	
	public void addScriptSelectorListener(JScriptSelectorListener listener) {
		this.scriptSelListeners.add(listener);
	}
	
	public void removeScriptSelectorListener(JScriptSelectorListener listener) {
		this.scriptSelListeners.remove(listener);
	}
	
	@Override
	public void valueChanged(TreeSelectionEvent e) {
		lastSelected = tree.getLastSelectedPathComponent();
	}
	
	class ScriptTreeNode extends DefaultMutableTreeNode {
		private ScriptManifest manifest;
		public ScriptTreeNode(ScriptManifest manifest) {
			super(manifest.getName() + " | " + manifest.getVersion());
			
			this.manifest = manifest;
		}
	}
}
