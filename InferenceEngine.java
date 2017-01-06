import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.Scanner;
import java.util.Stack;



public class InferenceEngine {
	static int query_count;
	static Clause[] queries;
	static int kb_count;
	static String[] kb;
	static LinkedList[] processed_kb;
	static HashMap<String, Integer> predicate_map = new HashMap<String, Integer>();
	static HashSet<String> known;
	static HashSet<Clause> old_singles = new HashSet<Clause>();
	static HashSet<Clause> old_multiples = new HashSet<Clause>();
	static HashSet<Clause> mix_kb = new HashSet<Clause>();


	public static void main(String[] args) throws IOException {

		// Get input and process it to create trees with nodes being either operators or operands.
		// A tree is created for every line of input
		// Get Clause, Literal and Argument objects from each tree created
		get_input();
		for(LinkedList<String > list : processed_kb){
			Node root = postfix(list);
			root = remove_implication(root);
			root = remove_negation(root, null);
			root = distribute_And(root);
			get_clauses(root);
		}

		kb = null;
		processed_kb = null;;
		predicate_map = null;
		known = null;

		// store answers for all the queries by creating individual Resolver objects for each query
		boolean answer[] = new boolean[query_count]; 
		for(int i = 0 ; i <query_count ; i++){
			Resolver q = new Resolver(queries[i]);
			answer[i] = q.answer(old_singles, old_multiples);
		}

		// Writing all the answers to the queries in a text file
		File f = new File("output.txt");
		BufferedWriter output = new BufferedWriter(new FileWriter(f));
		try {
			for(int i = 0;i < query_count;i++){
				String str = new String(String.valueOf(answer[i]));
				output.write(str.toUpperCase() +"\n");
			}
			output.close();
		}catch ( IOException e ) {System.out.print(e);}
	}

	static void get_input(){
		File file = new File("input.txt");
		try {
			Scanner sc = new Scanner(file);
			//store all the queries in an array of strings
			query_count = Integer.parseInt(sc.nextLine());
			queries = new Clause[query_count];
			for(int i =0;i < query_count; i++){
				String s = sc.nextLine();
				s = s.replaceAll(" ", ""); // remove all the spaces from the input line
				Literal l =new Literal(s);
				queries[i] = new Clause(l.get_negation(l)); // create negated query to add it to KB
			}

			// store KB statements as array of strings

			known = new HashSet<String>(create_known()); // A set of operators is created
			kb_count = Integer.parseInt(sc.nextLine());
			processed_kb = new LinkedList[kb_count];
			kb = new String[kb_count];
			for(int i = 0 ;i < kb_count;i++){
				kb[i] = sc.nextLine();
				process(kb[i], i, known);
			}
			sc.close();
		}
		catch (FileNotFoundException e) {
			e.printStackTrace();
		}
		return;
	}

	static void get_clauses(Node root){
		Node left = root.left;
		Node right = root.right;
		// if the root is "&",that is AND, then both the child nodes are separate clauses, else root is itself a clause
		if(root.key.equals("&")){
			get_clauses(left);
			get_clauses(right);
		}
		else{
			foundClause(root);
		}
	}

	static void foundClause(Node root){
		Clause c = new Clause();	
		// if the root is "|", that is OR, then we find literals in both the child nodes
		if(root.key.equals("|")){
			Node left = root.left;
			Node right = root.right;
			// find literals in left
			// find literals in right
			c = findLiteral(left, c);
			c = findLiteral(right, c);
		}
		else{
			// predicate found as the root is not an operator
			Literal l = new Literal(root.key);
			l.parent = c;
			c.add_literal(l);
		}

		// store the clauses according to number of literals in a clause
		if(c.get_size() == 1){
			old_singles.add(c);
		}
		else{
			old_multiples.add(c);
		}
	}

	static Clause findLiteral(Node root, Clause c){
		if(root.key.equals("|")){
			Node left = root.left;
			Node right = root.right;
			// find literals in left
			// find literals in right
			c = findLiteral(left, c);
			c = findLiteral(right, c);
		}

		else{
			Literal l = new Literal(root.key);
			l.parent = c;
			c.add_literal(l);
		}
		return c;
	} 

	static Node distribute_And(Node root){
		if(root == null){
			return null;
		}

		root.left = distribute_And(root.left);
		if (root.key.equals("|")) {
			if (root.left.key.equals("&") && root.right.key.equals("&")) {
				//Distributing AND when both children are AND
				Node A = root.left.left;
				Node B = root.left.right;
				Node C = root.right.left;
				Node D = root.right.right;

				Node newRightRight = new Node("|");
				newRightRight.left = B;
				newRightRight.right = D;

				Node newLeftRight = new Node("|");
				newLeftRight.left = A;
				newLeftRight.right = D;

				Node newRightLeft = new Node("|");
				newRightLeft.left = B;
				newRightLeft.right = C;

				Node newLeftLeft = new Node("|");
				newLeftLeft.left = A;
				newLeftLeft.right = C;

				root.left.left = newLeftLeft;
				root.left.right = newLeftRight;
				root.right.left = newRightLeft;
				root.right.right = newRightRight;
				root.key = "&";
			}

			else if (root.left.key.equals("&")) {
				//Distributing AND when only left child is AND
				Node temp = root.right;
				Node newRight = new Node("|");
				newRight.left = root.left.right;
				newRight.right = temp;
				root.right = newRight;				
				root.left.right = temp;
				root.key = "&";
				root.left.key = "|";
			}
			else if ( root.right.key.equals("&")) {
				//Distributing AND when only right child is AND
				Node temp = root.left;
				Node newLeft = new Node("|");
				newLeft.left = temp;				
				newLeft.right = root.right.left;
				root.left = newLeft;				
				root.right.left = temp;				
				root.key = "&";
				root.right.key = "|";
			}
			root.right = distribute_And(root.right);
		}
		return root;

	}

	static Node postfix(LinkedList<String> list){
		int open_negation = 0;
		Stack<Node> predicates = new Stack<Node>();
		Stack<Node> ops = new Stack<Node>();
		for(String s : list){
			// check if a predicate. If yes, then push to predicates stack
			if(known.contains(s) == false){
				predicates.push(new Node(s));
				// checking if previously open negation was pushed to stack
				if(open_negation == 1){
					predicates.push(apply_operator(ops.pop(), predicates.pop(), null));					
					open_negation = 0;
				}
			}
			// if closing bracket found, solve until opening bracket and add result back to predicates 
			else if(s.equals(")"))
			{
				while(ops.peek().key != null ){
					if(ops.peek().key.equals("(") == true ){
						ops.pop();
						break;
					}
					// pop twice from predicate and once from ops and create tree node
					if(ops.peek().key.equals("~")){
						predicates.push(apply_operator(ops.pop(), predicates.pop(), null));					
					}
					else{						
						predicates.push(apply_operator(ops.pop(), predicates.pop(), predicates.pop()));
					}

					if(ops.isEmpty() == true  ){
						break;
					}
				}
			}

			// if open bracket, push to ops, that is Operartor's stack
			else if(s.equals("~")){
				ops.push(new Node(s));
				// check if negation is on an atom or composite object
				if(list.get(list.indexOf(s)+1).equals("(")){
					open_negation = 0;
				}
				else{
					open_negation = 1;
				}
			}
			// checking for other operators 
			else if(known.contains(s) == true){
				ops.push(new Node(s));
			}
		}
		while( !ops.empty()){
			if(ops.peek().key.equals("~")){
				predicates.push(apply_operator(ops.pop(),  predicates.pop(), null));
			}
			else if(ops.peek().key.equals("(")){
				ops.pop();
			}
			else{
				break;
			}
		}
		//the returned node is the root of the tree
		return(predicates.pop());
	}

	static Node apply_operator(Node root, Node right, Node left){
		root.left = left;
		root.right = right;
		return root;
	} 	

	static Node remove_implication(Node root){
		if(root == null){
			return root;
		}
		if(root.key.equals("=>")){
			//changing the implication to OR
			root.key = new String("|");

			// negating the left node of root by creating a new node
			Node left = root.left;
			Node negation = new Node("~");
			negation.right = left;
			root.left = negation;
		}
		root.left = remove_implication(root.left);
		root.right = remove_implication(root.right);
		return root;
	}

	static Node remove_negation(Node root, Node parent){
		int count = 0;
		if(root == null){
			return null;
		}
		if(root.key.equals("~")){
			// root is negation, then left must be null, therefore check right only.
			Node current = root;
			count++;
			while(current.right.key.equals("~")){
				count++;				
				current = current.right;
			}
			current = current.right;

			// current is the term to be  negated   i.e. after negation (predicate or composite predicate)
			// apply negation as per count
			count = count % 2;
			if(count == 0){
				// remove negations
				root= current;
			}
			else{
				// keep one and propogate it
				root = propogate_neg(current);
			}
		}
		root.left = remove_negation(root.left, root);
		root.right = remove_negation(root.right, root);
		return root;
	}

	static Node propogate_neg(Node root){
		if(root == null){
			return null;
		}
		// checking if an operator
		if(known.contains(root.key)){
			if(root.key.equals("|")){
				root.key = new String("&");				
			}
			else if(root.key.equals("&")){
				root.key = new String("|");				
			}
			else if(root.key.equals("~")){
				root = root.right;
			}
			root.left = propogate_neg(root.left);
			root.right = propogate_neg(root.right);
		}
		else{
			// if a predicate is found			
			if(root.key.charAt(0) == '~'){
				// remove negation from the predicate
				root.key = new String(root.key.substring(1, root.key.length()-1));
			}
			else{
				// add negation to the predicate
				root.key = new String("~"+root.key);			
			}
		}
		return root;
	}

	static void process(String s, int index, HashSet<String> known){
		// removing all the spaces
		// read characters
		// find predicates
		// map predicates to no. of arguments
		// convert string to array of strings
		// store in 2D processed array

		s =s.replaceAll(" ", "");
		int len = s.length();		
		int i =0;
		int j =0;
		LinkedList<String> temp = new LinkedList<String>();
		while(i < len && j < len){
			// get first index of predicate and add known operators to list
			while(known.contains(String.valueOf(s.charAt(i)))){
				// creating string of the known character
				// add a linked list to this array
				if((s.charAt(i) != ' ') && (s.charAt(i) != '=') && (s.charAt(i) !='>')){
					if(s.charAt(i) == '~' && s.charAt(i+1) != '('){
						break;
					}
					temp.add(String.valueOf(s.charAt(i)));
				}
				else{
					if(s.charAt(i) == '='){
						temp.add(String.valueOf(s.charAt(i))+">");
						i++;
					}
				}
				i++;
				if(i == len || i > len)
					return ;
			}
			//get last index of a predicate
			j =i;
			while(String.valueOf(s.charAt(j)).equals("(") == false){
				j++;
				if(j == len)
					return ;
			}
			//create substring from i to j
			// creating the predicate and adding to array of strings 
			String pred = new String(s.substring(i, j));
			int k =j;
			j++;
			// adding the arguments to list
			String a = new String(pred + s.substring(k, j+1));
			temp.add(a);
			//adding the processed current string to an array 
			processed_kb[index] = temp;
			//increment for next iteration, look for another predicate in the given string 
			j++;
			if(j == len)
				break;
			i = j;
		}
		return ;
	}

	static HashSet<String> create_known(){
		HashSet<String> known = new HashSet<String>();
		known.add("(");
		known.add(")");
		known.add("=>");
		known.add("&");
		known.add("|");
		known.add("~");
		known.add("^");
		known.add(" ");
		known.add("=");
		known.add(">");
		return known;
	} 

}


