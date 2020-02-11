package parser;

// a production rule whose right hand side is a terminal
public class ProductionT {
	String left;
	String right;
	float p;
	
	ProductionT(String l, String r, float p){
		this.left = l;
		this.right = r;
		this.p = p;
	}
}
