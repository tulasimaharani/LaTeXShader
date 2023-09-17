package latexToGLSL.semantic;

import java.util.Hashtable;
import java.util.Stack;

import latexToGLSL.analysis.DepthFirstAdapter;
import latexToGLSL.node.AADimAExp;
import latexToGLSL.node.AADivAExp;
import latexToGLSL.node.AAEquacao;
import latexToGLSL.node.AAEquacaoABlocoEquacoes;
import latexToGLSL.node.AAEquacaoTrigAExp;
import latexToGLSL.node.AAEquacaoTrigPot;
import latexToGLSL.node.AAFaCdotAExp;
import latexToGLSL.node.AAFaProdAExp;
import latexToGLSL.node.AAFaSumAExp;
import latexToGLSL.node.AAFaTimesAExp;
import latexToGLSL.node.AAFlChavesAExp;
import latexToGLSL.node.AAFlColchetesAExp;
import latexToGLSL.node.AAFlOperadorCustomAExp;
import latexToGLSL.node.AAFlParentesesAExp;
import latexToGLSL.node.AAFlStyleAExp;
import latexToGLSL.node.AAFmExponencialAExp;
import latexToGLSL.node.AAFmFracAExp;
import latexToGLSL.node.AAFmIntegralAExp;
import latexToGLSL.node.AAFmLimAExp;
import latexToGLSL.node.AAFmMaxAExp;
import latexToGLSL.node.AAFmMinAExp;
import latexToGLSL.node.AAFmSqrtAExp;
import latexToGLSL.node.AAIdModificadoAExp;
import latexToGLSL.node.AAIdentificadorAExp;
import latexToGLSL.node.AAListaEquacoesABlocoEquacoes;
import latexToGLSL.node.AAModAExp;
import latexToGLSL.node.AAMultAExp;
import latexToGLSL.node.AANegativoAExp;
import latexToGLSL.node.AAParametrosAExp;
import latexToGLSL.node.AAPotAExp;
import latexToGLSL.node.AASomaAExp;
import latexToGLSL.node.AATkNumInteiroAExp;
import latexToGLSL.node.AATkNumRealAExp;
import latexToGLSL.node.AAVetorFaAExp;
import latexToGLSL.node.AAVetorIdFaAExp;
import latexToGLSL.node.AAVetorTresAExp;
import latexToGLSL.node.ATkAlphaAExp;
import latexToGLSL.node.ATkArccosAFlTrigonometrica;
import latexToGLSL.node.ATkArccotAFlTrigonometrica;
import latexToGLSL.node.ATkArcsinAFlTrigonometrica;
import latexToGLSL.node.ATkArctanAFlTrigonometrica;
import latexToGLSL.node.ATkBetaAExp;
import latexToGLSL.node.ATkChiAExp;
import latexToGLSL.node.ATkCosAFlTrigonometrica;
import latexToGLSL.node.ATkCoshAFlTrigonometrica;
import latexToGLSL.node.ATkCotAFlTrigonometrica;
import latexToGLSL.node.ATkCothAFlTrigonometrica;
import latexToGLSL.node.ATkCscAFlTrigonometrica;
import latexToGLSL.node.ATkDeltaAExp;
import latexToGLSL.node.ATkEpsilonAExp;
import latexToGLSL.node.ATkEtaAExp;
import latexToGLSL.node.ATkGammaAExp;
import latexToGLSL.node.ATkIotaAExp;
import latexToGLSL.node.ATkKappaAExp;
import latexToGLSL.node.ATkLambdaAExp;
import latexToGLSL.node.ATkMuAExp;
import latexToGLSL.node.ATkNuAExp;
import latexToGLSL.node.ATkOmegaAExp;
import latexToGLSL.node.ATkPhiAExp;
import latexToGLSL.node.ATkPiAExp;
import latexToGLSL.node.ATkPsiAExp;
import latexToGLSL.node.ATkRhoAExp;
import latexToGLSL.node.ATkSecAFlTrigonometrica;
import latexToGLSL.node.ATkSigmaAExp;
import latexToGLSL.node.ATkSinAFlTrigonometrica;
import latexToGLSL.node.ATkSinhAFlTrigonometrica;
import latexToGLSL.node.ATkTanAFlTrigonometrica;
import latexToGLSL.node.ATkTanhAFlTrigonometrica;
import latexToGLSL.node.ATkTauAExp;
import latexToGLSL.node.ATkThetaAExp;
import latexToGLSL.node.ATkUpDeltaAExp;
import latexToGLSL.node.ATkUpGammaAExp;
import latexToGLSL.node.ATkUpLambdaAExp;
import latexToGLSL.node.ATkUpOmegaAExp;
import latexToGLSL.node.ATkUpPhiAExp;
import latexToGLSL.node.ATkUpPiAExp;
import latexToGLSL.node.ATkUpPsiAExp;
import latexToGLSL.node.ATkUpSigmaAExp;
import latexToGLSL.node.ATkUpThetaAExp;
import latexToGLSL.node.ATkUpUpsilonAExp;
import latexToGLSL.node.ATkUpXiAExp;
import latexToGLSL.node.ATkUpsilonAExp;
import latexToGLSL.node.ATkVarepsilonAExp;
import latexToGLSL.node.ATkVarkappaAExp;
import latexToGLSL.node.ATkVarphiAExp;
import latexToGLSL.node.ATkVarpiAExp;
import latexToGLSL.node.ATkVarrhoAExp;
import latexToGLSL.node.ATkVarsigmaAExp;
import latexToGLSL.node.ATkVarthetaAExp;
import latexToGLSL.node.ATkXiAExp;
import latexToGLSL.node.ATkZetaAExp;
import latexToGLSL.node.Node;
import latexToGLSL.node.Start;
import latexToGLSL.node.TTkNumInteiro;

public class SemanticAnalyser extends DepthFirstAdapter {
	
	private Hashtable<Node, Object> symbolTable = new Hashtable<>();
	private Stack<Node> stack = new Stack<>();
		
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
	public void inAAEquacao(AAEquacao node) { 
		stack.add(node.getTkIdentificador());
		symbolTable.put(node.getTkIdentificador(), node.getAExp());
	}

	@Override
	public void outAAEquacao(AAEquacao node) {
		Object nodeAux = symbolTable.get(node.getTkIdentificador());
		
		Node nodeStack = stack.peek();
		if (nodeStack.getClass().equals(nodeAux)) 
			stack.pop();
		
		if (!(nodeAux.getClass().isInstance(AATkNumInteiroAExp.class)) 
			&& !(nodeAux.getClass().isInstance(AATkNumRealAExp.class)) 
			&& !(nodeAux.getClass().isInstance(AAVetorTresAExp.class)) 
			&& !(nodeAux.getClass().isInstance(AAVetorFaAExp.class)) 
			&& !(nodeAux.getClass().isInstance(AAVetorIdFaAExp.class)) ) {
			throw new RuntimeException("Uma equação deve ser um numero ou um vetor");
		} else {
			System.out.println("-------------------------------------------------");
			System.out.println("A função " + node.getTkIdentificador() + "é do tipo " + 
					nodeAux.toString().substring(nodeAux.toString().lastIndexOf('.') + 1)
					);
			stack.pop();
		}
	}

	@Override
	public void outAASomaAExp(AASomaAExp node) {
		System.out.println("-------------------------------------------------");
		System.out.println("O nó é " + node.getClass().getSimpleName());
		
		Node nodeRight = stack.pop();
		Node nodeLeft = stack.pop();

		if (!(nodeLeft instanceof AATkNumInteiroAExp) 
				|| !(nodeRight instanceof AATkNumInteiroAExp)) {
			throw new RuntimeException("Uma soma deve ser entre dois numeros");
		} else {
			System.out.println("-------------------------------------------------");
			System.out.println(nodeLeft.getClass().getSimpleName() 
				+ " + " + nodeRight.getClass().getSimpleName());
			
			Node nodeAux = stack.peek();
			symbolTable.replace(nodeAux, AATkNumInteiroAExp.class);
			stack.add(new AATkNumInteiroAExp());
		}
	}

	@Override
	public void outAADimAExp(AADimAExp node) {
		System.out.println("-------------------------------------------------");
		System.out.println("O nó é " + node.getClass().getSimpleName());
		
		Node nodeRight = stack.pop();
		Node nodeLeft = stack.pop();

		if (!(nodeLeft instanceof AATkNumInteiroAExp) 
				|| !(nodeRight instanceof AATkNumInteiroAExp)) {
			throw new RuntimeException("Uma diferença deve ser entre dois numeros");
		} else {
			System.out.println("-------------------------------------------------");
			System.out.println(nodeLeft.getClass().getSimpleName() 
				+ " - " + nodeRight.getClass().getSimpleName());
			
			Node nodeAux = stack.peek();
			symbolTable.replace(nodeAux, AATkNumInteiroAExp.class);
			stack.add(new AATkNumInteiroAExp());
		}
	}

	@Override
	public void outAADivAExp(AADivAExp node) {
		System.out.println("-------------------------------------------------");
		System.out.println("O nó é " + node.getClass().getSimpleName());
		
		Node nodeRight = stack.pop();
		Node nodeLeft = stack.pop();

		// Tratar divisão por 0?
		if (!(nodeLeft instanceof AATkNumInteiroAExp) 
				|| !(nodeRight instanceof AATkNumInteiroAExp)) {
			throw new RuntimeException("Uma divisão deve ser entre dois numeros");
		} else {
			System.out.println("-------------------------------------------------");
			System.out.println(nodeLeft.getClass().getSimpleName()  
				+ " / " + nodeRight.getClass().getSimpleName());
			
			Node nodeAux = stack.peek();
			symbolTable.replace(nodeAux, AATkNumInteiroAExp.class);
			stack.add(new AATkNumInteiroAExp());
		}
	}

	@Override
	public void outAAMultAExp(AAMultAExp node) {
		System.out.println("-------------------------------------------------");
		System.out.println("O nó é " + node.getClass().getSimpleName());
		
		Node nodeRight = stack.pop();
		Node nodeLeft = stack.pop();

		if (!(nodeLeft instanceof AATkNumInteiroAExp) 
				|| !(nodeRight instanceof AATkNumInteiroAExp)) {
			throw new RuntimeException("Uma multiplicação deve ser entre dois numeros");
		} else {
			System.out.println("-------------------------------------------------");
			System.out.println(nodeLeft.getClass().getSimpleName() 
				+ " * " + nodeRight.getClass().getSimpleName());
			
			Node nodeAux = stack.peek();
			symbolTable.replace(nodeAux, AATkNumInteiroAExp.class);
			stack.add(new AATkNumInteiroAExp());
		}
		
	}
	
	@Override
	public void outAATkNumInteiroAExp(AATkNumInteiroAExp node) {
		stack.add(node);
	}

	@Override
	public void outAATkNumRealAExp(AATkNumRealAExp node) {
		stack.add(node);
	}

	@Override
	public void outAAVetorTresAExp(AAVetorTresAExp node) {
		System.out.println("-------------------------------------------------");
		System.out.println("O nó é " + node.getClass().getSimpleName());
		
		Node z = stack.pop();
		Node y = stack.pop();
		Node x = stack.pop();
		if (
			x.toString().isEmpty() ||
			y.toString().isEmpty() ||
			z.toString().isEmpty()) {
			throw new RuntimeException("O componente de um vetor não podem ser um vetor ( "
					+ node.getX() +", "+ node.getY() +", "+node.getZ()+")");
		}

		if (
			!(	(x instanceof AATkNumInteiroAExp) &&
				(y instanceof AATkNumInteiroAExp) &&
				(z instanceof AATkNumInteiroAExp) ) 
			&&
			!(	(x instanceof AATkNumRealAExp) &&
				(y instanceof AATkNumRealAExp) && 
				(z instanceof AATkNumRealAExp) )) {
			throw new RuntimeException("Os componentes de um vetor devem ter o mesmo tipo ( "
					+ node.getX() +", "+ node.getY() +", "+node.getZ()+")");
		} else {
			System.out.println("-------------------------------------------------");
			System.out.println("X = " + x.toString() + x.getClass().getSimpleName() + 
					"\nY = "+ y + y.getClass().getSimpleName() + 
					"\nZ = "+ z + z.getClass().getSimpleName());
			
			Node nodeAux = stack.peek();
			symbolTable.replace(nodeAux, AAVetorTresAExp.class);
			stack.add(new AATkNumInteiroAExp());
		}
	}

	@Override
	public void inAAVetorIdFaAExp(AAVetorIdFaAExp node) {
		// TODO Auto-generated method stub
		super.inAAVetorIdFaAExp(node);
	}

	@Override
	public void outAAVetorIdFaAExp(AAVetorIdFaAExp node) {
		// TODO Auto-generated method stub
		super.outAAVetorIdFaAExp(node);
	}

	@Override
	public void caseAAVetorIdFaAExp(AAVetorIdFaAExp node) {
		// TODO Auto-generated method stub
		super.caseAAVetorIdFaAExp(node);
	}

	@Override
	public void inAAVetorFaAExp(AAVetorFaAExp node) {
		// TODO Auto-generated method stub
		super.inAAVetorFaAExp(node);
	}

	@Override
	public void outAAVetorFaAExp(AAVetorFaAExp node) {
		// TODO Auto-generated method stub
		super.outAAVetorFaAExp(node);
	}

	@Override
	public void caseAAVetorFaAExp(AAVetorFaAExp node) {
		// TODO Auto-generated method stub
		super.caseAAVetorFaAExp(node);
	}

}
