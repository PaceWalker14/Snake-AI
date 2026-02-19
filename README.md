# 🐍 Snake-AI

An intelligent autonomous agent built in Java that plays the classic Snake game. The project features a custom AI controller (`MyAgent.java`) that utilizes pathfinding algorithms and predictive heuristics to outlast opponents and maximize its score.

![Java Version](https://img.shields.io/badge/Java-8%2B-orange)
![License](https://img.shields.io/badge/License-MIT-blue)
![Algorithm](https://img.shields.io/badge/Algorithm-A*-green)

---

## 🧠 How It Works

The agent processes the game state through a multi-layered decision-making pipeline:

### 1. Perception & Grid Mapping
The AI receives raw coordinate data for all snakes and the apple. It reconstructs this into a 2D collision grid, intelligently interpolating body segments between joints to ensure 100% accuracy in obstacle detection.

### 2. A* Pathfinding
For food retrieval, the agent implements the **A* Search Algorithm**. It calculates the most efficient route to the apple using **Manhattan Distance** as its heuristic:
$$d(a, b) = |a_x - b_x| + |a_y - b_y|$$



### 3. Predictive Safety (Head-on Collision Avoidance)
To prevent deaths caused by other snakes, the agent calculates a **one-block buffer zone** around enemy heads. If a move could potentially result in an opponent moving into the same space, the agent proactively chooses a different path.

### 4. Temporal Awareness
The agent tracks the "Age" of the apple. If the apple has been active for over 50 timesteps without being reached, the AI recognizes it may be a high-risk target and prioritizes safer positioning over aggressive hunting.

---

## 🛠️ Project Structure

* **`MyAgent.java`**: The core logic. Contains the A* implementation, collision grid builder, and movement engine.
* **`SnakeRunner.jar`**: The simulation environment used to test and visualize the agent.
* **`README.md`**: Project documentation.

---

## 🚀 Getting Started

### Prerequisites
* **Java Development Kit (JDK) 8** or later.

### Running the Project Locally
1. Clone the repository.
2. Compile the source code.
3. Run the simulation using the provided JAR file:
```bash
java -jar SnakeRunner.jar
