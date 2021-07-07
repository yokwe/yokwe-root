package yokwe.util.xml;

import java.io.Reader;

import jakarta.xml.bind.JAXBContext;
import jakarta.xml.bind.JAXBException;
import jakarta.xml.bind.Unmarshaller;
import jakarta.xml.bind.ValidationEvent;
import jakarta.xml.bind.ValidationEventHandler;
import jakarta.xml.bind.ValidationEventLocator;
import yokwe.util.UnexpectedException;

public class JAXB {
	private static final org.slf4j.Logger logger = org.slf4j.LoggerFactory.getLogger(JAXB.class);

	private static class MyValidationEventHandler implements ValidationEventHandler {
		@Override
		public boolean handleEvent(ValidationEvent event) {
			int severity = event.getSeverity();
			
			String message = event.getMessage();
			ValidationEventLocator locator = event.getLocator();
			
			switch(severity) {
			case ValidationEvent.WARNING:
				logger.warn("message {}", message);
				logger.warn("location line {} column {}", locator.getLineNumber(), locator.getColumnNumber());
				return true; // continue process
			case ValidationEvent.ERROR:
				logger.error("message error {}", message);
				logger.error("location line {} column {}", locator.getLineNumber(), locator.getColumnNumber());
				return false; // stop process
			case ValidationEvent.FATAL_ERROR:
				logger.error("message fatal {}", message);
				logger.error("location line {} column {}", locator.getLineNumber(), locator.getColumnNumber());
				return false; // stop process
			default:
				logger.error("severity {}", severity);
				throw new UnexpectedException("Unexpected");
			}
		}
	}
	
	
	public static <T> T unmarshal(Reader reader, Class<T> clazz) {
		try {
			JAXBContext jaxbContext = JAXBContext.newInstance(clazz);
			Unmarshaller unmarshaller = jaxbContext.createUnmarshaller();
			unmarshaller.setEventHandler(new MyValidationEventHandler());
			Object o = unmarshaller.unmarshal(reader);
			@SuppressWarnings("unchecked")
			T ret = (T) o;
			return ret;
		} catch (JAXBException e) {
			String exceptionName = e.getClass().getSimpleName();
			logger.error("{} {}", exceptionName, e);
			throw new UnexpectedException(exceptionName, e);
		}
	}

}
