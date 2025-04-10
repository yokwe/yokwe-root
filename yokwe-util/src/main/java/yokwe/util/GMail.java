package yokwe.util;

import java.io.File;
import java.util.Map;
import java.util.Properties;
import java.util.TreeMap;

import jakarta.mail.Message;
import jakarta.mail.MessagingException;
import jakarta.mail.PasswordAuthentication;
import jakarta.mail.Session;
import jakarta.mail.Transport;
import jakarta.mail.internet.InternetAddress;
import jakarta.mail.internet.MimeMessage;
import yokwe.util.json.JSON;

public class GMail {
	private static final org.slf4j.Logger logger = yokwe.util.LoggerUtil.getLogger();

	public static final String PATH_DIR = "tmp/gmail";
	
	public static final String DEFAULT_NAME = "DEFAULT";
	
	public static String getPath(String name) {
		return String.format("%s/%s", PATH_DIR, name);
	}
	
	public static GMail load(String name) {
		File file = new File(getPath(name));
		if (!file.canRead()) {
			logger.error("Cannot read file");
			logger.error("  file  {}", file.getPath());
			throw new UnexpectedException("Cannot read file");
		}
		String jsonString = FileUtil.read().file(file);
		return JSON.unmarshal(GMail.class, jsonString);
	}
	public static void save(String name, GMail value) {
		File file = new File(getPath(name));
		String jsonString = JSON.toJSONString(value);
		FileUtil.write().file(file, jsonString);
	}
	
	public String username;
	public String password;
	public String to;
	
	public Map<String, String> config;
	
	public GMail() {
		this.username = null;
		this.password = null;
		this.to       = null;
		this.config   = new TreeMap<>();
	}
	
	@Override
	public String toString() {
		return ToString.withFieldName(this);
	}
	
	public static void sendMessage(GMail gmail, String subject, String text) {
        Properties prop = new Properties();
        gmail.config.forEach((k, v) -> prop.put(k, v));
        
        Session session = Session.getInstance(prop,
            new jakarta.mail.Authenticator() {
                protected PasswordAuthentication getPasswordAuthentication() {
                    return new PasswordAuthentication(gmail.username, gmail.password);
                }
            });

        try {
            Message message = new MimeMessage(session);
            
            message.setFrom(new InternetAddress(gmail.username));
            message.setRecipients(Message.RecipientType.TO, InternetAddress.parse(gmail.to));
            message.setSubject(subject);
            message.setText(text);

            Transport.send(message);

        } catch (MessagingException e) {
			String exceptionName = e.getClass().getSimpleName();
			logger.error("{} {}", exceptionName, e);
			throw new UnexpectedException(exceptionName, e);
        }
	}
	public static void sendMessage(String subject, String text) {
		GMail gmail = GMail.load(DEFAULT_NAME);
		sendMessage(gmail, subject, text);
	}
	
	public static void writeAccountFile(String accountName, String username, String password, String to) {
		GMail account = new GMail();
		
		account.username = username;
		account.password = password;
		account.to       = to;
		
		account.config.put("mail.smtp.host", "smtp.gmail.com");
		account.config.put("mail.smtp.port", "587");
		account.config.put("mail.smtp.auth", "true");
		account.config.put("mail.smtp.starttls.enable", "true");
		
		GMail.save(accountName, account);
		GMail copy = GMail.load(accountName);

		logger.info("toString   {}", account);
		logger.info("copyString {}", copy);
	}

	public static void main(String[] args) {
		logger.info("START");
		
		{
			GMail gmail = GMail.load(DEFAULT_NAME);
			logger.info("gmail {}", gmail);
			logger.info("gmail {}", JSON.toJSONString(gmail));
		}
//	   	GMail.writeAccountFile(GMail.DEFAULT_NAME, "hasegawa.yasuhiro@gmail.com", "YOUR_PASSWORD", "hasegawa.yasuhiro+ubuntu-dev@gmail.com");
//		sendMessage("AAA", "BBB");
		
		logger.info("STOP");		
	}
}