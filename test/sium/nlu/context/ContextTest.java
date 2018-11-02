/**
 * 
 */
package sium.nlu.context;

import org.junit.Test;

import sium.system.util.FileUtils;
import static org.junit.Assert.*;

/**
 * @author casey
 *
 */
public class ContextTest {

	@Test
	public void testContext() {
		
		Context<String,String> context = new Context<String,String>();
		
		context.addPropertyToEntity("obj1", "red");
		context.addPropertyToEntity("obj1", "square");
		context.addPropertyToEntity("obj2", "square");
		
		assertTrue(context.containsEntity("obj1"));
		
		assertFalse(context.entityContainsProperty("obj2", "beef"));
		
		assertEquals(context.getEntities().size(), 2);
		assertEquals(context.entityHasNumProperties("obj1"), 2);
		assertEquals(context.entityHasNumProperties("obj2"), 1);
		assertEquals(context.getPropertiesSet().size(), 2);
		
		assertTrue(context.entityContainsProperty("obj1", "red"));
		assertTrue(context.entityContainsProperty("obj1", "square"));
		assertTrue(context.entityContainsProperty("obj2", "square"));
		
		context.removePropertyForEntity("obj1", "square");
		assertFalse(context.entityContainsProperty("obj", "square"));
	}
	
	
	@Test
	public void testContextWithConfidenceScores() {
		runDataTests(createContext());
	}
	
	private Context<String,String> createContext() {
		Context<String,String> context = new Context<String,String>();
		context.addPropertyToEntity("obj1", "red", 0.9);
		context.addPropertyToEntity("obj1", "square", 0.8);
		context.addPropertyToEntity("obj2", "square", 0.5);
		return context;
	}
	
	private void runDataTests(Context<String, String> context) {
		assertTrue(context.containsEntity("obj1"));
		
		assertFalse(context.entityContainsProperty("obj2", "beef"));
		
		assertEquals(context.getEntities().size(), 2);
		assertEquals(context.entityHasNumProperties("obj1"), 2);
		assertEquals(context.entityHasNumProperties("obj2"), 1);
		assertEquals(context.getPropertiesSet().size(), 2);
		
		assertTrue(context.entityContainsProperty("obj1", "red"));
		assertTrue(context.entityContainsProperty("obj1", "square"));
		assertTrue(context.entityContainsProperty("obj2", "square"));
		
		assertTrue(context.getPropertyForEntity("obj1", "red").getConfidence() == 0.9);		
	}

	@Test
	public void testContextXml() {
		
		String fileName = "test.xml";
		
		Context<String,String> context = createContext();
		
		context.writeToXml(fileName);
		context = Context.readFromXml(fileName);
		runDataTests(context);
		FileUtils.deleteFile(fileName);
	}
	
}

