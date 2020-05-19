package parser;

import java.io.*;
import java.util.*;

/*
 * CFG: context-free grammar
 * pCFG: probabilistic context-free grammar 
 */
public class CFG {
	// productionsT: stores all production rules whose right side is a terminal
	HashMap<String, ArrayList<ProductionT>> productionsT = new HashMap<String, ArrayList<ProductionT>>();
	
	// productionsN : stores all production rules whose right side are two nonterminals
	HashMap<String, ArrayList<ProductionN>> productionsN = new HashMap<String, ArrayList<ProductionN>>();
	public ArrayList<String> nonTerminals = new ArrayList<String>();
	
	
	// default constructor (needed for the parser class temporarily)
	CFG(){}
	
	// constructor 
	CFG(String filePath){
		initG(filePath);
	}
	
	// initialize a CFG instance with a file of production rules
	private void initG(String filePath) {
		try(BufferedReader reader = new BufferedReader(new FileReader(
				filePath))){
			String line = reader.readLine();
			while(line != null) {
				String[] strs = line.split("(\\s\\|?\\s?)");
				if(strs.length>3) {
					// add a production rule whose right-hand-side consists of non-terminals
					addToProductionsN(strs[0], strs[1], strs[2], Float.valueOf(strs[3]));
				}else {
					// add a production rule whose right-hand-side consists of a terminal 
					addToProductionsT(strs[0], strs[1], Float.valueOf(strs[2]));
				}
				line = reader.readLine();
			}
			reader.close();			
		} catch (FileNotFoundException e) {
			System.out.println("file not found");
		} catch (IOException e) {
			System.out.println("IOexception");
		}		
	}
	
	// and a production that produces a terminal character to the hashmap of production rules
	public void addToProductionsT(String l, String r, float p) {
		if(!productionsT.containsKey(l)) {
			productionsT.put(l, new ArrayList<ProductionT>());
		}
		productionsT.get(l).add(new ProductionT(l, r, p));
		addNonTerminal(l);
	}
	
	// add a production that produces two non-terminals to the hashmap of productions rules 
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
	
	// gives the index of a non-terminal from the arraylist of nonterminals
	public int sIndex() {
		int i = nonTerminals.indexOf("S");
		if(i == -1) {
			System.out.println("This CFG does not have a start terminal.");
			return 0;
		}
		return i;
	}
	
	// print production rules that produce a terminal character
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
	
	// print production rules that produce two non-terminals
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
	
	// print a list of all the non-terminals 
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
	
	// find and modify the probability of a certain production rule (v -> yz)
	public void updateProbability(String v, String y, String z, float p) {		
		ArrayList<ProductionN> pns = productionsN.get(v);
		if(pns!=null) {
			for (int i=0; i<pns.size(); i++) {
				ProductionN pn = pns.get(i);
				if (pn.right1.equals(y) && pn.right2.equals(z)) {
					pn.p = p;
				}
			}			
		}		
	}
	
	// find and modify the probability of a certain production rule (v -> c)
	public void updateProbability(String v, String c, float p) {
		ArrayList<ProductionT> pts = productionsT.get(v);
		if(pts != null) {
			for(int i=0;i<pts.size();i++) {
				ProductionT pt = pts.get(i);
				if (pt.right.equals(c)){
					pt.p = p;
				}
			}
		}
	}
	
	// find the index of a non-terminal v from the arraylist of non-terminals 
	public int indexOf(String v) {
		for(int i=0; i<nonTerminals.size(); i++) {
			if(nonTerminals.get(i).equals(v)) {
				return i;
			}
		}
			return -1;
	}
	
	// clone a deep copy of a CFG instance
	public CFG clone() {
		CFG copyG = new CFG();
		
		// make deep copies of productionsT and productionsN
		// loop through all productions via each nonTerminal
		for(int v=0; v<nonTerminals.size(); v++) {
			// copy the arraylist of nonterminals 

			String nt = nonTerminals.get(v);
			copyG.nonTerminals.add(nt);
			// get all ProductionT instances associated with the current nonTerminal:
			if(productionsT.containsKey(nt)) {
				ArrayList<ProductionT> productionsVA = productionsT.get(nt);
				ArrayList<ProductionT> newProductionsT = new ArrayList<ProductionT>();
				for(int i=0; i<productionsVA.size(); i++) {
					ProductionT oldProductionT = productionsVA.get(i);
					// create a new productionT instance with the same content:
					ProductionT newProductionT = new ProductionT(oldProductionT.left, oldProductionT.right, oldProductionT.p);
					newProductionsT.add(newProductionT);
				}
				copyG.productionsT.put(nt, newProductionsT);
			}
			// get all ProductionN instances associated with the current nonTerminal:
			if(productionsN.containsKey(nt)) {
				ArrayList<ProductionN> productionsVYZ = productionsN.get(nt);
				ArrayList<ProductionN> newProductionsN = new ArrayList<ProductionN>();
				for(int i=0; i<productionsVYZ.size(); i++) {
					ProductionN oldProductionN = productionsVYZ.get(i);
					// create a new productionT instance with the same content:
					ProductionN newProductionN = new ProductionN(oldProductionN.left, oldProductionN.right1, oldProductionN.right2, oldProductionN.p);
					newProductionsN.add(newProductionN);
				}
				copyG.productionsN.put(nt, newProductionsN);
			}
		}
		return copyG;
	}
	
}
