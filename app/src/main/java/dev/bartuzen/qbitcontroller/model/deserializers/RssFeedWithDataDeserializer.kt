package dev.bartuzen.qbitcontroller.model.deserializers

import com.fasterxml.jackson.databind.JsonNode
import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import dev.bartuzen.qbitcontroller.model.Article
import dev.bartuzen.qbitcontroller.model.RssFeedWithData
import java.time.ZonedDateTime
import java.time.format.DateTimeFormatter
import java.util.Locale

fun parseRssFeedWithData(feeds: String, path: List<String>): RssFeedWithData? {
    val mapper = jacksonObjectMapper()
    val node = mapper.readTree(feeds)
    return parseRssFeedWithData(node, path)
}

private fun parseRssFeedWithData(node: JsonNode, path: List<String>): RssFeedWithData? {
    var currentNode = node
    var name = ""
    outer@ for (currentPath in path) {
        for ((key, value) in currentNode.fields()) {
            if (key == currentPath) {
                name = key
                currentNode = value
                continue@outer
            }
        }
        return null
    }

    return RssFeedWithData(
        name = name,
        path = path,
        uid = currentNode["uid"].asText(),
        articles = parseArticles(currentNode["articles"])
    )
}

private fun parseArticles(node: JsonNode): List<Article> {
    val articles = mutableListOf<Article>()

    for (article in node.iterator()) {
        val dateFormatter = DateTimeFormatter.ofPattern("dd MMM yyyy HH:mm:ss Z", Locale.ENGLISH)
        val date = ZonedDateTime.parse(article["date"].asText(), dateFormatter).toEpochSecond()

        articles += Article(
            id = article["id"].asText(),
            title = article["title"].asText(),
            description = article["description"]?.asText(),
            torrentUrl = article["torrentURL"].asText(),
            isRead = article["isRead"]?.asBoolean() ?: false,
            date = date
        )
    }

    articles.sortByDescending { it.date }

    return articles
}
