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
	private final StringBuilder documentBuilder;
	public ${className}(StringBuilder documentBuilder) {
		this.documentBuilder = documentBuilder;
	}
	<#list selections as typeName, fields>
		<#if typeName?has_content>
			<#lt/>	public ${className} on${typeName}(java.util.function.Consumer<${typeName}Selector> selector) {
			<#lt/>		if (selector != null) {
			<#lt/>			documentBuilder.append(" ...on ${typeName} {");
			<#lt/>			selector.accept(new ${typeName}Selector());
			<#lt/>			documentBuilder.append(" }");
			<#lt/>		}
			<#lt/>		return this;
			<#lt/>	}
			<#lt/>	public class ${typeName}Selector {
			<@classMembers.addSelectionFields className=typeName+'Selector' fields=fields indent='\t\t'/>
			<#lt/>	}
		<#else>
			<@classMembers.addSelectionFields className=className fields=fields indent='\t'/>
		</#if>
	</#list>
}
//CHECKSTYLE:ON
