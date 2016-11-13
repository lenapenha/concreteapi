package br.com.concrete.api;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Date;

import javax.crypto.KeyGenerator;
import javax.crypto.SecretKey;

import org.apache.tomcat.util.codec.binary.Base64;
import org.hibernate.Session;
import org.hibernate.Transaction;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import com.google.gson.Gson;

import br.com.concrete.util.HibernateUtil;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;

@Service
public class UserDaoImpl implements UserDao{

	@Override
	public ResponseEntity<String> addUser(User user) {
		String userStr;

		Session session = HibernateUtil.getSessionFactory().openSession();
		Transaction tx = null;
		try {
			tx = session.beginTransaction();

			if (getUserByEmail(user.getEmail(), session) != null) {
				Mensagem mensagem = new Mensagem();
				mensagem.setMensagem(Mensagem.EMAIL_EXISTE);
				mensagem.setCodigo(HttpStatus.UNAUTHORIZED.toString());
				return new ResponseEntity<String>(mensagem.getMensagemToString(mensagem), HttpStatus.UNAUTHORIZED);
			}else{
				user.setPassword(getHash(user.getPassword()));
				session.save(user);
				tx.commit();
			}
			
			//retorna usuario somente com informacoes necessarias			
			user.setName(null);
			user.setEmail(null);
			user.setPassword(null);

			Gson gson = new Gson();
			userStr = gson.toJson(user);

		} catch (Exception e) {
			if (tx!=null) tx.rollback();
			throw e;
		}finally {
			session.close();
		}

		return new ResponseEntity<String>(userStr, HttpStatus.ACCEPTED);
	}

	@Override
	public ResponseEntity<String> login(User user) {
		Mensagem mensagem = new Mensagem();
		Session session = HibernateUtil.getSessionFactory().openSession();
		Transaction tx = null;

		try {
			tx = session.beginTransaction();

			User userRtn = getUserByEmail(user.getEmail(), session);
			
			//usuario nao encontrado
			if(userRtn == null){
				mensagem.setMensagem(Mensagem.INVALIDO);
				mensagem.setCodigo(HttpStatus.BAD_REQUEST.toString());
				return new ResponseEntity<String>(mensagem.getMensagemToString(mensagem), HttpStatus.BAD_REQUEST);
			}
			
			//validar password
			String pwd = getHash(user.getPassword());
			if(!userRtn.getPassword().equals(pwd)){
				mensagem.setMensagem(Mensagem.INVALIDO);
				mensagem.setCodigo(HttpStatus.UNAUTHORIZED.toString());
				return new ResponseEntity<String>(mensagem.getMensagemToString(mensagem), HttpStatus.UNAUTHORIZED);
			}

			String token = getToken(user.getId()+":"+user.getName());

			user = new User(userRtn);
			user.setToken(token);

			String tokenHs = getHash(token);
			userRtn.setToken(tokenHs);
			userRtn.setLastLogin(new Date());

			session.update(userRtn);
			tx.commit();

		} catch (Exception e) {
			if (tx!=null) tx.rollback();
			throw e;
		}finally {
			session.close();
		}

		user.setName(null);
		user.setEmail(null);
		user.setPassword(null);

		Gson gson = new Gson();
		String userStr = gson.toJson(user);

		return new ResponseEntity<String>(userStr, HttpStatus.ACCEPTED);
	}

	@Override
	public ResponseEntity<String> updateUser(User user) {
		String userStr;
		Session session = HibernateUtil.getSessionFactory().openSession();
		Transaction tx = null;
		try {
			tx = session.beginTransaction();

			//seta o Usuario de retorno antes que a informacao de modificado seja alterado
			User userRtn = getUserById(user.getId(), session);

			user.setModified(new Date());
			session.update(user);
			tx.commit();
			
			//retorna usuario somente com informacoes necessarias
			userRtn.setName(null);
			userRtn.setEmail(null);
			userRtn.setPassword(null);

			Gson gson = new Gson();
			userStr = gson.toJson(userRtn);

		} catch (Exception e) {
			if (tx!=null) tx.rollback();
			throw e;
		}finally {
			session.close();
		}

		return new ResponseEntity<String>(userStr, HttpStatus.ACCEPTED);
	}

	@Override
	public ResponseEntity<String> perfil(Long id, String token) {

		Mensagem mensagem = new Mensagem();
		Session session = HibernateUtil.getSessionFactory().openSession();
		User userRtn;

		try {
			
			if(token == null){ 
				mensagem.setMensagem(Mensagem.NAO_AUTORIZADO);
				mensagem.setCodigo(HttpStatus.UNAUTHORIZED.toString());
				return new ResponseEntity<String>(mensagem.getMensagemToString(mensagem), HttpStatus.UNAUTHORIZED);
			}

			userRtn = getUserById(id, session);
			
			//validar token
			String tokenHs = getHash(token);
			if(!userRtn.getToken().equals(tokenHs)){
				mensagem.setMensagem(Mensagem.NAO_AUTORIZADO);
				mensagem.setCodigo(HttpStatus.UNAUTHORIZED.toString());
				return new ResponseEntity<String>(mensagem.getMensagemToString(mensagem), HttpStatus.UNAUTHORIZED);
			}

			//validar sessao
			Long sessao = System.currentTimeMillis() - userRtn.getLastLogin().getTime(); 
			int exp = (30*60)*1000;
			if(sessao > exp){
				mensagem.setMensagem(Mensagem.SESSAO_INVALIDA);
				mensagem.setCodigo(HttpStatus.UNAUTHORIZED.toString());
				return new ResponseEntity<String>(mensagem.getMensagemToString(mensagem), HttpStatus.UNAUTHORIZED);
			}

		} catch (Exception e) {
			throw e;
		}finally {
			session.close();
		}
		
		//retorna usuario somente com informacoes necessarias
		userRtn.setName(null);
		userRtn.setEmail(null);
		userRtn.setPassword(null);
		userRtn.setToken(null);

		Gson gson = new Gson();
		String userStr = gson.toJson(userRtn);

		return new ResponseEntity<String>(userStr, HttpStatus.ACCEPTED);
	}

	private User getUserByEmail(String email, Session session){
		User user = (User) session.createQuery("from User u where u.email = :email")
				.setParameter("email", email)
				.uniqueResult();

		return user;
	}

	private User getUserById(Long id, Session session){
		User user = (User) session.createQuery("from User u where u.id = :id")
				.setParameter("id", id)
				.uniqueResult();

		return user;
	}

	private String getHash(String str){
		String hash = null;
		try {
			MessageDigest algorithm = MessageDigest.getInstance("MD5");
			byte messageDigest[] = algorithm.digest(str.getBytes("UTF-8"));
			hash = new String(messageDigest);
		} catch (Exception e) {
			// TODO: handle exception
		}

		return hash;
	}

	private String getToken(String sub){
		long nowMillis = System.currentTimeMillis();
		Date now = new Date(nowMillis);
		SecretKey secretKey = null;
		try {
			secretKey = KeyGenerator.getInstance("AES").generateKey();
		} catch (NoSuchAlgorithmException e) {
			e.printStackTrace();
		}
		String encodedKey = Base64.encodeBase64String(	secretKey.getEncoded());

		String compactJws = Jwts.builder()
				.setSubject(sub)
				.setIssuedAt(now)
				.signWith(SignatureAlgorithm.HS256, encodedKey)
				.compact();

		return compactJws;
	}

}
