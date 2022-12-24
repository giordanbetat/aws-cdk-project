package com.awscdk.stack

import software.amazon.awscdk.Stack
import software.amazon.awscdk.StackProps

import software.constructs.Construct

class AwsCdkConfig @JvmOverloads constructor(scope: Construct, id: String, props: StackProps? = null) :
    Stack(scope, id, props)