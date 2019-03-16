package se.his.drts.message;

import java.util.UUID;

public class MessageConfirmed extends UniqueMessage {
	private static UUID uuid = UUID.fromString("979fde02-de91-4fe1-bd21-4a6253758218");
	
	private UUID messageUuid;
	
	protected MessageConfirmed() {
		super(MessageConfirmed.uuid);
	}
	public MessageConfirmed(UUID messageUuid) {
		super(MessageConfirmed.uuid);
		this.messageUuid = messageUuid;	
	}
	public UUID getMessageUuid() {
		return messageUuid;
	}
}
