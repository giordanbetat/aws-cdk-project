package com.awscdk.stack

import software.amazon.awscdk.Stack
import software.amazon.awscdk.StackProps
import software.amazon.awscdk.services.ec2.Vpc
import software.constructs.Construct

class VpcConfig @JvmOverloads constructor(scope: Construct, id: String, props: StackProps? = null) :
    Stack(scope, id, props) {
    val vpc: Vpc = Vpc.Builder.create(this, "Vpc01")
        .maxAzs(3)
        .build()
}