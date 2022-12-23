package com.awscdk.stack

import software.amazon.awscdk.Stack
import software.amazon.awscdk.StackProps
import software.amazon.awscdk.services.ec2.Vpc
import software.amazon.awscdk.services.ecs.Cluster
import software.constructs.Construct

class ClusterStackConfig(scope: Construct, id: String, props: StackProps?, vpc: Vpc) :
    Stack(scope, id, props) {
    val cluster: Cluster

    constructor(scope: Construct, id: String, vpc: Vpc) : this(scope, id, null, vpc)

    init {
        cluster = Cluster.Builder.create(this, id)
            .clusterName("cluster-01")
            .vpc(vpc)
            .build()
    }
}