# Percolation

This program generates two complex networks whose topology, size and characteristics are chosen by user input. The two networks are then interconnected with directed and undirected dependency link.

During the simulation step, various levels of damages are done to the network and we aim to find the final size of the stable Giant Connected Component, per percolation theory.

Networks considered and their generation algorithms:
1. Random Networks: generated using the Erdos Renyi method
2. Scale-free Networks: generated using the Barabasi Albert method
3. Small-world Networks: generated using the Watts Strogatz method

Using these three base models, further hybrids and/or combination complex topologies for networks are possible.