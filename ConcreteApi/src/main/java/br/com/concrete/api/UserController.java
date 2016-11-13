package br.com.concrete.api;

import java.util.Date;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class UserController {
	
	@Autowired
	UserDao userDao;
	
	@RequestMapping(value="/cadastro", method = RequestMethod.POST)
	public ResponseEntity<String> cadastro(@RequestBody User user){
		user.setCreated(new Date());
		ResponseEntity<String>  re = userDao.addUser(user);
		return re;
	}
	
	@RequestMapping(value="/login", method = RequestMethod.POST)
	public ResponseEntity<String> longin(@RequestBody User user){
		ResponseEntity<String>  re = userDao.login(user);
		return re;
	}
	
	@RequestMapping(value="/edicao", method = RequestMethod.POST)
	public ResponseEntity<String> edicao(@RequestBody User user){
		ResponseEntity<String>  re = userDao.updateUser(user);
		return re;
	}
}
