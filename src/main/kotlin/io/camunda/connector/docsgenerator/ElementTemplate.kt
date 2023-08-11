package io.camunda.connector.docsgenerator

import com.fasterxml.jackson.annotation.JsonInclude

@JsonInclude(JsonInclude.Include.NON_NULL)
data class ElementTemplate(
    val name: String,
    val groups: List<Group>,
    val properties: List<Property>
) {
    private val reservedGroups = listOf("errors", "output")
    fun removeHiddenProperties(): ElementTemplate {
        return copy(properties = properties.filter { it.type != "Hidden" })
    }

    fun removeReservedProperties(): ElementTemplate {
        return copy(groups = groups.filter { !reservedGroups.contains(it.id) })
    }
}


data class Group(
    val id: String,
    val label: String,
)

data class Property(
    val type: String,
    val group: String?,
    val id: String?,
    val label: String?,
    val value: String?,
    val choices: List<Choice>?,
    val condition: Map<String, Any>?
)

data class Choice(
    val name: String,
    val value: String
)
