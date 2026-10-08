package dpi;
import java.io.*;import java.util.*;import java.util.concurrent.*;

public class RuleManager {
 public record BlockReason(String type,String detail){}
 private final Set<Long> ips=ConcurrentHashMap.newKeySet(); private final Set<Types.AppType> apps=ConcurrentHashMap.newKeySet();
 private final Set<String> domains=ConcurrentHashMap.newKeySet(); private final Set<Integer> ports=ConcurrentHashMap.newKeySet();
 public void blockIP(String s){ips.add(ip(s));System.out.println("[RuleManager] Blocked IP: "+s);} public void unblockIP(String s){ips.remove(ip(s));}
 public boolean isIPBlocked(long i){return ips.contains(i);} public List<String> getBlockedIPs(){return ips.stream().map(PacketParser::ipToString).toList();}
 public void blockApp(Types.AppType a){apps.add(a);} public void unblockApp(Types.AppType a){apps.remove(a);} public boolean isAppBlocked(Types.AppType a){return apps.contains(a);}
 public List<Types.AppType> getBlockedApps(){return List.copyOf(apps);}
 public void blockDomain(String d){domains.add(d.toLowerCase(Locale.ROOT));} public void unblockDomain(String d){domains.remove(d.toLowerCase(Locale.ROOT));}
 public boolean isDomainBlocked(String d){if(d==null)return false;String x=d.toLowerCase(Locale.ROOT);for(String r:domains){if(r.startsWith("*.")){if(x.endsWith(r.substring(1)))return true;}else if(x.equals(r)||x.endsWith("."+r))return true;}return false;}
 public void blockPort(int p){ports.add(p);} public void unblockPort(int p){ports.remove(p);} public boolean isPortBlocked(int p){return ports.contains(p);}
 public Optional<BlockReason> shouldBlock(long src,int dst,Types.AppType app,String dom){if(isIPBlocked(src))return Optional.of(new BlockReason("IP",PacketParser.ipToString(src)));if(isAppBlocked(app))return Optional.of(new BlockReason("APP",Types.appTypeToString(app)));if(isDomainBlocked(dom))return Optional.of(new BlockReason("DOMAIN",dom));if(isPortBlocked(dst))return Optional.of(new BlockReason("PORT",String.valueOf(dst)));return Optional.empty();}
 public boolean saveRules(String file){try(PrintWriter w=new PrintWriter(file)){for(long x:ips)w.println("block-ip "+PacketParser.ipToString(x));for(var a:apps)w.println("block-app "+Types.appTypeToString(a));for(String d:domains)w.println("block-domain "+d);for(int p:ports)w.println("block-port "+p);return true;}catch(Exception e){return false;}}
 public boolean loadRules(String file){try{for(String l:java.nio.file.Files.readAllLines(java.nio.file.Path.of(file))){String[] a=l.trim().split("\\s+",2);if(a.length<2)continue;switch(a[0].toLowerCase()){case"block-ip"->blockIP(a[1]);case"block-app"->blockApp(parseApp(a[1]));case"block-domain"->blockDomain(a[1]);case"block-port"->blockPort(Integer.parseInt(a[1]));}}return true;}catch(Exception e){return false;}}
 public void clearAll(){ips.clear();apps.clear();domains.clear();ports.clear();} public String stats(){return "IPs="+ips.size()+", Apps="+apps.size()+", Domains="+domains.size()+", Ports="+ports.size();}
 private static long ip(String s){String[]a=s.split("\\.");long r=0;for(int i=0;i<4;i++)r|=(Long.parseLong(a[i])&255L)<<(8*i);return r;} private static Types.AppType parseApp(String s){for(var a:Types.AppType.values())if(Types.appTypeToString(a).equalsIgnoreCase(s)||a.name().equalsIgnoreCase(s))return a;return Types.AppType.UNKNOWN;}
}