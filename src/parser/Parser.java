package parser;

import java.util.*;
import java.io.*;

public class Parser {
	
	// print an array of strings
	public static void print(String[] strs) {
		for (int i=0;i<strs.length;i++) {
			System.out.print(strs[i]+" ");
		}
		System.out.println();
	}
	// print an arraylist of strings
	public static void printArrayList(ArrayList<String> strs) {
		for(int i=0; i<strs.size(); i++) {
			System.out.println(strs.get(i));
		}
	}
	// convert an arraylist of strings to an array of strings
	public static String[] toArray(ArrayList<String> strs) {
		String[] output = new String[strs.size()];
		for(int i=0;i<strs.size(); i++) {
			output[i] = strs.get(i);
		}
		return output;
	}
	
	
	// print only the non-zero terms in the 3D array
	public static void print3DArr(float[][][] f, CFG g) {
		for(int i=0; i<f.length;i++) {
			for(int j=0; j<f[0].length;j++) {
				for(int k=0; k<f[0][0].length; k++) {
//					if(f[i][j][k] != 0) {
						System.out.printf("%3d %3d %3s %10.5f %n", i, j, g.nonTerminals.get(k), f[i][j][k]);						
//					}
				}
			}
		}
	}
	
	// outputs a 3d array into a text file
	public void output3DArr(float[][][] arr, String filename) {
		try {
			// create file
			FileWriter output = new FileWriter("C:\\\\Users\\\\nox19\\\\eclipse-workspace\\\\parser for p-cfg and em algorithm\\\\src\\\\parser\\\\"+ filename + ".txt");
			for(int i=0; i<arr.length; i++) {
				for(int j=0; j<arr[0].length; j++) {
					for(int k=0; k<arr[0][0].length; k++) {
						if(arr[i][j][k] == 0.0) {
							output.write(0 + " ");
						}else {
							output.write(arr[i][j][k]+ " ");							
						}
					}
					output.write("\n");
				}
				output.write("\n\n");
			}
			output.close();
		}catch(IOException e) {
			System.out.println("IOException");
		}
	}
	
	
	// takes a .txt file that contains a list of string inputs 
	// return an arraylist of strings read from the input file 
	public ArrayList<String> getStringInput(String filePath) {
		ArrayList<String> inputs = new ArrayList<String>();
		try(BufferedReader reader = new BufferedReader(new FileReader(filePath))){
			String line = reader.readLine();
			while(line != null) {
				inputs.add(line);
				line = reader.readLine();
			}
			reader.close();
		}catch(FileNotFoundException e){
			System.out.println("file not found");
		}catch(IOException e) {
			System.out.println("IOException");
		}
		return inputs;
	}
	
	
	
	// takes a .txt file that represents a grammar, parses it and returns a CFG instance;
	// REMINDER: the file path needs to be a complete file path:
	// for example: "C:\\Users\\nox19\\eclipse-workspace\\parser\\src\\parser\\test.txt"
	public CFG initG(String filepath){
		CFG cfg = new CFG();
		try(BufferedReader reader = new BufferedReader(new FileReader(
				filepath))){
			String line = reader.readLine();
			while(line != null) {
				String[] strs = line.split("(\\s\\|?\\s?)");
				if(strs.length>3) {
					// add a production rule whose right-hand-side consists of non-terminals
					cfg.addToProductionsN(strs[0], strs[1], strs[2], Float.valueOf(strs[3]));
				}else {
					// add a production rule whose right-hand-side consists of a terminal 
					cfg.addToProductionsT(strs[0], strs[1], Float.valueOf(strs[2]));
				}
				line = reader.readLine();
			}
			reader.close();			
		} catch (FileNotFoundException e) {
			System.out.println("file not found");
		} catch (IOException e) {
			System.out.println("IOexception");
		}		
		return cfg;
	}
	
	// the inside algorithm
	// returns a 3D array of floats storing all the alpha values. 
	public float[][][] inside(CFG g, String str) {
		int l = str.length();
		int m = g.nonTerminals.size();
		float[][][] alpha = new float[l][l][m];
		// initialization
		for(int i=0; i<l; i++) {
			for(int v=0; v<m; v++) {
				alpha[i][i][v] = g.e(g.nonTerminals.get(v), str.charAt(i));
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
								alpha[i][j][v] += alpha[i][k][y] * alpha[k+1][j][z] * g.t(g.nonTerminals.get(v), g.nonTerminals.get(y), g.nonTerminals.get(z));
							}
						}
					}
				}
			}
		}
		output3DArr(alpha, "alpha");
		return alpha;
	}
	
	public float[][][] outside(CFG g, String str) {
		int l = str.length();
		int m = g.nonTerminals.size();
		float[][][] beta = new float[l][l][m];
		float[][][] alpha = inside(g, str);
		
		// initialization:
		for(int v=0; v<m; v++) {
			if(g.nonTerminals.get(v).equals("S")) {
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
									beta[i][j][v] += alpha[k1][i-1][z]*beta[k1][j][y]*g.t(g.nonTerminals.get(y), g.nonTerminals.get(z), g.nonTerminals.get(v));
								}
							}
							for(int k2=j+1; k2<l; k2++) {
								beta[i][j][v] += alpha[j+1][k2][z]*beta[i][k2][y]*g.t(g.nonTerminals.get(y), g.nonTerminals.get(v), g.nonTerminals.get(z));								
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
				p += beta[i][i][v] * g.e(g.nonTerminals.get(v), str.charAt(i));				
			}
		}		
		output3DArr(beta, "beta");
		return beta;
	}
	
	
	/*
	 * CFGConverge function compares the production rules in CFG instances g1 and g2
	 * g1 and g2 have the same set of production rules, but different probabilities
	 * and so each rule in g1 has a corresponding rule in g2 with a different probability
	 * if the difference between probabilities of EVERY two corresponding rules in g1 and g2 is smaller than the threshold, compareRules returns true. Otherwise, it returns false
	 * assume that rules in g1 and g2 have the same order 
	 */
	public boolean CFGConverge(float threshold, CFG g1, CFG g2) {
		// an arraylist of nonterminals 
		ArrayList<String> nts = g1.nonTerminals;
		ArrayList<String> nts2 = g2.nonTerminals;
		
		if(nts.size() != nts2.size()) {
			return false;
		}
		
		//loop through all nonterminals
		for(int i=0; i<nts.size(); i++) {
			String nt = nts.get(i);
			
			//get production rules with nonterminal nt on the left hand side in g1 and g2 
			ArrayList<ProductionT> pts1 = g1.productionsT.get(nt);
			ArrayList<ProductionN> pns1 = g1.productionsN.get(nt);
			
			ArrayList<ProductionT> pts2 = g2.productionsT.get(nt);
			ArrayList<ProductionN> pns2 = g2.productionsN.get(nt);
						
			//we assumed production rules in g1 and g2 have the same order
			//since g1 and g2 have the same set of rules, pts1 and pts2, pns1 and pns2 have the same size
			//now we compare the probabilities between each pair of corresponding rules in g1 and g2
			if(pts1!=null) {
				// pts1 == null means the nonterminal does not produce any terminal
				for(int j=0; j<pts1.size(); j++) {
					ProductionT p1 = pts1.get(j);
					ProductionT p2 = pts2.get(j);
					
//					System.out.println(p1.left + " " + p1.right + " " + p1.p);
//					System.out.println(p2.left + " " + p2.right + " " + p2.p);
					
					
					if(p1.p - p2.p > threshold) {
						return false;
					}
				}				
			}
			if(pns1!=null) {
				// pns1 == null means the nonterminal does not produce any pair of nonterminals
				for(int j=0; j<pns1.size(); j++) {
					ProductionN p1 = pns1.get(j);
					ProductionN p2 = pns2.get(j);				

//					System.out.println(p1.left + " " + p1.right1 + " " + p1.right2 + " " + p1.p);
//					System.out.println(p2.left + " " + p2.right1 + " " + p2.right2 + " " + p2.p);

					
					if(p1.p - p2.p > threshold) {
						return false;
					}
				}				
			}
		}
		// if the difference between each pair of production rules probabilities in g1 and g2 is smaller than the threshold, return true 
		return true;
	}
	 
	
	// the CYK algorithm
	public float[][][] cyk(CFG g, String str){
		int l = str.length();
		int m = g.nonTerminals.size();
		float[][][] gamma = new float[l][l][m];
		
		// initialization:
		for(int i=0; i<l; i++) {
			for(int j=0; j<l; j++) {
				for(int k=0; k<m; k++) {
					//initialize all elements to -inf
					gamma[i][j][k] = Float.NEGATIVE_INFINITY;
				}
			}
		}
		for(int i=0; i<l; i++) {
			for(int v=0; v<m; v++) {
				gamma[i][i][v] = (float)Math.log10(g.e(g.nonTerminals.get(v), str.charAt(i)));
			}
		}
		
		// iteration:
		for(int i=l-2; i>=0; i--) {
			for(int j=i+1; j<l; j++) {
				for(int v=0; v<m; v++) {

					for(int y=0; y<m; y++) {
						for(int z=0; z<m; z++) {
							for(int k=i; k<=j-1; k++) {
								if(gamma[i][k][y]!=Float.NEGATIVE_INFINITY && gamma[k+1][j][z]!=Float.NEGATIVE_INFINITY && g.t(g.nonTerminals.get(v), g.nonTerminals.get(y), g.nonTerminals.get(z))!=0) {
									float newGamma = gamma[i][k][y] + gamma[k+1][j][z] + (float)Math.log10(g.t(g.nonTerminals.get(v), g.nonTerminals.get(y), g.nonTerminals.get(z)));
									if(newGamma > gamma[i][j][v]) {
										gamma[i][j][v] = newGamma;
									}
								}
							}
						}
					}
				}
			}
		}
				
		// termination log(P(str, optimalParseTree)) = gamma(0, l-1, 0(S))
		System.out.println("P(" + str + ", optimalParseTree) = "+ Math.pow(10, gamma[0][l-1][0]));
		return gamma;
	}
	
	
	/*
	 * a helper function that generates terminal production rules given a list of probabilities of generating terminal characters (A, C, T, G)
	 * terminal production rules are output to console
	 */
	public void buildTerminalProductions(String fileTerminalProbabilities) {
		try(BufferedReader reader = new BufferedReader(new FileReader(
				fileTerminalProbabilities))){
			String line = reader.readLine();

			HashMap<Integer, String> chars = new HashMap<Integer, String>();
			chars.put(0, "a");
			chars.put(1, "c");
			chars.put(2, "g");
			chars.put(3, "t");
			
			
			for(int i=0; i<14; i++) {
				String[] ps = line.split("(\\s\\|?\\s?)");
				if(i>0) {
					for(int j=0; j<ps.length; j++) {
						System.out.println("P"+i+" | "+ chars.get(j) + " "+ ps[j]);
					}
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
	
	// parameter re-estimation by expectation maximization using inside var alpha and outside var beta
	public void EM(CFG g, String[] strs) {
		CFG oldG = new CFG();
		
		/*
		 * Hashmap alpha and beta:
		 * alpha: 
		 * 		string -> float[][][]
		 * betea:
		 * 		string -> float[][][]
		 * 
		 * while not converged:
		 * 		for each production rule:
		 * 			intialize c(v)Sum, c(v->yz)Sum or c(v->a)Sum
		 * 			for each string input: 
		 * 				get alpha; get beta
		 * 				calculate c(v) and c(v -> yz) or c(v -> a); add each to its corresponding sum
		 * 			end for 
		 * 			compute tHat or eHat from the cSums
		 * 			update probability for the production rule
		 * 		end for
		 * end while 
		 * 			
		 */
		
		
		// already implemented 
		/*
		 * hashMap for productionTs and productionNs: 
		 * productionT -> c(v->a)Sum 
		 * productionN -> c(v->yz)Sum
		 * 
		 * hashMap for nonTerminals: 
		 * v -> c(v)Sum
		 * 
		 * while not converged: 
		 * 		for each string:
		 * 			calculate alpha, beta
		 * 			for each nonTerminal v: 
		 * 				calculate c(v)
		 * 				c(v)Sum += c(v)
		 *			end for 
		 *			for each productionT:
		 *				calculate c(v->a)
		 *				c(v->a)Sum += c(v->a)
		 *			end for 
		 *			for each productionN:
		 *				calculate c(v->yz)
		 *				c(v->yz)Sum += c(v->yz)
		 *			end for 
		 * 		end for
		 * 		for each productionT (v->a):
		 * 			update probability with c(v->a)Sum/c(v)
		 * 		end for 
		 * 		for each productionN (v->yz):
		 * 			update probability with c(y->yz)Sum/c(v)
		 * 		end for
		 * end while 
		 */	
		
		
		// productionT -> c(v->a)Sum 
		HashMap<ProductionT, Float> pts = new HashMap<ProductionT, Float>();
		// productionN -> c(v->yz)Sum
		HashMap<ProductionN, Float> pns = new HashMap<ProductionN, Float>();
		// v -> c(v)Sum
		HashMap<String, Float> vs = new HashMap<String, Float>();

		// initialize the hashmaps:
		// loop through all productions via each nonTerminal
		for(int v=0; v<g.nonTerminals.size(); v++) {
			String nt = g.nonTerminals.get(v);
			vs.put(nt, 0f);
			
			// get all ProductionT instances associated with the current nonTerminal:
			if(g.productionsT.containsKey(nt)) {
				ArrayList<ProductionT> productionsVA = g.productionsT.get(nt);					
				for(int i=0; i<productionsVA.size(); i++) {
					pts.put(productionsVA.get(i), 0f);						
				}
			}
			// get all ProductionN instances associated with the current nonTerminal:
			if(g.productionsN.containsKey(nt)) {
				ArrayList<ProductionN> productionsVYZ = g.productionsN.get(nt);
				for(int i=0; i<productionsVYZ.size(); i++) {
					pns.put(productionsVYZ.get(i), 0f);
				}				
			}
		}			
		
		int count = 0;
		
		while(!CFGConverge(0.00001f, g, oldG)) {
			if(count==20) {
				break;
			}
			
			oldG = g.clone();
			
			count ++;
			
			// loop through all strings in strs:
			for(int s=0;s<strs.length;s++) {
				System.out.println("input: "+strs[s]);
				
				float[][][] alpha = inside(g, strs[s]);
				float[][][] beta = outside(g, strs[s]);
				int l = strs[s].length();
				
				// for each nonTerminal v:
				for(int v=0;v<g.nonTerminals.size();v++) {
					String nt = g.nonTerminals.get(v);
					// calculate c(v)
					float cv = getCV(g, l, g.nonTerminals.get(v), v, alpha, beta);
					System.out.println(nt +" "+ cv);					
					
					// add c(v) to c(v)Sum
					float cvSum = vs.get(nt) + cv;
					System.out.println(nt +" sum "+ cvSum);					
					vs.put(nt, cvSum);
				}
				
				
				// for each production rule: 
				for(int v=0; v<g.nonTerminals.size(); v++) {
					String nt = g.nonTerminals.get(v);
					// get all ProductionT instances associated with the current nonTerminal:
					if(g.productionsT.containsKey(nt)) {
						ArrayList<ProductionT> productionsVA = g.productionsT.get(nt);					
						for(int i=0; i<productionsVA.size(); i++) {
							ProductionT pt = productionsVA.get(i);
							// calculate c(v->a)
							float cva = getCVA(g, strs[s], l, pt, alpha, beta);							
							System.out.println(pt.left + " " + pt.right + " " + cva);												
							// add c(v->a) to c(v->a)Sum
							float cvaSum = pts.get(pt) + cva;
							System.out.println(pt.left + " " + "sum " + pt.right + " " + cvaSum);					
							pts.put(pt, cvaSum);
						}
					}
					// get all ProductionN instances associated with the current nonTerminal:
					if(g.productionsN.containsKey(nt)) {
						ArrayList<ProductionN> productionsVYZ = g.productionsN.get(nt);
						for(int i=0; i<productionsVYZ.size(); i++) {
							ProductionN pn = productionsVYZ.get(i);
							// calculate c(v->yz)
							float cvyz = getCVYZ(g, l, pn, alpha, beta);
							System.out.println(pn.left + " " + pn.right1 + " " + pn.right2+ " " + cvyz);												
							// add c(v->a) to c(v->yz)Sum 
							float cvyzSum = pns.get(pn) + cvyz;
							System.out.println(pn.left + " " + pn.right1 + " " + pn.right2+ " sum " + cvyzSum);							
							pns.put(pn, cvyzSum);
						}
					}
				}
			}
			
			for(int v=0; v<g.nonTerminals.size(); v++) {
				String nt = g.nonTerminals.get(v);
				//get cv
				float cv = vs.get(nt);
				
				// get all ProductionT instances associated with the current nonTerminal:
				if(g.productionsT.containsKey(nt)) {
					ArrayList<ProductionT> productionsVA = g.productionsT.get(nt);					
					for(int i=0; i<productionsVA.size(); i++) {
						ProductionT pt = productionsVA.get(i);
						float eva = pts.get(pt) / cv;
						g.updateProbability(pt.left, pt.right, eva);
					}
				}
				// get all ProductionN instances associated with the current nonTerminal:
				if(g.productionsN.containsKey(nt)) {
					ArrayList<ProductionN> productionsVYZ = g.productionsN.get(nt);
					for(int i=0; i<productionsVYZ.size(); i++) {
						ProductionN pn = productionsVYZ.get(i);
						float tvyz = pns.get(pn) / cv;
						g.updateProbability(pn.left, pn.right1, pn.right2, tvyz);
					}
				}
			}
			
			System.out.println("comparison" + count + ": ");
			System.out.println("oldG: ");
			oldG.printProductionsN();
			oldG.printProductionsT();
			System.out.println("g: ");
			g.printProductionsN();
			g.printProductionsT();
		}
		
		System.out.println("final output grammar: ");
		g.printProductionsN();
		g.printProductionsT();		
	}
	
		
	// calculate the expected number of times a production rule v->a is used
	private float getCVA(CFG g, String str, int l, ProductionT pt, float[][][] alpha, float[][][] beta) {
		/*
		 * the new probability for production rule v -> a
		 * e_hat(a) = c(v -> a)/c(v)
		 * 
		 * c(v -> a) = (1/P(x|theta)) * (SUM i | str[i] = a)beta(i, i, v)e(v, a)
		 */
		//get c(v->a)
		String a = pt.right;
		int v = g.indexOf(pt.left);
		float cva = 0;
		for(int i=0; i<l; i++) {
			if(Character.toString(str.charAt(i)).equals(a)) {
				cva += beta[i][i][v]*g.e(pt.left, str.charAt(i));
			}
		}
		cva = cva/(1/alpha[0][l-1][0]);
		return cva;	
	}
		
		
	// calculate expected number of times a production rule v->yz is used
	private float getCVYZ(CFG g, int l, ProductionN pn, float[][][] alpha, float[][][] beta) {
		/*
		 * c(v -> yz) = (1/P(x|theta) * (SUM i from 1 to L-1)(SUM j from i+1 to L)(SUM k from i to j-1) beta(i, j, v)alpha(i, k, y)alpha(k+1, j, z)t(v, y, z)
		 *
		 * the new probability for production rule v -> yz: 
		 * t_hat(v, y, z) = c(v -> yz)/c(v)
		 */
		float cvyz = 0;
		int v = g.indexOf(pn.left);
		int y = g.indexOf(pn.right1);
		int z = g.indexOf(pn.right2);
		for(int i=0; i<l-1; i++) {
			for(int j=i+1; j<l; j++) {
				for(int k=i; k<=j-1; k++) {
					cvyz += beta[i][j][v]*alpha[i][k][y]*alpha[k+1][j][z]*g.t(pn.left, pn.right1, pn.right2);
				}
			}
		}
		cvyz = cvyz/(1/alpha[0][l-1][0]);
		return cvyz;
	}
		
		
	// calculate expected number of times a state v is used: 			
	private float getCV(CFG g, int l, String v, int vIndex, float[][][] alpha, float[][][] beta) {
		/* 	
		 * c(v) = (1/(P(x|theta)) * (SUM i from 1 to L)(SUM j from i to L)alpha(i, j, v)beta(i, j, v)
		 * 
		 * P(x|theta) = alpha(0, L-1, 0);
		*/ 	
		// get c(v) 
		float cv = 0;
		for(int i=0; i<l; i++) {
			for(int j=i; j<l; j++) {
				cv += alpha[i][j][vIndex]*beta[i][j][vIndex];
			}
		}
		cv = cv/(1/alpha[0][l-1][0]);
		return cv;
	}
		
	
	
	public static void main(String[] args) {
		Parser p = new Parser();
		CFG g = p.initG("C:\\Users\\nox19\\eclipse-workspace\\parser for p-cfg and em algorithm\\src\\parser\\test3");		
		ArrayList<String> inputs = p.getStringInput("C:\\Users\\nox19\\eclipse-workspace\\parser for p-cfg and em algorithm\\src\\parser\\testInput");
		String[] inputsArr = Parser.toArray(inputs);
		p.EM(g, inputsArr);
	}
}
