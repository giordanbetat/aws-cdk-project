package com.awscdk.stack

import software.amazon.awscdk.Duration
import software.amazon.awscdk.RemovalPolicy
import software.amazon.awscdk.Stack
import software.amazon.awscdk.StackProps
import software.amazon.awscdk.services.applicationautoscaling.EnableScalingProps
import software.amazon.awscdk.services.dynamodb.Table
import software.amazon.awscdk.services.ecs.*
import software.amazon.awscdk.services.ecs.patterns.ApplicationLoadBalancedFargateService
import software.amazon.awscdk.services.ecs.patterns.ApplicationLoadBalancedTaskImageOptions
import software.amazon.awscdk.services.elasticloadbalancingv2.HealthCheck
import software.amazon.awscdk.services.events.targets.SnsTopic
import software.amazon.awscdk.services.logs.LogGroup
import software.amazon.awscdk.services.sns.subscriptions.SqsSubscription
import software.amazon.awscdk.services.sqs.DeadLetterQueue
import software.amazon.awscdk.services.sqs.Queue
import software.constructs.Construct


class Service02Config(
    scope: Construct,
    id: String,
    props: StackProps?,
    cluster: Cluster,
    exampleTopic: SnsTopic,
    exampleTable: Table
) :
    Stack(scope, id, props) {
    constructor(scope: Construct, id: String, cluster: Cluster, exampleTopic: SnsTopic, exampleTable: Table) :
            this(scope, id, null, cluster, exampleTopic, exampleTable)

    init {

        val exampleTopicDlq = Queue.Builder.create(this, "ProductEventsDql")
            .queueName("product-events-dlq")
            .build()

        val deadLetterQueue = DeadLetterQueue
            .builder()
            .queue(exampleTopicDlq)
            .maxReceiveCount(3)
            .build()

        val exampleTopicQueue = Queue.Builder.create(this, "ProductEvents")
            .queueName("product-events")
            .deadLetterQueue(deadLetterQueue)
            .build()

        val sqsSubscription = SqsSubscription.Builder.create(exampleTopicQueue).build()
        exampleTopic.topic.addSubscription(sqsSubscription)

        val envVariables: MutableMap<String, String> = HashMap()
        envVariables["AWS_REGION"] = "us-east-1";
        envVariables["AWS_SNS_TOPIC_PRODUCT_EVENTS_NAME"] = exampleTopicQueue.queueName

        val service02 = ApplicationLoadBalancedFargateService.Builder.create(this, "ALB02")
            .serviceName("service-02")
            .cluster(cluster)
            .cpu(512)
            .memoryLimitMiB(1024)
            .desiredCount(2)
            .listenerPort(9090)
            .taskImageOptions(
                ApplicationLoadBalancedTaskImageOptions.builder()
                    .containerName("aws_project02")
                    .image(ContainerImage.fromRegistry("aws/aws_cdk_project02:1.1.0"))
                    .containerPort(9090)
                    .logDriver(
                        LogDriver.awsLogs(
                            AwsLogDriverProps.builder()
                                .logGroup(
                                    LogGroup.Builder.create(this, "Service02LogGroup")
                                        .logGroupName("Service02")
                                        .removalPolicy(RemovalPolicy.DESTROY)
                                        .build()
                                )
                                .streamPrefix("Service02")
                                .build()
                        )
                    )
                    .environment(envVariables)
                    .build()
            )
            .publicLoadBalancer(true)
            .build()

        service02.targetGroup.configureHealthCheck(
            HealthCheck.Builder()
                .path("/actuator/health")
                .port("9090")
                .healthyHttpCodes("200")
                .build()
        )

        val scalableTaskCount = service02.service.autoScaleTaskCount(
            EnableScalingProps.builder()
                .minCapacity(2)
                .maxCapacity(4)
                .build()
        )

        scalableTaskCount.scaleOnCpuUtilization(
            "Service02AutoScaling", CpuUtilizationScalingProps.builder()
                .targetUtilizationPercent(50)
                .scaleInCooldown(Duration.seconds(60))
                .scaleOutCooldown(Duration.seconds(60))
                .build()
        )

        exampleTopicQueue.grantConsumeMessages(service02.taskDefinition.taskRole)
        exampleTable.grantReadWriteData(service02.taskDefinition.taskRole)
    }
}