
<#macro type t>
<#switch t.class.simpleName>
<#case 'TypeName'>${t.name}<#break>
<#case 'NonNullType'><@type t.type/>!<#break>
<#case 'ListType'>[<@type t.type/>]<#break>
</#switch>
</#macro>

<#macro value v>
<#switch v.class.simpleName>
<#case 'ArrayValue'>[<#list v.values as c><@value c/><#sep> </#list>]<#break>
<#case 'EnumValue'>${v.name}<#break>
<#case 'NullValue'>null<#break>
<#case 'ObjectValue'>{<#list v.objectFields as c>${c.name}: <@value c.value/><#sep> </#list>}<#break>
<#case 'VariableReference'>$${v.name}<#break>
<#case 'BooleanValue'>${v.value?c}<#break>
<#case 'FloatValue'>${v.value?c}<#break>
<#case 'IntValue'>${v.value?c}<#break>
<#case 'StringValue'>"""${v.value}"""<#break>
<#default>${v}
</#switch>
</#macro>

<#macro var v><#list v>(<#items as i>$${i.name}: <@type i.type/><#if i.defaultValue??> = <@value i.defaultValue/></#if><#sep> </#items>)</#list></#macro>

<#macro arg v><#list v>(<#items as i>${i.name}: <@value i.value/><#sep> </#items>)</#list></#macro>

<#macro dir v><#list v> <#items as i>@${i.name}<@arg i.arguments/><#sep> </#items></#list></#macro>

<#macro selection sel indent>
	<#switch sel.class.simpleName>
		<#case 'Field'>
			<#lt/>${indent}<#if sel.alias??>${sel.alias}: </#if>${sel.name}<@arg sel.arguments/><@dir sel.directives/><#if sel.selectionSet??><@selection_set sel=sel.selectionSet indent=indent/></#if>
		<#break>
		<#case 'InlineFragment'>
			<#lt/>${indent}...on ${sel.typeCondition.name}<@dir sel.directives/><@selection_set sel=sel.selectionSet indent=indent/>
		<#break>
		<#case 'FragmentSpread'>
			<#lt/>${indent}...${sel.name}<@dir sel.directives/>
		<#break>
	</#switch>
</#macro>

<#macro selection_set sel indent>
	<#if 0 < sel.selections?size>
		<#lt/> {
		<#list sel.selections as s>
			<@selection sel=s indent=indent+'\t'/>
		</#list>
		<#lt/>${indent}}<#rt/>
	</#if>
</#macro>

${o.operation?lower_case} <#if o.name??>${o.name}</#if><@var o.variableDefinitions/><@dir o.directives/><@selection_set sel=o.selectionSet indent=''/>
<#list fragments as f>
fragment ${f.name} on ${f.typeCondition.name}<@dir f.directives/><@selection_set sel=f.selectionSet indent=''/>
</#list>
