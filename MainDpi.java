package dpi;
public class MainDpi {
 public static void main(String[] args)throws Exception{
  if(args.length<2){usage();return;}DpiEngine.Config c=new DpiEngine.Config();
  for(int i=2;i<args.length;i++){switch(args[i]){case"--block-ip"->c.rulesFile=null;case"--block-app"->{if(i+1<args.length){i++;}}case"--rules"->{if(i+1<args.length)c.rulesFile=args[++i];}case"--lbs"->{c.lbs=Integer.parseInt(args[++i]);}case"--fps"->{c.fpsPerLb=Integer.parseInt(args[++i]);}case"--verbose"->c.verbose=true;}}
  DpiEngine e=new DpiEngine(c);
  for(int i=2;i<args.length;i++)if("--block-ip".equals(args[i])&&i+1<args.length)e.rules().blockIP(args[i+1]);
  for(int i=2;i<args.length;i++)if("--block-domain".equals(args[i])&&i+1<args.length)e.rules().blockDomain(args[i+1]);
  e.processFile(args[0],args[1]);System.out.println(e.report());
 }
 static void usage(){System.out.println("""
╔══════════════════════════════════════════════════════════════╗
║                    DPI ENGINE v1.0                          ║
║               Deep Packet Inspection System                 ║
╚══════════════════════════════════════════════════════════════╝
Usage: java dpi.MainDpi <input.pcap> <output.pcap> [options]
Options:
  --block-ip <ip>        Block packets from source IP
  --block-domain <dom>   Block domain (supports *.example.com)
  --rules <file>         Load blocking rules
  --lbs <n>              Number of load balancer threads (default: 2)
  --fps <n>              FP threads per LB (default: 2)
  --verbose              Enable verbose output
""");}
}