package se.his.drts.message;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.net.URL;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;


/** The purpose of this class is to serve as a base class for message types. Essentially, it handles serialization and deserialization
 *  from java objects to JSON objects and vice versa. 
 * @author Jonas Mellin
 *
 */
/**
 * @author melj
 *
 */
/**
 * @author melj
 *
 */
public class MessagePayload implements Comparable<MessagePayload> {
	/** The purpose of this class is to provide an exception that can be handled in the
	 * application. Essentially, the application should be able to rake corrective action.
	 * @author melj
	 *
	 */
	public static class IncorrectMessageException extends Exception {

		public IncorrectMessageException(String string) {
			super(string);
		}

		/**
		 * 
		 */
		private static final long serialVersionUID = 2622081140350800568L;

	}
	private UUID uuid = UUID.fromString("a9343c32-e126-4630-82ba-af983a5a7684"); // do not make static!
	private static Pattern uuidPattern = Pattern.compile("^.*\"[uU]{2}[iI][dD]\"\\s*:\\s*\"([^\"]+)\".*$");
	private static Pattern multipleUuidPattern = Pattern.compile("^.*\"[uU]{2}[iI][dD]\"\\s*:.*\"[uU]{2}[iI][dD]\"\\s*:.*$");
	
	private static MessageDigest messageDigest;
	private static Boolean messageDigestInitialized = false;
	
	private static ObjectMapper objectMapper = new ObjectMapper();
	private static Map<UUID,MessagePayload> candidateObjectMap;
	private static Boolean candidateObjectMapInitialized = false;
	
	public static MessageDigest getMessageDigest() {
		initializeMessageDigest();
		return MessagePayload.messageDigest;
	}
	/**
	 * @return the uuid
	 */
	public final UUID getUuid() {
		return uuid;
	}
	
	
	/**Initializes the message digest structure. The reason for encapsulating the initialization in a method 
	 * is that it is impossible to handle exceptions are part of the assignment of attributes 
	 * 
	 */
	private static void initializeMessageDigest() {
		try {
		synchronized(MessagePayload.messageDigestInitialized) {
			if (!MessagePayload.messageDigestInitialized) {
				if (MessagePayload.messageDigest == null) {
					MessagePayload.messageDigest = MessageDigest.getInstance("SHA-1");
				}	
				MessagePayload.messageDigestInitialized = true;
				
			}
		}
		} catch (NoSuchAlgorithmException nsae) {
			System.err.println("Could not initialize digest package");
			System.exit(1);
		}
	}

	/** Constructor used by the automatic systems for deserialization. 
	 * 
	 */
	private MessagePayload() {
		
	}
	
	/**Actual constructor.
	 * @param uuid
	 */
	protected MessagePayload(UUID uuid) {
		this.uuid = uuid;
	}
	
	/** Copy constructor.
	 * @param message
	 */
	protected MessagePayload(MessagePayload message) {
		this.uuid = message.uuid;
	}
	
	/** This method returns an empty prototype object of the right class.
	 * @param uuid the UUID of the message type
	 * @return a prototype object that can be employed to deserialize JSON objects
	 */
	public static MessagePayload getPrototypeMessage(UUID uuid) {
		initializeStaticCandidateObjectMap();
		return MessagePayload.candidateObjectMap.get(uuid);
	}
	
	/**
	 * Initializes the candidateObjectMap in the class Message to prototype objects of subclasses of Message
	 * and Message itself.  
	 */
	private static void initializeStaticCandidateObjectMap() {
		synchronized(MessagePayload.candidateObjectMapInitialized) {
			if (!MessagePayload.candidateObjectMapInitialized) {
				MessagePayload.candidateObjectMap = new ConcurrentHashMap<>();
				Package[] pkg = Package.getPackages();
				for (Package p: pkg) {
					String name = p.getName();
					if (!name.startsWith("/")) {
						name = "/" + name;
					}
					name = name.replace('.', '/');
					URL url = MessagePayload.class.getResource(name);
					if (url == null) {
						continue;
					}
					File directory = new File(url.getFile());
					if (directory.exists()) {
						String [] files = directory.list();
						for (int i=0;i<files.length;i++) {

							// we are only interested in .class files
							if (files[i].endsWith(".class")) {
								// removes the .class extension
								String classname = files[i].substring(0,files[i].length()-6);
								try {
									// Try to create an instance of the object
									Object o = Class.forName(p.getName()+"."+classname).newInstance();
									if (o instanceof MessagePayload) {
										final MessagePayload m = (MessagePayload)o;
										UUID uuid = m.getUuid();
										MessagePayload.candidateObjectMap.put(uuid,m);
									}
								} catch (ClassNotFoundException cnfex) {
									throw new IllegalStateException(cnfex);
								} catch (InstantiationException iex) {
									//throw new IllegalStateException("Could not instantiate class \""+classname+"\", since there is no default constructor",iex);
								} catch (IllegalAccessException iaex) {
									//throw new IllegalStateException("Could not instantiate class \""+classname+"\", since the class is not public",iaex);

								}
							}
						}
					}
				}
				MessagePayload.candidateObjectMapInitialized = true;
			}
		}
		
	}
	
	private static Set<MessagePayload> getCandidateObjectSet() {
		MessagePayload.initializeStaticCandidateObjectMap();
		Set<MessagePayload> candidateObjectSet = new HashSet<>();
		MessagePayload.candidateObjectMap.values().stream().forEach(m -> {
			candidateObjectSet.add(new MessagePayload(m));
		});
		return candidateObjectSet;
	}
	
	private static Optional<MessagePayload> getCandidateObject(UUID uuid) {
		final MessagePayload message = MessagePayload.candidateObjectMap.get(uuid);
		if (message != null) {
			return Optional.of(new MessagePayload(message));
		} else {
			return Optional.empty();
		}
	}
	
	/** Alternative 1 method for creating an object, which is based on looking of the prototype message object in the 
	 * map and use it.
	 * @param networkMessage the JSON object in byte format
	 * @return the Optional<MessagePayload> containing the 
	 * @throws IllegalArgumentException in case something is wrong with the JSON object in the networkMessage
	 */
	private static Optional<MessagePayload> createMessageAttempt1(byte[] networkMessage) {
		
		// get the UUID out of a assumed JSON object

		UUID uuid = MessagePayload.getUUIDFromJSONObject(networkMessage);

		// clone the candidate object
		final MessagePayload prototypeMessage = MessagePayload.getPrototypeMessage(uuid);
		final Optional<MessagePayload> message = prototypeMessage.deserialize(networkMessage);
		return message;
		
	}
	/** Alternative 2 method for creating an object, which is based on going through all subclasses and 
	 * trying to create an object based on any class.
	 * @param networkMessage
	 * @return
	 */
	private static Optional<MessagePayload> createMessageAttempt2(byte[] networkMessage) {
		// more expensive method, go through all subclasses
		
		for (MessagePayload prototypeMessage: MessagePayload.getCandidateObjectSet() ) {
			Optional<MessagePayload> result = prototypeMessage.deserialize(networkMessage);
			if (result.isPresent()) {
				return result;
			}
		}
		return Optional.empty();
		
	}
	
	/** Factory method that creates a message object of the right subclass based on the network messages represented as a byte array. 
	 * In contrast to deserialize, this method is static. 
	 * @param networkMessage the network message received from the network
	 * @return the <code>Optional\<MessagePayload\></code> object deserialized from the networkMessage
	 */
	public static Optional<MessagePayload> createMessage(byte[] networkMessage) {
		Optional<MessagePayload> message = createMessageAttempt1(networkMessage);
		if (message.isPresent() ) {
			return message;
		}
		message = createMessageAttempt2(networkMessage);
		return message;
	}

	
	/** Deserializes a message according the current class. Essentially, the UUID is used to discover a prototype 
	 * object of the right message type. This prototype object will then determine the value of <code> this.getClass()</code>
	 * and, thus, for how jackson should perform deserialization.   
	 * @param networkMessage
	 * @return Optional<MessagePayload>
	 */
	public Optional<MessagePayload> deserialize(byte[] networkMessage) {
		try {
			MessagePayload tmp = MessagePayload.objectMapper.readValue(networkMessage, this.getClass());
			return Optional.of(tmp);
		} catch (IOException e) {
			throw new IllegalArgumentException("Could not deserialize \""+new String(networkMessage)+"\", either something is wrong with the JSON object or the class: \""+this.getClass().getName()+"\"",e);
		}
	}

	/** Deserializes a message according the current class 
	 * @param networkMessage
	 * @return
	 */
	public Optional<MessagePayload> deserialize(String networkMessage) {
		try {
			MessagePayload tmp = MessagePayload.objectMapper.readValue(networkMessage, this.getClass());
			return Optional.of(tmp);
		} catch (IOException e) {
			return Optional.empty();
			// do nothing, we are trying out different possibilities
		}
	}
	/** Serializes a message according the current class 
	 * @return
	 */

	public byte[] serialize() {
		try {
			return MessagePayload.objectMapper.writeValueAsBytes(this);
		} catch (JsonProcessingException e) {
			throw new IllegalArgumentException("Could not marshal the object "+this,e);
		}
	}
	
	/** Serializes a nessage according to the current class in the form of a String. 
	 * @return
	 */
	public String serializeAsString() {
		try {
			return MessagePayload.objectMapper.writeValueAsString(this);
		} catch (JsonProcessingException e) {
			throw new IllegalArgumentException("Could not marshal the object "+this,e);
		}
		
	}

	/* (non-Javadoc)
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString() {
		return "Message [uuid=" + uuid + "]";
	}
	public static ObjectMapper getObjectMapper() {
		return MessagePayload.objectMapper;
	}
	/* (non-Javadoc)
	 * @see java.lang.Object#equals(java.lang.Object)
	 */
	@Override
	public boolean equals(Object obj) {
		return this.uuid.equals(((MessagePayload)obj).getUuid());
	}
	
	/* (non-Javadoc)
	 * @see java.lang.Comparable#compareTo(java.lang.Object)
	 */
	@Override
	public int compareTo(MessagePayload arg0) {
		return this.uuid.compareTo(arg0.getUuid());
	}

	/** Checks some basic things concerning the JSON object and returns the UUID if there is one and only one
	 * such field in the JSON object. 
	 * @param jsonObject
	 * @return
	 */
	public static UUID getUUIDFromJSONObject(String jsonObject) {
		if (jsonObject == null) {
			throw new IllegalArgumentException("Not a JSON Object: null");
		}
		if (jsonObject.length()<2 ) {
			throw new IllegalArgumentException("Not a JSON Object: \""+jsonObject+"\"");
		}
		Matcher incorrectMatcher = MessagePayload.multipleUuidPattern.matcher(jsonObject);
		if (incorrectMatcher.find()) {
			throw new IllegalArgumentException("The JSON Object contains two UUID: \""+jsonObject+"\"");
		}
		UUID result = null;
		Matcher matcher = MessagePayload.uuidPattern.matcher(jsonObject);
		if (!matcher.find()) {
			throw new IllegalArgumentException("Not a serialized MessagePayload object, since it lacks a UUID: \""+jsonObject+"\"");
		}
		try {
			result = UUID.fromString(matcher.group(1)); //throw IllegalArgumentException if it is not a UUID
		} catch (IllegalArgumentException iae) {
			// repack exception to something that is more meaningful in this context
			throw new IllegalArgumentException("Incorrect UUID in JSON Object: \""+jsonObject+"\"",iae);
		}
		return result;
	}
	
	public static UUID getUUIDFromJSONObject(byte[] jsonObject) {
		return getUUIDFromJSONObject(new String(jsonObject));
	}
}
