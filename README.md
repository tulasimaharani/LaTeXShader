# latexToGLSL
### Projeto prático em TCCII

Este projeto consiste em um compilador de equações em LaTeX para código de shadding em OpenGLSL.
Como nosso objetivo é compilar equações de shadding, é dado foco apenas nos comandos matemáticos em LaTeX,
em especial, o ambiente *\equation*.

A implementação é feita usando o SableCC para a geração do compilador. É definida no .sable a gramática 
da linguagem fonte, que é dividida em Léxico, Sintático, Sintático Abstrato e Semântico.


#### Convenção adaptada para expressar BRDFs

| Símbolo                                         | Significado                |
| ----------------------------------------------- | -------------------------- |
|$\vec{\omega_{i}}$ 	                            | direção da luz incidente |
|$\vec{\omega_{o}}$ 	                            | direção da luz refletida |
|$\vec{\omega_{i}}$ = ($\theta_{i}, \phi_{i})$    | coordenadas polares de $\vec{\omega_{i}}$|
|$\vec{\omega_{o}}$ = ($\theta_{o}, \phi_{o})$	  | coordenadas polares de $\vec{\omega_{o}}$|
|$\theta_{i}$									                    | ângulo de elevação da direção da luz incidente|
|$\theta_{o}$									                    | ângulo de elevação da direção da luz refletida|
|$\phi_{i}$          							                | ângulo azimutal da direção da luz incidente|
|$\phi_{o}$         								              | ângulo azimutal  da direção da luz refletida|
|$f$				    							                    | BRDF aproximada|
|$\vec{n}$										                    | vetor normal da superfície|
|$\vec{h}$										                    | vetor halfway|
|$\theta_{h}$									                    | ângulo entre $\vec{n}$ e $\vec{h}$|
|$\theta_{d}$									                    | ângulo entre $\vec{\omega_{i}}$ ou $\vec{\omega_{o}}$ e $\vec{h}$|
|$\rho_{d}$										                    | coeficiente de difusão em RGB|
|$\rho_{s}$										                    | coeficiente especular em RGB|
