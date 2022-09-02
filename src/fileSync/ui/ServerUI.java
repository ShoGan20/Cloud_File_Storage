package fileSync.ui;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.io.IOException;

import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.JTree;
import javax.swing.SwingUtilities;
import javax.swing.border.EmptyBorder;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeModel;

import fileSync.model.Updator;
import fileSync.server.Server;
import fileSync.utilities.Utilities;

public class ServerUI extends JFrame implements ActionListener, Updator {

	// Attributes..
	private JButton dataStoreBTN, clearBTN;
	private JTextArea output;
	private JTree tree;
	private DefaultMutableTreeNode root;
	private DefaultTreeModel model;
	private File serverRoot;
	private Server server;

	// Constructor..
	public ServerUI() throws IOException {

		setTitle("File Server");
		setBackground(Color.WHITE);
		setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		setSize(800, 700);
		setResizable(false);
		setLocationRelativeTo(null);

		serverRoot = new File("server_file_root");
		serverRoot.delete();
		serverRoot.mkdir();

		server = new Server(this, serverRoot.getName());

	}

	// Initialization of UI.
	public void init() {

		JPanel internalPanel = new JPanel();
		internalPanel.setBackground(Color.WHITE);
		internalPanel.setBorder(new EmptyBorder(5, 5, 5, 5));
		setContentPane(internalPanel);
		internalPanel.setLayout(new BorderLayout(0, 0));

		JPanel btnPanel = new JPanel();
		btnPanel.setBackground(new Color(224, 255, 255));
		internalPanel.add(btnPanel, BorderLayout.NORTH);

		dataStoreBTN = new JButton("Data Store");
		dataStoreBTN.setFocusable(false);
		dataStoreBTN.setPreferredSize(new Dimension(125, 25));
		dataStoreBTN.setForeground(new Color(255, 255, 255));
		dataStoreBTN.setBackground(Color.BLUE);

		clearBTN = new JButton("Clear");
		clearBTN.setFocusable(false);
		clearBTN.setPreferredSize(new Dimension(125, 25));
		clearBTN.setForeground(new Color(255, 255, 255));
		clearBTN.setBackground(new Color(0, 128, 0));

		dataStoreBTN.addActionListener(this);
		clearBTN.addActionListener(this);

		btnPanel.add(dataStoreBTN);
		btnPanel.add(clearBTN);

		root = new DefaultMutableTreeNode(serverRoot);
		model = new DefaultTreeModel(root);
		tree = new JTree(model);
		tree.setBackground(new Color(255, 255, 224));
		tree.setBorder(new EmptyBorder(5, 5, 0, 0));
		tree.setMinimumSize(new Dimension(200, 0));
		tree.setPreferredSize(new Dimension(200, 0));
		tree.setMaximumSize(new Dimension(500, 0));

		updateTree();

		output = new JTextArea();
		output.setBorder(new EmptyBorder(10, 10, 10, 10));
		output.setEditable(false);
		output.setBackground(new Color(240, 255, 240));

		internalPanel.add(new JScrollPane(tree), BorderLayout.WEST);
		internalPanel.add(new JScrollPane(output), BorderLayout.CENTER);

		setVisible(true);
		server.start();

	}

	@Override
	public void actionPerformed(ActionEvent e) {

		if (e.getSource() == dataStoreBTN) {

		} else if (e.getSource() == clearBTN) {
			output.setText("");
		}

	}

	@Override
	public void updateTree() {

		SwingUtilities.invokeLater(() -> {
			root.removeAllChildren();
			Utilities.updateTree(root, serverRoot);
			model.reload(root);
		});

	}

	@Override
	public void append(String text) {

		SwingUtilities.invokeLater(() -> {
			output.append(text + "\n");
			output.setCaretPosition(output.getText().length());
		});

	}

	@Override
	public void updateTitle(int number) {

		this.setTitle("File Server");

	}

}
