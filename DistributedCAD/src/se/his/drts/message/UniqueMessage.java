package se.his.drts.message;

import java.math.BigInteger;
import java.util.Random;
import java.util.UUID;

import com.fasterxml.jackson.annotation.JsonIgnoreType;

public class UniqueMessage extends MessagePayload {
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
		@Override
		public boolean equals(Object obj) {
			final MessageIdentity mi = (MessageIdentity) obj;
			return this.uuid.equals(mi.uuid) && this.subIdentity.equals(mi.subIdentity);
		}
		public final BigInteger getSubIdentity() {
			return subIdentity;
		}
		public final UUID getUuid() {
			return uuid;
		}
		@Override
		public int hashCode() {
			return this.uuid.hashCode()+this.subIdentity.hashCode();
		}
		@Override
		public String toString() {
			return "MessageIdentity [uuid=" + uuid + ", subIdentity=" + subIdentity + "]";
		}
	}
	/*
	 * 
	 * 
	 */
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
	public final BigInteger getSubIdentity() {
		return subIdentity;
	}
	
	public final MessageIdentity getMessageIdentity() {
		return new MessageIdentity(this.getUuid(),this.subIdentity);
	}
}
