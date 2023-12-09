package LaTeXShader.semantic;

import java.util.ArrayList;
import java.util.Hashtable;
import java.util.List;
import java.util.Stack;

import LaTeXShader.analysis.DepthFirstAdapter;
import LaTeXShader.node.AADimAExp;
import LaTeXShader.node.AADivAExp;
import LaTeXShader.node.AAEquacao;
import LaTeXShader.node.AAEquacaoTrigAExp;
import LaTeXShader.node.AAFaCdotAExp;
import LaTeXShader.node.AAFaTimesAExp;
import LaTeXShader.node.AAFmEulerAExp;
import LaTeXShader.node.AAFmFracAExp;
import LaTeXShader.node.AAFmMaxAExp;
import LaTeXShader.node.AAFmMinAExp;
import LaTeXShader.node.AAFmSqrtAExp;
import LaTeXShader.node.AAIdAExp;
import LaTeXShader.node.AAIdModificadoAExp;
import LaTeXShader.node.AAListaEquacoesABlocoEquacoes;
import LaTeXShader.node.AAMultAExp;
import LaTeXShader.node.AANegativoAExp;
import LaTeXShader.node.AANumeroAExp;
import LaTeXShader.node.AAParametrosAExp;
import LaTeXShader.node.AAPotAExp;
import LaTeXShader.node.AASomaAExp;
import LaTeXShader.node.AAVetorFaAExp;
import LaTeXShader.node.AAVetorIdFaAExp;
import LaTeXShader.node.AAVetorTridimensionalAExp;
import LaTeXShader.node.Node;
import LaTeXShader.node.PAEquacao;
import LaTeXShader.node.Start;

public class SemanticAnalyser extends DepthFirstAdapter {

	public enum Type {
		Numero(), Vetor(), Identificador(), Equacao();
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
	public void caseAAIdModificadoAExp(AAIdModificadoAExp node) {
		inAAIdModificadoAExp(node);
		if (node.getId() != null && node.getModificador() != null) {
			String nomeIdModificado = node.getId().toString() + node.getModificador().toString();
			Type idTable = symbolTable.get(nomeIdModificado);
			if (idTable == null) {
				stack.add(Type.Identificador);
				symbolTable.put(nomeIdModificado, Type.Identificador);
			} else {
				stack.add(idTable);
			}
		} else {
			throw new RuntimeException("Expressão modificada inválida: " + node.toString());
		}
		outAAIdModificadoAExp(node);
	}

	@Override
	public void outAAIdAExp(AAIdAExp node) {
		Type idTable = symbolTable.get(node.toString());
		if (idTable == null) {
			symbolTable.put(node.toString(), Type.Identificador);
			idTable = Type.Identificador;
		} else if (idTable == Type.Equacao) {
			// Pula na arvore para o nó referente a equação encontrada
			String currentEquationTemp = getCurrentEquation();
			JumpCaseAAListaEquacoesABlocoEquacoes((Node) node, node.toString());
			idTable = symbolTable.get(node.toString());
			setCurrentEquation(currentEquationTemp);
		}
		stack.add(idTable);
	}

	public void JumpCaseAAListaEquacoesABlocoEquacoes(Node node, String idEquacao) {
		while (!(node instanceof AAListaEquacoesABlocoEquacoes)) {
			node = (Node) node.parent();
		}
		AAListaEquacoesABlocoEquacoes lista = (AAListaEquacoesABlocoEquacoes) node;
		List<PAEquacao> copy = new ArrayList<PAEquacao>(lista.getAEquacao());
		for (PAEquacao e : copy) {
			if (e.getTkIdentificador().toString().equals(idEquacao)) {
				e.apply(this);
			}
		}
	}

	@Override
	public void inAAListaEquacoesABlocoEquacoes(AAListaEquacoesABlocoEquacoes node) {
		List<PAEquacao> copy = new ArrayList<PAEquacao>(node.getAEquacao());
		for (PAEquacao e : copy) {
			/*
			 * Regista na tabela de simbolos os identificadores das equações dessa lista de
			 * equações antes de realizar a DFS padrão do SableCCC
			 */
			symbolTable.put(e.getTkIdentificador().toString(), Type.Equacao);
		}
	}

	@Override
	public void caseAAEquacao(AAEquacao node) {
		inAAEquacao(node);
		// Checa a existencia da equação na tabela
		Type tipoEquacao = symbolTable.get(node.getTkIdentificador().toString());

		if (tipoEquacao == null || tipoEquacao == Type.Equacao) {
			// Inclui na tabela
			symbolTable.put(node.getTkIdentificador().toString(), Type.Equacao);
			setCurrentEquation(node.getTkIdentificador().toString());
			// Passeio normal
			if (node.getTkIdentificador() != null) {
				node.getTkIdentificador().apply(this);
			}
			if (node.getAExp() != null) {
				node.getAExp().apply(this);
			}
			outAAEquacao(node);
		} else {
			// senão, a equação já foi avaliada
			// não precisa fazer nada
		}
	}

	@Override
	public void outAAEquacao(AAEquacao node) {
		Type tipoTable = symbolTable.get(node.getTkIdentificador().toString());
		Type tipoStack = stack.pop();

		if (tipoStack.equals(Type.Identificador)) {
			symbolTable.put(node.getTkIdentificador().toString(), Type.Numero);
			tipoTable = Type.Numero;
			tipoStack = Type.Numero;
		}

		if (tipoStack.equals(Type.Numero) && tipoTable.equals(Type.Identificador)) {
			symbolTable.put(node.getTkIdentificador().toString(), Type.Numero);
			tipoTable = Type.Numero;
		}

		if (!tipoTable.equals(tipoStack)) {
			throw new RuntimeException("Uma equação deve ser um numero ou um vetor");
		} else {
			System.out.println("-------------------------------------------------");
			System.out.println("A função " + node.getTkIdentificador() + "é do tipo " + tipoTable);
		}
	}

	@Override
	public void outAASomaAExp(AASomaAExp node) {
		System.out.println("-------------------------------------------------");
		System.out.println("O nó é " + node.getClass().getSimpleName());

		Type tipoDireitoStack = stack.pop();
		Type tipoEsquerdoStack = stack.pop();

		if (tipoDireitoStack.equals(Type.Identificador)) {
			Type tipoDireitoTable = symbolTable.get(node.getLadoDireito().toString());
			if (tipoDireitoTable == null || tipoDireitoTable.equals(Type.Identificador)) {
				symbolTable.put(node.getLadoDireito().toString(), Type.Numero);
				tipoDireitoStack = Type.Numero;
			} else {
				tipoDireitoStack = tipoDireitoTable;
			}
		}

		if (tipoEsquerdoStack.equals(Type.Identificador)) {
			Type tipoEsquerdoTable = symbolTable.get(node.getLadoEsquerdo().toString());
			if (tipoEsquerdoTable == null || tipoEsquerdoTable.equals(Type.Identificador)) {
				symbolTable.put(node.getLadoEsquerdo().toString(), Type.Numero);
				tipoEsquerdoStack = Type.Numero;
			} else {
				tipoEsquerdoStack = tipoEsquerdoTable;
			}
		}

		if ((tipoEsquerdoStack.equals(Type.Vetor) || tipoDireitoStack.equals(Type.Vetor))
				&& (tipoEsquerdoStack.equals(Type.Numero) || tipoDireitoStack.equals(Type.Numero))) {
			throw new RuntimeException("Uma soma deve ser entre dois numeros ou dois vetores");
		}

		System.out.println("-------------------------------------------------");
		System.out.println(tipoEsquerdoStack + " + " + tipoDireitoStack);

		symbolTable.replace(getCurrentEquation(), tipoEsquerdoStack);
		stack.add(tipoEsquerdoStack);

	}

	@Override
	public void outAADimAExp(AADimAExp node) {
		System.out.println("-------------------------------------------------");
		System.out.println("O nó é " + node.getClass().getSimpleName());

		Type tipoDireitoStack = stack.pop();
		Type tipoEsquerdoStack = stack.pop();

		if (tipoDireitoStack.equals(Type.Identificador)) {
			Type tipoDireitoTable = symbolTable.get(node.getLadoDireito().toString());
			if (tipoDireitoTable == null || tipoDireitoTable.equals(Type.Identificador)) {
				symbolTable.put(node.getLadoDireito().toString(), Type.Numero);
				tipoDireitoStack = Type.Numero;
			} else {
				tipoDireitoStack = tipoDireitoTable;
			}
		}

		if (tipoEsquerdoStack.equals(Type.Identificador)) {
			Type tipoEsquerdoTable = symbolTable.get(node.getLadoEsquerdo().toString());
			if (tipoEsquerdoTable == null || tipoEsquerdoTable.equals(Type.Identificador)) {
				symbolTable.put(node.getLadoEsquerdo().toString(), Type.Numero);
				tipoEsquerdoStack = Type.Numero;
			} else {
				tipoEsquerdoStack = tipoEsquerdoTable;
			}
		}

		if ((tipoEsquerdoStack.equals(Type.Vetor) || tipoDireitoStack.equals(Type.Vetor))
				&& (tipoEsquerdoStack.equals(Type.Numero) || tipoDireitoStack.equals(Type.Numero))) {
			throw new RuntimeException("Uma diferença deve ser entre dois numeros ou dois vetores");
		}

		System.out.println("-------------------------------------------------");
		System.out.println(tipoEsquerdoStack + " - " + tipoDireitoStack);

		symbolTable.replace(getCurrentEquation(), tipoEsquerdoStack);
		stack.add(tipoEsquerdoStack);
	}

	@Override
	public void outAADivAExp(AADivAExp node) {
		// numerador / denominador
		System.out.println("-------------------------------------------------");
		System.out.println("O nó é " + node.getClass().getSimpleName());
		AADivAExp aaDivAExp = new AADivAExp();
		aaDivAExp.setDenominador(node.getDenominador());
		aaDivAExp.setNumerador(node.getNumerador());
		operacaoDivisao(aaDivAExp);
	}
	
	@Override
	public void outAAMultAExp(AAMultAExp node) {
		System.out.println("-------------------------------------------------");
		System.out.println("O nó é " + node.getClass().getSimpleName());

		Type tipoDireitoStack = stack.pop();
		Type tipoEsquerdoStack = stack.pop();

		if (tipoDireitoStack.equals(Type.Identificador)) {
			Type tipoDireitoTable = symbolTable.get(node.getLadoDireito().toString());
			if (tipoDireitoTable == null) {
				throw new RuntimeException("Identificador não encontrado: " + node.getLadoDireito());
			}
			if (!tipoDireitoTable.equals(Type.Identificador)) {
				tipoDireitoStack = tipoDireitoTable;
			} else {
				// Caso o id não seja de vetor, tratar como numero
				symbolTable.replace(node.getLadoDireito().toString(), Type.Numero);
				tipoDireitoTable = Type.Numero;
				tipoDireitoStack = Type.Numero;
			}
		}

		if (tipoEsquerdoStack.equals(Type.Identificador)) {
			Type tipoEsquerdoTable = symbolTable.get(node.getLadoEsquerdo().toString());
			if (tipoEsquerdoTable == null) {
				throw new RuntimeException("Identificador não encontrado: " + node.getLadoEsquerdo());
			}
			if (!tipoEsquerdoTable.equals(Type.Identificador)) {
				tipoEsquerdoStack = tipoEsquerdoTable;
			} else {
				// Caso o id não seja de vetor, tratar como numero
				symbolTable.replace(node.getLadoEsquerdo().toString(), Type.Numero);
				tipoEsquerdoTable = Type.Numero;
				tipoEsquerdoStack = Type.Numero;

			}
		}

		// vetor * vetor = erro
		if (tipoEsquerdoStack.equals(Type.Vetor) && tipoDireitoStack.equals(Type.Vetor)) {
			throw new RuntimeException("Não é possivel realizar uma multiplicação entre dois vetores: "
					+ node.getLadoEsquerdo() + "* " + node.getLadoDireito());
		}

		// vetor * numero OU numero * vetor = vetor
		if ((tipoEsquerdoStack.equals(Type.Vetor) || tipoDireitoStack.equals(Type.Vetor))
				&& (tipoEsquerdoStack.equals(Type.Numero) || tipoDireitoStack.equals(Type.Numero))) {
			symbolTable.replace(getCurrentEquation(), Type.Vetor);
			stack.add(Type.Vetor);
		}

		// numero * numero = numero
		if (tipoEsquerdoStack.equals(Type.Numero) && tipoDireitoStack.equals(Type.Numero)) {
			symbolTable.replace(getCurrentEquation(), Type.Numero);
			stack.add(Type.Numero);
		}

		System.out.println("-------------------------------------------------");
		System.out.println(tipoEsquerdoStack + " * " + tipoDireitoStack);
	}

	@Override
	public void outAAPotAExp(AAPotAExp node) {
		// base ^ expoente
		System.out.println("-------------------------------------------------");
		System.out.println("O nó é " + node.getClass().getSimpleName());

		Type expoenteStack = stack.pop();
		Type baseStack = stack.pop();

		if (expoenteStack.equals(Type.Identificador)) {
			Type expoenteTable = symbolTable.get(node.getExpoente().toString());
			if (expoenteTable == null) {
				throw new RuntimeException("Identificador não encontrado: " + node.getExpoente());
			}
			if (!expoenteTable.equals(Type.Identificador)) {
				expoenteStack = expoenteTable;
			} else {
				// Caso o id não seja de vetor, tratar como numero
				symbolTable.replace(node.getExpoente().toString(), Type.Numero);
				expoenteTable = Type.Numero;
				expoenteStack = Type.Numero;
			}
		}

		if (baseStack.equals(Type.Identificador)) {
			Type baseTable = symbolTable.get(node.getBase().toString());
			if (baseTable == null) {
				throw new RuntimeException("Identificador não encontrado: " + node.getBase());
			}
			if (!baseTable.equals(Type.Identificador)) {
				baseStack = baseTable;
			} else {
				// Caso o id não seja de vetor, tratar como numero
				symbolTable.replace(node.getBase().toString(), Type.Numero);
				baseTable = Type.Numero;
				baseStack = Type.Numero;
			}
		}

		// ( numero OU vetor ) ^ vetor = erro
		if (expoenteStack.equals(Type.Vetor)) {
			throw new RuntimeException("Um vetor não pode ser o expoente de uma potência.");
		}

		// numero ^ numero = numero
		if (baseStack.equals(Type.Numero) && expoenteStack.equals(Type.Numero)) {
			symbolTable.replace(getCurrentEquation(), Type.Numero);
			stack.add(Type.Numero);
		}

		// vetor ^ numero = vetor
		if (baseStack.equals(Type.Vetor) && expoenteStack.equals(Type.Numero)) {
			symbolTable.replace(getCurrentEquation(), Type.Vetor);
			stack.add(Type.Vetor);
		}

		System.out.println("-------------------------------------------------");
		System.out.println("(" + node.getBase() + ")" + " ^ " + "(" + node.getExpoente() + ")");
	}

	@Override
	public void outAAVetorTridimensionalAExp(AAVetorTridimensionalAExp node) {
		// (x, y, z)
		System.out.println("-------------------------------------------------");
		System.out.println("O nó é " + node.getClass().getSimpleName());

		Type z = stack.pop();
		Type y = stack.pop();
		Type x = stack.pop();

		// Sempre que um id é encontrado e ele não existe na tabela,
		// ele é inserido na tabela e tratado como número
		if (z.equals(Type.Identificador)) {
			Type zTable = symbolTable.get(node.getZ().toString());
			if (zTable == null) {
				symbolTable.put(node.getZ().toString(), Type.Numero);
				z = Type.Numero;
			}
		}
		if (y.equals(Type.Identificador)) {
			Type yTable = symbolTable.get(node.getY().toString());
			if (yTable == null) {
				symbolTable.put(node.getY().toString(), Type.Numero);
				y = Type.Numero;
			}
		}
		if (x.equals(Type.Identificador)) {
			Type xTable = symbolTable.get(node.getX().toString());
			if (xTable == null) {
				symbolTable.put(node.getX().toString(), Type.Numero);
				x = Type.Numero;
			}
		}

		if (x.equals(Type.Vetor) || y.equals(Type.Vetor) || z.equals(Type.Vetor)) {
			throw new RuntimeException("O componente de um vetor não pode ser um vetor ( " + node.getX() + ", "
					+ node.getY() + ", " + node.getZ() + ")");
		}

		if (!(x.equals(y) && x.equals(z) && y.equals(z))) {
			throw new RuntimeException("Os componentes de um vetor devem ter o mesmo tipo ( " + node.getX() + ", "
					+ node.getY() + ", " + node.getZ() + ")");
		} else {
			System.out.println("-------------------------------------------------");
			System.out.println("X = " + x + "\nY = " + y + "\nZ = " + z);

			symbolTable.replace(getCurrentEquation(), Type.Vetor);
			stack.add(Type.Vetor);
		}
	}

	@Override
	public void outAAVetorFaAExp(AAVetorFaAExp node) {
		// tk_vec fl_chaves '\vec{...}'
		System.out.println("-------------------------------------------------");
		System.out.println("O nó é " + node.getClass().getSimpleName());

		Type typeStack = stack.pop();

		if (typeStack.equals(Type.Numero)) {
			throw new RuntimeException("A expressão " + node.getAExp() + " não pode ser declarada como vetor.");
		}

		if (typeStack.equals(Type.Identificador)) {
			Type typeTable = symbolTable.get(node.toString());
			if (typeTable == null) {
				symbolTable.put(node.toString(), Type.Vetor);
				typeStack = Type.Vetor;
			}
			if (typeTable.equals(Type.Identificador)) {
				symbolTable.replace(node.toString(), Type.Vetor);
				typeStack = Type.Vetor;
			}
			if (typeTable.equals(Type.Numero)) {
				throw new RuntimeException("O identificador " + node.getAExp() + "já foi definido como número.");
			}
		}
		System.out.println("-------------------------------------------------");
		System.out.println(node.toString() + " é um vetor");
		
		symbolTable.replace(getCurrentEquation(), Type.Vetor);
		stack.add(typeStack);
	}

	@Override
	public void outAAVetorIdFaAExp(AAVetorIdFaAExp node) {
		// tk_vec tk_identificador '\vec a'
		System.out.println("-------------------------------------------------");
		System.out.println("O nó é " + node.getClass().getSimpleName());

		Type typeTable = symbolTable.get(node.getTkIdentificador().toString());
		if (typeTable == null || typeTable.equals(Type.Identificador)) {
			symbolTable.put(node.getTkIdentificador().toString(), Type.Vetor);
			typeTable = Type.Vetor;
			stack.add(Type.Vetor);
		}

		Type typeStack = stack.pop();
		if (typeTable.equals(Type.Numero) || typeStack.equals(Type.Numero)) {
			throw new RuntimeException(
					"O identificador " + node.getTkIdentificador() + " já foi definido como número.");
		}

		System.out.println("-------------------------------------------------");
		System.out.println(node.toString() + " é um vetor");

		symbolTable.replace(getCurrentEquation(), Type.Vetor);
		stack.add(Type.Vetor);
	}

	@Override
	public void outAAFaCdotAExp(AAFaCdotAExp node) {
		System.out.println("-------------------------------------------------");
		System.out.println("O nó é " + node.getClass().getSimpleName());

		Type tipoDireito = stack.pop();
		Type tipoEsquerdo = stack.pop();

		if (!(tipoEsquerdo.equals(Type.Vetor) && tipoDireito.equals(Type.Vetor))) {
			throw new RuntimeException("Um produto escalar deve ser entre dois vetores");
		} else {
			System.out.println("-------------------------------------------------");
			System.out.println(node.getLadoEsquerdo() + " cdot(escalar) " + node.getLadoDireito());

			symbolTable.replace(getCurrentEquation(), Type.Numero);
			stack.add(Type.Numero);
		}
	}

	@Override
	public void outAAFaTimesAExp(AAFaTimesAExp node) {
		System.out.println("-------------------------------------------------");
		System.out.println("O nó é " + node.getClass().getSimpleName());

		Type tipoDireito = stack.pop();
		Type tipoEsquerdo = stack.pop();

		if (!(tipoEsquerdo.equals(Type.Vetor) && tipoDireito.equals(Type.Vetor))) {
			throw new RuntimeException("Um produto vetorial deve ser entre dois vetores");
		} else {
			System.out.println("-------------------------------------------------");
			System.out.println(tipoDireito + " times(vetorial) " + tipoEsquerdo);

			symbolTable.replace(getCurrentEquation(), Type.Vetor);
			stack.add(Type.Vetor);
		}
	}

	@Override
	public void outAAEquacaoTrigAExp(AAEquacaoTrigAExp node) {
		// fl_trigonometrica fl_chaves '\sen{...}'
		System.out.println("-------------------------------------------------");
		System.out.println("O nó é " + node.getClass().getSimpleName());

		Type typeStack = stack.pop();

		if (typeStack.equals(Type.Vetor)) {
			throw new RuntimeException("A expressão " + node.getAFlTrigonometrica() + " deve ser aplicada em um número.");
		}

		if (typeStack.equals(Type.Identificador)) {
			Type typeTable = symbolTable.get(node.toString());
			if (typeTable == null) {
				symbolTable.put(node.toString(), Type.Numero);
				typeStack = Type.Numero;
			} else if (typeTable.equals(Type.Identificador)) {
				symbolTable.replace(node.toString(), Type.Numero);
				typeStack = Type.Numero;
			} 
		}
		
		System.out.println("-------------------------------------------------");
		System.out.println(node.toString() + " é um numero");
		
		symbolTable.replace(getCurrentEquation(), Type.Numero);
		stack.add(typeStack);
	}

	@Override
	public void outAAFmFracAExp(AAFmFracAExp node) {
		// \frac{numerador}/{denominador}
		System.out.println("-------------------------------------------------");
		System.out.println("O nó é " + node.getClass().getSimpleName());
		AADivAExp aaDivAExp = new AADivAExp();
		aaDivAExp.setDenominador(node.getDenominador());
		aaDivAExp.setNumerador(node.getNumerador());
		operacaoDivisao(aaDivAExp);
	}

	private void operacaoDivisao(AADivAExp node) {
		// numerador / denominador
		System.out.println("-------------------------------------------------");
		System.out.println("O nó é " + node.getClass().getSimpleName());
		
		Type denominadorStack = stack.pop();
		Type numeradorStack = stack.pop();
		
		if (denominadorStack.equals(Type.Identificador)) {
			Type denominadorTable = symbolTable.get(node.getDenominador().toString());
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
			Type numeradorTable = symbolTable.get(node.getNumerador().toString());
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

}
