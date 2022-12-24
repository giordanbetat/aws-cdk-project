package com.awscdk.stack

import software.amazon.awscdk.*
import software.amazon.awscdk.services.applicationautoscaling.EnableScalingProps
import software.amazon.awscdk.services.ecs.*
import software.amazon.awscdk.services.ecs.patterns.ApplicationLoadBalancedFargateService
import software.amazon.awscdk.services.ecs.patterns.ApplicationLoadBalancedTaskImageOptions
import software.amazon.awscdk.services.elasticloadbalancingv2.HealthCheck
import software.amazon.awscdk.services.events.targets.SnsTopic
import software.amazon.awscdk.services.logs.LogGroup
import software.constructs.Construct


class Service01Config(scope: Construct, id: String, props: StackProps?, cluster: Cluster, exampleTopic: SnsTopic) :
    Stack(scope, id, props) {
    constructor(scope: Construct, id: String, cluster: Cluster, exampleTopic: SnsTopic) : this(scope, id, null, cluster, exampleTopic)

    init {
        val envVariables: MutableMap<String, String> = HashMap()
        envVariables["SPRING_DATASOURCE_URL"] = ("jdbc:mariadb://" + Fn.importValue("rds-endpoint")
                + ":3306/aws_project01?createDatabaseIfNotExist=true")
        envVariables["SPRING_DATASOURCE_USERNAME"] = "admin"
        envVariables["SPRING_DATASOURCE_PASSWORD"] = Fn.importValue("rds-password")
        envVariables["AWS_REGION"] = "us-east-1";
        envVariables["AWS_SNS_TOPIC_PRODUCT_EVENTS_ARN"] = exampleTopic.topic.topicArn
        val service01 = ApplicationLoadBalancedFargateService.Builder.create(this, "ALB01")
            .serviceName("service-01")
            .cluster(cluster)
            .cpu(512)
            .memoryLimitMiB(1024)
            .desiredCount(2)
            .listenerPort(8080)
            .taskImageOptions(
                ApplicationLoadBalancedTaskImageOptions.builder()
                    .containerName("aws_project01")
                    .image(ContainerImage.fromRegistry("aws/aws_cdk_project01:1.1.0"))
                    .containerPort(8080)
                    .logDriver(
                        LogDriver.awsLogs(
                            AwsLogDriverProps.builder()
                                .logGroup(
                                    LogGroup.Builder.create(this, "Service01LogGroup")
                                        .logGroupName("Service01")
                                        .removalPolicy(RemovalPolicy.DESTROY)
                                        .build()
                                )
                                .streamPrefix("Service01")
                                .build()
                        )
                    )
                    .environment(envVariables)
                    .build()
            )
            .publicLoadBalancer(true)
            .build()
        service01.targetGroup.configureHealthCheck(
            HealthCheck.Builder()
                .path("/actuator/health")
                .port("8080")
                .healthyHttpCodes("200")
                .build()
        )
        val scalableTaskCount = service01.service.autoScaleTaskCount(
            EnableScalingProps.builder()
                .minCapacity(2)
                .maxCapacity(4)
                .build()
        )

        scalableTaskCount.scaleOnCpuUtilization(
            "Service01AutoScaling", CpuUtilizationScalingProps.builder()
                .targetUtilizationPercent(50)
                .scaleInCooldown(Duration.seconds(60))
                .scaleOutCooldown(Duration.seconds(60))
                .build()
        )

        exampleTopic.topic.grantPublish(service01.taskDefinition.taskRole)
    }
}