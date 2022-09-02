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

import fileSync.client.Client;
import fileSync.model.Message;
import fileSync.model.MessageType;
import fileSync.model.Updator;
import fileSync.utilities.Utilities;

public class ClientUI extends JFrame implements ActionListener, Updator {

	// Attributes..
	private JButton dataStoreBTN, clearBTN, suspendBTN, resumeBTN, syncStatusBTN;
	private JTextArea output;
	private JTree tree;
	private DefaultMutableTreeNode root;
	private DefaultTreeModel model;
	private File clientRoot;
	private Client client;

	// Constructor..
	public ClientUI() throws IOException {

		setTitle("client_");
		setBackground(Color.WHITE);
		setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		setSize(800, 700);
		setLocationRelativeTo(null);

		client = new Client(this, "client_file_root_");

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

		suspendBTN = new JButton("Suspend");
		suspendBTN.setPreferredSize(new Dimension(125, 25));
		suspendBTN.setForeground(Color.WHITE);
		suspendBTN.setFocusable(false);
		suspendBTN.setBackground(Color.BLUE);
		btnPanel.add(suspendBTN);

		resumeBTN = new JButton("Resume");
		resumeBTN.setPreferredSize(new Dimension(125, 25));
		resumeBTN.setForeground(Color.WHITE);
		resumeBTN.setFocusable(false);
		resumeBTN.setBackground(new Color(0, 128, 0));
		btnPanel.add(resumeBTN);

		btnPanel.add(dataStoreBTN);

		syncStatusBTN = new JButton("Sync Status");
		syncStatusBTN.setPreferredSize(new Dimension(125, 25));
		syncStatusBTN.setForeground(Color.WHITE);
		syncStatusBTN.setFocusable(false);
		syncStatusBTN.setBackground(Color.BLUE);
		btnPanel.add(syncStatusBTN);
		btnPanel.add(clearBTN);

		dataStoreBTN.addActionListener(this);
		clearBTN.addActionListener(this);
		suspendBTN.addActionListener(this);
		resumeBTN.addActionListener(this);
		syncStatusBTN.addActionListener(this);

		tree = new JTree();
		tree.setBackground(new Color(255, 255, 224));
		tree.setBorder(new EmptyBorder(5, 5, 0, 0));
		tree.setMinimumSize(new Dimension(200, 0));
		tree.setPreferredSize(new Dimension(200, 0));
		tree.setMaximumSize(new Dimension(500, 0));

		output = new JTextArea();
		output.setBorder(new EmptyBorder(10, 10, 10, 10));
		output.setEditable(false);
		output.setBackground(new Color(240, 255, 240));

		internalPanel.add(new JScrollPane(tree), BorderLayout.WEST);
		internalPanel.add(new JScrollPane(output), BorderLayout.CENTER);

		setVisible(true);
		client.start();
		client.send(new Message(MessageType.SIMPLE_MESSAGE, ""));

	}

	@Override
	public void actionPerformed(ActionEvent e) {

		if (e.getSource() == dataStoreBTN) {

		} else if (e.getSource() == clearBTN) {
			output.setText("");
		} else if (e.getSource() == suspendBTN) {
			client.send(new Message(MessageType.SUSPEND, ""));
		} else if (e.getSource() == resumeBTN) {
			client.send(new Message(MessageType.RESUME, ""));
		} else if (e.getSource() == syncStatusBTN) {
			client.printSync();
		}

	}

	@Override
	public void updateTree() {

		SwingUtilities.invokeLater(() -> {
			root.removeAllChildren();
			Utilities.updateTree(root, clientRoot);
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

		this.setTitle("client_" + number);
		clientRoot = new File("client_file_root_" + number);
		clientRoot.delete();
		clientRoot.mkdir();
		root = new DefaultMutableTreeNode(clientRoot);
		model = new DefaultTreeModel(root);
		tree.setModel(model);
		this.updateTree();

	}

}
