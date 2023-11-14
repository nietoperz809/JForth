package jforth;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.Map;
import java.util.TreeMap;
import java.util.function.Function;

public class LSystem {
    final TreeMap<String, String> m_map = new TreeMap<>
            (
                    // largest key comes first, greedy parser
                    Comparator.comparingInt(String::length).reversed()
                            .thenComparing(Function.identity())
            );
    String m_str;
    String save;
    String lastResult;

    public LSystem(String in) {
        setMaterial(in);
    }

    public String setAndRunFullSystem2(ArrayList<String> system) throws Exception{
        clrRules();
        setMaterial(system.get(0));
        for (int s = 1; s<system.size()-1; s++) {
            putRule(system.get(s));
        }
        int iter = Integer.parseInt(system.get(system.size()-1));
        StringBuilder allres = new StringBuilder();
        allres.append("\"").append(system.get(0)).append("\"");
        String res = doIt();
        allres.append(",\"").append(res).append("\"");
        while (iter > 0) {
            iter--;
            res = doNext();
            allres.append(",\"").append(res).append("\"");
        }
        return "{"+allres+"}";
    }

    public String setAndRunFullSystem(ArrayList<String> system) throws Exception{
        clrRules();
        setMaterial(system.get(0));
        for (int s = 1; s<system.size()-1; s++) {
            putRule(system.get(s));
        }
        int iter = Integer.parseInt(system.get(system.size()-1));
        String res = null;
        doIt();
        while (iter > 0) {
            iter--;
            res = doNext();
        }
        return res;
    }

    public LSystem() {
    }

//    public static void main(String[] args) throws Exception {
//        LSystem ch = new LSystem("A");
//
//        ch.putRule("A->AB");
//        ch.putRule("B->A");
//
//        String s = ch.doIt();
//        System.out.println(s);
//        s = ch.doNext();
//        System.out.println(s);
//        s = ch.doNext();
//        System.out.println(s);
//        s = ch.doNext();
//        System.out.println(s);
//    }

    public void setMaterial(String in) {
        m_str = in;
        save = in;
    }

    public void clrRules() {
        m_map.clear();
    }

    public void putRule(String from, String to) throws Exception {
        from = from.trim();
        if (to != null)
            to = to.trim();
        if (from.isEmpty())
            throw new Exception("Rule error");
        m_map.put(from, to);
    }

    public void putRule(String rule) throws Exception {
        String[] sp = rule.split("->");
        if (sp.length == 1) {
            putRule(sp[0], null);
            return;
        }
        if (sp.length != 2)
            throw new Exception("Not a valid rule! " + rule);
        putRule(sp[0], sp[1]);
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append(m_str).append('\n');
        for (Map.Entry<String, String> mapElement : m_map.entrySet()) {
            sb.append(mapElement.getKey()).append("->");
            sb.append(mapElement.getValue()).append('\n');
        }
        return sb.toString();
    }

    public String doIt() {
        StringBuilder bui = new StringBuilder();

        while (!m_str.isEmpty()) {
            boolean found;
            do {
                found = false;
                for (Map.Entry<String, String> mapElement : m_map.entrySet()) {
                    String k = mapElement.getKey();
                    if (m_str.startsWith(k)) {
                        String v = mapElement.getValue();
                        if (v != null)
                            bui.append(v);
                        m_str = m_str.substring(k.length());
                        found = true;
                        break;
                    }
                }
            } while (found);

            if (!m_str.isEmpty()) {
                bui.append(m_str.charAt(0));
                m_str = m_str.substring(1);
            }
        }
        m_str = save; // restore source string

        lastResult = bui.toString();
        return lastResult;
    }

    public String doNext() {
        m_str = lastResult;
        save = lastResult;
        return doIt();
    }
}

