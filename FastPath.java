package dpi;
import java.util.*;import java.util.concurrent.atomic.*;

public class FastPath implements Runnable {
 private final int id;private final ThreadSafeQueue<Types.PacketJob> q;private final RuleManager rules;private final ConnectionTracker tracker=new ConnectionTracker(100000);private final java.util.function.BiConsumer<Types.PacketJob,Types.PacketAction> output;
 private final AtomicBoolean running=new AtomicBoolean();private Thread thread;private long processed,forwarded,dropped,sniHits,classHits;
 public FastPath(int id,RuleManager r,java.util.function.BiConsumer<Types.PacketJob,Types.PacketAction> out,int qs){this.id=id;rules=r;output=out;q=new ThreadSafeQueue<>(qs);}
 public ThreadSafeQueue<Types.PacketJob> queue(){return q;} public void start(){if(running.getAndSet(true))return;thread=new Thread(this,"FP-"+id);thread.start();System.out.println("[FP"+id+"] Started");}
 public void stop(){running.set(false);q.shutdown();if(thread!=null)try{thread.join(1000);}catch(Exception ignored){}System.out.println("[FP"+id+"] Stopped (processed "+processed+" packets)");}
 public void run(){while(running.get()||q.size()>0){try{Types.PacketJob j=q.poll(100);if(j==null)continue;process(j);}catch(Exception e){if(running.get())e.printStackTrace();}}}
 private void process(Types.PacketJob j){processed++;var p=j.parsed;var t=new Types.FiveTuple(p.srcIp,p.destIp,p.srcPort,p.destPort,p.protocol);var c=tracker.getOrCreate(t);tracker.update(c,p.raw.length,false);
   String domain=null;Types.AppType app=Types.AppType.UNKNOWN;byte[] payload=p.payload();
   if(p.hasTcp&&p.destPort==443){var s=SniExtractor.extract(payload);if(s.isPresent()){domain=s.get();app=Types.sniToAppType(domain);sniHits++;}}
   if(app==Types.AppType.UNKNOWN&&p.hasTcp&&(p.destPort==80||p.srcPort==80)){var h=SniExtractor.httpHost(payload);if(h.isPresent()){domain=h.get();app=Types.AppType.HTTP;}}
   if(app==Types.AppType.UNKNOWN&&p.hasUdp&&(p.destPort==53||p.srcPort==53)){var d=SniExtractor.dnsQuery(payload);if(d.isPresent()){domain=d.get();app=Types.AppType.DNS;}}
   if(app!=Types.AppType.UNKNOWN||domain!=null){tracker.classify(c,app,domain);classHits++;}
   var reason=rules.shouldBlock(p.srcIp,p.destPort,app,domain);var action=reason.isPresent()?Types.PacketAction.DROP:Types.PacketAction.FORWARD;
   if(action==Types.PacketAction.DROP){dropped++;tracker.block(c);System.out.println("[FP"+id+"] DROP packet #"+j.id+" reason="+reason.get().type()+" "+reason.get().detail());}
   else {forwarded++;output.accept(j,action);}
 }
 public String stats(){return "FP"+id+": processed="+processed+", forwarded="+forwarded+", dropped="+dropped+", SNI="+sniHits;}
}