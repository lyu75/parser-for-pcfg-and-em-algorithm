package parser;

// a production rule whose right hand side consists of non-terminals
public class ProductionN {
	String left;
	String right1;
	String right2;
	float p;
	
	ProductionN(String l, String r1, String r2, float p){
		left = l;
		right1 = r1;
		right2 = r2;
		this.p = p;
	}
}
