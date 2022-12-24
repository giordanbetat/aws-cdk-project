package com.awscdk.stack

import software.amazon.awscdk.*
import software.amazon.awscdk.services.ec2.*
import software.amazon.awscdk.services.rds.*
import software.constructs.Construct

class RdsConfig (scope: Construct, id: String, props: StackProps?, vpc: Vpc) : Stack(scope, id, props) {
    constructor(scope: Construct, id: String, vpc: Vpc) : this(scope, id, null, vpc)

    init {
        val databasePassword = CfnParameter.Builder.create(this, "databasePassword")
            .type("String")
            .description("The RDS instance password")
            .build()
        val iSecurityGroup = SecurityGroup.fromSecurityGroupId(this, id, vpc.vpcDefaultSecurityGroup)
        iSecurityGroup.addIngressRule(Peer.anyIpv4(), Port.tcp(3306))
        val databaseInstance = DatabaseInstance.Builder
            .create(this, "Rds01")
            .instanceIdentifier("aws-project01-db")
            .engine(
                DatabaseInstanceEngine.mysql(
                    MySqlInstanceEngineProps.builder()
                        .version(MysqlEngineVersion.VER_5_7)
                        .build()
                )
            )
            .vpc(vpc)
            .credentials(
                Credentials.fromUsername(
                    "admin",
                    CredentialsFromUsernameOptions.builder()
                        .password(SecretValue.unsafePlainText(databasePassword.valueAsString))
                        .build()
                )
            )
            .instanceType(InstanceType.of(InstanceClass.BURSTABLE2, InstanceSize.MICRO))
            .multiAz(false)
            .allocatedStorage(10)
            .securityGroups(listOf(iSecurityGroup))
            .vpcSubnets(
                SubnetSelection.builder()
                    .subnets(vpc.privateSubnets)
                    .build()
            )
            .build()
        CfnOutput.Builder.create(this, "rds-endpoint")
            .exportName("rds-endpoint")
            .value(databaseInstance.dbInstanceEndpointAddress)
            .build()
        CfnOutput.Builder.create(this, "rds-password")
            .exportName("rds-password")
            .value(databasePassword.valueAsString)
            .build()
    }
}