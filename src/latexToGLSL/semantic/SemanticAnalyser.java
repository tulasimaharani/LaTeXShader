package latexToGLSL.semantic;

import java.util.Hashtable;
import java.util.Stack;

import latexToGLSL.analysis.DepthFirstAdapter;
import latexToGLSL.node.AADimAExp;
import latexToGLSL.node.AADivAExp;
import latexToGLSL.node.AAEquacao;
import latexToGLSL.node.AAFaCdotAExp;
import latexToGLSL.node.AAFaTimesAExp;
import latexToGLSL.node.AAIdAExp;
import latexToGLSL.node.AAIdModificadoAExp;
import latexToGLSL.node.AAMultAExp;
import latexToGLSL.node.AANumeroAExp;
import latexToGLSL.node.AASomaAExp;
import latexToGLSL.node.AAVetorFaAExp;
import latexToGLSL.node.AAVetorIdFaAExp;
import latexToGLSL.node.AAVetorTridimensionalAExp;
import latexToGLSL.node.Start;

public class SemanticAnalyser extends DepthFirstAdapter {
	
	public enum Type {
		Numero(),
		Vetor(),
		Identificador(),
		Equacao();
	}
	
	private Hashtable<String, Type> symbolTable = new Hashtable<>();
	private Stack<Type> stack = new Stack<>();
	private String currentEquation;
	
	public String getCurrentEquation() {
		return currentEquation;
	}

	public void setCurrentEquation(String currentEquation) {
		this.currentEquation = currentEquation;
	}

	public Type verifyTypeOnTable(String nodeName) {
		return symbolTable.get(nodeName);
	}
	
	public Boolean isIdentificador(String nodeName) {
		return symbolTable.get(nodeName).equals(Type.Identificador);
	}
	
	public Boolean isNumero(String nodeName) {
		return symbolTable.get(nodeName).equals(Type.Numero);
	}
	
	public Boolean isVetor(String nodeName) {
		return symbolTable.get(nodeName).equals(Type.Vetor);
	}
	
	@Override
	public void inStart(Start node) {
		System.out.println("-------------------------------------------------");
		System.out.println("Iniciando análise semântica...");
		super.inStart(node);
	}

	@Override
	public void outStart(Start node) {
		System.out.println("-------------------------------------------------");
	    System.out.println("Fim da análise semântica");
	    System.out.println("-------------------------------------------------");
		super.outStart(node);
	}
	
	@Override
	public void outAANumeroAExp(AANumeroAExp node) {
		stack.add(Type.Numero);
	}

	@Override
	public void outAAIdAExp(AAIdAExp node) {
		stack.add(Type.Identificador);
		symbolTable.put(node.toString(), Type.Identificador);
	}

	@Override
	public void outAAIdModificadoAExp(AAIdModificadoAExp node) {
		stack.add(Type.Identificador); //node.getId();
		stack.add(Type.Identificador); //node.getModificador()
		symbolTable.put(node.getId().toString(), Type.Identificador);
	}

	@Override
	public void inAAEquacao(AAEquacao node) { 
		stack.add(Type.Equacao);
		if (symbolTable.get(node.getTkIdentificador().toString()) == null) {
			symbolTable.put(node.getTkIdentificador().toString(), Type.Equacao);
			setCurrentEquation(node.getTkIdentificador().toString());
		}
	}
	
	@Override
	public void inAAListaEquacoesABlocoEquacoes(AAListaEquacoesABlocoEquacoes node) {
		List<PAEquacao> copy = new ArrayList<PAEquacao>(node.getAEquacao());
		for (PAEquacao e : copy) {
			/*
			 * Regista na tabela de simbolos os identificadores
			 * das equações dessa lista de equações antes de realizar 
			 * a DFS padrão do SableCCC
			 */
			symbolTable.put(e.getTkIdentificador().toString(), Type.Equacao);
		}
	}

	@Override
	public void outAAEquacao(AAEquacao node) {
		Type nodeAux = symbolTable.get(node.getTkIdentificador().toString());
		
		Type tipoEquacao = stack.peek();
		if (tipoEquacao.equals(Type.Numero) || 
				tipoEquacao.equals(Type.Vetor)) //TODO: checar tipo da função na tabela 
			stack.pop();
		
		if (!nodeAux.equals(tipoEquacao)) {
			throw new RuntimeException("Uma equação deve ser um numero ou um vetor");
		} else {
			System.out.println("-------------------------------------------------");
			System.out.println("A função " + node.getTkIdentificador() + "é do tipo " + nodeAux);
			stack.pop();
		}
	}

	@Override
	public void outAASomaAExp(AASomaAExp node) {
		System.out.println("-------------------------------------------------");
		System.out.println("O nó é " + node.getClass().getSimpleName());
		
		Type tipoRightStack = stack.pop();
		Type tipoLeftStack = stack.pop();

		Type tipoRightTable = symbolTable.get(node.getLadoDireito().toString());
		Type tipoLeftTable = symbolTable.get(node.getLadoEsquerdo().toString());
		
		if (tipoRightStack.equals(Type.Identificador) && !tipoRightTable.equals(Type.Identificador)) {
			tipoRightStack = tipoRightTable;
		}
		
		if (tipoLeftStack.equals(Type.Identificador) && !tipoLeftTable.equals(Type.Identificador)) {
			tipoLeftStack = tipoLeftTable;
		}
		
		if (!(tipoLeftStack.equals(Type.Numero) && tipoRightStack.equals(Type.Numero)) ||
			!(tipoLeftStack.equals(Type.Vetor) && tipoRightStack.equals(Type.Vetor))) {
			throw new RuntimeException("Uma soma deve ser entre dois numeros ou dois vetores");
		} 
		
		System.out.println("-------------------------------------------------");
		System.out.println(tipoLeftStack + " + " + tipoRightStack);
		
		symbolTable.replace(getCurrentEquation(), tipoLeftStack);
		stack.add(tipoLeftStack);
		
	}
	
	@Override
	public void outAADimAExp(AADimAExp node) {
		System.out.println("-------------------------------------------------");
		System.out.println("O nó é " + node.getClass().getSimpleName());
		
		Type tipoRightStack = stack.pop();
		Type tipoLeftStack = stack.pop();

		Type tipoRightTable = symbolTable.get(node.getLadoDireito().toString());
		Type tipoLeftTable = symbolTable.get(node.getLadoEsquerdo().toString());
		
		if (tipoRightStack.equals(Type.Identificador) && !tipoRightTable.equals(Type.Identificador)) {
			tipoRightStack = tipoRightTable;
		}
		
		if (tipoLeftStack.equals(Type.Identificador) && !tipoLeftTable.equals(Type.Identificador)) {
			tipoLeftStack = tipoLeftTable;
		}
		
		if (!(tipoLeftStack.equals(Type.Numero) && tipoRightStack.equals(Type.Numero)) ||
			!(tipoLeftStack.equals(Type.Vetor) && tipoRightStack.equals(Type.Vetor))) {
			throw new RuntimeException("Uma diferença deve ser entre dois numeros ou dois vetores");
		} 
		
		System.out.println("-------------------------------------------------");
		System.out.println(tipoLeftStack + " - " + tipoRightStack);
		
		symbolTable.replace(getCurrentEquation(), tipoLeftStack);
		stack.add(tipoLeftStack);
	}

	@Override
	public void outAADivAExp(AADivAExp node) {
		// numerador / denominador
		System.out.println("-------------------------------------------------");
		System.out.println("O nó é " + node.getClass().getSimpleName());
		
		Type denominadorStack = stack.pop();
		Type numeradorStack = stack.pop();

		Type denominadorTable = symbolTable.get(node.getDenominador().toString());
		Type numeradorTable = symbolTable.get(node.getNumerador().toString());
		
		if (denominadorStack.equals(Type.Identificador)) {
			if (denominadorTable == null) {
				throw new RuntimeException("Identificador não encontrado: " + node.getDenominador());
			}
			if (!denominadorTable.equals(Type.Identificador)) {	
				denominadorStack = denominadorTable;
			} else {
				//Caso o id não seja de vetor, tratar como numero 
				symbolTable.replace(node.getDenominador().toString(), Type.Numero);
				denominadorTable = Type.Numero;
				denominadorStack = Type.Numero;
			}
		}
		
		if (numeradorStack.equals(Type.Identificador)) {
			if (numeradorTable == null) {
				throw new RuntimeException("Identificador não encontrado: " + node.getNumerador());
			}
			if (!numeradorTable.equals(Type.Identificador)) {	
				numeradorStack = numeradorTable;
			} else {
				//Caso o id não seja de vetor, tratar como numero 
				symbolTable.replace(node.getNumerador().toString(), Type.Numero);
				numeradorTable = Type.Numero;
				numeradorStack = Type.Numero;
			}
		}
		
		// numero / vetor = erro
		if (denominadorStack.equals(Type.Vetor)) {
			if (!numeradorStack.equals(Type.Vetor)) {
				throw new RuntimeException("Um número não pode ser dividido para um vetor");
			}
		} 
		
		// numero / numero = numero 
		if (numeradorStack.equals(Type.Numero) && denominadorStack.equals(Type.Numero)) { 
			symbolTable.replace(getCurrentEquation(), Type.Numero);
			stack.add(Type.Numero);
		} 

		// vetor / vetor = vetor 
		if (numeradorStack.equals(Type.Vetor) && denominadorStack.equals(Type.Vetor)) { 
			symbolTable.replace(getCurrentEquation(), Type.Vetor);
			stack.add(Type.Vetor);
		} 
		
		// vetor / numero = vetor
		if (numeradorStack.equals(Type.Vetor) && denominadorStack.equals(Type.Numero)) { 
			symbolTable.replace(getCurrentEquation(), Type.Vetor);
			stack.add(Type.Vetor);
		} 

		System.out.println("-------------------------------------------------");
		System.out.println(numeradorStack + " / " + denominadorStack);
	}

	@Override
	public void outAAMultAExp(AAMultAExp node) {
		System.out.println("-------------------------------------------------");
		System.out.println("O nó é " + node.getClass().getSimpleName());	
		
		Type tipoRightStack = stack.pop();
		Type tipoLeftStack = stack.pop();

		Type tipoRightTable = symbolTable.get(node.getLadoDireito().toString());
		Type tipoLeftTable = symbolTable.get(node.getLadoEsquerdo().toString());
		
		if (tipoRightStack.equals(Type.Identificador)) {
			if (tipoRightTable == null) {
				throw new RuntimeException("Identificador não encontrado: " + node.getLadoDireito());
			}
			if (!tipoRightTable.equals(Type.Identificador)) {	
				tipoRightStack = tipoRightTable;
			} else {
				//Caso o id não seja de vetor, tratar como numero 
				symbolTable.replace(node.getLadoDireito().toString(), Type.Numero);
				tipoRightTable = Type.Numero;
				tipoRightStack = Type.Numero;
			}
		}
		
		if (tipoLeftStack.equals(Type.Identificador)) {
			if (tipoLeftTable == null) {
				throw new RuntimeException("Identificador não encontrado: " + node.getLadoEsquerdo());
			}
			if (!tipoLeftTable.equals(Type.Identificador)) {	
				tipoLeftStack = tipoLeftTable;
			} else {
				//Caso o id não seja de vetor, tratar como numero
				symbolTable.replace(node.getLadoEsquerdo().toString(), Type.Numero);
				tipoLeftTable = Type.Numero;
				tipoLeftStack = Type.Numero;
			}
		}
		
		// vetor * vetor = erro 
		if (tipoLeftStack.equals(Type.Vetor) && tipoRightStack.equals(Type.Vetor)) { 
			throw new RuntimeException("Não é possivel realizar uma multiplicação entre dois vetores" 
					+ node.getLadoEsquerdo() + "* " + node.getLadoDireito());
		} 
		
		// vetor * numero OU numero * vetor = vetor
		if ((tipoLeftStack.equals(Type.Vetor) || tipoRightStack.equals(Type.Vetor)) &&
			(tipoLeftStack.equals(Type.Numero) || tipoRightStack.equals(Type.Numero))) { 
			symbolTable.replace(getCurrentEquation(), Type.Vetor);
			stack.add(Type.Vetor);
		} 
		
		// numero * numero = numero 
		if (tipoLeftStack.equals(Type.Numero) && tipoRightStack.equals(Type.Numero)) { 
			symbolTable.replace(getCurrentEquation(), Type.Numero);
			stack.add(Type.Numero);
		} 
		
		System.out.println("-------------------------------------------------");
		System.out.println(tipoLeftStack + " * " + tipoRightStack);
	}

	@Override
	public void outAAVetorTridimensionalAExp(AAVetorTridimensionalAExp node) {
		// (x, y, z)
		System.out.println("-------------------------------------------------");
		System.out.println("O nó é " + node.getClass().getSimpleName());
		
		Type z = stack.pop();
		Type y = stack.pop();
		Type x = stack.pop();
		
		if (x.equals(Type.Vetor) ||
			y.equals(Type.Vetor) ||
			z.equals(Type.Vetor)) {
			throw new RuntimeException("O componente de um vetor não pode ser um vetor ( "
					+ node.getX() +", "+ node.getY() +", "+node.getZ()+")");
		}

		if (!(x.equals(y) &&
			  x.equals(z) &&
			  y.equals(z)) ) {
			throw new RuntimeException("Os componentes de um vetor devem ter o mesmo tipo ( "
					+ node.getX() +", "+ node.getY() +", "+node.getZ()+")");
		} else {
			System.out.println("-------------------------------------------------");
			System.out.println("X = " + x + 
								"\nY = "+ y +
								"\nZ = "+ z );
			
			symbolTable.replace(getCurrentEquation(), Type.Vetor);
			stack.add(Type.Vetor);
		}
	}
	
	@Override
	public void outAAVetorFaAExp(AAVetorFaAExp node) {
		// tk_vec fl_chaves '\vec{...}'
		System.out.println("-------------------------------------------------");
		System.out.println("O nó é " + node.getClass().getSimpleName());
		
		Type typeTable = symbolTable.get(node.toString());
		Type typeStack = stack.pop();
		
		if (typeTable == null) {
			symbolTable.put(node.toString(), Type.Vetor);
			typeTable = Type.Vetor;
		}
		
		if ( typeTable != null 
				&& !typeTable.equals(typeStack)
				&& !typeStack.equals(Type.Vetor)  ) {
			throw new RuntimeException("Tipo Vetor esperado: " + typeStack);
		} else {
			System.out.println("-------------------------------------------------");
			System.out.println(typeStack + " é um vetor");
			
			symbolTable.replace(getCurrentEquation(), Type.Vetor);
			symbolTable.replace(node.toString(), Type.Vetor);
			stack.add(Type.Vetor);
		}
	}

	@Override
	public void outAAVetorIdFaAExp(AAVetorIdFaAExp node) {
		// tk_vec tk_identificador '\vec a'
		System.out.println("-------------------------------------------------");
		System.out.println("O nó é " + node.getClass().getSimpleName());
		
		Type typeTable = symbolTable.get(node.getTkIdentificador().toString());
		Type typeStack = stack.pop();
		
		if (typeTable == null) {
			symbolTable.put(node.getTkIdentificador().toString(), Type.Vetor);
			typeTable = Type.Vetor;
		}
		
		if (!typeTable.equals(typeStack) || !typeStack.equals(Type.Vetor) ) {
			throw new RuntimeException("Tipo Vetor esperado: " + typeStack);
		} else {
			System.out.println("-------------------------------------------------");
			System.out.println(typeStack + " é um vetor");
			
			symbolTable.replace(getCurrentEquation(), Type.Vetor);
			stack.add(Type.Vetor);
		}
	}

	@Override
	public void outAAFaCdotAExp(AAFaCdotAExp node) {
		System.out.println("-------------------------------------------------");
		System.out.println("O nó é " + node.getClass().getSimpleName());
		
		Type tipoRight = stack.pop();
		Type tipoLeft = stack.pop();

		if (!(tipoLeft.equals(Type.Vetor) && tipoRight.equals(Type.Vetor))) {
			throw new RuntimeException("Um produto escalar deve ser entre dois vetores");
		} else {
			System.out.println("-------------------------------------------------");
			System.out.println(tipoRight + " cdot(escalar) " + tipoLeft);
			
			symbolTable.replace(getCurrentEquation(), Type.Numero);
			stack.add(Type.Numero);
		}
	}
	
	@Override
	public void outAAFaTimesAExp(AAFaTimesAExp node) {
		System.out.println("-------------------------------------------------");
		System.out.println("O nó é " + node.getClass().getSimpleName());
		
		Type tipoRight = stack.pop();
		Type tipoLeft = stack.pop();

		if (!(tipoLeft.equals(Type.Vetor) && tipoRight.equals(Type.Vetor))) {
			throw new RuntimeException("Um produto vetorial deve ser entre dois vetores");
		} else {
			System.out.println("-------------------------------------------------");
			System.out.println(tipoRight + " times(vetorial) " + tipoLeft);
			
			symbolTable.replace(getCurrentEquation(), Type.Vetor);
			stack.add(Type.Vetor);
		}
	}

}
