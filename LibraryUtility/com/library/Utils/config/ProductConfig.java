package com.library.Utils.config;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Properties;

import org.dom4j.Document;
import org.dom4j.DocumentException;
import org.dom4j.Node;
import org.dom4j.io.SAXReader;

import com.myapp.Data.ReadDataFromExcelSheet;

/**
 * 
 * Manages configuration data for automation tests - an instance represents a Product.
 * 
 * @author jyates
 *
 */



public class ProductConfig implements IProductConfig {
	/***
	 * 
	 * The "home directory" of the tests when run from ant or Eclipse.  For example, C:/p4/depot/qa_automation/selenium/main
	 * 
	 */
	public static String BRANCH_ROOT = System.getProperty("user.dir"); // this will be, for example, /p4/depot/qa_automation/selenium/main.
	/***
	 * 
	 * The AUTO_DATAPATH - a list of directories to search for data files during the test.  The default for the product is always added to the end of this list.
	 * 
	 * It will use the AUTO_DATAPATH environment variable, but can be overridden by the auto.datapath java property
	 * 
	 */
	public static String CONFIG_FILE_PATH = System.getProperty("auto.datapath", System.getenv("AUTO_DATAPATH"));
	
	/***
	 * 
	 * Create a ProductConfig for a product whose data is stored undre baseDataDir.
	 * 
	 * @param baseDataDir
	 */
	protected ProductConfig(String baseDataDir) {
		defaultBaseDataDir = baseDataDir;
	}
	
	/***
	 * 
	 * The Planning configuration information instance
	 * 
	 */
	public static final IProductConfig Planning = new PlanningConfig();
	/***
	 * 
	 * The Discovery configuration information instance
	 * 
	 */
	public static final IProductConfig Discovery = new DiscoveryConfig();
	/***
	 * 
	 * The Integration configuration information instance
	 * 
	 */
	public static final IProductConfig Integration = new IntegrationConfig();
	
	public static final IProductConfig DiscoveryLite = new DiscoveryLiteConfig();
	
	public static final IProductConfig DiscoveryVOS = new DiscoveryVOSConfig();
	
	
	// where all the default (i.e. "real" automation data is stored)
	private String defaultBaseDataDir = null;

	// The subdirectories for the various types of data
	private static final String CONFIG_DIR  = "config"  + File.separator;
	private static final String DATA_DIR    = "data"    + File.separator;

	// the default filenames, relative to the base dir
	protected String defaultApplicationFileName = "application";
	protected String defaultUsersFile           = "userdata";
	
	/**
	 * Class to enable chaining of containers (not found in one, check the parent)
	 * 
	 * @author jyates
	 *
	 */
	static abstract class AbstractNamedValueContainer implements INamedValueContainer {
		protected File file = null;
		
		protected AbstractNamedValueContainer(File f, INamedValueContainer aParent) {
			file = f;
			parent = aParent;
		}
		
		private INamedValueContainer parent = null;
		
		/**
		 * get the value of key within this single container
		 * 
		 * @param key - the keep to search for
		 * @return - the value of that value - null if not found.
		 * @throws Exception - bad things happened.
		 */
		abstract protected String _getValue(String key) throws Exception;
		
		/***
		 * 
		 * returns the value of key, going up through the ancestors until a value is found.
		 * 
		 */
		public String getValue(String key) throws Exception {
			String res = _getValue(key);
			if ((res == null) && (parent != null)) {
				res = parent.getValue(key);
			}
			return res;
		}
		
		public String toString() {
			StringBuffer sb = new StringBuffer("VC: " + file.getAbsolutePath());
			if (parent != null) {
				sb.append("\n\t").append(parent.toString());
			}
			return sb.toString();
		}
	}
	
	/***
	 * 
	 * A NamedValueContainer that has a utility method to get User info.
	 * 
	 * TODO: provide easy way to adjust for a cloned company
	 * 
	 * @author jyates
	 *
	 */
	static class  UserContainer implements IUserContainer {
		private INamedValueContainer container = null;
		
		public UserContainer(INamedValueContainer aContainer) {
			container = aContainer;
		}

		public UserLoginInfo getUserLoginInfo(String key) throws Exception {
			return new UserLoginInfo(
					getValue(String.format("%s.login", key)),
					getValue(String.format("%s.password", key)),
					getValue(String.format("%s.company", key))
					);
		}

		@Override
		public String getValue(String key) throws Exception {
			return container.getValue(key);
		}
	}
	
	/***
	 * 
	 * A NamedValueContainer based on a Properties file
	 * 
	 * @author jyates
	 *
	 */
	static class PropertyValueContainer extends AbstractNamedValueContainer {
		private Properties props = null;
		
		public PropertyValueContainer(File aPropertyFile, INamedValueContainer aParent) {
			super(aPropertyFile, aParent);
		}
		
		@Override
		protected String _getValue(String key) throws Exception {
			return getPropertyObject().getProperty(key);
		}

		private Properties getPropertyObject() throws FileNotFoundException, IOException {
			if (props == null) {
				props = new Properties();
				props.load(new FileInputStream(file));
			}
			return props;
		}
	} 
	
	/***
	 * 
	 * A NamedValueContainer based on an Excel file
	 * 
	 * @author jyates
	 *
	 */
	static class ExcelValueContainer  extends AbstractNamedValueContainer {
		private ReadDataFromExcelSheet sheet = null;
		
		public ExcelValueContainer(File anExcelFile, INamedValueContainer aParent) {
			super(anExcelFile, aParent);
		}
		
		@Override
		protected String _getValue(String key) throws Exception{
			String[] k = key.split("\\.", 3);
			if (k.length != 3) 
				throw new Exception("must provide 3 part name");
			return getSheet().getCellData(k[0], k[2], 1); /// changed for fixing error (need to understand later on)
		}

		private ReadDataFromExcelSheet getSheet() throws FileNotFoundException, IOException {
			if (sheet == null) {
				sheet = new ReadDataFromExcelSheet(file.getAbsolutePath());
			}
			return sheet;
		}
	} 
	
	/***
	 * 
	 * A NamedValueContainer based on an Xml file
	 * 
	 * @author jyates
	 *
	 */
	static class XmlValueContainer  extends AbstractNamedValueContainer {
		private Document doc = null;
		
		public XmlValueContainer(File anXmlFile, INamedValueContainer aParent) {
			super(anXmlFile, aParent);
		}
		
		@Override
		protected String _getValue(String key) throws Exception{
			
			Node node =  getDocument().selectSingleNode("//" + key.replace('.', '/'));
			
			return (node == null) ? null : node.getText();
		}

		private Document getDocument() throws DocumentException {
			if (doc == null) {
				SAXReader reader = new SAXReader();
				doc = reader.read(file);
			}
			return doc;
		}
	}
    	
	/***
	 * 
	 * Gets the application.properties type container for this Product.
	 * 
	 * Searches the data path for a xml, excel or properties file named with the defaultApplicationFileName 
	 * 
	 */
	public INamedValueContainer getApplicationValues() throws Exception {
		return getApplicationValuesContainer(defaultApplicationFileName);
	}

	/**
	 * 
	 * Gets the default user container for this product.
	 * 
	 * Looks for the defaultUsersFile.[xslx, xml, properties] along the datapath
	 * 
	 */
	public IUserContainer getUserContainer() throws Exception {
		return getUserContainer(defaultUsersFile);
	}

	
	private HashMap<String, INamedValueContainer> dataContainers = new HashMap<String, INamedValueContainer>();
	/***
	 * 
	 * Gets the named value container with the given filename along the datapath.
	 * 
	 * Looks for xslx, xml or properties file with that name (unless filename already has one of those extensions).
	 * 
	 */
	public INamedValueContainer getDataContainer(String fileName) throws Exception {
		INamedValueContainer res = dataContainers.get(fileName);
		if (res == null) {
			res = ValueContainerFactory.createContainer(new File(DATA_DIR + fileName), null);
			if (res != null) {
				dataContainers.put(fileName, res);
			}
		}
		return res;
	}

	private HashMap<String, IUserContainer> userContainers = new HashMap<String, IUserContainer>();
	/***
	 * 
	 * Gets the user container composed of the files with the filename from the datapath.
	 * 
	 * Looks for xslx, xml or properties file with that name (unless filename already has one of those extensions).
	 * 
	 * It will be cached.
	 * 
	 */
	public IUserContainer getUserContainer(String fileName) throws Exception {
		IUserContainer res = userContainers.get(fileName);
		if (res == null) {
			INamedValueContainer nvc = null;
			List<File> files = findConfigFile(DATA_DIR + fileName, CONFIG_FILE_PATH, new String[] { "xlsx", "properties", "xml"});
			
			if (files.isEmpty())
				throw new FileNotFoundException(fileName);
			
			int i = files.size() - 1;
			nvc =  ValueContainerFactory.createContainer(files.get(i), null); // the last one should be the one in perforce.
			while (i-- > 0) {
				 nvc = ValueContainerFactory.createContainer(files.get(i), res);
			}
			
			res = new UserContainer(nvc); // only the top one need be wrapped
			
			userContainers.put(fileName, res); // cache
		}
		return res;
	}

	
	private HashMap<String, INamedValueContainer> applicationContainers = new HashMap<String, INamedValueContainer>();
	
	/**
	 * 
	 * Gets the value container composed of the files with the filename from the datapath..
	 * 
	 * It will be cached.
	 * 
	 */
	public INamedValueContainer getApplicationValuesContainer(String fileName) throws Exception {
		INamedValueContainer res = applicationContainers.get(fileName);
		if (res == null) {
			List<File> files = findConfigFile(CONFIG_DIR + fileName, CONFIG_FILE_PATH, new String[] { "xlsx", "properties", "xml"});
			
			if (files.isEmpty())
				throw new FileNotFoundException(fileName);
			
			int i = files.size() - 1;
			res =  ValueContainerFactory.createContainer(files.get(i), null); // the last one should be the one in perforce.
			while (i-- > 0) {
				 res = ValueContainerFactory.createContainer(files.get(i), res);
			}
			
			applicationContainers.put(fileName, res); // cache
		}
		return res;
	}
	
	private static final String[] NO_EXTENSION_SEARCH = new String[] {""};
	
	/**
	 * 
	 * Utility function to set up and call finding the config files.
	 * 
	 * The deafultBaseDataDir is ALWAYS added to the path.
	 * 
	 * @param filename - the filename to use
	 * @param path - the datapath string
	 * @param extensions - the extension to check for (use NO_EXTENSION_SEARCH if you want the filename as is).
	 * @return a list of Files along the path that match 
	 */
	private List<File> findConfigFile(String filename, String path, String[] extensions) {
		
		extensions = getExtensionsToCheck(filename, extensions);
		
		List<File> dirPath = extractDirPath(path);
		
		dirPath.add(new File(defaultBaseDataDir)); // always have this there.
		
		return findConfigFileOnPath(filename, extensions, dirPath);
	}
	
	/**
	 * 
	 * gets Files on the datapath matching Data/<filename>.[xml, xsls, properties]
	 * 
	 * @param filename
	 * @return
	 */
	public File getDataFile(String filename) {
		List<File> files = findConfigFile(DATA_DIR + filename, defaultBaseDataDir, null);
		return files.isEmpty() ? null : files.get(0);
	}

	
	/**
	 * 
	 * converts a datapath string to a list of File objects representing the directories therein.
	 * 
	 * Will log if an entry does not exist or is not a directory.
	 * 
	 * @param path - a string of directory names separated by the File.pathSeparator  
	 * @return
	 */
	private List<File> extractDirPath(String path) {
		ArrayList<File> dirPath = new ArrayList<File>();
		if (path != null) {
			for (String dir : path.split(File.pathSeparator)) {
				File d = new File(dir);
				if (d.isDirectory()) {
					dirPath.add(d);
				} else {
					log(d.getAbsolutePath() + " is not a directory or does not exist, ignoring for data file search.");
				}
			}
		}
		return dirPath;
	}

	/**
	 * 
	 * Check the filename to be searched to see if it already ends in one of the desired extensions.  If it does,
	 * return an arrays with the empty string in it.  Otherwise, return an array of extensions to check.
	 * 
	 * @param filename
	 * @param extensions
	 * @return
	 */
	private String[] getExtensionsToCheck(String filename, String[] extensions) {
		String[] extensionsToCheck = (extensions == null) ? NO_EXTENSION_SEARCH : extensions;
		if (extensions != null) {
			for (String ext : extensions) {
				if ((ext.length() > 0) && filename.endsWith(ext)) {
					extensions = NO_EXTENSION_SEARCH;
					break;
				}
			}
		}
		return extensionsToCheck;
	}

	/**
	 * 
	 * scan dirPath for filenames that match <filename>.[extensionsToCheck].
	 * 
	 * @param filename - the filename to match
	 * @param extensionsToCheck - a list of extensions to match (might be [""] to not check any)
	 * @param dirPath - the list of directories to check.
	 * @return
	 */
	private List<File> findConfigFileOnPath(final String filename, final String[] extensionsToCheck, List<File> dirPath) {
		ArrayList<File> files = new ArrayList<File>(dirPath.size());
		for (File dir : dirPath) {
			
			File fileFound = null;
			for (String ext : extensionsToCheck) {
				File fileToCheck = new File(dir, ext.length() > 0 ? String.format("%s.%s", filename, ext) : filename);
				if (fileToCheck.isFile() && fileToCheck.exists()) {
					if (fileFound != null) {
						log(fileToCheck.getAbsolutePath() + " ignored in favor of " + fileFound.getName());
					} else {
						fileFound = fileToCheck;
					}
				}
			}
			
			if (fileFound != null) {
				files.add(fileFound);
			}
		}
		return files;
	}
	
	/**
	 * 
	 * TODO: figure out where to put these.
	 * 
	 * @param string
	 */
	private void log(String string) {
		System.err.println(string);
	}

	
	/***
	 * 
	 * Create a NamedValueContainer of the appropriate type based on the extension in the filename.
	 * 
	 * @author jyates
	 *
	 */
	protected static class ValueContainerFactory {
		public static INamedValueContainer createContainer(File containerFile, INamedValueContainer parent) throws Exception {
			if (!containerFile.exists()) {
				throw new FileNotFoundException(containerFile.getCanonicalPath());
			}
			INamedValueContainer res = null;
			String fn = containerFile.getName();
			if (fn.endsWith(".xlsx")) {
				res = new ExcelValueContainer(containerFile, parent);
			} else if (fn.endsWith(".xml")) {
				res = new XmlValueContainer(containerFile, parent);
			} else if (fn.endsWith(".properties")) {
				res = new PropertyValueContainer(containerFile, parent);
			}
			return res;
		}
	}
}
