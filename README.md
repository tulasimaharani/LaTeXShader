# latexToGLSL
Projeto prático em TCCII

Este projeto consiste em um compilador de equações em LaTeX para código de shadding em OpenGLSL.
Como nosso objetivo é compilar equações de shadding, é dado foco apenas nos comandos matemáticos em LaTeX,
em especial, o ambiente *\equation*.

A implementação é feita usando o SableCC para a geração do compilador. É definida no .sable a gramática 
da linguagem fonte, que é dividida em Léxico, Sintático, Sintático Abstrato e Semântico.
