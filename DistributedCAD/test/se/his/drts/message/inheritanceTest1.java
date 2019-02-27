package se.his.drts.message;

import static org.junit.jupiter.api.Assertions.*;

import java.math.BigInteger;
import java.util.Optional;
import java.util.UUID;

import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import com.fasterxml.jackson.annotation.JsonIgnoreType;

class inheritanceTest1 {
	
	
	/** This is an example of a message type based on the "base" class. 
	 * @author melj
	 *
	 */
	public static class TestUniqueMessage extends UniqueMessage {
		private static UUID uuid = UUID.fromString("979fde02-de91-4fe1-bd21-4a6253758218");
		private String name;
		/**Empty constructor used by Jackson.
		 * 
		 */
		protected TestUniqueMessage() {
			super(TestUniqueMessage.uuid);
		}
		/**The actual constructor of this class.
		 * @param name
		 */
		public TestUniqueMessage(String name) {
			super(TestUniqueMessage.uuid);
			this.name = name;
		}
		/**
		 * @return the name
		 */
		public final String getName() {
			return name;
		}

		/* (non-Javadoc)
		 * @see java.lang.Object#toString()
		 */
		@Override
		public String toString() {
			return "TestUniqueMessage [name=" + name + ", toString()=" + super.toString() + "]";
		}
		/* (non-Javadoc)
		 * @see se.his.drts.message.MessagePayload#equals(java.lang.Object)
		 */
		@Override
		public boolean equals(Object obj) {
			return super.equals(obj) && this.name.equals(((TestUniqueMessage)obj).name);
		}
		/* (non-Javadoc)
		 * @see se.his.drts.message.MessagePayload#compareTo(se.his.drts.message.MessagePayload)
		 */
		@Override
		public int compareTo(MessagePayload arg0) {
			final int n = super.compareTo(arg0);
			if (n!=0) {
				return n;
			}
			return this.name.compareTo(((TestUniqueMessage)arg0).name);
		}
		/* (non-Javadoc)
		 * @see java.lang.Object#hashCode()
		 */
		@Override
		public int hashCode() {
			return super.hashCode()+name.hashCode();
		}
		
		
		
	}
	/** This class emulates a "base" class for an application. In this case, an identity
	 * is added, which is unique for each message sent. Further, the full message identity is the UUID and identity
	 * in this case. The message identity is a nested class that is created based on the UUID and the identity. 
	 * 
	 * N.B., the MessageIdentity is ignored to avoid its interference in the serialization process. 
	 * @author Jonas Mellin
	 *
	 */
	public static class UniqueMessage extends MessagePayload {
		@JsonIgnoreType
		public static class MessageIdentity implements Comparable<MessageIdentity> {
			private UUID uuid;
			private BigInteger subIdentity;
			public MessageIdentity(UUID uuid2, BigInteger subIdentity2) {
				this.uuid = uuid2;
				this.subIdentity = subIdentity2;
			}
			@Override
			public int compareTo(MessageIdentity arg0) {
				final int n = this.uuid.compareTo(arg0.uuid);
				if (n!=0) {
					return n;
				}
				return this.subIdentity.compareTo(arg0.subIdentity);
			}
			/* (non-Javadoc)
			 * @see java.lang.Object#equals(java.lang.Object)
			 */
			@Override
			public boolean equals(Object obj) {
				final MessageIdentity mi = (MessageIdentity) obj;
				return this.uuid.equals(mi.uuid) && this.subIdentity.equals(mi.subIdentity);
			}
			/**
			 * @return the identity
			 */
			public final BigInteger getSubIdentity() {
				return subIdentity;
			}
			/**
			 * @return the uuid
			 */
			public final UUID getUuid() {
				return uuid;
			}
			/* (non-Javadoc)
			 * @see java.lang.Object#hashCode()
			 */
			@Override
			public int hashCode() {
				return this.uuid.hashCode()+this.subIdentity.hashCode();
			}
			/* (non-Javadoc)
			 * @see java.lang.Object#toString()
			 */
			@Override
			public String toString() {
				return "MessageIdentity [uuid=" + uuid + ", subIdentity=" + subIdentity + "]";
			}

			
			
			
			
		}
		private static BigInteger nextSubIdentity = BigInteger.ONE;
		private static UUID uuid = UUID.fromString("32eb76f7-e72b-4fa5-ad02-95d92115c45d");
		private BigInteger subIdentity;
		protected UniqueMessage() {
			super(UniqueMessage.uuid);
			synchronized(UniqueMessage.nextSubIdentity) {
				this.subIdentity = UniqueMessage.nextSubIdentity;
				UniqueMessage.nextSubIdentity = UniqueMessage.nextSubIdentity.add(BigInteger.ONE);
			}
		}
		protected UniqueMessage(UUID uuid) {
			super(uuid);
			synchronized(UniqueMessage.nextSubIdentity) {
				this.subIdentity = UniqueMessage.nextSubIdentity;
				UniqueMessage.nextSubIdentity = UniqueMessage.nextSubIdentity.add(BigInteger.ONE);
			}
		}
		/**
		 * @return the identity
		 */
		public final BigInteger getSubIdentity() {
			return subIdentity;
		}
		
		public final MessageIdentity getMessageIdentity() {
			return new MessageIdentity(this.getUuid(),this.subIdentity);
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
		TestUniqueMessage tumA = new TestUniqueMessage("A");
		TestUniqueMessage tumB = new TestUniqueMessage("B");
		byte[] byteA = tumA.serialize();
		byte[] byteB = tumB.serialize();
		Optional<MessagePayload> tumA2 = MessagePayload.createMessage(byteA);
		Optional<MessagePayload> tumB2 = MessagePayload.createMessage(byteB);
		assertTrue(tumA2.isPresent());
		assertTrue(tumB2.isPresent());
		assertTrue(tumA2.get().equals(tumA));
		assertTrue(tumB2.get().equals(tumB));
	}

}
