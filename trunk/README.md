# CloudSim Task Allocation and Scheduling

* Particle Swarm Optimization `PSO.PSO_Scheduler`
* Round Robin Algorithm       `RoundRobin.RoundRobinScheduler`
* Shortest Job First          `SJF.SJF_Scheduler`
* First Come First Serve      `FCFS.FCFS_Scheduler`
* Self adaptive weight PSO    `MOPSO.MOPSO_Scheduler`
* Ant Colony Optimization     `ACO.ACO_Scheduler`


# Working Principle (according to microservice usecase)
1. list of dockerfiles with tasks (cloudlets)
2. Claudius has a host machine, it runs a LinuxVm, it then runs Dockerfiles, and each one of it has a task with different resource requirements (cpu, IO, power)
3. Claudius have default Provisioning Policy & Schedulers
4. LinuxVm has custom Scheduler to cluster Dockerfiles and run their associated tasks [TBD]
5. Start, retrieve data about tasks, and Stop simulation using CloudSim class

# TODO: Algorithms
- find a class to fix as the base (task / cloudlet) -> name it as Dockerfile
- find how to integrate scheduling algorithm
- see how to add other parameters like bandwidth
- design a overall system resources and call it as ResourcePool / InfrastructureResources
- a new output format, which is friendly

# TODO: Illustrations
- List out various graphs that we can use to compare algos
- Clustering Illustration

# Output Format
there will be many algo
algo1, algo2, algo3 etc:
and for each, these many parameters will be there:
network: 1,2,4,214,3,4,234,5,32 ...
cpu time: "" ""    ""  ms
latency: "" "" "" ms

# Graphs
* Bar chart 
* Pie chart 
* Histogram 
* Heat map 
* Line graph
* Box plot
* Radar
* Gantt
* Star glyph
* scatter
* plotly, dash and streamlit - interactive

# Clues 
- fix VM ID, (HOST) - Resource Pool
- 4, 5: mapping broker (tasks / vms) - have separate class, compute using broker
- Debugging message print
- coupling / cohersion pathuko 

# Tips
- use duet 
- https://cloud.google.com/ai/duet-ai?hl=en