package com.iksgmbh.data;

import java.text.CharacterIterator;
import java.text.StringCharacterIterator;
import java.util.HashMap;
import java.util.Map;

import com.iksgmbh.utils.StringUtil;

public class ClassNameData {
	
	private static Map<String, String> KNOWN_CLASSES = new HashMap<String, String>();
	static { // these classes can be instantiated by their simple name
		KNOWN_CLASSES.put("BigDecimal", "java.math.BigDecimal");
		KNOWN_CLASSES.put("DateTime", "org.joda.time.DateTime");
		KNOWN_CLASSES.put("List", "java.util.List");
		KNOWN_CLASSES.put("String", "java.lang.String");
	}
	
	private String simpleClassName;
	private String packageName;
	private String fullyQualifiedClassname;
	
	
	public ClassNameData(String classname) {
		classname = classname.trim();
		classname = makeNameFullyQualifiedNameIfsimple(classname);
		
		if (! isFullyQualifiedClassnameValid(classname)) {
			throw new IllegalArgumentException("Not a valid fully qualified class name: <" 
					+ classname + ">"); 
		}
		
		final int pos = classname.lastIndexOf('.');
		packageName = classname.substring(0, pos);
		simpleClassName = classname.substring(pos + 1);
		fullyQualifiedClassname = classname;
		KNOWN_CLASSES.put(simpleClassName, fullyQualifiedClassname);  // dynamically growing knowledge
	}

	public String getSimpleClassName() {
		return simpleClassName;
	}

	public String getFullyQualifiedClassname() {
		return fullyQualifiedClassname;
	}

	public String getPackageName() {
		return packageName;
	}

	
	@Override
	public String toString() {
		return getFullyQualifiedClassname();
	}
	

	/**
	 * searchs for FullyQualifiedClassname in the list of KNOWN_CLASSES
	 * @param simpleName
	 * @return fullyQualifiedClassname
	 */
	private String makeNameFullyQualifiedNameIfsimple(String simpleName) {
		final String fullyQualifiedClassname = KNOWN_CLASSES.get(simpleName);
		if (fullyQualifiedClassname == null) {
			return simpleName;
		}
		return fullyQualifiedClassname;
	}

	public String getSubdirPackageHierarchy() {
		return getPackageName().replace('.', '/');
	}	

	public static boolean isFullyQualifiedClassnameValid(final String classname) {
	      if (classname == null) return false;
	      if (classname.trim().length() == 0) return false;
	      if (classname.endsWith(".")) return false;
	      if (classname.indexOf(".") == -1) return false;
	      
	      final String[] parts = classname.split("[\\.]");
	      if (parts.length == 0) return false;
	      String packageName = "";
	      for (int i = 0; i < parts.length; i++) {
	          
	    	  final CharacterIterator iter = new StringCharacterIterator(parts[i]);
	          // Check first character (there should at least be one character for each part) ...
	          char c = iter.first();
	          if (c == CharacterIterator.DONE) return false;
	          if (!Character.isJavaIdentifierStart(c) && !Character.isIdentifierIgnorable(c)) return false;
	          c = iter.next();
	          // Check the remaining characters, if there are any ...
	          while (c != CharacterIterator.DONE) {
	              if (!Character.isJavaIdentifierPart(c) && !Character.isIdentifierIgnorable(c)) return false;
	              c = iter.next();
	          }
	          
	          if (i < parts.length - 1) {
	    	      if (StringUtil.startsWithUpperCase(parts[i])) {
	    	    	  return false;
	    	      }
	    	      packageName += "." + parts[i];
	          }
	      }
	      
	      packageName = packageName.substring(1);
	      final String simpleClassName = parts[parts.length-1];
	      if (StringUtil.startsWithLowerCase(simpleClassName)) {
	    	  return false;
	      }
	      
	      return true;
	  }

}

