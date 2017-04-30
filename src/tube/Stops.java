package tube;

import java.awt.BorderLayout;
import java.awt.EventQueue;

import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.border.EmptyBorder;

import javax.swing.JScrollPane;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JSpinner;
import javax.swing.JButton;
import javax.swing.JTextArea;
import java.awt.event.ActionListener;
import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.SortedMap;
import java.util.TreeMap;
import java.awt.event.ActionEvent;
import java.awt.Dimension;
import javax.swing.JTextField;

/**
 *  The Stops program implements an application that
 *  displays a list of stops that are N stops away
 *  from the origin station in the London tube lines.
 *  
 * @author Krystyna Wisnaskas
 *
 */
public class Stops extends JFrame {

	/**
	 *  Default serial version ID
	 */
	private static final long serialVersionUID = 1L;

	/**
	 * The main content pane.
	 */
	private JPanel contentPane = null;
	
	/**
	 * Map of all the stops and their next hops.
	 */
	private Map<String, ArrayList<String>> stopsMap = new HashMap<String, ArrayList<String>>();
	
	/**
	 * Sorted map of visited stops during a search for the requested stop list.
	 */
	private SortedMap<String, Integer> visitedStops = new TreeMap<String, Integer>();
	
	/**
	 * Origin text field for entering the starting station. The default station is "East Ham".
	 */
	private JTextField originTextField = null;

	/**
	 * Launch the application.
	 */
	public static void main(String[] args) {
		EventQueue.invokeLater(new Runnable() {
			public void run() {
				try {					
					Stops frame = new Stops();
					frame.setVisible(true);
					boolean dataLoaded = frame.loadData();
					if (!dataLoaded) {
						JOptionPane.showMessageDialog(null, "Failed to load data. Please make sure the London tube lines.csv file exists.", "Failed to load data", JOptionPane.ERROR_MESSAGE);
						System.exit(ERROR);
					}
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		});
	}

	/**
	 * Create the frame.
	 */
	public Stops() {
		setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		setBounds(100, 100, 497, 367);

		setContentPane(getContentPane());
		
		JPanel panel = new JPanel();
		getContentPane().add(panel, BorderLayout.NORTH);
		
		JLabel lblNewLabel = new JLabel("Origin:");
		panel.add(lblNewLabel);
		
		panel.add(getOriginTextField());
		
		JLabel lblNumberOfStops = new JLabel("Number Of Stops:");
		panel.add(lblNumberOfStops);
		
		JScrollPane scrollPane = new JScrollPane();
		contentPane.add(scrollPane, BorderLayout.CENTER);
		
		JTextArea textArea = new JTextArea();
		scrollPane.setViewportView(textArea);
		
		JSpinner spinner = new JSpinner();
		spinner.setPreferredSize(new Dimension(40, 20));

		panel.add(spinner);
		
		JButton btnShowStops = new JButton("Show Stops");
		btnShowStops.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				long startTime = System.nanoTime();
				int n = (int) spinner.getValue();
				String origin = getOriginTextField().getText();
				
				if (!getStopsMap().containsKey(origin)) {
					JOptionPane.showMessageDialog(null, "Please enter a valid station", "Invalid station", JOptionPane.ERROR_MESSAGE);
				} else {
					createStopList(n, origin, n);
					
					textArea.setText("Stops " + n  + " hops away from " + origin + ":\n\n");				
					getVisitedStops().forEach((key, value) -> {
					    if (value==n) {
					    	textArea.append("\t" + "--   " + key + "\n");
					    }
					});
					getVisitedStops().clear();
					textArea.append("\nTime: " + String.valueOf((System.nanoTime() - startTime)/1000000) + " ms");
				}
			}
		});
		panel.add(btnShowStops);
	}
	
	/**
	 * Searches the stopsMap to create a sorted map of all the visited stations
	 * and their least number of hops away from the origin station. The exit criteria
	 * occur when the counter reaches 0, a null stop has been passed in or when the
	 * current stop being evaluated can be reached in fewer hops than the current level number.
	 * 
	 * @param n - the requested number of hops from the origin station.
	 * @param stop - the current stop.
	 * @param counter - a decrementing counter to search for stops n hops away from the origin.
	 */
	private void createStopList(int n, String stop, int counter) {
		if (stop == null) {
			return;
		}
		int currentLevel = n - counter;
		Integer visitedStopLevel = getVisitedStops().get(stop);
		if (visitedStopLevel == null || visitedStopLevel > currentLevel) {
			getVisitedStops().put(stop, currentLevel);
			visitedStopLevel = currentLevel;
		}
		if (counter > 0 && visitedStopLevel >= currentLevel) {
			ArrayList<String> children = getStopsMap().get(stop);
			if (children != null) {
				for (String station : children) {
					createStopList(n, station, (counter - 1));
				}
			}
		}			
	}
	
	/**
	 * Loads the stop and their next hops data from the .csv file into
	 * the stopsMap.
	 * 
	 * @return true if the data was successfully loaded; false otherwise.
	 */
	private boolean loadData() {
		boolean result = false;
		BufferedReader reader;
		try {
			reader = new BufferedReader(new FileReader("London tube lines.csv"));
			reader.readLine();
			String line = null;
			
			String[] stations = null;
			String from = null;
			String to = null;
			while ((line = reader.readLine()) != null) {
				stations = line.split(",");
				from = stations[1];
				to = stations[2];
				createChildren(from, to);
				createChildren(to, from);	
			}
			result = true;
		} catch (FileNotFoundException e1) {
			e1.printStackTrace();
		}
		catch (IOException e1) {
			e1.printStackTrace();
		}
		return result;
	}

	/**
	 * Helper method to add next hops to a given pair of from and to pair.
	 * 
	 * @param from - a from stop used as a key in the stopsMap map.
	 * @param to - a to stop used as a key in the stopsMap map.
	 */
	private void createChildren(String from, String to) {
		List<String> children = getStopsMap().get(from);
		if (children == null) {
			children = new ArrayList<String>();
			getStopsMap().put(from, (ArrayList<String>) children);
		} 
		if (!children.contains(to)){
			children.add(to);
		}		
	}

	/* (non-Javadoc)
	 * @see javax.swing.JFrame#getContentPane()
	 */
	public JPanel getContentPane() {
		if (contentPane == null) {
			contentPane = new JPanel();
			contentPane.setBorder(new EmptyBorder(5, 5, 5, 5));
			contentPane.setLayout(new BorderLayout(0, 0));
		}
		return contentPane;
	}

	/**
	 * Gets the loaded station data map.
	 * 
	 * @return stopsMap.
	 */
	public Map<String, ArrayList<String>> getStopsMap() {
		return stopsMap;
	}

	/**
	 * Gets the visited stops map
	 * 
	 * @return visitedStops map.
	 */
	public SortedMap<String, Integer> getVisitedStops() {
		return visitedStops;
	}

	/**
	 * Gets the origin station text field.
	 * @return originTextField
	 */
	public JTextField getOriginTextField() {
		if (originTextField == null) {
			originTextField = new JTextField("East Ham");
			originTextField.setToolTipText("\"Enter the origin station\"");
			originTextField.setColumns(10);
		}
		return originTextField;
	}
}
