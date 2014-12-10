package AsynchGHSSimulator;

import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.StreamTokenizer;
import java.util.ArrayList;
import java.util.Iterator;

public class MSTviewer implements Runnable {
  static final int MAX_NODES = 50; // limit on the number of nodes in graph
  boolean started; // true when the algorithm has been started up by user
  public static ArrayList<Node> nodes = new ArrayList<Node>();   // Node objects indexed by unique IDs
  static int edgeCount = 0; //number of edges
  static int  nodeCount ;   //number of nodes
  static int[][] connections ; //keeps track of edges between two nodes

  synchronized void setStarted(boolean b) {
    started = b;
  }

  synchronized boolean isStarted() {
    return started;
  }

  public MSTviewer() {
    //setStarted(true);
  }

  @Override
  public void run() {
    //while (true) {
    try {
      acceptRegistration();
      createEdges();
      startAlgorithm();
      //sendEdges();
      //waitForTermination();
    } catch (Exception e) {
      System.out.println(" stopped ");
      e.printStackTrace();
    }
    //}
  }
  /*
  * Method to initialize nodes 
  */
  void acceptRegistration() {
    //System.out.print(nodeCount);
    for (int i = 0; i < nodeCount; i++) {
      if ((nodes.size() < MAX_NODES + 1 || nodes.size() <= 3) && !isStarted()) {
        try {
          nodes.add(new Node(nodes.size()));
          //System.out.print(nodes.size());
        } catch (Exception e) {
          System.out.println(e);
        }
      }
    }
    //System.out.print(nodes.size());
  }
  
  void createEdges() {
    // first, create a cycle through the graph to ensure connectedness
    for (int i = 0; i < nodeCount; i++) {
      for (int j = 0; j < nodeCount; j++) {
        if (connections[i][j] == 1) {
          new Edge(nodes.get(i), nodes.get(j));
          // To avoid creation of another edge from j to i. We can alter the array connections because
          // we don't need it anymore.
          connections[i][j] = 0;
          connections[j][i] = 0;
        }
      }

    }
  }

  void startAlgorithm() {
    Iterator it = nodes.iterator();
    while (it.hasNext()) {
      Node node = (Node) it.next();
      if (node != null) // skip dummy entry 0
        node.sendMessage(new Message(Message.WAKEUP, 100, node.UID));
      (new Thread(node)).start();
    }
  }

  void closeConnections() {
    Iterator it = nodes.iterator();
    while (it.hasNext()) {
      Node n = (Node) it.next();
      if (n != null) {
        n.closeConnection("normal termination");
      }
    }
  }
  /*void waitForTermination() {
    while (isStarted())
      synchronized(endButton) {
        try {endButton.wait();} catch (InterruptedException ie) {}
      }
    System.out.println("\n SESSION TERMINATED BY USER \n");
  }*/

  public static void main(String[] args) {
    int[] arrayIds = null;

    StreamTokenizer tokenizer = null;

    try{
      tokenizer = new StreamTokenizer(new FileReader("input.txt"));
      tokenizer.slashSlashComments(true);
      tokenizer.eolIsSignificant(false);
      tokenizer.nextToken();
      nodeCount = (int)tokenizer.nval;
      arrayIds = new int[nodeCount];
      connections = new int[nodeCount][nodeCount];

      for(int i=0;i<nodeCount;i++){
        tokenizer.nextToken();
        if (tokenizer.ttype == StreamTokenizer.TT_NUMBER){
          arrayIds[i] = (int)tokenizer.nval;
        }
      }
      for(int i=0;i<nodeCount;i++){
        for(int j=0;j<nodeCount;j++){
          tokenizer.nextToken();
          if (tokenizer.ttype == StreamTokenizer.TT_NUMBER){
            connections[i][j] = (int)tokenizer.nval;
          }
        }
      }
    }catch(FileNotFoundException e){
      System.out.println("Exception::" +e);
    } catch (IOException e) {
      System.out.println("Exception::" +e);
    }
    MSTviewer viewer = new MSTviewer();
    (new Thread(viewer)).start();
  }
}
