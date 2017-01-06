import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Set;

public class Resolver {

	// convert stringto clause
	// add query to kb and resolve 
	// return answer
	Clause query;
	boolean resolved_something = false;
	HashSet<String> resolved = new HashSet<String>();
	int max_iter =5;

	// Constructor: creates a resolver object from a clause object
	Resolver(Clause query){
		this.query = query;
	}

	// returns the answer to a Query
	boolean answer(HashSet<Clause> a, HashSet<Clause> b){
		HashSet<Clause> old_singles = new HashSet<Clause>(a);
		HashSet<Clause> old_multiples = new HashSet<Clause>(b);
		boolean complete = false;
		// creating a temporary set to add resolved clauses
		HashSet<Clause> temp = new HashSet<Clause>();
		boolean none_with_query = true;
		Clause q = this.query;
		// Resolving Query with all the clauses in old_singles
		for(Clause c2 : old_singles){
			if(q != c2 && !is_parent(q, c2)){
				String str = new String("["+get_String(q)+"]"+"["+get_String(c2)+"]");
				String str2 = new String("["+get_String(c2)+"]"+"["+get_String(q)+"]");
				// Check if two clauses have been resolved before or not
				if(!resolved.contains(str) && !resolved.contains(str2)){
					resolved.add(str);
					resolved.add(str2);

					HashSet<Clause> resolvent = resolve(q, c2);
					if(resolvent != null){
						none_with_query = false;
					}
					if(resolvent == null){  // TODO check
						if(resolved_something == true){
							return true;
						}
					}
					temp.addAll(resolvent);
				}
			}
		}
		// Resolving Query with all the clauses in old_multiples
		for(Clause c2 : old_multiples){
			if(q != c2 && !is_parent(q, c2)){
				String str = new String("["+get_String(q)+"]"+"["+get_String(c2)+"]");
				String str2 = new String("["+get_String(c2)+"]"+"["+get_String(q)+"]");
				// Check if two clauses have been resolved before or not
				if(!resolved.contains(str) && !resolved.contains(str2)){
					resolved.add(str);
					resolved.add(str2);

					HashSet<Clause> resolvent = resolve(q, c2);
					if(resolvent != null){
						none_with_query = false;
					}
					if(resolvent == null){  // TODO check
						if(resolved_something == true){
							return true;
						}
					}
					temp.addAll(resolvent);
				}
			}
		}

		if(none_with_query == true){
			return false;
		}
		for(Clause c1 : temp){
			// check if query again resolved
			if(c1.get_size() == 1){
				if(c1.isEqual(query)){
					return false;
				}
				old_singles.add(c1);
			}
			else{
				old_multiples.add(c1);
			}
		}

		int count = 0;
		while(complete == false & count<max_iter){
			count++;
			for(Clause c1 : old_singles){
				// Resolving each clause of old_singles with all the other clauses in old_singles
				for(Clause c2 : old_singles){
					if(c1 != c2 && !is_parent(c1, c2)){
						String str = new String("["+get_String(c1)+"]"+"["+get_String(c2)+"]");
						String str2 = new String("["+get_String(c2)+"]"+"["+get_String(c1)+"]");
						// Check if two clauses have been resolved before or not
						if(!resolved.contains(str) && !resolved.contains(str2)){
							resolved.add(str);
							resolved.add(str2);

							HashSet<Clause> resolvent = resolve(c1, c2);
							if(resolvent == null){ 
								if(resolved_something == true){
									return true;
								}
							}
							temp.addAll(resolvent);
						}
					}
				}
				// Resolving each clause of old_singles with all the clauses in old_multiples
				for(Clause c2 : old_multiples){
					if(c1 != c2 && !is_parent(c1, c2)){
						String str = new String("["+get_String(c1)+"]"+"["+get_String(c2)+"]");
						String str2 = new String("["+get_String(c2)+"]"+"["+get_String(c1)+"]");
						// Check if two clauses have been resolved before or not
						if(!resolved.contains(str) && !resolved.contains(str2)){
							resolved.add(str);
							resolved.add(str2);
							HashSet<Clause> resolvent = resolve(c1, c2);
							if(resolved_something == true){
								return true;
							}
							temp.addAll(resolvent);
						}}
				}

			}

			int single1 = old_singles.size();
			int multiple1 = old_multiples.size();
			// adding the resolved clauses to KB
			for(Clause c1 : temp){
				if(c1.get_size() == 1){
					old_singles.add(c1);
				}
				else{
					old_multiples.add(c1);
				}
			}

			int single2 = old_singles.size();
			int multiple2 = old_multiples.size();
			// if size of Kb is same after and before the addition of resolved clauses, then we stop.
			if(single1 == single2 && multiple1 == multiple2){
				complete = true;
			}
		}
		return false;
	}

	// returns the Clause object in the form of a string
	String get_String(Clause c){
		String s = new String("");
		for(Literal l : c.list){
			if(l.is_negated == true){
				s = s+"~";
			}
			s = s+l.predicate+"(";
			int size = l.args.size();
			for(Argument a : l.args){
				s = s+a.name;
				int index = l.args.indexOf(a);
				if(index != size-1){
					s = s+",";
				}
				else{
					s = s+")";
				}
			}
			s = s+"|";
		}
		return s;
	}

	// Resolves two clauses, if they can be resolved
	HashSet<Clause> resolve(Clause c1, Clause c2){
		HashSet<Clause> resolved_Clauses = new HashSet<Clause>();
		// standardizing variables in two clauses
		c2 = std_var(c1, c2);
		for(Literal l1 : c1.list){
			for(Literal l : c2.list ){

				Literal l2  = Resolver.can_unify(l1, l);
				if(l2 != null){
					// Can unify these two literals -- l1 and l2
					HashMap<String , String > subs = new HashMap<String, String>();
					// subs is the Substitution Map after unification of two literals
					subs = Resolver.unification(l1, l2, subs);
					subs = mod_subs(subs);
					if(subs != null){
						HashSet<Literal> to_delete = new HashSet<Literal>();
						to_delete.add(l1);
						to_delete.add(l2);
						// substitute and remove negations
						Clause resolvant = new Clause(c1, c2, to_delete);
						resolvant = Resolver.substitute(resolvant, subs);
						if(resolvant == null){
							resolved_something = true;
							resolved_Clauses = null;
							break;
						}
						resolved_Clauses.add(resolvant);
					}
				}

			}
			if(this.resolved_something == true){
				break;
			}
		}
		return resolved_Clauses;
	}

	// returns the unified literal, if two literals, l1 and l2 can be unified, otherwise returns Null
	static Literal can_unify(Literal l1, Literal l2){
		if(!l1.predicate.equals(l2.predicate) || l1.is_negated == l2.is_negated)
			return null;		
		int len = l1.getArgumentcount();
		if(len != l2.getArgumentcount())
			return null;
		for(int i =0; i < len;i++){
			Argument a1 = l1.args.get(i);
			Argument a2 = l2.args.get(i);
			if(a1.IsVariable == false && a2.IsVariable == false){
				// both are constants
				if(!a1.name.equals(a2.name))
					// different constants
					return null;
			}
		}
		return l2;
	}

	static HashMap<String,String> unifyLiteral(ArrayList<Argument> first, ArrayList<Argument> second, HashMap<String, String> theta) {

		if(theta==null)return null;

		int size1 = first.size();
		int size2 = second.size();
		if(size1!=size2) {
			return null;
		}
		else if(size1 == 0 && size2 == 0){
			check_condition_isvalid();
			System.out.print("subs");
			return theta;
		}
		else if(size1 == 1 && size2 == 1){
			Argument zero1 = first.get(0);
			Argument zero2 = second.get(0);
			return unification(zero1,zero2,theta);
		}

		else {
			check_condition_isvalid();
			ArrayList<Argument> x_next = new ArrayList<Argument>(first);
			ArrayList<Argument> y_next = new ArrayList<Argument>(second);
			Type one = (x_next.iterator().next());
			Type tw = y_next.iterator().next();
			check_condition_isvalid();
			theta=unification(one,tw,theta);
			x_next.remove(one);
			y_next.remove(tw);
			check_condition_isvalid();
			return unifyLiteral(x_next, y_next, theta);
		}

	}

	static  HashMap<String, String> unification(Type first, Type second, HashMap<String, String> substitution) {

		// checking if same
		if(first.equals(second)){
			return substitution;
		}

		if(substitution == null)
		{
			return null;
		}


		boolean is_first = first instanceof Argument;
		boolean is_var = false;
		if(is_first == true){
			is_var = ((Argument) first).IsVariable;
		}
		boolean is_second = second instanceof Argument;
		boolean is_var2 = false;
		if(is_second == true){
			is_var2 = ((Argument) second).IsVariable;
		}

		boolean is_lit1 = first instanceof  Literal;
		boolean is_lit2 = second instanceof  Literal;

		if(is_first && !is_var  && is_second && !is_var2){
			// check if names equal
			if(!((Argument) first).name.equals(((Argument) second).name)){
				return null;
			}
			else{
				return substitution;
			}
		}

		else if(is_first && is_var ){
			check_condition_isvalid();
			return unifyVariable((Argument) first, second,substitution);
		}
		else if(is_second && is_var2){
			check_condition_isvalid();
			return unifyVariable((Argument) second, first,substitution);
		}
		else if(is_lit1  &&  is_lit2){
			check_condition_isvalid();
			return unifyLiteral(((Literal)first).args, ((Literal)second).args,substitution);
		}
		else{
			return null;
		}

	}


	static HashMap<String, String> unifyVariable(Argument args, Type var,HashMap<String, String> theta){

		if(theta==null)
		{
			return null;
		}
		else{
			check_condition_isvalid();
		}

		for(String v: theta.keySet()) {
			if (!v.equals(args.name) || theta.get(v) == null) {
				check_condition_isvalid();
			}
			else if (v.equals(args.name) && theta.get(v) != null) {
				Argument temp = new Argument(theta.get(v));
				return unification(temp, var,theta);
			}
			else{
				check_condition_isvalid();
			}
		}
		Set<String> set = theta.keySet();
		String var_string = var.toString();
		boolean chk_contains = set.contains(var_string);
		boolean is_null = true;
		if(theta.get(var_string) != null){
			is_null = false;
		}

		if(chk_contains && (is_null == false) ){
			return unification(args, new Argument(theta.get(var_string)),theta);
		}
		String arg_name = args.name;

		if(!set.contains(arg_name) || (theta.get(arg_name) == null)) {
			String lower = null;
			if(theta.get(arg_name) != null){
				lower = theta.get(arg_name).toLowerCase();
			}

			if(theta.get(arg_name)!=null && theta.get(arg_name).equals(lower))
			{
				theta.put(arg_name, var_string);
			}
			theta.put(arg_name, var_string);

			return theta;
		}

		return theta;
	}


	static Clause substitute(Clause c,  HashMap<String, String> subs ){
		// replacing values from Subs map
		for(Literal l : c.list){
			for(Argument a : l.args){
				if(a.IsVariable == true){
					int index = l.args.indexOf(a);
					String substi = subs.get(a.name);
					if(substi  != null){
						Argument subst_arg = new Argument(substi); 
						l.args.set(index, subst_arg);
					}
				}
			}
		}

		if(c.list.size()==0){
			c = null;
		}
		return c;
	}

	HashMap<String, String> mod_subs(HashMap<String, String> subs){
		if(subs == null){
			return null;
		}
		for(String key : subs.keySet()){
			String value = subs.get(key);
			if(value.charAt(0) >= 97 && value.charAt(0)<=122){
				// is variable
				String value2 = subs.get(value);
				if(value2 != null){
					subs.put(key, value2);}
			}
		}
		return subs;
	}

	// standardizing variables
	Clause std_var(Clause c1, Clause c2){
		HashSet<String> set1 = new HashSet<String>();
		HashSet<String> set2 = new HashSet<String>();
		// get_args
		for(Literal l : c1.list){
			for(Argument a : l.args){
				if(a.IsVariable == true){
					set1.add(a.name);
				}
			}
		}

		for(Literal l : c2.list){
			for(Argument a : l.args){
				if(a.IsVariable == true){
					set2.add(a.name);
				}
			}
		}


		for(Literal l : c2.list){
			for(Argument a : l.args){
				// if 'set' contains the argument 'a', then replace it with new argument a2
				if(a.IsVariable == true && set1.contains(a.name)){
					Argument a2 = new Argument(new String(a.name+"1"));
					Collections.replaceAll(l.args, a, a2);
				}
			}
		}

		return c2;	
	}

	// returns true if either c1 is the parent of c2, or vice versa, otherwise returns false
	boolean is_parent(Clause c1, Clause c2){
		// check for c1's parent
		if(c1.parents==null && c2.parents==null){
			return false;
		}
		if(c1.parents!= null){
			for(int i = 0;i < 2;i++){
				if(c1.parents[i] == c2){
					return true;
				}
			}
		}
		// check for c2's parent
		if(c2.parents!= null){
			for(int i = 0;i < 2;i++){
				if(c2.parents[i] == c1){
					return true;
				}
			}}
		return false;
	}

	static void check_condition_isvalid(){

	}

}