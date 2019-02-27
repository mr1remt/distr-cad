package se.his.drts.message;

import static org.junit.jupiter.api.Assertions.*;

import java.util.Optional;
import java.util.UUID;

import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class basicTest1 {
	
	public static class TestMessage extends MessagePayload  {
		private String name;
		public TestMessage(MessagePayload message) {
			super(message);
			this.name = ((TestMessage)message).name;
		}
		public TestMessage() {
			super(UUID.fromString("d1ca604c-fba1-4011-ae53-2a622f95c1c8"));
		}
		public TestMessage(String name) {
			super(UUID.fromString("d1ca604c-fba1-4011-ae53-2a622f95c1c8"));
			this.name = name;
			
		}
		
		public String getName() {
			return this.name;
		}
		/* (non-Javadoc)
		 * @see java.lang.Object#equals(java.lang.Object)
		 */
		@Override
		public boolean equals(Object obj) {
			String objName = ((TestMessage)obj).getName();
			return super.equals(obj) && this.name.compareTo(objName)==0;
		}
		/* (non-Javadoc)
		 * @see se.his.drts.message.Message#compareTo(se.his.drts.message.Message)
		 */
		@Override
		public int compareTo(MessagePayload arg0) {
			final int n = super.compareTo(arg0);
			if (n!=0) {
				return n;
			}
			return this.name.compareTo(((TestMessage)arg0).getName());
		}
		/* (non-Javadoc)
		 * @see java.lang.Object#toString()
		 */
		@Override
		public String toString() {
			return "TestMessage [name=" + name + ", getUuid()=" + getUuid() + "]";
		}

		
	}

	@BeforeAll
	static void setUpBeforeClass() throws Exception {
	}

	@AfterAll
	static void tearDownAfterClass() throws Exception {
	}

	@BeforeEach
	void setUp() throws Exception {
	}

	@AfterEach
	void tearDown() throws Exception {
	}

	@Test
	void test() {
		TestMessage testMessage = new TestMessage("Test");
		byte[] networkMessage = testMessage.serialize();
		Optional<MessagePayload> testMessage2 = MessagePayload.createMessage(networkMessage);
		assertTrue(testMessage2.isPresent());
		assertTrue(testMessage2.get().equals(testMessage));
	}

}
