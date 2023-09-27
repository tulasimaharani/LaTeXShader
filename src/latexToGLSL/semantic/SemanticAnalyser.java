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
		Identificador();
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
		stack.add(Type.Identificador);
		symbolTable.put(node.getTkIdentificador().toString(), Type.Identificador);
		setCurrentEquation(node.getTkIdentificador().toString());
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
		
		Type tipoRight = stack.pop();
		Type tipoLeft = stack.pop();

		if (!(tipoLeft.equals(Type.Numero) && tipoRight.equals(Type.Numero)) &&
			!(tipoLeft.equals(Type.Vetor) && tipoRight.equals(Type.Vetor))   &&
		    !(tipoLeft.equals(Type.Identificador) && tipoRight.equals(Type.Identificador)) ) {
			throw new RuntimeException("Uma soma deve ser entre dois numeros ou dois vetores");
		} else {
			System.out.println("-------------------------------------------------");
			System.out.println(tipoLeft + " + " + tipoRight);
			
			symbolTable.replace(getCurrentEquation(), tipoLeft);
			stack.add(tipoLeft);
		}
	}
	
	@Override
	public void outAADimAExp(AADimAExp node) {
		System.out.println("-------------------------------------------------");
		System.out.println("O nó é " + node.getClass().getSimpleName());
		
		Type tipoRight = stack.pop();
		Type tipoLeft = stack.pop();

		if (!(tipoLeft.equals(Type.Numero) && tipoRight.equals(Type.Numero)) &&
			!(tipoLeft.equals(Type.Vetor) && tipoRight.equals(Type.Vetor))   &&
			!(tipoLeft.equals(Type.Identificador) && tipoRight.equals(Type.Identificador)) ) {
			throw new RuntimeException("Uma diferença deve ser entre dois numeros");
		} else {
			System.out.println("-------------------------------------------------");
			System.out.println(tipoLeft + " - " + tipoRight);
			
			symbolTable.replace(getCurrentEquation(), tipoLeft);
			stack.add(tipoLeft);
		}
	}

	@Override
	public void outAADivAExp(AADivAExp node) {
		System.out.println("-------------------------------------------------");
		System.out.println("O nó é " + node.getClass().getSimpleName());
		
		Type denominador = stack.pop();
		Type numerador = stack.pop();

		if (!(denominador.equals(Type.Numero) && numerador.equals(Type.Numero))) { 
			//se os ids representarem numeros
			//(!tipoLeft.equals(Type.Identificador) && !tipoRight.equals(Type.Identificador)) ) { 
			throw new RuntimeException("Uma divisão deve ser entre dois numeros");
		} else {
			System.out.println("-------------------------------------------------");
			System.out.println(numerador + " / " + denominador);
			
			symbolTable.replace(getCurrentEquation(), denominador);
			stack.add(denominador);
		}
	}

	@Override
	public void outAAMultAExp(AAMultAExp node) {
		System.out.println("-------------------------------------------------");
		System.out.println("O nó é " + node.getClass().getSimpleName());	
		
		Type tipoRight = stack.pop();
		Type tipoLeft = stack.pop();

		if (!(tipoLeft.equals(Type.Numero) && tipoRight.equals(Type.Numero)) &&
				//numero * vetor = vetor
				//!(tipoLeft.equals(Type.Vetor) && tipoRight.equals(Type.Vetor))   &&
			//se os ids representarem numeros ou um numero e um vetor
			!(tipoLeft.equals(Type.Identificador) && tipoRight.equals(Type.Identificador)) ) {
			throw new RuntimeException("Uma multiplicação deve ser entre dois numeros");
		} else {
			System.out.println("-------------------------------------------------");
			System.out.println(tipoLeft + " * " + tipoRight);
			
			symbolTable.replace(getCurrentEquation(), tipoLeft);
			stack.add(tipoLeft);
		}
		
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
		// tk_vec fl_chaves \vec{...}
		System.out.println("-------------------------------------------------");
		System.out.println("O nó é " + node.getClass().getSimpleName());
		Type typeTable = symbolTable.get(node.toString());
		Type typeStack = stack.pop();
		
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
		// tk_vec tk_identificador \vec{a}
		System.out.println("-------------------------------------------------");
		System.out.println("O nó é " + node.getClass().getSimpleName());
		
		Type typeTable = symbolTable.get(node.getTkIdentificador().toString());
		Type typeStack = stack.pop();
		
		if ( (typeTable != null && !typeTable.equals(typeStack)) || !typeStack.equals(Type.Vetor) ) {
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
