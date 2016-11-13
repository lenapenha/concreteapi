import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;

import br.com.concrete.api.Phone;
import br.com.concrete.api.User;
import br.com.concrete.api.UserService;

import static org.junit.Assert.*;

import java.util.ArrayList;
import java.util.List;

/*
 * This Java source file was auto generated by running 'gradle init --type java-library'
 * by 'Usuario' at '11/11/16 11:52' with Gradle 2.14.1
 *
 * @author Usuario, @date 11/11/16 11:52
 */
public class LibraryTest {
	
	@Autowired(required = true)
	private UserService userService;
    
	@Test 
    public void testSomeLibraryMethod() {
        Library classUnderTest = new Library();
        assertTrue("someLibraryMethod should return 'true'", classUnderTest.someLibraryMethod());
    }
	
	@Test
	public void cadastroUserOK(){
		
		User user = new User();
		
		user.setName("Jo�o da Silva");
		user.setEmail("joao@silva.org");
		user.setPassword("hunter2");
		List<Phone> phones = new ArrayList<Phone>();
		phones.add(new Phone("21", "987654321"));
		user.setPhones(phones);
		
		ResponseEntity<String> re = userService.addUser(user);
		
		assertEquals("{\"id\":1,\"created\":\"Nov 13, 2016 4:14:50 PM\"}", re.toString());
	}
}
