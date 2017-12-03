import java.io.*;
import java.util.*;


// This program tests the running speed of decremental connectivity algorithms vs the standard BFS algorithm
// to determine how much improvements can be made for the case of single ER breakdown
// Uses the Node class to represent a node within a network and its connections


public class Optimization{
	
	public static void main(String[] args){
		
		Optimization O = new Optimization();
		
		O.perform();
		
	}

	////////////////////////////////////////////////////////////////////////////
	// 0. Program flow and results collection method
	
	// Performing the program flow
	void perform(){
		
		// Collecting network A attributes
		System.out.println("Attributes for network A: ");
		double[] genA = generationInput();
		System.out.println();
		// Collecting network B attributes
		System.out.println("Attributes for network B: ");
		double[] genB = generationInput();
		System.out.println();
		// Collecting coupled system and simulation parameters
		double[] system = systemInput();
		
		
		// Algorithms start time
		long start = System.currentTimeMillis();
		
		// Array of 100 values to store breakdown result
		int[][] results = new int[100][(int)system[2]];
		
		// Repeat the calculations, obtain the average and the standard deviation
		for (int rep = 0; rep < system[2]; rep++){
			
			// Repetition count
			System.out.println("Rep: " + (rep+1));
			
			// Perform the cascade process for damage levels from 0 to 99%
			for (int i = 0; i < 100; i++){

				// Generate Node networks with the corresponding attributes
				ArrayList<Node> networkA = generateNetwork(genA);
				ArrayList<Node> networkB = generateNetwork(genB);
				
				// Generate interdependency links
				generateDependency(system, networkA, networkB);
				
				// Find final giant cluster size from the cascade process
				double initialDamage = i/100.0;
				results[i][rep] = cascade(initialDamage, networkA, networkB);
				
				// Percolation progress
				System.out.println(results[i][rep]);
				
			}
			
		}
		
		// Print out simulation specs
		System.out.println("Network A: " + genA[0] + " " + genA[1] + " " + genA[2] + " " + genA[3]);
		System.out.println("Network B: " + genB[0] + " " + genB[1] + " " + genB[2] + " " + genB[3]);
		System.out.println("Interdependency: " + system[0] + " " + system[1]);
		
		// Algorithms end time
		long end = System.currentTimeMillis();
		long length = (end - start)/1000;
		System.out.println("Running time: " + length + "s");
		
		
		printResults(results);
		
	}
	
	
	////////////////////////////////////////////////////////////////////////////
	// I. Input
	
	// Collect all network attributes and put them into an array
	// Attributes: complexity (ER, BA, WS, hybrid); size
	// Complexity specifics: ER - average degree.
	double[] generationInput(){
		
		double[] input = new double[4];
		
		// Network type - input[0]
		// 1 - ER, 2 - BA, 3 - WS
		System.out.println("Type of the network: (1-ER, 2-BA, 3-WS)");
		input[0] = readDouble();
		int type = (int) input[0];
		
		// Network size - input[1]
		System.out.println("Size of the network: ");
		input[1] = readDouble();
		
		if (type == 1){
			// ER average degree - input[2]
			System.out.println("Average degree in ER: ");
			input[2] = readDouble();
			
			// ER placeholder input - input[3]
			input[3] = 0;
		}
		else if (type == 2){
			// BA initial seed network - input[2]
			System.out.println("Size of the seed network in BA: ");
			input[2] = readDouble();
			
			// BA number of links for each new node - input[3]
			System.out.println("Number of new links for each new node in BA: ");
			input[3] = readDouble();
		}
		else if (type == 3){
			// WS initial uniform node degree - input[2]
			System.out.println("Number of edges on each side of a node initially in WS: ");
			input[2] = readDouble();
			
			// WS rewiring probability - input [3]
			System.out.println("Probability of rewiring for each edge in WS: ");
			input[3] = readDouble();
		}
		else{
			input[1] = 0;
			input[2] = 0;
			input[3] = 0;
		}
		
		return input;
	}
	
	// Collect general coupled systems input
	// Coupling type - Undirected, directed; Coupling strength - 0.1 to 0.9
	// Initial damage strength
	double[] systemInput(){
		
		double[] input = new double[3];
		
		// Interdependency type - input[0]
		System.out.println("Type of interdependency: (1-Undirected, 2-Directed)");
		input[0] = readDouble();
		
		// Coupling strength - input[1]
		System.out.println("Coupling strength: ");
		input[1] = readDouble();
		
		// Number of repetitions for each system - input[2]
		System.out.println("Number of repetitions: ");
		input[2] = readDouble();
		
		return input;
		
	}
	
	// Read an user input
	double readDouble(){
		
		// For command prompt
		Scanner sc = new Scanner(System.in);
		double input = 0;
		boolean check = true;
			
		while (check){
			try{
				input = sc.nextDouble();
				check = false;
				if (input < 0){
					System.out.println("Please input a positive number.");
					check = true;
				}
			}
			catch (Exception e){
				System.out.println("Please input a number.");
				sc = new Scanner(System.in);
			}
		}
		
		return input;
		
		// For Eclipse
		/*Console console = System.console();
		double input = 0;
		boolean check = true;
		
		while (check) {
			try {
				String sample = console.readLine();
				input = Double.parseDouble(sample);
				check = false;
				if (input < 0) {
					System.out.println("Please input a positive number.");
					check = true;
				}
			}
			catch (Exception e) {
				System.out.println("Please input a number.");
				console = System.console();
			}
		}
		
		return input;*/
		
	}
	
	
	////////////////////////////////////////////////////////////////////////////
	// II. Network Generation
	
	// Choose a network based on input
	ArrayList<Node> generateNetwork(double[] input){
		
		int type = (int) input[0];
		
		if (type == 1){
			return generateER((int)input[1], input[2]);
		}
		else if (type == 2){
			return generateBA((int)input[1], (int)input[2], input[3]);
		}
		else if (type == 3){
			return generateWS((int)input[1], (int)input[2], input[3]);
		}
		else{
			ArrayList<Node> temp = new ArrayList<Node>();
			return temp;
		}
		
	}
	
	// Generate an ER network
	ArrayList<Node> generateER(int size, double avgDeg){
		
		ArrayList<Node> network = new ArrayList<Node>();
		
		// Initialize the empty network
		for (int i = 0; i < size; i++){
			Node a = new Node(i);
			network.add(a);
		}
		
		// Number of edges to be added
		int edges = (int) (size*avgDeg/2);
		
		// Random variables
		Random r = new Random();
		int node1 = r.nextInt(size);
		int node2 = r.nextInt(size);
		
		// Add the appropriate number of edges
		for (int i = 0; i < edges; i++){
			while ((node1 ==  node2) || network.get(node1).connected(node2) || network.get(node2).connected(node1)){
				node1 = r.nextInt(size);
				node2 = r.nextInt(size);
			}
			network.get(node1).addLink(node2);
			network.get(node2).addLink(node1);
		}
		
		// Return the ER network
		return network;
	}
	
	// Generate a BA network
	ArrayList<Node> generateBA(int size, int seed, double links){
		
		ArrayList<Node> network = new ArrayList<Node>();
		
		// Initialize the empty network
		for (int i = 0; i < size; i++){
			Node a = new Node(i);
			network.add(a);
		}	
		
		// Initialize order in which nodes are added
		Random r = new Random();
		int rand = 0;
		int temp = 0;
		int ind = 0;
		ArrayList<Integer> order = new ArrayList<Integer>();
		for (int i = 0; i < size; i++){
			order.add(i);
		}
		for (int i = 0; i < size; i++){
			rand = r.nextInt(size-i);
			ind = i + rand;
			temp = order.get(ind);
			order.set(ind, order.get(i));
			order.set(i, temp);
		}
		
		// Auxillary list used to determine the node to be attached during preferential attachment
		ArrayList<Integer> pref = new ArrayList<Integer>();
		
		// Create the seed network
		for (int i = 0; i < seed; i++){
			for (int j = 0; j < seed; j++){
				if (i != j){
					network.get(order.get(i)).addLink(order.get(j));
					pref.add(order.get(i));
				}
			}
		}

		// Perform preferential attachment until the desired size is reached
		// Total degrees of the network
		int totalDeg = pref.size();
		
		// Add new nodes according to the mixed order
		for (int i = seed; i < size; i++){

			// Randomly pick a node to connect to in the current network
			int j = 0;
			while (j < links){
				rand = r.nextInt(totalDeg);
				int ans = pref.get(rand);
				
				// Pick the appropriate node from the preferential attachment probability list
				if (!network.get(ans).connected(order.get(i))){
					
					// Connec the nodes
					network.get(ans).addLink(order.get(i));
					network.get(order.get(i)).addLink(ans);
					
					// Modify the probability list
					pref.add(ans);
					pref.add(order.get(i));
					
					// Register that the link is added without duplicate
					j++;
				}
				
			}
			totalDeg = pref.size();
		}
		
		return network;
	}
	
	// Generate a WS network
	ArrayList<Node> generateWS(int size, int edge, double prob){
		
		ArrayList<Node> network = new ArrayList<Node>();
		
		// Initialize the empty network
		for (int i = 0; i < size; i++){
			Node a = new Node(i);
			network.add(a);
		}
		
		// Temporary variables to be used during generation
		Random r = new Random();
		int rand = 0;
		int ind = 0;
		
		// Generate the initial regular lattice, 
		// add edges on left and right hand sides at the same time
		for (int i = 0; i < size; i++){
			for (int j = 1; j <= edge; j++){
				ind = (i-j+size)%(size);
				network.get(i).addLink(ind);
				ind = (i+j+size)%(size);
				network.get(i).addLink(ind);
			}
		}
		
		// Rewire each edge according to a given probability
		for (int i = 0; i < size; i++){
			for (int j = (i+1); j <= (i+edge); j++){
				if (Math.random() < prob){
					ind = j%size;
					network.get(i).deleteLink(ind);
					network.get(ind).deleteLink(i);
					rand = r.nextInt(size);
					while ( (i==rand) || (network.get(i).connected(rand)) ){
						rand = r.nextInt(size);
					}
					network.get(i).addLink(rand);
					network.get(rand).addLink(i);
				}
			}
		}
		
		return network;
	}
	
	
	////////////////////////////////////////////////////////////////////////////
	// III. Interdependency Formation	
	
	// Generate dependency based on input
	void generateDependency(double[] system, ArrayList<Node> networkA, ArrayList<Node> networkB){
		
		if ((int)system[0] == 1){
			undirectedDependency(system[1], networkA, networkB);
		}
		else if ((int)system[0] == 2){
			directedDependency(system[1], networkA, networkB);
			directedDependency(system[1], networkB, networkA);
		}
		
	}
	
	// Generate undirected dependency links between networks
	void undirectedDependency(double strength, ArrayList<Node> networkA, ArrayList<Node> networkB){
		
		// Order of addition for interdependency
		int[] orderA = mix(networkA.size());
		int[] orderB = mix(networkB.size());
		
		// Temporary index
		int a = 0;
		int b = 0;
		
		// Number of interdependency links
		double coupling = strength*networkA.size();
		
		// Add the required links to the connection matrix
		for (int i = 0; i < coupling; i++){
			a = orderA[i];
			b = orderB[i];
			networkA.get(a).addDependency(b);
			networkB.get(b).addDependency(a);
		}
		
	}
	
	// Generate directed dependency links from one network to the other
	// Networks at the end of the link is dependent upon the host network
	void directedDependency(double strength, ArrayList<Node> networkA, ArrayList<Node> networkB){
		
		// Temporary index
		int a = 0;
		int b = 0;
		
		// Order of addition for interdependency
		int[] orderA = mix(networkA.size());
		int[] orderB = mix(networkB.size());
		
		// Number of interdependency links
		double coupling = strength*networkA.size();
		
		// Add the required links to the connection matrix
		for (int i = 0; i < coupling; i++){
			a = orderA[i];
			b = orderB[i];
			networkA.get(a).addDependency(b);
		}
		
	}

	// Method to mix the order of nodes to be added with interdependency
	int[] mix(int size){
		
		int[] order = new int[size];
		
		// Initially ordered array
		for (int i = 0; i < size; i++){
			order[i] = i;
		}
		
		// Variables for mixing
		Random r = new Random();
		int tmp = 0;
		int rand = 0;
		
		for (int i = 0; i < size; i++){
			rand = r.nextInt(size-i);
			tmp = order[i];
			order[i] = order[i + rand];
			order[i + rand] = tmp;
		}
		
		return order;
		
	}
	
	
	////////////////////////////////////////////////////////////////////////////
	// IV. Damage and Cascade Process
	
	// Method to perform the cascade process
	int cascade(double damage, ArrayList<Node> networkA, ArrayList<Node> networkB){
		
		// List of nodes to delete from a network
		ArrayList<Integer> deleteList = initialDamage(damage, networkA.size());

		// Size of the giant cluster in A
		int giant = 0;
		
		// Size of the giant cluster in the previous step
		int prev = 0;
		
		// Check if the process has converged
		boolean check = true;
		
		// Cascade until the process has converged, where there are no more nodes to be deleted
		while (check){
			
			// Check for convergence
			prev = giant;
			
			// Apply damage to A, find nodes not belonging to the giant cluster and their dependent nodes
			applyDamage(networkA, deleteList);
			giant = BFS(networkA);
			deleteList = findDelete(networkA);

			// Apply damage to B, find nodes not belonging to the giant cluster and their dependent nodes
			applyDamage(networkB, deleteList);
			BFS(networkB);
			deleteList = findDelete(networkB);
			
			check = !((giant - prev) == 0); 
			
		}
		
		return giant;
		
	}
	
	// Nodes deleted in the initial damage
	ArrayList<Integer> initialDamage(double strength, int networkSize){
		
		// List of nodes to be deleted
		ArrayList<Integer> damage = new ArrayList<Integer>();
		
		// Number of nodes to be deleted
		int size = (int) (strength*networkSize);
		
		// Order of nodes to be deleted
		int[] order = mix(networkSize);
		
		for (int i = 0; i < size; i++){
			damage.add(order[i]);
		}
		
		return damage;
	}
	
	// Add nodes not belonging to the giant cluster to the delete list
	ArrayList<Integer> findDelete(ArrayList<Node> network){
		
		ArrayList<Integer> delete = new ArrayList<Integer>();
		
		for (int i = 0; i < network.size(); i++){
			if (!network.get(i).checkGiant() &&
					(network.get(i).getDependency() != -1)){
				delete.add(network.get(i).getDependency());
			}
			else {
				network.get(i).resetGiant();
			}
		}
		
		return delete;
		
	}
	
	// Apply the given damage to a network
	void applyDamage(ArrayList<Node> network, ArrayList<Integer> damage){
		
		for (int i = 0; i < damage.size(); i++){
			
			int node = damage.get(i);
			
			// Delete links towards the node
			for (int j = 0; j < network.get(node).getSize(); j++){
				int ind = network.get(node).getNeighbour(j);
				network.get(ind).deleteLink(node);
			}
			
			// Delete links from the node
			network.get(node).clearLink();
			
		}

	}
	
	// BFS to obtain the size of the giant cluster
	int BFS(ArrayList<Node> network){
		
		// Queue of nodes to be traversed
		ArrayList<Node> queue = new ArrayList<Node>();
		
		// Size of the giant cluster
		int giant = 0;
		ArrayList<Integer> giantCluster = new ArrayList<Integer>();
		
		// Traverse through the entire network
		for (int i = 0; i < network.size(); i++){
			
			// Size of the current cluster
			int current = 0;
			ArrayList<Integer> currentCluster = new ArrayList<Integer>();
			
			if (!network.get(i).checkVisited()){;
				network.get(i).markVisited();
				queue.add(network.get(i));
			}
			
			// While there are nodes to be evaluated
			while (!queue.isEmpty()){
				current++;
				currentCluster.add(queue.get(0).getIndex());
				for (int j = 0; j < queue.get(0).getSize(); j++){
					int temp = queue.get(0).getNeighbour(j);
					if (!network.get(temp).checkVisited()){
						network.get(temp).markVisited();
						queue.add(network.get(temp));
					}
				}
				queue.remove(0);
			}
			
			// Check cluster size
			if (current > giant){
				giant = current;
				giantCluster = currentCluster;
			}
			
		}
		
		for (int i = 0; i < giantCluster.size(); i++){
			network.get(giantCluster.get(i)).setGiant();
		}
		
		for (int i = 0; i < network.size(); i++){
			network.get(i).unmarkVisited();
		}
		
		return giantCluster.size();	
	}

	
	////////////////////////////////////////////////////////////////////////////
	// V. Results Output
	
	// Print network
	void printNetwork(ArrayList<Node> network){
		
		try{
			PrintStream ps = new PrintStream("matrix.txt");
			
			for (int i = 0; i < network.size(); i++){
				ps.println(network.get(i).getIndex() + ": " + network.get(i).getNeighbours());
			}
			
			ps.close();
		}
		catch (FileNotFoundException e){
			e.printStackTrace();
		}
		
	}
	
	// Print the degree distribution of a network
	void printDist(ArrayList<Node> network){
		
		// List of node degrees
		ArrayList<Integer> degree = new ArrayList<Integer>();
		
		// Find the largest node degree
		int max = 0;
		for (int i = 0; i < network.size(); i++){
			int temp = network.get(i).getSize();
			degree.add(temp);
			if (temp > max){
				max = temp;
			}
		}
		
		// Distribution array list
		ArrayList<Integer> dist = new ArrayList<Integer>();
		for (int i = 0; i <= max; i++){
			dist.add(0);
		}
		
		// Creating the distribution
		for (int i = 0; i < degree.size(); i++){
			int temp = dist.get(degree.get(i));
			dist.set(degree.get(i), temp + 1);
		}
		
		try{
			PrintStream ps = new PrintStream("dist.txt");
			for (int i = 0; i < dist.size(); i++){
				ps.println(dist.get(i));
			}
			ps.close();
		}
		catch (Exception e){
			e.printStackTrace();
		}
	}
	
	// Method to print out the interdependency links
	void printInterdependency(ArrayList<Node> networkA, ArrayList<Node> networkB){
		
		try{
			PrintStream ps = new PrintStream("test-dependency.txt");
			
			ps.println("Network A: ");
			for (int i = 0; i < networkA.size(); i++){
				ps.print("[" + networkA.get(i).getIndex() + "]" + ": ");
				ps.println(networkA.get(i).getDependency());
			}
			ps.println();
			
			ps.println("Network B: ");
			for (int i = 0; i < networkA.size(); i++){
				ps.print("[" + networkB.get(i).getIndex() + "]" + ": ");
				ps.println(networkB.get(i).getDependency());
			}
			
			ps.close();
			
		}
		catch (FileNotFoundException e){
			e.printStackTrace();
		}
		
	}
	
	// Print out the cascade result
	void printResults(int[][] results){
		
		// Array of mean values for the breakdown
		ArrayList<Double> mean = new ArrayList<Double>();
		
		// Array of standard deviation for each value
		ArrayList<Double> sd = new ArrayList<Double>();
		
		// Calculate the mean
		double tempMean = 0;
		for (int i = 0; i < results.length; i++){
			
			// Total across all repetitions
			for (int j = 0; j < results[0].length; j++){
				tempMean = tempMean + results[i][j];
			}
			
			// Take the average
			mean.add(tempMean/results[0].length);
			tempMean = 0;
		}
		
		// Calculate the standard deviation
		double tempSd = 0;
		for (int i = 0; i < results.length; i++){
			
			// Total square of difference accross all repetitions
			for (int j = 0; j < results[0].length; j++){
				tempSd = tempSd + (results[i][j] - mean.get(i))*(results[i][j] - mean.get(i));
			}
			
			// Take the average
			tempSd = Math.sqrt(tempSd/results[0].length);
			sd.add(tempSd);
			tempSd = 0;
		}
		
		try{
			PrintStream psMean = new PrintStream("breakdown_mean.txt");
			PrintStream psSd = new PrintStream("breakdown_sd.txt");
			
			for (int i = 0; i < mean.size(); i++){
				psMean.println(mean.get(i));
				psSd.println(sd.get(i));
			}
			
			psMean.close();
			psSd.close();
		}
		catch (FileNotFoundException e){
			e.printStackTrace();
		}
	}

}