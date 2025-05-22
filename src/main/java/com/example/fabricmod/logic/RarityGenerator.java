package com.example.fabricmod.logic;

import com.example.fabricmod.util.Adler32;
import java.util.Random;

public class RarityGenerator {
    public static class RarityResult {
        private final String tierLine, certLine;
        public RarityResult(String tierLine, String certLine) { this.tierLine = tierLine; this.certLine = certLine; }
        public String getTierLine() { return tierLine; }
        public String getCertLine() { return certLine; }
    }

    private static final Random RANDOM = new Random();
    private static final Tier[] TIERS = {
        new Tier("Garbage", 30, "§7"),
        new Tier("Common", (70-0.001)/6, "§6"),
        new Tier("Uncommon", (70-0.001)/6, "§a"),
        new Tier("Rare", (70-0.001)/6, "§9"),
        new Tier("Epic", (70-0.001)/6, "§d"),
        new Tier("Legendary", (70-0.001)/6, "§c"),
        new Tier("Mythic", (70-0.001)/6, "§5"),
        new Tier("Cosmic Perfection", 0.001, null)
    };
    private static final double[] THRESHOLDS;
    private static final int TOTAL_WEIGHT;
    static {
        THRESHOLDS = new double[TIERS.length];
        double cum=0;
        for(int i=0;i<TIERS.length;i++){ cum+=TIERS[i].prob; THRESHOLDS[i]=cum; }
        int sum=0; for(int n=1;n<=100;n++) sum+=n;
        TOTAL_WEIGHT=sum;
    }

    public static RarityResult generateRarity(String playerName, String itemName) {
        double r=RANDOM.nextDouble()*100;
        Tier chosen=TIERS[TIERS.length-1];
        for(int i=0;i<THRESHOLDS.length;i++){
            if(r<=THRESHOLDS[i]){ chosen=TIERS[i]; break; }
        }
        double r2=RANDOM.nextDouble()*TOTAL_WEIGHT; int cum=0, quality=100;
        for(int n=1;n<=100;n++){ cum+=n; if(r2<=cum){ quality=n; break; } }
        String ts=java.time.LocalDateTime.now().format(java.time.format.DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));
        int hash=Adler32.compute(itemName+ts);
        String tierName = chosen.name.equals("Cosmic Perfection") ? randomColorize(chosen.name) : chosen.color+chosen.name;
        return new RarityResult(tierName+" "+quality, "§7Cert: "+String.format("%08X",hash));
    }
    private static String randomColorize(String s){
        String[] colors={"§c","§6","§e","§b","§a","§d"}; StringBuilder sb=new StringBuilder();
        for(char c: s.toCharArray()) sb.append(colors[RANDOM.nextInt(colors.length)]).append(c);
        return sb.toString();
    }
    private static class Tier{ String name; double prob; String color; Tier(String n,double p,String c){name=n;prob=p;color=c;} }
}
