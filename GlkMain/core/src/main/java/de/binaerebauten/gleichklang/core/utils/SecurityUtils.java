package de.binaerebauten.gleichklang.core.utils;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.util.UriUtils;

import java.io.UnsupportedEncodingException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

public class SecurityUtils
{
	public static final String DEFAULT_ENCODING = "UTF-8";
	private static final String HASH_ALGORITHM = "SHA-256";

	private static final Logger LOG = LoggerFactory.getLogger(SecurityUtils.class);

	/**
	 * Encodes string to UTF-8 it is necessary for correct URL encoding
	 * @param stringToEncode
	 * @return
	 */
	public static String encodeString(String stringToEncode){
		try
		{
			return UriUtils.encode(stringToEncode, DEFAULT_ENCODING);
		}
		catch (UnsupportedEncodingException e)
		{
			LOG.error("Encoding error. Couldn't encode {} ", stringToEncode, e);
		}
		return stringToEncode;
	}

	/**
	 * Decodes string from UTF-8 it is necessary for correct URL encoding
	 * @param stringToDecode
	 * @return
	 */
	public static String decodeString(String stringToDecode){
		try
		{
			return UriUtils.decode(stringToDecode, DEFAULT_ENCODING);
		}
		catch (UnsupportedEncodingException e)
		{
			LOG.error("Decoding error. Couldn't decode {} ", stringToDecode, e);
		}
		return stringToDecode;
	}

	/**
	 * Creates digest of the given message using {@value HASH_ALGORITHM}
	 * @param message message to encode
	 * @param salt
	 * @return digest as HEX string
	 */
	public static String getMessageDigest(String message, String salt)
	{
		message += salt;
		StringBuilder tokenBuilder =  new StringBuilder();
		try
		{
			MessageDigest messageDigest = MessageDigest.getInstance(HASH_ALGORITHM);
			messageDigest.update(message.getBytes());

			byte byteData[] = messageDigest.digest();
			//convert the byte to hex format
			for (byte aByteData : byteData)
			{
				String hex = Integer.toHexString(0xff & aByteData);
				if (hex.length() == 1){
					tokenBuilder.append('0');
				}
				tokenBuilder.append(hex);
			}

		}
		catch (NoSuchAlgorithmException e)
		{
			LOG.error("Algorithm {} doesn't exists", HASH_ALGORITHM, e);
		}
		return tokenBuilder.toString();
	}

	public static String generateTempPassword(String email, String salt)
	{
		String token = getMessageDigest(email, salt);
		return token.substring(0, 10);
	}
}
