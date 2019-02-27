package se.his.drts.message;

import java.io.IOException;
import java.util.Optional;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.core.JsonProcessingException;

import se.his.drts.message.MessagePayload.IncorrectMessageException;

public class Envelope {
	private byte[] serializedMessage;
	private byte[] digest;

	
	/**
	 * @return the message
	 */
	public final Optional<MessagePayload> getMessage() {
		return MessagePayload.createMessage(serializedMessage);
	}
	
	/**
	 * @return
	 */
	public final byte[] getSerializedMessage() {
		return this.getSerializedMessage();
	}
	
	public final byte[] getDigest() {
		return this.digest;
	}

	/** Empty constructor, to ensure that Jackson can deserialize
	 * 
	 */
	private Envelope() {
		
	}

	/** Creates an envelope based on the message. Essentially, creates a digest and adds it to the envelope.
	 * @param message
	 */
	public Envelope(MessagePayload message) {
		this.serializedMessage = message.serialize();
		this.digest = MessagePayload.getMessageDigest().digest(this.serializedMessage);
	}
	

	/** Creates an envelope based on the message (header and content) and a digest based on the message. 
	 * @param networkMessage
	 * @return
	 * @throws IncorrectMessageException
	 */
	public static Envelope createEnvelope(byte[] networkMessage) throws IncorrectMessageException {
		try {

			// convert the message and check that the digest is the same as the message
			
			Envelope envelope = (Envelope)MessagePayload.getObjectMapper().readValue(networkMessage, Envelope.class);
			byte[] networkMessageContent = envelope.getSerializedMessage();
			byte[] digest = MessagePayload.getMessageDigest().digest(networkMessageContent);
			String digestAsString = new String(digest);
			String passedDigestAsString = new String(envelope.digest);
			if (digestAsString.compareTo(passedDigestAsString) != 0) {
				throw new MessagePayload.IncorrectMessageException("Network message has an incorrect digest");
			}
			return envelope;
		} catch (IOException e) {
			throw new IllegalArgumentException("Could not deserialize message",e);
		}
	}
	/** Serializes the object into an JSON object.
	 * @return
	 */
	public byte[] serialize() {
		try {
			return MessagePayload.getObjectMapper().writeValueAsBytes(this);
		} catch (JsonProcessingException e) {
			throw new IllegalStateException("Could not serialize the envelope for "+serializedMessage,e);
		}
	}
	/** Deserializes the network message into an Envelope.
	 * @param networkMessage
	 * @return
	 * @throws IncorrectMessageException
	 */
	public static Optional<Envelope> deserialize(byte[] networkMessage) throws IncorrectMessageException {
		Envelope envelope;
		try {
			envelope = Envelope.createEnvelope(networkMessage);
		} catch (IllegalArgumentException | IllegalStateException e) {
			return Optional.empty();
		}
		return Optional.of(envelope);
	}
	
	public boolean validate() {
		final byte[] networkMessageContent = getSerializedMessage();
		final byte[] digest = MessagePayload.getMessageDigest().digest(networkMessageContent);
		if (digest.length != this.digest.length){
			return false;
		}
		final int l = digest.length;
		for (int i=0; i<l; ++i) {
			if (digest[i] != this.digest[i]) {
				return false;
				
			}
		}
		return true;
	}

}
