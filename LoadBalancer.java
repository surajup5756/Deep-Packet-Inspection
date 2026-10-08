package dpi;
import java.util.*;import java.util.concurrent.atomic.*;

public class LoadBalancer implements Runnable {
 private final int id,start;private final List<ThreadSafeQueue<Types.PacketJob>> fps;private final ThreadSafeQueue<Types.PacketJob> q;private Thread t;private volatile boolean running;private long received,dispatched;
 public LoadBalancer(int id,List<ThreadSafeQueue<Types.PacketJob>> f,int start,int qs){this.id=id;this.fps=f;this.start=start;q=new ThreadSafeQueue<>(qs);}
 public ThreadSafeQueue<Types.PacketJob> queue(){return q;} public void start(){running=true;t=new Thread(this,"LB-"+id);t.start();System.out.println("[LB"+id+"] Started (serving FP"+start+"-FP"+(start+fps.size()-1)+")");}
 public void stop(){running=false;q.shutdown();try{if(t!=null)t.join(1000);}catch(Exception ignored){}}
 public void run(){while(running||q.size()>0){try{var j=q.poll(100);if(j==null)continue;received++;var p=j.parsed;long h=(new Types.FiveTuple(p.srcIp,p.destIp,p.srcPort,p.destPort,p.protocol)).hashCode()&0x7fffffff;fps.get((int)(h%fps.size())).push(j);dispatched++;}catch(Exception e){if(running)e.printStackTrace();}}}
 public long received(){return received;}public long dispatched(){return dispatched;}
}