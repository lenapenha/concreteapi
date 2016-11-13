package br.com.concrete.api;

import com.google.gson.Gson;

public class Mensagem {
	
	public static final String EMAIL_EXISTE = "E-mail já existente"; 
	public static final String SUCESSO = "Usuário cadastrado com sucesso";
	public static final String INVALIDO = "Usuário e/ou senha inválidos";
	
	private String mensagem;
	private String codigo;
	
	public String getMensagem() {
		return mensagem;
	}

	public void setMensagem(String mensagem) {
		this.mensagem = mensagem;
	}

	public String getCodigo() {
		return codigo;
	}

	public void setCodigo(String codigo) {
		this.codigo = codigo;
	}

	public String getMensagemToString(Mensagem mensagem){
		Gson gson = new Gson();
		String msg = gson.toJson(mensagem);
		
		return msg;
	}
	
	
}
