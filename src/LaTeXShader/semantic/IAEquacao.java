package LaTeXShader.semantic;

import LaTeXShader.node.TTkIdentificador;

public interface IAEquacao {
	/*
	 * Adiciona método abstrato para recuperar o identificador da equação,
	 * o que permitirá registar na tabela de simbolos os identificadores
	 * de equações de uma lista de equações antes de realizar a DFS padrão
	 * do SableCCC
	 */
	public TTkIdentificador getTkIdentificador();
}
