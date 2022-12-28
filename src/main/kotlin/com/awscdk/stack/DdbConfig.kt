package com.awscdk.stack

import software.amazon.awscdk.RemovalPolicy
import software.amazon.awscdk.Stack
import software.amazon.awscdk.StackProps
import software.amazon.awscdk.services.dynamodb.Attribute
import software.amazon.awscdk.services.dynamodb.AttributeType
import software.amazon.awscdk.services.dynamodb.BillingMode
import software.amazon.awscdk.services.dynamodb.Table
import software.constructs.Construct


class DdbConfig @JvmOverloads constructor(scope: Construct, id: String, props: StackProps? = null) :
    Stack(scope, id, props) {
    init {
        Table.Builder
            .create(this, "ExampleDb")
            .tableName("example-table")
            .billingMode(BillingMode.PROVISIONED)
            .readCapacity(1)
            .writeCapacity(1)
            .partitionKey(
                Attribute
                    .builder()
                    .name("pk")
                    .type(AttributeType.STRING)
                    .build()
            )
            .sortKey(
                Attribute
                    .builder()
                    .name("sk")
                    .type(AttributeType.STRING)
                    .build()
            )
            .timeToLiveAttribute("ttl")
            .removalPolicy(RemovalPolicy.DESTROY)
            .build()
    }
}