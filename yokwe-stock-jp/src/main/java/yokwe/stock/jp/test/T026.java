package yokwe.security.japan.test;

import java.util.Optional;
import java.util.OptionalDouble;
import java.util.OptionalInt;
import java.util.OptionalLong;

import org.slf4j.LoggerFactory;

public class T026 {
	static final org.slf4j.Logger logger = LoggerFactory.getLogger(T026.class);
	
	public static void main(String[] args) {
		logger.info("START");
		
		logger.info("{}", String.class.getTypeName());
		
		logger.info("{}", Double.class.getTypeName());
		logger.info("{}", Long.class.getTypeName());
		logger.info("{}", Integer.class.getTypeName());
		
		logger.info("{}", Double.TYPE.getTypeName());
		logger.info("{}", Long.TYPE.getTypeName());
		logger.info("{}", Integer.TYPE.getTypeName());

		logger.info("{}", OptionalDouble.class.getTypeName());
		logger.info("{}", OptionalLong.class.getTypeName());
		logger.info("{}", OptionalInt.class.getTypeName());
		
		logger.info("{}", Optional.class.getTypeName());
		
		logger.info("=======");
		Optional<String> os = Optional.empty();
		logger.info("{}", os);
		logger.info("{}", os.getClass().getTypeName());
		
		
		logger.info("STOP");
	}
}
