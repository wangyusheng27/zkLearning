import org.apache.log4j.Logger;
import org.apache.zookeeper.KeeperException;
import org.apache.zookeeper.WatchedEvent;
import org.apache.zookeeper.Watcher;
import org.apache.zookeeper.ZooKeeper;

import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

/**
 * Created by wys on 2016/8/17.
 */
public class Executor implements Watcher, Runnable, DataMonitor.DataMonitorListener{
    String filename;
    String[] exec;
    ZooKeeper zk;
    DataMonitor dm;
    Process child;
    static Logger logger = Logger.getLogger(Executor.class);
    public Executor(String hostPort, String znode, String filename,
                    String exec[]) throws KeeperException, IOException {
        this.filename = filename;
        this.exec = exec;
        zk = new ZooKeeper(hostPort, 3000, this);
        dm = new DataMonitor(zk, znode, null, this);
    }

    public static void main(String[] args) {
        logger.info("sdfdsf");
        String hostPort = "10.235.100.17:2181";
        String znode = "/idea";
        String filename="D:/1.txt";
        String exec[] =  { "cmd", "/c"};
        int i = 1;
        try {
            new Thread(new Executor(hostPort, znode, filename, exec)).start();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }


    public void run() {
        try {
            synchronized (this) {
                while (!dm.dead) {
                    wait();
                }
            }
        } catch (InterruptedException e) {
        }
    }

    public void process(WatchedEvent watchedEvent) {
        System.out.println("step 0");
        dm.process(watchedEvent);
    }

    //implements from DataMonitorListener
    public void exists( byte[] data ) {
        if (data == null) {
            if (child != null) {
                System.out.println("Killing process");
                child.destroy();
                try {
                    child.waitFor();
                } catch (InterruptedException e) {
                }
            }
            child = null;
        } else {
            if (child != null) {
                System.out.println("Stopping child");
                child.destroy();
                try {
                    child.waitFor();
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
            try {
                FileOutputStream fos = new FileOutputStream(filename);
                fos.write(data);
                fos.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
            try {
                System.out.println("Starting child");
                child = Runtime.getRuntime().exec(exec);
                new StreamWriter(child.getInputStream(), System.out);
                new StreamWriter(child.getErrorStream(), System.err);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
    //implements from DataMonitorListener
    public void closing(int rc) {
        synchronized (this) {
            notifyAll();
        }
    }
    static class StreamWriter extends Thread {
        OutputStream os;

        InputStream is;

        StreamWriter(InputStream is, OutputStream os) {
            this.is = is;
            this.os = os;
            start();
        }

        public void run() {
            byte b[] = new byte[80];
            int rc;
            try {
                while ((rc = is.read(b)) > 0) {
                    os.write(b, 0, rc);
                }
            } catch (IOException e) {
            }

        }
    }
}
