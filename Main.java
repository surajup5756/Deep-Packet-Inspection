package dpi;
import java.io.*;import java.time.*;import java.time.format.*;

public class Main {
 public static void main(String[] args)throws Exception{
  if(args.length<1){System.out.println("Usage: java dpi.Main <pcap_file>");return;}
  PcapReader r=new PcapReader();if(!r.open(args[0]))return;PcapReader.RawPacket raw;int n=0;
  while((raw=r.next())!=null){n++;Types.ParsedPacket p=new Types.ParsedPacket();if(!PacketParser.parse(raw,p))continue;print(p,n);}
  r.close();System.out.println("\\nTotal packets: "+n);
 }
 static void print(Types.ParsedPacket p,int n){System.out.println("\\n========== Packet #"+n+" ==========");System.out.printf("Time: %d.%06d%n",p.timestampSec,p.timestampUsec);
  System.out.println("\\n[Ethernet]");System.out.println("  Source MAC:      "+p.srcMac);System.out.println("  Destination MAC: "+p.destMac);System.out.printf("  EtherType:       0x%04x%n",p.etherType);
  if(p.hasIp){System.out.println("\\n[IP]");System.out.println("  Source:          "+PacketParser.ipToString(p.srcIp));System.out.println("  Destination:     "+PacketParser.ipToString(p.destIp));System.out.println("  Protocol:        "+PacketParser.protocolToString(p.protocol));
   if(p.hasTcp||p.hasUdp){System.out.println("\\n[Transport]");System.out.println("  Source Port:     "+p.srcPort);System.out.println("  Destination Port:"+p.destPort);if(p.hasTcp)System.out.println("  TCP Flags:       "+PacketParser.tcpFlagsToString(p.tcpFlags));}
  }System.out.println("  Payload Length:  "+p.payloadLength);
 }
}