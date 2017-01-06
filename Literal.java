import java.util.ArrayList;

public class Literal implements Type{
	String predicate;
	boolean is_negated;	
	ArrayList<Argument> args ;
	Clause parent;

	// copy constructor
	Literal(Literal l){
		predicate = new String(l.predicate);
		is_negated = l.is_negated;
		parent = l.parent;
		args = new ArrayList<Argument>(l.args);
	}

	// constructor : creates a Literal object from a string
	Literal(String complete){
		args = new ArrayList<Argument>();
		int i =0;
		while(complete.charAt(i) != '(')
			i++;
		if(complete.charAt(0) == '~'){
			is_negated = true;
			predicate = new String(complete.substring(1, i));
		}
		else{
			is_negated = false;
			predicate = new String(complete.substring(0, i));
		}
		i++;
		// now i has the first character of the first arg 
		while(complete.charAt(i) != ')'){
			int s = i;
			// s is the first char of current arg
			while(complete.charAt(i) != ',' ) {
				if(complete.charAt(i) == ')')
					break;
				i++;
			}
			// i has a comma
			args.add(new Argument(complete.substring(s, i)));
			if(complete.charAt(i) == ')')
				break;
			i++;
		}			
	}

	Literal(){}

	// returns the number of arguments in a Literal
	int getArgumentcount(){
		return args.size();
	}

	// returns true if two literals are same, otherwise returns false
	boolean isEqual(Literal l){
		if(!this.predicate.equals(l.predicate)){
			return false;
		}
		if(this.is_negated != l.is_negated){
			return false;
		}
		int count1 = this.args.size();
		int count2 = l.args.size();
		if(count1 != count2){
			return false;
		}
		for(int i =0; i < count1; i++){
			if(!this.args.get(i).equal(l.args.get(i))){
				return false;
			}
		}
		return true;
	}

	//returns the negated version of a literal
	Literal get_negation(Literal l){
		Literal l2 = new Literal(l);
		if(l2.is_negated == true){
			l2.is_negated = false;
		}
		else{
			l2.is_negated = true;
		}
		return l2;
	}

	// checks if a literal "b" is negated version of current literal object or not 
	boolean is_negation(Literal b){
		if(this.is_negated == b.is_negated)
			return false;	

		String pred2 = b.predicate;
		if(!predicate.equals(pred2))
			return false;
		int c1 =this.getArgumentcount();
		int c2 = b.getArgumentcount();
		if(c1 != c2){
			// different number of arguments found
			return false;
		}

		for(int i =0; i < c1;i++){
			if(!args.get(i).equal(b.args.get(i)))
				//Arguments mismatch
				return false;			
		}
		return true;
	}

	// creates a query from a given string
	static Literal create_query(String complete){
		complete = complete.replaceAll(" ", "");
		Literal l = new Literal();
		l.args = new ArrayList<Argument>();
		char c = complete.charAt(0);
		int i = 0;
		if(c == '(' && complete.charAt(1) == '~'){
			l.is_negated = true;
			i = 2;
		}
		else{
			l.is_negated = false;
			i = 0;
		}
		int j = i; // j is the starting of predicate
		while(complete.charAt(i) != '('){
			i++;
		}
		l.predicate = new String(complete.substring(j,i));
		i++;

		// now i has the first character of the first arg 
		while(complete.charAt(i) != ')'){
			int s = i;
			while(complete.charAt(i) != ',' ) {
				if(complete.charAt(i) == ')')
					break;
				i++;
			}
			l.args.add(new Argument(complete.substring(s, i)));
			if(complete.charAt(i) == ')')
				break;			
			i++;
		}		
		return l;
	}
}
