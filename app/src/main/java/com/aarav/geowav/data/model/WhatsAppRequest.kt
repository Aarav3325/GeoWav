package com.aarav.geowav.data.model

data class TemplateMessageRequest(
    val messaging_product: String,
    val to: String,
    val type: String,
    val template: Template
)

data class Template(
    val name: String,
    val language: Language,
    val components: List<Component>
)

data class Language(
    val code: String
)

data class Component(
    val type: String,
    val parameters: List<Parameter>
)

data class Parameter(
    val type: String,
    val parameter_name: String,
    val text: String
)

data class WhatsAppMessageResponse(
    val messaging_product: String,
    val contacts: List<Contact>,
    val messages: List<Message>
)

data class Contact(
    val input: String,
    val wa_id: String
)

data class Message(
    val id: String,
    val message_status: String
)

