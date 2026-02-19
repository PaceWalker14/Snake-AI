# 🐍 Snake-AI

An intelligent autonomous agent built in Java that plays the classic Snake game. The project uses a custom-built AI controller (`MyAgent.java`) to navigate the board, avoid obstacles, and hunt for food with high efficiency.

![Java Version](https://img.shields.io/badge/Java-8%2B-orange)
![License](https://img.shields.io/badge/License-MIT-blue)

---

## 🚀 How It Works

The agent processes the game state in real-time to make optimal survival decisions. Its logic is divided into four main layers:

* **Perception:** The agent receives a full map of the board every tick, identifying the coordinates of the Snake's head, body segments, food locations, and boundary walls [00:00:05].
* **Pathfinding:** It calculates the most efficient route (Up, Down, Left, or Right) to the nearest food source using distance-based heuristics [00:00:12].
* **Safety Analysis:** Before executing a move, the AI runs a "Collision Check" to ensure the next coordinate isn't a wall or a part of its own growing body [00:00:18].
* **Fallback Strategy:** If a direct path to food is dangerous or blocked, the agent enters "Survival Mode," rerouting to the largest available open space to avoid getting trapped [00:00:25].

---

## 🛠️ Getting Started

### Prerequisites
* **Java Development Kit (JDK) 8** or later installed on your system.

### Running the Simulation
1.  Clone this repository.
2.  Ensure `MyAgent.java` is compiled.
3.  Launch the simulation using the provided runner:
    ```bash
    java -jar SnakeRunner.jar
    ```

---

## 📂 Project Structure
* `MyAgent.java`: The core AI logic and decision-making engine.
* `SnakeRunner.jar`: The game engine and GUI used to visualize the AI's performance.
* `/src`: Contains supporting classes for game state management.

---

## 📝 License
This project is licensed under the MIT License - see the LICENSE file for details.
