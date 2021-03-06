package br.com.concrete.api;

import com.google.gson.Gson;

public class Mensagem {
	
	public static final String EMAIL_EXISTE = "E-mail j� existente"; 
	public static final String SUCESSO = "Usu�rio cadastrado com sucesso";
	public static final String INVALIDO = "Usu�rio e/ou senha inv�lidos";
	public static final String NAO_AUTORIZADO = "N�o autorizado";
	public static final String SESSAO_INVALIDA = "Sess�o Inv�lida";
	
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
