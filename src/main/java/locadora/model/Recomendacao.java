package locadora.model;

// titulo sugerido para um cliente; porque e o titulo que ele alugou e que levou a sugestao (nulo quando e so popular)
public record Recomendacao(Titulo titulo, Titulo porque) {
}
