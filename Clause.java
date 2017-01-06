
import java.util.HashSet;


public class Clause {
	// list : a set of all the literals contained by the clause
	// parents : An array which consists of the two clauses from which this clause has been resolved, if any.
	HashSet<Literal> list = new HashSet<Literal>();
	Clause[] parents= new Clause[2];

	// default constructor
	Clause(){
		parents = null;
	}

	// creates a new clause consisting of a single literal l
	Clause(Literal l){
		add_literal(l);
		parents = null;
	}

	// copy constructor
	Clause(Clause c){
		list = new HashSet<Literal>(c.list);
		parents = c.parents;
	}

	// creates a new clause from the combination of two clauses
	Clause(Clause c1, Clause c2){
		parents[0] = c1;
		parents[1] = c2;
		for(Literal l1 : c1.list){
			Literal temp = new Literal(l1);
			list.add(temp);
		}

		for(Literal l1 : c2.list){
			Literal temp = new Literal(l1);
			list.add(temp);
		}

	}

	// creates a new clause from the combination of two clauses but deletes all the literals present in to_delete set
	Clause(Clause c1, Clause c2, HashSet<Literal> to_delete){

		parents[0] = c1;
		parents[1] = c2;
		for(Literal l1 : c1.list){
			Literal temp = new Literal(l1);
			if(!to_delete.contains(l1))
				list.add(temp);
		}

		for(Literal l1 : c2.list){
			Literal temp = new Literal(l1);
			if(!to_delete.contains(l1))
				list.add(temp);
		}

	}

	// adding a literal to the current clause
	void add_literal(Literal l){
		list.add(l);

	}
	// returns the number of unique literals in the current clause 
	int get_size(){
		return list.size();
	}

	// returns true if the current clause is equal to the clause c, otherwise returns false
	boolean isEqual(Clause c){
		if(this.list.size() != 1 || c.list.size()!= 1){
			return false;
		}
		for(Literal l : c.list){
			for(Literal l2 : this.list){
				return l.isEqual(l2);
			}
		}
		return false;
	}
}
