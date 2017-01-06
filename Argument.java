
public class Argument implements Type{
	String name;
	boolean IsVariable;

	// Constructor ; Variables start with small letters, and Constants start with capital letters
	Argument(String s){
		this.name = s;
		if(s.charAt(0) >= 97 && s.charAt(0) <= 122){
			this.IsVariable = true;
		}
		else{
			this.IsVariable = false;
		}
	}

	// To replace an argument with other -- This is used during Unification of two predicates
	void replaceName(String s_new){
		this.name = s_new;
	}

	// Returns the length of Argument's name
	int length(){
		return this.name.length();
	}

	// Returns true if the two Arguments are same
	boolean equal(Argument a){
		if(this.name.equals(a.name) && this.IsVariable==a.IsVariable){
			return true;
		}
		return false;
	}
}

