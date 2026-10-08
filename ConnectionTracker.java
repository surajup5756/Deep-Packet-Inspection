package dpi;
import java.util.*;import java.util.concurrent.*;

public class ConnectionTracker {
 private final Map<Types.FiveTuple,Types.Connection> map=new ConcurrentHashMap<>(); private final int max; private long total,classified,blocked;
 public ConnectionTracker(int max){this.max=max;}
 public synchronized Types.Connection getOrCreate(Types.FiveTuple t){return map.computeIfAbsent(t,k->{if(map.size()>=max)map.entrySet().stream().min(Comparator.comparingLong(e->e.getValue().lastSeen)).ifPresent(e->map.remove(e.getKey()));total++;Types.Connection c=new Types.Connection();c.tuple=t;return c;});}
 public void update(Types.Connection c,int bytes,boolean out){if(out){c.packetsOut++;c.bytesOut+=bytes;}else{c.packetsIn++;c.bytesIn+=bytes;}c.lastSeen=System.nanoTime();}
 public void classify(Types.Connection c,Types.AppType a,String s){if(c.state!=Types.ConnectionState.CLASSIFIED){classified++;}c.appType=a;c.sni=s==null?"":s;c.state=Types.ConnectionState.CLASSIFIED;}
 public void block(Types.Connection c){if(c.state!=Types.ConnectionState.BLOCKED)blocked++;c.state=Types.ConnectionState.BLOCKED;c.action=Types.PacketAction.DROP;}
 public Collection<Types.Connection> all(){return List.copyOf(map.values());}
 public long total(){return total;} public long classified(){return classified;} public long blocked(){return blocked;} public int active(){return map.size();}
 public void clear(){map.clear();}
}