package br.com.concrete.api;

import org.springframework.http.ResponseEntity;

public interface UserService {
	public ResponseEntity<String> addUser(User user);
	public ResponseEntity<String> updateUser(User user);
	public ResponseEntity<String> login(User user);
	public ResponseEntity<String> perfil(Long id, String token);
	
	public UserDao getDao();
}
