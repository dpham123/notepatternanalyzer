package notepatternanalyzer;

import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Properties;

class PropertyAccessObject {
	static String getProperty(String property) throws IOException {
		Properties defaultProps = new Properties();
		FileInputStream in = new FileInputStream("data/config.properties");
		defaultProps.load(in);
		in.close();
		
		return defaultProps.getProperty(property);
	}
	
	static void setProperty(String property, String value) throws IOException {
		Properties prop = new Properties();
		FileOutputStream out = new FileOutputStream("data/config.properties");
		prop.setProperty(property, value);
		prop.store(out, "Note Pattern Analyzer Properties");
		out.close();
	}
}
