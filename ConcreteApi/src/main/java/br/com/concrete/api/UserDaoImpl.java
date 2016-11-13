package br.com.concrete.api;

import java.security.MessageDigest;
import java.util.Date;
import java.util.List;

import javax.crypto.spec.SecretKeySpec;
import javax.xml.bind.DatatypeConverter;

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
			if(userRtn == null){
				mensagem.setMensagem(Mensagem.INVALIDO);
				mensagem.setCodigo(HttpStatus.BAD_REQUEST.toString());
				return new ResponseEntity<String>(mensagem.getMensagemToString(mensagem), HttpStatus.BAD_REQUEST);
			}
			
			String pwd = getHash(user.getPassword());
			
			if(!userRtn.getPassword().equals(pwd)){
				mensagem.setMensagem(Mensagem.INVALIDO);
				mensagem.setCodigo(HttpStatus.UNAUTHORIZED.toString());
				return new ResponseEntity<String>(mensagem.getMensagemToString(mensagem), HttpStatus.UNAUTHORIZED);
			}
			
			user = new User(userRtn);
			//TODO retornar TOKEN
			userRtn.setLastLogin(new Date());
			session.update(userRtn);
			tx.commit();
			
		} catch (Exception e) {
			if (tx!=null) tx.rollback();
		     throw e;
		}finally {
			session.close();
		}
		
		//TODO retornar TOKEN
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
	public void removeUser(int id) {
		// TODO Auto-generated method stub
		
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
	
	private String getToken(){
		String compactJws = Jwts.builder()
				.setIssuer("concreteApi")
				.setSubject("Joe")
				.signWith(SignatureAlgorithm.HS512, key)
				.compact();
	}

	
	
	//Sample method to construct a JWT
	private String createJWT(String id, String issuer, String subject, long ttlMillis) {

	    //The JWT signature algorithm we will be using to sign the token
	    SignatureAlgorithm signatureAlgorithm = SignatureAlgorithm.HS256;

	    long nowMillis = System.currentTimeMillis();
	    Date now = new Date(nowMillis);

	    //We will sign our JWT with our ApiKey secret
	    byte[] apiKeySecretBytes = DatatypeConverter.parseBase64Binary(apiKey.getSecret());
	    Key signingKey = new SecretKeySpec(apiKeySecretBytes, signatureAlgorithm.getJcaName());

	    //Let's set the JWT Claims
	    JwtBuilder builder = Jwts.builder().setId(id)
	                                .setIssuedAt(now)
	                                .setSubject(subject)
	                                .setIssuer(issuer)
	                                .signWith(signatureAlgorithm, signingKey);

	    //if it has been specified, let's add the expiration
	    if (ttlMillis >= 0) {
	    long expMillis = nowMillis + ttlMillis;
	        Date exp = new Date(expMillis);
	        builder.setExpiration(exp);
	    }

	    //Builds the JWT and serializes it to a compact, URL-safe string
	    return builder.compact();
	}


}
