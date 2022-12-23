
import com.awscdk.stack.ClusterStackConfig
import com.awscdk.stack.Service01StackConfig
import com.awscdk.stack.VpcStackConfig
import software.amazon.awscdk.App


fun main(args: Array<String>) {
    val app = App()

    val vpcStack = VpcStackConfig(app, "Vpc")

    val clusterStack = ClusterStackConfig(app, "Cluster", vpcStack.vpc)
    clusterStack.addDependency(vpcStack)

    val service01Stack = Service01StackConfig(app, "Service01", clusterStack.cluster)
    service01Stack.addDependency(clusterStack)

    app.synth()
}