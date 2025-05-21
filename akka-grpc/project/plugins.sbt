resolvers += "Akka library repository".at("https://repo.akka.io/maven")

addSbtPlugin("com.lightbend.akka.grpc" % "sbt-akka-grpc" % "2.5.5")
addSbtPlugin("com.lightbend.sbt" % "sbt-javaagent" % "0.1.6")
