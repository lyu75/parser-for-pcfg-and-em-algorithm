package parser;

import java.io.*;
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

	
	// default constructor 
	CFG(){}

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
	
	// gives the index of a non-terminal from the arraylist of nonterminals
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
	
	
	// run inside algorithm given a string input
	public float[][][] inside(String str){
		int l = str.length();
		int m = nonTerminals.size();
		float[][][] alpha = new float[l][l][m];
		// initialization
		for(int i=0; i<l; i++) {
			for(int v=0; v<m; v++) {
				alpha[i][i][v] = e(nonTerminals.get(v), str.charAt(i));
			}
		}
		
		// iteration 
		for(int i=l-1; i>=0; i--) {
			for(int j=i+1; j<l; j++) {
				for(int v=0; v<m; v++) {
					// calculating a single alpha value: alpha[i][j][v]					
					for(int y=0; y<m; y++) {
						for(int z=0; z<m; z++) {
							for(int k=i; k<=j-1; k++) {
								alpha[i][j][v] += alpha[i][k][y] * alpha[k+1][j][z] * t(nonTerminals.get(v), nonTerminals.get(y), nonTerminals.get(z));
							}
						}
					}
				}
			}
		}
		
		this.alpha = alpha;
		return alpha;
	}
	
	// run outside algorithm given a string input
	public float[][][] outside(String str){
		int l = str.length();
		int m = nonTerminals.size();
		float[][][] beta = new float[l][l][m];
		float[][][] alpha = inside(str);
		
		// initialization:
		for(int v=0; v<m; v++) {
			if(nonTerminals.get(v).equals("S")) {
				beta[0][l-1][v] = 1;				
			}else {
				beta[0][l-1][v]	= 0;
			}
		}

		// iteration:
		for(int i=0; i<l; i++) {
			for(int j=l-1; j>=i; j--) {
				for(int v=0; v<m; v++) {
					// calculate beta:
					
					// for each pair of (y, z)
					for(int y=0; y<m; y++) {
						for (int z=0; z<m; z++) {
							
							// k1 goes from 0 to i-1; inclusive
							// k2 goes from j+1 to l-1; inclusive 
							for(int k1=0; k1<i; k1++) {
								if(i>0) {
									beta[i][j][v] += alpha[k1][i-1][z]*beta[k1][j][y]*t(nonTerminals.get(y), nonTerminals.get(z), nonTerminals.get(v));
								}
							}
							for(int k2=j+1; k2<l; k2++) {
								beta[i][j][v] += alpha[j+1][k2][z]*beta[i][k2][y]*t(nonTerminals.get(y), nonTerminals.get(v), nonTerminals.get(z));								
							}
						}
					}
				}
			}
		}		
		
		// Termination:
		for(int i=0; i<l; i++) {
			float p = 0;
			for(int v=0; v<m; v++) {
				// verification of correctness: p is the same for all i
				p += beta[i][i][v] * e(nonTerminals.get(v), str.charAt(i));				
			}
		}
		this.beta = beta;
		return beta;
	}
	
	float[][][] alpha;
	float[][][] beta;
	
	// getter methods for alpha and beta 
	public float[][][] getAlpha(){
		return alpha;
	}
	
	public float[][][] getBeta(){
		return beta;
	}

}
