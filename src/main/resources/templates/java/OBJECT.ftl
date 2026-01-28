<#import "import/classMemberTemplates.ftl" as classMembers/>
//CHECKSTYLE:OFF
package ${currentPackage};

<#list importPackages as i>
import ${i}.*;
</#list>

<@classMembers.addJavadoc javadoc=javadoc indent=''/>
<@classMembers.addRootClassAnnotations/>
public class ${className}
<#if parents?has_content>
implements ${parents?join(", ")}
</#if>
{
	<@classMembers.addFields className=name fields=fields indent='\t'/>
	<@classMembers.addEquals className=name fields=fields indent='\t'/>
	<@classMembers.addHashCode fields=fields indent='\t'/>
	<@classMembers.addToString fields=fields indent='\t'/>
	<#if dtoConstructor>
		<@classMembers.addConstructor className=name fields=fields indent='\t'/>
	</#if>
	<#if dtoBuilder>
		<@classMembers.addBuilderMethod builderClassName='Builder' indent='\t'/>
		<#lt/>	public static class Builder
		<#lt/>	{
		<#lt/>		private Builder() { }
		<@classMembers.addBuilderFields className='Builder' fields=fields indent='\t\t'/>
		<@classMembers.addBuildMethod resultClassName=name fields=fields indent='\t\t'/>
		<#lt/>	}
		<@classMembers.addToBuilderMethod builderClassName='Builder' fields=fields indent='\t'/>
	</#if>
}
//CHECKSTYLE:ON
