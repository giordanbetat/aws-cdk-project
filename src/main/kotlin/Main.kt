
import com.awscdk.stack.ClusterConfig
import com.awscdk.stack.RdsConfig
import com.awscdk.stack.Service01Config
import com.awscdk.stack.VpcConfig
import software.amazon.awscdk.App


fun main(args: Array<String>) {
    val app = App()

    val vpcConfig = VpcConfig(app, "Vpc")

    val clusterConfig = ClusterConfig(app, "Cluster", vpcConfig.vpc)
    clusterConfig.addDependency(vpcConfig)

    val rdsConfig = RdsConfig(app, "Rds", vpcConfig.vpc)
    rdsConfig.addDependency(vpcConfig)

    val service01Config = Service01Config(app, "Service01", clusterConfig.cluster)
    service01Config.addDependency(clusterConfig)
    service01Config.addDependency(rdsConfig)

    app.synth()
}