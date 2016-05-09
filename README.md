# Project6110
Rigorous Systems Project 

This project contains all the source files needed to compile and run my class project. It implements heuristics to find safety and liveness violations in a distributed system.

The steps to compile and run the code is as follows:

1. Install scala : http://www.scala-lang.org/download/

2. Install SBT: http://www.scala-sbt.org/release/docs/Setup.html

3. Download my project files from the repository

4. Navigate to the project directory

5. Run: sbt run

The above command will compile taking into account all project dependencies and run the code for 20 minutes showing how each thread makes a transition in the byte state space.
At the end, the number of bugs found by each heuristic i.e. Random walk and random walk + bounded BFS for safety violations and Bounded DFS + Random walks for liveness violations will be output. The total number of nodes, edges and the injected bugs will also be output.

A sample output is as follows:

Number of nodes = 23014

Number of edges = 24028

Total Number of bugs 230

Buggy states from random walk 58

Buggy states from random walk + bounded BFS 79

Number of states that might violate liveness 180

The repository also contains images of some sample graphs obtained by my code. Some are not randomized and later I randomized them to obtain large cycles.
