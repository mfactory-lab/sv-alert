lazy val sbtBuildInfoPluginVersion = "0.10.0"
lazy val sbtNativePackagerPluginVersion = "1.9.2"
lazy val wartRemoverPluginVersion = "2.4.16"


addSbtPlugin("com.eed3si9n" % "sbt-buildinfo" % sbtBuildInfoPluginVersion)

addDependencyTreePlugin

addSbtPlugin("com.github.sbt" % "sbt-native-packager" % sbtNativePackagerPluginVersion)

addSbtPlugin("org.wartremover" % "sbt-wartremover" % wartRemoverPluginVersion)
