<#import "import/classMemberTemplates.ftl" as classMembers/>
//CHECKSTYLE:OFF
package ${currentPackage};

<#if schemaTypesAvailable>
import ${schemaTypesPackage}.*;
</#if>

<#list importPackages as i>
import ${i}.*;
</#list>

<@classMembers.addRootClassAnnotations/>
public class ${className}
{
	private final java.util.Map<String, Object> map;
	public ${className}(java.util.Map<String, Object> map) {
		this.map = map;
	}
	<@classMembers.addVariables className=className variables=variables indent='\t'/>
}
//CHECKSTYLE:ON
