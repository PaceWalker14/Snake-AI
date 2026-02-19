import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.*;
import za.ac.wits.snake.DevelopmentAgent;

public class MyAgent extends DevelopmentAgent {
    private static final int[] dx = {0, 0, -1, 1}; // up, down, left, right
    private static final int[] dy = {-1, 1, 0, 0};
    
    private int boardWidth, boardHeight;
    private int mySnakeNum;
    private List<Snake> snakes;
    private Point apple;
    private Point lastApple;
    private int appleTimestamp = 0;
    private int timestep = 0;
    private boolean[][] grid;

    public static void main(String args[]) {
        MyAgent agent = new MyAgent();
        MyAgent.start(agent, args);
    }

    @Override
    public void run() {
        try (BufferedReader br = new BufferedReader(new InputStreamReader(System.in))) {
            String initString = br.readLine();
            String[] temp = initString.split(" ");
            int nSnakes = Integer.parseInt(temp[0]);
            boardWidth = Integer.parseInt(temp[1]);
            boardHeight = Integer.parseInt(temp[2]);

            while (true) {
                String line = br.readLine();
                if (line.contains("Game Over")) {
                    break;
                }

                // Parse apple position
                String[] appleCoords = line.split(" ");
                apple = new Point(Integer.parseInt(appleCoords[0]), Integer.parseInt(appleCoords[1]));
                
                // Track when apple spawns/changes
                if (lastApple == null || !lastApple.equals(apple)) {
                    lastApple = new Point(apple.x, apple.y);
                    appleTimestamp = timestep;
                }
                
                mySnakeNum = Integer.parseInt(br.readLine());
                snakes = new ArrayList<>();
                
                for (int i = 0; i < nSnakes; i++) {
                    String snakeLine = br.readLine();
                    Snake snake = parseSnake(snakeLine, i);
                    snakes.add(snake);
                }
                
                Snake mySnake = snakes.get(mySnakeNum);
                if (!mySnake.alive || mySnake.body.isEmpty()) {
                    System.out.println(new Random().nextInt(4));
                    timestep++;
                    continue;
                }
                
                // Build collision grid
                buildCollisionGrid();
                
                // Decide move
                int move = decideBestMove(mySnake);
                System.out.println(move);
                
                timestep++;
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private Snake parseSnake(String snakeLine, int index) {
        String[] parts = snakeLine.split(" ");
        boolean alive = parts[0].equals("alive");
        
        if (!alive) {
            return new Snake(index, false, 0, new ArrayList<>());
        }
        
        int length = Integer.parseInt(parts[1]);
        
        List<Point> body = new ArrayList<>();
        for (int i = 3; i < parts.length; i++) {
            String[] coords = parts[i].split(",");
            body.add(new Point(Integer.parseInt(coords[0]), Integer.parseInt(coords[1])));
        }
        
        return new Snake(index, alive, length, body);
    }

    private void buildCollisionGrid() {
        grid = new boolean[boardWidth][boardHeight];
        
        // Mark all living snake bodies as occupied
        for (Snake snake : snakes) {
            if (snake.alive && !snake.body.isEmpty()) {
                markSnakeOnGrid(snake);
            }
        }
    }

    private void markSnakeOnGrid(Snake snake) {
        if (snake.body.size() < 2) {
            if (!snake.body.isEmpty()) {
                Point p = snake.body.get(0);
                if (isValid(p.x, p.y)) {
                    grid[p.x][p.y] = true;
                }
            }
            return;
        }

        // Reconstruct full snake body from segments
        for (int i = 0; i < snake.body.size() - 1; i++) {
            Point from = snake.body.get(i);
            Point to = snake.body.get(i + 1);
            
            if (isValid(from.x, from.y)) {
                grid[from.x][from.y] = true;
            }
            
            fillSegment(from, to);
        }
        
        // Mark the tail
        Point tail = snake.body.get(snake.body.size() - 1);
        if (isValid(tail.x, tail.y)) {
            grid[tail.x][tail.y] = true;
        }
    }

    private void fillSegment(Point from, Point to) {
        if (from.x == to.x) {
            // Vertical line
            int startY = Math.min(from.y, to.y);
            int endY = Math.max(from.y, to.y);
            for (int y = startY + 1; y < endY; y++) {
                if (isValid(from.x, y)) {
                    grid[from.x][y] = true;
                }
            }
        } else if (from.y == to.y) {
            // Horizontal line
            int startX = Math.min(from.x, to.x);
            int endX = Math.max(from.x, to.x);
            for (int x = startX + 1; x < endX; x++) {
                if (isValid(x, from.y)) {
                    grid[x][from.y] = true;
                }
            }
        }
    }

    private int decideBestMove(Snake mySnake) {
        Point head = mySnake.body.get(0);
        
        // Get all safe moves
        List<Integer> safeMoves = new ArrayList<>();
        for (int dir = 0; dir < 4; dir++) {
            if (isSafeMove(head, dir)) {
                safeMoves.add(dir);
            }
        }
        
        // If no safe moves, pick randomly
        if (safeMoves.isEmpty()) {
            return new Random().nextInt(4);
        }
        
        // Check if apple has been alive for less than 5 seconds (50 timesteps at 0.1s each)
        int appleAge = timestep - appleTimestamp;
        boolean shouldPursueApple = appleAge < 50;
        
        // If we should pursue the apple, use A* to find path
        if (shouldPursueApple) {
            List<Point> path = findPathAStar(head, apple);
            
            if (path != null && path.size() > 1) {
                Point nextStep = path.get(1);
                
                // Find which direction leads to this next step
                for (int dir : safeMoves) {
                    Point newPos = new Point(head.x + dx[dir], head.y + dy[dir]);
                    if (newPos.equals(nextStep)) {
                        return dir;
                    }
                }
            }
        }
        
        // Apple is too old or no path found - just pick any safe move
        return safeMoves.get(0);
    }

    private boolean isSafeMove(Point head, int direction) {
        Point newPos = new Point(head.x + dx[direction], head.y + dy[direction]);
        
        // Check boundaries
        if (!isValid(newPos.x, newPos.y)) {
            return false;
        }
        
        // Check if hitting any snake body
        if (grid[newPos.x][newPos.y]) {
            return false;
        }
        
        // Check for one-block buffer from other snake heads
        for (Snake enemy : snakes) {
            if (enemy.index == mySnakeNum || !enemy.alive || enemy.body.isEmpty()) {
                continue;
            }
            
            Point enemyHead = enemy.body.get(0);
            
            // Check if enemy head could move to the same position as us
            for (int enemyDir = 0; enemyDir < 4; enemyDir++) {
                Point enemyNewPos = new Point(enemyHead.x + dx[enemyDir], enemyHead.y + dy[enemyDir]);
                
                if (enemyNewPos.equals(newPos)) {
                    return false; // Enemy could move here - avoid collision
                }
            }
        }
        
        return true;
    }

    private List<Point> findPathAStar(Point start, Point goal) {
        if (start.equals(goal)) return Arrays.asList(start);
        
        PriorityQueue<AStarNode> openList = new PriorityQueue<>();
        Set<Point> closedSet = new HashSet<>();
        Map<Point, AStarNode> allNodes = new HashMap<>();
        
        AStarNode startNode = new AStarNode(start, 0, manhattanDistance(start, goal), null);
        openList.add(startNode);
        allNodes.put(start, startNode);
        
        while (!openList.isEmpty()) {
            AStarNode current = openList.poll();
            
            if (current.pos.equals(goal)) {
                return reconstructPath(current);
            }
            
            closedSet.add(current.pos);
            
            for (int dir = 0; dir < 4; dir++) {
                Point neighbor = new Point(current.pos.x + dx[dir], current.pos.y + dy[dir]);
                
                if (!isValid(neighbor.x, neighbor.y) || closedSet.contains(neighbor)) {
                    continue;
                }
                
                // Can move through apple position, but not through snake bodies
                if (grid[neighbor.x][neighbor.y] && !neighbor.equals(goal)) {
                    continue;
                }
                
                double tentativeG = current.g + 1;
                AStarNode neighborNode = allNodes.get(neighbor);
                
                if (neighborNode == null) {
                    neighborNode = new AStarNode(neighbor, tentativeG, manhattanDistance(neighbor, goal), current);
                    allNodes.put(neighbor, neighborNode);
                    openList.add(neighborNode);
                } else if (tentativeG < neighborNode.g) {
                    neighborNode.g = tentativeG;
                    neighborNode.f = tentativeG + neighborNode.h;
                    neighborNode.parent = current;
                }
            }
        }
        
        return null; // No path found
    }

    private List<Point> reconstructPath(AStarNode node) {
        List<Point> path = new ArrayList<>();
        while (node != null) {
            path.add(0, node.pos);
            node = node.parent;
        }
        return path;
    }

    private boolean isValid(int x, int y) {
        return x >= 0 && x < boardWidth && y >= 0 && y < boardHeight;
    }

    private double manhattanDistance(Point a, Point b) {
        return Math.abs(a.x - b.x) + Math.abs(a.y - b.y);
    }

    // Helper classes
    private static class Snake {
        int index;
        boolean alive;
        int length;
        List<Point> body;
        
        Snake(int index, boolean alive, int length, List<Point> body) {
            this.index = index;
            this.alive = alive;
            this.length = length;
            this.body = body;
        }
    }

    private static class Point {
        int x, y;
        
        Point(int x, int y) {
            this.x = x;
            this.y = y;
        }
        
        @Override
        public boolean equals(Object obj) {
            if (!(obj instanceof Point)) return false;
            Point p = (Point) obj;
            return x == p.x && y == p.y;
        }
        
        @Override
        public int hashCode() {
            return x * 1000 + y;
        }
    }

    private static class AStarNode implements Comparable<AStarNode> {
        Point pos;
        double g, h, f;
        AStarNode parent;
        
        AStarNode(Point pos, double g, double h, AStarNode parent) {
            this.pos = pos;
            this.g = g;
            this.h = h;
            this.f = g + h;
            this.parent = parent;
        }
        
        @Override
        public int compareTo(AStarNode other) {
            return Double.compare(this.f, other.f);
        }
    }
}