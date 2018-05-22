package routine;

import javax.swing.*;
import java.io.File;
import java.io.FileNotFoundException;
import java.lang.reflect.Array;
import java.net.Inet6Address;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.*;

public class RuleParser {
    public static String transformAdress(String address) {
        StringTokenizer str = new StringTokenizer(address, "/.");
        ArrayList<Integer> values = new ArrayList<>();
        while (str.hasMoreTokens()) {
            String st = str.nextToken();
            if (st.startsWith("0x")) {
                values.add(Integer.decode(st));
            } else {
                values.add(Integer.parseInt(st));
            }
        }


        String resExact = "";

        for (int i = 0; i < values.size() - 1; i++) {
            int k = values.get(i);
            String cur = Integer.toBinaryString(k + (1 << 8));
            resExact += cur.substring(1);
        }

        String res = "";
        for (int i = 0; i < resExact.length(); i++) {
            res += (i < values.get(values.size() - 1)) ? resExact.charAt(i) : "*";
        }
        return res;
    }

    public static String transformField(String str) {
        String[] tokens = str.split("/");
        String a = Integer.toBinaryString(Integer.decode(tokens[0]) + (1 << 16)).substring(1);
        String b = Integer.toBinaryString(Integer.decode(tokens[1]) + (1 << 16)).substring(1);
        String ans = "";
        for (int i = 0; i < a.length(); i++) {
            if (b.charAt(i) == '1') {
                ans += a.charAt(i);
            } else {
                ans += "*";
            }
        }
        return ans;
    }

    public static ArrayList<Rule> parseClassifier(File f) throws FileNotFoundException {
        Scanner in = new Scanner(f);
        ArrayList<Rule> ans = new ArrayList<>();
        int cntK = 0;
        while (in.hasNext()) {
            String str = in.nextLine().substring(1);
            if (str.length() == 0) {
                continue;
            }

            String[] tokens = str.split("\\s+");

            String prefix = transformAdress(tokens[0]) + transformAdress(tokens[1]) + transformField(tokens[2]);
            // transform(tokens[2]+"/8");


            ArrayList<String> inExact = new ArrayList<>();
            ArrayList<String> outExact = new ArrayList<>();


            boolean isOutToken = false;
            for (int i = 3; i < tokens.length; i++) {
                String token = new StringTokenizer(tokens[i], "[] \t\',").nextToken();
                if (!isOutToken) {
                    inExact.add(token);
                } else {
                    outExact.add(token);
                }
                isOutToken |= tokens[i].contains("]");
            }


            cntK++;
            for (String inE : inExact) {
                for (String outE : outExact) {
                    ans.add(new Rule(prefix + inE + outE, cntK));
                }
            }
        }
        return ans;
    }

    public static ArrayList<Rule> parseClassifier(String... f) throws FileNotFoundException {
        if (f.length == 1) {
            return parseClassifier(new File(f[0]));
        }
        ArrayList<Rule>[] rules = new ArrayList[f.length];
        for (int i = 0; i < f.length; i++) {
            rules[i] = parseClassifier(new File(f[i]));
        }
        int sumLen = 0;
        for (int i = 0; i < f.length; i++) {
            rules[i] = ClassifierConstruct.cutWithManyStars(rules[i]);
            sumLen += rules[i].size();
        }
        ArrayList<Rule> ans = new ArrayList<Rule>();
        Random rnd = new Random("for classifier join".hashCode());
        int[] point = new int[f.length];
        while (ans.size() < sumLen) {
            int k = rnd.nextInt(f.length);
            if (point[k] < rules[k].size()) {
                ans.add(rules[k].get(point[k]));
                point[k]++;
            }
        }
        System.err.println(ans.size());
        ans = Optimizator.applyAllForwardSubsumtions(ans);
        System.err.println(ans.size());
        return ans;
    }


    public static ArrayList<Rule> parseIpClassifier(File f) throws FileNotFoundException, UnknownHostException {
        System.out.println(f.toString());
        Scanner in = new Scanner(f);
        ArrayList<Rule> ans = new ArrayList<>();
        HashMap<String, Integer> actionMap = new HashMap<String, Integer>();
        while (in.hasNext()) {
            String str = in.nextLine();
            String[] tokens = str.split("\\s+");
            String[] addrMask = tokens[0].split("/");

            byte[] parseBytes = InetAddress.getByName(addrMask[0]).getAddress();
            String ruleWithoutStar = "";
            for (int i = 0; i < parseBytes.length; i++) {
                String strK = Integer.toBinaryString(((int) parseBytes[i] & 0xFF) + 0x100).substring(1);
                ruleWithoutStar += strK;
            }
            String rule = "";
            for (int i = 0; i < ruleWithoutStar.length(); i++) {
                if (i < Integer.parseInt(addrMask[1])) {
                    rule += ruleWithoutStar.charAt(i);
                } else {
                    rule += '*';
                }
            }
            if (!actionMap.containsKey(tokens[1])) {
                actionMap.put(tokens[1], tokens[1].equals("drop") ? -1 : actionMap.size());
            }
            Rule r = new Rule(rule, ans.size());
            r.actions.clear();
            r.actions.add(actionMap.get(tokens[1]));
            ans.add(r);
        }
        System.err.println(ans.size());
        Collections.sort(ans, Comparator.comparingInt(o -> o.stars));
        System.err.println(ans.size());
        return ans;
    }

}
