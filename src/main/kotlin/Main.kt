
import com.awscdk.stack.*
import software.amazon.awscdk.App


fun main() {
    val app = App()

    val vpcConfig = VpcConfig(app, "Vpc")

    val clusterConfig = ClusterConfig(app, "Cluster", vpcConfig.vpc)
    clusterConfig.addDependency(vpcConfig)

    val rdsConfig = RdsConfig(app, "Rds", vpcConfig.vpc)
    rdsConfig.addDependency(vpcConfig)

    val snsConfig = SnsConfig(app, "Sns")

    val service01Config = Service01Config(app, "Service01", clusterConfig.cluster, snsConfig.exampleTopic)
    service01Config.addDependency(clusterConfig)
    service01Config.addDependency(rdsConfig)
    service01Config.addDependency(snsConfig)

    val service02Config = Service02Config(app, "Service02", clusterConfig.cluster, snsConfig.exampleTopic)
    service02Config.addDependency(clusterConfig)
    service02Config.addDependency(snsConfig)

    app.synth()
}