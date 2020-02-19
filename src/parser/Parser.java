package parser;

import java.util.*;
import java.io.*;

public class Parser {
	
	// print an array of strings
	public static void print(String[] strs) {
		for (int i=0;i<strs.length;i++) {
			System.out.print(strs[i]+", ");
		}
		System.out.println();
	}
	
	// print only the non-zero terms in the 3D array
	public static void print3DArr(float[][][] f, CFG g) {
		for(int i=0; i<f.length;i++) {
			for(int j=0; j<f[0].length;j++) {
				for(int k=0; k<f[0][0].length; k++) {
					if(f[i][j][k] != 0) {
						System.out.printf("%3d %3d %3s %10.5f %n", i, j, g.nonTerminals.get(k), f[i][j][k]);						
					}
				}
			}
		}
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
								
//								if(alpha[i][k][y]!=0 || alpha[k+1][j][z]!=0 || g.t(g.nonTerminals.get(v), g.nonTerminals.get(y), g.nonTerminals.get(z))!=0) {
//									System.out.printf("%3d %3d %3s %10.5f %n", i, k, g.nonTerminals.get(y), alpha[i][k][y]);
//									System.out.printf("%3d %3d %3s %10.5f %n", k+1, j, g.nonTerminals.get(z), alpha[k+1][j][z]);
//									System.out.printf("%3d %3d %3s %10.5f %n", v, y, z, g.t(g.nonTerminals.get(v), g.nonTerminals.get(y), g.nonTerminals.get(z)));
//									System.out.printf("%3d %3d %3s %10.5f %n", i, j, g.nonTerminals.get(v), alpha[i][j][v]);
//									System.out.println("----------------------------");									
//								}
							}
						}
					}
				}
			}
		}
		
//		print3DArr(alpha, g);
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

		// interation:
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
//		print3DArr(beta, g);
		
		// Termination:
		for(int i=0; i<l; i++) {
			float p = 0;
			for(int v=0; v<m; v++) {
				// verification of correctness: p is the same for all i
				p += beta[i][i][v] * g.e(g.nonTerminals.get(v), str.charAt(i));				
			}
//			System.out.println("p"+"(i=" + i + ")" + " = " + p);			
		}
		
		return beta;
	}
	
	
	
	public static void main(String[] args) {
		Parser p = new Parser();
		CFG g = new CFG();
		g = p.initG("C:\\Users\\nox19\\eclipse-workspace\\parser for p-cfg and em algorithm\\src\\parser\\test.txt");
//		g.printProductionsT();
//		g.printProductionsN();
//		g.printNonterminals();
//		g.e("S", 'c');
//		g.t("VP", "VP", "PP");
//		p.inside(g, "cgt");
//		p.outside(g, "cgt");
	}
}
