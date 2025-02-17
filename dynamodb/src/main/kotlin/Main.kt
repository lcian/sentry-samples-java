package dev.lcian

import io.sentry.Sentry
import software.amazon.awssdk.services.dynamodb.DynamoDbClient
import software.amazon.awssdk.services.dynamodb.model.AttributeValue
import software.amazon.awssdk.services.dynamodb.model.DynamoDbException
import software.amazon.awssdk.services.dynamodb.model.GetItemRequest
import software.amazon.awssdk.services.dynamodb.model.PutItemRequest
import java.net.URI

fun getItem(
    client: DynamoDbClient,
    table: String,
    key: String,
    value: String,
) {
    val keyToGet = mapOf(key to AttributeValue.builder().s(value).build())

    val request =
        GetItemRequest
            .builder()
            .key(keyToGet)
            .tableName(table)
            .build()

    try {
        val returnedItem = client.getItem(request).item()

        if (returnedItem != null && returnedItem.isNotEmpty()) {
            println("Amazon DynamoDB table attributes: \n")
            for ((k, v) in returnedItem) {
                println("$k: $v")
            }
        } else {
            println("No item found with the key $key!")
        }
    } catch (e: DynamoDbException) {
        Sentry.captureException(e)
    }
}

fun putItem(
    client: DynamoDbClient,
    table: String,
    item: Map<String, AttributeValue>,
) {
    val request =
        PutItemRequest
            .builder()
            .tableName(table)
            .item(item)
            .build()

    try {
        client.putItem(request)
        println("Item successfully inserted into table $table")
    } catch (e: DynamoDbException) {
        Sentry.captureException(e)
    }
}

fun getClient(): DynamoDbClient = DynamoDbClient.builder().endpointOverride(URI.create("http://localhost:8000")).build()

fun main() {
    Sentry.init {
        it.dsn = "https://b9ca97be3ff8f1cef41dffdcb1e5100b@o447951.ingest.us.sentry.io/4508683222843393"
        it.tracesSampleRate = 1.0
        it.isSendDefaultPii = true
        it.isDebug = true
    }
    val client = getClient()
    putItem(client, "orders_table", mapOf("orderId" to AttributeValue.builder().s("12345").build()))
    getItem(client, "orders_table", "orderId", "12345")
}
