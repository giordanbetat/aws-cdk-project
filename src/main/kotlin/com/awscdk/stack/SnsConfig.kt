package com.awscdk.stack

import software.amazon.awscdk.*
import software.amazon.awscdk.services.ec2.*
import software.amazon.awscdk.services.events.targets.SnsTopic
import software.amazon.awscdk.services.rds.*
import software.amazon.awscdk.services.sns.Topic
import software.amazon.awscdk.services.sns.subscriptions.EmailSubscription
import software.constructs.Construct


class SnsConfig @JvmOverloads constructor(scope: Construct, id: String, props: StackProps? = null) :
    Stack(scope, id, props) {
    val productEventsTopic: SnsTopic

    init {
        productEventsTopic = SnsTopic.Builder.create(
            Topic.Builder.create(this, "ProductEventsTopic")
                .topicName("product-events")
                .build()
        )
            .build()
        productEventsTopic.topic.addSubscription(
            EmailSubscription.Builder.create("siecola@gmail.com")
                .json(true)
                .build()
        )
    }
}