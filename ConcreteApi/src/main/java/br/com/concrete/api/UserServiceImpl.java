package br.com.concrete.api;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;

public class UserServiceImpl implements UserService{
	@Autowired(required = true)
	private UserDao dao;

	@Override
	public ResponseEntity<String> addUser(User user) {
		return getDao().addUser(user);
	}

	@Override
	public ResponseEntity<String> updateUser(User user) {
		return getDao().updateUser(user);
	}

	@Override
	public ResponseEntity<String> login(User user) {
		return getDao().login(user);
	}

	@Override
	public ResponseEntity<String> perfil(Long id, String token) {
		return getDao().perfil(id, token);
	}

	@Override
	public UserDao getDao() {
		return dao;
	}

}
