@TargetFileName ${classDescriptor.simpleName}ValidatorUnitTest.java # Name of output file with extension but without path 
@TargetDir C:/Temp/MogliCodeCreator/_Demo/src/test/java/<package>
@CreateNew true # creates target dir if not existing and overwrites target file if existing

package ${classDescriptor.package};
'
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;
'
import org.junit.Before;
import org.junit.Test;
'
public class ${classDescriptor.simpleName}ValidatorUnitTest {
'
'	private ${classDescriptor.simpleName} testData;
'
	#parse("C_setupMethod.tpl")
'	
'	@Test
'	public void validatesWithoutErrorIfAllFieldsAreSetCorrectly() {
'		${classDescriptor.simpleName}Validator.doYourJob(testData);
'	}
'	
	#parse("D_mandatoryTestMethods.tpl")
'	
	#parse("E_minLengthTestMethods.tpl")
'
	#parse("F_maxLengthTestMethods.tpl")
}