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

import se.his.drts.message.MessagePayload.IncorrectMessageException;

class inheritanceTest2 {
	
	
	/** This is an example of a message type based on the "base" class. 
	 * @author melj
	 *
	 */
	public static class TestUniqueMessage extends UniqueMessage {
		private static UUID uuid = UUID.fromString("979fde02-de91-4fe1-bd21-4a6253758218");
		private String name;
		protected TestUniqueMessage() {
			super(TestUniqueMessage.uuid);
		}
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
		
		
		
	}
	/** This class emulates a "base" class for an application. In this case, an identity
	 * is added, which is unique for each message sent. Further, the full message identity is the UUID and identity
	 * in this case. The message identity is a nested class that is created based on the UUID and the identity. 
	 * 
	 * N.B., the MessageIdentity is ignored to avoid its interference in the serialization process. 
	 * @author melj
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
		/* (non-Javadoc)
		 * @see java.lang.Object#toString()
		 */
		@Override
		public String toString() {
			return "UniqueMessage [subIdentity=" + subIdentity + ", toString()=" + super.toString() + "]";
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
		byte[] byteA = tumA.serialize();
		Optional<MessagePayload> tumA2 = MessagePayload.createMessage(byteA);
		assertTrue(tumA2.isPresent());
		assertTrue(tumA2.get().equals(tumA));
		Envelope envelope = new Envelope(tumA);
		Optional<MessagePayload> tumA3 = envelope.getMessage();
		assertTrue(tumA3.get().equals(tumA));
		byte[] envNetworkMessage = envelope.serialize();
		try {
			Envelope envelope2 = Envelope.createEnvelope(envNetworkMessage);
			Optional<MessagePayload> tumA4 = envelope2.getMessage();
			assertTrue(tumA.equals(tumA4.get()));
		} catch (IncorrectMessageException e) {
			fail(e);
		}
	}

}
