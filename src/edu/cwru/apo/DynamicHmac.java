package edu.cwru.apo;

import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;

public class DynamicHmac extends Hmac {
	
	private int counter = 0;
	private int increment = 0;
	
	public DynamicHmac()
	{
		SecureRandom random = new SecureRandom();
		secretKey = new byte[defaultKeyLength];
		random.nextBytes(secretKey);
	}
	
	public DynamicHmac(Hex secretKey, int counter, int increment)
	{
		this.secretKey = secretKey.toBytes();
		this.counter = counter;
		this.increment = increment;
	}
	
	public DynamicHmac(String mode, int keyLength)
	{
		SecureRandom random = new SecureRandom();
		secretKey = new byte[keyLength];
		random.nextBytes(secretKey);
		this.mode = mode;
	}
	
	public int getCounter()
	{
		return counter;
	}
	
	public int getIncrement()
	{
		return increment;
	}
	
	public void setCounter(int count)
	{
		counter = count;
	}
	
	public void setIncrement(int inc)
	{
		increment = inc;
	}
	
	public Hex generate(String data) throws InvalidKeyException,NoSuchAlgorithmException
	{
		if(secretKey == null)
			return null;
		SecretKeySpec sk = new SecretKeySpec(HOTP(), mode);
		Mac mac;
		mac = Mac.getInstance(mode);
	    mac.init(sk);
	    return new Hex(mac.doFinal(data.getBytes()));
	}
	
	private byte[] HOTP()
	{
		
		try {
			counter += increment;						//increments the counter to the next value
			if(secretKey == null)
				return null;
			SecretKeySpec sk = new SecretKeySpec(secretKey, mode);
			Mac mac;
			mac = Mac.getInstance(mode);
		    mac.init(sk);
		    
		    //byte[] hmac_result =  mac.doFinal(intToBytes(counter));
		    byte[] hmac_result =  mac.doFinal((""+counter).getBytes());

		    // make sure the index will be inbounds
		    if(hmac_result.length < 19)
		    	return null;
		    
		    // get the last 4 bits of the 19th byte of the hmac_result
		    // this acts as an offset
		    int offset = (int)(hmac_result[19] & 0xf); 
		    
		    // get the binary code
		    int bin_code = (int)((hmac_result[offset] & 0x7f) << 24
		    						| (hmac_result[offset+1] & 0xff) << 16
		    						| (hmac_result[offset+2] & 0xff) << 8
		    						| (hmac_result[offset+3] & 0xff));
		    String secret = new Hex(secretKey).toString();
		    return (""+bin_code).getBytes();
		} catch (InvalidKeyException e) {
			// TODO Auto-generated catch block
			counter -= increment;				//incase something goes wrong, counter will be returned to previous state
			e.printStackTrace();
		} catch (NoSuchAlgorithmException e) {
			// TODO Auto-generated catch block
			counter -= increment;				//incase something goes wrong, counter will be returned to previous state
			e.printStackTrace();
		}
		return null;
	}
	
	private byte[] intToBytes(int input)
	{
	        return new byte[] {
	                (byte)(input >>> 24),
	                (byte)(input >>> 16),
	                (byte)(input >>> 8),
	                (byte)input};
	}

}
