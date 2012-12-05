@TargetFileName ${classDescriptor.simpleName}Builder.java # Name of output file with extension but without path 
@TargetDir C:/Temp/MogliCodeCreator/_Demo/src/main/java/<package>
@CreateNew true # creates target dir if not existing and overwrites target file if existing

package ${classDescriptor.package};
'
#parse("commonSubtemplates/B_ImportStatements.tpl")
'
public class ${classDescriptor.simpleName}Builder {
'
'	private ${classDescriptor.simpleName} toBuild;
'
'	public ${classDescriptor.simpleName}Builder(final ${classDescriptor.simpleName} data) {
'		this.toBuild = clone${classDescriptor.simpleName}(data);
'	}
'
'	public ${classDescriptor.simpleName}Builder() {
'		this.toBuild = new ${classDescriptor.simpleName}();
'	}
'
'	public ${classDescriptor.simpleName} build() {
'		return clone${classDescriptor.simpleName}(toBuild);
'	}
'
'	public ${classDescriptor.simpleName} buildAndValidate() {
'		final ${classDescriptor.simpleName} toReturn = clone${classDescriptor.simpleName}(toBuild);
'		try {
'			${classDescriptor.simpleName}Validator.doYourJob(toReturn);
'		} catch (Exception e) {
'			e.printStackTrace();
'		}
'		return toReturn;
'	}
'	
'	#parse("C_withMethods.tpl")
'	
'	#parse("D_cloneWithMethods.tpl")
'	
'	#parse("E_cloneDataObjectMethod.tpl")
}