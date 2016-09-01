import org.apache.zookeeper.*;
import org.apache.zookeeper.data.Stat;

import java.io.IOException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * 类Master.java的实现描述：TODO 类实现描述 
 * @author yangqi Jan 1, 2014 1:37:01 PM
 */
public class Master implements Watcher, Runnable {

    private ZooKeeper zk;

    private String    connectString;

    private String    serverId;

    private static final String MASTER_PATH = "/master";

    public Master(String connectString,String serverId) {
        this.connectString = connectString;
        this.serverId = serverId;
    }

    public void process(WatchedEvent event) {
        System.out.println(event);
    }

    public void startZK() {
        try {
            zk = new ZooKeeper(connectString, 2000, this);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void stopZK() {
        try {
            zk.close();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    public void createMaterNode(){
        try {
            zk.create(MASTER_PATH, serverId.getBytes(), ZooDefs.Ids.OPEN_ACL_UNSAFE,
                    CreateMode.EPHEMERAL);
        } catch (KeeperException e) {
            e.printStackTrace();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    public boolean checkForMaster() {

        Stat stat=new Stat();
        byte[] data = null;
        try {
            data = zk.getData(MASTER_PATH, false, stat);
            return serverId.equals(new String(data));
        } catch (KeeperException e) {
            e.printStackTrace();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        return Boolean.FALSE;
    }

    public boolean registerForMaster() {

        boolean isLeader = false;
        while (true) {
            if (!checkForMaster()) {
                createMaterNode();
                sleep(5);
            } else {
                isLeader = true;
                log(" master registered with ");
                break;
            }
        }
        return isLeader;
    }


    public void run() {

        startZK();

        boolean isLeader = registerForMaster();
        if (isLeader) {
            stopZK();
        }

    }

    private void sleep(int seconds) {
        try {
            Thread.sleep(1000 * seconds);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    private void log(String msg) {
        System.out.println(String.format("serverId %s %s", serverId, msg));
    }

    public static void main(String[] args) throws InterruptedException {
        int masterCount = 3;
        ExecutorService service = Executors.newFixedThreadPool(masterCount);
        for (int i = 0; i < masterCount; i++) {
            Master master = new Master("10.235.100.22:2181", "o2-" + i);
            service.submit(master);
        }

    }
}