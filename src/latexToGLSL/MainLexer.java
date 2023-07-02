package latexToGLSL;

import java.io.*;

import latexToGLSL.lexer.*;
import latexToGLSL.node.*;
import java.io.* ;

public class MainLexer
{
	public static void main(String[] args)
	{
		try
		{
			String arquivo = "src/exemplos/eq_CookTorranceModel.tex";
			//src/exemplos/eq_AshikhminShirleyModel.tex
			//src/exemplos/eq_BlinnPhongModel.tex
			//src/exemplos/eq_CookTorranceModel.tex
			//src/exemplos/eq_WardModel.tex
			Lexer lexer =
					new Lexer(
							new PushbackReader(  
									new FileReader(arquivo), 1024)); 
			Token token;
			while(!((token = lexer.next()) instanceof EOF)) {
				if (token.getClass().getName() != "latexToGLSL.node.TVazio") {
					System.out.print(token.getClass());
					System.out.println(" ( "+token.toString()+")");
				}
			}
		}
		catch(Exception e)
		{
			System.out.println(e.getMessage());
		}
	}
}