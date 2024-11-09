import org.apache.zookeeper.WatchedEvent;
import org.apache.zookeeper.Watcher;
import org.apache.zookeeper.ZooKeeper;
import java.io.IOException;

public class LeaderElection implements Watcher {
    private static final String ZOOKEEPER_ADDRESS = "localhost:2181";
    private static final int SESSION_TIMEOUT = 3000;
    private ZooKeeper zooKeeper;

    public void connectToZookeeper() throws IOException {
        // Register itself as a connection watcher
        this.zooKeeper = new ZooKeeper(ZOOKEEPER_ADDRESS, SESSION_TIMEOUT, this);
    }

    @Override
    public void process(WatchedEvent watchedEvent) {
        switch (watchedEvent.getType()) {
            case None:
                if (watchedEvent.getState() == Event.KeeperState.SyncConnected) {
                    System.out.println("Connected to Zookeeper successfully");
                } else {
                    synchronized (zooKeeper) {
                        zooKeeper.notify();
                    }
                }
                break;
        }
    }

    public void run() throws InterruptedException {
        synchronized (zooKeeper) {
            System.out.println("Start running zookeeper connection");
            zooKeeper.wait();
        }
    }

    public void close() throws InterruptedException {
        System.out.println("Stop zookeeper connection");
        synchronized (zooKeeper) {
            zooKeeper.close();
        }
    }

    public static void main(String[] args) throws IOException, InterruptedException {
        LeaderElection leaderElection = new LeaderElection();
        leaderElection.connectToZookeeper();
        leaderElection.run();
        leaderElection.close();
    }
}
