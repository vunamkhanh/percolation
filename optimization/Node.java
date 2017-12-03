import java.util.ArrayList;

// Class node for network representation
// Two important properties:
// - If a node is visited
// - Index (or label) of a node

public class Node{
	
	private boolean giant;
	private boolean visited;
	private int index;
	private ArrayList<Integer> neighbours;
	private int dependency;
	
	// Constructor
	public Node(int ind){
		giant = false;
		visited = false;
		index = ind;
		neighbours = new ArrayList<Integer>();
		dependency = -1;
	}
	
	// Get values
	public boolean checkGiant() { return giant; }
	public boolean checkVisited() { return visited; }
	public int getIndex() { return index; }
	
	public ArrayList<Integer> getNeighbours() { return neighbours; }
	public int getNeighbour(int id) { return neighbours.get(id); }
	public int getSize() { return neighbours.size(); }
	
	public int getDependency() { return dependency; }
	
	// Setting values
	public void setGiant(){
		giant = true;
	}
	
	public void resetGiant(){
		giant = false;
	}

	public void markVisited(){
		visited = true;
	}
	
	public void unmarkVisited(){
		visited = false;
	}
	
	public void setIndex(int newIndex){
		index = newIndex;
	}
	
	public boolean connected(int node){
		if (neighbours.contains(node)){
			return true;
		}
		else{
			return false;
		}
	}
	
	public void addLink(int node){
		neighbours.add(node);
	}
	
	public void deleteLink(int node){
		for (int i = 0; i < neighbours.size(); i++){
			if (neighbours.get(i) == node){
				neighbours.remove(i);
				break;
			}
		}
	}
	
	public void clearLink(){
		neighbours.clear();
	}
	
	public void addDependency(int node){
		dependency = node;
	}
	
	public void deleteDependency(){
		dependency = -1;
	}
}