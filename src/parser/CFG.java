package parser;

import java.util.*;

/*
 * CFG: context-free grammar
 */
public class CFG {
	// productionsT: stores all production rules whose right side is a terminal
	HashMap<String, ArrayList<ProductionT>> productionsT = new HashMap<String, ArrayList<ProductionT>>();
	
	// productionsN : stores all production rules whose right side are two nonterminals
	HashMap<String, ArrayList<ProductionN>> productionsN = new HashMap<String, ArrayList<ProductionN>>();
	ArrayList<String> nonTerminals = new ArrayList<String>();

	public void addToProductionsT(String l, String r, float p) {
		if(!productionsT.containsKey(l)) {
			productionsT.put(l, new ArrayList<ProductionT>());
		}
		productionsT.get(l).add(new ProductionT(l, r, p));
		addNonTerminal(l);
	}
	public void addToProductionsN(String l, String r1, String r2, float p) {
		if(!productionsN.containsKey(l)) {
			productionsN.put(l, new ArrayList<ProductionN>());
		}
		productionsN.get(l).add(new ProductionN(l, r1, r2, p));
		addNonTerminal(l);
	}
	public void addNonTerminal(String str) {
		if(!nonTerminals.contains(str)) {
			nonTerminals.add(str);
		}
	}
	
	public int sIndex() {
		int i = nonTerminals.indexOf("S");
		if(i == -1) {
			System.out.println("This CFG does not have a start terminal.");
			return 0;
		}
		return i;
	}
	
	public void printProductionsT() {
		Set<Map.Entry<String, ArrayList<ProductionT>>> pts = productionsT.entrySet();
		Iterator<Map.Entry<String, ArrayList<ProductionT>>> ptsIterator = pts.iterator();
		while(ptsIterator.hasNext()) {
			ArrayList<ProductionT> ptsArrList = ptsIterator.next().getValue();
			for(int i=0; i<ptsArrList.size(); i++) {
				ProductionT pt = ptsArrList.get(i);
				System.out.println(pt.left + ", " + pt.right + ", " + pt.p);
			}
		}
	}
	public void printProductionsN() {
		Set<Map.Entry<String, ArrayList<ProductionN>>> pns = productionsN.entrySet();
		Iterator<Map.Entry<String, ArrayList<ProductionN>>> pnsIterator = pns.iterator();
		while(pnsIterator.hasNext()) {
			ArrayList<ProductionN> pnsArrList = pnsIterator.next().getValue();
			for(int i=0; i<pnsArrList.size(); i++) {
				ProductionN pn = pnsArrList.get(i);
				System.out.println(pn.left + ", " + pn.right1 + ", " + pn.right2 + ", " + pn.p);
			}
		}
	}
	
	public void printNonterminals() {
		for(int i=0; i<nonTerminals.size(); i++) {
			System.out.print(nonTerminals.get(i)+ " ");
		}
	}
	
	// get the probability of emitting a char from a certain nonterminal
	public float e(String nonTerminal, char ch) {
		float p = 0;
		ArrayList<ProductionT> pts = productionsT.get(nonTerminal);
		if(pts != null) {
			for(int i=0;i<pts.size();i++) {
				ProductionT pt = pts.get(i);			
				if (pt.right.equals(Character.toString(ch))) {
					p = pt.p;
				}
			}		
		}
		return p;
	} 
	
	// get the probability of generating v -> y z, where v, y, z are nonTerminals 
	public float t(String v, String y, String z) {
		float p = 0;
		ArrayList<ProductionN> pns = productionsN.get(v);
		if(pns!=null) {
			for (int i=0; i<pns.size(); i++) {
				ProductionN pn = pns.get(i);
				if (pn.right1.equals(y) && pn.right2.equals(z)) {
					p = pn.p;
				}
			}			
		}
		return p;
	}

}
